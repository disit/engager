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
package edu.unifi.disit.wallet_user_mngt.object;

import java.util.List;

import edu.unifi.disit.wallet_user_mngt.datamodel.Consent;

public class PrivacyForm {

	// List<IOTApp> IOTApps;
	// List<MobileApp> mobileApps;

	List<Consent> consents;

	public PrivacyForm() {
	}

	public List<Consent> getConsents() {
		return consents;
	}

	public void setConsents(List<Consent> consents) {
		this.consents = consents;
	}

	Integer choosen;

	public Integer getChoosen() {
		return choosen;
	}

	public void setChoosen(Integer choosen) {
		this.choosen = choosen;
	}

	// public List<IOTApp> getIOTApps() {
	// return IOTApps;
	// }
	//
	// public void setIOTApps(List<IOTApp> iOTApps) {
	// IOTApps = iOTApps;
	// }
	//
	// public List<MobileApp> getMobileApps() {
	// return mobileApps;
	// }
	//
	// public void setMobileApps(List<MobileApp> mobileApps) {
	// this.mobileApps = mobileApps;
	// }

}
