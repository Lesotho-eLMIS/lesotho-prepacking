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

package org.openlmis.pointofdelivery.domain.event;

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
import org.openlmis.pointofdelivery.domain.BaseEntity;
import org.openlmis.pointofdelivery.domain.qualitychecks.Discrepancy;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "point_of_delivery_event", schema = "pointofdelivery")
public class PointOfDeliveryEvent extends BaseEntity {
    
  private UUID sourceId;
  private String sourceFreeText;

  private UUID destinationId;
  private String destinationFreeText;

  private UUID receivedByUserId;
  private String receivedByUserNames;

  @Column(nullable = false, columnDefinition = "timestamp")
  private ZonedDateTime receivingDate;

  private String referenceNumber;

  @Column(nullable = false, columnDefinition = "timestamp")
  private LocalDate packingDate;

  private String packedBy;

  private Integer cartonsQuantityOnWaybill;

  private Integer cartonsQuantityShipped;

  private Integer cartonsQuantityAccepted;

  private Integer cartonsQuantityRejected;

  private Integer containersQuantityOnWaybill;

  private Integer containersQuantityShipped;

  private Integer containersQuantityAccepted;

  private Integer containersQuantityRejected;

  private String remarks;

  // One-to-many relationship with Discrepancy
  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinColumn(name = "point_of_delivery_event_id") // foreign key in Discrepancy table
  private List<Discrepancy> discrepancies;

}
