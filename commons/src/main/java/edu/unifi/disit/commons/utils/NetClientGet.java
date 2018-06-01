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
package edu.unifi.disit.commons.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.NoSuchElementException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NetClientGet {

	private static final Logger logger = LogManager.getLogger("NetClientGet");

	public String get(URL request, int readTimeout) throws NoSuchElementException {
		return method(request, readTimeout, Level.ERROR, null, "GET", null);
	}

	public String get(URL request, int readTimeout, org.apache.logging.log4j.Level errorLevel) throws NoSuchElementException {
		return method(request, readTimeout, errorLevel, null, "GET", null);
	}

	public String post(URL request, int readTimeout, String postParameters) throws NoSuchElementException {
		return method(request, readTimeout, Level.ERROR, postParameters, "POST", null);
	}

	public String post(URL request, int readTimeout, String postParameters, String contentType) throws NoSuchElementException {
		return method(request, readTimeout, Level.ERROR, postParameters, "POST", contentType);
	}

	public String post(URL request, int readTimeout, org.apache.logging.log4j.Level errorLevel, String postParameters) throws NoSuchElementException {
		return method(request, readTimeout, errorLevel, postParameters, "POST", null);
	}

	public String post(URL request, int readTimeout, org.apache.logging.log4j.Level errorLevel, String postParameters, String contentType) throws NoSuchElementException {
		return method(request, readTimeout, errorLevel, postParameters, "POST", contentType);
	}

	public String delete(URL request, Integer readTimeout) {
		return method(request, readTimeout, Level.ERROR, null, "DELETE", null);
	}

	// public String put(URL request, int readTimeout) throws NoSuchElementException {
	// return method(request, readTimeout, Level.ERROR, null, "PUT");
	// }
	//
	// public String put(URL request, int readTimeout, String putParameters) throws NoSuchElementException {
	// return method(request, readTimeout, Level.ERROR, putParameters, "PUT");
	// }
	//
	// public String put(URL request, int readTimeout, org.apache.logging.log4j.Level errorLevel) throws NoSuchElementException {
	// return method(request, readTimeout, errorLevel, null, "PUT");
	// }
	//
	// // response is always a string, maybe empty
	// public String put(URL request, int readTimeout, org.apache.logging.log4j.Level errorLevel, String putParameters) throws NoSuchElementException {
	// return method(request, readTimeout, errorLevel, putParameters, "PUT");
	// }

	// response is always a string, maybe empty
	private String method(URL request, int readTimeout, org.apache.logging.log4j.Level errorLevel, String postParameters, String method, String contentType) throws NoSuchElementException {

		HttpURLConnection conn = null;
		InputStreamReader isr = null;
		BufferedReader br = null;

		String response = new String();
		String errorMsg = null;

		try {
			logger.debug("Contacting:{}", request);

			conn = (HttpURLConnection) request.openConnection();
			conn.setRequestProperty("Accept", "application/json");
			conn.setUseCaches(true);
			conn.setConnectTimeout(300); // 0.3 sec
			conn.setReadTimeout(readTimeout);
			conn.setRequestMethod(method);

			if (postParameters != null) {

				if (contentType != null)
					conn.setRequestProperty("Content-Type", contentType);
				else
					conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

				conn.setRequestProperty("charset", "utf-8");
				conn.setRequestProperty("Content-Length", Integer.toString(postParameters.getBytes().length));

				conn.setDoOutput(true);
				DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
				wr.writeBytes(postParameters);
				wr.flush();
				wr.close();
			}

			logger.trace("debugging netclient/uno");

			if (conn.getResponseCode() != 200) {
				conn.disconnect();
				throw new Exception("Failed : HTTP error code : " + conn.getResponseCode() + ". Request was:" + request.toString());
			}

			logger.trace("debugging netclient/due");

			isr = new InputStreamReader((conn.getInputStream()));
			br = new BufferedReader(isr);
			String output = new String();

			while ((output = br.readLine()) != null) {
				response += output;
			}

			logger.trace("debugging netclient/tre");

			logger.debug("Output from Server is:{}", response);

		} catch (

		SocketTimeoutException ste) {
			logger.log(errorLevel, "SocketTimeoutException:", ste);
			logger.log(errorLevel, "Request was:{}", request.toString());
			errorMsg = ste.getMessage();
		} catch (IOException ioe) {
			logger.log(errorLevel, "IOException:", ioe);
			logger.log(errorLevel, "Request was:{}", request.toString());
			errorMsg = ioe.getMessage();
		} catch (Exception e) {
			logger.log(errorLevel, "Exception:", e);
			logger.log(errorLevel, "Request was:{}", request.toString());
			errorMsg = e.getMessage();
		} finally {
			if (br != null)
				try {
					br.close();
					logger.trace("BR closed");
				} catch (IOException e) {
					logger.error("cannot close BR:", e);
				}
			if (isr != null)
				try {
					isr.close();
					logger.trace("ISR closed");
				} catch (IOException e) {
					logger.error("cannot close ISR:", e);
				}
			if (conn != null) {
				conn.disconnect();
				logger.trace("CONN closed");
			}
		}

		if (errorMsg != null)
			throw new NoSuchElementException(errorMsg);

		return response;
	}

}
