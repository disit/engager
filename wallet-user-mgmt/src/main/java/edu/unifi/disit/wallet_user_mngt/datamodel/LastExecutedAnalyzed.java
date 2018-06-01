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

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class LastExecutedAnalyzed {

	@Id
	private Long id;

	private Long lastExecuted;

	public LastExecutedAnalyzed() {
		super();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getLastExecuted() {
		return lastExecuted;
	}

	public void setLastExecuted(Long lastExecuted) {
		this.lastExecuted = lastExecuted;
	}

	@Override
	public String toString() {
		return "LastExecutedAnalyzed [id=" + id + ", lastExecuted=" + lastExecuted + "]";
	}
}
