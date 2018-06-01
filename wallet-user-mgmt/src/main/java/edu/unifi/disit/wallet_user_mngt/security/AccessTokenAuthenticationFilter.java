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
package edu.unifi.disit.wallet_user_mngt.security;

import java.io.IOException;
import java.net.URI;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.web.authentication.rememberme.AbstractRememberMeServices;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.api.impl.FacebookTemplate;
import org.springframework.social.google.api.Google;
import org.springframework.social.google.api.impl.GoogleTemplate;
import org.springframework.social.linkedin.api.LinkedIn;
import org.springframework.social.linkedin.api.impl.LinkedInTemplate;
import org.springframework.social.support.URIBuilder;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.social.twitter.api.impl.TwitterTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.GenericFilterBean;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.unifi.disit.wallet_user_mngt.datamodel.Socialusertype;
import edu.unifi.disit.wallet_user_mngt.exception.SocialNotRecognizedException;
import edu.unifi.disit.wallet_user_mngt.object.Response;
import edu.unifi.disit.wallet_user_mngt.service.ISecurityService;
import edu.unifi.disit.wallet_user_mngt.service.ISocialService;
import edu.unifi.disit.wallet_user_mngt.service.TokenAuthenticationService;

@Component
public class AccessTokenAuthenticationFilter extends GenericFilterBean {

	// @Autowired
	// private MessageSource messages;

	@Autowired
	ISecurityService securityService;

	private static final Logger logger = LogManager.getLogger();

	@Value("${spring.social.facebook.appId}")
	private String fb_appid;

	@Value("${facebook.app.access_token}")
	private String fb_apptoken;

	@Value("${spring.social.google.appId}")
	private String google_appid;

	@Value("${spring.social.google.appSecret}")
	private String google_appsecret;

	@Value("${application.url}")
	private String redirect_uri;

	@Autowired
	private ISocialService socialservice;

	ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws ServletException, IOException {

		final HttpServletRequest req = (HttpServletRequest) request;
		String xAuthAccessToken = req.getParameter("accesstoken");

		// TODO here read language and customize the error message

		if (xAuthAccessToken != null) {

			Socialusertype socialusertype = extractSocialName(xAuthAccessToken);
			String accesstoken = extractAccessToken(xAuthAccessToken);

			try {

				if ((accesstoken = isValid(socialusertype, accesstoken)) == null) {
					// throw new SecurityException("accesstoken is not valid");// this message is included in HTTP/1.1 500 Internal Server Error
					Response toreturn2 = new Response();
					toreturn2.setResult(false);
					toreturn2.setMessage("loginSocial.tokenNotValid");

					((HttpServletResponse) response).setStatus(401);
					((HttpServletResponse) response).getWriter().write(objectMapper.writeValueAsString(toreturn2));
					return;
				}

				switch (socialusertype) {
				case FACEBOOK:
					Facebook facebook = new FacebookTemplate(accesstoken);
					socialservice.registerOrLogin(facebook, socialusertype, request.getLocale());
					break;
				case TWITTER:
					Twitter twitter = new TwitterTemplate(accesstoken);
					socialservice.registerOrLogin(twitter, socialusertype, request.getLocale());
					break;
				case LINKEDIN:
					LinkedIn linkedin = new LinkedInTemplate(accesstoken);
					socialservice.registerOrLogin(linkedin, socialusertype, request.getLocale());
					break;
				case GOOGLE:
					Google google = new GoogleTemplate(accesstoken);
					socialservice.registerOrLogin(google, socialusertype, request.getLocale());
					break;
				default:
					throw new SecurityException(socialusertype.toString() + " is not recognized");
				}
			} catch (SocialNotRecognizedException snre) {
				// throw new SecurityException(snre.getMessage());// this message is included in HTTP/1.1 500 Internal Server Error
				logger.error("errore", snre);

				Response toreturn2 = new Response();
				toreturn2.setResult(false);
				toreturn2.setMessage(snre.getMessage());

				((HttpServletResponse) response).setStatus(401);
				((HttpServletResponse) response).getWriter().write(objectMapper.writeValueAsString(toreturn2));

				return;
			}

			if (isRememberMeRequested(request, AbstractRememberMeServices.DEFAULT_PARAMETER)) {
				securityService.rememberMe((HttpServletRequest) request, (HttpServletResponse) response);
			}

			// jwt header (username from user logger before)
			TokenAuthenticationService.addAuthentication((HttpServletResponse) response, securityService.findLoggedInUsername());

		} else
			filterChain.doFilter(request, response);
	}

	private String extractAccessToken(String xAuthAccessToken) {
		logger.debug("header arrived: " + xAuthAccessToken);
		logger.debug("return accesstoken: " + xAuthAccessToken.substring(xAuthAccessToken.indexOf(":") + 1));

		return xAuthAccessToken.substring(xAuthAccessToken.indexOf(":") + 1);
	}

	private Socialusertype extractSocialName(String xAuthAccessToken) {
		logger.debug("header arrived: " + xAuthAccessToken);
		logger.debug("return socialname: " + xAuthAccessToken.substring(0, xAuthAccessToken.indexOf(":")));

		return Socialusertype.fromString(xAuthAccessToken.substring(0, xAuthAccessToken.indexOf(":")));
	}

