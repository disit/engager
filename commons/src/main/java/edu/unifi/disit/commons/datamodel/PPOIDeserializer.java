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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class PPOIDeserializer extends StdDeserializer<PPOI> {

	private static final long serialVersionUID = 1L;

	public PPOIDeserializer() {
		this(null);
	}

	public PPOIDeserializer(Class<?> vc) {
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
	public PPOI deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		JsonNode node = jp.getCodec().readTree(jp);

		Long idValue = null;
		Boolean confValue = null;
		String labelValue = null;
		String cpzValue = null;
		String addressValue = null;
		String municipalityValue = null;
		String numberValue = null;

		if (node.get("id") != null)
			idValue = node.get("id").asLong();

		if (node.get("confirmation") != null)
			confValue = node.get("confirmation").asBoolean();

		if (node.get("label") != null)
			labelValue = node.get("label").asText();

		if ((node.get("cpz") == null) || (node.get("address") == null) || (node.get("municipality") == null) || (node.get("number") == null)) {
			// TODO retrieve info from serviceMap
			cpzValue = "null";
			addressValue = "null";
			municipalityValue = "null";
			numberValue = "null";
		} else {
			cpzValue = node.get("cpz").asText();
			addressValue = node.get("address").asText();
			municipalityValue = node.get("municipality").asText();
			numberValue = node.get("number").asText();
		}

		return new PPOI(idValue, node.get("latitude").asDouble(), node.get("longitude").asDouble(), node.get("name").asText(),
				(float) node.get("accuracy").asDouble(), cpzValue, addressValue,
				confValue, municipalityValue, numberValue, labelValue);
	}
}
