package co.mcsniper.mcsniper.sniper.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MySQLConnection {

	private MySQLCredentials credentials;

	public MySQLConnection(MySQLCredentials credentials) throws SQLException {
		this.credentials = credentials;
	}

	public Connection createConnection() throws SQLException {
		return DriverManager.getConnection("jdbc:mysql://" + this.credentials.getHost() + "/" + this.credentials.getDatabase(), this.credentials.getUsername(), this.credentials.getPassword());
	}

	public void updateStatus(int snipeID, int statusCode) {
		for (int attempts = 0; attempts < 10; attempts++) {
			Connection con = null;

			try {
				con = this.createConnection();

				PreparedStatement ps = con.prepareStatement("UPDAPTE `sniper` SET `success` = ? WHERE `id` = ?;");
				ps.setInt(1, snipeID);
				ps.setInt(2, statusCode);
				ps.executeUpdate();

				ps.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
			} finally {
				if (con != null)
					try {
						con.close();
					} catch (SQLException ex) {
						ex.printStackTrace();
					}
			}
		}
	}
}
