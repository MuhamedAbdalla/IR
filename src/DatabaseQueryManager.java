import java.sql.*;
import java.util.ArrayList;

public class DatabaseQueryManager {

    private static final String url = "jdbc:sqlserver://DESKTOP-PVBOS8R\\SQLEXPRESS01:1433;databaseName=clawer;integratedSecurity=true;";
    private static Connection connection;

    public static boolean insert(SiteInfo siteInfo) {
        try {
            if(connection == null) {
                connection = DriverManager.getConnection(url);
            }
            String query = "INSERT INTO document (url, content)"
                    + "VALUES (?, ?)";
            PreparedStatement parameter = connection.prepareStatement(query);
            parameter.setString(1, siteInfo.getUrl());
            parameter.setString(2, siteInfo.getContent());
            int row = parameter.executeUpdate();
            if (row > 0) {
                return true;
            }
        }
        catch (Exception exception) {
            exception.printStackTrace();
            System.exit(0);
        }
        return false;
    }

    public static boolean insertIntoTerms(String term, int docId, int pos) {
        try {
            if(connection == null) {
                connection = DriverManager.getConnection(url);
            }
            String query = "INSERT INTO term (term, doc_id, freq)"
                    + "VALUES (?, ?, 1) ";
            PreparedStatement parameter = connection.prepareStatement(query);
            parameter.setString(1, term);
            parameter.setInt(2, docId);
            int row = parameter.executeUpdate();
            int term_id;

            if (row > 0) {
                query = "SELECT id FROM term WHERE term=? AND doc_id=?";
                parameter = connection.prepareStatement(query);
                parameter.setString(1, term);
                parameter.setInt(2, docId);
                ResultSet rs = parameter.executeQuery();
                rs.next();
                term_id = rs.getInt(1);
                rs.close();
            }
            else {
                query = "SELECT id, freq FROM term WHERE term=? AND doc_id=?";
                parameter = connection.prepareStatement(query);
                parameter.setString(1, term);
                parameter.setInt(2, docId);
                ResultSet rs = parameter.executeQuery();
                rs.next();
                term_id = rs.getInt(1);
                int freq = rs.getInt(2) + 1;
                rs.close();
                query = "UPDATE term SET freq = ? where term=? and doc_id=?";
                parameter = connection.prepareStatement(query);
                parameter.setInt(1, freq);
                parameter.setString(2, term);
                parameter.setInt(3, docId);
                parameter.executeUpdate();
            }


            query = "INSERT INTO term_pos (term_id, pos)"
                    + "VALUES (?, ?)";
            parameter = connection.prepareStatement(query);
            parameter.setInt(1, term_id);
            parameter.setInt(2, pos);
            row = parameter.executeUpdate();

            if(row > 0) {
                return true;
            }
        }
        catch (Exception exception) {
            exception.printStackTrace();
            System.exit(0);
        }
        return false;
    }

    public static ArrayList<SiteInfo> getAllDocs() {
        try {
            if(connection == null) {
                connection = DriverManager.getConnection(url);
            }
            String query = "SELECT id, url, content from document";
            PreparedStatement parameter = connection.prepareStatement(query);
            ResultSet rs = parameter.executeQuery();
            ArrayList<SiteInfo> docs = new ArrayList<>();
            while (rs.next()) {
                int id = rs.getInt(1);
                String url = rs.getString(2);
                String content = rs.getString(3);
                docs.add(new SiteInfo(url, content, id));
            }
            return docs;
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
            return null;
        }
    }

    public static void closeConnection() {
        try {
            if(connection != null) {
                connection.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }
}
