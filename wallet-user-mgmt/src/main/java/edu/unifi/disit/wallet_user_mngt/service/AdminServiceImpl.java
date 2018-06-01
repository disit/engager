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
package edu.unifi.disit.wallet_user_mngt.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import edu.unifi.disit.wallet_user_mngt.datamodel.Campaign;
import edu.unifi.disit.wallet_user_mngt.datamodel.CampaignDAO;
import edu.unifi.disit.wallet_user_mngt.datamodel.Ecosystem;
import edu.unifi.disit.wallet_user_mngt.datamodel.EcosystemDAO;
import edu.unifi.disit.wallet_user_mngt.datamodel.Roletype;
import edu.unifi.disit.wallet_user_mngt.datamodel.Rule;
import edu.unifi.disit.wallet_user_mngt.datamodel.RuleDAO;
import edu.unifi.disit.wallet_user_mngt.datamodel.TraceInDAO;
import edu.unifi.disit.wallet_user_mngt.datamodel.User;
import edu.unifi.disit.wallet_user_mngt.datamodel.UserDAO;

@Service
public class AdminServiceImpl implements IAdminService {

	@Autowired
	private UserDAO userDAO;

	@Autowired
	private RuleDAO ruleDAO;

	@Autowired
	private CampaignDAO campaignDAO;

	@Autowired
	private TraceInDAO traceInDAO;

	@Autowired
	private EcosystemDAO ecosystemDAO;

	@Override
	public Page<User> getAllUsers(PageRequest pageRequest) {
		return userDAO.findAll(pageRequest);
	}

	@Override
	public Page<User> getAllManagers(PageRequest pageRequest) {
		return userDAO.findByRoletype(Roletype.ROLE_MANAGER, pageRequest);
	}

	@Override
	public Page<Object[]> getEcosystems(PageRequest pageRequest) {
		Page<Object[]> alleco = ecosystemDAO.findMyAll(pageRequest);
		List<Object[]> eco_with_rule = ecosystemDAO.findAllAndGroupby();
		for (Object[] o : alleco.getContent()) {
			boolean found = false;
			for (Object[] e : eco_with_rule) {
				if (((Long) o[2]).longValue() == ((Long) e[2]).longValue()) {
					o[1] = e[1];
					found = true;
				}
			}
			if (found == false)
				o[1] = 0;
		}
		return alleco;
	}

	@Override
	public Ecosystem getEcosystem(Long pageNumber) {
		return ecosystemDAO.findById(pageNumber);
	}

	@Override
	public Ecosystem saveEcosystem(Ecosystem ecosystem) {
		return ecosystemDAO.save(ecosystem);
	}

	@Override
	public List<Rule> getRules() {
		return ruleDAO.findAll();
	}

	@Override
	public List<Rule> getRulesByEcosystem(Integer pageNumber) {
		return ruleDAO.findByEcosystemsId(pageNumber.longValue());
	}

	@Override
	public Rule getRuleById(Long id) {
		return ruleDAO.findOne(id);
	}

	@Override
	public void removeEcosystem(Integer removeid) {
		ecosystemDAO.delete(removeid.longValue());
	}

	@Override
	public void saveRules(List<Rule> new_rule) {
		// remove old rules not in ruleList
		List<Rule> old_rules = ruleDAO.findAll();
		for (Rule old_rule : old_rules) {
			if (!new_rule.contains(old_rule)) {
				ruleDAO.delete(old_rule);
			}
		}
		ruleDAO.save(new_rule);
	}

	// check if the passed rule is references by tracein
	@Override
	public boolean getCheckRef(Long id) {
		if ((traceInDAO.countByRuleId(id) == 0) && (ecosystemDAO.countByRulesId(id) == 0))
			return false;
		else
			return true;
	}

	@Override
	public User getUser(Long id) {
		return userDAO.findOne(id);
	}

	@Override
	public void saveUsers(User tosave) {
		// the ecosystem removed are deleted in cascade
		userDAO.save(tosave);
	}

	@Override
	public List<Ecosystem> getEcosystemByUser(String username) {
		return ecosystemDAO.findByUsersUsername(username);
	}

	@Override
	public List<Ecosystem> getEcosystems() {
		return ecosystemDAO.findAll();
	}

	@Override
	public boolean getEcosytemRef(Integer removeid) {
		if ((campaignDAO.countByEcosystemId(removeid.longValue()) == 0) && (userDAO.countByEcosystemsId(removeid.longValue()) == 0) && (ruleDAO.countByEcosystemsId(removeid.longValue()) == 0))
			return false;
		else
			return true;
	}

	@Override
	public List<Campaign> getCampaignByManager(Long currentManagerId) {
		return campaignDAO.findByUserId(currentManagerId);
	}
}
