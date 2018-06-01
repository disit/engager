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
package edu.unifi.disit.commons.datamodel;

public enum DatasetType {
	USERPROFILE("USERPROFILE", "User Profile"), DEVICE("DEVICE", "Device"), USERBEHAVIOUR("USERBEHAVIOUR", "User Behaviour"), IOTAPP("IOTAPP", "IOT App"), CONTRIBUTION("CONTRIBUTION", "Contribution");

	private final String text;
	private final String label;

	private DatasetType(final String text, final String label) {
		this.text = text;
		this.label = label;

	}

	@Override
	public String toString() {
		return text;
	}

	public String getLabel() {
		return label;
	}

	public static DatasetType findBy(String tosearch) {
		for (DatasetType v : values()) {
			if (v.toString().equals(tosearch)) {
				return v;
			}
		}
		return null;
	}
}
