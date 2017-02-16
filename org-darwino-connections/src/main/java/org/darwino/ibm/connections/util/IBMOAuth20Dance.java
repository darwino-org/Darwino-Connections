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


package org.darwino.ibm.connections.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.darwino.ibm.connections.ConnectionsFactory;
import org.darwino.ibm.connections.ConnectionsSession;
import org.darwino.ibm.connections.ConnectionsUser;

import com.darwino.commons.Platform;
import com.darwino.commons.httpclnt.HttpBase;
import com.darwino.commons.httpclnt.HttpClient;
import com.darwino.commons.httpclnt.HttpClient.OAuth2Authenticator;
import com.darwino.commons.httpclnt.HttpClientService;
import com.darwino.commons.httpclnt.HttpException;
import com.darwino.commons.json.JsonException;
import com.darwino.commons.json.JsonObject;
import com.darwino.commons.util.PathUtil;
import com.darwino.commons.util.StringUtil;
import com.darwino.commons.util.io.content.FormUrlEncodedContent;

/**
 * IBM OAuth 2.0 implementation
 * 
	// See:
	// Cloud:
	//   https://www-10.lotus.com/ldd/appdevwiki.nsf/xpDocViewer.xsp?lookupName=Dev+Guide+topics#action=openDocument&res_title=Understanding_OAuth_2.0&content=sdkcontent
	//
	// On prem:
	//   https://www.ibm.com/support/knowledgecenter/SSYGQH_5.0.0/admin/admin/c_admin_common_oauth.html	
	// Register a key:
	//	 https://www.ibm.com/support/knowledgecenter/SSYGQH_5.5.0/admin/admin/t_admin_registeroauthclientwprovider.html
	// Launching wsadmin
	//	 https://www.ibm.com/support/knowledgecenter/SSYGQH_4.5.0/admin/admin/t_admin_wsadmin_starting.html
	// Ex: register the discdb app:
	//   OAuthApplicationRegistrationService.addApplication('discdb', 'Discussion database', 'https://localhost:8443/dominodisc/dwo_auth_ibmconn_oa2')
	//   print OAuthApplicationRegistrationService.getApplicationById('discdb').get('client_secret')
	
 Steps to register an OAuth key on-prem
	* Launch the ws admin https://www.ibm.com/support/knowledgecenter/SSYGQH_4.5.0/admin/admin/t_admin_wsadmin_starting.html
	  (launch from the dm profile dir, ex: e:\IBM\WebSphere\AppServer\profiles\Dmgr01\bin\)
		wsadmin -lang jython -username wasadm -password was@... -port 8879
	
	* Load the oauth admin python code
		execfile('oauthAdmin.py')
	
	* List the registered applications https://www.ibm.com/support/knowledgecenter/SSYGQH_5.5.0/admin/admin/r_admin_common_oauth_manage_list.html
		OAuthApplicationRegistrationService.browseApplications()
	
	* Register an application
		OAuthApplicationRegistrationService.addApplication('darwinodemo', 'Darwino demo', 'https://localhost:8443/darwino-connections/dwo_auth_ibmconn_oa2')
				
	* Print the key for this app	
		print OAuthApplicationRegistrationService.getApplicationById('darwinodemo').get('client_secret')
		
 */
public class IBMOAuth20Dance {
	
	public static final String OAUTH2_CALLBACK	= "/dwo_auth_ibmconn_oa2";

	private String clientId;
	private String clientSecret;
	
	public IBMOAuth20Dance(String clientId, String clientSecret) {
		this.clientId = clientId;
		this.clientSecret = clientSecret;
	}

	public String getClientId() {
		return clientId;
	}

	public String getClientSecret() {
		return clientSecret;
	}
	
	public boolean isTrustAllSSLCertificates() {
		return false;
	}

	public String getCallbackPathinfo() {
		return OAUTH2_CALLBACK;
	}

	public boolean isCloud() {
		return ConnectionsFactory.get().isCloud();
	}
	
