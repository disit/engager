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
package edu.unifi.disit.userprofiler.datamodel;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import edu.unifi.disit.commons.datamodel.DatasetType;
import edu.unifi.disit.commons.datamodel.userprofiler.Log;

public interface LogDAO extends JpaRepository<Log, Long> {

	Log findById(Long id);

	Log findByIdAndDeleteTimeIsNull(Long id);

	List<Log> findByDeleteTimeBefore(Date now);

	List<Log> findByDeviceDeviceIdAndDeleteTimeIsNull(String deviceId);

	Integer countByDeviceDeviceIdAndDeleteTimeIsNull(String deviceId);

	Page<Log> findByDeviceDeviceIdAndDataTimeAfterAndDataTimeBeforeAndDeleteTimeIsNull(String deviceId, Date from, Date to, Pageable pageable);

	Page<Log> findByDeviceDeviceIdAndValueTypeAndDataTimeAfterAndDataTimeBeforeAndDeleteTimeIsNull(String deviceId, String valueType, Date from, Date to, Pageable pageRequest);

	Page<Log> findByDeviceDeviceIdAndDatasetAndDataTimeAfterAndDataTimeBeforeAndDeleteTimeIsNull(String deviceId, DatasetType dataset, Date from, Date to, Pageable pageRequest);

	Page<Log> findByDeviceDeviceIdAndValueTypeAndDatasetAndDataTimeAfterAndDataTimeBeforeAndDeleteTimeIsNull(String deviceId, String valueType, DatasetType dataset, Date from, Date to, Pageable pageRequest);

}
