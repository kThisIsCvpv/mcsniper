package co.mcsniper;

import co.mcsniper.mcsniper.MCSniper;

/**
 * Java Command Line Execution Main Class
 * @author Charles
 */
public class Main {

	public static void main(String[] args) {
		try {
			new MCSniper(false);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
