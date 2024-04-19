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
import org.openlmis.prepacking.dto.PrepackingEventDto;
import org.openlmis.prepacking.dto.PrepackingEventLineItemDto;
import org.openlmis.prepacking.exception.ResourceNotFoundException;
import org.openlmis.prepacking.repository.PrepackingEventsRepository;
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

  /**
   * Get a list of Prepacking events.
   *
   * @param programId destination id.
   * @return a list of prepacking events.
   */
  public List<PrepackingEventDto> getPrepackingEventsByProgramId(UUID programId) {
    List<PrepackingEvent> prepackingEvents = prepackingEventsRepository
        .findByProgramId(programId);

    if (prepackingEvents == null) {
      return Collections.emptyList();
    }
    return prepackingToDto(prepackingEvents);
  }

  /**
   * Get a Prepacking event by id.
   *
   * @param id prepacking event id.
   * @return a prepacking event.
   */
  public Optional<PrepackingEvent> getPrepackingEventById(UUID id) {
    return prepackingEventsRepository.findById(id);
  }

  /**
   * Get a Prepacking event by status.
   *
   * @param status prepacking event status.
   * @return a prepacking event.
   */
  public List<PrepackingEvent> getPrepackingEventsByStatus(String status) {
    return prepackingEventsRepository.findByStatus(status);
  }

  /**
   * Save or update prepacking.
   *
   * @param dto prepacking event dto.
   * @return the saved prepacking event.
   */
  public PrepackingEventDto updatePrepackingEvent(PrepackingEventDto dto, UUID id) {
    // LOGGER.info("update POS event");
    // physicalInventoryValidator.validateDraft(dto, id);
    // checkPermission(dto.getProgramId(), dto.getFacilityId());

    // checkIfDraftExists(dto, id);

    LOGGER.info("Attempting to fetch prepacking event with id = " + id);
    Optional<PrepackingEvent> existingPrepackingEventOpt = prepackingEventsRepository.findById(id);

    if (existingPrepackingEventOpt.isPresent()) {
      PrepackingEvent existingPrepackingEvent = existingPrepackingEventOpt.get();
      PrepackingEventProcessContext context = contextBuilder.buildContext(dto);
      dto.setContext(context);
      PrepackingEvent incomingPrepackingEvent = dto.toPrepackingEvent();

      // Update the Existing PodEvent object with values incoming DTO data
      existingPrepackingEvent = copyAttributes(existingPrepackingEvent, incomingPrepackingEvent);

      // save updated prepacking event
      prepackingEventsRepository.save(existingPrepackingEvent);
      return prepackingToDto(existingPrepackingEvent);
    } else {
      return null;
    }
  }

  private PrepackingEvent copyAttributes(PrepackingEvent existingPrepackingEvent,
      PrepackingEvent incomingPrepackingEvent) {
    if (incomingPrepackingEvent.getDateCreated() != null) {
      existingPrepackingEvent.setDateCreated(incomingPrepackingEvent.getDateCreated());
    }
    if (incomingPrepackingEvent.getFacilityId() != null) {
      existingPrepackingEvent.setFacilityId(incomingPrepackingEvent.getFacilityId());
    }
    if (incomingPrepackingEvent.getProgramId() != null) {
      existingPrepackingEvent.setProgramId(incomingPrepackingEvent.getProgramId());
    }
    if (incomingPrepackingEvent.getComments() != null) {
      existingPrepackingEvent.setComments(incomingPrepackingEvent.getComments());
    }
    if (incomingPrepackingEvent.getStatus() != null) {
      existingPrepackingEvent.setStatus(incomingPrepackingEvent.getStatus());
    }
    return existingPrepackingEvent;
  }

  /**
   * Delete prepacking.
   *
   * @param id prepacking event id.
   */
  public void deletePrepackingEvent(UUID id) {
    // LOGGER.info("update POS event");
    // physicalInventoryValidator.validateDraft(dto, id);
    // checkPermission(dto.getProgramId(), dto.getFacilityId());

    // checkIfDraftExists(dto, id);

    LOGGER.info("Attempting to fetch prepacking event with id = " + id);
    Optional<PrepackingEvent> existingPrepackingEventOpt = prepackingEventsRepository.findById(id);

    if (existingPrepackingEventOpt.isPresent()) {
      // delete prepacking event
      prepackingEventsRepository.delete(existingPrepackingEventOpt.get());
    } else {
      throw new ResourceNotFoundException(new Message("Prepacking event not found ", id));
    }
  }

  /**
   * Create from jpa model.
   *
   * @param prepackingEvents prepacking event jpa model.
   * @return created dto.
   */
  private List<PrepackingEventDto> prepackingToDto(
      Collection<PrepackingEvent> prepackingEvents) {

    List<PrepackingEventDto> prepackingDtos = new ArrayList<>(prepackingEvents.size());
    prepackingEvents.forEach(i -> prepackingDtos.add(prepackingToDto(i)));
    return prepackingDtos;
  }

  /**
   * Create dto from jpa model.
   *
   * @param prepackingEvent prepacking event jpa model.
   * @return created dto.
   */
  private PrepackingEventDto prepackingToDto(PrepackingEvent prepackingEvent) {
    return PrepackingEventDto.builder()
        .id(prepackingEvent.getId())
        .dateCreated(prepackingEvent.getDateCreated())
        .facilityId(prepackingEvent.getFacilityId())
        .programId(prepackingEvent.getProgramId())
        .comments(prepackingEvent.getComments())
        .status(prepackingEvent.getStatus())
        .lineItems(prepackingEventLineItemsToDtos(prepackingEvent.getLineItems()))
        .build();
  }

  /**
   * Create from jpa model.
   *
   * @param prepackingEventLineItems prepacking event line item jpa model.
   * @return created dto.
   */
  private List<PrepackingEventLineItemDto> prepackingEventLineItemsToDtos(
      Collection<PrepackingEventLineItem> prepackingEventLineItems) {

    List<PrepackingEventLineItemDto> prepackingEventLineItemDtos = new ArrayList<>(
        prepackingEventLineItems.size());
    prepackingEventLineItems.forEach(i -> prepackingEventLineItemDtos
        .add(prepackingEventLineItemDto(i)));
    return prepackingEventLineItemDtos;
  }

  /**
   * Create dto from jpa model.
   *
   * @param prepackingEventLineItem prepacking event line item jpa model.
   * @return created dto.
   */
  private PrepackingEventLineItemDto prepackingEventLineItemDto(
      PrepackingEventLineItem prepackingEventLineItem) {
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
