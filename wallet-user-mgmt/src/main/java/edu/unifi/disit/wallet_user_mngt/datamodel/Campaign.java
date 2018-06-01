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

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import edu.unifi.disit.wallet_user_mngt.object.CampaignStatus;

@Entity
public class Campaign {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	private Long rate;

	private Long howmany;

	private Date startDate;

	private Date endDate;

	@ManyToOne
	private Ecosystem ecosystem;

	@ManyToOne
	private User user;

	@OneToMany(mappedBy = "campaign", cascade = CascadeType.REMOVE, orphanRemoval = true)
	private Set<TraceIn> traceIns = new HashSet<TraceIn>();

	@OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<Prize> prizes = new HashSet<Prize>();

	public Campaign() {
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

	public Long getRate() {
		return rate;
	}

	public void setRate(Long rate) {
		this.rate = rate;
	}

	public Long getHowmany() {
		return howmany;
	}

	public void setHowmany(Long howmany) {
		this.howmany = howmany;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Ecosystem getEcosystem() {
		return ecosystem;
	}

	public void setEcosystem(Ecosystem ecosystem) {
		this.ecosystem = ecosystem;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Set<TraceIn> getTraceIns() {
		return traceIns;
	}

	public void setTraceIns(Set<TraceIn> traceIns) {
		this.traceIns = traceIns;
	}

	public Set<Prize> getPrizes() {
		return prizes;
	}

	public void setPrizes(Set<Prize> prize) {
		this.prizes = prize;
	}

	@Override
	public String toString() {
		return "Campaign [id=" + id + ", name=" + name + " , rate=" + rate + ", howmany=" + howmany + ", startDate=" + startDate + ", endDate=" + endDate + "]";// ecosystem=" + ecosystem + ", user=" + user + ", traceIns=" + traceIns + ",
																																								// prize=" + prizes + "]";
	}

	public CampaignStatus getCampaignStatus() {
		CampaignStatus cs = new CampaignStatus();
		Date nowDate = new Date();
		if (this.getStartDate().after(nowDate)) {
			cs.setStatus(CampaignStatustype.CAMPAIGNSTATUS_WAITING);
			cs.setProgress(1 + new Double(Math.floor((this.getStartDate().getTime() - System.currentTimeMillis()) / (24 * 60 * 60 * 1000))).intValue());
		}
		// campaign status using howmany
		// else if ((this.getStartDate().getTime() + this.getHowmany() * 24 * 60 * 60 * 1000) > System.currentTimeMillis()) {
		// cs.setStatus(CampaignStatustype.CAMPAIGNSTATUS_ACTIVE);
		// cs.setProgress(this.getHowmany().intValue() - new Double(Math.floor((this.getStartDate().getTime() + this.getHowmany() * 24 * 60 * 60 * 1000 - System.currentTimeMillis()) / (24 * 60 * 60 * 1000))).intValue());
		// cs.setTotal(this.getHowmany().intValue());
		// }
		// campaign status using endDate
		else if (this.getStartDate().before(nowDate) && this.getEndDate().after(nowDate)) {
			cs.setStatus(CampaignStatustype.CAMPAIGNSTATUS_ACTIVE);
			Calendar c = Calendar.getInstance();
			c.setTime(startDate);
			Integer start = c.get(Calendar.DAY_OF_YEAR);
			c.setTime(nowDate);
			Integer now = c.get(Calendar.DAY_OF_YEAR);
			c.setTime(endDate);
			Integer end = c.get(Calendar.DAY_OF_YEAR);
			// cs.setProgress(this.getHowmany().intValue() - new Double(Math.floor((this.getStartDate().getTime() + this.getHowmany() * 24 * 60 * 60 * 1000 - System.currentTimeMillis()) / (24 * 60 * 60 * 1000))).intValue());
			// cs.setTotal(this.getHowmany().intValue());
			cs.setProgress(now - start);
			cs.setTotal(end - start);
		}

		else
			cs.setStatus(CampaignStatustype.CAMPAIGNSTATUS_TERMINATED);
		return cs;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
}
