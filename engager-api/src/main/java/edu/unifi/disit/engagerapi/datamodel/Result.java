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
package edu.unifi.disit.engagerapi.datamodel;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import disit.engager_base.ACTION;
import edu.unifi.disit.engager_utils.SampleDataSource;

public class Result {

	private static final Logger logger = LogManager.getLogger("Result");

	boolean assessor = false;

	List<ACTION> engagement;
	List<ACTION> assistance;

	public Result() {
		super();
		this.engagement = new ArrayList<ACTION>();
		this.assistance = new ArrayList<ACTION>();
		this.assessor = false;
	}

	public Result(List<ACTION> engagement, List<ACTION> assistance, boolean assessor) {
		super();
		this.engagement = engagement;
		this.assistance = assistance;
		this.assessor = assessor;
	}

	public List<ACTION> getEngagement() {
		return engagement;
	}

	public void setEngagement(List<ACTION> engagement) {
		this.engagement = engagement;
	}

	public List<ACTION> getAssistance() {
		return this.assistance;
	}

	public void setAssistance(List<ACTION> assistance) {
		this.assistance = assistance;
	}

	public List<ACTION> get(String type) {
		if (type.equals(SampleDataSource.ENGAGEMENT))
			return this.getEngagement();
		else if (type.equals(SampleDataSource.ASSISTANCE))
			return this.getAssistance();
		else
			logger.error("selection not recognized:{}", type);
		return new ArrayList<ACTION>();
	}

	public void set(List<ACTION> actions, String type) {
		if (type.equals(SampleDataSource.ENGAGEMENT))
			this.setEngagement(actions);
		else if (type.equals(SampleDataSource.ASSISTANCE))
			this.setAssistance(actions);
		else
			logger.error("selection not recognized:{}", type);
	}

	@Override
	public String toString() {
		return "Result [assessor=" + assessor + ", engagement=" + engagement + ", assistance=" + assistance + "]";
	}

	public boolean isAssessor() {
		return assessor;
	}

	public void setAssessor(boolean assessor) {
		this.assessor = assessor;
	}
}
