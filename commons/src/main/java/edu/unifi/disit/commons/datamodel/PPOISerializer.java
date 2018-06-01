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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class PPOISerializer extends StdSerializer<PPOI> {

	private static final long serialVersionUID = 1L;

	public PPOISerializer() {
		this(null);
	}

	public PPOISerializer(Class<PPOI> t) {
		super(t);
	}

	@Override
	public void serialize(PPOI value, JsonGenerator jgen, SerializerProvider provider) throws IOException {

		jgen.writeStartObject();

		if (value.getId() != null)
			jgen.writeNumberField("id", value.getId());
		if (value.getLatitude() != null)
			jgen.writeNumberField("latitude", value.getLatitude());
		if (value.getLongitude() != null)
			jgen.writeNumberField("longitude", value.getLongitude());
		if (value.getName() != null)
			jgen.writeStringField("name", value.getName());
		if (value.getAccuracy() != null)
			jgen.writeNumberField("accuracy", value.getAccuracy());
		if (value.getCpz() != null)
			jgen.writeStringField("cpz", value.getCpz());
		if (value.getAddress() != null)
			jgen.writeStringField("address", value.getAddress());
		if (value.getMunicipality() != null)
			jgen.writeStringField("municipality", value.getMunicipality());
		if (value.getNumber() != null)
			jgen.writeStringField("number", value.getNumber());
		if (value.getConfirmation() != null)
			jgen.writeBooleanField("confirmation", value.getConfirmation());
		if (value.getLabel() != null)
			jgen.writeStringField("label", value.getLabel());

		jgen.writeEndObject();
	}
}
