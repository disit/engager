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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import edu.unifi.disit.wallet_user_mngt.datamodel.Campaign;
import edu.unifi.disit.wallet_user_mngt.datamodel.Prize;
import edu.unifi.disit.wallet_user_mngt.object.Pager;
import edu.unifi.disit.wallet_user_mngt.object.PrizeListForm;
import edu.unifi.disit.wallet_user_mngt.object.ScoreboardEntry;
import edu.unifi.disit.wallet_user_mngt.service.IManagerService;
import edu.unifi.disit.wallet_user_mngt.validator.CampaignValidator;
import edu.unifi.disit.wallet_user_mngt.validator.PrizeListFormValidator;

@Controller
public class ManagerController {

	private static final Logger logger = LogManager.getLogger();

	@Autowired
	private MessageSource messages;

	@Autowired
	private IManagerService managerService;

	private static final int BUTTONS_TO_SHOW = 5;
	private static final int INITIAL_PAGE_SIZE_1 = 5;
	private static final int INITIAL_PAGE_SIZE_2 = 5;
	private static final int INITIAL_PAGE_SIZE_3 = 5;

	// MANAGER VIEW--------------------------------------------------------------------------------------------------------------------------------------------
	@RequestMapping(value = "/manager", method = RequestMethod.GET)
	public String redirectManager(Model model) {
		return "redirect:/manager/1";
	}

	@RequestMapping(value = "/manager/{pageNumber}", method = RequestMethod.GET)
	public String manager(Model model, @PathVariable Integer pageNumber) {

		logger.debug("/manage/{} invoked", pageNumber);

		Page<Campaign> campaigns = managerService.getCampaignsForLoggedUser(new PageRequest(pageNumber - 1, INITIAL_PAGE_SIZE_1));
		Pager pager = new Pager(campaigns.getTotalPages(), campaigns.getNumber(), BUTTONS_TO_SHOW);
		model.addAttribute("pager", pager);
		model.addAttribute("current", pageNumber);
		model.addAttribute("campaigns", campaigns);
		model.addAttribute("campaignstatus", managerService.getStatus(campaigns.getContent()));// status of campaign

		return "manager";
	}

	// CAMPAIGN SHOW VIEW--------------------------------------------------------------------------------------------------------------------------------------------
	@RequestMapping(value = "/showcampaign/{id}", method = RequestMethod.GET)
	public String redirectShow(Model model, @PathVariable Long id) {
		return "redirect:/showcampaign/" + id + "/1/1";
	}

	@RequestMapping(value = "/showcampaign/{id}/{pageNumber1}/{pageNumber2}", method = RequestMethod.GET)
	public String show(Model model, @PathVariable Long id, @PathVariable Integer pageNumber1, @PathVariable Integer pageNumber2) {

		model.addAttribute("id", id);

		List<ScoreboardEntry> scoreboardTotal = managerService.getScoreboardTotalPilota();

		Page<ScoreboardEntry> data_1 = new PageImpl<ScoreboardEntry>(scoreboardTotal.subList(INITIAL_PAGE_SIZE_2 * (pageNumber1 - 1), Math.min(INITIAL_PAGE_SIZE_2 * pageNumber1, scoreboardTotal.size())),
				new PageRequest(pageNumber1 - 1, INITIAL_PAGE_SIZE_2),
				scoreboardTotal.size());
		Pager pager_1 = new Pager(data_1.getTotalPages(), data_1.getNumber(), BUTTONS_TO_SHOW);
		model.addAttribute("pager1", pager_1);
		model.addAttribute("current1", pageNumber1);
		model.addAttribute("data1", data_1);

		List<ScoreboardEntry> scoreboard7days = managerService.getScoreboardTotalPilota();

		Page<ScoreboardEntry> data_2 = new PageImpl<ScoreboardEntry>(scoreboard7days.subList(INITIAL_PAGE_SIZE_3 * (pageNumber2 - 1), Math.min(INITIAL_PAGE_SIZE_3 * pageNumber2, scoreboard7days.size())),
				new PageRequest(pageNumber2 - 1, INITIAL_PAGE_SIZE_3),
				scoreboard7days.size());
		Pager pager_2 = new Pager(data_2.getTotalPages(), data_2.getNumber(), BUTTONS_TO_SHOW);
		model.addAttribute("pager2", pager_2);
		model.addAttribute("current2", pageNumber2);
		model.addAttribute("data2", data_2);

		Campaign c = managerService.getCampaign(id);
		model.addAttribute("campaign", c);

		return "showcampaign";
	}

