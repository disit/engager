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

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

@Entity
// @IValidPassword
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String password;

	@Transient
	private String passwordConfirm;

	private String username;

	private String nickname;

	private Date registrationDate;

	private Date lastLogin;

	@Enumerated(EnumType.ORDINAL)
	private Roletype roletype;

	@ManyToMany // (cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Set<Ecosystem> ecosystems = new HashSet<Ecosystem>();

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
	private Set<Device> devices = new HashSet<Device>();

	@OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
	private Set<TraceIn> traceIns = new HashSet<TraceIn>();

	@OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
	private Set<TraceOut> traceOuts = new HashSet<TraceOut>();

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<Campaign> campaigns = new HashSet<Campaign>();

	// default is FetchType.EAGER
	@OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private VerificationToken verificationToken;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<Socialuser> socialuser = new HashSet<Socialuser>();

	private Boolean visible = true;

	@OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, fetch = FetchType.EAGER, orphanRemoval = true)
	Set<Consent> consents = null;

	public User() {
		super();
	}

	public User(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Roletype getRoletype() {
		return roletype;
	}

	public void setRoletype(Roletype roletype) {
		this.roletype = roletype;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPasswordConfirm() {
		return passwordConfirm;
	}

	public void setPasswordConfirm(String passwordConfirm) {
		this.passwordConfirm = passwordConfirm;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Date getRegistrationDate() {
		return registrationDate;
	}

	public void setRegistrationDate(Date registrationDate) {
		this.registrationDate = registrationDate;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public Set<Ecosystem> getEcosystems() {
		return this.ecosystems;
	}

	public void setEcosystems(Set<Ecosystem> ecosystems) {
		this.ecosystems = ecosystems;
	}

	// public void addEcosystem(Ecosystem ecosystem) {
	// this.ecosystems.add(ecosystem);
	// ecosystem.getUsers().add(this);
	// }
	//
	// public void removeEcosystem(Ecosystem ecosystem) {
	// this.ecosystems.remove(ecosystem);
	// ecosystem.getUsers().remove(this);
	// }

	public Set<TraceIn> getTraceIns() {
		return traceIns;
	}

	public void setTraceIns(Set<TraceIn> traceIns) {
		this.traceIns = traceIns;
	}

	public Set<TraceOut> getTraceOuts() {
		return traceOuts;
	}

	public void setTraceOuts(Set<TraceOut> traceOuts) {
		this.traceOuts = traceOuts;
	}

	public Boolean getVisible() {
		return visible;
	}

	public void setVisible(Boolean visible) {
		this.visible = visible;
	}

	public Set<Socialuser> getSocialuser() {
		return socialuser;
	}

	public void setSocialuser(Set<Socialuser> socialuser) {
		this.socialuser = socialuser;
	}

	public void addSocialuser(Socialuser sc) {
		this.socialuser.add(sc);
	}

	public VerificationToken getVerificationToken() {
		return verificationToken;
	}

	public void setVerificationToken(VerificationToken verificationToken) {
		this.verificationToken = verificationToken;
	}

	public Set<Campaign> getCampaigns() {
		return campaigns;
	}

	public void setCampaigns(Set<Campaign> campaigns) {
		this.campaigns = campaigns;
	}

	public Set<Device> getDevices() {
		return devices;
	}

	public void setDevices(Set<Device> devices) {
		this.devices = devices;
	}

	public Date getLastLogin() {
		return lastLogin;
	}

	public void setLastLogin(Date lastLogin) {
		this.lastLogin = lastLogin;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final int maxLen = 10;
		return "User [id=" + id + ", password=" + password
				+ ", passwordConfirm=" + passwordConfirm
				+ ", username=" + username
				+ ", nickname=" + nickname
				+ ", registrationDate=" + registrationDate
				+ ", lastLogin=" + lastLogin
				+ ", roletype=" + roletype
				// + ", ecosystems=" + (ecosystems != null ? toString(ecosystems, maxLen) : null)
				+ ", devices=" + (devices != null ? toString(devices, maxLen) : null)
				// + ", traceIns=" + (traceIns != null ? toString(traceIns, maxLen) : null)
				// + ", traceOuts=" + (traceOuts != null ? toString(traceOuts, maxLen) : null)
				// + ", campaigns=" + (campaigns != null ? toString(campaigns, maxLen) : null)
				+ ", verificationToken=" + verificationToken
				// + ", socialuser=" + (socialuser != null ? toString(socialuser, maxLen) : null)
				+ ", visible=" + visible + "]";
	}

	private String toString(Collection<?> collection, int maxLen) {
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		int i = 0;
		for (Iterator<?> iterator = collection.iterator(); iterator.hasNext() && i < maxLen; i++) {
			if (i > 0)
				builder.append(", ");
			builder.append(iterator.next());
		}
		builder.append("]");
		return builder.toString();
	}
}
