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
package edu.unifi.disit.wallet_user_mngt.scheduled;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.unifi.disit.commons.datamodel.AggregatedMobility;
import edu.unifi.disit.commons.datamodel.SOType;
import edu.unifi.disit.commons.utils.NetClientGet;
import edu.unifi.disit.commons.utils.Utils;
import edu.unifi.disit.wallet_user_mngt.datamodel.Device;
import edu.unifi.disit.wallet_user_mngt.datamodel.User;
import edu.unifi.disit.wallet_user_mngt.datamodel.UserDAO;
import edu.unifi.disit.wallet_user_mngt.datamodel.feedback.EmailFeedbackCampaign;
import edu.unifi.disit.wallet_user_mngt.datamodel.feedback.EmailFeedbackCampaignToken;
import edu.unifi.disit.wallet_user_mngt.event.EmailScenarioType;
import edu.unifi.disit.wallet_user_mngt.event.OnPreparationEmailCompleteEvent;
import edu.unifi.disit.wallet_user_mngt.service.IEmailFeedbackCampaignService;

@EnableScheduling
@Component
public class ActivityEmailFeedbackCampaignTask extends BasicFeedbackCampaignTask {

	private static final Logger logger = LoggerFactory.getLogger(ActivityEmailFeedbackCampaignTask.class);

	@Value("${application.url}")
	private String myappuri;

	@Value("${userprofiler.url}")
	private String upuri;

	@Value("${userprofiler.timeout}")
	private Integer timeout;

	@Autowired
	ApplicationEventPublisher eventPublisher;

	@Autowired
	UserDAO userDAO;

	@Autowired
	public IEmailFeedbackCampaignService emailFeedbackCampaignService;

	NetClientGet ncg = new NetClientGet();

	// @Autowired
	// EmailFeedbackCampaignTokenDAO efctrepo;

	@Override // this is default information... insert in db if not yet available
	public EmailFeedbackCampaign getDefaultFeedbackCampaign() {
		EmailFeedbackCampaign myFC = new EmailFeedbackCampaign();
		myFC.setId(1l);// BEWARE, use an unique identifier, progressing from 1 (here)
		myFC.setName("activity feedback campaign");
		myFC.setRate("0 0 3,15 * * *");// every 12 hours, at 0 and 12
		return myFC;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void doFedbackCampaign() {

		logger.debug("do campaign {}", currentFC);

		// retrieve the user NOT cancelled to this emailFeedbackCampaignID
		// TODO manage cancellation from any kind of notification
		Set<String> users = emailFeedbackCampaignService.retrieveUsers(currentFC.getId());

		for (String username : users) {

			logger.info("Sending email to {}", username);

			User user = userDAO.findByUsername(username);

			// create new feedback-token
			String feedbackToken = UUID.randomUUID().toString();

			EmailFeedbackCampaignToken efct = new EmailFeedbackCampaignToken();

			efct.setSentTime(new Date());
			efct.setUser(user);
			efct.setToken(feedbackToken);
			efct.setEmailfeedbackcampaign(currentFC);

			Set<Device> devices = user.getDevices();

			if (devices.size() == 0) {
				logger.warn("be ware the user {} has not attached devices", username);
				continue;
			} else {
				if (devices.size() > 1)
					logger.warn("be ware the user {} has more than one attached devices. anyway we consider just the first one", username);

			}

			try {

				edu.unifi.disit.commons.datamodel.userprofiler.Device d = searchLastDevice(user.getDevices());

				if (d == null) {
					logger.warn("cannot find a valid terminal attached to  {} ", username);
					continue;
				}

				logger.debug("DEVICE is {}", d);

				Long now = System.currentTimeMillis();

				String request = upuri + "/api/v1/device/" + d.getDeviceId() + "/aggregatedmobility?from=" + (now - 86400000) + "&to=" + now;

				String response = ncg.get(new URL(request), timeout);

				List<AggregatedMobility> ams = null;

				if (response.length() != 0) {
					ObjectMapper mapper = new ObjectMapper();
					ams = ((List<AggregatedMobility>) mapper.readValue(response, new TypeReference<List<AggregatedMobility>>() {
					}));
				}

				for (AggregatedMobility am : ams) {
					logger.debug("am is {}", am);
				}

				// ---send the email
				if (user.getUsername().equals("angelo.difino@unifi.it")) {
					eventPublisher.publishEvent(prepareEventForRegistration(user, feedbackToken, d.getTerminal_language(), d.getTerminalAppID(), ams));
					currentFC.getEmailfeedbackCampaignTokens().add(efct);// add this when is sent
				} else
					logger.debug("This is a dummy email to {}", user.getUsername());

			} catch (MalformedURLException e) {
				logger.error("ai ai {}", e);
			} catch (JsonParseException e) {
				logger.error("ai ai {}", e);
			} catch (JsonMappingException e) {
				logger.error("ai ai {}", e);
			} catch (IOException e) {
				logger.error("ai ai {}", e);
			}
		}

		emailFCDAO.save(currentFC);// update the sent token
	}

	private edu.unifi.disit.commons.datamodel.userprofiler.Device searchLastDevice(Set<Device> devices) throws NoSuchElementException, JsonParseException, JsonMappingException, IOException {
		Iterator<Device> id = devices.iterator();

		Date last_updated = new Date(0);
		edu.unifi.disit.commons.datamodel.userprofiler.Device mydevice = null;
		while (id.hasNext()) {
			Device current = id.next();
			logger.debug("check  {}", current);

			// ---retrieve terminal type of user

			String request = upuri + "/api/v1/device/" + current.getDeviceId();

			String response = ncg.get(new URL(request), timeout);

			edu.unifi.disit.commons.datamodel.userprofiler.Device d = null;

			if (response.length() != 0) {
				ObjectMapper mapper = new ObjectMapper();
				d = ((edu.unifi.disit.commons.datamodel.userprofiler.Device) mapper.readValue(response, new TypeReference<edu.unifi.disit.commons.datamodel.userprofiler.Device>() {
				}));
			}

			if (d != null) {
				// if this is the first cycled device, add it anyway (even if lastUpdate==null)
				if ((mydevice == null)) {
					logger.debug("first terminal {}", d.getDeviceId());

					if (d.getLastUpdate() != null)
						last_updated = d.getLastUpdate();

					mydevice = d;

				} else if ((d.getLastUpdate() != null) && (d.getLastUpdate().after(last_updated))) {
					logger.debug("new terminal {}", d.getDeviceId());

					last_updated = d.getLastUpdate();

					mydevice = d;
				}
			}

		}

		return mydevice;

	}

	private OnPreparationEmailCompleteEvent prepareEventForRegistration(User user, String token, Locale locale, String appid, List<AggregatedMobility> ams) {

		// TODO convert terminalType

		String recipientAddress = user.getUsername();
		String subject = "Feedback";
		// TODO use web template
		// String testo = "to not receive anymore this kind of messages press here " + myappuri + "/feedback/cancel?token=" + token
		// + "\r\n"
		// + "your so is:" + terminalType
		// + "\r\n"
		// + "your lang is:" + locale.getDisplayLanguage()
		// + "\r\n";
		// + "your agg status is:" + aggreStatus;

		SOType so = Utils.extractSOType(appid);

		logger.debug("the so to customize the email is {}", so);

		List<String> testo = new ArrayList<String>();
		testo.add(myappuri + "/feedback/redirect?token=" + token);
		testo.add(myappuri + "/feedback/cancel?token=" + token);

		return new OnPreparationEmailCompleteEvent(recipientAddress, subject, testo, locale, EmailScenarioType.FEEDBACK_OPEN);
	}
}
