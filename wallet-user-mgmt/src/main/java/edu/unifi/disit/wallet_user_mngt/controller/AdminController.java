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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import edu.unifi.disit.wallet_user_mngt.datamodel.Campaign;
import edu.unifi.disit.wallet_user_mngt.datamodel.Ecosystem;
import edu.unifi.disit.wallet_user_mngt.datamodel.Roletype;
import edu.unifi.disit.wallet_user_mngt.datamodel.Rule;
import edu.unifi.disit.wallet_user_mngt.datamodel.User;
import edu.unifi.disit.wallet_user_mngt.datamodel.feedback.EmailFeedbackCampaign;
import edu.unifi.disit.wallet_user_mngt.datamodel.feedback.EmailFeedbackCampaignDAO;
import edu.unifi.disit.wallet_user_mngt.datamodel.feedback.EmailFeedbackCampaignToken;
import edu.unifi.disit.wallet_user_mngt.datamodel.feedback.EmailFeedbackCampaignTokenDAO;
import edu.unifi.disit.wallet_user_mngt.object.CampaignStatus;
import edu.unifi.disit.wallet_user_mngt.object.EcoEcosystemListForm;
import edu.unifi.disit.wallet_user_mngt.object.EcosystemListForm;
import edu.unifi.disit.wallet_user_mngt.object.Pager;
import edu.unifi.disit.wallet_user_mngt.object.RoletypeListForm;
import edu.unifi.disit.wallet_user_mngt.object.RuleListForm;
import edu.unifi.disit.wallet_user_mngt.object.UserListForm;
import edu.unifi.disit.wallet_user_mngt.service.IAdminService;
import edu.unifi.disit.wallet_user_mngt.service.IManagerService;
import edu.unifi.disit.wallet_user_mngt.validator.EcosystemValidator;

@Controller
public class AdminController {

	// private static final Logger logger = LogManager.getLogger();

	@Autowired
	private MessageSource messages;

	@Autowired
	private IAdminService adminService;

	@Autowired
	private IManagerService managerService;

	@Autowired
	private EmailFeedbackCampaignTokenDAO emailFeedbackCampaignTokenDAO;

	@Autowired
	private EmailFeedbackCampaignDAO emailFeedbackCampaignDAO;

	private static final int BUTTONS_TO_SHOW = 5;
	private static final int INITIAL_PAGE_SIZE_1 = 5;
	private static final int INITIAL_PAGE_SIZE_2 = 5;

	// ADMIN VIEW --------------------------------------------------------------------------------------------------------------------------------------------------
	@RequestMapping(value = "/admin", method = RequestMethod.GET)
	public String redirectAdmin(Model model) {
		return "redirect:/admin/1";
	}

	@RequestMapping(value = "/admin/{pageNumber}", method = RequestMethod.GET)
	public String admin(Model model, @PathVariable Integer pageNumber) {

		Page<Object[]> data = adminService.getEcosystems(new PageRequest(pageNumber - 1, INITIAL_PAGE_SIZE_1));
		Pager pager = new Pager(data.getTotalPages(), data.getNumber(), BUTTONS_TO_SHOW);
		model.addAttribute("pager", pager);
		model.addAttribute("current", pageNumber);
		model.addAttribute("data", data);

		return "admin";
	}

	@RequestMapping(value = "/admin/{pageNumber}", method = RequestMethod.POST)
	public String removeEcosystem(Model model, @PathVariable Integer pageNumber, @RequestParam Integer removeid) {

		if (removeid != null) {
			if (adminService.getEcosytemRef(removeid))
				model.addAttribute("message", messages.getMessage("editeco.notpossible", null, LocaleContextHolder.getLocale()));
			else
				adminService.removeEcosystem(removeid);
		}

		// update the model with new current data
		Page<Object[]> data = adminService.getEcosystems(new PageRequest(pageNumber - 1, INITIAL_PAGE_SIZE_1));
		// if the page now is empty, go back 1
		if ((data.getContent().size() == 0) && (pageNumber > 0)) {
			pageNumber--;
			data = adminService.getEcosystems(new PageRequest(pageNumber - 1, INITIAL_PAGE_SIZE_1));
		}
		Pager pager = new Pager(data.getTotalPages(), data.getNumber(), BUTTONS_TO_SHOW);
		model.addAttribute("pager", pager);
		model.addAttribute("current", pageNumber);
		model.addAttribute("data", data);

		return "admin";
	}

