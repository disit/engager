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

import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import edu.unifi.disit.wallet_user_mngt.exception.UserNotFoundException;
import edu.unifi.disit.wallet_user_mngt.object.StringForm;
import edu.unifi.disit.wallet_user_mngt.service.IEmailFeedbackCampaignService;

@Controller
public class EmailFeedbackCampaignController {

	private static final Logger logger = LogManager.getLogger();

	@Autowired
	private MessageSource messages;

	@Autowired
	private IEmailFeedbackCampaignService emailFCS;

	@Value("${mobile.application.url}")
	private String mobileappuri;

	// called from email
	@RequestMapping(value = "/feedback/cancel", method = RequestMethod.GET)
	public ModelAndView emailFeedbackCampaign(@RequestParam("token") String token, Locale locale) {

		logger.debug("/ email feedback invoked");

		ModelAndView mav = new ModelAndView("feedback");

		StringForm stringForm = new StringForm(token);

		mav.addObject("stringForm", stringForm);

		return mav;
	}

	// called from email
	@RequestMapping(value = "/feedback/cancel", method = RequestMethod.POST)
	public ModelAndView cancelEmailFeedbackCampaign(@ModelAttribute StringForm stringForm, Locale locale) {

		logger.debug("/cancel confirmed email feedback invoked {}", stringForm);

		ModelAndView mav = new ModelAndView("feedback");

		if ((stringForm != null) && (stringForm.getString() != null)) {
			emailFCS.removeUserFrom(stringForm.getString());
			logger.debug("/cancel ");
			mav.addObject("error", messages.getMessage("feedbackemail.cancel.ok", null, locale));

		} else {
			logger.debug("/no ");
			mav.addObject("error", messages.getMessage("feedbackemail.cancel.ko", null, locale));
		}

		mav.addObject("stringForm", null);

		return mav;
	}

	@RequestMapping(value = "/feedback/redirect", method = RequestMethod.GET)
	public RedirectView redirectresetpassword(@RequestParam("token") String token) throws UserNotFoundException {

		RedirectView rv = new RedirectView(mobileappuri + "://feedbackopen?token=" + token);
		rv.setHttp10Compatible(false);

		return rv;
	}

}
