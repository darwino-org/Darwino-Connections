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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.darwino.ibm.connections.cloud.ConnectionsCloud;
import org.darwino.ibm.connections.onprem.ConnectionsOnPrem;
import org.darwino.ibm.connections.util.IBMOAuth20Dance;

import com.darwino.commons.util.StringUtil;
import com.darwino.commons.util.io.StreamUtil;

/**
 * IBM Connections Filter.
 */
public class ConnectionsFilter implements Filter {
	
	public ConnectionsFilter() {
	}

	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// Initialize the Connections Platform here or in a derived class!
		//createConnectionsOnPremOAuth();
		//createConnectionsOnPremSSO();
	}
	protected void createConnectionsCloudOAuth(FilterConfig filterConfig) {
		throw new IllegalStateException("Not implemented");
	}
	protected void createConnectionsOnPremOAuth(FilterConfig filterConfig) {
		throw new IllegalStateException("Not implemented");
	}
	protected void createConnectionsOnPremSSO(FilterConfig filterConfig) {
		throw new IllegalStateException("Not implemented");
	}
	

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest _request, ServletResponse _response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest)_request;
		HttpServletResponse response = (HttpServletResponse)_response;
		
		if(ConnectionsSession.isAvailable()) {
			chain.doFilter(request, response);
		} else {
			ConnectionsSession s = new ConnectionsSession(request, response);
			ConnectionsSession.push(s);
			try {
				IBMOAuth20Dance dance = ConnectionsFactory.get().getDance();
				if(dance!=null && dance.isCallbackRequest(s)) {
					dance.processCallbackRequest(s);
				} else {
					if(!s.isAuthenticated()) {
						if(s.authenticate()) {
							return;
						}
					}
					chain.doFilter(request, response);
				}
			} finally {
				ConnectionsSession.pop();
			}
		}
	}
	
	protected boolean shouldAuthenticate(HttpServletRequest request) {
		return true;
	}
}
