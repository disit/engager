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

import org.springframework.data.jpa.repository.JpaRepository;

public interface LastExecutedAnalyzedDAO extends JpaRepository<LastExecutedAnalyzed, Long> {

	// @Query("SELECT * FROM LastExecutedAnalyzed l WHERE l.id=0")
	// LastExecutedAnalyzed getLastExecuted();

	// @Query("UPDATE LastExecutedAnalyzed l SET l.lastExecuted=?1 WHERE l.id=0")
	// void setLastExecuted(Long last_executed);

	// @Query("INSERT INTO LastExecutedAnalyzed(id,lastExecuted) VALUES(0,?1)")
	// void insertLastExecuted(Long last_executed);
}
