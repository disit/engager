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

import org.springframework.stereotype.Component;

import edu.unifi.disit.commons.datamodel.PPOI;
import edu.unifi.disit.engager_utils.SampleDataSource;
import edu.unifi.disit.userprofiler.exception.OperationNotPermittedException;
import edu.unifi.disit.userprofiler.exception.PPOIAlreadyExistsException;
import edu.unifi.disit.userprofiler.exception.PPOINotExistsException;
import edu.unifi.disit.userprofiler.ppois.datamodel.UserProfilerPPOI;

@Component
public class DevicePPOIServiceImpl implements IDevicePPOIService {

	private static final edu.unifi.disit.userprofiler.ppois.DBinterface dbi_local = edu.unifi.disit.userprofiler.ppois.DBinterface.getInstance();

	// ------------------------------------------------------------------------------------------------------------------------------get
	@Override
	public List<PPOI> getAllPPOIs(String deviceId, Boolean confirmed, String lang) {
		return dbi_local.getAllPPOIs(deviceId, confirmed);
	}

	@Override
	public List<PPOI> getPPOIs(String deviceId, String ppoiName, Boolean wildcard, Boolean confirmed, String lang) {
		return dbi_local.getPPOIs(deviceId, ppoiName, wildcard, confirmed);
	}

	// ------------------------------------------------------------------------------------------------------------------------------add
	@Override
	public PPOI addPPOI(String deviceId, PPOI ppoi, String lang) throws PPOIAlreadyExistsException {
		if (ppoi.getName().equals(SampleDataSource.USERGENERATED))
			ppoi.setName(dbi_local.getNewName(ppoi.getName(), dbi_local.getPPOIs(deviceId, ppoi.getName(), true, null)));
		return dbi_local.updatePPOI(deviceId, ppoi);
	}

	// ------------------------------------------------------------------------------------------------------------------------------update
	@Override
	public PPOI updatePPOI(String deviceId, PPOI ppoi, String lang) throws PPOINotExistsException {
		// the ppoi has to be exist, since this is an update (otehrwise use a post)
		if (dbi_local.getPPOI(ppoi.getId()) == null)
			throw new PPOINotExistsException("the " + ppoi.getId() + " is not found for user " + deviceId + " ... please use an HTTP POST");
		return dbi_local.updatePPOI(deviceId, ppoi);
	}

	@Override
	public void updateLabelPPOI(String deviceId, Long id, String label, String lang) throws PPOINotExistsException {
		UserProfilerPPOI ppoi = dbi_local.getPPOI(id);
		if (ppoi == null)
			throw new PPOINotExistsException("the " + id + " is not found for user " + deviceId);
		ppoi.setLabel(label);
		dbi_local.updatePPOI(ppoi);
	}

	@Override
	public void updateConfirmationPPOI(String deviceId, Long id, Boolean confirmation, String lang) throws PPOINotExistsException {
		UserProfilerPPOI ppoi = dbi_local.getPPOI(id);
		if (ppoi == null)
			throw new PPOINotExistsException("the " + id + " is not found for user " + deviceId);
		ppoi.setConfirm(confirmation);
		dbi_local.updatePPOI(ppoi);
	}

	// ------------------------------------------------------------------------------------------------------------------------------delete
	@Override
	public void deletePPOI(String deviceId, Long id, String lang) throws PPOINotExistsException, OperationNotPermittedException {
		UserProfilerPPOI ppoi = dbi_local.getPPOI(id);

		if (ppoi == null)
			throw new PPOINotExistsException("the " + id + " is not found for user " + deviceId);

		dbi_local.deletePPOI(ppoi);
	}
}
