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
package edu.unifi.disit.userprofiler.ppois;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import edu.unifi.disit.commons.datamodel.MobilitySpeed;
import edu.unifi.disit.commons.datamodel.PPOI;
import edu.unifi.disit.commons.datamodel.Position;
import edu.unifi.disit.userprofiler.ppois.datamodel.UserProfilerLastPPOI;
import edu.unifi.disit.userprofiler.ppois.datamodel.UserProfilerPPOI;
import edu.unifi.disit.userprofiler.ppois.datamodel.UserServiceInterest;

public class DBinterface {

	private static final Logger logger = LogManager.getLogger();
	private SessionFactory sessionFactoryLocal; // engager DB

	private static DBinterface instance = null;

	public static DBinterface getInstance() {
		if (instance == null) {
			synchronized (DBinterface.class) {
				if (instance == null) {
					instance = new DBinterface();
				}
			}
		}
		return instance;
	}

	private DBinterface() {
		this.sessionFactoryLocal = new Configuration().configure("hibernate-local.cfg.xml").buildSessionFactory();
	}

	public void close() throws Throwable {
		if (instance != null) {
			sessionFactoryLocal.close();
			sessionFactoryLocal = null;
			instance = null;
			super.finalize();
		}
	}

	// ----------------------------------------------------------------------------------------------------------------------------------------------------------------new stuff, clean scenario
	@SuppressWarnings("unchecked")
	public UserProfilerPPOI getPPOI(Long id) {

		UserProfilerPPOI toreturn = null;

		Session session = sessionFactoryLocal.openSession();
		Transaction tx = null;

		try {

			tx = session.beginTransaction();

			Criteria criteria = session.createCriteria(UserProfilerPPOI.class).add(Restrictions.like("id", id));
			List<UserProfilerPPOI> ppois = criteria.list();

			if (ppois.size() > 0) {
				toreturn = ppois.get(0);
				if (ppois.size() > 1) {
					logger.warn("seems very strange there is more than one");
				}
			}

			tx.commit();

		} catch (Exception ex) {
			logger.error("cannot getUP due:", ex);
			if (tx != null)
				tx.rollback();
		} finally {
			try {
				session.close();
			} catch (Exception e) {
				logger.error("cannot flush due:", e);
			}
		}

		return toreturn;
	}

	@SuppressWarnings("unchecked")
	private List<UserProfilerPPOI> getUPPOIs(String user_id, String ppoi_name, Boolean wildcard, Boolean confirmed) {
		List<UserProfilerPPOI> toreturn = null;

		Session session = sessionFactoryLocal.openSession();
		Transaction tx = null;

		try {

			tx = session.beginTransaction();

			Criteria criteria = session.createCriteria(UserProfilerPPOI.class).add(Restrictions.like("userid", user_id));

			if ((ppoi_name != null) && (wildcard != null) && (wildcard))
				criteria = criteria.add(Restrictions.like("ppoi", ppoi_name, MatchMode.START));

			if ((ppoi_name != null) && (wildcard != null) && (!wildcard))
				criteria = criteria.add(Restrictions.like("ppoi", ppoi_name));

			// confirmation!=null------------------>specified in query
			// confirmation==null AND valid==true-->not rejected (valid)
			// confirmation==null AND valid==false-->any
			if ((confirmed != null)) // {
				criteria = criteria.add(Restrictions.like("confirm", confirmed));
			// } else {
			// if (valid == true) {
			// criteria = criteria.add(Restrictions.or(Restrictions.isNull("confirm"), Restrictions.not(Restrictions.like("confirm", false))));
			// }
			// }

			if (ppoi_name == null)
				criteria = criteria.add(Restrictions.not(Restrictions.like("ppoi", "null")));

			toreturn = criteria.list();

			tx.commit();

		} catch (Exception ex) {
			logger.error("cannot getUP due:", ex);
			if (tx != null)
				tx.rollback();
		} finally {
			try {
				session.close();
			} catch (Exception e) {
				logger.error("cannot flush due:", e);
			}
		}

		return toreturn;
	}