    public String getAuthorizationURL() {
    	if(isCloud()) {
        	return PathUtil.concat(ConnectionsFactory.get().getUrl(), "manage/oauth2/authorize");
    	}
    	return PathUtil.concat(ConnectionsFactory.get().getUrl(), "oauth2/endpoint/connectionsProvider/authorize");
    }
    public String getAccessTokenURL() {
    	if(isCloud()) {
        	return PathUtil.concat(ConnectionsFactory.get().getUrl(), "manage/oauth2/token");
    	}
    	return PathUtil.concat(ConnectionsFactory.get().getUrl(), "oauth2/endpoint/connectionsProvider/token");
    }

	
	//
	// STEP 1 - Start the dance
	//

	public void startOAuthDance(ConnectionsSession session) throws IOException, ServletException {
		session.saveRedirectPage();
		session.getHttpResponse().setHeader(HttpBase.HEADER_DWOAUTHMSG, "authrequired");
		String url = createAuthorizationUrl(session);
		session.getHttpResponse().sendRedirect(url);
	}
	protected String createAuthorizationUrl(ConnectionsSession session) {
		String url = StringUtil.format("{0}?response_type=code&client_id={1}&callback_uri={2}",
				getAuthorizationURL(),
				encodeUrlParam(getClientId()),
				encodeUrlParam(calculateCallbackUrl(session.getHttpRequest())));
		return url;
	}
	

	//
	// STEP 2 - Process the callback
	//
	
