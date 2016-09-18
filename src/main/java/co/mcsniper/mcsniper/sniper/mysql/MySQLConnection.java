package co.mcsniper.mcsniper.sniper.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MySQLConnection {

    private final int MAX_ATTEMPTS = 5;
    private MySQLCredentials credentials;

    public MySQLConnection(MySQLCredentials credentials) throws SQLException {
        this.credentials = credentials;
    }

    public Connection createConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://" + this.credentials.getHost() + "/" + this.credentials.getDatabase(), this.credentials.getUsername(), this.credentials.getPassword());
    }

    public void pushLog(String serverName, int snipeID, String name, String time, int success, String log) {
        for (int attempts = 0; attempts < this.MAX_ATTEMPTS; attempts++) {
            Connection con = null;

            try {
                con = this.createConnection();

                PreparedStatement ps = con.prepareStatement("INSERT INTO `log` VALUES (?,?,?,?,?,?);");
                ps.setString(1, serverName);
                ps.setInt(2, snipeID);
                ps.setString(3, name);
                ps.setString(4, time);
                ps.setInt(5, success);
                ps.setString(6, log);
                ps.execute();

                ps.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            } finally {
                if (con != null) {
                    try {
                        con.close();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }
    
    public void updateStatus(int snipeID, int statusCode) {
        for (int attempts = 0; attempts < this.MAX_ATTEMPTS; attempts++) {
            Connection con = null;

            try {
                con = this.createConnection();

                PreparedStatement ps = con.prepareStatement("UPDATE `sniper` SET `success` = ? WHERE `id` = ?;");
                ps.setInt(1, snipeID);
                ps.setInt(2, statusCode);
                ps.executeUpdate();

                ps.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            } finally {
                if (con != null) {
                    try {
                        con.close();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

}