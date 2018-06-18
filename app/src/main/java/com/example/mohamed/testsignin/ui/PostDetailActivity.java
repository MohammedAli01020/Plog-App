package com.example.mohamed.testsignin.ui;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mohamed.testsignin.R;
import com.example.mohamed.testsignin.model.Comment;
import com.example.mohamed.testsignin.model.Post;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


public class PostDetailActivity extends AppCompatActivity {

    private ListView mCommentListView;
    private Button mCommentButton;
    private ProgressBar mLoadingIndicatorProgressBar;
    private TextView mEmptyListTextView;
    private DatabaseReference mDatabaseReference;
    private EditText mCommentText;
    private String mPostKey;
    private FirebaseListAdapter<Comment> mCommentFirebaseListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        mCommentButton = findViewById(R.id.bt_comment);
        mCommentText = findViewById(R.id.et_comment);
        mCommentListView = findViewById(R.id.list_comments);
        mLoadingIndicatorProgressBar = findViewById(R.id.pb_comment_list);
        mEmptyListTextView = findViewById(R.id.tv_empty_comment_list);

        mCommentListView.setEmptyView(mEmptyListTextView);

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        mPostKey = getIntent().getStringExtra(Intent.EXTRA_TEXT);
        if (TextUtils.isEmpty(mPostKey)) {
            throw new IllegalArgumentException("Must pass EXTRA_POST_KEY");
        }

        mCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (TextUtils.isEmpty(mCommentText.getText().toString())) {
                    mCommentButton.setError("required");
                    return;
                }

                Toast.makeText(PostDetailActivity.this, "Commenting...", Toast.LENGTH_SHORT).show();

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String text = mCommentText.getText().toString();

                String author = null;
                Comment comment = null;

                if (user != null) {
                    author = usernameFromEmail(user.getEmail());
                    comment = new Comment(user.getUid(), author, text);
                }

                mDatabaseReference.child("comments").child(mPostKey).push().setValue(comment);
                mCommentText.setText("");
            }
        });

        Query query = mDatabaseReference.child("comments").child(mPostKey).limitToLast(50);

        FirebaseListOptions<Comment> options = new FirebaseListOptions.Builder<Comment>()
                .setQuery(query, Comment.class)
                .setLayout(R.layout.comment_lsit_item)
                .build();

        mCommentFirebaseListAdapter = new FirebaseListAdapter<Comment>(options) {
            @Override
            protected void populateView(View v, Comment comment, int position) {
                TextView author = v.findViewById(R.id.tv_comment_author);
                TextView text = v.findViewById(R.id.tv_comment_text);

                author.setText(comment.getAuthor());
                text.setText(comment.getText());
            }
        };

        mCommentListView.setAdapter(mCommentFirebaseListAdapter);
    }

    private boolean isConnected() {

        ConnectivityManager cm =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = null;
        if (cm != null) {
            activeNetwork = cm.getActiveNetworkInfo();
        }
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    private String usernameFromEmail(String email) {
        if (email.contains("@")) {
            return email.split("@")[0];
        } else {
            return email;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mCommentFirebaseListAdapter.startListening();

        if (TextUtils.isEmpty(mPostKey)) return;
        mDatabaseReference.child("posts").child(mPostKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mLoadingIndicatorProgressBar.setVisibility(View.INVISIBLE);

                Post post = dataSnapshot.getValue(Post.class);
                if (post == null) {
                    return;
                }

                ((TextView) findViewById(R.id.tv_post_title)).setText(post.getTitle());
                ((TextView) findViewById(R.id.tv_post_author)).setText(post.getAuthor());
                ((TextView) findViewById(R.id.tv_post_body)).setText(post.getBody());
                ImageView imageView = findViewById(R.id.img_post_item_image);

                Log.d("fileUri", post.getFileUri() + "");

                if (!TextUtils.isEmpty(post.getFileUri())) {
                    Glide.with(PostDetailActivity.this).load(post.getFileUri()).into(imageView);
                } else {
                    imageView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        mCommentFirebaseListAdapter.stopListening();
    }
}
