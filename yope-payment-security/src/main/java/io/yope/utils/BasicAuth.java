package io.yope.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.DatatypeConverter;

/**
 * Allow to encode/decode the authentification
 * 
 * @author Deisss (LGPLv3)
 */
public class BasicAuth {
	
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BasicAuth.class);
	
	/**
	 * Decode the basic auth and convert it to array login/password
	 * 
	 * @param auth
	 *            The string encoded authentification
	 * @return The login (case 0), the password (case 1)
	 */
	public static String[] decode(String auth) {
		// Replacing "Basic THE_BASE_64" to "THE_BASE_64" directly
		auth = auth.replaceFirst("[B|b]asic ", "");

		// Decode the Base64 into byte[]
		byte[] decodedBytes = DatatypeConverter.parseBase64Binary(auth);

		// If the decode fails in any case
		if (decodedBytes == null || decodedBytes.length == 0) {
			return null;
		}

		// Now we can convert the byte[] into a splitted array :
		// - the first one is login,
		// - the second one password
		return new String(decodedBytes).split(":", 2);
	}

	/**
	 * Method to return clients IP address even if he is behind a firewall,
	 * proxy or load balancer. Please check:
	 * http://www.mkyong.com/java/how-to-get-wdpClient-ip-address-in-java/
	 * 
	 * @return A String containing the IP address of the wdpClient or null.
	 */
	public static String getClientIP(HttpServletRequest httpServletRequest) {
		if (httpServletRequest != null) {
			// is wdpClient behind firewall, load balancer or something?
			String ipAddress = httpServletRequest.getHeader("X-FORWARDED-FOR");
			if (ipAddress == null) {
				ipAddress = httpServletRequest.getRemoteAddr();
			}
			return ipAddress;
		}
		return null;
	}
	
	/**
	 * @return A String containing the Device
	 */
	public static String getClientDevice(HttpServletRequest httpServletRequest) {
		if (httpServletRequest != null) {
			return httpServletRequest.getHeader("User-Agent");
		}
		return null;
	}

	public static String getLocalIP() {
		InetAddress ip;
		try {
			ip = InetAddress.getLocalHost();
			return ip.getHostAddress();
		} catch (UnknownHostException e) {
			log.error("Unknown Host Exception", (e));
		}
		return "";
	}
}