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

import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class SMLocation {

	protected String address;
	protected String number;
	protected String municipality;
	protected String province;
	protected String cpz;

	public SMLocation() {
		super();
	}

	public SMLocation(String address, String number, String municipality, String province, String cpz) {
		super();
		this.address = address;
		this.number = number;
		this.municipality = municipality;
		this.province = province;
		this.cpz = cpz;
	}

	public SMLocation(SMLocation sml) {
		this.address = sml.address;
		this.number = sml.number;
		this.municipality = sml.municipality;
		this.province = sml.province;
		this.cpz = sml.cpz;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getMunicipality() {
		return municipality;
	}

	public void setMunicipality(String municipality) {
		this.municipality = municipality;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getCpz() {
		return cpz;
	}

	public void setCpz(String cpz) {
		this.cpz = cpz;
	}

	@Override
	public String toString() {
		return "Location [address=" + address + ", number=" + number + ", municipality=" + municipality + ", province=" + province + ", cpz=" + cpz + "]";
	}

	// is valid if at least one info is there
	public boolean valid() {
		if ((this.cpz != null) || (this.address != null) || (this.number != null) || (this.municipality != null) || (this.province != null))
			return true;
		return false;
	}

}
