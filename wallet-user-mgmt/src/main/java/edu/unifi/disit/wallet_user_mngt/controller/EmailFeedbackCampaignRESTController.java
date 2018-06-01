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
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.unifi.disit.wallet_user_mngt.exception.FeedbackTokenNotExistException;
import edu.unifi.disit.wallet_user_mngt.object.Response;
import edu.unifi.disit.wallet_user_mngt.service.IEmailFeedbackCampaignService;

@RestController
public class EmailFeedbackCampaignRESTController {

	private static final Logger logger = LogManager.getLogger();

	@Autowired
	private IEmailFeedbackCampaignService emailFCS;

	@Autowired
	private MessageSource messages;

	@RequestMapping(value = "/api/v1/feedback", method = RequestMethod.POST)
	public Response feedbackV1(@RequestParam("token") String token, @RequestParam(value = "lang", defaultValue = "en") String lang) {

		Response toreturn = new Response();

		logger.debug("requested feedback for token {}, lang {}", token, lang);

		try {
			emailFCS.feedbackUserFrom(token);
		} catch (FeedbackTokenNotExistException e) {
			logger.warn("token {} not found", token);

			Locale locale = new Locale(lang);

			toreturn.setResult(Boolean.FALSE);
			toreturn.setMessage(messages.getMessage("feedbackemail.notfound.ko", null, locale));

		}

		logger.debug("response {}", toreturn);

		return toreturn;
	}
}
