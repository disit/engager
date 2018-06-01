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
package edu.unifi.disit.wallet_user_mngt.signup;

import javax.inject.Inject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.connect.web.ProviderSignInUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.NativeWebRequest;

import edu.unifi.disit.wallet_user_mngt.exception.SocialNotRecognizedException;
import edu.unifi.disit.wallet_user_mngt.service.ISecurityService;
import edu.unifi.disit.wallet_user_mngt.service.ISocialService;
import edu.unifi.disit.wallet_user_mngt.signin.MyProviderSignInController;

@Controller
public class SignupController {

	private static final Logger logger = LogManager.getLogger();

	@Autowired
	ISecurityService securityService;

	private final ProviderSignInUtils providerSignInUtils;

	@Autowired
	private ISocialService socialservice;

	@Inject
	public SignupController(ConnectionFactoryLocator connectionFactoryLocator,
			UsersConnectionRepository connectionRepository) {
		this.providerSignInUtils = new ProviderSignInUtils(connectionFactoryLocator, connectionRepository);
	}

	@RequestMapping(value = "/signup", method = RequestMethod.GET)
	public String signupForm(NativeWebRequest request) {
		Connection<?> connection = providerSignInUtils.getConnectionFromSession(request);
		if (connection != null) {
			try {
				logger.info(connection.createData().getAccessToken());
				String redirect = socialservice.registerOrLogin(connection.getApi(), request.getLocale());
				// if remember me, add cookie (we simulate a passing parameter in the http header)
				if (isRememberMeRequested(request, MyProviderSignInController.MY_REMEMBER_ME_COOKIE_KEY)) {
					// User u = securityService.findLoggedInUser();
					// UserDetails userDetails = userDetailsService.loadUserByUsername(u.getUsername());
					// UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, u.getPassword(), userDetails.getAuthorities());
					// myRememberMeServices.setAlwaysRemember(true);
					// myRememberMeServices.loginSuccess((HttpServletRequest) request.getNativeRequest(), (HttpServletResponse) request.getNativeResponse(), usernamePasswordAuthenticationToken);
					// myRememberMeServices.setAlwaysRemember(false);
					securityService.rememberMe((HttpServletRequest) request, (HttpServletResponse) request.getNativeResponse());
				}
				return redirect;
			} catch (SocialNotRecognizedException snre) {
				return "redirect:/user?error=KO";
			}
		} else
			return null;
	}

	private boolean isRememberMeRequested(NativeWebRequest request, String myRememberMeCookieKey) {
		HttpServletRequest requestHTTP = (HttpServletRequest) request.getNativeRequest();
		Cookie[] cs = ((HttpServletRequest) requestHTTP).getCookies();
		for (Cookie c : cs) {
			if (c.getName().equals(myRememberMeCookieKey))
				return true;
		}
		return false;
	}
}
