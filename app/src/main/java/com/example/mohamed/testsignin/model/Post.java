package com.example.mohamed.testsignin.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

// [START post_class]
@IgnoreExtraProperties
public class Post {

    private String uid;
    private String author;
    private String title;
    private String body;
    private String fileUri;

    public Post() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public Post(String uid, String author, String title, String body, String fileUri) {
        this.uid = uid;
        this.author = author;
        this.title = title;
        this.body = body;
        this.fileUri = fileUri;
    }

    public String getUid() {
        return uid;
    }

    public String getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getFileUri() {
        return fileUri;
    }

    // [START post_to_map]
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("author", author);
        result.put("title", title);
        result.put("body", body);
        result.put("fileUri", fileUri);

        return result;
    }
    // [END post_to_map]

}
// [END post_class]
