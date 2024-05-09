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

package org.openlmis.prepacking.dto;

import static java.time.ZonedDateTime.now;
import static java.util.Collections.emptyList;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.openlmis.prepacking.domain.ExtraDataEntity;
import org.openlmis.prepacking.domain.event.PrepackingEvent;
import org.openlmis.prepacking.domain.event.PrepackingEventLineItem;
import org.openlmis.prepacking.domain.status.PrepackingEventStatus;
import org.openlmis.prepacking.dto.PrepackingEventLineItemDto;
import org.openlmis.prepacking.util.PrepackingEventProcessContext;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class PrepackingEventDto {

  private UUID id;

  private ZonedDateTime dateCreated;
  private UUID facilityId;
  private UUID programId;
  private String comments;
  private PrepackingEventStatus status;
  private String draftStatusMessage;
  private String remarks;
  private List<PrepackingEventLineItemDto> lineItems;
  private ExtraDataEntity extraData = new ExtraDataEntity();

  private PrepackingEventProcessContext context;

  /**
   * Convert dto to jpa model.
   *
   * @return the converted jpa model object.
   */
  public PrepackingEvent toPrepackingEvent() {
    PrepackingEvent prepackingEvent = new PrepackingEvent(
        now(), facilityId, programId, comments, status,draftStatusMessage,lineItems(),extraData);
    return prepackingEvent;
  }

  /**
   * Gets lineItems as {@link PrepackingEventLineItem}.
   */
  public List<PrepackingEventLineItem> lineItems() {
    if (null == lineItems) {
      return emptyList();
    }

    List<PrepackingEventLineItem> lineItemsList = new ArrayList<>();
    for (PrepackingEventLineItemDto lineItemdto : lineItems) {
      lineItemsList.add(lineItemdto.toPrepackingEventLineItem());
    }
    return lineItemsList;
  }

}
