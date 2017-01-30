package co.mcsniper.mcsniper.sniper.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import co.mcsniper.mcsniper.MCSniper;

public class RestartManager {

    private MCSniper sniper;

    public RestartManager(MCSniper sniper) {
        this.sniper = sniper;
    }

    /**
     * Set restart to 0 in MySQL
     */
    public void updateStatus() {
        try {
            Connection connection = this.sniper.getMySQL().createConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO restart VALUES (?, 0) ON DUPLICATE KEY UPDATE restart = 0");
            preparedStatement.setString(1, this.sniper.getServerName());
            preparedStatement.execute();
            preparedStatement.close();

            preparedStatement = connection.prepareStatement("INSERT INTO version VALUES (?, ?) ON DUPLICATE KEY UPDATE version = ?");
            preparedStatement.setString(1, this.sniper.getServerName());
            preparedStatement.setString(2, this.sniper.getVersion());
            preparedStatement.setString(3, this.sniper.getVersion());
            preparedStatement.execute();
            preparedStatement.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Check for pending restart & restart if needed
     */
    public void checkForRestart() {
        try {
            Connection connection = this.sniper.getMySQL().createConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM restart WHERE server = ?");
            preparedStatement.setString(1, this.sniper.getServerName());
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                if (resultSet.getInt("restart") == 1) {
                    Runtime.getRuntime().exec("killall -9 java");
                    System.exit(0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
