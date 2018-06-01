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

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TraceInDAO extends JpaRepository<TraceIn, Long> {

	List<TraceIn> findAll();

	@Query("SELECT u.username, SUM(r.value) FROM TraceIn ti INNER JOIN ti.user u INNER JOIN ti.rule r GROUP BY u.username ORDER BY SUM(r.value) DESC")
	Page<Object[]> countTotalPointForAllUsers(Pageable pageable);

	@Query("SELECT u.username, u.nickname, SUM(r.value) FROM TraceIn ti INNER JOIN ti.user u INNER JOIN ti.rule r WHERE u.visible=true GROUP BY u.username, u.nickname ORDER BY SUM(r.value) DESC")
	Page<Object[]> countTotalPointForAllUsersVisible(Pageable pageable);

	@Query("SELECT SUM(r.value) FROM TraceIn ti INNER JOIN ti.user u INNER JOIN ti.rule r WHERE u.username=?1")
	Integer countTotalPointForAllUsers(String findLoggedInUsername);

	@Query("SELECT e.name, SUM(r.value), e.id FROM TraceIn ti INNER JOIN ti.user u INNER JOIN ti.rule r INNER JOIN ti.campaign c INNER JOIN c.ecosystem e WHERE u.username=?1 GROUP BY e.name, e.id")
	Page<Object[]> countPointForEcosystemForLoggedUser(String string, Pageable pageable);

	// same as above, with filter on ecosystemId
	@Query("SELECT SUM(r.value) FROM TraceIn ti INNER JOIN ti.user u INNER JOIN ti.rule r INNER JOIN ti.campaign c INNER JOIN c.ecosystem e WHERE u.username=?1 AND e.id=?2")
	Long countPointForEcosystem(String findLoggedInUsername, Long ecosystemId);

	@Query("SELECT ti.time, r.name, r.value FROM TraceIn ti INNER JOIN ti.user u INNER JOIN ti.rule r INNER JOIN ti.campaign c WHERE u.username=?1")
	Page<Object[]> countAllPointForLoggedUser(String findLoggedInUsername, Pageable pageable);

	@Query("SELECT u.username, r.name, t.time FROM TraceIn t INNER JOIN t.user u INNER JOIN t.rule r WHERE t.campaign.id=?1")
	Page<Object[]> findAllForEcosytem(Long id, Pageable pageRequest);

	Long countByRuleId(Long id);

	@Query("SELECT SUM(r.value) FROM TraceIn ti INNER JOIN ti.user u INNER JOIN ti.rule r INNER JOIN ti.campaign c WHERE u.username=?1 and c.name=?2")
	Integer countAllPointsForUserForCampaing(String findLoggedInUsername, String campaignName);

	@Query("SELECT ti.time, r.label, r.value FROM TraceIn ti INNER JOIN ti.user u INNER JOIN ti.rule r INNER JOIN ti.campaign c WHERE u.username=?1 and c.name=?2")
	Page<Object[]> getAllTraceinsForUserForCampaing(String findLoggedInUsername, String campaignName, Pageable pageRequest);

	@Query("SELECT SUM(r.value) FROM TraceIn ti INNER JOIN ti.user u INNER JOIN ti.rule r INNER JOIN ti.campaign c WHERE u.username=?1 and c.name=?2 and ti.time>?3")
	Integer countPointsForUserForCampaingFromDate(String findLoggedInUsername, String campaignName, Date from);

	@Query("SELECT ti.time, r.label, r.value FROM TraceIn ti INNER JOIN ti.user u INNER JOIN ti.rule r INNER JOIN ti.campaign c WHERE u.username=?1 and c.name=?2 and ti.time>?3")
	Page<Object[]> getAllTraceinsForUserForCampaingFromDate(String findLoggedInUsername, String campaignName, Date from, Pageable pageRequest);
}
