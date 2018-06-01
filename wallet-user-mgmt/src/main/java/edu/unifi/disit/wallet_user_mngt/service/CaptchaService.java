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

import java.net.URI;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestOperations;

import edu.unifi.disit.wallet_user_mngt.captcha.CaptchaSettings;
import edu.unifi.disit.wallet_user_mngt.captcha.GoogleResponse;
import edu.unifi.disit.wallet_user_mngt.exception.InvalidReCaptchaException;

@Service
public class CaptchaService implements ICaptchaService {

	@Autowired
	private CaptchaSettings captchaSettings;

	@Autowired
	private RestOperations restTemplate;

	private static Pattern RESPONSE_PATTERN = Pattern.compile("[A-Za-z0-9_-]+");

	@Override
	public void processResponse(String response, HttpServletRequest request) throws InvalidReCaptchaException {
		if (!responseSanityCheck(response)) {
			throw new InvalidReCaptchaException("Response contains invalid characters");
		}

		URI verifyUri = URI.create(String.format(
				"https://www.google.com/recaptcha/api/siteverify?secret=%s&response=%s&remoteip=%s",
				captchaSettings.getSecret(), response, getClientIP(request)));

		GoogleResponse googleResponse = restTemplate.getForObject(verifyUri, GoogleResponse.class);

		if (!googleResponse.isSuccess()) {
			throw new InvalidReCaptchaException("reCaptcha was not successfully validated");
		}
	}

	private boolean responseSanityCheck(String response) {
		return StringUtils.hasLength(response) && RESPONSE_PATTERN.matcher(response).matches();
	}

	private String getClientIP(HttpServletRequest request) {

		String remoteAddr = "";

		if (request != null) {
			remoteAddr = request.getHeader("X-FORWARDED-FOR");
			if (remoteAddr == null || "".equals(remoteAddr)) {
				remoteAddr = request.getRemoteAddr();
			}
		}

		return remoteAddr;
	}
}
