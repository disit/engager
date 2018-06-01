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
package edu.unifi.disit.engager_utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class SampleDataSource {

	public static String NULL = "null";

	// ----------------------------------------------------------------------------------------GROUPS TYPE
	public static String GROUP_WINNER_USB = "GROUP_WINNER_USB";
	public static String GROUP_SIIMOBILITY_PILOTA = "GROUP_SIIMOBILITY_PILOTA";
	public static String GROUP_WALLET_REGISTERED = "GROUP_WALLET_REGISTERED";

	public List<String> getListOfGroupType() {
		List<String> d = new ArrayList<String>();
		d.add(GROUP_WINNER_USB);
		d.add(GROUP_SIIMOBILITY_PILOTA);
		d.add(GROUP_WALLET_REGISTERED);
		return d;
	}

	// ----------------------------------------------------------------------------------------PPOITYPE
	public static String HOME = "HOME";
	public static String WORK = "WORK";
	public static String SCHOOL = "SCHOOL";
	public static String PPOI = "PPOI";
	public static String SPENT_TIME = "SPENT_TIME";
	public static String USERGENERATED = "USERGENERATED";

	public List<String> getListOfPPOIType() {
		List<String> d = new ArrayList<String>();
		d.add(HOME);
		d.add(WORK);
		d.add(SCHOOL);
		d.add(PPOI);
		d.add(SPENT_TIME);
		d.add(NULL);
		return d;
	}

	// ----------------------------------------------------------------------------------------TIMETYPE
	public static String NIGHT = "NIGHT";
	public static String EARLY_MORNING = "EARLY_MORNING";
	public static String MORNING = "MORNING";
	public static String LATE_MORNING = "LATE_MORNING";
	public static String LUNCH = "LUNCH";
	public static String AFTERNOON = "AFTERNOON";
	public static String DINNER = "DINNER";
	public static String EVENING = "EVENING";

	public List<String> getListOfDaySlotType() {
		List<String> d = new ArrayList<String>();
		d.add(MORNING);
		d.add(EARLY_MORNING);
		d.add(MORNING);
		d.add(LATE_MORNING);
		d.add(LUNCH);
		d.add(AFTERNOON);
		d.add(EVENING);
		d.add(NIGHT);
		return d;
	}

	public static final String retrieveDaySlot(Long milliseconds) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(milliseconds);
		return retrieveDaySlot(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
	}

	// 8slots
	public static final String retrieveDaySlot(Integer hours, Integer minutes) {
		if (hours < 5)
			return SampleDataSource.NIGHT;
		else if ((hours >= 5) && (hours < 8))
			return SampleDataSource.MORNING;
		else if ((hours >= 8) && (hours < 10))
			return SampleDataSource.MORNING;
		else if (((hours >= 10) && (hours < 12)) ||
				((minutes < 30) && (hours == 12)))
			return SampleDataSource.LUNCH;
		else if (((hours >= 13) && (hours < 14)) ||
				((minutes >= 30) && (hours == 12)))
			return SampleDataSource.LUNCH;
		else if (((hours >= 14) && (hours < 18)) ||
				((minutes < 30) && (hours == 18)))
			return SampleDataSource.AFTERNOON;
		else if (((hours >= 19) && (hours < 21)) ||
				((minutes >= 30) && (hours == 18)))
			return SampleDataSource.AFTERNOON;
		else if (hours >= 21)
			return SampleDataSource.EVENING;
		else
			return SampleDataSource.NULL;
	}
	// public static final String retrieveDaySlot(Integer hours, Integer minutes) {
	// if (hours < 5)
	// return SampleDataSource.NIGHT;
	// else if ((hours >= 5) && (hours < 8))
	// return SampleDataSource.EARLY_MORNING;
	// else if ((hours >= 8) && (hours < 10))
	// return SampleDataSource.MORNING;
	// else if (((hours >= 10) && (hours < 12)) ||
	// ((minutes < 30) && (hours == 12)))
	// return SampleDataSource.LATE_MORNING;
	// else if (((hours >= 13) && (hours < 14)) ||
	// ((minutes >= 30) && (hours == 12)))
	// return SampleDataSource.LUNCH;
	// else if (((hours >= 14) && (hours < 18)) ||
	// ((minutes < 30) && (hours == 18)))
	// return SampleDataSource.AFTERNOON;
	// else if (((hours >= 19) && (hours < 21)) ||
	// ((minutes >= 30) && (hours == 18)))
	// return SampleDataSource.DINNER;
	// else if (hours >= 21)
	// return SampleDataSource.EVENING;
	// else
	// return SampleDataSource.NULL;
	// }

	public static final Integer retrieveDaySlotInteger(Long milliseconds) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(milliseconds);
		return retrieveDaySlotInteger(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
	}

	public static final Integer retrieveDaySlotInteger(Integer hours, Integer minutes) {
		if (hours < 5)
			return 0;
		else if ((hours >= 5) && (hours < 8))
			return 1;
		else if ((hours >= 8) && (hours < 10))
			return 1;
		else if (((hours >= 10) && (hours < 12)) ||
				((minutes < 30) && (hours == 12)))
			return 2;
		else if (((hours >= 13) && (hours < 14)) ||
				((minutes >= 30) && (hours == 12)))
			return 2;
		else if (((hours >= 14) && (hours < 18)) ||
				((minutes < 30) && (hours == 18)))
			return 3;
		else if (((hours >= 19) && (hours < 21)) ||
				((minutes >= 30) && (hours == 18)))
			return 3;
		else if (hours >= 21)
			return 4;
		else
			return -1;
	}

	// 8 slots
	// public static final Integer retrieveDaySlotInteger(Integer hours, Integer minutes) {
	// if (hours < 5)
	// return 0;
	// else if ((hours >= 5) && (hours < 8))
	// return 1;
	// else if ((hours >= 8) && (hours < 10))
	// return 2;
	// else if (((hours >= 10) && (hours < 12)) ||
	// ((minutes < 30) && (hours == 12)))
	// return 3;
	// else if (((hours >= 13) && (hours < 14)) ||
	// ((minutes >= 30) && (hours == 12)))
	// return 4;
	// else if (((hours >= 14) && (hours < 18)) ||
	// ((minutes < 30) && (hours == 18)))
	// return 5;
	// else if (((hours >= 19) && (hours < 21)) ||
	// ((minutes >= 30) && (hours == 18)))
	// return 6;
	// else if (hours >= 21)
	// return 7;
	// else
	// return -1;
	// }

	// ----------------------------------------------------------------------------------------USERTYPE
	public List<String> getListOfProfileType() {
		List<String> d = new ArrayList<String>();
		d.add("all");
		d.add("commuter");
		d.add("student");
		d.add("tourist");
		d.add("citizen");
		d.add("operator");
		d.add("disabled");
		return d;
	}

	public List<String> getListOfSupportedLanguages() {
		List<String> d = new ArrayList<String>();
		d.add(Locale.ITALIAN.toString());
		d.add(Locale.ENGLISH.toString());
		d.add(Locale.FRENCH.toString());
		d.add(Locale.GERMAN.toString());
		d.add("es");
		return d;
	}

	public static String PARKING = "PARKING";
	public static String IN_MOBILITY = "IN_MOBILITY";

	public List<String> getListOfSwitchMobilityMode() {
		List<String> d = new ArrayList<String>();
		d.add(NULL);
		d.add(PARKING);
		d.add(IN_MOBILITY);
		return d;
	}

	public List<String> getListOfMobilityMode() {
		List<String> d = new ArrayList<String>();
		d.add("STATIONARY");
		d.add("WALK");
		d.add("RUNNING");
		d.add("BIKE");
		d.add("MOTORBIKE");
		d.add("BUS-LOCAL");
		d.add("BUS-REGIONAL/NATIONAL");
		d.add("TRAM");
		d.add("CAR");
		d.add("TRAIN");
		d.add("AEROPLANE");
		d.add("FERRY");
		return d;
	}

	// ----------------------------------------------------------------------------------------ACTIONTYPE
	public static String ENGAGEMENT = "ENGAGEMENT";
	public static String ASSISTANCE = "ASSISTANCE";

	public List<String> getListOfActionClasse() {
		List<String> d = new ArrayList<String>();
		d.add(ENGAGEMENT);
		d.add(ASSISTANCE);
		return d;
	}

	public static String SHOW = "SHOW";
	public static String REQUEST_PHOTO = "REQUEST_PHOTO";
	public static String SURVEY = "SURVEY";

	public List<String> getListOfActionType() {
		// Map<String, List<String>> data = new HashMap<String, List<String>>();
		List<String> d = new ArrayList<String>();
		d.add(SHOW);
		d.add(REQUEST_PHOTO);
		d.add(SURVEY);
		// data.put("Fact.field", d);
		return d;
	}

	// public static String EVENTID_STAY = "stay";
	//
	// public List<String> getListOfEventType() {
	// List<String> d = new ArrayList<String>();
	// d.add(EVENTID_STAY);
	// d.add("walk");
	// d.add("car-moto-bus");
	// d.add("car-moto-train");
	// d.add("car-train");
	// d.add("train");
	// d.add("airplain");
	// return d;
	// }
	public static String EVENTID_STAY = "Stay";
	public static String EVENTID_WALK = "Walk";
	public static String EVENTID_PRIVATE_TRANSPORT = "PrivateTransport";
	public static String EVENTID_PUBLIC_TRANSPORT = "PublicTransport";

	public List<String> getListOfEventType() {
		List<String> d = new ArrayList<String>();
		d.add(EVENTID_STAY);
		d.add(EVENTID_WALK);
		d.add(EVENTID_PRIVATE_TRANSPORT);
		d.add(EVENTID_PUBLIC_TRANSPORT);
		return d;
	}

	// ----------------------------------------------------------------------------------------TUSCANY PROVINCE
	public static String TUSCANY_PROVINCE_AREZZO = "AREZZO";
	public static String TUSCANY_PROVINCE_FIRENZE = "FIRENZE";
	public static String TUSCANY_PROVINCE_GROSSETO = "GROSSETO";
	public static String TUSCANY_PROVINCE_LIVORNO = "LIVORNO";
	public static String TUSCANY_PROVINCE_LUCCA = "LUCCA";
	public static String TUSCANY_PROVINCE_MASSACARRARA = "MASSA-CARRARA";
	public static String TUSCANY_PROVINCE_PISA = "PISA";
	public static String TUSCANY_PROVINCE_PISTOIA = "PISTOIA";
	public static String TUSCANY_PROVINCE_PRATO = "PRATO";
	public static String TUSCANY_PROVINCE_SIENA = "SIENA";

	public List<String> getListOfTuscanyProvinceType() {
		List<String> d = new ArrayList<String>();
		d.add(TUSCANY_PROVINCE_AREZZO);
		d.add(TUSCANY_PROVINCE_FIRENZE);
		d.add(TUSCANY_PROVINCE_GROSSETO);
		d.add(TUSCANY_PROVINCE_LIVORNO);
		d.add(TUSCANY_PROVINCE_LUCCA);
		d.add(TUSCANY_PROVINCE_MASSACARRARA);
		d.add(TUSCANY_PROVINCE_PISA);
		d.add(TUSCANY_PROVINCE_PISTOIA);
		d.add(TUSCANY_PROVINCE_PRATO);
		d.add(TUSCANY_PROVINCE_SIENA);
		return d;
	}

}
