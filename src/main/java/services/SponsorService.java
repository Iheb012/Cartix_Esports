package services;

import models.Sponsor;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SponsorService implements IService<Sponsor> {

    private Connection connection;

    public SponsorService() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void add(Sponsor s) throws Exception {
        String query = "INSERT INTO sponsor (nom, secteur, budgetAllouee, dateDebutContrat, dateFinContrat) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setString(1, s.getNom());
        ps.setString(2, s.getSecteur());
        ps.setFloat(3, s.getBudgetAllouee());
        ps.setTimestamp(4, s.getDateDebutContrat());
        ps.setTimestamp(5, s.getDateFinContrat());
        ps.executeUpdate();
        System.out.println("✅ Sponsor added successfully!");
    }

    @Override
    public void update(Sponsor s) throws Exception {
        String query = "UPDATE sponsor SET nom=?, secteur=?, budgetAllouee=?, dateDebutContrat=?, dateFinContrat=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setString(1, s.getNom());
        ps.setString(2, s.getSecteur());
        ps.setFloat(3, s.getBudgetAllouee());
        ps.setTimestamp(4, s.getDateDebutContrat());
        ps.setTimestamp(5, s.getDateFinContrat());
        ps.setInt(6, s.getId());
        ps.executeUpdate();
        System.out.println("✅ Sponsor updated successfully!");
    }

    @Override
    public void delete(int id) throws Exception {
        String query = "DELETE FROM sponsor WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, id);
        ps.executeUpdate();
        System.out.println("✅ Sponsor deleted successfully!");
    }

    @Override
    public Sponsor getById(int id) throws Exception {
        String query = "SELECT * FROM sponsor WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return new Sponsor(
                rs.getInt("id"),
                rs.getString("nom"),
                rs.getString("secteur"),
                rs.getFloat("budgetAllouee"),
                rs.getTimestamp("dateDebutContrat"),
                rs.getTimestamp("dateFinContrat")
            );
        }
        return null;
    }

    @Override
    public List<Sponsor> getAll() throws Exception {
        List<Sponsor> list = new ArrayList<>();
        String query = "SELECT * FROM sponsor";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(query);
        while (rs.next()) {
            list.add(new Sponsor(
                rs.getInt("id"),
                rs.getString("nom"),
                rs.getString("secteur"),
                rs.getFloat("budgetAllouee"),
                rs.getTimestamp("dateDebutContrat"),
                rs.getTimestamp("dateFinContrat")
            ));
        }
        return list;
    }
}
