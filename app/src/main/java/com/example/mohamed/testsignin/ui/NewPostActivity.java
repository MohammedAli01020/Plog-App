package com.example.mohamed.testsignin.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mohamed.testsignin.R;
import com.example.mohamed.testsignin.model.Post;
import com.example.mohamed.testsignin.model.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class NewPostActivity extends AppCompatActivity {
    private static final String TAG = "NewPostActivity";
    private static final String REQUIRED = "Required";
    private static final int RC_PHOTO_PICKER = 2;

    private EditText mTitleField;
    private EditText mBodyField;
    private ImageView mImageViewButton;
    private ImageView mPostImageView;
    private DatabaseReference mDatabaseReference;
    private StorageReference mStorageReference;
    private Uri mFileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mStorageReference = FirebaseStorage.getInstance().getReference();

        mTitleField = findViewById(R.id.field_title);
        mBodyField = findViewById(R.id.field_body);
        mImageViewButton = findViewById(R.id.img_button);
        mPostImageView = findViewById(R.id.img_post_image);

        mImageViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.new_post, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_post) {
            submitPost();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_PHOTO_PICKER) {
            if (resultCode == RESULT_OK) {
                mFileUri = data.getData();
                Glide.with(NewPostActivity.this).load(mFileUri).into(mPostImageView);
            }
        }
    }

    private void submitPost() {
        if (!validateForm()) {
            return;
        }

        if (mFileUri != null) {
            StorageReference reference = mStorageReference.child("post_photos").child(mFileUri.getLastPathSegment());
            reference.putFile(mFileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    final Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    final String title = mTitleField.getText().toString();
                    final String body = mBodyField.getText().toString();

                    setEditingEnabled(false);
                    Toast.makeText(NewPostActivity.this, "Posting...", Toast.LENGTH_SHORT).show();

                    final String userId = getUid();
                    mDatabaseReference.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);

                            String username = null;
                            if (user != null) {
                                username = user.getUsername();
                            }

                            writeNewPost(userId, username, title, body, downloadUrl.toString());

                            setEditingEnabled(true);
                            finish();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            setEditingEnabled(true);

                        }
                    });
                }
            });
        } else {

            final String title = mTitleField.getText().toString();
            final String body = mBodyField.getText().toString();

            setEditingEnabled(false);
            Toast.makeText(NewPostActivity.this, "Posting...", Toast.LENGTH_SHORT).show();

            final String userId = getUid();
            mDatabaseReference.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);

                    String username = null;
                    if (user != null) {
                        username = user.getUsername();
                    }

                    writeNewPost(userId, username, title, body, null);

                    setEditingEnabled(true);
                    finish();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    setEditingEnabled(true);
                }
            });
        }
    }

    private void writeNewPost(String userId, String username, String title, String body, String fileUri) {

        String key = mDatabaseReference.child("posts").push().getKey();
        Post post = new Post(userId, username, title, body, fileUri);

        Map<String, Object> childUpdates = new HashMap<>();

        childUpdates.put("/posts/" + key, post);
        childUpdates.put("/user-posts/" + userId + "/" + key, post);

        mDatabaseReference.updateChildren(childUpdates);
    }

    private void setEditingEnabled(boolean enabled) {
        mTitleField.setEnabled(enabled);
        mBodyField.setEnabled(enabled);
        /*
        if (enabled) {
            mSubmitButton.setVisibility(View.VISIBLE);
        } else {
            mSubmitButton.setVisibility(View.GONE);
        }
        */
    }

    private boolean validateForm() {
        String title = mBodyField.getText().toString();
        String body = mBodyField.getText().toString();

        if (TextUtils.isEmpty(title)) {
            mBodyField.setError(REQUIRED);
            return false;
        } else {
            mBodyField.setError(null);
        }

        if (TextUtils.isEmpty(body)) {
            mBodyField.setError(REQUIRED);
            return false;
        } else {
            mBodyField.setError(null);
        }
        return true;
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }
}
