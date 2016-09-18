package co.mcsniper.mcsniper.sniper.proxy;

public class ProxyValidator {

	public boolean validateProxy(String proxy) {
		try {
			String[] proxySplit = proxy.split(":");
			if (proxySplit.length == 2 && validateIP(proxySplit[0]) && Integer.parseInt(proxySplit[1]) > 0 && Integer.parseInt(proxySplit[1]) <= 65565)
				return true;
			return false;
		} catch (Exception ex) {
			return false;
		}
	}

	private boolean validateIP(String ip) {
		try {
			if (ip == null || ip.isEmpty())
				return false;

			String[] parts = ip.split("\\.");
			if (parts.length != 4)
				return false;

			for (String s : parts) {
				int i = Integer.parseInt(s);
				if ((i < 0) || (i > 255))
					return false;
			}

			if (ip.endsWith("."))
				return false;

			return true;
		} catch (NumberFormatException nfe) {
			return false;
		}
	}
}
