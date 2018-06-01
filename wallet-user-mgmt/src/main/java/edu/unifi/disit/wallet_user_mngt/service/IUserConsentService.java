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

import java.util.List;

import edu.unifi.disit.commons.datamodel.DatasetType;
import edu.unifi.disit.wallet_user_mngt.datamodel.Consent;
import edu.unifi.disit.wallet_user_mngt.exception.OperationNotPermittedException;
import edu.unifi.disit.wallet_user_mngt.object.MobileApp;

public interface IUserConsentService {

	Consent updateConsentForLoggedUser(Consent consent, String lang);

	List<Consent> getConsentsForDevice(String deviceId, DatasetType dataset, String lang);

	List<Consent> getConsentsForLoggedUser(DatasetType dataset, String lang);

	void checkConsent(String deviceId, DatasetType dataset, String lang) throws OperationNotPermittedException;

	void checkConsentForLoggedUser(DatasetType dataset, String lang) throws OperationNotPermittedException;

	String getUsernameForDevice(String deviceId, DatasetType dataset);

	List<String> getDeviceForLoggedUser(DatasetType dataset);

	List<MobileApp> getDeviceForLoggedUserDetailed(DatasetType dataset);
}
