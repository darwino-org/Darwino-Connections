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

import org.darwino.ibm.connections.util.IBMOAuth20Dance;

import com.darwino.commons.util.StringUtil;

/**
 * IBM Connections factory.
 */
public abstract class ConnectionsFactory {
	
	private static ConnectionsFactory instance;
	public static ConnectionsFactory get() {
		return instance;
	}
	
	private String url;

	private String clientId;
	private String clientSecret;

	private IBMOAuth20Dance dance;	
	
	protected ConnectionsFactory(String url, String clientId, String clientSecret) {
		ConnectionsFactory.instance = this;
		this.url = url;
		this.clientId = clientId;
		this.clientSecret = clientSecret;

		if(StringUtil.isNotEmpty(clientId) && StringUtil.isNotEmpty(clientSecret)) {
			this.dance = new IBMOAuth20Dance(clientId, clientSecret) {
				@Override
				public boolean isTrustAllSSLCertificates() {
					return ConnectionsFactory.this.isTrustSSLCertificates();
				}
			};
		}
	}
	

	/**
	 * Returns true if it should use OAuth as the authentication mechanism.
	 * This is always true for Connections cloud, and an option for on-premises
	 * @return
	 */
	public boolean useOAuth() {
		return dance!=null;
	}

	public IBMOAuth20Dance getDance() {
		return dance;
	}

	public String getUrl() {
		return url;
	}

	public String getClientId() {
		return clientId;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	// Enable that for dev purposes...
	public boolean isTrustSSLCertificates() {
		return true;
	}

	/**
	 * Checks if the connections server is cloud or on-prem
	 * @return
	 */
	public abstract boolean isCloud();

	/**
	 * Starts the authentication mechanism.
	 * @param session
	 */
	public abstract boolean authenticate(ConnectionsSession session) throws IOException, ServletException;
}
