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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

@Entity
public class Ecosystem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	@OneToMany(mappedBy = "ecosystem", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<Campaign> campaign = new HashSet<Campaign>();

	@ManyToMany(cascade = { CascadeType.REFRESH })
	private Set<Rule> rules = new HashSet<Rule>();

	@ManyToMany(mappedBy = "ecosystems", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Set<User> users = new HashSet<User>();

	public Ecosystem() {
		super();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<Rule> getRules() {
		return rules;
	}

	public void setRules(Set<Rule> rules) {
		this.rules = rules;
	}

	// public void addRule(Rule rule) {
	// this.rules.add(rule);
	// rule.getEcosystems().add(this);
	// }
	//
	// public void removeRule(Rule rule) {
	// this.rules.remove(rule);
	// rule.getEcosystems().remove(this);
	// }

	public Set<User> getUsers() {
		return users;
	}

	public void setUsers(Set<User> users) {
		this.users = users;
	}

	public Set<Campaign> getCampaign() {
		return campaign;
	}

	public void setCampaign(Set<Campaign> campaign) {
		this.campaign = campaign;
	}

	@Override
	public String toString() {
		return "Ecosystem [id=" + id + ", name=" + name + "]";// , campaign=" + campaign + ", rules=" + rules + ", users=" + users + "]";
	}
}
