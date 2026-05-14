package models;

import java.time.LocalDateTime;

public class Comment {
    private int id;
    private int auteurId;        // ✅ lié à la DB
    private int postId;          // ✅ lié au post
    private String author;
    private String content;
    private LocalDateTime createdAt;
    private boolean hidden;

    // ✅ Constructeur ancien (sans DB)
    public Comment(int id, String author, String content) {
        this.id = id;
        this.author = author;
        this.content = content;
        this.createdAt = LocalDateTime.now();
        this.hidden = false;
    }

    // ✅ Constructeur nouveau (avec DB)
    public Comment(int id, int auteurId, int postId,
                   String author, String content, LocalDateTime createdAt) {
        this.id = id;
        this.auteurId = auteurId;
        this.postId = postId;
        this.author = author;
        this.content = content;
        this.createdAt = createdAt;
        this.hidden = false;
    }

    // ✅ Getters & Setters
    public int getId() { return id; }
    public int getAuteurId() { return auteurId; }
    public void setAuteurId(int auteurId) { this.auteurId = auteurId; }
    public int getPostId() { return postId; }
    public void setPostId(int postId) { this.postId = postId; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isHidden() { return hidden; }
    public void setHidden(boolean hidden) { this.hidden = hidden; }
}