	private String isValid(Socialusertype socialusertype, String accesstoken) throws SocialNotRecognizedException {
		switch (socialusertype) {
		case GOOGLE:
			return authGoogle(accesstoken);
		case FACEBOOK:
			return authFacebook(accesstoken);
		case LINKEDIN:
			// linkein does ot support client id validation
			return accesstoken;
		case TWITTER:
			// create signature https://dev.twitter.com/oauth/overview/creating-signatures and create oauth https://dev.twitter.com/oauth/overview/authorizing-requests
			// and invoke the https://api.twitter.com/1.1/account/verify_credentials.json
			return null;

		default:
			return null;
		}
	}

	private String authFacebook(String accesstoken) throws SocialNotRecognizedException {
		// https://developers.facebook.com/docs/facebook-login/access-tokens/debugging-and-error-handling
		// {
		// "data": {
		// "app_id": "698493816993779",
		// "application": "engager",
		// "expires_at": 1486218305,
		// "is_valid": true,
		// "issued_at": 1481034305,
		// "scopes": [
		// "user_posts",
		// "email",
		// "public_profile"
		// ],
		// "user_id": "116894488786314"
		// }
		// }

		// retrieve debug info from accesstoken
		URIBuilder builder = URIBuilder.fromUri(String.format("%s/debug_token", "https://graph.facebook.com"));
		builder.queryParam("access_token", fb_apptoken);
		builder.queryParam("input_token", accesstoken);
		URI uri = builder.build();
		RestTemplate restTemplate = new RestTemplate();

		JsonNode resp = null;
		try {
			resp = restTemplate.getForObject(uri, JsonNode.class);
		} catch (HttpClientErrorException e) {
			throw new SocialNotRecognizedException("issues arised contacting: " + uri.toString());
		}

		logger.debug("got response from debug_token {}", resp);

		// check the token is valid
		// check the token came from my appID
		if ((resp.path("data").findValue("is_valid").asBoolean()) && (resp.path("data").findValue("app_id").asText().equals(fb_appid)))
			return accesstoken;
		else
			return null;
	}

	private String authGoogle(String serverAccesstoken) throws SocialNotRecognizedException {
		// 1-https://www.googleapis.com/oauth2/v4/token

		// 2-https://www.googleapis.com/oauth2/v3/tokeninfo?access_token={}
		// {
		// "azp": "390949221055-b00orgl4egp3v1bcnhlplac2mttcf8d5.apps.googleusercontent.com",
		// "aud": "390949221055-b00orgl4egp3v1bcnhlplac2mttcf8d5.apps.googleusercontent.com",
		// "sub": "100421709908755714724",
		// "scope": "https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/plus.login https://www.googleapis.com/auth/plus.me https://www.googleapis.com/auth/tasks https://www.googleapis.com/auth/drive",
		// "exp": "1481042624",
		// "expires_in": "3482",
		// "email": "angelo.difino@gmail.com",
		// "email_verified": "true",
		// "access_type": "offline"
		// }

		// 1-retrieve an accesstoken from a serverauthcode
		URIBuilder builder = URIBuilder.fromUri(String.format("%s/oauth2/v4/token", "https://www.googleapis.com"));

		MultiValueMap<String, String> form = new LinkedMultiValueMap<String, String>();

		form.add("code", serverAccesstoken);
		form.add("client_id", google_appid);
		form.add("client_secret", google_appsecret);
		form.add("redirect_uri", redirect_uri + "/signin/google");
		form.add("grant_type", "authorization_code");

		URI uri = builder.build();
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(new FormHttpMessageConverter());
		JsonNode resp = null;
		try {
			resp = restTemplate.postForObject(uri, form, JsonNode.class);
		} catch (HttpClientErrorException e) {
			logger.error(" {} ", e.getResponseBodyAsString());
			throw new SocialNotRecognizedException("issues arised contacting: " + uri.toString());
		}

		String accessToken = resp.findValue("access_token").asText();

		logger.debug("got response from token {}", resp);

		// 2-retrieve an tokeninfo from the accesstoken
		builder = URIBuilder.fromUri(String.format("%s/oauth2/v3/tokeninfo", "https://www.googleapis.com"));
		builder.queryParam("access_token", accessToken);
		uri = builder.build();
		restTemplate = new RestTemplate();

		resp = null;
		try {
			resp = restTemplate.getForObject(uri, JsonNode.class);
		} catch (HttpClientErrorException e) {
			throw new SocialNotRecognizedException("issues arised contacting: " + uri.toString());
		}

		logger.debug("got response from tokeninfo {}", resp);

		// check that the accesstoken is not expired
		// check that the accesstoken came from my appID
		if ((resp.findValue("expires_in").asInt() > 0) && (resp.findValue("aud").asText().equals(google_appid)))
			return accessToken;
		else
			return null;
	}

	private boolean isRememberMeRequested(ServletRequest request, String parameter) {

		String paramValue = request.getParameter(parameter);

		if (paramValue != null) {
			if (paramValue.equalsIgnoreCase("true") || paramValue.equalsIgnoreCase("on")
					|| paramValue.equalsIgnoreCase("yes") || paramValue.equals("1")) {
				return true;
			}
		}

		return false;
	}
}
