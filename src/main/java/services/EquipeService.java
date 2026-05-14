package services;

import models.Equipe;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EquipeService implements IService<Equipe> {

    private Connection connection;

    public EquipeService() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void add(Equipe e) throws Exception {
        String query = "INSERT INTO equipe (nom, capitaineId, nbMembres) VALUES (?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setString(1, e.getNom());
        ps.setInt(2, e.getCapitaineId());
        ps.setInt(3, e.getNbMembres());
        ps.executeUpdate();
        System.out.println(" Equipe added successfully!");
    }

    @Override
    public void update(Equipe e) throws Exception {
        String query = "UPDATE equipe SET nom=?, capitaineId=?, nbMembres=? WHERE Id=?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setString(1, e.getNom());
        ps.setInt(2, e.getCapitaineId());
        ps.setInt(3, e.getNbMembres());
        ps.setInt(4, e.getId());
        ps.executeUpdate();
        System.out.println(" Equipe updated successfully!");
    }

    @Override
    public void delete(int id) throws Exception {
        String query = "DELETE FROM equipe WHERE Id=?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, id);
        ps.executeUpdate();
        System.out.println(" Equipe deleted successfully!");
    }

    @Override
    public Equipe getById(int id) throws Exception {
        String query = "SELECT * FROM equipe WHERE Id=?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return new Equipe(
                rs.getInt("Id"),
                rs.getString("nom"),
                rs.getInt("capitaineId"),
                rs.getInt("nbMembres")
            );
        }
        return null;
    }

    @Override
    public List<Equipe> getAll() throws Exception {
        List<Equipe> list = new ArrayList<>();
        String query = "SELECT * FROM equipe";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(query);
        while (rs.next()) {
            list.add(new Equipe(
                rs.getInt("Id"),
                rs.getString("nom"),
                rs.getInt("capitaineId"),
                rs.getInt("nbMembres")
            ));
        }
        return list;
    }
}
