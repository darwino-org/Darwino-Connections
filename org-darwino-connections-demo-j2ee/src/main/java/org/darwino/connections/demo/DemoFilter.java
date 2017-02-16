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

package org.darwino.connections.demo;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

import org.darwino.ibm.connections.ConnectionsFilter;
import org.darwino.ibm.connections.cloud.ConnectionsCloud;
import org.darwino.ibm.connections.onprem.ConnectionsOnPrem;

import com.darwino.commons.util.StringUtil;
import com.darwino.commons.util.io.StreamUtil;

/**
 * IBM Connections Filter.
 */
public class DemoFilter extends ConnectionsFilter {

	public DemoFilter() {
	}
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		super.init(filterConfig);

		
		// If running in TOMCAT, then we can be on prem or on the cloud
		//   -> The app is not deployed on a Connections server
		// Else, we assume that the app is on WAS, running on a Connections server
		String home = System.getProperty("catalina.home");
		if(StringUtil.isNotEmpty(home)) {
			// TOMCAT
			createConnectionsCloudOAuth(filterConfig);
			createConnectionsOnPremOAuth(filterConfig);
		} else {
			// WEBSPHERE
			createConnectionsOnPremSSO(filterConfig);
		}
	}
	
	//
	// No way for me to share my user/password and OAuth keys in the source code, come on!
	//
	// Instead, I put them in a property file located in tomcat home dir, which contains something like:
	//		connections.cloud.url=https://apps.collabservnext.com
	//		connections.cloud.clientId=xxxx
	//		connections.cloud.clientSecret=yyyy
	//
	//		connections.ovprem.url=https://tglc5demo.triloggroup.com
	//		connections.onprem.clientId=xxxx
	//		connections.onprem.clientSecret=yyyy
	//
	// Note that Darwino properties makes it easier as well, as it handles external properties.
	//
	
	@Override
	protected void createConnectionsCloudOAuth(FilterConfig filterConfig) {
		try {
			String home = System.getProperty("catalina.home");
			if(StringUtil.isNotEmpty(home)) {
				File file = new File(home,"connections.properties");
				if(file.exists()) {
					Properties env = new Properties();
					InputStream is = new FileInputStream(file);
					try {
						env.load(is);
						String url = (String)env.get("connections.cloud.url");	
						String clientId = (String)env.get("connections.cloud.clientId");
						String clientSecret = (String)env.get("connections.cloud.clientSecret");
						new ConnectionsCloud(url, clientId, clientSecret);
					} finally {
						StreamUtil.close(is);
					}
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void createConnectionsOnPremOAuth(FilterConfig filterConfig) {
		try {
			String home = System.getProperty("catalina.home");
			if(StringUtil.isNotEmpty(home)) {
				File file = new File(home,"connections.properties");
				if(file.exists()) {
					Properties env = new Properties();
					InputStream is = new FileInputStream(file);
					try {
						env.load(is);
						String url = (String)env.get("connections.onprem.url");	
						String clientId = (String)env.get("connections.onprem.clientId");
						String clientSecret = (String)env.get("connections.onprem.clientSecret");
						new ConnectionsOnPrem(url, clientId, clientSecret);
					} finally {
						StreamUtil.close(is);
					}
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void createConnectionsOnPremSSO(FilterConfig filterConfig) {
		try {
			// We hard code the machine here... 
			String url = "https://tglc5demo.triloggroup.com";	
			new ConnectionsOnPrem(url);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}
