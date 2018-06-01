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
package edu.unifi.disit.wallet_user_mngt.validator;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import edu.unifi.disit.wallet_user_mngt.datamodel.User;

public class UserValidator implements Validator {

	private Pattern pattern;
	private Matcher matcher;
	private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-+]+(.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(.[A-Za-z0-9]+)*(.[A-Za-z]{2,})$";
	private MessageSource messages;

	public UserValidator(MessageSource messages) {
		pattern = Pattern.compile(EMAIL_PATTERN);
		this.messages = messages;
	}

	public boolean validate(final String hex) {
		matcher = pattern.matcher(hex);
		return matcher.matches();
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return User.class.equals(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		validate(target, errors, LocaleContextHolder.getLocale());
	}

	public void validate(Object target, Errors errors, Locale lang) {
		User user = (User) target;
		if (user.getUsername() == null) {
			errors.rejectValue("username", "error.user", messages.getMessage("user.validator.ko.username.notnull", null, lang));
		} else if (user.getUsername().length() < 2) {
			errors.rejectValue("username", "error.user", messages.getMessage("user.validator.ko.username.length", new Object[] { 2 }, lang));
		} else if (!validate(user.getUsername())) {
			errors.rejectValue("username", "error.user", messages.getMessage("user.validator.ko.username.notvalid", null, lang));
		}
		if (user.getPassword() == null) {
			errors.rejectValue("password", "error.user", messages.getMessage("user.validator.ko.password.notnull", null, lang));
			// } else if (user.getPassword().length() < 6) {
			// errors.rejectValue("password", "error.user", messages.getMessage("validator.length", new Object[] { 6 }, LocaleContextHolder.getLocale()));
		} else if (!user.getPassword().equals(user.getPasswordConfirm())) {
			errors.rejectValue("passwordConfirm", "error.user", messages.getMessage("user.validator.ko.password.notmatching", null, lang));
		}
	}
}
