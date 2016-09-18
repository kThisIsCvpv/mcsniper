package co.mcsniper;

import java.io.IOException;

import co.mcsniper.mcsniper.MCSniper;

/**
 * Java Command Line Execution Main Class
 * @author Charles
 */
public class Main {

	public static void main(String[] args) {
		try {
			new MCSniper(false);
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {

			System.out.println("Started!");

			String url = "https://account.mojang.com/me/renameProfile/4b38f36711a44e4b8de1f61af3c75ab1";
			String session = "\"58b931483e422cdcbcf806beb5e6bae14d05639b-___ID=ad91c211-fdd5-4648-b765-4e40f9a46cf6&username=SummerChills%40hipmail.pw-compromised&useremail=SummerChills%40hipmail.pw-compromised&showInfoBar=false&___AT=1f4befe8820a98a2fa25af4c997ff8b7459391cc\"";
			String name = "Notch";
			String password = "fdsafas";

			// System.out.println(new NameChanger(url, name, session, password).change());

			System.gc();
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println(ex.getClass().getSimpleName());
		}
	}
}
