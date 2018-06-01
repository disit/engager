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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import edu.unifi.disit.wallet_user_mngt.datamodel.Campaign;
import edu.unifi.disit.wallet_user_mngt.datamodel.Ecosystem;
import edu.unifi.disit.wallet_user_mngt.datamodel.Rule;
import edu.unifi.disit.wallet_user_mngt.datamodel.User;

public interface IAdminService {

	Page<User> getAllUsers(PageRequest pageRequest);

	Page<User> getAllManagers(PageRequest pageRequest);

	Ecosystem saveEcosystem(Ecosystem ecosystem);

	Page<Object[]> getEcosystems(PageRequest pageRequest);

	List<Rule> getRules();

	List<Rule> getRulesByEcosystem(Integer pageNumber);

	Rule getRuleById(Long id);

	void removeEcosystem(Integer removeid);

	void saveRules(List<Rule> ruleList);

	boolean getCheckRef(Long id);

	User getUser(Long id);

	void saveUsers(User tosave);

	List<Ecosystem> getEcosystemByUser(String username);

	List<Ecosystem> getEcosystems();

	Ecosystem getEcosystem(Long pageNumber);

	boolean getEcosytemRef(Integer removeid);

	List<Campaign> getCampaignByManager(Long currentManagerId);
}
