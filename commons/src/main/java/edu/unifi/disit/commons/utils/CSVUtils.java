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

import java.io.IOException;
import java.io.Writer;
import java.util.List;

public class CSVUtils {

	private static final char DEFAULT_SEPARATOR = ',';

	public static void writeLine(Writer w, List<String> values) throws IOException {
		writeLine(w, values, DEFAULT_SEPARATOR, ' ');
	}

	public static void writeLine(Writer w, List<String> values, char separators) throws IOException {
		writeLine(w, values, separators, ' ');
	}

	// https://tools.ietf.org/html/rfc4180
	private static String followCVSformat(String value) {

		String result = value;
		if (result.contains("\"")) {
			result = result.replace("\"", "\"\"");
		}
		return result;

	}

	public static void writeLine(Writer w, List<String> values, char separators, char customQuote) throws IOException {

		boolean first = true;

		// default customQuote is empty

		if (separators == ' ') {
			separators = DEFAULT_SEPARATOR;
		}

		StringBuilder sb = new StringBuilder();
		for (String value : values) {
			if (!first) {
				sb.append(separators);
			}
			if (customQuote == ' ') {
				sb.append(followCVSformat(value));
			} else {
				sb.append(customQuote).append(followCVSformat(value)).append(customQuote);
			}

			first = false;
		}
		sb.append("\n");
		w.append(sb.toString());

	}

}
