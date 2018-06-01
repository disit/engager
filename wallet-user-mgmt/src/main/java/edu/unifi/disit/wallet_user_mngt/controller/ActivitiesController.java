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
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.unifi.disit.commons.datamodel.AggregatedMobility;
import edu.unifi.disit.commons.datamodel.userprofiler.Timeline;
import edu.unifi.disit.commons.utils.NetClientGet;
import edu.unifi.disit.wallet_user_mngt.datamodel.Device;
import edu.unifi.disit.wallet_user_mngt.object.SettingsForm;
import edu.unifi.disit.wallet_user_mngt.service.IDeviceService;
import edu.unifi.disit.wallet_user_mngt.service.ISettingsService;
import edu.unifi.disit.wallet_user_mngt.service.IUserService;

@Controller
public class ActivitiesController {

	@Value("${application.url}")
	private String webappuri;

	@Value("${userprofiler.url}")
	private String upuri;

	@Value("${userprofiler.timeout}")
	private Integer timeout;

	NetClientGet ncg = new NetClientGet();

	private static final Logger logger = LogManager.getLogger();

	@Autowired
	private ISettingsService settingsService;

	@Autowired
	private IUserService userService;

	@Autowired
	private IDeviceService deviceService;

	@RequestMapping(value = "/activities", method = RequestMethod.GET)
	public ModelAndView activities(String message, String error, @ModelAttribute SettingsForm settingsForm) throws ParseException, IOException {
		logger.debug("/activities GET invoked");
		return createSettingModel(message, error, settingsForm);
	}

	@RequestMapping(value = "/activities", method = RequestMethod.POST, params = { "refresh" })
	public ModelAndView refreshdevice(String message, String error, @ModelAttribute SettingsForm settingsForm) throws ParseException, IOException {

		return createSettingModel(message, error, settingsForm);
	}

	@RequestMapping(value = "/activities", method = RequestMethod.POST, params = { "submit" })
	public ModelAndView submitdevice(String message, String error, @ModelAttribute SettingsForm settingsForm) throws ParseException, IOException {

		List<Timeline> tls = getTimeline(settingsForm.getActivedeviceid(), settingsForm.getTimelinedate());// TODO avoid this recall!!!!

		for (int i = 0; i < tls.size(); i++) {
			Timeline tl = tls.get(i);
			updatetimeline(settingsForm.getActivedeviceid(), tl.getDate().getTime(), tl.getDate().getTime() + tl.getSeconds() * 1000, settingsForm.getMobilities().get(i));
		}

		return createSettingModel(message, error, settingsForm);
	}

	private ModelAndView createSettingModel(String message, String error, SettingsForm settingsForm) throws ParseException, IOException {
		ModelAndView mav = new ModelAndView("activities");

		// ------------------------devices + settings form
		Set<Device> devices = userService.getConnectedDevicesForLoggedUser();

		Map<String, String> inputsettings = settingsService.loadSettings();
		mav.addObject("inputsettings", inputsettings);

		if (settingsForm.getActivedeviceid() == null) {
			settingsForm = new SettingsForm(); // output
			Map<String, String> outputsettings = new HashMap<String, String>();
			outputsettings.putAll(inputsettings);
			settingsForm.setProperties(outputsettings);

			if ((devices != null) && (devices.size() > 0))
				settingsForm.setActivedeviceid(getLastIterator(devices).getDeviceId());// active device

			// timelinedate
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Calendar cal = Calendar.getInstance();
			String timelinedate = dateFormat.format(cal.getTime());
			settingsForm.setTimelinedate(timelinedate);

		}

		// ppois etc-----------------------
		if ((devices != null) && (devices.size() > 0)) {
			mav.addObject("connecteddevices", deviceService.getDeviceModelNameForLoggedUser());

			mav.addObject("deviceid", settingsForm.getActivedeviceid());

			mav.addObject("aggmobility", getAggMobility(settingsForm.getActivedeviceid(), settingsForm.getTimelinedate()));

			List<Timeline> tl = getTimeline(settingsForm.getActivedeviceid(), settingsForm.getTimelinedate());
			mav.addObject("timeline", tl);
			if ((tl != null) && (tl.size() > 0))
				mav.addObject("gpss", getPositions(settingsForm.getActivedeviceid(), tl.get(0).getDate(), settingsForm.getTimelinedate()));

			edu.unifi.disit.commons.datamodel.userprofiler.Device d = deviceService.getDevice(settingsForm.getActivedeviceid());

			if (d != null) {

				mav.addObject("useractivities", d.getUserActivities());
				mav.addObject("isassessor", d.getIsAssessor());

				if (settingsForm.getMobilities().size() == 0) {
					List<String> mob = new ArrayList<String>();
					for (int i = 0; i < tl.size(); i++) {
						mob.add("Stay");
					}
					settingsForm.setMobilities(mob);
				}
			}

			mav.addObject("settingsForm", settingsForm);

			// # mttm$modalityClass9[mttm$modality=="1"]="Stay"
			// # mttm$modalityClass9[mttm$modality=="2"]="Walk"
			// # mttm$modalityClass9[mttm$modality=="4"]="Bike"
			// # mttm$modalityClass9[mttm$modality=="5"]="Motorbike"
			// # mttm$modalityClass9[mttm$modality=="6"]="Bus"
			// # mttm$modalityClass9[mttm$modality=="7"]="Tram"
			// # mttm$modalityClass9[mttm$modality=="8"]="Car"
			// # mttm$modalityClass9[mttm$modality=="9"]="Train"

			List<String> allMob = new ArrayList<String>();
			allMob.add("Stay");
			allMob.add("Walk");
			allMob.add("Run");
			allMob.add("Bike");
			allMob.add("Motorbike");
			allMob.add("Bus");
			allMob.add("Tram");
			allMob.add("Car");
			allMob.add("Train");
			allMob.add("unknown");

			mav.addObject("allmob", allMob);
		}

		// -------------------------error
		if (message != null)
			mav.addObject("message", message);

		if (error != null)
			mav.addObject("error", error);

		mav.addObject("webappuri", webappuri);

		return mav;
	}

