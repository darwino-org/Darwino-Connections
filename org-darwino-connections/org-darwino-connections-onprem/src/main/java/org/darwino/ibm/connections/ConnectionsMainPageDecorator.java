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

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.darwino.commons.Platform;
import com.darwino.ibm.connections.IbmConnections;
import com.darwino.j2ee.servlet.ServletExceptionEx;



/**
 * Class used to decorate.
 * 
 */
public class ConnectionsMainPageDecorator extends PageDecorator {

	private IbmConnections connections;
	
	public ConnectionsMainPageDecorator() {
	}
	
	@Override
	public void init(DecoratorServlet servlet) throws ServletException {
		super.init(servlet);

		ServletConfig config = servlet.getServletConfig(); 
		
		String bean = servlet.getInitParameter(config,"bean");
		this.connections = (IbmConnections)Platform.getManagedBean(IbmConnections.BEAN_TYPE, bean);
		if(connections==null) {
			throw new ServletExceptionEx(null,"There is no available bean of type {0} for this application",IbmConnections.BEAN_TYPE);
		}
	}

	public IbmConnections getConnections() {
		return connections;
    }

	@Override
	public void process(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	}
}
