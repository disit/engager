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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "properties")
public class Properties {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	Long id;

	@Column(name = "properties_name")
	String properties_name;

	@Column(name = "properties_value")
	String properties_value;

	@Column(name = "properties_desc")
	String properties_desc;

	public Properties() {
	}

	public Properties(Long id, String properties_name, String properties_value, String properties_desc) {
		this.id = id;
		this.properties_name = properties_name;
		this.properties_value = properties_value;
		this.properties_desc = properties_desc;
	}

	public Properties(String properties_name, String properties_value, String properties_desc) {
		this.properties_name = properties_name;
		this.properties_value = properties_value;
		this.properties_desc = properties_desc;
	}

	public String getProperties_name() {
		return properties_name;
	}

	public void setProperties_name(String properties_name) {
		this.properties_name = properties_name;
	}

	public String getProperties_value() {
		return properties_value;
	}

	public void setProperties_value(String properties_value) {
		this.properties_value = properties_value;
	}

	public String getProperties_desc() {
		return properties_desc;
	}

	public void setProperties_desc(String properties_desc) {
		this.properties_desc = properties_desc;
	}

	@Override
	public String toString() {
		return "Properties [id=" + id + ", properties_name=" + properties_name + ", properties_value=" + properties_value + ", properties_desc=" + properties_desc + "]";
	}
}