	private void updatetimeline(String deviceId, Long from, Long to, String mobility) {

		try {

			String params = "from=" + from + "&to=" + to + "&mobility_status=" + mobility;

			ncg.post(new URL(upuri + "/api/v1/device/" + deviceId + "/timeline"), timeout, Level.ERROR, params);

		} catch (Exception e) {
			logger.error("ai ai {}", e);
			e.printStackTrace();
		}

	}

	// private List<DeviceModelForm> extractModelName(Set<Device> devices) {
	// Iterator<Device> i = devices.iterator();
	//
	// List<DeviceModelForm> l = new ArrayList<DeviceModelForm>();
	//
	// while (i.hasNext()) {
	// Device d = i.next();
	//
	// Hashtable<String, String> devicespecs = getModel(d.getDeviceId());
	// String label = null;
	//
	// if (devicespecs != null) {
	// label = devicespecs.get("label");
	// if (label == null)
	// label = devicespecs.get("id");
	// }
	// if (label == null)
	// label = d.getDeviceId();
	//
	// l.add(new DeviceModelForm(d.getDeviceId(), label));
	//
	// }
	// return l;
	// }

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

	private String getPositions(String deviceId, Date from, String date) throws ParseException {

		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Date dated = format.parse(date);

		String toreturn = null;

		try {

			toreturn = ncg.get(new URL(upuri + "/api/v1/device/" + deviceId + "/positions?from=" + from.getTime() + "&to=" + (dated.getTime() + 86400000)), timeout);

		} catch (MalformedURLException e) {
			logger.error("ai ai {}", e);
		}

		return toreturn;
	}

	private List<Timeline> getTimeline(String deviceId, String date) throws ParseException, JsonParseException, JsonMappingException, IOException {
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Date dated = format.parse(date);

		ObjectMapper mapper = new ObjectMapper();

		List<Timeline> toreturn = null;

		try {

			String response = ncg.get(new URL(upuri + "/api/v1/device/" + deviceId + "/timeline?from=" + dated.getTime() + "&to=" + (dated.getTime() + 86399000)), timeout);
			toreturn = mapper.readValue(response, new TypeReference<List<Timeline>>() {
			});

		} catch (MalformedURLException e) {
			logger.error("ai ai {}", e);
		}
		return toreturn;
	}

	private List<AggregatedMobility> getAggMobility(String deviceId, String date) throws ParseException, JsonParseException, JsonMappingException, IOException {
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Date dated = format.parse(date);

		ObjectMapper mapper = new ObjectMapper();

		List<AggregatedMobility> toreturn = null;

		try {

			String response = ncg.get(new URL(upuri + "/api/v1/device/" + deviceId + "/aggregatedmobility?from=" + dated.getTime() + "&to=" + (dated.getTime() + 86400000)), timeout);
			toreturn = mapper.readValue(response, new TypeReference<List<AggregatedMobility>>() {
			});

		} catch (MalformedURLException e) {
			logger.error("ai ai {}", e);
		}

		return toreturn;
	}

	private Device getLastIterator(Set<Device> devices) {
		Iterator<Device> i = devices.iterator();
		Device toreturn = null;

		while (i.hasNext()) {
			toreturn = i.next();
		}
		return toreturn;
	}

}
