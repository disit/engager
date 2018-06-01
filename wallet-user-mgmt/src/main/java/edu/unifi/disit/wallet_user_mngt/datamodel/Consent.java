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
public class Consent {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	private DatasetType dataset;

	private Boolean consent;

	@JsonIgnore
	@ManyToOne
	private User user;

	public Consent(Boolean consent, DatasetType dataset, User user) {
		this.consent = consent;
		this.dataset = dataset;
		this.user = user;
	}

	public Consent(DatasetType dataset) {
		this(true, dataset, null);
	}

	public Consent(DatasetType dataset, User user) {
		this(true, dataset, user);
	}

	public Consent() {
		this(true, null, null);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public DatasetType getDataset() {
		return dataset;
	}

	public void setDataset(DatasetType dataset) {
		this.dataset = dataset;
	}

	public Boolean getConsent() {
		return consent;
	}

	public void setConsent(Boolean consent) {
		this.consent = consent;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@Override
	public String toString() {
		return "Consent [id=" + id + ", dataset=" + dataset + ", consent=" + consent + ", userid=" + user.getUsername() + "]";
	}

}
