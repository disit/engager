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
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import edu.unifi.disit.commons.datamodel.Prediction;

@JsonSerialize(using = MarkovNetworkSerializer.class)
public class MarkovNetwork implements java.io.Serializable {

	private static final long serialVersionUID = 2206648034826702853L;
	private static final Logger logger = LogManager.getLogger("MarkovNetwork");

	HashMap<List<Integer>, Predictions> predictionMatrix;
	List<String> statesLabel;

	public MarkovNetwork(List<Integer> constrainsSize, List<String> statesLabel) throws Exception {

		// constrainSize has to exist
		if ((constrainsSize == null) || (statesLabel == null)) {
			logger.error("passed constrainSize/stateLabel is null");
			throw new Exception("passed constrainSize/stateLabel is null");
		}
		constrainsSize.add(0, statesLabel.size());

		int nConstrains = constrainsSize.size();

		// at list one constrain has to be present
		if (nConstrains == 0) {
			logger.error("at least one constrain has to be present");
			throw new Exception("at least one constrain has to be present");
		}

		// // statesLabel has be the same of constrains.get(0)
		// if (constrainsSize.get(0) != statesLabel.size()) {
		// logger.error("states label have wrong size");
		// throw new Exception("states label have wrong size");
		// }

		logger.debug("Creating predictionMatrix with {} constrains", nConstrains);
		this.statesLabel = statesLabel;

		// create empty constrain
		List<Integer> constrain = new ArrayList<Integer>();
		for (int i = 0; i < nConstrains; i++) {
			constrain.add(new Integer(0));
		}

		// create all combination of constrains
		List<List<Integer>> allComboConstrain = insert(new Integer(0), constrainsSize, constrain, new ArrayList<List<Integer>>());

		// created empty predictionMatrix
		predictionMatrix = new HashMap<List<Integer>, Predictions>();
		for (List<Integer> r : allComboConstrain) {
			predictionMatrix.put(r, new Predictions(statesLabel));
		}

	}

	public void observe(String obsString, String condString, List<Integer> condizioni, Long duration, Double distance, String modality) throws Exception {

		Integer obs = statesLabel.indexOf(obsString);

		if (obs == -1) {
			logger.error("passed obs is not correct:");
			throw new Exception("passed obs is not correct");
		}

		Integer cond = statesLabel.indexOf(condString);

		if (cond == -1) {
			logger.error("passed obs is not correct:");
			throw new Exception("passed obs is not correct");
		}

		List<Integer> myCond = new ArrayList<Integer>(condizioni);
		myCond.add(0, cond);

		Predictions p = predictionMatrix.get(myCond);
		if (p != null)
			p.addObservation(obsString, duration, distance, modality);
		else {
			logger.error("passed condizioni are not correct:");
			throw new Exception("passed condizioni are not correct");
		}
	}

	public Predictions predict(String obsString, List<Integer> condizioni) throws Exception {
		Integer obs = statesLabel.indexOf(obsString);

		if (obs == -1) {
			logger.error("passed obs is not correct:");
			throw new Exception("passed obs is not correct");
		}

		List<Integer> myCond = new ArrayList<Integer>(condizioni);
		myCond.add(0, obs);

		Predictions p = predictionMatrix.get(myCond);
		if (p != null)
			return p;
		else {
			logger.error("passed condizioni are not correct:");
			throw new Exception("passed condizioni are not correct");
		}
	}

	public Prediction predictMost(String obsString, List<Integer> condizioni) throws Exception {
		Integer obs = statesLabel.indexOf(obsString);

		if (obs == -1) {
			logger.warn("searching {} in", obsString);
			for (String s : statesLabel)
				logger.warn("- {}", s);
			throw new Exception("passed obs is not correct");
		}

		List<Integer> myCond = new ArrayList<Integer>(condizioni);
		myCond.add(0, obs);

		Prediction p = predictionMatrix.get(myCond).getMost();
		if (p != null)
			return p;
		else {
			logger.error("passed condizioni are not correct:");
			throw new Exception("passed condizioni are not correct");
		}
	}

	private List<List<Integer>> insert(Integer profondita, List<Integer> tutto, List<Integer> temp, List<List<Integer>> toreturn) {
		if (profondita == (tutto.size())) {
			toreturn.add(new ArrayList<>(temp));
		} else {
			for (int i = 0; i < tutto.get(profondita); i++) {
				temp.set(profondita, new Integer(i));
				toreturn = insert(new Integer(profondita + 1), tutto, new ArrayList<>(temp), toreturn);
			}
		}
		return toreturn;
	}

	public String toString() {
		String toreturn = new String();
		toreturn = toreturn.concat("------------------------------------------\n");
		Set<List<Integer>> keys = predictionMatrix.keySet();
		for (List<Integer> key : keys) {
			toreturn = toreturn.concat(key.toString());
			toreturn = toreturn.concat(" --> ");
			toreturn = toreturn.concat(predictionMatrix.get(key).toString()).concat("\n");
		}
		toreturn = toreturn.concat("------------------------------------------\n");
		return toreturn;
	}

	protected HashMap<List<Integer>, Predictions> getPredictionMatrix() {
		return predictionMatrix;
	}

	protected List<String> getStatesLabel() {
		return this.statesLabel;
	}
}
