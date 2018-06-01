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
package edu.unifi.disit.wallet_user_mngt.service;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import javax.naming.NamingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.google.api.Google;
import org.springframework.social.google.api.plus.Person;
import org.springframework.social.linkedin.api.LinkedIn;
import org.springframework.social.linkedin.api.LinkedInProfileFull;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.social.twitter.api.TwitterProfile;
import org.springframework.stereotype.Service;

import edu.unifi.disit.wallet_user_mngt.datamodel.Roletype;
import edu.unifi.disit.wallet_user_mngt.datamodel.Socialuser;
import edu.unifi.disit.wallet_user_mngt.datamodel.SocialuserDAO;
import edu.unifi.disit.wallet_user_mngt.datamodel.Socialusertype;
import edu.unifi.disit.wallet_user_mngt.datamodel.User;
import edu.unifi.disit.wallet_user_mngt.datamodel.UserDAO;
import edu.unifi.disit.wallet_user_mngt.exception.EmailExistsException;
import edu.unifi.disit.wallet_user_mngt.exception.SocialNotRecognizedException;

@Service
public class SocialServiceImpl implements ISocialService {

	private static final Logger logger = LogManager.getLogger();

	@Autowired
	private IUserService userservice;

	@Autowired
	private SecurityServiceImpl securityService;

	@Autowired
	private UserDAO userDAO;

	@Autowired
	private SocialuserDAO socialUserDAO;

	@Autowired
	private MessageSource messages;

	@Override
	public String registerOrLogin(Object o, Socialusertype socialusertype, Locale lang) throws SocialNotRecognizedException {
		String userId = null;
		String email = null;
		Object socialuser = null;
		switch (socialusertype) {
		case FACEBOOK:
			String[] fields = { "id", "email", "first_name", "last_name", "name", "age_range", "currency", "gender", "cover" };
			socialuser = ((Facebook) o).fetchObject("me", org.springframework.social.facebook.api.User.class, fields);
			userId = ((org.springframework.social.facebook.api.User) socialuser).getId();
			email = ((org.springframework.social.facebook.api.User) socialuser).getEmail();
			break;
		case GOOGLE:
			socialuser = ((Google) o).plusOperations().getGoogleProfile();
			userId = ((Person) socialuser).getId();
			email = ((Person) socialuser).getAccountEmail();
			break;
		case LINKEDIN:
			socialuser = ((LinkedIn) o).profileOperations().getUserProfileFull();
			userId = ((LinkedInProfileFull) socialuser).getId();
			email = ((LinkedInProfileFull) socialuser).getEmailAddress();
			break;
		case TWITTER:
			socialuser = ((Twitter) o).userOperations().getUserProfile();
			userId = String.valueOf(((TwitterProfile) socialuser).getId());
			// twitter email is available if you configure properly the app in https://apps.twitter.com/app/13159783/permissions
			// moreover there is a bug in spring-social-twitter that need to be hacked https://github.com/spring-projects/spring-social-twitter/issues/97
			// so let's do later
			break;
		case LDAP:// TODO
			DirContextAdapter ctx = (DirContextAdapter) o;
			try {
				userId = (String) ctx.getAttributes().get("cn").get();
				email = (String) ctx.getAttributes().get("mail").get();
			} catch (NamingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			socialuser = ctx;
			break;
		default:
			throw new SocialNotRecognizedException(o.getClass().toString() + " is not a valid social");
		}
		if (email == null)
			throw new SocialNotRecognizedException("loginSocial.emailNotSpecified");
		else if (userId == null)
			throw new SocialNotRecognizedException("username is null");
		User user = userDAO.findByUsername(email);
		if (user == null) {
			// new user
			Socialuser sc = new Socialuser(socialusertype, socialuser);
			User myuser = new edu.unifi.disit.wallet_user_mngt.datamodel.User();
			myuser.setUsername(email);
			myuser.setPassword(UUID.randomUUID().toString());
			User registered = null;
			try {
				registered = userservice.registerNewUserAccount(myuser, lang, Roletype.ROLE_USER);
			} catch (EmailExistsException e) {
				return "redirect:/user?error=KO";
			}
			// basic role is ROLE_USER
			// registered.setRoletype(Roletype.ROLE_USER);
			// registered.setRegistrationDate(new Date());
			// userservice.saveRegisteredUser(registered);
			sc.setUser(myuser);
			socialUserDAO.save(sc);
			securityService.autologin(registered.getUsername(), registered.getPassword());
			// always redirect to hello-user for new subscription
			return "redirect:/user?successregistration=" + messages.getMessage("registration.ok", null, LocaleContextHolder.getLocale());
		} else {
			// old user
			Socialuser sc = findSocial(user, socialusertype);
			if (sc == null) {
				// new social user
				sc = new Socialuser(socialusertype, socialuser);
				sc.setUser(user);
				socialUserDAO.save(sc);
				userservice.saveRegisteredUser(user);
				securityService.autologin(user.getUsername(), user.getPassword());
				return "redirect:" + securityService.determineHomeUrl() + "?successregistration=" + messages.getMessage("registration.ok.merged", null, LocaleContextHolder.getLocale());
			} else {
				// old social user
				securityService.autologin(user.getUsername(), user.getPassword());
				return "redirect:/" + securityService.determineHomeUrl() + "?successregistration=" + messages.getMessage("login.welcome", new String[] { user.getUsername() }, LocaleContextHolder.getLocale());
			}
		}
	}

	private Socialuser findSocial(User user, Socialusertype socialusertype) {
		Set<Socialuser> sc = socialUserDAO.findByUserAndSocialusertype(user, socialusertype);
		if (sc.size() == 0)
			return null;
		else if (sc.size() > 1)
			logger.warn("warning there are more than one user sharing the same social id for:{}, user:{}", socialusertype.toString(), user.getId());
		return sc.iterator().next();
	}

	@Override
	public Set<String> getConnectedSocialLoggedUser() {
		Set<String> toreturn = new HashSet<String>();
		for (Socialuser su : socialUserDAO.findByUser(securityService.findLoggedInUser())) {
			toreturn.add(su.getSocialusertype().toString());
		}
		return toreturn;
	}

	@Override
	public String registerOrLogin(Object o, Locale lang) throws SocialNotRecognizedException {
		if (o instanceof Facebook)
			return registerOrLogin(o, Socialusertype.FACEBOOK, lang);
		else if (o instanceof Google)
			return registerOrLogin(o, Socialusertype.GOOGLE, lang);
		else if (o instanceof LinkedIn)
			return registerOrLogin(o, Socialusertype.LINKEDIN, lang);
		else if (o instanceof Twitter)
			return registerOrLogin(o, Socialusertype.TWITTER, lang);
		else
			throw new SocialNotRecognizedException(o.getClass().toString() + " is not a valid social");
	}
}
