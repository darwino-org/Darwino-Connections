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

import org.darwino.ibm.connections.ConnectionsSession;

import com.darwino.commons.httpclnt.HttpClient;
import com.darwino.commons.json.JsonException;
import com.darwino.commons.util.StringUtil;

/**
 * Get the IBM Connections NavBar.
 */
public class NavBarOnPrem {

	private ConnectionsSession session;
	private String page;
	private String title;
	private String body;
	
	public NavBarOnPrem(ConnectionsSession session) throws JsonException {
		this.session = session;
		this.page = readNavBar();
	}
	
	private String readNavBar() throws JsonException {
//		try {
//			//https://tglc5demo.triloggroup.com/search/web/jsp/containers/headerfooter.jsp
//			File f = new File("c:\\temp\\headerfooter.html");
//			String s = StreamUtil.readString(f);
//			return s;
//		} catch(IOException ex) {
//			throw new JsonException(ex);
//		}
		HttpClient c = session.createHttpClient();
		
		String s = c.getAsText(new String[]{"search","web","jsp","containers","headerfooter.jsp"});
		return s;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String composeFinalPage() {
		String finalPage = page;
		if(title!=null) {
			finalPage = StringUtil.replaceFirst(finalPage, "<!-- LCONN_CONTAINER_HEAD_START -->", title);
		}
		if(body!=null) {
			finalPage = StringUtil.replaceFirst(finalPage, "<!-- LCONN_CONTAINER_MAIN -->", body);
		}
		return finalPage;
	}
    
}
