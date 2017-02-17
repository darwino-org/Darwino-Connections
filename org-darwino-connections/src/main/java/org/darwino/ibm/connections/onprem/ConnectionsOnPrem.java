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

package org.darwino.ibm.connections.onprem;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.ServletException;

import org.darwino.ibm.connections.ConnectionsEnvironment;
import org.darwino.ibm.connections.ConnectionsSession;
import org.darwino.ibm.connections.ConnectionsUser;

/**
 * IBM Connections on-prem factory.
 */
public class ConnectionsOnPrem extends ConnectionsEnvironment {
	
	// Single sign-on
	public ConnectionsOnPrem(String url) {
		super(url,null,null);
	}
	
	// OAuth
	public ConnectionsOnPrem(String url, String clientId, String clientSecret) {
		super(url,clientId,clientSecret);
	}
	
	@Override
	public String toString() {
		return "IBM Connections On-Premises";
	}

	@Override
	public boolean isCloud() {
		return false;
	}

	@Override
	public boolean authenticate(ConnectionsSession session) throws IOException, ServletException {
		if(useOAuth()) {
			getDance().startOAuthDance(session);
			return true;
		} else {
			Principal p = session.getHttpRequest().getUserPrincipal();
			if(p!=null) {
				// We could read more user info by calling the VMM directory (or LDAP...)
				String dn = p.getName();
				ConnectionsUser user=new ConnectionsUser(dn);
				session.setCurrentUser(user);
			} else {
				session.setCurrentUser(ConnectionsUser.ANONYMOUS_USER);
			}
			return false;
		}		
	}
}
