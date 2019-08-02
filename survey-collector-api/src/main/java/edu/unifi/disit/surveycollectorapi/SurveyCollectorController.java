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
package edu.unifi.disit.surveycollectorapi;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

import javax.annotation.PreDestroy;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.unifi.disit.commons.datamodel.PPOI;
import edu.unifi.disit.commons.datamodel.Result;
import edu.unifi.disit.commons.utils.NetClientGet;
import edu.unifi.disit.commons.utils.Utils;
import edu.unifi.disit.engager_utils.SampleDataSource;

@RestController
public class SurveyCollectorController {

	private static final Logger logger = LogManager.getLogger("SurveyCollectorController");
	private static final GetPropertyValues properties = GetPropertyValues.getInstance();
	private static final DBinterface dbi = DBinterface.getInstance();

	NetClientGet ncg = new NetClientGet();

	SurveyCollectorController() {
		// setting logger level
		Utils.setLoggerLevel(properties.getLogLevel());
	}

	@RequestMapping(value = "/test", method = RequestMethod.GET)
	public ResponseEntity<String> engagerTest() {
		return new ResponseEntity<String>("alive", HttpStatus.OK);
	}

	@CrossOrigin
	@RequestMapping(value = "/survey-collector", method = RequestMethod.POST)
	public Result survey(@RequestBody String json) {//

		try {
			json = URLDecoder.decode(json, java.nio.charset.StandardCharsets.UTF_8.toString());
			logger.debug("Received POST request:" + json);
			SurveyResponse sr = parsa(json);
			dbi.add(sr);

			manage(sr);

			return new Result("OK");
		} catch (Exception e) {
			logger.error("error in survey:", e);
			return new Result("KO", e.getMessage());
		}
	}

	// this routine check the type of the survey and manage it
	// now support:
	// ---------------------------
	// 1- survey about CONFIRM_PPOI, if the name is : confirm_ppoi_home_XX, confirm_ppoi_work_XX, confirm_ppoi_school_XX
	// 1a-if the result is "one" (YES) -> ADD CONFIRMATION for this PPOI
	// 1b-if the result is "two" (NO) -> ADD NOTIFICATION for this PPOI about NOT
	// ---------------------------
	// 2- survey about SPENT_TIME, if the name is : spent_time_XX
	// 2a-if the result is " WhereYouBeen":"Work/Home/School", retriev\e the gps coordinate from db sensor, from engagement_id checking against timeCretated and INSERT CONFIRMATION for this ppoi, if was not alreaqdy confirmed!!!
	// 2b-if result is HaveYouBeenHere\":\"yes, as above ++ INSERT CONFIRMATION fro SPENT_TIME
	// ----------------------------
	// 3- survey about CONFIRM_EXTRAPPOI
	// 3a-if HOUSE/WORK/SCHOOL , confirm HOUSE/WORK/SCHOOL (if was not already confirmed)
	// 3b-if PPOI, confirm the serviceuri
	// 3c-if OTHER, confrim simple the PPOI
	// 3d if REJECTED, reject the PPOI

