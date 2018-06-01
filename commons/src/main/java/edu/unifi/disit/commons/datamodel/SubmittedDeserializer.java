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
package edu.unifi.disit.commons.datamodel;

import java.io.IOException;
import java.util.Date;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class SubmittedDeserializer extends StdDeserializer<Submitted> {

	private static final long serialVersionUID = 1L;

	public SubmittedDeserializer() {
		this(null);
	}

	public SubmittedDeserializer(Class<?> vc) {
		super(vc);
	}

	// mandatory are
	// latitude
	// longitude
	// name
	// accuracy

	// optional are
	// confirmation
	// label

	// toretrieve externally
	// cpz
	// address
	// municipality
	// number

	@Override
	public Submitted deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		JsonNode node = jp.getCodec().readTree(jp);

		Long timeValue = null;
		String serviceUriValue = null;
		String textValue = null;
		String serviceUriName = null;
		String serviceUriServiceType = null;

		if (node.get("time") != null)
			timeValue = node.get("time").asLong();

		if (node.get("serviceUri") != null)
			serviceUriValue = node.get("serviceUri").asText();

		if (node.get("text") != null)
			textValue = node.get("text").asText();

		if (node.get("serviceUriName") != null)
			serviceUriName = node.get("serviceUriName").asText();

		if (node.get("serviceUriServiceType") != null)
			serviceUriServiceType = node.get("serviceUriServiceType").asText();

		return new Submitted(new Date(timeValue), serviceUriValue, textValue, serviceUriName, serviceUriServiceType);
	}
}
