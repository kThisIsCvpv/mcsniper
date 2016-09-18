package co.mcsniper;

import co.mcsniper.mcsniper.MCSniper;

public class Main {

	public static void main(String[] args) {
		try {
			new MCSniper(false);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}