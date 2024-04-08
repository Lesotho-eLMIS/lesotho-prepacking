/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org.
 */

package org.openlmis.pointofdelivery.service.requisition;

import java.util.UUID;
import org.openlmis.pointofdelivery.dto.requisition.RejectionReasonDto;
import org.springframework.stereotype.Service;

@Service
public class RejectionReasonService extends BaseRequisitionService<RejectionReasonDto> {  


  @Override
  protected String getUrl() {
    return "/api/rejectionReasons/";
  }
  
  @Override
  protected Class<RejectionReasonDto> getResultClass() {
    return RejectionReasonDto.class;
  }
  
  @Override
  protected Class<RejectionReasonDto[]> getArrayResultClass() {
    return RejectionReasonDto[].class;
  }

  /**
   * Return one rejection reason from the requisition service.
   *
   * @param id UUID of rejection reason.
   * @return Requesting requisition data object - rejection reason.
   */
  public RejectionReasonDto getRejectionReason(UUID id) {
    return findOne(id);
  }

}
