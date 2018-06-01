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

public interface EcosystemDAO extends JpaRepository<Ecosystem, Long> {

	Ecosystem findByName(String name);

	Ecosystem findById(Long id);

	List<Ecosystem> findByUsersUsername(String username);

	@Query("SELECT e.name, e.id, e.id FROM Ecosystem e")
	Page<Object[]> findMyAll(Pageable pageRequest);

	@Query("SELECT e.name, COUNT(r), e.id FROM Ecosystem e INNER JOIN e.rules r GROUP BY e.name, e.id")
	List<Object[]> findAllAndGroupby();

	List<Ecosystem> findByUsersId(Long id);

	Long countByRulesId(Long id);
}
