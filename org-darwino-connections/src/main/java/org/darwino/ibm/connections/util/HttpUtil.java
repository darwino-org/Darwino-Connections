/*!COPYRIGHT HEADER! - CONFIDENTIAL 
 *
 * Darwino Inc Confidential.
 *
 * (c) Copyright Darwino Inc. 2014-2016.
 *
 * Notice: The information contained in the source code for these files is the property 
 * of Darwino Inc. which, with its licensors, if any, owns all the intellectual property 
 * rights, including all copyright rights thereto.  Such information may only be used 
 * for debugging, troubleshooting and informational purposes.  All other uses of this information, 
 * including any production or commercial uses, are prohibited. 
 */

package org.darwino.ibm.connections.util;

import javax.servlet.http.HttpServletRequest;

import com.darwino.commons.util.PathUtil;
import com.darwino.commons.util.StringUtil;

/**
 * HTTP URL Utilities.
 * 
 * @author Philippe Riand
 */
public class HttpUtil {

	public static String getServerUrl(HttpServletRequest req) {
		StringBuilder b = new StringBuilder(64);
			String scheme = req.getScheme();
		String server = req.getServerName();
		int port = req.getServerPort();

		b.append(scheme);
		b.append("://");
		b.append(server);
		if (!((scheme.equals("http") && port == 80) || (scheme.equals("https") && port == 443))) {
			b.append(":");
			b.append(Integer.toString(port));
		}
		return b.toString();
	}

	public static String getContextUrl(HttpServletRequest req) {
		String serverUrl = getServerUrl(req);
		return PathUtil.concat(serverUrl, req.getContextPath(), '/');
	}
	
	public static String getRequestUrl(HttpServletRequest req) {
		StringBuilder b = new StringBuilder();
		String scheme = req.getScheme();
		b.append(scheme);
		b.append("://");
		b.append(req.getServerName());
		if (scheme.equals("http") && req.getServerPort() != 80) { 
			b.append(":");
			b.append(req.getServerPort());
		}
		if (scheme.equals("https") && req.getServerPort() != 443) {
			b.append(":");
			b.append(req.getServerPort());
		}
		String uri = req.getRequestURI();
		b.append(uri);
		String qs = req.getQueryString();
		if (StringUtil.isNotEmpty(qs)) {
			b.append("?");
			b.append(qs);
		}
		return b.toString();
	}
		
}
