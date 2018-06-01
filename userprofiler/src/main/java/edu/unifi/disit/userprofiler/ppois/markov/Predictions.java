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
package edu.unifi.disit.userprofiler.ppois.markov;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.unifi.disit.commons.datamodel.Prediction;

public class Predictions implements java.io.Serializable {

	private static final long serialVersionUID = -6273397261253666352L;
	private static final Logger logger = LogManager.getLogger("Predictions");

	int nTotalObservation;
	List<String> labels = new ArrayList<String>();
	HashMap<Integer, Prediction> predictions;

	public Predictions(List<String> labels) {
		nTotalObservation = 0;
		this.labels = labels;
		predictions = new HashMap<Integer, Prediction>();
		for (int i = 0; i < labels.size(); i++)
			predictions.put(i, new Prediction(labels.get(i)));
	}

	public String toString() {
		String toreturn = new String();
		toreturn = toreturn.concat("nObs" + nTotalObservation + " ");
		for (Integer key : predictions.keySet())
			toreturn = toreturn.concat(key + "(" + predictions.get(key).toString() + ") ");
		return toreturn;
	}

	public void addObservation(String obsString, Long time, Double distance, String modality) throws Exception {

		if (obsString == null) {
			logger.error("passed observation is null");
			throw new Exception("passed observation is null");
		}

		int obs = labels.indexOf(obsString);

		if (obs == -1) {
			logger.error("passed observation is not valid ({})", obs);
			throw new Exception("passed observation is not valid: " + obs);
		}

		// increment number of total observation
		nTotalObservation = nTotalObservation + 1;

		// retrieve the current prediction
		Prediction p = predictions.get(obs);

		// update the current prediction
		p.observe(time, distance, modality);

		// put back the current prediction
		predictions.put(obs, p);// TODO needed?

		// update accurancy of all
		updateAccuracy();
	}

	// can return null, if there where no observation yet
	public Prediction getMost() {
		Prediction bestPred = null;
		for (Integer key : predictions.keySet()) {
			Prediction p = predictions.get(key);
			if ((bestPred == null) || (p.getHowmany() > bestPred.getHowmany()))
				bestPred = p;
		}
		return bestPred;
	}

	private void updateAccuracy() {
		for (Integer key : predictions.keySet()) {
			Prediction p = predictions.get(key);
			p.updateAccuracy(nTotalObservation);
		}
	}
}
