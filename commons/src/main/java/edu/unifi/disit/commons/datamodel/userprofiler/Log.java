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
package edu.unifi.disit.commons.datamodel.userprofiler;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import edu.unifi.disit.commons.datamodel.DatasetType;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity
public class Log {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;

	Date dataTime;

	Date insertTime;

	Date elapseTime;

	Date deleteTime;

	@Enumerated(EnumType.STRING)
	private DatasetType dataset;

	String valueType;

	String value;

	@JsonIgnore
	@ManyToOne
	private Device device;

	public Log() {
		super();
	}

	public Log(Date dataTime, Date insertTime, Date elapseTime, Date deleteTime, DatasetType dataset, String valueType, String value, Device device) {
		super();
		this.dataTime = dataTime;
		this.insertTime = insertTime;
		this.elapseTime = elapseTime;
		this.deleteTime = deleteTime;
		this.dataset = dataset;
		this.valueType = valueType;
		this.value = value;
		this.device = device;
	}

	public Log(Long id, Date dataTime, Date insertTime, Date elapseTime, Date deleteTime, DatasetType dataset, String valueType, String value, Device device) {
		super();
		this.id = id;
		this.dataTime = dataTime;
		this.insertTime = insertTime;
		this.elapseTime = elapseTime;
		this.deleteTime = deleteTime;
		this.dataset = dataset;
		this.valueType = valueType;
		this.value = value;
		this.device = device;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getDataTime() {
		return dataTime;
	}

	public void setDataTime(Date dataTime) {
		this.dataTime = dataTime;
	}

	public Date getInsertTime() {
		return insertTime;
	}

	public void setInsertTime(Date insertTime) {
		this.insertTime = insertTime;
	}

	public Date getElapseTime() {
		return elapseTime;
	}

	public void setElapseTime(Date elapseTime) {
		this.elapseTime = elapseTime;
	}

	public Date getDeleteTime() {
		return deleteTime;
	}

	public void setDeleteTime(Date deleteTime) {
		this.deleteTime = deleteTime;
	}

	public DatasetType getDataset() {
		return dataset;
	}

	public void setDataset(DatasetType dataset) {
		this.dataset = dataset;
	}

	public String getValueType() {
		return valueType;
	}

	public void setValueType(String valueType) {
		this.valueType = valueType;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Device getDevice() {
		return device;
	}

	public void setDevice(Device device) {
		this.device = device;
	}

	@Override
	public String toString() {
		return "Log [id=" + id + ", dataTime=" + dataTime + ", insertTime=" + insertTime + ", elapseTime=" + elapseTime + ", deleteTime=" + deleteTime + ", dataset=" + dataset + ", valueType=" + valueType + ", value=" + value + ", device="
				+ device + "]";
	}
}
