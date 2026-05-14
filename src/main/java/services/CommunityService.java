package services;

import models.Comment;
import models.Message;
import models.Notification;
import models.Post;
import org.example.MyDataBase;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CommunityService {

    private final BadWordFilter filter = new BadWordFilter();

    // ══════════════════════════════
    // CHARGER TOUS LES POSTS
    // ══════════════════════════════
    public List<Post> getAllPosts() {
        List<Post> posts = new ArrayList<>();
        String sql = "SELECT p.postId, p.auteurId, u.nom, p.Titre, " +
                "p.Contenue, p.Type, p.DatePub " +
                "FROM post p " +
                "LEFT JOIN user u ON p.auteurId = u.Id " +
                "ORDER BY p.DatePub DESC";
        try {
            Connection conn = MyDataBase.getInstance();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("DatePub");
                LocalDateTime createdAt = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();
                LocalDate datePub = createdAt.toLocalDate();
                Post post = new Post(
                        rs.getInt("postId"),
                        rs.getInt("auteurId"),
                        rs.getString("nom") != null ? rs.getString("nom") : "Anonyme",
                        rs.getString("Titre"),
                        rs.getString("Contenue"),
                        rs.getString("Type"),
                        datePub,
                        createdAt
                );
                loadReactions(post);
                loadComments(post);
                posts.add(post);
            }
            System.out.println("✅ getPosts : " + posts.size() + " posts chargés");
        } catch (SQLException e) {
            System.out.println("❌ Erreur getPosts : " + e.getMessage());
        }
        return posts;
    }

    // ══════════════════════════════
    // CRÉER UN POST
    // ══════════════════════════════
    public Post createPost(String author, String rawContent) {
        String filtered = filter.filter(rawContent);
        boolean hidden = filter.shouldHide(rawContent);

        String sql = "INSERT INTO post (auteurId, Titre, Contenue, Type, DatePub)" +
                " VALUES (1, ?, ?, 'community', NOW())";
        try {
            Connection conn = MyDataBase.getInstance();
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, filtered.substring(0, Math.min(filtered.length(), 255)));
            stmt.setString(2, filtered);
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                int newId = keys.getInt(1);
                System.out.println("✅ Post sauvegardé en DB : " + newId);
                Post post = new Post(newId, author, filtered);
                post.setHidden(hidden);
                return post;
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur createPost : " + e.getMessage());
        }

        System.out.println("⚠️ Fallback post sans DB");
        Post post = new Post(0, author, filtered);
        post.setHidden(hidden);
        return post;
    }

    // ══════════════════════════════
    // SUPPRIMER UN POST
    // ══════════════════════════════
    public void deletePost(int postId) {
        String sqlReactions = "DELETE FROM reaction WHERE Type LIKE ?";
        String sqlComments  = "DELETE FROM commentaire WHERE postId = ?";
        String sqlPost      = "DELETE FROM post WHERE postId = ?";
        try {
            Connection conn = MyDataBase.getInstance();

            PreparedStatement stmtR = conn.prepareStatement(sqlReactions);
            stmtR.setString(1, "%_post_" + postId);
            stmtR.executeUpdate();

            PreparedStatement stmtC = conn.prepareStatement(sqlComments);
            stmtC.setInt(1, postId);
            stmtC.executeUpdate();

            PreparedStatement stmtP = conn.prepareStatement(sqlPost);
            stmtP.setInt(1, postId);
            stmtP.executeUpdate();

            System.out.println("✅ Post supprimé : " + postId);
        } catch (SQLException e) {
            System.out.println("❌ Erreur deletePost : " + e.getMessage());
        }
    }

    // ══════════════════════════════
    // CHARGER LES COMMENTAIRES
    // ══════════════════════════════
    public void loadComments(Post post) {
        String sql = "SELECT c.Id, c.auteurId, u.nom, c.Contenue, c.DateCreation " +
                "FROM commentaire c " +
                "LEFT JOIN user u ON c.auteurId = u.Id " +
                "WHERE c.postId = ? " +
                "ORDER BY c.DateCreation ASC";
        try {
            Connection conn = MyDataBase.getInstance();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, post.getId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Comment comment = new Comment(
                        rs.getInt("Id"),
                        rs.getInt("auteurId"),
                        post.getId(),
                        rs.getString("nom") != null ? rs.getString("nom") : "Anonyme",
                        rs.getString("Contenue"),
                        rs.getTimestamp("DateCreation") != null ?
                                rs.getTimestamp("DateCreation").toLocalDateTime() :
                                LocalDateTime.now()
                );
                post.addComment(comment);
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur loadComments : " + e.getMessage());
        }
    }

    // ══════════════════════════════
    // AJOUTER UN COMMENTAIRE
    // ══════════════════════════════
    public Comment addComment(Post post, String author, String rawContent) {
        String filtered = filter.filter(rawContent);
        boolean hidden  = filter.shouldHide(rawContent);

        String sql = "INSERT INTO commentaire (auteurId, postId, Contenue, DateCreation) VALUES (1, ?, ?, NOW())";
        try {
            Connection conn = MyDataBase.getInstance();
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, post.getId());
            stmt.setString(2, filtered);
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                int newId = keys.getInt(1);
                System.out.println("✅ Commentaire sauvegardé : " + newId);
                Comment comment = new Comment(newId, author, filtered);
                comment.setHidden(hidden);
                post.addComment(comment);
                // Notification à l'auteur du post
                int postAuthorId = post.getAuteurId();
                if (postAuthorId != 1) {
                    addNotification(postAuthorId, 1, "comment", post.getId(),
                            author + " a commenté ton post \"" + post.getTitre() + "\"");
                }
                return comment;
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur addComment : " + e.getMessage());
        }

        System.out.println("⚠️ Fallback commentaire sans DB");
        Comment comment = new Comment(0, author, filtered);
        comment.setHidden(hidden);
        post.addComment(comment);
        return comment;
    }

    // ══════════════════════════════
    // REACTIONS (LIKE / DISLIKE)
    // ══════════════════════════════
    public void loadReactions(Post post) {
        String sql = "SELECT Type, COUNT(*) as total FROM reaction " +
                "WHERE Type = ? OR Type = ? GROUP BY Type";
        try {
            Connection conn = MyDataBase.getInstance();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, "like_post_" + post.getId());
            stmt.setString(2, "dislike_post_" + post.getId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String type = rs.getString("Type");
                int count   = rs.getInt("total");
                if (type.startsWith("like"))    post.setLikes(count);
                if (type.startsWith("dislike")) post.setDislikes(count);
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur loadReactions : " + e.getMessage());
        }
    }

    public void saveReaction(int postId, String type) {
        String sql = "INSERT INTO reaction (userid, Type) VALUES (1, ?)";
        try {
            Connection conn = MyDataBase.getInstance();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, type + "_post_" + postId);
            stmt.executeUpdate();
            System.out.println("✅ Réaction sauvegardée : " + type);
            // Notification à l'auteur du post
            int postAuthorId = getPostAuthorId(postId);
            if (postAuthorId != 1) {
                String emoji = type.equals("like") ? "👍" : "👎";
                addNotification(postAuthorId, 1, type, postId,
                        "Anaghim a mis " + emoji + " sur ton post");
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur saveReaction : " + e.getMessage());
        }
    }

    public void removeReaction(int postId, String type) {
        String sql = "DELETE FROM reaction WHERE userid = 1 AND Type = ?";
        try {
            Connection conn = MyDataBase.getInstance();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, type + "_post_" + postId);
            stmt.executeUpdate();
            System.out.println("✅ Réaction supprimée : " + type);
        } catch (SQLException e) {
            System.out.println("❌ Erreur removeReaction : " + e.getMessage());
        }
    }

    public void likePost(Post post) {
        post.setLikes(post.getLikes() + 1);
        saveReaction(post.getId(), "like");
    }

    public void dislikePost(Post post) {
        post.setDislikes(post.getDislikes() + 1);
        saveReaction(post.getId(), "dislike");
    }

    public int countMembers() {
        String sql = "SELECT COUNT(*) FROM user";
        try {
            Connection conn = MyDataBase.getInstance();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.out.println("❌ Erreur countMembers : " + e.getMessage());
        }
        return 0;
    }

    public List<String> getAllMemberNames() {
        List<String> members = new ArrayList<>();
        String sql = "SELECT nom FROM user ORDER BY nom ASC";
        try {
            Connection conn = MyDataBase.getInstance();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                members.add(rs.getString("nom"));
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur getAllMemberNames : " + e.getMessage());
        }
        return members;
    }

    // ══════════════════════════════════
    // TABLE MESSAGE
    // ══════════════════════════════════
    public void initMessageTable() {
        String sql = "CREATE TABLE IF NOT EXISTS message (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "room VARCHAR(50) NOT NULL," +
                "auteurId INT NOT NULL," +
                "author VARCHAR(100) NOT NULL," +
                "content TEXT NOT NULL," +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ")";
        try {
            Connection conn = MyDataBase.getInstance();
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);
            System.out.println("✅ Table 'message' prête");
        } catch (SQLException e) {
            System.out.println("❌ Erreur initMessageTable : " + e.getMessage());
        }
    }

    public void initCommentTable() {
        String sql = "CREATE TABLE IF NOT EXISTS commentaire (" +
                "Id INT AUTO_INCREMENT PRIMARY KEY," +
                "auteurId INT NOT NULL," +
                "postId INT," +
                "Contenue TEXT NOT NULL," +
                "DateCreation DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ")";
        try {
            Connection conn = MyDataBase.getInstance();
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);
            System.out.println("✅ Table 'commentaire' prête");
            try {
                stmt.executeUpdate("ALTER TABLE commentaire ADD COLUMN postId INT");
                System.out.println("✅ Colonne postId ajoutée à commentaire");
            } catch (SQLException e) {
                // colonne existe déjà — normal
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur initCommentTable : " + e.getMessage());
        }
    }

    public void initNotificationTable() {
        String sql = "CREATE TABLE IF NOT EXISTS notification (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "userId INT NOT NULL," +
                "fromUserId INT NOT NULL," +
                "type VARCHAR(20) NOT NULL," +
                "postId INT," +
                "message TEXT," +
                "lu BOOLEAN DEFAULT FALSE," +
                "createdAt DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ")";
        try {
            Connection conn = MyDataBase.getInstance();
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);
            System.out.println("✅ Table 'notification' prête");
            // Ajouter les colonnes manquantes si la table existait déjà
            try { stmt.executeUpdate("ALTER TABLE notification ADD COLUMN fromUserId INT"); } catch (SQLException e) { /* existe déjà */ }
            try {
                stmt.executeUpdate("ALTER TABLE notification ADD COLUMN lu BOOLEAN DEFAULT FALSE");
            } catch (SQLException e) { /* existe déjà */ }
            try {
                stmt.executeUpdate("ALTER TABLE notification ADD COLUMN createdAt DATETIME DEFAULT CURRENT_TIMESTAMP");
            } catch (SQLException e) { /* existe déjà */ }
            stmt.executeUpdate("UPDATE notification SET lu = FALSE WHERE lu IS NULL");
        } catch (SQLException e) {
            System.out.println("❌ Erreur initNotificationTable : " + e.getMessage());
        }
    }

    public void addNotification(int userId, int fromUserId, String type, int postId, String message) {
        String sql = "INSERT INTO notification (userId, fromUserId, type, postId, message, lu, createdAt) " +
                "VALUES (?, ?, ?, ?, ?, FALSE, NOW())";
        try {
            Connection conn = MyDataBase.getInstance();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, fromUserId);
            stmt.setString(3, type);
            stmt.setInt(4, postId);
            stmt.setString(5, message);
            stmt.executeUpdate();
            System.out.println("✅ Notification ajoutée pour l'utilisateur " + userId);
        } catch (SQLException e) {
            System.out.println("❌ Erreur addNotification : " + e.getMessage());
        }
    }

    public List<Notification> getNotificationsByUser(int userId) {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT n.*, u.nom as fromUserName " +
                "FROM notification n " +
                "LEFT JOIN user u ON n.fromUserId = u.Id " +
                "WHERE n.userId = ? ORDER BY n.createdAt DESC LIMIT 50";
        try {
            Connection conn = MyDataBase.getInstance();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Notification notif = new Notification(
                        rs.getInt("id"),
                        rs.getInt("userId"),
                        rs.getInt("fromUserId"),
                        rs.getString("fromUserName") != null ? rs.getString("fromUserName") : "Anonyme",
                        rs.getString("type"),
                        rs.getInt("postId"),
                        rs.getString("message"),
                        rs.getBoolean("lu"),
                        rs.getTimestamp("createdAt") != null ?
                                rs.getTimestamp("createdAt").toLocalDateTime() : LocalDateTime.now()
                );
                list.add(notif);
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur getNotificationsByUser : " + e.getMessage());
        }
        return list;
    }

    public int countUnreadNotifications(int userId) {
        String sql = "SELECT COUNT(*) FROM notification WHERE userId = ? AND lu = FALSE";
        try {
            Connection conn = MyDataBase.getInstance();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.out.println("❌ Erreur countUnreadNotifications : " + e.getMessage());
        }
        return 0;
    }

    public void markNotificationAsRead(int notificationId) {
        String sql = "UPDATE notification SET lu = TRUE WHERE id = ?";
        try {
            Connection conn = MyDataBase.getInstance();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, notificationId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("❌ Erreur markNotificationAsRead : " + e.getMessage());
        }
    }

    public void markAllNotificationsAsRead(int userId) {
        String sql = "UPDATE notification SET lu = TRUE WHERE userId = ? AND lu = FALSE";
        try {
            Connection conn = MyDataBase.getInstance();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("❌ Erreur markAllNotificationsAsRead : " + e.getMessage());
        }
    }

    public int getPostAuthorId(int postId) {
        String sql = "SELECT auteurId FROM post WHERE postId = ?";
        try {
            Connection conn = MyDataBase.getInstance();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, postId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("auteurId");
        } catch (SQLException e) {
            System.out.println("❌ Erreur getPostAuthorId : " + e.getMessage());
        }
        return -1;
    }

    public Message saveMessage(String room, String author, String content) {
        String sql = "INSERT INTO message (room, auteurId, author, content, created_at) VALUES (?, 1, ?, ?, NOW())";
        try {
            Connection conn = MyDataBase.getInstance();
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, room);
            stmt.setString(2, author);
            stmt.setString(3, content);
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                int newId = keys.getInt(1);
                System.out.println("✅ Message sauvegardé : " + newId);
                Message msg = new Message(newId, room, 1, author, content, java.time.LocalDateTime.now());
                return msg;
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur saveMessage : " + e.getMessage());
        }
        return new Message(0, room, 1, author, content, java.time.LocalDateTime.now());
    }

    public List<Message> loadMessagesByRoom(String room) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT id, room, auteurId, author, content, created_at FROM message WHERE room = ? ORDER BY created_at ASC";
        try {
            Connection conn = MyDataBase.getInstance();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, room);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("created_at");
                LocalDateTime createdAt = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();
                Message msg = new Message(
                        rs.getInt("id"),
                        rs.getString("room"),
                        rs.getInt("auteurId"),
                        rs.getString("author"),
                        rs.getString("content"),
                        createdAt
                );
                messages.add(msg);
            }
            System.out.println("✅ " + messages.size() + " messages chargés pour le salon '" + room + "'");
        } catch (SQLException e) {
            System.out.println("❌ Erreur loadMessagesByRoom : " + e.getMessage());
        }
        return messages;
    }
}