	public List<PPOI> getAllPPOIs(String user_id, Boolean confirmed) {
		return getPPOIs(user_id, null, null, confirmed);
	}

	public List<PPOI> getPPOIs(String user_id, String ppoi_name, Boolean wildcard, Boolean confirmed) {

		List<PPOI> toreturn = null;

		List<UserProfilerPPOI> ppois = getUPPOIs(user_id, ppoi_name, wildcard, confirmed);

		if ((ppois != null) && (ppois.size() > 0))
			toreturn = new ArrayList<PPOI>();

		for (UserProfilerPPOI ppoi : ppois) {
			toreturn.add(ppoi.toPPOI());// TODO remove this conversion!!!
		}

		return toreturn;
	}

	// return just one
	public PPOI getPPOI(String user_id, String ppoi_name, Boolean wildcard, Boolean confirmed) {
		PPOI toreturn = null;

		List<PPOI> ppois = getPPOIs(user_id, ppoi_name, false, confirmed);

		if ((ppois != null) && (ppois.size() > 0)) {
			toreturn = ppois.get(0);
			if (ppois.size() > 1)
				logger.warn("be were there is more than one " + ppoi_name + " for user " + user_id);
		}

		return toreturn;
	}

	public PPOI updatePPOI(String deviceId, PPOI ppoi) {
		return updatePPOI(new UserProfilerPPOI(deviceId, ppoi)).toPPOI();
	}

	public UserProfilerPPOI updatePPOI(UserProfilerPPOI ppoi) {

		Session session = sessionFactoryLocal.openSession();
		Transaction tx = null;

		try {

			tx = session.beginTransaction();

			session.saveOrUpdate(ppoi);

			tx.commit();

		} catch (Exception ex) {
			logger.error("cannot update due:", ex);
			if (tx != null)
				tx.rollback();
		} finally {
			try {
				session.close();
			} catch (Exception e) {
				logger.error("cannot flush due:", e);
			}
		}

		return ppoi;
	}

	public void deletePPOI(UserProfilerPPOI ppoi) {

		if (ppoi != null) {

			Session session = sessionFactoryLocal.openSession();
			Transaction tx = null;

			try {

				tx = session.beginTransaction();

				session.delete(ppoi);

				tx.commit();

			} catch (Exception ex) {
				logger.error("cannot update due:", ex);
				if (tx != null)
					tx.rollback();
			} finally {
				try {
					session.close();
				} catch (Exception e) {
					logger.error("cannot flush due:", e);
				}
			}
		}
	}

	// ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------local db apis
	@SuppressWarnings("unchecked")
	public List<String> getUsersHasPPOI() {
		List<String> users = new ArrayList<>();

		Session session = sessionFactoryLocal.openSession();
		Transaction tx = null;

		try {

			tx = session.beginTransaction();

			users = session.createCriteria(UserProfilerPPOI.class)
					.add(Restrictions.not(Restrictions.like("ppoi", "null")))
					.setProjection(Projections.distinct(Projections.property("userid")))
					.list();

			tx.commit();

		} catch (Exception ex) {
			logger.error("cannot getUP due:", ex);
			if (tx != null)
				tx.rollback();
		} finally {
			try {
				session.close();
			} catch (Exception e) {
				logger.error("cannot flush due:", e);
			}
		}

		logger.debug("Retrieved n={} users", users.size());

		return users;
	}

	// ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------local db apis/last
	// return the last entry of the user
	// if transaction == true return the "null" ppoi
	// if transaction == falser return the ppoi without null
	@SuppressWarnings("unchecked")
	public UserProfilerLastPPOI getLast(String user_id, boolean transactions) {
		UserProfilerLastPPOI toreturn = null;

		Session session = sessionFactoryLocal.openSession();
		Transaction tx = null;

		try {

			List<UserProfilerLastPPOI> ppoi;

			tx = session.beginTransaction();

			if (transactions) {

				ppoi = session.createCriteria(UserProfilerLastPPOI.class)
						.add(Restrictions.like("userid", user_id))
						.add(Restrictions.like("ppoi", "null"))
						.list();
			} else {
				ppoi = session.createCriteria(UserProfilerLastPPOI.class)
						.add(Restrictions.like("userid", user_id))
						.add(Restrictions.not(Restrictions.like("ppoi", "null")))
						.list();
			}

			tx.commit();

			if (ppoi.size() > 1) {
				logger.warn("Seems very strange, there are more than a LAST ppoi for the user:{} ... anyway we take the first one", user_id);
			}

			if (ppoi.size() == 0) {
				logger.debug("Still there are no LAST poi for the user:{} ", user_id);
			} else {
				logger.debug("There is a LAST poi for the user:{} is:{}", user_id, ppoi.get(0));
				toreturn = ppoi.get(0);
			}

		} catch (Exception ex) {
			logger.error("cannot getLast due:", ex);
			if (tx != null)
				tx.rollback();
		} finally {
			try {
				session.close();
			} catch (Exception e) {
				logger.error("cannot flush due:", e);
			}
		}

		return toreturn;
	}

	public double updateLast(String user_id, String ppoi, Long when, Double distance_tot) {

		Date when_d = new Date(when);
		UserProfilerLastPPOI user_old = null;

		if (ppoi == null) {// we're moving
			ppoi = "null";
			user_old = getLast(user_id, true);
		} else {// we're in ppoi
			user_old = getLast(user_id, false);
		}

		Session session = sessionFactoryLocal.openSession();
		Transaction tx = null;
		try {

			if (user_old == null) {// first run, create new fresh entry
				user_old = new UserProfilerLastPPOI(user_id, ppoi, when_d, distance_tot);
				logger.debug("Created LAST for user:{} with newpoi_name=null date is:{}", user_id, when_d);
			} else {// not first run, update last entry
				if ((ppoi.equals("null") || (!ppoi.equals(user_old.getPpoi())))) // se siamo in transizioni oppure non siamo nello stesso stato, aggiorniamo la distanza
					user_old.setDistance(distance_tot);

				if ((!ppoi.equals("null") && (ppoi.equals(user_old.getPpoi())))) // se non siamo in transizione e siamo nello stesso stato, resettiamo la distanza
					user_old.setDistance(0d);

				user_old.setPpoi(ppoi);
				user_old.setTime(when_d);
				logger.debug("Updated LAST for user:{} with newpoi_name={} date is:{}", user_id, ppoi, when_d);
			}

			logger.trace("begin trans last");
			tx = session.beginTransaction();
			logger.trace("done trans last");

			session.saveOrUpdate(user_old);

			logger.trace("begin commit last");
			tx.commit();
			logger.trace("done commit last");

		} catch (Exception ex) {
			logger.error("cannot updateLast due:", ex);
			if (tx != null)
				tx.rollback();
		} finally {
			try {
				session.close();
			} catch (Exception e) {
				logger.error("cannot flush due:", e);
			}
		}

		return user_old.getDistance();
	}

	// ---------------mobilityspeed
	@SuppressWarnings("unchecked")
	public List<MobilitySpeed> getMobilitySpeed(String deviceId) {

		Session session = sessionFactoryLocal.openSession();
		Transaction tx = null;

		List<MobilitySpeed> toreturn = new ArrayList<MobilitySpeed>();

		try {

			tx = session.beginTransaction();

			toreturn = session.createCriteria(MobilitySpeed.class)
					.add(Restrictions.like("deviceId", deviceId))
					.list();

			tx.commit();

		} catch (Exception ex) {
			logger.error("cannot retrieveLocations due:", ex);
			if (tx != null)
				tx.rollback();
		} finally {
			try {
				// session.flush();
				session.close();
			} catch (Exception e) {
				logger.error("cannot flush due:", e);
			}
		}

		return toreturn;

	}

