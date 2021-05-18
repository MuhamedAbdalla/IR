import java.sql.*;

public class DatabaseQueryManager {
    private static final String url = "jdbc:sqlserver://DESKTOP-75A2EL9\\SQLEXPRESS:1433;databaseName=Clawer;integratedSecurity=true;";

    public boolean insert(SiteInfo siteInfo) {
        try {
            Connection connection = DriverManager.getConnection(url);
            String query = "INSERT INTO SiteContent (url, content)"
                    + "VALUES (?, ?)";
            PreparedStatement parameter = connection.prepareStatement(query);
            parameter.setString(1, siteInfo.getUrl());
            parameter.setString(2, siteInfo.getContent());
            int row = parameter.executeUpdate();
            connection.close();
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
}
