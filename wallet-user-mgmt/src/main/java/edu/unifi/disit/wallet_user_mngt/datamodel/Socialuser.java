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

import java.util.Date;
import java.util.Locale;

import javax.naming.NamingException;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.social.google.api.plus.Person;
import org.springframework.social.linkedin.api.LinkedInProfileFull;
import org.springframework.social.twitter.api.TwitterProfile;

@Entity
@Table(name = "socialuser")
public class Socialuser {

	private static final Logger logger = LogManager.getLogger();

	public enum Gender {
		MALE, FEMALE
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.ORDINAL)
	public Socialusertype socialusertype;

	public String socialid;

	public String coveruri;

	public String firstname;

	public String lastname;

	public String agerange;

	public Date birthdate;

	public String language;

	@Enumerated(EnumType.ORDINAL)
	public Gender gender;

	public String homelocation;

	@ManyToOne
	private User user;

	public Socialuser() {
		super();
	}

	public Socialuser(Socialusertype socialusertype) {
		this.socialusertype = socialusertype;
	}

	public Socialuser(Socialusertype socialusertype, Object socialuser) {
		switch (socialusertype) {
		case FACEBOOK:
			this.fromFacebook(socialuser);
			break;
		case GOOGLE:
			this.fromGoogle(socialuser);
			break;
		case TWITTER:
			this.fromTwitter(socialuser);
			break;
		case LINKEDIN:
			this.fromLinkedIn(socialuser);
			break;
		case LDAP:
			this.fromLDAP(socialuser);
			break;
		default:
			logger.error("Received an invalid social user:{}", socialusertype.toString());
			break;
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Socialusertype getSocialusertype() {
		return socialusertype;
	}

	public void setSocialusertype(Socialusertype socialusertype) {
		this.socialusertype = socialusertype;
	}

	public String getSocialid() {
		return socialid;
	}

	public void setSocialid(String socialid) {
		this.socialid = socialid;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getCoveruri() {
		return coveruri;
	}

	public void setCoveruri(String coveruri) {
		this.coveruri = coveruri;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getAgerange() {
		return agerange;
	}

	public void setAgerange(String agerange) {
		this.agerange = agerange;
	}

	public Date getBirthdate() {
		return birthdate;
	}

	public void setBirthdate(Date birthdate) {
		this.birthdate = birthdate;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguages(String language) {
		this.language = language;
	}

	public Gender getGender() {
		return gender;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}

	public String getHomelocation() {
		return homelocation;
	}

	public void setHomelocation(String homelocation) {
		this.homelocation = homelocation;
	}

	public void fromLinkedIn(Object socialuser) {
		LinkedInProfileFull linkedinuser = (LinkedInProfileFull) socialuser;
		this.socialusertype = Socialusertype.LINKEDIN;
		this.socialid = linkedinuser.getId();
		this.firstname = linkedinuser.getFirstName();
		this.lastname = linkedinuser.getLastName();
		this.homelocation = (linkedinuser.getLocation().getName() != null) ? linkedinuser.getLocation().getName() : linkedinuser.getLocation().getName();
		this.coveruri = linkedinuser.getProfilePictureUrl();
	}

	public void fromFacebook(Object socialuser) {
		org.springframework.social.facebook.api.User facebookuser = (org.springframework.social.facebook.api.User) socialuser;
		this.socialusertype = Socialusertype.FACEBOOK;
		this.socialid = facebookuser.getId();
		this.firstname = facebookuser.getFirstName();
		this.lastname = facebookuser.getLastName();
		this.coveruri = (facebookuser.getCover() != null) ? facebookuser.getCover().getSource() : null;
		this.agerange = (facebookuser.getAgeRange() != null) ? facebookuser.getAgeRange().name() : null;
		this.gender = (facebookuser.getGender().equals("male")) ? Gender.MALE : Gender.FEMALE;
	}

	public void fromTwitter(Object socialuser) {
		TwitterProfile twitteruser = (TwitterProfile) socialuser;
		this.socialusertype = Socialusertype.TWITTER;
		this.socialid = String.valueOf(twitteruser.getId());

		String newlanguage = convertInLocale(twitteruser.getLanguage(), this.socialid + "-twitter");// insert the language just if we can recognize in convertInLocale
		if (newlanguage != null)
			this.language = newlanguage;

		this.coveruri = twitteruser.getProfileImageUrl();
	}

	public void fromGoogle(Object socialuser) {
		Person person = (Person) socialuser;
		this.socialusertype = Socialusertype.GOOGLE;
		this.socialid = String.valueOf(person.getId());
		this.firstname = person.getGivenName();
		this.lastname = person.getFamilyName();
		this.coveruri = person.getImageUrl();
		if (person.getGender() != null)
			this.gender = (person.getGender().equals("male")) ? Gender.MALE : Gender.FEMALE;
		this.birthdate = person.getBirthday();

		String newlanguage = convertInLocale(person.getLanguage(), this.socialid + "-google");// insert the language just if we can recognize in convertInLocale
		if (newlanguage != null)
			this.language = newlanguage;
	}

	private void fromLDAP(Object socialuser) {
		DirContextAdapter ctx = (DirContextAdapter) socialuser;
		this.socialusertype = Socialusertype.LDAP;
		try {
			this.socialid = (String) ctx.getAttributes().get("cn").get();
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		return "Socialuser [id=" + id + ", socialsource=" + socialusertype.toString() + ", socialid=" + socialid + ", coveruri=" + coveruri + ", firstname=" + firstname + ", lastname=" + lastname + ", agerange=" + agerange + ", birthdate="
				+ birthdate
				+ ", language=" + language + ", gender=" + gender + ", homelocation=" + homelocation + ", user=" + user + "]";
	}

	private String convertInLocale(String language, String usernameAndSocial) {

		Locale l = new Locale(language);
		if (l.toString() != null) {
			return l.toString();
		} else {
			logger.warn("No valid language code for user:{} was ({}) ... set default: italian", usernameAndSocial, language);
			return null;
		}
	}
}
