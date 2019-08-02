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

import java.util.Calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import edu.unifi.disit.wallet_user_mngt.object.Pager;
import edu.unifi.disit.wallet_user_mngt.service.IUserService;

@Controller
public class UserController {

	// private static final Logger logger = LogManager.getLogger();

	@Autowired
	private MessageSource messages;

	@Autowired
	private IUserService userService;

	@Autowired
	ApplicationEventPublisher eventPublisher;

	private static final int BUTTONS_TO_SHOW = 5;
	private static final int INITIAL_PAGE_SIZE_1 = 5;
	private static final int INITIAL_PAGE_SIZE_2 = 5;
	private static final int INITIAL_PAGE_SIZE_3 = 5;
	private static final int INITIAL_PAGE_SIZE_4 = 5;
	private static final int INITIAL_PAGE_SIZE_5 = 5;
	private static final int INITIAL_PAGE_SIZE_6 = 5;

	private static final String CAMPAIGN_PILOTA = "PILOTA";

	@RequestMapping(value = "/api/test", method = RequestMethod.GET)
	public ResponseEntity<String> engagerTest() {
		return new ResponseEntity<String>("alive", HttpStatus.OK);
	}

	// HOME VIEW-----------------------------------------------------------------------------------------------------------
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home() {
		return "redirect:/home";
	}

	@RequestMapping(value = "/home", method = RequestMethod.GET)
	public String redirectHome(Model model, String error, String logout, String message) {

		if (error != null)
			return "redirect:/home/1?error";

		if (logout != null)
			return "redirect:/home/1?logout";

		if (message != null)
			return "redirect:/home/1?message=" + message;

		return "redirect:/home/1";
	}

	@RequestMapping(value = "/home/{pageNumber}", method = RequestMethod.GET)
	public String home(Model model, String error, String logout, String message, @PathVariable Integer pageNumber) {

		if (error != null)
			model.addAttribute("error", messages.getMessage("signin.usernamepasswordinvalid", null, LocaleContextHolder.getLocale()));

		if (logout != null)
			model.addAttribute("logout", messages.getMessage("logout.ok", null, LocaleContextHolder.getLocale()));

		if (message != null)
			model.addAttribute("message", message);

		Page<Object[]> data = userService.getTotalPointForAllUsersVisible(new PageRequest(pageNumber - 1, INITIAL_PAGE_SIZE_1));
		Pager pager = new Pager(data.getTotalPages(), data.getNumber(), BUTTONS_TO_SHOW);
		model.addAttribute("data", data);
		model.addAttribute("current", pageNumber);
		model.addAttribute("pager", pager);

		return "home";
	}

	// USER VIEW-------------------------------------------------------------------------------------------------------------------------------
	@RequestMapping(value = "/user", method = RequestMethod.GET)
	public String redirectUser(Model model, String message) {
		if (message != null)
			return "redirect:/user/1/1/1?message=" + message;
		else
			return "redirect:/user/1/1/1";
	}

	@RequestMapping(value = "/user/{pageNumber1}/{pageNumber2}/{pageNumber3}", method = RequestMethod.GET)
	public String user(Model model, String message, @PathVariable Integer pageNumber1, @PathVariable Integer pageNumber2, @PathVariable Integer pageNumber3) {

		if (message != null)
			model.addAttribute("message", message);

		Integer totalPoint = userService.getAllPointsForLoggedUserForCampaign(CAMPAIGN_PILOTA);
		model.addAttribute("totalpoint", totalPoint);

		Page<Object[]> data_1 = userService.getAllTraceinsForLoggedUserForCampaign(CAMPAIGN_PILOTA, new PageRequest(pageNumber1 - 1, INITIAL_PAGE_SIZE_2));
		Pager pager_1 = new Pager(data_1.getTotalPages(), data_1.getNumber(), BUTTONS_TO_SHOW);
		model.addAttribute("pager1", pager_1);
		model.addAttribute("current1", pageNumber1);
		model.addAttribute("data1", data_1);

		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		c.add(Calendar.HOUR, -24 * 7);

		Integer point7days = userService.getPointsFromDateForLoggedUsersForCampaign(CAMPAIGN_PILOTA, c.getTime());
		model.addAttribute("point7days", point7days);

		Page<Object[]> data_2 = userService.getTraceinsFromDateForLoggedUserForCampaign(CAMPAIGN_PILOTA, new PageRequest(pageNumber2 - 1, INITIAL_PAGE_SIZE_3), c.getTime());
		Pager pager_2 = new Pager(data_2.getTotalPages(), data_2.getNumber(), BUTTONS_TO_SHOW);
		model.addAttribute("pager2", pager_2);
		model.addAttribute("current2", pageNumber2);
		model.addAttribute("data2", data_2);

		Page<Object[]> data_3 = userService.getAllTraceoutsForLoggedUserForCampaign(CAMPAIGN_PILOTA, new PageRequest(pageNumber3 - 1, INITIAL_PAGE_SIZE_4));
		Pager pager_3 = new Pager(data_3.getTotalPages(), data_3.getNumber(), BUTTONS_TO_SHOW);
		model.addAttribute("pager3", pager_3);
		model.addAttribute("current3", pageNumber3);
		model.addAttribute("data3", data_3);

		return "user";
	}

	// PRIZE VIEW -------------------------------------------------------------------------------------------------------------------------------
	@RequestMapping(value = "/prize/{ecoid}", method = RequestMethod.GET)
	public String redirectRequest(Model model, @PathVariable Long ecoid) {
		return "redirect:/prize/" + ecoid + "/1/1";
	}

	@RequestMapping(value = "/prize/{ecoid}/{pageNumber1}/{pageNumber2}", method = RequestMethod.GET)
	public String request(Model model, @PathVariable Long ecoid, @PathVariable Integer pageNumber1, @PathVariable Integer pageNumber2) {

		// model.addAttribute("id", id);
		model.addAttribute("ecosystemname", userService.getNameOfEcosystem(ecoid));
		model.addAttribute("ecosystempoint", userService.getCurrentPointEcosystemForLoggedUser(ecoid));

		Page<Object[]> data_1 = userService.getPrizesForEcosystem(ecoid, new PageRequest(pageNumber1 - 1, INITIAL_PAGE_SIZE_5));
		Pager pager_1 = new Pager(data_1.getTotalPages(), data_1.getNumber(), BUTTONS_TO_SHOW);

		model.addAttribute("pager1", pager_1);
		model.addAttribute("current1", pageNumber1);
		model.addAttribute("data1", data_1);

		Page<Object[]> data_2 = userService.findAllTraceOutForLoggedUserForEcosystem(ecoid, new PageRequest(pageNumber2 - 1, INITIAL_PAGE_SIZE_6));
		Pager pager_2 = new Pager(data_2.getTotalPages(), data_2.getNumber(), BUTTONS_TO_SHOW);
		model.addAttribute("pager2", pager_2);
		model.addAttribute("current2", pageNumber2);
		model.addAttribute("data2", data_2);

		return "prize";
	}
}
