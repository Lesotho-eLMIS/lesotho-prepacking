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

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map; 
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openlmis.prepacking.domain.BaseEntity;
import org.openlmis.prepacking.domain.ExtraDataEntity;
import org.openlmis.prepacking.domain.ExtraDataEntity.ExtraDataExporter;
import org.openlmis.prepacking.domain.ExtraDataEntity.ExtraDataImporter;
import org.openlmis.prepacking.domain.event.PrepackingEventLineItem;
import org.openlmis.prepacking.domain.status.PrepackingEventStatus;
import org.openlmis.prepacking.domain.status.StatusChange;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "prepacking_event", schema = "prepacking")
public class PrepackingEvent extends BaseEntity {

  @Column(nullable = false, columnDefinition = "timestamp")
  private ZonedDateTime dateCreated;
  private UUID facilityId;
  private UUID programId;
  private String comments;
  private UUID prepackerUserId;
  private String prepackerUserNames;
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  @Getter
  @Setter
  private PrepackingEventStatus status;


  @Getter
  private String draftStatusMessage;

  // One-to-many relationship with PrepackingEventLineItem
  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinColumn(name = "prepacking_event_id") // foreign key in PrepackingEventLineItem table
  private List<PrepackingEventLineItem> lineItems;

  @Embedded
  private ExtraDataEntity extraData = new ExtraDataEntity();

  /**
   * Export this object to the specified exporter (DTO).
   *
   * @param exporter exporter to export to
   */
  public void export(PrepackingEvent.Exporter exporter) {
    exporter.setId(id);
    exporter.setDateCreated(getDateCreated());
    exporter.setStatus(status);
    //exportStatusChanges(exporter);
    exporter.setFacilityId(facilityId);
    //exporter.setSupervisoryNode(supervisoryNodeId);
    exporter.setDraftStatusMessage(draftStatusMessage);

    extraData = ExtraDataEntity.defaultEntity(extraData);
    extraData.export(exporter);
  }

  public interface Exporter extends ExtraDataExporter {
    void setId(UUID id);

    void setDateCreated(ZonedDateTime createdDate);

    void setStatus(PrepackingEventStatus status);

    void setFacilityId(UUID supplyingFacility);

    void setDraftStatusMessage(String draftStatusMessage);

    void addStatusChange(StatusChange.Exporter providedExporter);
  }

  public interface Importer extends ExtraDataImporter {
    UUID getId();

    ZonedDateTime getCreatedDate();

    UUID getFacilityId();

    UUID getProgramId();

    PrepackingEventStatus getStatus();

    String getDraftStatusMessage();

    Map<String,Object> getExtraData();
  }

}



