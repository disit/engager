/* Data Manager (DM).
   Copyright (C) 2015 DISIT Lab http://www.disit.org - University of Florence
   This program is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public License
   as published by the Free Software Foundation; either version 2
   of the License, or (at your option) any later version.
   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.
   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA. */
package edu.unifi.disit.userprofiler.ppois.markov;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import edu.unifi.disit.commons.datamodel.Prediction;

public class MarkovNetworkSerializer extends StdSerializer<MarkovNetwork> {

	private static final long serialVersionUID = 1L;

	public MarkovNetworkSerializer() {
		this(null);
	}

	public MarkovNetworkSerializer(Class<MarkovNetwork> t) {
		super(t);
	}

	@Override
	public void serialize(MarkovNetwork value, JsonGenerator jgen, SerializerProvider provider) throws IOException {

		jgen.writeStartObject();

		List<String> statesLabel = value.getStatesLabel();
		jgen.writeArrayFieldStart("ppoisLabel");
		for (String s : statesLabel) {
			jgen.writeString(s);
		}
		jgen.writeEndArray();

		HashMap<List<Integer>, Predictions> predictionMatrix = value.getPredictionMatrix();

		Set<List<Integer>> keys = predictionMatrix.keySet();
		jgen.writeFieldName("trips");
		jgen.writeStartArray();
		for (List<Integer> key : keys) {
			jgen.writeStartObject();
			jgen.writeNumberField("from", key.get(0));
			jgen.writeNumberField("slot", key.get(1));
			jgen.writeNumberField("day", key.get(2));

			Predictions ps = predictionMatrix.get(key);
			jgen.writeNumberField("total", ps.getNTotalObservation());

			jgen.writeFieldName("activity");
			jgen.writeStartArray();

			HashMap<Integer, Prediction> p = ps.getPredictions();
			for (int i = 0; i < statesLabel.size(); i++) {
				jgen.writeStartObject();

				Prediction pre = p.get(i);
				jgen.writeStringField("to", pre.getName());
				jgen.writeNumberField("accuracy", pre.getAccuracy());
				jgen.writeNumberField("howmany", pre.getHowmany());
				jgen.writeNumberField("duration", pre.getDuration());
				jgen.writeNumberField("distance", pre.getDistance());
				List<String> modality = pre.getModality();
				jgen.writeArrayFieldStart("modality");
				for (String s : modality) {
					jgen.writeString(s);
				}
				jgen.writeEndArray();

				jgen.writeEndObject();
			}
			jgen.writeEndArray();

			jgen.writeEndObject();
		}
		jgen.writeEndArray();

		//
		// if (value.getUsername() != null)
		// jgen.writeStringField("username", value.getUsername());
		// if (value.getDataTime() != null)
		// jgen.writeNumberField("dataTime", value.getDataTime().getTime());
		//
		// if (value.getAppName() != null)
		// jgen.writeStringField("APPName", value.getAppName());
		// if (value.getAppId() != null)
		// jgen.writeStringField("APPID", value.getAppId());
		// if (value.getMotivation() != null)
		// jgen.writeStringField("motivation", value.getMotivation());
		// if (value.getVariableName() != null)
		// jgen.writeStringField("variableName", value.getVariableName());
		// if (value.getVariableValue() != null)
		// jgen.writeStringField("variableValue", value.getVariableValue());
		// if (value.getVariableUnit() != null)
		// jgen.writeStringField("variableUnit", value.getVariableUnit());

		jgen.writeEndObject();
	}
}