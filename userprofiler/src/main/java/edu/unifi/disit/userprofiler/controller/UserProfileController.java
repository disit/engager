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
package edu.unifi.disit.userprofiler.controller;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.unifi.disit.userprofiler.datamodel.UserProfile;
import edu.unifi.disit.userprofiler.service.IUserProfileService;

@SuppressWarnings({ "rawtypes", "unchecked" })
@RestController
public class UserProfileController {

	private static final Logger logger = LogManager.getLogger();

	@Autowired
	IUserProfileService userProfileService;

	// -------------------Retrieve All UserProfiles---------------------------------------------
	@RequestMapping(value = "/api/v1/userprofile/", method = RequestMethod.GET)
	public ResponseEntity<List<UserProfile>> getAllUserProfilesV1(@RequestParam(value = "lang", defaultValue = "en") String lang) {

		logger.debug("Requested all user profiles, lang {}", lang);

		List<UserProfile> users = userProfileService.getAllUserProfiles(lang);
		if ((users == null) || (users.isEmpty())) {
			return new ResponseEntity(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<List<UserProfile>>(users, HttpStatus.OK);
	}

	// -------------------Retrieve Single UserProfile------------------------------------------
	@RequestMapping(value = "/api/v1/userprofile/{email}", method = RequestMethod.GET)
	public ResponseEntity<UserProfile> getUserProfileV1(@PathVariable("email") String email, @RequestParam(value = "lang", defaultValue = "en") String lang) {

		logger.debug("Requested user profile for {}, lang {}", email, lang);

		UserProfile user = userProfileService.getUserProfile(email, lang);

		if (user == null) {
			return new ResponseEntity(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<UserProfile>(user, HttpStatus.OK);
	}
}
