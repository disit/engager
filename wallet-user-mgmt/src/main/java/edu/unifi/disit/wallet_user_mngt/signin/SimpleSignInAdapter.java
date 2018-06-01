/*******************************************************************************
 * Engager
 *    Copyright (C) 2016-2018 DISIT Lab http://www.disit.org - University of Florence
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Affero General Public License as
 *    published by the Free Software Foundation, either version 3 of the
 *    License, or (at your option) any later version.
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Affero General Public License for more details.
 *    You should have received a copy of the GNU Affero General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package edu.unifi.disit.wallet_user_mngt.signin;

import javax.inject.Inject;

import org.springframework.social.connect.Connection;
import org.springframework.social.connect.web.SignInAdapter;
import org.springframework.web.context.request.NativeWebRequest;

//behaviour is depicted in signup!?
public class SimpleSignInAdapter implements SignInAdapter {

	// private final RequestCache requestCache;

	@Inject
	public SimpleSignInAdapter() {
		// this.requestCache = requestCache;
	}

	@Override
	public String signIn(String localUserId, Connection<?> connection, NativeWebRequest request) {
		return null;// never invoked, it just need for remmber-me scenario
	}

	// private String extractOriginalUrl(NativeWebRequest request) {
	// HttpServletRequest nativeReq = request.getNativeRequest(HttpServletRequest.class);
	// HttpServletResponse nativeRes = request.getNativeResponse(HttpServletResponse.class);
	// SavedRequest saved = requestCache.getRequest(nativeReq, nativeRes);
	// if (saved == null) {
	// return null;
	// }
	// requestCache.removeRequest(nativeReq, nativeRes);
	// removeAutheticationAttributes(nativeReq.getSession(false));
	// return saved.getRedirectUrl();
	// }
	//
	// private void removeAutheticationAttributes(HttpSession session) {
	// if (session == null) {
	// return;
	// }
	// session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
	// }
	//
}
