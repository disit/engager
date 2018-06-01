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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TraceOutDAO extends JpaRepository<TraceOut, Long> {

	@Query("SELECT p.name, t.time FROM TraceOut t INNER JOIN t.user u INNER JOIN t.prize p INNER JOIN p.campaign c WHERE u.username=?1 AND c.ecosystem.id=?2")
	Page<Object[]> findAllTraceOutByUsernameForEcosytem(String findLoggedInUsername, Long ecosystemid, Pageable pageable);

	@Query("SELECT u.username, p.name, t.time FROM TraceOut t INNER JOIN t.user u INNER JOIN t.prize p INNER JOIN p.campaign c WHERE c.id=?1")
	Page<Object[]> findAllForEcosytem(Long id, Pageable pageRequest);

	@Query("SELECT t.time, p.name  FROM TraceOut t INNER JOIN t.user u INNER JOIN t.prize p INNER JOIN p.campaign c WHERE u.username=?1 AND c.name=?2")
	Page<Object[]> getAllTraceoutsForUserForCampaing(String findLoggedInUsername, String campaignName, Pageable pageRequest);
}