	private void manage(SurveyResponse sr) throws Exception {
		if ((sr.getSurvey_id() != null) && (sr.getSurvey_id().startsWith("confirm_ppoi_"))) {

			String ppoi_name;

			if (sr.getSurvey_id().contains("home"))
				ppoi_name = SampleDataSource.HOME;
			else if (sr.getSurvey_id().contains("work"))
				ppoi_name = SampleDataSource.WORK;
			else if (sr.getSurvey_id().contains("school"))
				ppoi_name = SampleDataSource.SCHOOL;
			else
				throw new Exception("the name of the rule is not correct. looking for:" + sr.getSurvey_id());

			if (sr.getSurvey_response().contains("\"yes\"")) // PPOI is confirmed
				updateUP(sr.getUser_id(), ppoi_name, true);

			else if (sr.getSurvey_response().contains("\"no\"")) // PPOI is rejected
				updateUP(sr.getUser_id(), ppoi_name, false);
			else
				logger.warn("The survey doesn't not contains yes/not");
		} else if ((sr.getSurvey_id() != null) && (sr.getSurvey_id().startsWith("spent_time_"))) {

			String ppoi_name = null;

			if (sr.getSurvey_response().contains("\"WhereYouBeen\":\"Home\"")) { // Home is confirmed
				ppoi_name = SampleDataSource.HOME;
			} else if (sr.getSurvey_response().contains("\"WhereYouBeen\":\"Work\"")) { // Work is confirmed
				ppoi_name = SampleDataSource.WORK;
			} else if (sr.getSurvey_response().contains("\"WhereYouBeen\":\"School\"")) { // School is confirmed
				ppoi_name = SampleDataSource.SCHOOL;
			} else if (sr.getSurvey_response().contains("\"HaveYouBeenHere\":\"yes\"")) { // SPENT TIME is confirmed
				ppoi_name = SampleDataSource.SPENT_TIME;
				addUserServiceInterest(sr.getUser_id(), dbi.getEngagementServiceUri(sr.getEngagement_id()), getRating(sr.getSurvey_response()), ppoi_name);
			} else
				logger.debug("no information are confirmed. probably other of something else");

			// if is home/work/school and (never confirmed/rejeccted OR never confirmed)
			if ((ppoi_name != null)) {
				// update user profile
				updateUP(sr.getUser_id(), ppoi_name, dbi.getEngagementDate(sr.getEngagement_id()));
			} else {
				logger.debug("was something else or already confirmed");
			}
		} else if ((sr.getSurvey_id() != null) && (sr.getSurvey_id().startsWith("confirm_extrappoi_"))) {

			logger.debug("got confirmation for extra");

			String ppoi_name = null;
			boolean confirmed = true;

			if (sr.getSurvey_response().contains("\"confirm_extrappoi\":\"yes\"")) { // PPOI is confirmed
				ppoi_name = SampleDataSource.PPOI;
			} else if (sr.getSurvey_response().contains("\"confirm_extrappoi\":\"no\"")) { // PPOI is rejected
				ppoi_name = SampleDataSource.PPOI;
				confirmed = false;
			} else
				logger.debug("no information are confirmed. probably other of something else");

			if ((ppoi_name != null)) {
				// update user profile
				updateUP(sr.getUser_id(), ppoi_name, dbi.getEngagementDate(sr.getEngagement_id()), confirmed);
			} else {
				logger.debug("coordinate are null {}", sr);
			}
		} else {
			logger.debug("was something else or already confirmed");
		}
	}

	private Integer getRating(String survey_response) {

		logger.debug("extra rating from :{}:", survey_response);

		try {
			int indexSTR = survey_response.indexOf("HowMuchDidYouLike?") + 21;

			logger.debug("indexSTR is:{}", indexSTR);

			if (indexSTR > 0)
				logger.debug("str is:{}:", survey_response.substring(indexSTR, indexSTR + 1));
			return Integer.parseInt(survey_response.substring(indexSTR, indexSTR + 1));
		} catch (Exception e) {
			logger.error(e);
		}
		return null;
	}

	@RequestMapping("/get-survey")
	public List<SurveyResponse> getSurvey(
			@RequestParam("survey_id") String survey_id) {

		logger.debug("Received a request for getSurvey");

		return dbi.get(survey_id);
	}

