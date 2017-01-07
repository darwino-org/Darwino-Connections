/*!COPYRIGHT HEADER! 
 *
 * (c) Copyright Darwino Inc. 2014-2016.
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
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.darwino.commons.Platform;
import com.darwino.commons.httpclnt.SSLCertificateExtension;
import com.darwino.commons.net.SSLUnverified;
import com.darwino.commons.services.HttpServiceError;
import com.darwino.commons.util.StringUtil;
import com.darwino.commons.util.io.StreamUtil;
import com.darwino.ibm.connections.IbmConnections;
import com.darwino.j2ee.servlet.ServletExceptionEx;
import com.darwino.j2ee.servlet.server.AbstractHttpServlet;



/**
 * Main page servlet.
 * 
 */
@SuppressWarnings("serial")
public class DecoratorServlet extends AbstractHttpServlet {

	private IbmConnections connections;
	
	public DecoratorServlet() {
	}
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		String bean = getInitParameter(config,"bean");
		this.connections = (IbmConnections)Platform.getManagedBean(IbmConnections.BEAN_TYPE, bean);
		if(connections==null) {
			throw new ServletExceptionEx(null,"There is no available bean of type {0} for this application",IbmConnections.BEAN_TYPE);
		}
	}

	public IbmConnections getConnections() {
		return connections;
    }

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// Read the content
	}
	
//	protected String readConnectionsPage() {
//		connections.
//	}
	
	public void includeContent(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	}
	
	public void service(HttpServletRequest req) {
		try {
			String url = "https://tglc5demo.triloggroup.com/search/web/jsp/toolsHomepage.jsp";
	
	    	HttpURLConnection con = (HttpURLConnection)new URL(url).openConnection();
	    	if((con instanceof HttpsURLConnection) && SSLCertificateExtension.shouldTrustSSLCertificate(url)) {
	    		try {
	    			SSLUnverified.trustSSLCertificates((HttpsURLConnection)con);
	    		} catch(Exception ex) {
	    			throw HttpServiceError.error500(ex);
	    		}
	    	}
	    	con.setRequestMethod("GET");

	    	// We pass the headers from the requester
	    	for(Enumeration<String> it=req.getHeaderNames(); it.hasMoreElements(); ) {
	    		String name = it.nextElement();
	    		String value = req.getHeader(name);
	    		if(acceptRequestHeader(name, value)) {
	    			con.addRequestProperty(name, value);
	    		}
	    	}
	    	con.connect();
	    	
	    	int status = con.getResponseCode();
	    	if(status==200) {
				InputStream ris = null;
				try {
					String encoding = con.getContentEncoding();
			    	ris = con.getInputStream();
					StreamUtil.readString(ris, encoding);
				} finally {
					StreamUtil.close(ris);
				}
	    	} else {
				throw HttpServiceError.error500("Error {0} while reading Connections page");
	    	}
		} catch(IOException ex) {
			throw HttpServiceError.error500(ex);
		}
	}
	
	protected boolean acceptRequestHeader(String name, String value) {
		if(StringUtil.isEmpty(name)) {
			return false;
		}
		return true;
	}
	
}
