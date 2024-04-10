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

package org.openlmis.prepacking.domain.event;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openlmis.prepacking.domain.BaseEntity;
import org.openlmis.prepacking.domain.event.PrepackingEventLineItem;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "prepacking_event", schema = "prepacking")
public class PrepackingEvent extends BaseEntity {

  @Column(nullable = false, columnDefinition = "timestamp")
  private ZonedDateTime dateCreated;
  private UUID userId;
  @Column(nullable = true, columnDefinition = "timestamp")
  private ZonedDateTime dateAuthorised;
  private UUID facilityId;
  private UUID programId;
  private String comments;
  private UUID supervisoryNodeId;
  private String status;

  // One-to-many relationship with PrepackingEventLineItem
  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinColumn(name = "prepacking_event_id") // foreign key in PrepackingEventLineItem table
  private List<PrepackingEventLineItem> lineItems;

}
