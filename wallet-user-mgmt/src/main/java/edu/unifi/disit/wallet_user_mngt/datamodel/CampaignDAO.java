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
package edu.unifi.disit.wallet_user_mngt.datamodel;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CampaignDAO extends JpaRepository<Campaign, Long> {

	Campaign findByName(String string);

	Campaign findById(Long id);

	@Query("SELECT c FROM Campaign c INNER JOIN c.user u WHERE u.username=?1")
	Page<Campaign> getCampaigns(String findLoggedInUsername, Pageable pageRequest);

	Long countByEcosystemId(Long id);

	List<Campaign> findByUserId(Long id);

	@Query("SELECT c FROM Campaign c INNER JOIN c.ecosystem e INNER JOIN e.rules r WHERE r.name=?1")
	List<Campaign> getCampaignIdFromRulename(String ruleName);

}
