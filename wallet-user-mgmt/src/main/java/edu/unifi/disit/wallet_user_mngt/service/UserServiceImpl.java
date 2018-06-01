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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.unifi.disit.wallet_user_mngt.datamodel.Device;
import edu.unifi.disit.wallet_user_mngt.datamodel.DeviceDAO;
import edu.unifi.disit.wallet_user_mngt.datamodel.EcosystemDAO;
import edu.unifi.disit.wallet_user_mngt.datamodel.PasswordResetToken;
import edu.unifi.disit.wallet_user_mngt.datamodel.PasswordResetTokenDAO;
import edu.unifi.disit.wallet_user_mngt.datamodel.Prize;
import edu.unifi.disit.wallet_user_mngt.datamodel.PrizeDAO;
import edu.unifi.disit.wallet_user_mngt.datamodel.Roletype;
import edu.unifi.disit.wallet_user_mngt.datamodel.TraceInDAO;
import edu.unifi.disit.wallet_user_mngt.datamodel.TraceOutDAO;
import edu.unifi.disit.wallet_user_mngt.datamodel.User;
import edu.unifi.disit.wallet_user_mngt.datamodel.UserDAO;
import edu.unifi.disit.wallet_user_mngt.datamodel.VerificationToken;
import edu.unifi.disit.wallet_user_mngt.datamodel.VerificationTokenDAO;
import edu.unifi.disit.wallet_user_mngt.event.OnPreparationEmailCompleteEvent;
import edu.unifi.disit.wallet_user_mngt.exception.EmailExistsException;

@Service
public class UserServiceImpl implements IUserService {

	@Value("${application.url}")
	private String myappuri;

	@Autowired
	private TraceInDAO traceInDAO;

	@Autowired
	private TraceOutDAO traceOutDAO;

	@Autowired
	private UserDAO userDAO;

	@Autowired
	private DeviceDAO deviceDAO;

	@Autowired
	private PrizeDAO prizeDAO;

	@Autowired
	private EcosystemDAO ecosystemDAO;

	@Autowired
	private VerificationTokenDAO verificationTokenDAO;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private ISecurityService securityService;

	@Autowired
	private PasswordResetTokenDAO passwordResetTokenDAO;

	@Autowired
	private MessageSource messages;

	@Transactional
	@Override
	public User registerNewUserAccount(User user, Locale lang, Roletype role) throws EmailExistsException {
		if (emailExist(user.getUsername()))
			throw new EmailExistsException(messages.getMessage("registration.ko.emailalreadytaken", new Object[] { user.getUsername() }, lang));
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		// basic role is ROLE_USERNOTENABLED
		user.setRoletype(role);
		user.setRegistrationDate(new Date());
		userDAO.save(user);
		return user;
	}

	@Override
	public User getUser(String verificationToken) {
		User user = verificationTokenDAO.findByToken(verificationToken).getUser();
		return user;
	}

	@Override
	public VerificationToken getVerificationToken(String VerificationToken) {
		return verificationTokenDAO.findByToken(VerificationToken);
	}

	@Override
	public void saveRegisteredUser(User user) {
		userDAO.save(user);
	}

	@Override
	public void createVerificationToken(User user, String token) {
		VerificationToken myToken = new VerificationToken(token, user);
		verificationTokenDAO.save(myToken);
	}

	@Override
	public Integer getTotalPointForLoggedUsers() {
		Integer toreturn = traceInDAO.countTotalPointForAllUsers(securityService.findLoggedInUsername());
		return (toreturn != null) ? toreturn : 0;
	}

	@Override
	public Page<Object[]> getTotalPointForAllUsers(PageRequest pr) {
		return traceInDAO.countTotalPointForAllUsers(pr);
	}

	@Override
	public Page<Object[]> getTotalPointForAllUsersVisible(PageRequest pr) {
		return traceInDAO.countTotalPointForAllUsersVisible(pr);
	}

	@Override
	public Page<Object[]> getCurrentPointForEcosystemForLoggedUser(PageRequest pageRequest) {
		return traceInDAO.countPointForEcosystemForLoggedUser(securityService.findLoggedInUsername(), pageRequest);
		// TODO remove traceout from tracein to retrieve the current situation
	}

	@Override
	public Page<Object[]> getAllPointForLoggedUser(PageRequest pageRequest) {
		return traceInDAO.countAllPointForLoggedUser(securityService.findLoggedInUsername(), pageRequest);
	}

	private boolean emailExist(String email) {
		User user = userDAO.findByUsername(email);
		if (user != null) {
			return true;
		}
		return false;
	}

	@Override
	public String getNameOfEcosystem(Long id) {
		return ecosystemDAO.findById(id).getName();
	}

	@Override
	public Long getCurrentPointEcosystemForLoggedUser(Long id) {
		return traceInDAO.countPointForEcosystem(securityService.findLoggedInUsername(), id);
		// - traceOutDAO.countPointForEcosystem(securityService.findLoggedInUsername(), id);//TODO remove traceout from tracein to retrieve the current situation
	}

