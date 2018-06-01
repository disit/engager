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

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.stereotype.Service;

import edu.unifi.disit.wallet_user_mngt.datamodel.Roletype;
import edu.unifi.disit.wallet_user_mngt.datamodel.UserDAO;

@Service
public class SecurityServiceImpl implements ISecurityService {

	@Autowired
	PersistentTokenBasedRememberMeServices myRememberMeServices;

	@Autowired
	private UserDetailsServiceImpl userDetailsService;

	@Autowired
	private UserDAO userDAO;

	@Override
	public String findLoggedInUsername() {
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return user.getUsername();
	}

	@Override
	public void autologin(String username, String password) {
		UserDetails userDetails = userDetailsService.loadUserByUsername(username);
		UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, password, userDetails.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
	}

	@Override
	public edu.unifi.disit.wallet_user_mngt.datamodel.User findLoggedInUser() {
		return userDAO.findByUsername(((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername());
	}

	@Override
	public String determineHomeUrl(Authentication authentication) {
		boolean isUser = false;
		boolean isManager = false;
		boolean isAdmin = false;
		Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
		for (GrantedAuthority grantedAuthority : authorities) {
			if (grantedAuthority.getAuthority().equals(Roletype.ROLE_USER.toString())) {
				isUser = true;
				break;
			} else if (grantedAuthority.getAuthority().equals(Roletype.ROLE_MANAGER.toString())) {
				isManager = true;
				break;
			} else if (grantedAuthority.getAuthority().equals(Roletype.ROLE_ADMIN.toString())) {
				isAdmin = true;
				break;
			}
		}
		if (isUser) {
			return "/user";
		} else if (isAdmin) {
			return "/admin";
		} else if (isManager) {
			return "/manager";
		} else {
			throw new IllegalStateException();
		}
	}

	@Override
	public String determineHomeUrl() {
		return determineHomeUrl(SecurityContextHolder.getContext().getAuthentication());
	}

	@Override
	public void rememberMe(HttpServletRequest request, HttpServletResponse response) {
		edu.unifi.disit.wallet_user_mngt.datamodel.User u = findLoggedInUser();
		UserDetails userDetails = userDetailsService.loadUserByUsername(u.getUsername());
		UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, u.getPassword(), userDetails.getAuthorities());
		myRememberMeServices.setAlwaysRemember(true);
		myRememberMeServices.loginSuccess(request, response, usernamePasswordAuthenticationToken);
		myRememberMeServices.setAlwaysRemember(false);
	}
}
