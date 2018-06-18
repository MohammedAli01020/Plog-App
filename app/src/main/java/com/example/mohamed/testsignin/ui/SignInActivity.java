package com.example.mohamed.testsignin.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.mohamed.testsignin.R;
import com.example.mohamed.testsignin.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignInActivity extends AppCompatActivity {

    private EditText mEmailField;
    private EditText mPasswordField;
    private Button mSignInButton;
    private Button mSignUpButton;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // Views
        mEmailField = findViewById(R.id.field_email);
        mPasswordField = findViewById(R.id.field_password);
        mSignInButton = findViewById(R.id.button_sign_in);
        mSignUpButton = findViewById(R.id.button_sign_up);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Sign in
                if (!validateForm()) {
                    return;
                }
                String email = mEmailField.getText().toString();
                String password = mPasswordField.getText().toString();

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    onAuthSuccess(user);
                                } else {
                                    Toast.makeText(SignInActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                    onAuthSuccess(null);
                                }
                            }
                        });

            }
        });

        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validateForm()) {
                    return;
                }
                String email = mEmailField.getText().toString();
                String password = mPasswordField.getText().toString();

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    onAuthSuccess(mAuth.getCurrentUser());
                                } else {
                                    Toast.makeText(SignInActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                    onAuthSuccess(null);
                                }
                            }
                        });
            }
        });

    }

    private boolean validateForm() {

        String email = mEmailField.getText().toString();
        String password = mPasswordField.getText().toString();

        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("Required");
            return false;
        } else {
            mEmailField.setError(null);
        }

        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError("Required");
            return false;
        } else {
            mPasswordField.setError(null);
        }

        return true;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Check auth on Activity start
        if (mAuth.getCurrentUser() != null) {
            onAuthSuccess(mAuth.getCurrentUser());
        }
    }

    private void onAuthSuccess(FirebaseUser user) {
        if (user == null) return;
        String username = usernameFromEmail(user.getEmail());

        // Write new user
        writeNewUser(user.getUid(), username, user.getEmail());

        // Go to SignInActivity
        Intent intent = new Intent(SignInActivity.this, MainActivity.class);
        intent.putExtra(Intent.EXTRA_TEXT, username);
        startActivity(intent);
        finish();
    }

    private void writeNewUser(String uid, String username, String email) {
        User user = new User(username, email);
        mDatabaseReference.child("users").child(uid).setValue(user);

    }

    private String usernameFromEmail(String email) {
        if (email.contains("@")) {
            return email.split("@")[0];
        } else {
            return email;
        }
    }

}
