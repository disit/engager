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
package edu.unifi.disit.wallet_user_mngt.captcha;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({
		"success",
		"challenge_ts",
		"hostname",
		"error-codes"
})
public class GoogleResponse {

	@JsonProperty("success")
	private boolean success;

	@JsonProperty("challenge_ts")
	private String challengeTs;

	@JsonProperty("hostname")
	private String hostname;

	@JsonProperty("error-codes")
	private ErrorCode[] errorCodes;

	@SuppressWarnings("incomplete-switch")
	@JsonIgnore
	public boolean hasClientError() {
		ErrorCode[] errors = getErrorCodes();
		if (errors == null) {
			return false;
		}
		for (ErrorCode error : errors) {
			switch (error) {
			case InvalidResponse:
			case MissingResponse:
				return true;
			}
		}
		return false;
	}

	static enum ErrorCode {
		MissingSecret, InvalidSecret, MissingResponse, InvalidResponse;

		private static Map<String, ErrorCode> errorsMap = new HashMap<String, ErrorCode>(4);

		static {
			errorsMap.put("missing-input-secret", MissingSecret);
			errorsMap.put("invalid-input-secret", InvalidSecret);
			errorsMap.put("missing-input-response", MissingResponse);
			errorsMap.put("invalid-input-response", InvalidResponse);
		}

		@JsonCreator
		public static ErrorCode forValue(String value) {
			return errorsMap.get(value.toLowerCase());
		}
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getChallengeTs() {
		return challengeTs;
	}

	public void setChallengeTs(String challengeTs) {
		this.challengeTs = challengeTs;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public ErrorCode[] getErrorCodes() {
		return errorCodes;
	}

	public void setErrorCodes(ErrorCode[] errorCodes) {
		this.errorCodes = errorCodes;
	}
}
