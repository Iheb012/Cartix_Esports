package models;

import java.time.LocalDateTime;

public class Message {
    private int id;
    private String room;
    private int auteurId;
    private String author;
    private String content;
    private LocalDateTime createdAt;

    public Message(int id, String room, int auteurId, String author, String content, LocalDateTime createdAt) {
        this.id = id;
        this.room = room;
        this.auteurId = auteurId;
        this.author = author;
        this.content = content;
        this.createdAt = createdAt;
    }

    public Message(String room, String author, String content) {
        this.room = room;
        this.author = author;
        this.content = content;
        this.auteurId = 1;
        this.createdAt = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getRoom() { return room; }
    public int getAuteurId() { return auteurId; }
    public String getAuthor() { return author; }
    public String getContent() { return content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
