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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import edu.unifi.disit.commons.datamodel.DatasetType;
import edu.unifi.disit.commons.datamodel.userprofiler.Log;
import edu.unifi.disit.commons.utils.CSVUtils;
import edu.unifi.disit.wallet_user_mngt.datamodel.Consent;
import edu.unifi.disit.wallet_user_mngt.exception.OperationNotPermittedException;
import edu.unifi.disit.wallet_user_mngt.object.Pager;
import edu.unifi.disit.wallet_user_mngt.object.PrivacyForm;
import edu.unifi.disit.wallet_user_mngt.object.SettingsForm;
import edu.unifi.disit.wallet_user_mngt.service.IDeviceService;
import edu.unifi.disit.wallet_user_mngt.service.ISecurityService;
import edu.unifi.disit.wallet_user_mngt.service.IUserConsentService;

@Controller
public class PrivacyController {

	@Value("${application.url}")
	private String webappuri;

	@Value("${download.folder}")
	private String downloadFolder;

	private static final Logger logger = LogManager.getLogger();

	@Autowired
	IUserConsentService userConsentService;

	@Autowired
	ISecurityService securityService;

	@Autowired
	IDeviceService deviceService;

	private static final int INITIAL_PAGE_SIZE_1 = 5;
	private static final int BUTTONS_TO_SHOW = 5;

	@RequestMapping(value = "/privacy", method = RequestMethod.GET)
	public ModelAndView privacy() {
		logger.debug("/privacy GET invoked");

		return createPrivacyForm();
	}

	@RequestMapping(value = "/privacy", method = RequestMethod.POST, params = { "save" })
	public ModelAndView saveConsents(@ModelAttribute PrivacyForm privacyForm, BindingResult bindingResultPrivacyForm) {

		String error = null;
		String msg = null;

		logger.debug("/privacy POST consent invoked");

		for (Consent c : privacyForm.getConsents()) {

			c.setUser(securityService.findLoggedInUser());

			userConsentService.updateConsentForLoggedUser(c, null);

		}

		msg = "saved";

		return createPrivacyForm(error, msg);
	}

	// TODO UNIFORM!!!!!!!!!!!!!!!! with above

	@RequestMapping(value = "/privacy", method = RequestMethod.POST, params = { "download" })
	public ModelAndView donwloadConsents(@ModelAttribute PrivacyForm privacyForm, BindingResult bindingResultPrivacyForm) throws OperationNotPermittedException {

		Long userId = securityService.findLoggedInUser().getId();

		logger.debug("/privacy POST download invoked");

		String error = null;
		String urlForDownload = null;

		if (privacyForm.getChoosen() == null)
			error = "not choosen";
		else {

			DatasetType dataset = privacyForm.getConsents().get(privacyForm.getChoosen()).getDataset();
			try {
				userConsentService.checkConsentForLoggedUser(dataset, "en");// if not consent it throw an exception

				List<String> devices = userConsentService.getDeviceForLoggedUser(dataset);

				List<Log> logs = new ArrayList<Log>();
				for (String device : devices) {
					logger.debug("getting log for  {}", device);
					logs.addAll(deviceService.getLogs(device, dataset.toString(), "en"));
				}

				// create a csv file
				String filename = "download-" + userId + "-" + System.currentTimeMillis();

				logger.debug("filename is {}", filename);

				writeInCSV(downloadFolder + filename + ".csv", logs);

				// zip the file
				zip(downloadFolder + filename);

				// prepare the zip for download

				urlForDownload = webappuri + "/download?filename=" + filename + ".zip";

			} catch (OperationNotPermittedException o) {
				error = o.getMessage();
			}
		}

		return createPrivacyForm(error, null, null, urlForDownload);
	}

