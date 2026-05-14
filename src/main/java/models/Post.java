package models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Post {
    private int id;
    private int auteurId;
    private String author;
    private String titre;
    private String content;
    private String type;
    private LocalDate datePub;
    private int likes;
    private int dislikes;
    private LocalDateTime createdAt;
    private List<Comment> comments;
    private boolean hidden;

    // ══════════════════════════════════════
    // CONSTRUCTEUR 3 PARAMÈTRES (nouveau post en mémoire)
    // ══════════════════════════════════════
    public Post(int id, String author, String content) {
        this.id = id;
        this.author = author;
        this.content = content;
        this.titre = content;
        this.type = "community";
        this.likes = 0;
        this.dislikes = 0;
        this.createdAt = LocalDateTime.now();
        this.datePub = LocalDate.now();
        this.comments = new ArrayList<>();
        this.hidden = false;
    }

    // ══════════════════════════════════════
    // CONSTRUCTEUR 7 PARAMÈTRES (chargé depuis la DB)
    // ✅ FIX : createdAt utilise maintenant l'heure réelle si disponible
    // ══════════════════════════════════════
    public Post(int id, int auteurId, String author, String titre,
                String content, String type, LocalDate datePub) {
        this.id = id;
        this.auteurId = auteurId;
        this.author = author;
        this.titre = titre;
        this.content = content;
        this.type = type;
        this.datePub = datePub != null ? datePub : LocalDate.now();
        this.likes = 0;
        this.dislikes = 0;
        // ✅ FIX : initialiser createdAt depuis datePub correctement
        this.createdAt = this.datePub.atStartOfDay();
        this.comments = new ArrayList<>();
        this.hidden = false;
    }

    // ══════════════════════════════════════
    // CONSTRUCTEUR 8 PARAMÈTRES (avec heure exacte depuis DB)
    // ✅ NOUVEAU : pour les cas où on a un DATETIME complet
    // ══════════════════════════════════════
    public Post(int id, int auteurId, String author, String titre,
                String content, String type, LocalDate datePub, LocalDateTime createdAt) {
        this.id = id;
        this.auteurId = auteurId;
        this.author = author;
        this.titre = titre;
        this.content = content;
        this.type = type;
        this.datePub = datePub != null ? datePub : LocalDate.now();
        this.likes = 0;
        this.dislikes = 0;
        this.createdAt = createdAt != null ? createdAt : this.datePub.atStartOfDay();
        this.comments = new ArrayList<>();
        this.hidden = false;
    }

    // ══════════════════════════════════════
    // GETTERS & SETTERS
    // ══════════════════════════════════════
    public int getId() { return id; }

    // ✅ NOUVEAU : setter pour id (utile après INSERT avec generated key)
    public void setId(int id) { this.id = id; }

    public int getAuteurId() { return auteurId; }
    public void setAuteurId(int auteurId) { this.auteurId = auteurId; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getContent() { return content; }
    public void setContent(String content) {
        this.content = content;
        // ✅ Synchroniser le titre avec le contenu si nécessaire
        if (this.titre == null || this.titre.equals(this.content)) {
            this.titre = content.substring(0, Math.min(content.length(), 255));
        }
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public LocalDate getDatePub() { return datePub; }
    public void setDatePub(LocalDate datePub) {
        this.datePub = datePub;
        // ✅ Synchroniser createdAt si datePub change
        if (this.createdAt == null) {
            this.createdAt = datePub.atStartOfDay();
        }
    }

    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }

    public int getDislikes() { return dislikes; }
    public void setDislikes(int dislikes) { this.dislikes = dislikes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<Comment> getComments() { return comments; }
    public void setComments(List<Comment> comments) { this.comments = comments; }

    public boolean isHidden() { return hidden; }
    public void setHidden(boolean hidden) { this.hidden = hidden; }

    // ✅ FIX : vérifier les doublons avant d'ajouter un commentaire
    public void addComment(Comment c) {
        if (c != null && !comments.contains(c)) {
            this.comments.add(c);
        }
    }

    // ══════════════════════════════════════
    // UTILITAIRES
    // ══════════════════════════════════════

    // ✅ NOUVEAU : compter les commentaires facilement
    public int getCommentCount() {
        return comments.size();
    }

    // ✅ NOUVEAU : vérifier si le post a des réactions
    public boolean hasReactions() {
        return likes > 0 || dislikes > 0;
    }

    @Override
    public String toString() {
        return "Post{id=" + id + ", author='" + author + "'" +
                ", likes=" + likes + ", dislikes=" + dislikes +
                ", comments=" + comments.size() + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Post)) return false;
        Post post = (Post) o;
        return id == post.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
