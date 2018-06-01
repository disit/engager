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
package edu.unifi.disit.engagerapi.datamodel;

import java.sql.Timestamp;

public class UserClick {

	String userName;
	Timestamp timestamp;

	public UserClick(String userName, Timestamp timestamp) {
		this.userName = userName;
		this.timestamp = timestamp;
	}

	// from db
	// 0 is timestamp
	// 1 is username
	public UserClick(Object[] fromDB) {
		this.timestamp = (Timestamp) fromDB[0];
		this.userName = (String) fromDB[1];
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Timestamp getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public String toString() {
		return "UserClick [userName=" + userName + ", timestamp=" + timestamp + "]";
	}
}