	private void writeInCSV(String filename, List<Log> logs) {
		try {

			logger.debug("creating writer for {}", filename);

			FileWriter writer = new FileWriter(filename);

			logger.debug("created");

			CSVUtils.writeLine(writer, Arrays.asList("id", "dataset_type", "insert_time", "log_time", "value_type", "value"));

			logger.debug("wrote testa");

			for (Log log : logs) {
				CSVUtils.writeLine(writer, Arrays.asList(log.getId().toString(), log.getDataset().toString(), log.getInsertTime().toString(), log.getDataTime().toString(), log.getValueType().toString(), log.getValue().toString()));
			}

			logger.debug("wrote coda");

			writer.flush();
			writer.close();

			logger.debug("CSV created {}", filename);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void zip(String filename) {
		try {
			String sourceFile = filename + ".csv";
			FileOutputStream fos = new FileOutputStream(filename + ".zip");
			ZipOutputStream zipOut = new ZipOutputStream(fos);
			File fileToZip = new File(sourceFile);
			FileInputStream fis = new FileInputStream(fileToZip);
			ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
			zipOut.putNextEntry(zipEntry);
			final byte[] bytes = new byte[1024];
			int length;
			while ((length = fis.read(bytes)) >= 0) {
				zipOut.write(bytes, 0, length);
			}
			zipOut.close();
			fis.close();
			fos.close();

			logger.debug("zipped {}", filename);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private ModelAndView createPrivacyForm() {
		return createPrivacyForm(null, null, null, null);
	}

	private ModelAndView createPrivacyForm(String error, String msg) {
		return createPrivacyForm(error, msg, null, null);
	}

	private ModelAndView createPrivacyForm(String error, String msg, String msgStatus) {
		return createPrivacyForm(error, msg, msgStatus, null);
	}

	private ModelAndView createPrivacyForm(String error, String msg, String msg2, String url) {

		ModelAndView modelAndView = new ModelAndView("privacy");

		PrivacyForm pf = new PrivacyForm();

		pf.setConsents(userConsentService.getConsentsForLoggedUser(null, null));

		modelAndView.addObject("pf", pf);

		if (error != null) {
			modelAndView.addObject("error", error);
		}
		if (msg != null) {
			modelAndView.addObject("message", msg);
		}
		if (msg2 != null) {
			modelAndView.addObject("message2", msg2);
		}
		if (url != null) {
			modelAndView.addObject("url", url);
		}

		return modelAndView;
	}

	@RequestMapping(value = "/download", method = RequestMethod.GET, produces = "application/zip")
	public ResponseEntity<ByteArrayResource> download(@RequestParam String filename) throws IOException {

		File file = new File(downloadFolder + filename);
		Path path = Paths.get(downloadFolder + filename);
		ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));

		HttpHeaders headers = new HttpHeaders();
		headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
		headers.add("Pragma", "no-cache");
		headers.add("Expires", "0");
		headers.add("Content-Disposition", "attachment; filename=\"" + filename + "\"");

		return ResponseEntity.ok()
				.headers(headers)
				.contentLength(file.length())
				.contentType(MediaType.parseMediaType("application/zip"))
				.body(resource);
	}

	@RequestMapping(value = "/privacy", method = RequestMethod.POST, params = { "browse" })
	public ModelAndView browseConsents(@ModelAttribute PrivacyForm privacyForm, BindingResult bindingResultPrivacyForm) throws OperationNotPermittedException {

		logger.debug("/privacy POST browse invoked");

		String error = null;

		if (privacyForm.getChoosen() == null)
			error = "not choosen";
		else {

			DatasetType dataset = privacyForm.getConsents().get(privacyForm.getChoosen()).getDataset();

			try {
				userConsentService.checkConsentForLoggedUser(dataset, "en");// if not consent it throw an exception

				List<String> devices = userConsentService.getDeviceForLoggedUser(dataset);

				if (devices.size() != 0) {
					return new ModelAndView("redirect:/browse/" + devices.get(0) + "/" + dataset.toString() + "/1");
				} else
					error = "no connected devices";

			} catch (OperationNotPermittedException o) {
				error = o.getMessage();
			}
		}

		return createPrivacyForm(error, null);
	}

	@RequestMapping(value = "/browse/{deviceId}/{dataset}/{pageNumber}", method = RequestMethod.GET)
	public ModelAndView browse(@PathVariable String deviceId, @PathVariable String dataset, @PathVariable Integer pageNumber) {

		// List<String> devices = userConsentService.getDeviceForLoggedUser(dataset);

		ModelAndView mv = new ModelAndView("browse");

		mv.addObject("devices", userConsentService.getDeviceForLoggedUserDetailed(DatasetType.findBy(dataset)));

		Integer pages = new Double(Math.ceil(new Double(deviceService.countLogs(deviceId)) / INITIAL_PAGE_SIZE_1)).intValue();

		Pager pager_1 = new Pager(pages, pageNumber, BUTTONS_TO_SHOW);
		mv.addObject("pager", pager_1);
		mv.addObject("current", pageNumber);
		mv.addObject("logs", deviceService.getLogs(deviceId, dataset, "en", (pageNumber - 1), INITIAL_PAGE_SIZE_1));

		// mv.addObject("deviceId", deviceId);
		// mv.addObject("dataset", dataset.toString());
		mv.addObject("datasetLabel", DatasetType.findBy(dataset).getLabel());

		SettingsForm sf = new SettingsForm();
		sf.setDataset(dataset);
		sf.setActivedeviceid(deviceId.toString());
		mv.addObject("settingsForm", sf);

		mv.addObject("datasetLabel", DatasetType.findBy(dataset).getLabel());

		return mv;
	}

	@RequestMapping(value = "/browse", method = RequestMethod.POST, params = { "refresh" })
	public ModelAndView refreshdevice(@ModelAttribute SettingsForm settingsForm) throws ParseException, IOException {

		return new ModelAndView("redirect:/browse/" + settingsForm.getActivedeviceid() + "/" + settingsForm.getDataset() + "/1");
	}

	@RequestMapping(value = "/privacy", method = RequestMethod.POST, params = { "delete" })
	public ModelAndView deleteConsents(@ModelAttribute PrivacyForm privacyForm, BindingResult bindingResultPrivacyForm) throws OperationNotPermittedException {

		logger.debug("/privacy POST delete invoked");

		String error = null;
		String msg2 = null;

		if (privacyForm.getChoosen() == null)
			error = "not choosen";
		else {

			DatasetType dataset = privacyForm.getConsents().get(privacyForm.getChoosen()).getDataset();
			try {
				userConsentService.checkConsentForLoggedUser(dataset, "en");// if not consent it throw an exception

				List<String> devices = userConsentService.getDeviceForLoggedUser(dataset);

				for (String device : devices) {
					logger.debug("delete log for  {}", device);
					deviceService.deleteLogs(device, dataset.toString(), "en");
				}

				msg2 = "deleted data from dataset \"" + dataset.getLabel() + "\"";

			} catch (OperationNotPermittedException o) {
				error = o.getMessage();
			}
		}

		return createPrivacyForm(error, null, msg2);
	}

	@RequestMapping(value = "/privacy", method = RequestMethod.POST, params = { "forget" })
	public ModelAndView forgetConsents(@ModelAttribute PrivacyForm privacyForm, BindingResult bindingResultPrivacyForm) throws OperationNotPermittedException {

		logger.debug("/privacy POST forget invoked");

		String error = null;
		String msg2 = null;

		DatasetType dataset = null;

		if (privacyForm.getChoosen() == null)
			error = "not choosen";
		else {

			dataset = privacyForm.getConsents().get(privacyForm.getChoosen()).getDataset();
			try {

				userConsentService.checkConsentForLoggedUser(dataset, "en");// if not consent it throw an exception

				// -----------------------------------------delete data from this dataset

				List<String> devices = userConsentService.getDeviceForLoggedUser(dataset);

				for (String device : devices) {
					logger.debug("delete log for  {}", device);
					deviceService.deleteLogs(device, dataset.toString(), "en");
				}

				// ------------------------------------remove consents from this dataset

				for (Consent c : privacyForm.getConsents()) {
					if (c.getDataset().equals(dataset)) {
						c.setUser(securityService.findLoggedInUser());
						c.setConsent(false);
						userConsentService.updateConsentForLoggedUser(c, null);
					}

				}

				msg2 = "forgot from dataset \"" + dataset.getLabel() + "\"";

			} catch (OperationNotPermittedException o) {
				error = o.getMessage();
			}
		}

		return createPrivacyForm(error, null, msg2);

	}

	@RequestMapping(value = "/privacy", method = RequestMethod.POST, params = { "remove" })
	public ModelAndView forgetAllConsents(@ModelAttribute PrivacyForm privacyForm, BindingResult bindingResultPrivacyForm) throws OperationNotPermittedException {

		logger.debug("/privacy POST forget invoked");

		String error = null;
		String msg2 = null;

		try {
			// -----------------------------------------delete data from ANY dataset
			// for any dataset check if it is possibile
			for (DatasetType dataset : DatasetType.values()) {

				userConsentService.checkConsentForLoggedUser(dataset, "en");// if not consent it throw an exception
			}
			// delete
			for (DatasetType dataset : DatasetType.values()) {
				List<String> devices = userConsentService.getDeviceForLoggedUser(dataset);

				for (String device : devices) {
					logger.debug("delete log for  {}", device);
					deviceService.deleteLogs(device, "en");
				}
			}

			// ------------------------------------remove consents from ANY dataset

			for (Consent c : privacyForm.getConsents()) {

				c.setUser(securityService.findLoggedInUser());
				c.setConsent(false);
				userConsentService.updateConsentForLoggedUser(c, null);

			}

			msg2 = "forgot ALL";

		} catch (OperationNotPermittedException o) {
			error = o.getMessage();
		}

		return createPrivacyForm(error, null, msg2);

	}
}
