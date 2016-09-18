package co.mcsniper.mcsniper.sniper.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLCredentials {

    private String host;
    private String username;
    private String password;
    private String database;

    public MySQLCredentials(String host, String username, String password, String database) {
        this.host = host;
        this.username = username;
        this.password = password;
        this.database = database;
    }

    public boolean verifyConnection() {
        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://" + this.host + "/" + this.database, this.username, this.password);
            connection.close();
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public String getHost() {
        return this.host;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public String getDatabase() {
        return this.database;
    }

}