	public void deleteMobilitySpeed(String deviceId, Date limite) {

		Session session = sessionFactoryLocal.openSession();
		Transaction tx = null;

		try {

			String limite_s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(limite);

			String sqlquery = "DELETE FROM mobility_speed where deviceId='" + deviceId + "' and time<'" + limite_s + "' ;";

			tx = session.beginTransaction();

			SQLQuery query = session.createSQLQuery(sqlquery);

			query.executeUpdate();

			tx.commit();

		} catch (Exception ex) {
			logger.error("cannot retrieveLocations due:", ex);
			if (tx != null)
				tx.rollback();
		} finally {
			try {
				// session.flush();
				session.close();
			} catch (Exception e) {
				logger.error("cannot flush due:", e);
			}
		}
	}

	public void addMobilitySpeed(MobilitySpeed ms) {

		Session session = sessionFactoryLocal.openSession();
		Transaction tx = null;

		try {

			tx = session.beginTransaction();

			session.saveOrUpdate(ms);

			tx.commit();

		} catch (Exception ex) {
			logger.error("cannot retrieveLocations due:", ex);
			if (tx != null)
				tx.rollback();
		} finally {
			try {
				// session.flush();
				session.close();
			} catch (Exception e) {
				logger.error("cannot flush due:", e);
			}
		}
	}

	// -----------------------------
	public void addUserServiceInterest(String userId, String serviceUri, String ppoiType, Integer rating) {

		logger.debug("addUserServiceInterest for {}", userId, serviceUri, ppoiType);

		UserServiceInterest usi = new UserServiceInterest(userId, serviceUri, ppoiType, rating);

		logger.debug("goign to save {}", usi);

		Session session = sessionFactoryLocal.openSession();
		Transaction tx = null;

		try {

			tx = session.beginTransaction();

			session.save(usi);

			tx.commit();

		} catch (Exception ex) {
			logger.error("cannot retrieveLocations due:", ex);
			if (tx != null)
				tx.rollback();
		} finally {
			try {

				session.close();
			} catch (Exception e) {
				logger.error("cannot flush due:", e);
			}
		}

		logger.debug("saved");
	}

	// ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------general
	public String retrievePoiName(String user_id, double latid, double longi, List<PPOI> ppois, float distance) {
		return retrievePoiName(user_id, new Position(latid, longi), ppois, distance);
	}

	public String retrievePoiName(String user_id, Position p, List<PPOI> ppois, float distance) {
		for (PPOI ppoi : ppois) {
			if (ppoi.isClose(p, distance))
				return ppoi.getName();
		}
		return null;
	}

	public Position retrievePosition(List<PPOI> ppois, String ppoiname) {
		// TODO if in the meantime the ppoi situation changed, this never return the correct situation
		// example: in last poi there is SCHOOL, but in the meantime the ppoi is HOME and WORK
		for (PPOI ppoi : ppois) {
			if (ppoi.getName().equals(ppoiname))
				return ppoi;
		}
		return null;
	}

	public String getNewName(String name, List<PPOI> bannedNames) {

		logger.debug("looking for a name for: " + name);
		if (bannedNames != null)
			for (int i = 0; i < bannedNames.size(); i++) {
				logger.debug("bannedNames are: " + bannedNames.get(i));
			}
		else
			return name + "0";

		Boolean available;
		String tentativo;
		int index = 0;
		do {

			available = true;
			tentativo = name.concat(String.valueOf(index));

			for (int i = 0; (i < bannedNames.size()) && (available == true); i++)
				if (bannedNames.get(i).getName().equals(tentativo))
					available = false;

			index++;

		} while (available == false);

		logger.debug("found: " + tentativo);

		return tentativo;
	}

	// return the first close PPOI
	// null otherwise
	public PPOI getClosePPOI(String user_id, String ppoi_name, Position p, Float distance) {
		List<PPOI> ppois = getPPOIs(user_id, ppoi_name, true, null);
		if (ppois != null)
			for (PPOI ppoi : ppois) {
				if (p.isClose(ppoi, distance)) {
					return ppoi;
				}
			}
		return null;
	}
}
