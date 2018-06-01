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

import java.util.List;

import org.springframework.stereotype.Service;

import edu.unifi.disit.commons.datamodel.PPOI;
import edu.unifi.disit.userprofiler.exception.OperationNotPermittedException;
import edu.unifi.disit.userprofiler.exception.PPOIAlreadyExistsException;
import edu.unifi.disit.userprofiler.exception.PPOINotExistsException;

@Service
public interface IDevicePPOIService {

	List<PPOI> getAllPPOIs(String deviceId, Boolean confirmated, String lang);

	List<PPOI> getPPOIs(String deviceId, String ppoiName, Boolean wildcard, Boolean confirmed, String lang);

	void updateConfirmationPPOI(String deviceId, Long id, Boolean confirmation, String lang) throws PPOINotExistsException;

	void updateLabelPPOI(String deviceId, Long id, String label, String lang) throws PPOINotExistsException;

	PPOI updatePPOI(String deviceId, PPOI ppoi, String lang) throws PPOINotExistsException;

	PPOI addPPOI(String deviceId, PPOI ppoi, String lang) throws PPOIAlreadyExistsException;

	void deletePPOI(String deviceId, Long id, String lang) throws PPOINotExistsException, OperationNotPermittedException;
}