	// CAMPAIGN EDIT VIEW--------------------------------------------------------------------------------------------------------------------------------------------
	@RequestMapping(value = "/campaign/{campid}", method = RequestMethod.GET)
	public String editCampaign(Model model, @PathVariable String campid) {

		Campaign campaign;
		PrizeListForm prizeListForm = new PrizeListForm();
		;
		if (campid.equals("new")) {
			campaign = new Campaign();
			campaign.setStartDate(new Date());

		} else {
			campaign = managerService.getCampaign(Long.parseLong(campid));
			prizeListForm.setPrizeList(new ArrayList<Prize>(campaign.getPrizes()));
		}

		model.addAttribute("campaign", campaign);
		model.addAttribute("prizeListForm", prizeListForm);
		model.addAttribute("ecosystemList", managerService.getEcosystems());

		return "campaign";
	}

	@RequestMapping(value = "/campaign/{campid}", method = RequestMethod.POST, params = { "Save" })
	public String editCampaign(Model model, @PathVariable String campid,
			@ModelAttribute("campaign") Campaign campaign, BindingResult bindingResultCampaign,
			@ModelAttribute("prizeListForm") PrizeListForm prizeListForm, BindingResult bindingResultPrizeListForm) {

		model.addAttribute("pageNumber", campid);

		CampaignValidator campaignValidator = new CampaignValidator(messages);
		campaignValidator.validate(campaign, bindingResultCampaign);

		PrizeListFormValidator prizeListValidator = new PrizeListFormValidator(messages);
		prizeListValidator.validate(prizeListForm, bindingResultPrizeListForm);

		if ((!bindingResultCampaign.hasErrors()) && (!bindingResultPrizeListForm.hasErrors())) {

			campaign = managerService.saveCampaign(campaign, prizeListForm);
			model.addAttribute("message", "Saved");
			model.addAttribute("campid", campaign.getId());
		}

		model.addAttribute("ecosystemList", managerService.getEcosystems());

		return "campaign";
	}

	// temp view
	@RequestMapping(value = "/campaign/{campid}", method = RequestMethod.POST, params = { "Add" })
	public String addPrize(Model model, @PathVariable String campid,
			@ModelAttribute("campaign") Campaign campaign, BindingResult bindingResult,
			@ModelAttribute("prizeListForm") PrizeListForm prizeListForm, BindingResult bindingResultPrizeListForm) {

		Prize p = new Prize();
		p.setName("name" + System.currentTimeMillis());
		p.setValue(0l);
		prizeListForm.getPrizeList().add(p);

		model.addAttribute("ecosystemList", managerService.getEcosystems());

		return "campaign";
	}

	// temp view
	@RequestMapping(value = "/campaign/{campid}", method = RequestMethod.POST)
	public String removePrize(Model model, @PathVariable String campid,
			@ModelAttribute("campaign") Campaign campaign, BindingResult bindingResult,
			@ModelAttribute("prizeListForm") PrizeListForm prizeListForm, BindingResult bindingResultPrizeListForm,
			@RequestParam Integer removeid) {

		if (removeid != null) {
			prizeListForm.getPrizeList().remove(removeid.intValue());
		}

		model.addAttribute("ecosystemList", managerService.getEcosystems());

		return "campaign";
	}

	// private--------------------------------------------------------------------------------------------------------------------------------------------
	@InitBinder
	private void dateBinder(WebDataBinder binder) {
		// The date format to parse or output your dates
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// Create a new CustomDateEditor
		CustomDateEditor editor = new CustomDateEditor(dateFormat, true);
		// Register it as custom editor for the Date type
		binder.registerCustomEditor(Date.class, editor);
	}
}
