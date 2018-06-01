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
package edu.unifi.disit.wallet_user_mngt.object;

public class ScoreboardEntry implements Comparable<ScoreboardEntry> {

	private String username;

	private Integer points;

	public ScoreboardEntry() {
		super();
	}

	public ScoreboardEntry(String username, Integer points) {
		super();
		this.username = username;
		this.points = points;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Integer getPoints() {
		return points;
	}

	public void setPoints(Integer points) {
		this.points = points;
	}

	@Override
	public String toString() {
		return "ScoreboardEntry [username=" + username + ", points=" + points + "]";
	}

	@Override
	public int compareTo(ScoreboardEntry o) {

		// null condition
		if (this.points == null) {
			if (o.getPoints() == null) {
				return 0;
			} else {
				return 1;
			}
		} else if (o.getPoints() == null) {
			return -1;
		}

		// normal condition
		if (this.points == o.getPoints()) {
			return 0;
		} else if (this.points < o.getPoints()) {
			return 1;
		} else
			return -1;
	}
}
