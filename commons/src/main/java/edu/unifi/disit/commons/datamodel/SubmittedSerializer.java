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

public class SubmittedSerializer extends StdSerializer<Submitted> {

	private static final long serialVersionUID = 1L;

	public SubmittedSerializer() {
		this(null);
	}

	public SubmittedSerializer(Class<Submitted> t) {
		super(t);
	}

	@Override
	public void serialize(Submitted value, JsonGenerator jgen, SerializerProvider provider) throws IOException {

		jgen.writeStartObject();

		if (value.getTime() != null)
			jgen.writeNumberField("time", value.getTime().getTime());
		if (value.getServiceUri() != null)
			jgen.writeStringField("serviceUri", value.getServiceUri());
		if (value.getText() != null)
			jgen.writeStringField("text", value.getText());
		if (value.getServiceUriName() != null)
			jgen.writeStringField("serviceUriName", value.getServiceUriName());
		if (value.getServiceUriServiceType() != null)
			jgen.writeStringField("serviceUriServiceType", value.getServiceUriServiceType());

		jgen.writeEndObject();
	}
}
