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
public class Rule {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	private String label;

	private Long value;

	@ManyToMany(mappedBy = "rules", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Set<Ecosystem> ecosystems = new HashSet<Ecosystem>();

	@OneToMany(mappedBy = "rule", cascade = CascadeType.REMOVE, orphanRemoval = true)
	private Set<TraceIn> traceIns = new HashSet<TraceIn>();

	public Rule() {
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

	public Long getValue() {
		return value;
	}

	public void setValue(Long value) {
		this.value = value;
	}

	public Set<Ecosystem> getEcosystems() {
		return ecosystems;
	}

	public void setEcosystems(Set<Ecosystem> ecosystems) {
		this.ecosystems = ecosystems;
	}

	public Set<TraceIn> getTraceIns() {
		return traceIns;
	}

	public void setTraceIns(Set<TraceIn> traceIns) {
		this.traceIns = traceIns;
	}

	@Override
	public String toString() {
		return "Rule [id=" + id + ", name=" + name + ", label=" + label + ", value=" + value + ", ecosystems=" + ecosystems + ", traceIns=" + traceIns + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Rule other = (Rule) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
}
