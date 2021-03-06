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

public enum Roletype {
	ROLE_USERNOTENABLED("ROLE_USERNOTENABLED"), ROLE_USER("ROLE_USER"), ROLE_MANAGER("ROLE_MANAGER"), ROLE_ADMIN("ROLE_ADMIN");

	private final String text;

	private Roletype(final String text) {
		this.text = text;
	}

	@Override
	public String toString() {
		return text;
	}
}
