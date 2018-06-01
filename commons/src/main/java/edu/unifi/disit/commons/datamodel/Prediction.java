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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Prediction implements java.io.Serializable {

	private static final long serialVersionUID = -1976741427057901825L;
	String name;
	Float accuracy;
	int howmany;

	Long duration = null;
	Double distance = null;
	List<String> modality = null;

	public Prediction() {
		this("UNKOWN", 0f, 0, 0l, 0d, new ArrayList<String>());
	}

	public Prediction(String name) {
		this(name, 0f, 0, 0l, 0d, new ArrayList<String>());
	}

	public Prediction(String name, Float accuracy, int howmany, Long duration, Double distance, List<String> modality) {
		this.name = name;
		this.accuracy = accuracy;
		this.howmany = howmany;
		this.duration = duration;
		this.distance = distance;
		this.modality = modality;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Float getAccuracy() {
		return accuracy;
	}

	public void setAccuracy(Float accuracy) {
		this.accuracy = accuracy;
	}

	public int getHowmany() {
		return howmany;
	}

	public void setHowmany(int howmany) {
		this.howmany = howmany;
	}

	public Long getDuration() {
		return duration;
	}

	public void setDuration(Long duration) {
		this.duration = duration;
	}

	public Double getDistance() {
		return distance;
	}

	public void setDistance(Double distance) {
		this.distance = distance;
	}

	public List<String> getModality() {
		return modality;
	}

	public void setModality(List<String> modality) {
		this.modality = modality;
	}

	@Override
	public String toString() {

		String toReturn = "Prediction [";

		if (name != null)
			toReturn = toReturn + "name=" + name + ", ";

		if (accuracy != null)
			toReturn = toReturn + "accuracy=" + accuracy + ", ";

		toReturn = toReturn + "howmany=" + howmany + ", ";

		if (duration != null)
			toReturn = toReturn + "duration=" + getDuration() + ", ";

		if (distance != null)
			toReturn = toReturn + "duration=" + getDistance() + ", ";

		if ((modality != null) && (modality.size() > 0))
			toReturn = toReturn + "modality=" + getModality();

		return toReturn + "]";
	}

	public void observe(Long duration, Double distance, String modality) {
		this.howmany = howmany + 1;
		this.duration += duration;
		this.distance += distance;
		this.modality.add(modality);
	}

	public void updateAccuracy(int nObservation) {
		this.accuracy = (this.howmany / ((float) nObservation));
	}

	@JsonIgnore
	public Long getMeanDuration() {
		if (duration != null)
			return duration / howmany;
		else
			return null;
	}

	@JsonIgnore
	public Double getMeanDistance() {
		if (distance != null)
			return distance / howmany;
		else
			return null;
	}

	@JsonIgnore
	public String getMeanModality() {

		if ((modality != null) && (modality.size() > 0)) {

			Hashtable<String, Integer> count = new Hashtable<String, Integer>();

			int max = 0;
			String maxS = null;

			for (String s : modality) {
				Integer i = count.get(s);
				if (i == null)
					i = 0;
				count.put(s, i + 1);

				if (i >= max) {
					maxS = s;
					max = i;
				}
			}

			return maxS;
		} else
			return null;
	}
}
