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

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.web.authentication.rememberme.AbstractRememberMeServices;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.connect.web.ProviderSignInController;
import org.springframework.social.connect.web.SignInAdapter;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.servlet.view.RedirectView;

public class MyProviderSignInController extends ProviderSignInController {

	public static final String MY_REMEMBER_ME_COOKIE_KEY = "myremember";

	public MyProviderSignInController(ConnectionFactoryLocator connectionFactoryLocator, UsersConnectionRepository usersConnectionRepository, SignInAdapter signInAdapter) {
		super(connectionFactoryLocator, usersConnectionRepository, signInAdapter);
	}

	@Override
	@RequestMapping(value = "/{providerId}", method = RequestMethod.POST)
	public RedirectView signIn(@PathVariable String providerId, NativeWebRequest request) {
		// if remember me, set a cookie with remember-me
		if (isRememberMeRequested((NativeWebRequest) request, AbstractRememberMeServices.DEFAULT_PARAMETER)) {
			Cookie c = new Cookie(MY_REMEMBER_ME_COOKIE_KEY, "on");
			c.setPath(getCookiePath((HttpServletRequest) request.getNativeRequest()));
			c.setMaxAge(300);// 5 minutes to login
			((HttpServletResponse) request.getNativeResponse()).addCookie(c);
		}
		return super.signIn(providerId, request);
	}

	private boolean isRememberMeRequested(NativeWebRequest request, String parameter) {
		String paramValue = request.getParameter(parameter);
		if (paramValue != null) {
			if (paramValue.equalsIgnoreCase("true") || paramValue.equalsIgnoreCase("on")
					|| paramValue.equalsIgnoreCase("yes") || paramValue.equals("1")) {
				return true;
			}
		}
		return false;
	}

	private String getCookiePath(HttpServletRequest request) {
		String contextPath = request.getContextPath();
		return contextPath.length() > 0 ? contextPath : "/";
	}
}
