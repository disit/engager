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
package edu.unifi.disit.wallet_user_mngt.service;

import java.io.File;
import java.io.FileInputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
public class TokenAuthenticationService {

	private static String keyStorePath;

	private static UserDetailsServiceImpl userDetailsService;

	private static final Logger logger = LogManager.getLogger();

	static final long EXPIRATIONTIME = 14_400_000; // 4 hours (in milliseconds)
	// static final String SECRET = "ThisIsASecret";
	static final String TOKEN_PREFIX = "Bearer";
	static final String HEADER_STRING = "Authorization";

	@SuppressWarnings("static-access")
	@Autowired
	public TokenAuthenticationService(UserDetailsServiceImpl userDetailsService) {
		this.userDetailsService = userDetailsService;
	}

	@Value("${jwtkey.path}")
	@SuppressWarnings("static-access")
	public void setDatabase(String keyStorePath) {
		this.keyStorePath = keyStorePath;
	}

	public static void addAuthentication(HttpServletResponse res, String username) {

		Key key = getKey();

		logger.debug("adding JWT header for: {}", username);

		String JWT = Jwts.builder()
				.setSubject(username)
				.setExpiration(new Date(System.currentTimeMillis() + EXPIRATIONTIME))
				// .signWith(SignatureAlgorithm.HS512, SECRET)
				.signWith(SignatureAlgorithm.RS512, key)
				.compact();
		res.addHeader(HEADER_STRING, TOKEN_PREFIX + " " + JWT);
	}

	static public Authentication getAuthentication(HttpServletRequest request) {

		PublicKey key = getPublicKey();

		logger.debug("getting JWT auth frp, {}", request.toString());
		String token = request.getHeader(HEADER_STRING);
		if (token != null) {
			// parse the token.
			String username = Jwts.parser()
					// .setSigningKey(SECRET)
					.setSigningKey(key)
					.parseClaimsJws(token.replace(TOKEN_PREFIX, ""))
					.getBody()
					.getSubject();

			// put in the securitycontext the User detail
			org.springframework.security.core.userdetails.UserDetails user = userDetailsService.loadUserByUsername(username);
			// TODO what if the user is still not registered here???

			List<GrantedAuthority> grants = new ArrayList<>();// (Collection<? extends GrantedAuthority>) emptyList()
			// TODO give some grants here???

			return user != null ? new UsernamePasswordAuthenticationToken(user, null, grants) : null;
		}
		return null;
	}

	static private Key getKey() {
		Key key = null;
		File f = new File(keyStorePath);
		FileInputStream fis;

		try {
			fis = new FileInputStream(f);

			KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
			keystore.load(fis, "admin-areare".toCharArray());

			key = keystore.getKey("jwtkey", "admin-areare".toCharArray());

		} catch (Exception e) {
			logger.error("FAILED TO LOAD KEY. WE WILL NOT BE ABLE TO VERIFY JWTs.THINGS WILL BREAK", e);
		}
		return key;

	}

	static private PublicKey getPublicKey() {
		PublicKey publicKey = null;
		File f = new File(keyStorePath);
		FileInputStream fis;

		try {
			fis = new FileInputStream(f);

			KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
			keystore.load(fis, "admin-areare".toCharArray());

			Certificate cert = keystore.getCertificate("jwtkey");
			publicKey = cert.getPublicKey();
		} catch (Exception e) {
			logger.error("FAILED TO LOAD PUBLIC KEY. WE WILL NOT BE ABLE TO VERIFY JWTs.THINGS WILL BREAK", e);
		}
		return publicKey;
	}
}