	public boolean isCallbackRequest(ConnectionsSession session) throws IOException, ServletException {
		if(session.getHttpRequest().getMethod().equalsIgnoreCase("GET")) {
			String sp = session.getHttpRequest().getServletPath();
			String pi = session.getHttpRequest().getPathInfo();
			if(pi!=null) {
				sp = sp + pi;
			}
			if(StringUtil.equals(sp,getCallbackPathinfo())) {
				return true;
			}
		}
		return false;
	}
	public void processCallbackRequest(ConnectionsSession session) throws IOException, ServletException {
		try {
			String code = session.getHttpRequest().getParameter("code");
			if(StringUtil.isNotEmpty(code)) {
				// Get a final token
				HttpClient c = Platform.getService(HttpClientService.class).createHttpClient(getAccessTokenURL());
				c.setTrustAllSSLCertificates(isTrustAllSSLCertificates());

				Map<String,Object> values = new HashMap<String, Object>();
				values.put("callback_uri", calculateCallbackUrl(session.getHttpRequest()));
				values.put("client_id", getClientId());
				values.put("client_secret", getClientSecret());
				values.put("grant_type", "authorization_code");
				values.put("code", code);
				
				String text = tokenFromPostForm(c, values);
				
				// Parse the result
				// OnPrem -> return a JSON object
				// Cloud: returns form encoded content
				Map<String,Object> params = null;
				if(isCloud()) {
					params = parseFormEncodedValues(text); 
				} else {
					params = JsonObject.fromJson(text); 
				}
				String accessToken = toString(params.get("access_token"));
				String refreshToken = toString(params.get("refresh_token"));
				String issuedOn = toString(params.get("issued_on"));
				String expiresIn = toString(params.get("expires_in"));
				
				long _issuedOn = StringUtil.isNotEmpty(issuedOn) ? Long.parseLong(issuedOn) : (System.currentTimeMillis()-5*1000); 
				long _expiresIn = Long.parseLong(expiresIn);
				if(!isCloud()) {
					_expiresIn *= 1000; // On prem is in secs, Cloud in ms... Thanks IBM!
				}
				//_expiresIn = 30*1000;
				
				long _expiresOn = _issuedOn+_expiresIn;
				//Platform.log("Acquire,Now: {0}, Expires: {1}, {2}secs", (new Date()).toGMTString(), (new Date(_expiresOn)).toGMTString(), _expiresIn/1000 );				
				
				OAuth20Token token = new OAuth20Token(accessToken, refreshToken, _expiresOn);
				acquiredToken(session,token);
			}
		} catch(JsonException e) {
			throw new ServletException(e);
		}
	}

	
	//
	// STEP 3 - do something with the token once acquired
	//
	protected void acquiredToken(ConnectionsSession session, OAuth20Token token) throws IOException {
		try {
			// Access the current user for authentication
			OAuth2Authenticator auth = new OAuth2Authenticator(token.getAccessToken());
			String url = ConnectionsFactory.get().getUrl();
			HttpClient testClient = Platform.getService(HttpClientService.class).createHttpClient(url,auth);
			if(ConnectionsFactory.get().isTrustSSLCertificates()) {
				testClient.setTrustAllSSLCertificates(true);
			}

			// Read the current user ID from the server
			ConnectionsUser user = isCloud() ? getUserCloud(testClient) : getUserOnPrem(testClient);
			session.setCurrentUser(user!=null ? user : ConnectionsUser.ANONYMOUS_USER);
			
			// Save the credentials as a token
			OAuth20Token credentials = new OAuth20Token(token.getAccessToken(), token.getRefreshToken(), token.getExpiresOn());
			session.setOAuth2Token(credentials);
			
			// Ok, now redirect to the initial page (request)
			String uri = session.loadRedirectPage();
			if(StringUtil.isNotEmpty(uri)) {
				session.getHttpResponse().sendRedirect(uri);
			} else {
				session.getHttpResponse().sendRedirect(HttpUtil.getContextUrl(session.getHttpRequest()));
			}
		} catch(JsonException ex) {
			Platform.log(ex);
		}
	}
	protected ConnectionsUser getUserOnPrem(HttpClient client) throws JsonException {
		/* Payload example
			{
			  "entry": {
			    "emails": [
			      {
			        "type": "primary",
			        "value": "amass@triloggroup.com",
			        "primary": true
			      }
			    ],
			    "displayName": "Al Mass",
			    "id": "urn:lsid:lconn.ibm.com:profiles.person:D9E01101-B158-C2CC-8525-798000554701",
			    "appData": {
			      "connections": {
			        "organizationId": "urn:lsid:lconn.ibm.com:connections.organization:00000000-0000-0000-0000-000000000000",
			        "isExternal": "false"
			      }
			    }
			  }
			}		 
		*/
		// Looks like this is the userid, at least on our system, so it is ok...
		// 	urn:lsid:lconn.ibm.com:profiles.person:5F55F4C1-E90E-BD4E-C225-7B440049F5A9
		JsonObject o = (JsonObject)client.getAsJson(new String[]{"connections","opensocial","oauth","rest","people","@me","@self"});
		String id = o.getObject("entry").getAsString("id");
		int pos = id.lastIndexOf(':');
		if(pos>=0) {
			id = id.substring(pos+1);
		}
		ConnectionsUser user = new ConnectionsUser(id);
		user.setAttribute("displayName", o.getObject("entry").getAsString("displayName"));
		// Can add more attributes here...
		return user;
	}
	protected ConnectionsUser getUserCloud(HttpClient client) throws JsonException {
		/* Payload example
			{
			  "name": "Philippe Riand",
			  "customerid": "20092023",
			  "subscriberid": "20098922",
			  "email": "phil@triloggroup.com"
			}		
		*/
		JsonObject o = (JsonObject)client.getAsJson(new String[]{"manage","oauth","getUserIdentity"});
		String id = o.getAsString("subscriberid");
		ConnectionsUser user = new ConnectionsUser(id);
		user.setAttribute("displayName", o.getAsString("name"));
		// Can add more attributes here...
		return user;
	}
	

