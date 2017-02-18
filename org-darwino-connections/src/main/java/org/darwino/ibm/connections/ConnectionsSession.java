/*!COPYRIGHT HEADER! 
 *
 * (c) Copyright Darwino Inc. 2014-2017.
 *
 * Licensed under The MIT License (https://opensource.org/licenses/MIT)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software 
 * and associated documentation files (the "Software"), to deal in the Software without restriction, 
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, 
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial 
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT 
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.darwino.ibm.connections;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.darwino.ibm.connections.util.HttpUtil;
import org.darwino.ibm.connections.util.OAuth20Token;

import com.darwino.commons.Platform;
import com.darwino.commons.httpclnt.HttpClient;
import com.darwino.commons.httpclnt.HttpClientService;
import com.darwino.commons.json.JsonException;


/**
 * IBM Connections user.
 */
public class ConnectionsSession {
	
	
	private static ThreadLocal<ConnectionsSession> sessions = new ThreadLocal<ConnectionsSession>();
	public static ConnectionsSession get() {
		ConnectionsSession s = sessions.get();
		if(s==null) {
			throw new IllegalStateException("ConnectionsSession has not been setup by a filter");
		}
		return s;
	}
	public static boolean isAvailable() {
		return sessions.get()!=null;
	}
	public static void push(ConnectionsSession s) {
		if(sessions.get()!=null) {
			throw new IllegalStateException("ConnectionsSession has already been pushed");
		}
		sessions.set(s);
	}
	public static void pop() {
		if(sessions.get()==null) {
			throw new IllegalStateException("ConnectionsSession is not available");
		}
		sessions.remove();
	}
	

	public static final class OauthCredentials {
		private String consumerKey;
		private String consumerSecret;
		public OauthCredentials() {
		}
		public String getConsumerKey() {
			return consumerKey;
		}
		public void setConsumerKey(String consumerKey) {
			this.consumerKey = consumerKey;
		}
		public String getConsumerSecret() {
			return consumerSecret;
		}
		public void setConsumerSecret(String consumerSecret) {
			this.consumerSecret = consumerSecret;
		}
	}
	
	public static final String KEY_USER		= "_lconn_user";
	public static final String KEY_OAUTH	= "_lconn_oauth";
	public static final String KEY_REDIRECT	= "_lconn_redirect";
	
	private ConnectionsEnvironment connections;
	private HttpServletRequest request;
	private HttpServletResponse response;
	
	public ConnectionsSession(ConnectionsEnvironment connections, HttpServletRequest request, HttpServletResponse response) {
		this.connections = connections;
		this.request = request;
		this.response = response;
	}

	public ConnectionsEnvironment getConnections() {
		return connections;
	}
	public HttpSession getHttpSession() {
		return request.getSession();
	}

	public HttpServletRequest getHttpRequest() {
		return request;
	}

	public HttpServletResponse getHttpResponse() {
		return response;
	}

	public boolean isCloud() {
		return getConnections().isCloud();
	}

	public boolean isAuthenticated() {
		return !getCurrentUser().isAnonymous();
	}

	public HttpClient createHttpClient() {
		if(getConnections().useOAuth()) {
			OAuth20Token auth = (OAuth20Token)getHttpSession().getAttribute(KEY_OAUTH);
			HttpClient c = Platform.getService(HttpClientService.class).createHttpClient(getConnections().getUrl(),auth);
			c.setTrustAllSSLCertificates(getConnections().isTrustSSLCertificates());
			return c;
		} else {
			String ltpaToken2 = null;
			Cookie[] ck = request.getCookies();
			for(int i=0; i<ck.length; i++) {
				if(ck[i].getName().equalsIgnoreCase("LtpaToken2")) {
					ltpaToken2 = ck[i].getValue();
				}
			}
			HttpClient.LtpaToken2Authenticator auth = new HttpClient.LtpaToken2Authenticator(ltpaToken2);
			HttpClient c = Platform.getService(HttpClientService.class).createHttpClient(getConnections().getUrl(),auth);
			c.setTrustAllSSLCertificates(getConnections().isTrustSSLCertificates());
			return c;
		}
	}
	

	/**
	 * Authenticate the current user.
	 * This returns true if it provides a response to the client.
	 * @return
	 */
	public boolean authenticate() throws IOException, ServletException {
		return getConnections().authenticate(this);
	}
	
	public ConnectionsUser getCurrentUser() {
		ConnectionsUser user = (ConnectionsUser)getHttpSession().getAttribute(KEY_USER);
		if(user!=null) {
			return user;
		}
		return ConnectionsUser.ANONYMOUS_USER;
	}
	public void setCurrentUser(ConnectionsUser user) {
		getHttpSession().setAttribute(KEY_USER,user);
	}
	
	public OAuth20Token getOAuth2Token() {
		OAuth20Token token = (OAuth20Token)getHttpSession().getAttribute(KEY_OAUTH);
		return token;
	}
	public void setOAuth2Token(OAuth20Token token) {
		getHttpSession().setAttribute(KEY_OAUTH,token);
	}
	
	public void saveRedirectPage() {
		// We save the redirect page as a token
		String uri = HttpUtil.getRequestUrl(getHttpRequest());
		getHttpSession().setAttribute(KEY_REDIRECT, uri);
	}
	
	public String loadRedirectPage() {
		String uri = (String)getHttpSession().getAttribute(KEY_REDIRECT);
		getHttpSession().removeAttribute(KEY_REDIRECT);
		return uri;
	}
	
	//
	// Some services to call...
	//
	public Object getOpenSocialProfile() throws JsonException {
		HttpClient c = createHttpClient();
		String auth = getConnections().useOAuth() ? "oauth" : "basic"; 
		return c.getAsJson(new String[]{"connections","opensocial",auth,"rest","people","@me","@self"});
	}
}
