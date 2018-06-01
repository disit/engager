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

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import edu.unifi.disit.wallet_user_mngt.datamodel.Device;
import edu.unifi.disit.wallet_user_mngt.datamodel.PasswordResetToken;
import edu.unifi.disit.wallet_user_mngt.datamodel.Prize;
import edu.unifi.disit.wallet_user_mngt.datamodel.Roletype;
import edu.unifi.disit.wallet_user_mngt.datamodel.User;
import edu.unifi.disit.wallet_user_mngt.datamodel.VerificationToken;
import edu.unifi.disit.wallet_user_mngt.event.OnPreparationEmailCompleteEvent;
import edu.unifi.disit.wallet_user_mngt.exception.EmailExistsException;

public interface IUserService {

	User registerNewUserAccount(User accountDto, Locale lang, Roletype role) throws EmailExistsException;

	User getUser(String verificationToken);

	void saveRegisteredUser(User user);

	void deleteLoggedUser();

	void createVerificationToken(User user, String token);

	VerificationToken getVerificationToken(String VerificationToken);

	Page<Object[]> getTotalPointForAllUsers(PageRequest pr);

	Page<Object[]> getTotalPointForAllUsersVisible(PageRequest pageRequest);

	Page<Object[]> getCurrentPointForEcosystemForLoggedUser(PageRequest pageRequest);

	public Integer getTotalPointForLoggedUsers();

	public Page<Object[]> getAllPointForLoggedUser(PageRequest pageRequest);

	public String getNameOfEcosystem(Long id);

	Long getCurrentPointEcosystemForLoggedUser(Long id);

	Page<Object[]> getPrizesForEcosystem(Long id, PageRequest pageRequest);

	Page<Object[]> findAllTraceOutForLoggedUserForEcosystem(Long id, PageRequest pageRequest);

	List<Prize> getAllPrizeByEcosystem(Long id);

	public void createPasswordResetTokenForUser(User user, String token);

	public PasswordResetToken getPasswordResetToken(String token);

	void addDeviceIdForCurrentUser(String deviceId);

	void changePasswordForLogged(String password);

	OnPreparationEmailCompleteEvent prepareEventForRegistration(User user, Locale locale);

	Date setLastLogin(Date date);

	String getLoggedUsername();

	Set<Device> getConnectedDevicesForLoggedUser();

	Boolean isConfirmed();

	// PILOTA

	Integer getAllPointsForLoggedUserForCampaign(String campaignName);

	Page<Object[]> getAllTraceinsForLoggedUserForCampaign(String campaignName, PageRequest pageRequest);

	Integer getPointsFromDateForLoggedUsersForCampaign(String campaignName, Date date);

	Page<Object[]> getTraceinsFromDateForLoggedUserForCampaign(String campaignName, PageRequest pageRequest, Date date);

	Page<Object[]> getAllTraceoutsForLoggedUserForCampaign(String campaignName, PageRequest pageRequest);

}