	//
	// STEP +1 - Refresh a token
	//
	public OAuth20Token refreshToken(HttpServletRequest httpRequest, HttpServletResponse httpResponse, OAuth20Token token) throws IOException, ServletException {
		try {
			// Refresh the token
			HttpClient c = Platform.getService(HttpClientService.class).createHttpClient(getAccessTokenURL());
			c.setTrustAllSSLCertificates(isTrustAllSSLCertificates());

			Map<String,Object> values = new HashMap<String, Object>();
			values.put("grant_type", "refresh_token");
			values.put("client_id", getClientId());
			values.put("client_secret", getClientSecret());
			values.put("refresh_token", token.getRefreshToken());
			
			String text = tokenFromPostForm(c, values);
			
			// Parse the result
			// OnPrem -> return a JSON object
			// Cloud: returns form encoded content
			Map<String,Object> params = null;
			if(isCloud()) {
				params = parseFormEncodedValues(text); 
			} else {
				params = JsonObject.fromJson(text); 
			}
			String accessToken = toString(params.get("access_token"));
			String refreshToken = toString(params.get("refresh_token"));
			String issuedOn = toString(params.get("issued_on"));
			String expiresIn = toString(params.get("expires_in"));
			
			long _issuedOn = StringUtil.isNotEmpty(issuedOn) ? Long.parseLong(issuedOn) : (System.currentTimeMillis()-5*1000); 
			long _expiresIn = Long.parseLong(expiresIn);
			if(!isCloud()) {
				_expiresIn *= 1000; // On prem is in secs, Cloud in ms... Thanks IBM!
			}
			//_expiresIn = 30*1000;
			
			long _expiresOn = _issuedOn+_expiresIn;
			
			return new OAuth20Token(accessToken, refreshToken, _expiresOn);
		} catch(JsonException e) {
			throw new ServletException(e);
		}
	}
	
		
	//
	// Utilities
	// 
	protected static String toString(Object o) {
		return o!=null ? o.toString() : null;
	}
	protected static String encodeUrlParam(String s) {
		try {
			return URLEncoder.encode(s,"utf-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException();
		}
	}

	protected String tokenFromPostForm(HttpClient c, Map<String,Object> values) throws IOException, ServletException {
		try {
			FormUrlEncodedContent content = new FormUrlEncodedContent(values);
			// Connections on-prem does not support gzipped post, while cloud does...
			c.setGzipPost(false);
			return c.postAsText((String[])null,null,new HttpClient.HttpContentBinary(content));
		} catch (JsonException e) {
			throw new ServletException(e);
		}
	}

	protected boolean isExpiredToken(HttpException ex) {
		int code = ex.getCode();
		if(code==401) {
			return true;
		}
		return false;
	}
	

	protected static Map<String,Object> parseFormEncodedValues(String values) throws UnsupportedEncodingException {
		Map<String, Object> map = new HashMap<String, Object>();
		String[] pairs = StringUtil.splitString(values,'&');
		for (int i=0; i<pairs.length; i++) {
			String p = pairs[i];
			int pos = p.indexOf('=');
			if(pos>=0) {
				String key = URLDecoder.decode(p.substring(0, pos), "UTF-8");
				String val = URLDecoder.decode(p.substring(pos+1), "UTF-8");
				map.put(key,val);
			}
		}
		return map;
	}

	protected String calculateCallbackUrl(HttpServletRequest req) {
		String ctx = HttpUtil.getContextUrl(req);
		String url = PathUtil.concat(ctx, getCallbackPathinfo(), '/');
		return url;
	}
	
	protected void unauthorizedResponse(HttpServletResponse response, String message, Object...params) throws IOException {
		response.sendError(HttpClient.SC_INTERNAL_SERVER_ERROR, StringUtil.format(message,params));
	}	


	//
	// Helper
	//
	
	public static String getServerUrl(HttpServletRequest req) {
		StringBuilder b = new StringBuilder(64);
		return appendServerUrl(b, req).toString();
	}

	public static StringBuilder appendServerUrl(StringBuilder b, HttpServletRequest req) {
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
		return b;
	}

	public static String getContextUrl(HttpServletRequest req) {
		String serverUrl = getServerUrl(req);
		return PathUtil.concat(serverUrl, req.getContextPath(), '/');
	}
}
