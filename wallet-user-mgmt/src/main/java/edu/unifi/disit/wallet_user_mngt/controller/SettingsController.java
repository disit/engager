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

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import edu.unifi.disit.commons.utils.NetClientGet;
import edu.unifi.disit.wallet_user_mngt.object.SettingsForm;
import edu.unifi.disit.wallet_user_mngt.service.IEmailFeedbackCampaignService;
import edu.unifi.disit.wallet_user_mngt.service.ISettingsService;
import edu.unifi.disit.wallet_user_mngt.service.IUserService;

@Controller
public class SettingsController {

	@Value("${userprofiler.url}")
	private String upuri;

	@Value("${userprofiler.timeout}")
	private Integer timeout;

	NetClientGet ncg = new NetClientGet();

	private static final Logger logger = LogManager.getLogger();

	@Autowired
	private MessageSource messages;

	@Autowired
	private ISettingsService settingsService;

	// @Autowired
	// private ISocialService socialService;

	@Autowired
	private IUserService userService;

	@Autowired
	private IEmailFeedbackCampaignService emailFeedbackCampaignService;

	@RequestMapping(value = "/settings", method = RequestMethod.GET)
	public ModelAndView settings(String message, String error, @ModelAttribute SettingsForm settingsForm) throws ParseException, IOException {
		logger.debug("/settings GET invoked");
		return createSettingModel(message, error, settingsForm);
	}

	@RequestMapping(value = "/settings", method = RequestMethod.POST, params = { "refresh" })
	public ModelAndView refreshdevice(String message, String error, @ModelAttribute SettingsForm settingsForm) throws ParseException, IOException {

		return createSettingModel(message, error, settingsForm);
	}

	@RequestMapping(value = "/settings", method = RequestMethod.POST)
	public ModelAndView savesettings(@ModelAttribute SettingsForm settingsForm, @RequestParam(required = false) String removeid) {

		logger.debug("/settings POST invoked");

		ModelAndView mav = new ModelAndView("redirect:/settings");

		try {
			if (removeid == null) {

				settingsService.saveSettings(settingsForm.getProperties());
				mav.addObject("message", messages.getMessage("saved", null, LocaleContextHolder.getLocale()));

			} else {
				logger.debug("removing {}", removeid);

				emailFeedbackCampaignService.removeCancellation(removeid);
				mav.addObject("message", messages.getMessage("removed", null, LocaleContextHolder.getLocale()));

			}
		} catch (Exception e) {
			logger.error("Error catched:", e);
			mav.addObject("error", e.getMessage());
		}

		return mav;
	}

	private ModelAndView createSettingModel(String message, String error, SettingsForm settingsForm) throws ParseException, IOException {
		ModelAndView mav = new ModelAndView("settings");

		// ------------------------devices + settings form
		// Set<Device> devices = userService.getConnectedDevices();

		Map<String, String> inputsettings = settingsService.loadSettings();
		mav.addObject("inputsettings", inputsettings);

		if (settingsForm.getActivedeviceid() == null) {
			settingsForm = new SettingsForm(); // output
			Map<String, String> outputsettings = new HashMap<String, String>();
			outputsettings.putAll(inputsettings);
			settingsForm.setProperties(outputsettings);

			// timelinedate
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Calendar cal = Calendar.getInstance();
			String timelinedate = dateFormat.format(cal.getTime());
			settingsForm.setTimelinedate(timelinedate);

		}
		mav.addObject("settingsForm", settingsForm);

		// ------------------------other

		// mav.addObject("connecteddevices", extractModelName(devices));
		mav.addObject("isconfirmed", userService.isConfirmed());

		// -------------------------error
		if (message != null)
			mav.addObject("message", message);

		if (error != null)
			mav.addObject("error", error);

		return mav;
	}

	// private List<DeviceModelForm> extractModelName(Set<Device> devices) {
	// Iterator<Device> i = devices.iterator();
	//
	// List<DeviceModelForm> l = new ArrayList<DeviceModelForm>();
	//
	// while (i.hasNext()) {
	// Device d = i.next();
	//
	// String label = getModel(d.getDeviceId()).get("label");
	// if (label == null)
	// label = getModel(d.getDeviceId()).get("id");
	// if (label == null)
	// label = d.getDeviceId();
	//
	// l.add(new DeviceModelForm(d.getDeviceId(), label));
	//
	// }
	// return l;
	// }
	//
	// private Hashtable<String, String> getModel(String id) {
	// ObjectMapper mapper = new ObjectMapper();
	//
	// Hashtable<String, String> toreturn = null;
	//
	// try {
	//
	// String response = ncg.get(new URL(upuri + "/api/v1/device/" + id + "/terminal/terminalmodel"), timeout);
	// toreturn = mapper.readValue(response, new TypeReference<Hashtable<String, String>>() {
	// });
	//
	// } catch (Exception e) {
	// logger.error("ai ai {}", e);
	// e.printStackTrace();
	// }
	//
	// logger.debug("returning these ppois");
	//
	// return toreturn;
	// }

}
