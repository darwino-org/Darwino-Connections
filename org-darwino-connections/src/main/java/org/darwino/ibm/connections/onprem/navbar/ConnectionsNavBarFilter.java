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

package org.darwino.ibm.connections.onprem.navbar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.darwino.ibm.connections.ConnectionsEnvironment;
import org.darwino.ibm.connections.ConnectionsSession;

import com.darwino.commons.json.JsonException;

/**
 * IBM Connections on-prem navbar filter.
 */
public class ConnectionsNavBarFilter implements Filter {

	public ConnectionsNavBarFilter() {
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest _request, ServletResponse _response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) _request;
		HttpServletResponse response = (HttpServletResponse) _response;

		if (shouldDisplayNavBar(request)) {
			try {
				NavBarOnPrem navBar = new NavBarOnPrem(ConnectionsSession.get());
	
				NavBarServletResponse navBarResp = new NavBarServletResponse(response);
				chain.doFilter(request, navBarResp);

				String body = navBarResp.getResponseAsString();
				initNavBar(navBar, body);
				
				response.setStatus(200);
				response.setCharacterEncoding("UTF-8");
				response.getWriter().println(navBar.composeFinalPage());
				return;
			} catch (JsonException ex) {
				throw new ServletException(ex);
			}
		}

		chain.doFilter(request, response);
	}
	
	protected void initNavBar(NavBarOnPrem navBar, String body) {
		navBar.setBody(body);
	}

	protected boolean shouldDisplayNavBar(HttpServletRequest request) {
		 // We inject the on prem navbar id we are onprem, installed in the
		 // same server (not using oauth)
		 ConnectionsEnvironment f = ConnectionsSession.get().getConnections();
		 return !f.isCloud() && !f.useOAuth();
	}

	private static class NavBarServletResponse extends HttpServletResponseWrapper {
		
		String encoding;
		ByteArrayOutputStream baos = new ByteArrayOutputStream(8192);
		
		PrintWriter pw;
		ServletOutputStream os;

		public NavBarServletResponse(HttpServletResponse response) {
			super(response);
		}

	    @Override
		public void setCharacterEncoding(String charset) {
	    	if(pw!=null) {
				throw new IllegalStateException("Writer is already created");
	    	}
	    	this.encoding = charset;
	    }
		
		public String getResponseAsString() throws UnsupportedEncodingException {
			if(pw!=null) {
				pw.flush();
			} else if(os!=null) {
				//os.flush();
			}
			if(encoding!=null) {
				return new String(baos.toByteArray(),encoding);
			}
			return new String(baos.toByteArray());
		}

		@Override
		public ServletOutputStream getOutputStream() throws IOException {
			if (pw!=null) {
				throw new IOException("Writer is already in use!");
			}
			if(os==null) {
				os = new ServletOutputStream() {
					@Override
					public void write(int param) throws IOException {
						baos.write(param);
					}
					@Override
					public void write(byte[] b, int off, int len) throws IOException {
						baos.write(b,off,len);
					}
				};
			}
			return os;
		}

		@Override
		public PrintWriter getWriter() throws IOException {
			// will error out, if in use
			if (os!=null) {
				throw new IOException("OutputStream is already in use!");
			}
			if(pw==null) {
				// Make sure we handle all the characters properly
				if(encoding==null) {
					encoding = "UTF-8";
				}
				pw = new PrintWriter(new OutputStreamWriter(baos,encoding));
				//pw = new TraceWriter(pw,new FileWriter("c:\\temp\\log.txt"));
			}
			return pw;
		}
	}
}