	private SurveyResponse parsa(String json) {
		ObjectMapper objectMapper = new ObjectMapper();

		SurveyResponse sr = null;

		try {
			JsonNode rootNode = objectMapper.readTree(json.getBytes());

			JsonNode useridNode = rootNode.path("user_id");
			JsonNode surveyidNode = rootNode.path("survey_id");
			JsonNode completedtimeNode = rootNode.path("completed_time");
			JsonNode surveyresponseNode = rootNode.path("survey_response");
			JsonNode engagementidNode = rootNode.path("engagement_id");

			if (!useridNode.isNull() && !surveyidNode.isNull() && !completedtimeNode.isNull() && !surveyidNode.isNull()) {

				// if engagement id is not passed, retrieve from db. can be null
				Long engagementid = null;
				if (engagementidNode.asLong() == 0l) {
					logger.error("engagement id node is null!!! old version of the application!!! problem!!!");
					// engagementid = db_up.retrieveEngagementId(useridNode.asText(), surveyidNode.asText(), completedtimeNode.asLong());
				} else {
					logger.debug("engagement id node is NOT null: {}", engagementidNode.asLong());
					engagementid = engagementidNode.asLong();
				}

				if (engagementid != null)
					sr = new SurveyResponse(useridNode.asText(), surveyidNode.asText(), completedtimeNode.asLong(), surveyresponseNode.toString(), engagementid);
				else
					logger.warn("engagement id was not found and we cannot retrieve it from db");
			} else {
				logger.warn("some data are null. not adding :{} {} {} {}", useridNode, surveyidNode, completedtimeNode, surveyresponseNode);
			}
		} catch (JsonProcessingException e) {
			logger.error("cannot add due:", e);
		} catch (IOException e) {
			logger.error("cannot add due:", e);
		}

		return sr;
	}

	@PreDestroy
	public void cleanUp() throws Exception {
		try {
			properties.stopit();
			dbi.close();
			super.finalize();
		} catch (Throwable e) {
			logger.error("Error in cleanup:", e);
		}
	}

	private void addUserServiceInterest(String user_id, String engagementServiceUri, Integer rating, String ppoiType) throws MalformedURLException, UnsupportedEncodingException, SocketTimeoutException {
		URL url = new URL(properties.getUserProfilerURL() + "device/" + user_id + "/interest");

		logger.debug("pppppp: {}", url.toString());

		ncg.post(url, properties.getReadTimeoutMillisecond(), Level.ERROR, "serviceuri=" + URLEncoder.encode(engagementServiceUri, "UTF-8") + "&rate=" + rating + "&type=" + ppoiType);
		// TODO manage response
	}

	private void updateUP(String user_id, String ppoi_name, Boolean confirmed) throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();

		// do it just if confirmation==true
		if (confirmed == true) {

			// get the id of the ppoi *just if confirmation==False
			String ppois_string = ncg.get(new URL(properties.getUserProfilerURL() + "device/" + user_id + "/ppoi/" + ppoi_name + "?confirmation=false"), properties.getReadTimeoutMillisecond(), Level.ERROR);

			if (ppois_string.length() > 0) {

				List<PPOI> ppois = mapper.readValue(ppois_string, new TypeReference<List<PPOI>>() {
				});

				// set the confirmation to true
				if (ppois != null)
					for (PPOI ppoi : ppois) {

						logger.debug("confirm the ppoi: {}", ppoi);

						ncg.put(new URL(properties.getUserProfilerURL() + "device/" + user_id + "/ppoi/" + ppoi.getId() + "?confirmation=true"), properties.getReadTimeoutMillisecond(), Level.ERROR);
					}
			}
		}
		// TODO manage response
	}

	private void updateUP(String user_id, String ppoi_name, Date engagementDate) throws MalformedURLException, SocketTimeoutException {
		ncg.post(new URL(properties.getUserProfilerURL() + "device/" + user_id + "/time/" + engagementDate.getTime()), properties.getReadTimeoutMillisecond(), Level.ERROR, "ppoi_name=" + ppoi_name + "&confirmation=true");// always confirmed
		// TODO manage response
	}

	private void updateUP(String user_id, String ppoi_name, Date engagementDate, Boolean confirmation) throws MalformedURLException, SocketTimeoutException {
		ncg.post(new URL(properties.getUserProfilerURL() + "device/" + user_id + "/time/" + engagementDate.getTime()), properties.getReadTimeoutMillisecond(), Level.ERROR, "&confirmation=" + confirmation);// always confirmed
		// TODO manage response
	}
}