	// ECOSYSTEM VIEW --------------------------------------------------------------------------------------------------------------------------------------------------
	@RequestMapping(value = "/ecosystem/{ecoid}", method = RequestMethod.GET)
	public String showEcosystem(Model model, @PathVariable String ecoid) {

		RuleListForm myrules = new RuleListForm();

		if (!ecoid.equals("new")) {
			model.addAttribute("ecosystem", adminService.getEcosystem(Long.valueOf(ecoid)));
			myrules.setRuleList(adminService.getRulesByEcosystem(Integer.valueOf(ecoid)));
		} else
			model.addAttribute("ecosystem", new Ecosystem());

		model.addAttribute("ruletoadd", new Rule());
		model.addAttribute("myrules", myrules);

		model.addAttribute("allrules", adminService.getRules());

		return "ecosystem";
	}

	@RequestMapping(value = "/ecosystem/{ecoid}", method = RequestMethod.POST, params = { "Save" })
	public String saveEcosystem(Model model, @PathVariable String ecoid,
			@ModelAttribute Ecosystem ecosystem, BindingResult bindingResultEcosytem,
			@ModelAttribute("ruletoadd") Rule ruletoadd,
			@ModelAttribute("myrules") RuleListForm myrules) {

		EcosystemValidator ecosystemValidator = new EcosystemValidator(messages);
		ecosystemValidator.validate(ecosystem, bindingResultEcosytem);

		Ecosystem eco_to_save = ecosystem;

		// update name + rules
		if (!ecoid.equals("new")) {
			eco_to_save = adminService.getEcosystem(Long.valueOf(ecoid));
			eco_to_save.setName(ecosystem.getName());
		}

		if (!bindingResultEcosytem.hasErrors()) {
			eco_to_save.setRules(new HashSet<Rule>(myrules.getRuleList()));
			ecosystem = adminService.saveEcosystem(eco_to_save);
			model.addAttribute("message", "Saved");
			model.addAttribute("ecoid", ecosystem.getId());
		}

		model.addAttribute("allrules", adminService.getRules());

		return "ecosystem";
	}

	// temp view
	@RequestMapping(value = "/ecosystem/{ecoid}", method = RequestMethod.POST, params = { "Add" })
	public String addRule(Model model, @PathVariable String ecoid,
			@ModelAttribute Ecosystem ecosystem, BindingResult bindingResultEcosytem,
			@ModelAttribute("ruletoadd") Rule ruletoadd,
			@ModelAttribute("myrules") RuleListForm myrules) {

		if (ruletoadd.getId() != -1) {
			List<Rule> rules = myrules.getRuleList();
			rules.add(adminService.getRuleById(ruletoadd.getId()));
			myrules.setRuleList(rules);
			model.addAttribute("ruletoadd", new Rule());
		} else
			model.addAttribute("error", messages.getMessage("editeco.notpossible.rule", null, LocaleContextHolder.getLocale()));

		model.addAttribute("allrules", adminService.getRules());

		return "ecosystem";
	}

	// temp view
	@RequestMapping(value = "/ecosystem/{ecoid}", method = RequestMethod.POST)
	public String removeRule(Model model, @PathVariable String ecoid,
			@ModelAttribute Ecosystem ecosystem, BindingResult bindingResultEcosytem,
			@ModelAttribute("ruletoadd") Rule ruletoadd,
			@ModelAttribute("myrules") RuleListForm myrules,
			@RequestParam Integer removeid) {

		if (removeid != null)
			myrules.getRuleList().remove(removeid.intValue());

		model.addAttribute("allrules", adminService.getRules());

		return "ecosystem";
	}

