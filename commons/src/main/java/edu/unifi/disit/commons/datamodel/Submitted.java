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

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonDeserialize(using = SubmittedDeserializer.class)
@JsonSerialize(using = SubmittedSerializer.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Submitted {

	private Date time;

	private String serviceUri;

	private String text;

	private String serviceUriName;
	private String serviceUriServiceType;

	// constructor with
	// data from
	// one object 0-time 1-serviceuri 2-text

	public Submitted(Object[] o) {
		this((Date) o[0], (String) o[1], (String) o[2], null, null);
	}

	// contructor with data from two object
	// 0-time (ACCESSLOG)
	// 1-serviceuri (ACCESSLOG)
	// + text fromExternalTable
	public Submitted(Object[] fromaccesslog, String fromexternalTable) {
		this((Date) fromaccesslog[0], (String) fromaccesslog[1], fromexternalTable, null, null);
	}

	public Submitted(Date time, String serviceUri, String text, String serviceUriName, String serviceUriServiceType) {
		this.time = time;
		this.serviceUri = serviceUri;
		this.text = text;
		this.serviceUriName = serviceUriName;
		this.serviceUriServiceType = serviceUriServiceType;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public String getServiceUri() {
		return serviceUri;
	}

	public void setServiceUri(String serviceUri) {
		this.serviceUri = serviceUri;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getServiceUriName() {
		return serviceUriName;
	}

	public void setServiceUriName(String serviceUriName) {
		this.serviceUriName = serviceUriName;
	}

	public String getServiceUriServiceType() {
		return serviceUriServiceType;
	}

	public void setServiceUriServiceType(String serviceUriServiceType) {
		this.serviceUriServiceType = serviceUriServiceType;
	}

	@Override
	public String toString() {
		return "Submitted [time=" + time + ", serviceUri=" + serviceUri + ", text=" + text + ", serviceUriName=" + serviceUriName + ", serviceUriServiceType=" + serviceUriServiceType + "]";
	}
}
