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

import java.util.Calendar;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import edu.unifi.disit.wallet_user_mngt.datamodel.Roletype;
import edu.unifi.disit.wallet_user_mngt.datamodel.User;
import edu.unifi.disit.wallet_user_mngt.datamodel.VerificationToken;
import edu.unifi.disit.wallet_user_mngt.datamodel.VerificationTokenDAO;
import edu.unifi.disit.wallet_user_mngt.exception.EmailExistsException;
import edu.unifi.disit.wallet_user_mngt.exception.InvalidReCaptchaException;
import edu.unifi.disit.wallet_user_mngt.service.ICaptchaService;
import edu.unifi.disit.wallet_user_mngt.service.ISecurityService;
import edu.unifi.disit.wallet_user_mngt.service.IUserService;
import edu.unifi.disit.wallet_user_mngt.validator.UserValidator;

@Controller
public class RegistrationController {

	// private static final Logger logger = LogManager.getLogger();

	@Autowired
	private ICaptchaService captchaService;

	@Autowired
	private MessageSource messages;

	@Autowired
	private ISecurityService securityService;

	@Autowired
	private VerificationTokenDAO verificationTokenDAO;

	@Autowired
	private IUserService userService;

	@Autowired
	ApplicationEventPublisher eventPublisher;

	@RequestMapping(value = "/registration", method = RequestMethod.GET)
	public String registration(Model model) {

		model.addAttribute("user", new User());

		return "registration";
	}

	@RequestMapping(value = "/registration", method = RequestMethod.POST)
	public ModelAndView registration(@ModelAttribute User user, BindingResult bindingResult, HttpServletRequest request) throws InvalidReCaptchaException {

		String response = request.getParameter("g-recaptcha-response");
		try {
			captchaService.processResponse(response, request);
		} catch (InvalidReCaptchaException e) {
			// if the email was already taken, return error immediatly
			ModelAndView mav = new ModelAndView("registration", "user", user);
			mav.addObject("captchaerror", messages.getMessage("captchaerror.notvalid", null, LocaleContextHolder.getLocale()));
			return mav;
		}

		UserValidator userValidator = new UserValidator(messages);
		userValidator.validate(user, bindingResult);

		// check other validation error (from user's field, i.e. not null, ...)
		if (bindingResult.hasErrors()) {
			return new ModelAndView("registration", "user", user);
		}

		User registered = null;

		try {
			registered = userService.registerNewUserAccount(user, request.getLocale(), Roletype.ROLE_USERNOTENABLED);
		} catch (EmailExistsException e) {
			// if the email was already taken, return error immediatly
			bindingResult.rejectValue("username", "registration.ko.emailalreadytaken");
			return new ModelAndView("registration", "user", user);
		}

		eventPublisher.publishEvent(userService.prepareEventForRegistration(registered, request.getLocale()));

		return new ModelAndView("feedback", "user", user);
	}

	@RequestMapping(value = "/confirmemail", method = RequestMethod.GET)
	public ModelAndView confirmEmail(HttpServletRequest request) throws InvalidReCaptchaException {

		User user = securityService.findLoggedInUser();

		eventPublisher.publishEvent(userService.prepareEventForRegistration(user, request.getLocale()));

		return new ModelAndView("feedback", "user", user);
	}

	@RequestMapping(value = "/signin", method = RequestMethod.GET)
	public String signin(Model model, String error) {

		if (error != null)
			model.addAttribute("error", messages.getMessage("signin.usernamepasswordinvalid", null, LocaleContextHolder.getLocale()));

		return "signin";
	}

	@RequestMapping(value = "/confirm", method = RequestMethod.GET)
	public String registrationfeedback(Model model, @RequestParam("token") String token) {

		VerificationToken verificationToken = userService.getVerificationToken(token);

		// 1-check this token exist
		if (verificationToken == null) {
			model.addAttribute("error", messages.getMessage("registration.ko.notvalid", null, LocaleContextHolder.getLocale()));
			return "feedback";
		}

		// 2-check this token was not already used
		if (verificationToken.getVerified() == true) {
			model.addAttribute("error", messages.getMessage("registration.ko.alreadyused", null, LocaleContextHolder.getLocale()));
			return "feedback";
		}

		// 3-check this token is still valid
		Calendar cal = Calendar.getInstance();
		if ((verificationToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
			model.addAttribute("error", messages.getMessage("registration.ko.expired", null, LocaleContextHolder.getLocale()));
			return "feedback";
		}

		User user = verificationToken.getUser();
		user.setRoletype(Roletype.ROLE_USER);// basic role is ROLE_USER
		user.setRegistrationDate(new Date());
		userService.saveRegisteredUser(user);

		verificationToken.setVerified(true);
		verificationTokenDAO.save(verificationToken);

		securityService.autologin(user.getUsername(), user.getPassword());

		return "redirect:/user?message=" + messages.getMessage("registration.ok", null, LocaleContextHolder.getLocale());
	}
}
