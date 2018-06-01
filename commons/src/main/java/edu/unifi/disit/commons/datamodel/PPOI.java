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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import edu.unifi.disit.commons.datamodel.Position;

@JsonSerialize(using = PPOISerializer.class)
@JsonDeserialize(using = PPOIDeserializer.class)
public class PPOI extends Position implements Comparable<PPOI> {

	Long id;

	Float accuracy;
	String cpz;
	String address;
	String municipality;
	String number;
	Boolean confirmation;

	String label;

	public PPOI() {
		super();
	}

	public PPOI(Long id, Double latitude, Double longitude, String name, Float accuracy, String cpz, String address, Boolean confirmation, String municipality, String number, String label) {
		super(latitude, longitude, name);
		this.id = id;
		this.accuracy = accuracy;
		this.cpz = cpz;
		this.address = address;
		this.confirmation = confirmation;
		this.municipality = municipality;
		this.number = number;
		this.label = label;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public Float getAccuracy() {
		return accuracy;
	}

	public void setAccuracy(Float accuracy) {
		this.accuracy = accuracy;
	}

	public String getCpz() {
		return cpz;
	}

	public void setCpz(String cpz) {
		this.cpz = cpz;
	}

	public Boolean getConfirmation() {
		return confirmation;
	}

	public void setConfirmation(Boolean confirmation) {
		this.confirmation = confirmation;
	}

	public String getMunicipality() {
		return municipality;
	}

	public void setMunicipality(String municipality) {
		this.municipality = municipality;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "PPOI [id=" + id + ", accuracy=" + accuracy + ", cpz=" + cpz + ", address=" + address + ", municipality=" + municipality + ", number=" + number + ", confirmation=" + confirmation + ", label=" + label + "]";
	}

	public int compareTo(PPOI o) {
		return (Float.compare(o.getAccuracy(), this.accuracy));
	}

	public void setLocationData(SMLocation loc_data) {
		// String[] loc_data = { ppoi.getCpz(), ppoi.getAddress(), ppoi.getMunicipality(), ppoi.getNumber() };
		// TODO update(user_id, ppoi.getName(), ppoi.getLatitude(), ppoi.getLongitude(), ppoi.getAccuracy(), loc_data, ppoi.getConfirmation());
		this.cpz = loc_data.getCpz();
		this.address = loc_data.getAddress();
		this.municipality = loc_data.getMunicipality();
		this.number = loc_data.getNumber();
	}

}
