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
package edu.unifi.disit.wallet_user_mngt.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import edu.unifi.disit.wallet_user_mngt.service.ISecurityService;
import edu.unifi.disit.wallet_user_mngt.service.TokenAuthenticationService;

public class MyWebSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

	// passed from outside since it happens to be null otherwise
	ISecurityService securityService;

	public MyWebSuccessHandler(ISecurityService securityService) {
		super();
		this.securityService = securityService;
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request,
			HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

		// jwt header
		TokenAuthenticationService.addAuthentication(response, authentication.getName());

		String referer = request.getHeader("Referer");

		if (referer.contains("home")) {
			handle(request, response, authentication);
			clearAuthenticationAttributes(request);
		} else
			super.onAuthenticationSuccess(request, response, authentication);
	}

	protected void handle(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

		String targetUrl = securityService.determineHomeUrl(authentication);

		if (response.isCommitted()) {
			return;
		}

		getRedirectStrategy().sendRedirect(request, response, targetUrl);
	}
}
