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
package edu.unifi.disit.userprofiler.service;

import javax.annotation.PreDestroy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rosuda.JRI.Rengine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import edu.unifi.disit.commons.datamodel.MobilityTrainingData;

@Component
public class RPredictionServiceImpl implements IRPredictionService {

	private static final Logger logger = LogManager.getLogger();

	Rengine rengine = null;

	@Autowired
	public RPredictionServiceImpl(@Value("${r_models_folder}") String RmodelsFolder) {

		rengine = new Rengine(new String[] { "--no-save" }, false, null);
		// rengine.DEBUG = 100;
		rengine.eval("library(extraTrees)");
		rengine.eval("load(\"" + RmodelsFolder + "modello.R_TRUE_TRUE_TRUE\")");
		rengine.eval("modello_TRUE_TRUE_TRUE=trainMod");
		rengine.eval("load(\"" + RmodelsFolder + "modello.R_TRUE_TRUE_FALSE\")");
		rengine.eval("modello_TRUE_TRUE_FALSE=trainMod");
		rengine.eval("load(\"" + RmodelsFolder + "modello.R_TRUE_FALSE_TRUE\")");
		rengine.eval("modello_TRUE_FALSE_TRUE=trainMod");
		rengine.eval("load(\"" + RmodelsFolder + "modello.R_TRUE_FALSE_FALSE\")");
		rengine.eval("modello_TRUE_FALSE_FALSE=trainMod");
		rengine.eval("load(\"" + RmodelsFolder + "modello.R_FALSE_TRUE_TRUE\")");
		rengine.eval("modello_FALSE_TRUE_TRUE=trainMod");
		rengine.eval("load(\"" + RmodelsFolder + ".R_FALSE_TRUE_FALSE\")");
		rengine.eval("modello_FALSE_TRUE_FALSE=trainMod");
		rengine.eval("load(\"" + RmodelsFolder + "modello.R_FALSE_FALSE_TRUE\")");
		rengine.eval("modello_FALSE_FALSE_TRUE=trainMod");
		rengine.eval("load(\"" + RmodelsFolder + "modello.R_FALSE_FALSE_FALSE\")");
		rengine.eval("modello_FALSE_FALSE_FALSE=trainMod");
	}

	@Override
	public String predict(MobilityTrainingData td) {

		String toreturn = "unknown";

		try {

			rengine.eval("vector=c(" + td.getScenarioColumnsValue() + ")");
			rengine.eval("test=matrix(data=vector, nrow =1, ncol=length(vector))");
			rengine.eval("colnames(test)=c(" + td.getScenarioColumnsName() + ")");
			rengine.eval("test=as.data.frame(test)");
			rengine.eval("prediction <- predict(modello_" + td.getScenario() + " , test[,-2])");

			toreturn = rengine.eval("prediction").asFactor().at(0);

		} catch (Exception e) {
			logger.error("cannot predict due:{}", e.toString());
			logger.error(e);
		}

		return toreturn;
	}

	@PreDestroy
	public void close() {
		rengine.end();
	}
}
