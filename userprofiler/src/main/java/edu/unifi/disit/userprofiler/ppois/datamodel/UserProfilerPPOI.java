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
package edu.unifi.disit.userprofiler.ppois.datamodel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import edu.unifi.disit.commons.datamodel.PPOI;
import edu.unifi.disit.commons.datamodel.SMLocation;

@Entity
@Table(name = "users_ppoi")
public class UserProfilerPPOI {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	Long id;

	@Column(name = "userid")
	String userid;

	@Column(name = "ppoi")
	String ppoi;

	@Column(name = "latitude")
	Double latitude;

	@Column(name = "longitude")
	Double longitude;

	@Column(name = "accuracy")
	Float accuracy;

	@Column(name = "cpz")
	String cpz;

	@Column(name = "address")
	String address;

	@Column(name = "number")
	String number;

	@Column(name = "municipality")
	String municipality;

	@Column(name = "confirmed", columnDefinition = "TINYINT", length = 1)
	Boolean confirm;

	@Column(name = "label")
	String label;

	public UserProfilerPPOI() {
	}

	public UserProfilerPPOI(Long id, String userid, String ppoi, Double latitude, Double longitude, Float accuracy, String[] loc_data, Boolean confirm, String label) {
		this.id = id;
		this.userid = userid;
		this.ppoi = ppoi;
		this.latitude = latitude;
		this.longitude = longitude;
		this.accuracy = accuracy;
		this.cpz = loc_data[0];
		this.address = loc_data[1];
		this.municipality = loc_data[2];
		this.number = loc_data[3];
		this.confirm = confirm;
		this.label = label;
	}

	public UserProfilerPPOI(String userid, String ppoi, Double latitude, Double longitude, Float accuracy, SMLocation loc_data, Boolean confirm, String label) {
		this.userid = userid;
		this.ppoi = ppoi;
		this.latitude = latitude;
		this.longitude = longitude;
		this.accuracy = accuracy;
		this.cpz = loc_data.getCpz();
		this.address = loc_data.getAddress();
		this.municipality = loc_data.getMunicipality();
		this.number = loc_data.getNumber();
		this.confirm = confirm;
		this.label = label;
	}

	public UserProfilerPPOI(String deviceId, PPOI ppoi) {
		if (deviceId != null)
			this.userid = deviceId;

		if (ppoi.getId() != null)
			this.id = ppoi.getId();
		if (ppoi.getLatitude() != null)
			this.latitude = ppoi.getLatitude();
		if (ppoi.getLongitude() != null)
			this.longitude = ppoi.getLongitude();
		if (ppoi.getName() != null)
			this.ppoi = ppoi.getName();
		if (ppoi.getAccuracy() != null)
			this.accuracy = ppoi.getAccuracy();
		if (ppoi.getCpz() != null)
			this.cpz = ppoi.getCpz();
		if (ppoi.getAddress() != null)
			this.address = ppoi.getAddress();
		if (ppoi.getConfirmation() != null)
			this.confirm = ppoi.getConfirmation();
		if (ppoi.getMunicipality() != null)
			this.municipality = ppoi.getMunicipality();
		if (ppoi.getNumber() != null)
			this.number = ppoi.getNumber();
		if (ppoi.getLabel() != null)
			this.label = ppoi.getLabel();
	}

	public Long getUserProfilerPPOIId() {
		return id;
	}

	public void setUserProfilerPPOIId(Long id) {
		this.id = id;
	}

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public String getPpoi() {
		return ppoi;
	}

	public void setPpoi(String ppoi) {
		this.ppoi = ppoi;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
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

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public Boolean getConfirm() {
		return confirm;
	}

	public void setConfirm(Boolean confirm) {
		this.confirm = confirm;
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

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public String toString() {
		return "UserProfilerPPOI [id=" + id + ", userid=" + userid + ", ppoi=" + ppoi + ", latitude=" + latitude + ", longitude=" + longitude + ", accuracy=" + accuracy + ", cpz=" + cpz + ", address=" + address + ", number=" + number
				+ ", municipality=" + municipality + ", confirm=" + confirm + ", label=" + label + "]";
	}

	public PPOI toPPOI() {
		return new PPOI(this.id, this.latitude, this.longitude, this.ppoi, this.accuracy, this.cpz, this.address, this.confirm, this.municipality, this.number, this.label);
	}

	public void fromPPOI(PPOI ppoi) {
		if (ppoi.getId() != null)
			this.id = ppoi.getId();
		if (ppoi.getLatitude() != null)
			this.latitude = ppoi.getLatitude();
		if (ppoi.getLongitude() != null)
			this.longitude = ppoi.getLongitude();
		if (ppoi.getName() != null)
			this.ppoi = ppoi.getName();
		if (ppoi.getAccuracy() != null)
			this.accuracy = ppoi.getAccuracy();
		if (ppoi.getCpz() != null)
			this.cpz = ppoi.getCpz();
		if (ppoi.getAddress() != null)
			this.address = ppoi.getAddress();
		if (ppoi.getConfirmation() != null)
			this.confirm = ppoi.getConfirmation();
		if (ppoi.getMunicipality() != null)
			this.municipality = ppoi.getMunicipality();
		if (ppoi.getNumber() != null)
			this.number = ppoi.getNumber();
		if (ppoi.getLabel() != null)
			this.label = ppoi.getLabel();
	}
}