	// RULES VIEW --------------------------------------------------------------------------------------------------------------------------------------------------
	@RequestMapping(value = "/rules", method = RequestMethod.GET)
	public String editRules(Model model) {

		RuleListForm ruleList = new RuleListForm();
		ruleList.setRuleList(adminService.getRules());
		model.addAttribute("ruleList", ruleList);

		return "rules";
	}

	@RequestMapping(value = "/rules", method = RequestMethod.POST, params = { "Save" })
	public String saveRules(Model model,
			@ModelAttribute("ruleList") RuleListForm ruleList) {

		if ((ruleList.getRuleList() == null) || (checkRuleListNameNotNull(ruleList.getRuleList()))) {
			model.addAttribute("error", messages.getMessage("editeco.notpossible.rule", null, LocaleContextHolder.getLocale()));
		} else {
			adminService.saveRules(ruleList.getRuleList());
			model.addAttribute("message", messages.getMessage("saved", null, LocaleContextHolder.getLocale()));
		}

		return "rules";
	}

	@RequestMapping(value = "/rules", method = RequestMethod.POST, params = { "Add" })
	public String addPrize(Model model,
			@ModelAttribute("ruleList") RuleListForm ruleList) {

		Rule r = new Rule();
		r.setName("name" + System.currentTimeMillis());
		r.setValue(0l);
		ruleList.getRuleList().add(r);

		return "rules";
	}

	@RequestMapping(value = "/rules", method = RequestMethod.POST)
	public String removePrize(Model model,
			@ModelAttribute("ruleList") RuleListForm ruleList,
			@RequestParam Integer removeid) {

		if (removeid != null) {

			if (((ruleList.getRuleList().get(removeid.intValue()).getId()) != null) && (adminService.getCheckRef(ruleList.getRuleList().get(removeid.intValue()).getId()))) {
				model.addAttribute("error", messages.getMessage("editrules.notpossible", null, LocaleContextHolder.getLocale()));
			} else {
				ruleList.getRuleList().remove(removeid.intValue());
			}
		}

		return "rules";
	}

	// USER VIEW--------------------------------------------------------------------------------------------------------------------------------------------------
	@RequestMapping(value = "/editusers", method = RequestMethod.GET)
	public String redirectEditUsers(Model model) {
		return "redirect:/editusers/1";
	}

	@RequestMapping(value = "/editusers/{pageNumber1}", method = RequestMethod.GET)
	public String editUsers(Model model, @PathVariable Integer pageNumber1) {

		Page<User> data_1 = adminService.getAllUsers(new PageRequest(pageNumber1 - 1, INITIAL_PAGE_SIZE_2));
		Pager pager_1 = new Pager(data_1.getTotalPages(), data_1.getNumber(), BUTTONS_TO_SHOW);
		model.addAttribute("pager1", pager_1);
		model.addAttribute("current1", pageNumber1);
		model.addAttribute("userList", new UserListForm(data_1.getContent()));

		EcoEcosystemListForm ecoList = new EcoEcosystemListForm();
		List<Roletype> rtl = new ArrayList<Roletype>();
		for (User u : data_1.getContent()) {
			rtl.add(u.getRoletype());
			EcosystemListForm e = new EcosystemListForm();
			e.setEcosystemList(adminService.getEcosystemByUser(u.getUsername()));
			ecoList.getEcosystemListForm().add(e);
		}

		model.addAttribute("roletypeList", new RoletypeListForm(rtl));
		model.addAttribute("ecoList", ecoList);
		model.addAttribute("allEcosystemList", adminService.getEcosystems());

		return "editusers";
	}

