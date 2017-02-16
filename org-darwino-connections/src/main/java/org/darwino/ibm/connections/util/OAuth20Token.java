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


package org.darwino.ibm.connections.util;

import com.darwino.commons.httpclnt.HttpClient.OAuth2Authenticator;

/**
 * IBM OAuth 2.0 token
 */
@SuppressWarnings("serial")
public class OAuth20Token extends OAuth2Authenticator {

	private String refreshToken;
	private long expiresOn;
	
	public OAuth20Token(String accessToken, String refreshToken, long expiresOn) {
		super(accessToken);
		this.refreshToken = refreshToken;
		this.expiresOn = expiresOn;
	}

	public String getRefreshToken() {
		return refreshToken;
	}
	public long getExpiresOn() {
		return expiresOn;
	}
	
	public long getExpirationMargin() {
		return 60*1000; // 1 mins by default
	}
	
	public boolean isExpired() {
		if(getExpiresOn()>0) {
			long now = System.currentTimeMillis();
			long exp = getExpiresOn()-getExpirationMargin();
			return now > exp;
		}
		return false;
	}
}	
