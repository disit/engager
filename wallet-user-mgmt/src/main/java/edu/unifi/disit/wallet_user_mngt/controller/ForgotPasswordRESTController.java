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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.security.web.authentication.rememberme.AbstractRememberMeServices;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.unifi.disit.commons.datamodel.SOType;
import edu.unifi.disit.commons.utils.Utils;
import edu.unifi.disit.wallet_user_mngt.datamodel.PasswordResetToken;
import edu.unifi.disit.wallet_user_mngt.datamodel.User;
import edu.unifi.disit.wallet_user_mngt.datamodel.UserDAO;
import edu.unifi.disit.wallet_user_mngt.event.EmailScenarioType;
import edu.unifi.disit.wallet_user_mngt.event.OnPreparationEmailCompleteEvent;
import edu.unifi.disit.wallet_user_mngt.object.Response;
import edu.unifi.disit.wallet_user_mngt.service.ISecurityService;
import edu.unifi.disit.wallet_user_mngt.service.IUserService;

@RestController
public class ForgotPasswordRESTController {

	@Value("${application.url}")
	private String myappuri;

	private static final Logger logger = LogManager.getLogger();

	@Autowired
	private IUserService userService;

	@Autowired
	private UserDAO userDAO;

	@Autowired
	private MessageSource messages;

	@Autowired
	ApplicationEventPublisher eventPublisher;

	@Autowired
	private ISecurityService securityService;

	@RequestMapping(value = "/api/v1/forgotpwd", method = RequestMethod.POST)
	public Response resetpasswordV1(@RequestParam("username") String userEmail, @RequestParam("appid") String appid, @RequestParam(value = "lang", defaultValue = "en") String lang) throws IOException {

		Response toreturn = new Response();

		logger.debug("resetpassword for {}, appid {}, lang {}", appid, userEmail, lang);

		Locale locale = new Locale(lang);

		User user = userDAO.findByUsername(userEmail);

		if (user == null) {
			toreturn.setResult(false);
			toreturn.setMessage(messages.getMessage("resetpwd.ko.notfound", new Object[] { userEmail }, locale));
			return toreturn;
		}

		try {
			eventPublisher.publishEvent(prepareEventForEMAIL(user, locale, appid));
		} catch (Exception e) {
			toreturn.setResult(false);
			toreturn.setMessage(messages.getMessage("resetpwd.ko.notreacheable", new Object[] { userEmail }, locale));
			return toreturn;
		}

		toreturn.setMessage(messages.getMessage("resetpwd.ok.emailsent", null, locale));

		logger.debug("response {}", toreturn);

		return toreturn;
	}

	private OnPreparationEmailCompleteEvent prepareEventForEMAIL(edu.unifi.disit.wallet_user_mngt.datamodel.User user, Locale locale, String appid) throws IOException {
		String token = UUID.randomUUID().toString();
		userService.createPasswordResetTokenForUser(user, token);

		String recipientAddress = user.getUsername();
		String subject = "Reset Password";

		// if we're in scenario "local", we wrote on disk this info since are used by testing
		if (System.getProperty("spring.profiles.active").equals("local")) {
			FileOutputStream fos = new FileOutputStream("forgotpwd.dat");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(user.getId());
			oos.writeObject(token);
			oos.close();
		}

		SOType so = Utils.extractSOType(appid);

		logger.debug("the so to customize the email is {}", so);

		String testo = myappuri + "/forgotpwd/redirect?id=" + user.getId() + "&token=" + token;

		List<String> testos = new ArrayList<String>();
		testos.add(testo);

		return new OnPreparationEmailCompleteEvent(recipientAddress, subject, testos, locale, EmailScenarioType.FORGOT_PWD);
	}

	// called when the new password is saved
	@RequestMapping(value = "/api/v1/savepwd", method = RequestMethod.POST)
	public Response savepasswordV1(@RequestParam("id") Long id, @RequestParam("token") String token, @RequestParam("password") String password, @RequestParam(value = "lang", defaultValue = "en") String lang, ServletRequest request,
			ServletResponse response) {

		Response toreturn = new Response();

		logger.debug("savepassword invoked for  {}, token {}, pwd {}, lang {}", id, token, password, lang);

		Locale locale = new Locale(lang);

		PasswordResetToken passToken = userService.getPasswordResetToken(token);
		if (passToken == null) {
			toreturn.setResult(false);
			toreturn.setMessage(messages.getMessage("resetpwd.ko.invalidToken", null, locale));
			return toreturn;
		}

		User user = passToken.getUser();
		if (user == null || user.getId().longValue() != id.longValue()) {
			toreturn.setResult(false);
			toreturn.setMessage(messages.getMessage("resetpwd.ko.invalidToken", null, locale));
			return toreturn;
		}

		Calendar cal = Calendar.getInstance();
		if ((passToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
			toreturn.setResult(false);
			toreturn.setMessage(messages.getMessage("resetpwd.ko.expiredToken", null, locale));
			return toreturn;
		}

		securityService.autologin(user.getUsername(), user.getPassword());

		if ((password.length() == 0)) {
			toreturn.setResult(false);
			toreturn.setMessage(messages.getMessage("validator.ko.password.notnull", null, locale));
			return toreturn;
		}

		userService.changePasswordForLogged(password);

		if (isRememberMeRequested(request, AbstractRememberMeServices.DEFAULT_PARAMETER)) {
			securityService.rememberMe((HttpServletRequest) request, (HttpServletResponse) response);
		}

		toreturn.setMessage(messages.getMessage("resetpwd.ok.passwordchanged", null, locale));

		logger.debug("response {}", toreturn);

		return toreturn;
	}

	private boolean isRememberMeRequested(ServletRequest request, String parameter) {

		String paramValue = request.getParameter(parameter);

		if (paramValue != null) {
			if (paramValue.equalsIgnoreCase("true") || paramValue.equalsIgnoreCase("on")
					|| paramValue.equalsIgnoreCase("yes") || paramValue.equals("1")) {
				return true;
			}
		}

		return false;
	}
}
