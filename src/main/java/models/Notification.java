package models;

import java.time.LocalDateTime;

public class Notification {
    private int id;
    private int userId;
    private int fromUserId;
    private String fromUserName;
    private String type;      // "comment", "like", "dislike"
    private int postId;
    private String message;
    private boolean lu;
    private LocalDateTime createdAt;

    public Notification(int id, int userId, int fromUserId, String fromUserName,
                        String type, int postId, String message, boolean lu, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.fromUserId = fromUserId;
        this.fromUserName = fromUserName;
        this.type = type;
        this.postId = postId;
        this.message = message;
        this.lu = lu;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public int getFromUserId() { return fromUserId; }
    public String getFromUserName() { return fromUserName; }
    public String getType() { return type; }
    public int getPostId() { return postId; }
    public String getMessage() { return message; }
    public boolean isLu() { return lu; }
    public void setLu(boolean lu) { this.lu = lu; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