	@Override
	public Page<Object[]> getPrizesForEcosystem(Long id, PageRequest pageRequest) {
		Page<Object[]> p = prizeDAO.findAllByEcosystem(id, pageRequest);
		return p;
	}

	@Override
	public Page<Object[]> findAllTraceOutForLoggedUserForEcosystem(Long id, PageRequest pageRequest) {
		return traceOutDAO.findAllTraceOutByUsernameForEcosytem(securityService.findLoggedInUsername(), id, pageRequest);
	}

	// TESTING_EXTERNAL_APIS
	@Override
	public List<Prize> getAllPrizeByEcosystem(Long id) {
		return prizeDAO.findAllByEcosystem(id);
	}

	@Override
	public void deleteLoggedUser() {

		List<PasswordResetToken> prts = passwordResetTokenDAO.findByUserId(securityService.findLoggedInUser().getId());
		for (PasswordResetToken prt : prts) {
			passwordResetTokenDAO.delete(prt.getId());
		}
		userDAO.delete(securityService.findLoggedInUser());
	}

	@Override
	public void createPasswordResetTokenForUser(User user, String token) {
		PasswordResetToken prt = new PasswordResetToken(user, token);
		passwordResetTokenDAO.save(prt);
	}

	@Override
	public void changePasswordForLogged(String password) {
		User user = securityService.findLoggedInUser();
		user.setPassword(passwordEncoder.encode(password));
		userDAO.save(user);
		securityService.autologin(user.getUsername(), user.getPassword());
	}

	@Override
	public PasswordResetToken getPasswordResetToken(String token) {
		return passwordResetTokenDAO.findByToken(token);
	}

	@Override
	public void addDeviceIdForCurrentUser(String deviceId) {
		if (deviceId == null)// request come from the web
			return;
		User user = securityService.findLoggedInUser();
		Device newDevice = new Device(deviceId);
		newDevice.setUser(user);
		Set<Device> oldDevices = user.getDevices();
		if (!oldDevices.contains(newDevice)) {
			oldDevices.add(newDevice);
			deviceDAO.save(newDevice);
		}
	}

	@Override
	public OnPreparationEmailCompleteEvent prepareEventForRegistration(edu.unifi.disit.wallet_user_mngt.datamodel.User user, Locale locale) {
		String token = UUID.randomUUID().toString();
		createVerificationToken(user, token);
		String recipientAddress = user.getUsername();
		String subject = "Registration Confirmation";
		String testo = messages.getMessage("registration.invitation", null, locale) + " " + myappuri + "/confirm?token=" + token;

		List<String> testos = new ArrayList<String>();
		testos.add(testo);

		return new OnPreparationEmailCompleteEvent(recipientAddress, subject, testos, locale);
	}

	@Override
	public Date setLastLogin(Date date) {
		User user = securityService.findLoggedInUser();
		Date toreturn = user.getLastLogin();
		user.setLastLogin(date);
		userDAO.save(user);
		return toreturn;
	}

	@Override
	public String getLoggedUsername() {
		return securityService.findLoggedInUsername();
	}

	@Override
	public Set<Device> getConnectedDevicesForLoggedUser() {
		User user = securityService.findLoggedInUser();
		return user.getDevices();
	}

	@Override
	public Boolean isConfirmed() {
		User user = securityService.findLoggedInUser();
		if (user.getVerificationToken() == null)
			return false;
		return user.getVerificationToken().getVerified();
	}

	@Override
	public Integer getAllPointsForLoggedUserForCampaign(String campaignName) {
		return traceInDAO.countAllPointsForUserForCampaing(securityService.findLoggedInUsername(), campaignName);
	}

	@Override
	public Page<Object[]> getAllTraceinsForLoggedUserForCampaign(String campaignName, PageRequest pageRequest) {
		return traceInDAO.getAllTraceinsForUserForCampaing(securityService.findLoggedInUsername(), campaignName, pageRequest);
	}

	@Override
	public Integer getPointsFromDateForLoggedUsersForCampaign(String campaignName, Date from) {
		return traceInDAO.countPointsForUserForCampaingFromDate(securityService.findLoggedInUsername(), campaignName, from);
	}

	@Override
	public Page<Object[]> getTraceinsFromDateForLoggedUserForCampaign(String campaignName, PageRequest pageRequest, Date from) {
		return traceInDAO.getAllTraceinsForUserForCampaingFromDate(securityService.findLoggedInUsername(), campaignName, from, pageRequest);
	}

	@Override
	public Page<Object[]> getAllTraceoutsForLoggedUserForCampaign(String campaignName, PageRequest pageRequest) {
		return traceOutDAO.getAllTraceoutsForUserForCampaing(securityService.findLoggedInUsername(), campaignName, pageRequest);
	}

}
