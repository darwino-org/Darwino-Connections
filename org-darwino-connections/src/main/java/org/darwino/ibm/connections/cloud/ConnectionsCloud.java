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

package org.darwino.ibm.connections.cloud;

import java.io.IOException;

import javax.servlet.ServletException;

import org.darwino.ibm.connections.ConnectionsEnvironment;
import org.darwino.ibm.connections.ConnectionsSession;


/**
 * IBM Connections cloud factory.
 */
public class ConnectionsCloud extends ConnectionsEnvironment {

	// callback
	//   https://localhost:8080/darwino-connections/dwo_auth_ibmconn_oa2
		
	// Connections cloud production
	public static final String URL_NA	= "https://apps.na.collabserv.com";
	public static final String URL_EU	= "https://apps.ce.collabserv.com";
	
	// Connection cloud test
	public static final String URL_CA1	= "https://apps.collabservnext.com";
	
	public ConnectionsCloud(String url, String clientId, String clientSecret) {
		super(url,clientId,clientSecret);
	}
	
	@Override
	public String toString() {
		return "IBM Connections Cloud";
	}
	
	@Override
	public boolean isCloud() {
		return true;
	}

	@Override
	public boolean authenticate(ConnectionsSession session) throws IOException, ServletException {
		getDance().startOAuthDance(session);
		return true;
	}
}
