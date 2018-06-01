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

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

public class MyWebFailureHandler extends SimpleUrlAuthenticationFailureHandler {

	private MessageSource messages;

	public MyWebFailureHandler(String defaultURI, MessageSource messages) {
		super(defaultURI);
		this.messages = messages;
	}

	@Override
	public void onAuthenticationFailure(HttpServletRequest request,
			HttpServletResponse response, AuthenticationException exception)
			throws ServletException, IOException {

		String referer = request.getHeader("referer");

		if (!referer.contains("home")) {
			super.onAuthenticationFailure(request, response, exception);

		} else {
			if (exception instanceof DisabledException) {
				getRedirectStrategy().sendRedirect(request, response, "/home?message=" + messages.getMessage("signin.notenabled", null, LocaleContextHolder.getLocale()));
			} else {
				getRedirectStrategy().sendRedirect(request, response, "/home?error");
			}
		}
	}
}
