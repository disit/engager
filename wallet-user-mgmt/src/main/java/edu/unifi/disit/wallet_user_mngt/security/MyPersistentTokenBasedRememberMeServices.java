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

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

public class MyPersistentTokenBasedRememberMeServices extends PersistentTokenBasedRememberMeServices {

	private static final Logger logger = LogManager.getLogger();

	PersistentTokenRepository mytokenRepository;

	public MyPersistentTokenBasedRememberMeServices(String key, UserDetailsService userDetailsService, PersistentTokenRepository tokenRepository) {
		super(key, userDetailsService, tokenRepository);

		mytokenRepository = tokenRepository;

		logger.debug("MyPersistentTokenBasedRememberMeServices created");
	}

	@Override
	public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

		Cookie[] cs = request.getCookies();
		if (cs != null)
			for (Cookie c : cs) {

				logger.debug("got cookie name: {}", c.getName());

				if (c.getName().equals("remember-me")) {

					logger.debug("got cookie: {}", c.getValue());
					logger.debug("decoding cookie: {}", decodeCookie(c.getValue())[0]);

					PersistentRememberMeToken prt = mytokenRepository.getTokenForSeries(decodeCookie(c.getValue())[0]);

					if (prt != null)
						mytokenRepository.removeUserTokens(prt.getUsername());

					prt = mytokenRepository.getTokenForSeries(decodeCookie(c.getValue())[0]);
					if (prt != null)
						logger.debug("still alive");
					else
						logger.debug("finally delated!");
				}
			}

		super.logout(request, response, authentication);
	}
}