	@RequestMapping(value = "/editusers/{pageNumber1}", method = RequestMethod.POST, params = { "Save" })
	public String saveEco(Model model, @PathVariable Integer pageNumber1,
			@ModelAttribute("userList") UserListForm userList,
			@ModelAttribute("roletypeList") RoletypeListForm roletypeList,
			@ModelAttribute("ecoList") EcoEcosystemListForm ecoList) {

		// if none of the displayed is a manager, the returned list is empty, so we need to reset it!
		if (ecoList.getEcosystemListForm().size() != userList.getUserList().size()) {
			for (int i = ecoList.getEcosystemListForm().size(); i < userList.getUserList().size(); i++) {
				EcosystemListForm e = new EcosystemListForm();
				ecoList.getEcosystemListForm().add(e);
			}
		}

		int i = 0;
		for (User u : userList.getUserList()) {
			User tosave = adminService.getUser(u.getId());
			tosave.setRoletype(roletypeList.getRoletypeList().get(i));
			tosave.setEcosystems(new HashSet<Ecosystem>(ecoList.getEcosystemListForm().get(i).getEcosystemList()));
			adminService.saveUsers(tosave);
			i++;
		}

		Page<User> data_1 = adminService.getAllUsers(new PageRequest(pageNumber1 - 1, INITIAL_PAGE_SIZE_2));
		Pager pager_1 = new Pager(data_1.getTotalPages(), data_1.getNumber(), BUTTONS_TO_SHOW);
		model.addAttribute("pager1", pager_1);
		model.addAttribute("current1", pageNumber1);
		model.addAttribute("userList", new UserListForm(data_1.getContent()));

		model.addAttribute("message", "saved");

		model.addAttribute("allEcosystemList", adminService.getEcosystems());

		return "editusers";
	}

	// temp view
	@RequestMapping(value = "/editusers/{pageNumber1}", method = RequestMethod.POST, params = { "Add" })
	public String addEco(Model model, @PathVariable Integer pageNumber1,
			@ModelAttribute("roletypeList") RoletypeListForm roletypeList,
			@ModelAttribute("userList") UserListForm userList,
			@ModelAttribute("ecoList") EcoEcosystemListForm ecoList) {

		// if none of the displayed is a manager, the returned list is empty, so we need to reset it!
		if (ecoList.getEcosystemListForm().size() != userList.getUserList().size()) {
			for (int i = ecoList.getEcosystemListForm().size(); i < userList.getUserList().size(); i++) {
				EcosystemListForm e = new EcosystemListForm();
				ecoList.getEcosystemListForm().add(e);
			}
		}

		for (EcosystemListForm eco : ecoList.getEcosystemListForm()) {
			if (eco.getToadd() != -1) {
				Ecosystem e = adminService.getEcosystem(eco.getToadd());
				eco.getEcosystemList().add(e);
				eco.setToadd(-1l);
			}

		}

		Page<User> data_1 = adminService.getAllUsers(new PageRequest(pageNumber1 - 1, INITIAL_PAGE_SIZE_2));
		Pager pager_1 = new Pager(data_1.getTotalPages(), data_1.getNumber(), BUTTONS_TO_SHOW);
		model.addAttribute("pager1", pager_1);
		model.addAttribute("current1", pageNumber1);
		model.addAttribute("userList", new UserListForm(data_1.getContent()));

		model.addAttribute("allEcosystemList", adminService.getEcosystems());

		return "editusers";
	}

