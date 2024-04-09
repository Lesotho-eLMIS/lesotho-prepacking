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

package org.openlmis.prepacking.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.openlmis.prepacking.domain.event.PrepackingEvent;
import org.openlmis.prepacking.domain.event.PrepackingEventLineItem;
import org.openlmis.prepacking.dto.PrepackingEventLineItemDto;
import org.openlmis.prepacking.dto.PrepackingEventDto;
import org.openlmis.prepacking.exception.ResourceNotFoundException;
import org.openlmis.prepacking.repository.PrepackingEventsRepository;
import org.openlmis.prepacking.service.requisition.RejectionReasonService;
import org.openlmis.prepacking.util.Message;
import org.openlmis.prepacking.util.PrepackingEventProcessContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PrepackingService {
  private static final Logger LOGGER = LoggerFactory.getLogger(PrepackingService.class);

  @Autowired
  private PrepackingEventsRepository prepackingEventsRepository;

  @Autowired
  private PrepackingEventProcessContextBuilder contextBuilder;

  @Autowired
  private RejectionReasonService rejectionReasonService;

  /**
   * Get a list of Point of Delivery events.
   *
   * @param destinationId destination id.
   * @return a list of pod events.
   */
  public List<PrepackingEventDto> getPrepackingEventsByDestinationId(UUID destinationId) {
    List<PrepackingEvent> prepackingEvents = prepackingEventsRepository
        .findByDestinationId(destinationId);

    if (prepackingEvents == null) {
      return Collections.emptyList();
    }
    return prepackingToDto(prepackingEvents);
  }

  /**
   * Get a Point of Delivery event by id.
   *
   * @param id point of delivery event id.
   * @return a pod event.
   */
  public Optional<PrepackingEvent> getPrepackingEventById(UUID id) {
    return prepackingEventsRepository.findById(id);
  }

  /**
   * Save or update POD.
   *
   * @param dto POD event dto.
   * @return the saved POD event.
   */
  public PrepackingEventDto updatePrepackingEvent(PrepackingEventDto dto, UUID id) {
    // LOGGER.info("update POS event");
    // physicalInventoryValidator.validateDraft(dto, id);
    // checkPermission(dto.getProgramId(), dto.getFacilityId());

    // checkIfDraftExists(dto, id);

    LOGGER.info("Attempting to fetch pod event with id = " + id);
    Optional<PrepackingEvent> existingPodEventOpt = prepackingEventsRepository.findById(id);

    if (existingPodEventOpt.isPresent()) {
      PrepackingEvent existingPodEvent = existingPodEventOpt.get();
      PrepackingEventProcessContext context = contextBuilder.buildContext(dto);
      dto.setContext(context);
      PrepackingEvent incomingPodEvent = dto.toPrepackingEvent();

      // Update the Existing PodEvent object with values incoming DTO data
      existingPodEvent = copyAttributes(existingPodEvent, incomingPodEvent);

      // save updated pod event
      prepackingEventsRepository.save(existingPodEvent);
      return prepackingToDto(existingPodEvent);
    } else {
      return null;
    }
  }

  private PrepackingEvent copyAttributes(
      PrepackingEvent existingPodEvent, PrepackingEvent incomingPodEvent) {
    if (incomingPodEvent.getSourceId() != null) {
      existingPodEvent.setSourceId(incomingPodEvent.getSourceId());
    }
    if (incomingPodEvent.getSourceFreeText() != null) {
      existingPodEvent.setSourceFreeText(incomingPodEvent.getSourceFreeText());
    }
    if (incomingPodEvent.getDestinationId() != null) {
      existingPodEvent.setDestinationId(incomingPodEvent.getDestinationId());
    }
    if (incomingPodEvent.getDestinationFreeText() != null) {
      existingPodEvent.setDestinationFreeText(incomingPodEvent.getDestinationFreeText());
    }
    if (incomingPodEvent.getReferenceNumber() != null) {
      existingPodEvent.setReferenceNumber(incomingPodEvent.getReferenceNumber());
    }
    if (incomingPodEvent.getPackingDate() != null) {
      existingPodEvent.setPackingDate(incomingPodEvent.getPackingDate());
    }
    if (incomingPodEvent.getPackedBy() != null) {
      existingPodEvent.setPackedBy(incomingPodEvent.getPackedBy());
    }
    if (incomingPodEvent.getCartonsQuantityOnWaybill() != null) {
      existingPodEvent.setCartonsQuantityOnWaybill(
          incomingPodEvent.getCartonsQuantityOnWaybill());
    }
    if (incomingPodEvent.getCartonsQuantityShipped() != null) {
      existingPodEvent.setCartonsQuantityShipped(
          incomingPodEvent.getCartonsQuantityShipped());
    }
    if (incomingPodEvent.getCartonsQuantityAccepted() != null) {
      existingPodEvent.setCartonsQuantityAccepted(
          incomingPodEvent.getCartonsQuantityAccepted());
    }
    if (incomingPodEvent.getCartonsQuantityRejected() != null) {
      existingPodEvent.setCartonsQuantityRejected(
          incomingPodEvent.getCartonsQuantityRejected());
    }
    if (incomingPodEvent.getContainersQuantityOnWaybill() != null) {
      existingPodEvent.setContainersQuantityOnWaybill(
          incomingPodEvent.getContainersQuantityOnWaybill());
    }
    if (incomingPodEvent.getContainersQuantityShipped() != null) {
      existingPodEvent.setContainersQuantityShipped(
          incomingPodEvent.getContainersQuantityShipped());
    }
    if (incomingPodEvent.getContainersQuantityAccepted() != null) {
      existingPodEvent.setContainersQuantityAccepted(
          incomingPodEvent.getContainersQuantityAccepted());
    }
    if (incomingPodEvent.getContainersQuantityRejected() != null) {
      existingPodEvent.setContainersQuantityRejected(
          incomingPodEvent.getContainersQuantityRejected());
    }
    if (incomingPodEvent.getRemarks() != null) {
      existingPodEvent.setRemarks(incomingPodEvent.getRemarks());
    }
    if (incomingPodEvent.getDiscrepancies() != null) {
      existingPodEvent.setDiscrepancies(incomingPodEvent.getDiscrepancies());
    }
    return existingPodEvent;
  }

  /**
   * Delete POD.
   *
   * @param id POD event id.
   */
  public void deletePrepackingEvent(UUID id) {
    // LOGGER.info("update POS event");
    // physicalInventoryValidator.validateDraft(dto, id);
    // checkPermission(dto.getProgramId(), dto.getFacilityId());

    // checkIfDraftExists(dto, id);

    LOGGER.info("Attempting to fetch pod event with id = " + id);
    Optional<PrepackingEvent> existingPodEventOpt = prepackingEventsRepository.findById(id);

    if (existingPodEventOpt.isPresent()) {
      // delete pod event
      prepackingEventsRepository.delete(existingPodEventOpt.get());
    } else {
      throw new ResourceNotFoundException(new Message("Point of delivery event not found ", id));
    }
  }

  /**
   * Create from jpa model.
   *
   * @param prepackingEvents inventory jpa model.
   * @return created dto.
   */
  private List<PrepackingEventDto> prepackingToDto(
      Collection<PrepackingEvent> prepackingEvents) {

    List<PrepackingEventDto> podDtos = new ArrayList<>(prepackingEvents.size());
    prepackingEvents.forEach(i -> podDtos.add(prepackingToDto(i)));
    return podDtos;
  }

  /**
   * Create dto from jpa model.
   *
   * @param prepackingEvent inventory jpa model.
   * @return created dto.
   */
  private PrepackingEventDto prepackingToDto(PrepackingEvent prepackingEvent) {

    return PrepackingEventDto.builder()
        .id(prepackingEvent.getId())
        .dateCreated(prepackingEvent.getDateCreated())
        .userId(prepackingEvent.getUserId())
        .dateAuthorised(prepackingEvent.getDateAuthorised())
        .programId(prepackingEvent.getProgramId())
        .comments(prepackingEvent.getComments())
        .supervisoryNodeId(prepackingEvent.getSupervisoryNodeId())
        .status(prepackingEvent.getStatus())
        .remarks(prepackingEvent.getRemarks())
        .lineItems(prepackingEvent.getLineItems())
        .build();
  }

  /**
   * Create from jpa model.
   *
   * @param prepackingEventLineItems inventory jpa model.
   * @return created dto.
   */
  private List<PrepackingEventLineItemDto> prepackingEventLineItemsToDtos(
      Collection<PrepackingEventLineItem> prepackingEventLineItems) {

    List<PrepackingEventLineItemDto> prepackingEventLineItemDtos = new ArrayList<>(prepackingEventLineItems.size());
    prepackingEventLineItems.forEach(i -> prepackingEventLineItemDtos.add(prepackingEventLineItemDto(i)));
    return prepackingEventLineItemDtos;
  }

  /**
   * Create dto from jpa model.
   *
   * @param prepackingEventLineItem inventory jpa model.
   * @return created dto.
   */
  private PrepackingEventLineItemDto prepackingEventLineItemDto(PrepackingEventLineItem prepackingEventLineItem) {
    /*
     * prepackingEventId,
     * orderableId,
     * numberOfPrepacks,
     * prepackSize,
     * lotId,
     * remarks);
     */
    return PrepackingEventLineItemDto.builder()
        .id(prepackingEventLineItem.getId())
        .prepackingEventId(prepackingEventLineItem.getPrepackingEventId())
        .orderableId(prepackingEventLineItem.getOrderableId())
        .numberOfPrepacks(prepackingEventLineItem.getNumberOfPrepacks())
        .prepackSize(prepackingEventLineItem.getPrepackSize())
        .lotId(prepackingEventLineItem.getLotId())
        .remarks(prepackingEventLineItem.getRemarks())
        .build();
  }

}
