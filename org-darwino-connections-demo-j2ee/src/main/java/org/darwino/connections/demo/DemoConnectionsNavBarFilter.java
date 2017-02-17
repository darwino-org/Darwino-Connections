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

import javax.servlet.http.HttpServletRequest;

import org.darwino.ibm.connections.onprem.navbar.ConnectionsNavBarFilter;
import org.darwino.ibm.connections.onprem.navbar.NavBarOnPrem;

import com.darwino.commons.util.PathUtil;

/**
 * IBM Connections Filter.
 */
public class DemoConnectionsNavBarFilter extends ConnectionsNavBarFilter {
	
	public DemoConnectionsNavBarFilter() {
	}
	
	@Override
	protected void initNavBar(NavBarOnPrem navBar, String body) {
		super.initNavBar(navBar, body);
		navBar.setTitle("IBM Connections Demo");
	}

	@Override
	protected boolean shouldDisplayNavBar(HttpServletRequest request) {
		if(super.shouldDisplayNavBar(request)) {
			// only for -nav.jsp
			String sp = request.getServletPath();
			String pi = request.getPathInfo();
			String s = PathUtil.concat(sp,pi);
			if(s.endsWith("-nav.jsp")) {
				return true;
			}
		}
		return false;
	}
}