	// temp-view
	@RequestMapping(value = "/editusers/{pageNumber1}", method = RequestMethod.POST)
	public String removeEcoForManager(Model model, @PathVariable Integer pageNumber1,
			@RequestParam String removeid,
			@ModelAttribute("roletypeList") RoletypeListForm roletypeList,
			@ModelAttribute("userList") UserListForm userList,
			@ModelAttribute("ecoList") EcoEcosystemListForm ecoList) {

		// if none of the displayed is a manager, the returned list is empty, so we need to reset it!
		if (ecoList.getEcosystemListForm().size() != userList.getUserList().size()) {
			for (int i = ecoList.getEcosystemListForm().size(); i < userList.getUserList().size(); i++) {
				EcosystemListForm e = new EcosystemListForm();
				ecoList.getEcosystemListForm().add(e);
			}
		}

		if (removeid != null) {
			// 1;4 means the eco-1 in the user-4
			int ecoIndex = Integer.valueOf(removeid.substring(0, removeid.indexOf(";")));
			int userIndex = Integer.valueOf(removeid.substring(removeid.indexOf(";") + 1));
			ecoList.getEcosystemListForm().get(userIndex).getEcosystemList().remove(ecoIndex);
		}

		Page<User> data_1 = adminService.getAllUsers(new PageRequest(pageNumber1 - 1, INITIAL_PAGE_SIZE_2));
		Pager pager_1 = new Pager(data_1.getTotalPages(), data_1.getNumber(), BUTTONS_TO_SHOW);
		model.addAttribute("pager1", pager_1);
		model.addAttribute("current1", pageNumber1);
		model.addAttribute("userList", new UserListForm(data_1.getContent()));

		model.addAttribute("allEcosystemList", adminService.getEcosystems());

		return "editusers";
	}

	// REPORT CAMPAIGN VIEW --------------------------------------------------------------------------------------------------------------------------------------------------
	@RequestMapping(value = "/reportcampaign", method = RequestMethod.GET)
	public String redirectReportManager(Model model) {
		return "redirect:/reportcampaign/1";
	}

	// manager id is the i/esimo in the list of data_1
	@RequestMapping(value = "/reportcampaign/{managerid}", method = RequestMethod.GET)
	public String reportManager(Model model, @PathVariable Integer managerid) {

		Page<User> data_1 = adminService.getAllManagers(new PageRequest(managerid - 1, 1));
		Pager pager_1 = new Pager(data_1.getTotalPages(), data_1.getNumber(), BUTTONS_TO_SHOW);
		model.addAttribute("pager", pager_1);
		model.addAttribute("current", managerid);
		model.addAttribute("userList", new UserListForm(data_1.getContent()));

		// if there are any managers
		if (data_1.getContent().size() > 0) {
			Long currentManagerId = data_1.getContent().get(0).getId();
			List<Campaign> campaigns = adminService.getCampaignByManager(currentManagerId);
			model.addAttribute("campaigns", campaigns);
			model.addAttribute("campaignstatus", managerService.getStatus(campaigns));// status of campaign
		} else {
			// else empty campaigns
			model.addAttribute("campaigns", new ArrayList<Campaign>());
			model.addAttribute("campaignstatus", new ArrayList<CampaignStatus>());// status of campaign
		}

		return "reportcampaign";

	}

	// FEEDBACK FROM EMAIL --------------------------------------------------------------------------------------------------------------------------------------------------
	@RequestMapping(value = "/emailfeedback", method = RequestMethod.GET)
	public String redirectEmailFeedbacked(Model model) {
		return "redirect:/emailfeedback/1";
	}

	@RequestMapping(value = "/emailfeedback/{pageNumber}", method = RequestMethod.GET)
	public String emailFeedback(Model model, @PathVariable Long pageNumber) {

		List<EmailFeedbackCampaignToken> efcts = emailFeedbackCampaignTokenDAO.findByConfirmedRejectedNotNullAndEmailfeedbackcampaign_Id(pageNumber);

		EmailFeedbackCampaign efc = emailFeedbackCampaignDAO.findOne(pageNumber);

		Pager pager = new Pager((int) emailFeedbackCampaignDAO.count(), pageNumber.intValue(), BUTTONS_TO_SHOW);

		model.addAttribute("name", efc.getName());
		model.addAttribute("pager", pager);
		model.addAttribute("current", pageNumber);
		model.addAttribute("data", efcts);

		return "emailfeedback";
	}

	// -------private------------------------------------------------------------------------------------------------------------------
	private boolean checkRuleListNameNotNull(List<Rule> list) {
		for (Rule p : list) {
			if ((p.getName() == null) || (p.getName().equals("")))
				return true;
		}
		return false;
	}
}
