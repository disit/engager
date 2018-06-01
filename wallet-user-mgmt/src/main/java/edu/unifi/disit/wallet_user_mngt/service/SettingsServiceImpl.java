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

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.unifi.disit.wallet_user_mngt.datamodel.User;
import edu.unifi.disit.wallet_user_mngt.datamodel.UserDAO;

@Service
public class SettingsServiceImpl implements ISettingsService {

	@Autowired
	private MessageSource messages;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private UserDAO userDAO;

	@Autowired
	private SecurityServiceImpl securityService;

	// private EmailValidator emailValidator = new EmailValidator();

	@Transactional
	@Override
	public void saveSettings(Map<String, String> settings) throws Exception {
		// if (!emailValidator.validate(settings.get("EMAIL"))) {
		// throw new Exception("registration.ko.emailnotvalid");
		// }
		User logged = securityService.findLoggedInUser();
		// logged.setUsername(settings.get("EMAIL"));
		logged.setVisible(Boolean.valueOf(settings.get("PARTECIPA")));
		logged.setNickname(settings.get("NICKNAME"));
		boolean pwd_changed = !(settings.get("NUOVA PASSWORD").length() == 0);
		// if passwordchanged, update parameter, and autologin after saved
		if ((pwd_changed)) {
			// if pwd and confirm is equals
			if (!settings.get("NUOVA PASSWORD").equals(settings.get("CONFERMA NUOVA PASSWORD")))
				throw new Exception(messages.getMessage("user.validator.ko.password.notmatching", null, LocaleContextHolder.getLocale()));
			else {
				logged.setPassword(passwordEncoder.encode(settings.get("NUOVA PASSWORD")));
				userDAO.save(logged);
				securityService.autologin(logged.getUsername(), logged.getPassword());
			}
		}
	}

	@Override
	public Map<String, String> loadSettings() {
		Map<String, String> toreturn = new HashMap<String, String>();
		User logged = securityService.findLoggedInUser();
		toreturn.put("PARTECIPA", logged.getVisible().toString());
		toreturn.put("NICKNAME", logged.getNickname());
		toreturn.put("NUOVA PASSWORD", "");
		toreturn.put("CONFERMA NUOVA PASSWORD", "");
		return toreturn;
	}
}
