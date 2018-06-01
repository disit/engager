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
package edu.unifi.disit.wallet_user_mngt.controller;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import edu.unifi.disit.wallet_user_mngt.datamodel.PasswordResetToken;
import edu.unifi.disit.wallet_user_mngt.datamodel.User;
import edu.unifi.disit.wallet_user_mngt.datamodel.UserDAO;
import edu.unifi.disit.wallet_user_mngt.event.OnPreparationEmailCompleteEvent;
import edu.unifi.disit.wallet_user_mngt.exception.UserNotFoundException;
import edu.unifi.disit.wallet_user_mngt.service.ISecurityService;
import edu.unifi.disit.wallet_user_mngt.service.IUserService;

@Controller
public class ForgotPasswordController {

	private static final Logger logger = LogManager.getLogger();

	@Autowired
	private MessageSource messages;

	@Autowired
	private IUserService userService;

	@Autowired
	private ISecurityService securityService;

	@Autowired
	private UserDAO userDAO;

	@Autowired
	ApplicationEventPublisher eventPublisher;

	@Value("${application.url}")
	private String myappuri;

	@Value("${mobile.application.url}")
	private String mobileappuri;

	@RequestMapping(value = "/forgotpwd", method = RequestMethod.GET)
	public ModelAndView forgotpassword() {

		logger.debug("/forgotpwd invoked");

		ModelAndView mav = new ModelAndView("forgotpwd");

		return mav;
	}

	@RequestMapping(value = "/forgotpwd", method = RequestMethod.POST)
	public ModelAndView resetpassword(@RequestParam("username") String userEmail, WebRequest request) throws UserNotFoundException {

		ModelAndView mav = new ModelAndView("feedback");

		User user = userDAO.findByUsername(userEmail);

		if (user == null) {
			mav.addObject("error", messages.getMessage("resetpwd.ko.notfound", new Object[] { userEmail }, LocaleContextHolder.getLocale()));
			return mav;
		}

		eventPublisher.publishEvent(prepareEventForRegistration(user, request.getLocale()));

		mav.addObject("user", user);
		return mav;
	}

	private OnPreparationEmailCompleteEvent prepareEventForRegistration(edu.unifi.disit.wallet_user_mngt.datamodel.User user, Locale locale) {
		String token = UUID.randomUUID().toString();
		userService.createPasswordResetTokenForUser(user, token);

		String recipientAddress = user.getUsername();
		String subject = "Reset Password";
		String testo = messages.getMessage("resetpwd.ok.invitation", null, locale) + " " + myappuri + "/user/changepwd?id=" + user.getId() + "&token=" + token;

		List<String> testos = new ArrayList<String>();

		testos.add(testo);

		return new OnPreparationEmailCompleteEvent(recipientAddress, subject, testos, locale);
	}

	// called from email
	@RequestMapping(value = "/user/changepwd", method = RequestMethod.GET)
	public ModelAndView changepassword(@RequestParam("id") Long id, @RequestParam("token") String token, Locale locale) {

		logger.debug("/changepassword invoked");

		ModelAndView mav = new ModelAndView("updatepwd");

		PasswordResetToken passToken = userService.getPasswordResetToken(token);
		if (passToken == null) {
			mav.setViewName("feedback");
			mav.addObject("error", messages.getMessage("resetpwd.ko.invalidToken", null, locale));
			return mav;
		}
		User user = passToken.getUser();
		if (user == null || user.getId().longValue() != id.longValue()) {
			mav.setViewName("feedback");
			mav.addObject("error", messages.getMessage("resetpwd.ko.invalidToken", null, locale));
			return mav;
		}
		Calendar cal = Calendar.getInstance();
		if ((passToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
			mav.setViewName("feedback");
			mav.addObject("error", messages.getMessage("resetpwd.ko.expiredToken", null, locale));
			return mav;
		}

		securityService.autologin(user.getUsername(), user.getPassword());

		return mav;
	}

	// called when the new password is saved
	@RequestMapping(value = "/user/savepwd", method = RequestMethod.POST)
	@PreAuthorize("hasRole('READ_PRIVILEGE')")
	public ModelAndView savepassword(@RequestParam("password") String password, @RequestParam("altpassword") String altpassword, Locale locale, HttpServletRequest request) {

		logger.debug("/savepassword invoked");

		ModelAndView mav = new ModelAndView("updatepwd");

		if ((password.length() == 0) && (altpassword.length() == 0)) {
			mav.addObject("error", messages.getMessage("registration.ko.passwordempty", null, locale));
		} else if (password.equals(altpassword)) {

			userService.changePasswordForLogged(password);

			try {
				mav.setViewName("redirect:" + securityService.determineHomeUrl());
				mav.addObject("message", messages.getMessage("resetpwd.ok.passwordchanged", null, locale));
			} catch (Exception e) {
				logger.error("errore, probably not granted to continue", e);
				mav.setViewName("redirect:/home/1");
				mav.addObject("message", messages.getMessage("resetpwd.ok.notenable", null, locale));
				// logout the user, since it is not enabled
				try {
					request.logout();
				} catch (ServletException e1) {
					logger.error("error", e1);
				}
			}
		} else {
			mav.addObject("error", messages.getMessage("validator.password.notmatching", null, locale));
		}

		return mav;
	}

	@RequestMapping(value = "/forgotpwd/redirect", method = RequestMethod.GET)
	public RedirectView redirectresetpassword(@RequestParam("id") String id, @RequestParam("token") String token) throws UserNotFoundException {

		RedirectView rv = new RedirectView(mobileappuri + "://resetpassword?id=" + id + "&token=" + token);
		rv.setHttp10Compatible(false);

		return rv;
	}
}
