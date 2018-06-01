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
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import edu.unifi.disit.wallet_user_mngt.datamodel.Roletype;
import edu.unifi.disit.wallet_user_mngt.datamodel.TraceInDAO;
import edu.unifi.disit.wallet_user_mngt.datamodel.User;
import edu.unifi.disit.wallet_user_mngt.exception.EmailExistsException;
// import edu.unifi.disit.wallet_user_mngt.exception.NotImplementedException;
import edu.unifi.disit.wallet_user_mngt.object.Response;
import edu.unifi.disit.wallet_user_mngt.service.UserServiceImpl;
import edu.unifi.disit.wallet_user_mngt.validator.UserValidator;

@RestController
public class UserRESTController {

	private static final Logger logger = LogManager.getLogger();

	@Autowired
	private TraceInDAO traceinRepo;

	@Autowired
	private UserServiceImpl userService;

	@Autowired
	private MessageSource messages;

	private static final String CAMPAIGN_PILOTA = "PILOTA";

	@RequestMapping(value = "/api/v1/deviceid", method = RequestMethod.POST)
	public Response addDeviceIdV1(@RequestParam("deviceid") String deviceid, @RequestParam(value = "lang", defaultValue = "en") String lang) {

		logger.debug("{} requested add device for {}, lang {}", userService.getLoggedUsername(), deviceid, lang);

		Response toreturn = new Response();

		userService.addDeviceIdForCurrentUser(deviceid);

		logger.debug("response {}", toreturn);

		return toreturn;
	}

	@RequestMapping(value = "/api/v1/lastlogin", method = RequestMethod.GET)
	public Date getLastLoginV1(@RequestParam(value = "lang", defaultValue = "en") String lang) {

		// simply throw NotImplementedException when this API will be obsolete
		// throw new NotImplementedException();

		logger.debug("{} requested last login, lang {}", userService.getLoggedUsername(), lang);

		Date toreturn = userService.setLastLogin(new Date());

		logger.debug("response {}", toreturn);

		return toreturn;
	}

	@RequestMapping(value = "/api/v1/welcomemsg", method = RequestMethod.GET)
	public List<String> getWelcomeMessagesV1(@RequestParam(value = "lang", defaultValue = "en") String lang) {

		logger.debug("{} requested welcome, lang {}", userService.getLoggedUsername(), lang);

		List<String> welcomeMessages = new ArrayList<String>();

		// ----------------------
		welcomeMessages.add(messages.getMessage("login.welcome", new String[] { userService.getLoggedUsername() }, new Locale(lang)));// benvenuto angelo

		// ----------------------
		Integer total_points = traceinRepo.countAllPointsForUserForCampaing(userService.getLoggedUsername(), CAMPAIGN_PILOTA);

		if ((total_points != null) && (total_points != 0))
			welcomeMessages.add(messages.getMessage("welcome.pilota1", new String[] { total_points.toString() }, new Locale(lang)));

		// ----------------------
		Calendar ca = Calendar.getInstance();
		ca.setTimeInMillis(System.currentTimeMillis());
		ca.add(Calendar.HOUR, -24 * 7);

		Integer points_7days = traceinRepo.countPointsForUserForCampaingFromDate(userService.getLoggedUsername(), CAMPAIGN_PILOTA, ca.getTime());

		if ((points_7days != null) && (points_7days != 0))
			welcomeMessages.add(messages.getMessage("welcome.pilota2", new String[] { points_7days.toString() }, new Locale(lang)));

		for (String s : welcomeMessages) {
			logger.debug("response {}", s);
		}

		return welcomeMessages;
	}

	@RequestMapping(value = "/api/v1/registration", method = RequestMethod.POST)
	public Response registrationV1(@RequestParam("username") String username, @RequestParam("password") String password, @RequestParam(value = "lang", defaultValue = "en") String lang) {

		logger.debug("{} requested registration, lang {}", username, lang);

		User u = new User(username, password);

		u.setPasswordConfirm(password);
		Errors er = new org.springframework.validation.BindException(u, "user");
		Response toreturn = new Response();

		UserValidator userValidator = new UserValidator(messages);

		userValidator.validate(u, er, new Locale(lang));

		// User registered = null;

		try {

			if (!er.hasErrors()) {
				// registered=
				userService.registerNewUserAccount(u, new Locale(lang), Roletype.ROLE_USER);
			} else {
				// toreturn.setErrorMessage(messages.getMessage("signin.usernamepasswordinvalid", null, new Locale(lang)));
				toreturn.setErrorMessage(er.getAllErrors().get(0).getDefaultMessage());
				return toreturn;
			}
		} catch (EmailExistsException e) {
			toreturn.setErrorMessage(e.getMessage());
			return toreturn;
		}

		// don't request registration for mobile scenario
		// eventPublisher.publishEvent(userService.prepareEventForRegistration(registered, new Locale(lang), servletContext.getContextPath()));

		logger.debug("response {}", toreturn);

		return toreturn;
	}

	@RequestMapping(value = "/api/v1/delete", method = RequestMethod.POST)
	public Response deleteUserV1(@RequestParam(value = "lang", defaultValue = "en") String lang) {

		Response toreturn = new Response();

		userService.deleteLoggedUser();

		return toreturn;
	}
}
