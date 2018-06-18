package com.example.mohamed.testsignin.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.mohamed.testsignin.R;
import com.example.mohamed.testsignin.model.Post;
import com.example.mohamed.testsignin.ui.PostDetailActivity;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class MyPostsFragment extends Fragment {

    private ListView mPostsListView;
    private ProgressBar mLoadingIndicatorProgressBar;
    private TextView mEmptyListTextView;
    private DatabaseReference mDatabaseReference;
    private FirebaseListAdapter<Post> mFirebaseListAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_posts, container, false);
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        mPostsListView = rootView.findViewById(R.id.list_posts);
        mEmptyListTextView = rootView.findViewById(R.id.tv_empty_post_list);
        mLoadingIndicatorProgressBar = rootView.findViewById(R.id.pb_post_loading_indicator);

        mPostsListView.setEmptyView(mEmptyListTextView);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Query query = mDatabaseReference.child("user-posts").child(uid).limitToLast(50);


        FirebaseListOptions<Post> options = new FirebaseListOptions.Builder<Post>()
                .setQuery(query, Post.class)
                .setLayout(R.layout.post_list_item)
                .build();

        mFirebaseListAdapter = new FirebaseListAdapter<Post>(options) {
            @Override
            protected void populateView(View v, Post post, int position) {
                mLoadingIndicatorProgressBar.setVisibility(View.INVISIBLE);

                TextView title = v.findViewById(R.id.tv_post_title);
                TextView body = v.findViewById(R.id.tv_post_body);
                TextView author = v.findViewById(R.id.tv_post_author);
                ImageView imageView = v.findViewById(R.id.img_post_item_image);

                Log.d("post:", "title: " + post.getTitle());
                title.setText(post.getTitle());
                body.setText(post.getBody());
                author.setText(post.getAuthor());

                if (!TextUtils.isEmpty(post.getFileUri())) {
                    Glide.with(getContext()).load(post.getFileUri()).into(imageView);
                } else {
                    imageView.setVisibility(View.GONE);
                }

            }
        };

        mPostsListView.setAdapter(mFirebaseListAdapter);

        mPostsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DatabaseReference postsReference = mFirebaseListAdapter.getRef(position);
                String postKey = postsReference.getKey();

                Intent intent = new Intent(view.getContext(), PostDetailActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT, postKey);
                startActivity(intent);
            }
        });

        return rootView;
    }

    private boolean isConnected() {

        ConnectivityManager cm =
                (ConnectivityManager)getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = null;
        if (cm != null) {
            activeNetwork = cm.getActiveNetworkInfo();
        }
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }
    @Override
    public void onStart() {
        super.onStart();
        mFirebaseListAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        mFirebaseListAdapter.stopListening();
    }

}
