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

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.openlmis.prepacking.domain.event.PrepackingEvent;
import org.openlmis.prepacking.domain.event.PrepackingEventLineItem;
import org.openlmis.prepacking.dto.PrepackingEventDto;
import org.openlmis.prepacking.dto.PrepackingEventLineItemDto;
import org.openlmis.prepacking.dto.referencedata.OrderableDto;
import org.openlmis.prepacking.dto.stockmanagement.StockCardSummaryDto;
//import org.openlmis.prepacking.dto.stockmanagement.StockEventAdjustmentDto;
import org.openlmis.prepacking.dto.stockmanagement.StockEventDto;
import org.openlmis.prepacking.dto.stockmanagement.StockEventLineItemDto;
import org.openlmis.prepacking.exception.ResourceNotFoundException;
import org.openlmis.prepacking.repository.PrepackingEventsRepository;
import org.openlmis.prepacking.service.referencedata.OrderableReferenceDataService;
import org.openlmis.prepacking.service.stockmanagement.StockCardSummariesStockManagementService;
import org.openlmis.prepacking.service.stockmanagement.StockEventStockManagementService;
import org.openlmis.prepacking.util.Message;
import org.openlmis.prepacking.util.PrepackingEventProcessContext;
import org.openlmis.prepacking.util.RequestParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

@Service
public class PrepackingService {
  private static final Logger LOGGER = LoggerFactory.getLogger(PrepackingService.class);

  @Autowired
  private PrepackingEventsRepository prepackingEventsRepository;

  @Autowired
  private PrepackingEventProcessContextBuilder contextBuilder;

  @Autowired
  private StockCardSummariesStockManagementService stockCardSummariesStockManagementService;

  @Autowired
  private OrderableReferenceDataService orderableReferenceDataService;

  @Autowired
  private StockEventStockManagementService stockEventStockManagementService;

  @Value("${prepacking.prepackingdebit.reasonId}")
  private String prepackingDebitReasonId;

  @Value("${prepacking.prepackingcredit.reasonId}")
  private String prepackingCreditReasonId;

  /**
   * Get a list of Prepacking events.
   *
   * @param facilityId facility id.
   * @return a list of prepacking events.
   */
  public List<PrepackingEventDto> getPrepackingEventsByFacilityId(UUID facilityId) {
    List<PrepackingEvent> prepackingEvents = prepackingEventsRepository
        .findByFacilityId(facilityId);

    if (prepackingEvents == null) {
      return Collections.emptyList();
    }
    return prepackingToDto(prepackingEvents);
  }

  /**
   * Get a list of Prepacking events.
   *
   * @param programId program id.
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
   * Get a list of Prepacking events.
   *
   * @param facilityId facility id.
   * @param programId  program id.
   * @return a list of prepacking events.
   */
  public List<PrepackingEventDto> getPrepackingEventsByFacilityIdAndProgramId(
      UUID facilityId,
      UUID programId) {
    List<PrepackingEvent> prepackingEvents = prepackingEventsRepository
        .findByFacilityIdAndProgramId(facilityId, programId);

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
  public PrepackingEventDto getPrepackingEventById(UUID id) {
    Optional<PrepackingEvent> existingPrepackingEventOpt = prepackingEventsRepository.findById(id);

    if (existingPrepackingEventOpt.isPresent()) {
      PrepackingEvent existingPrepackingEvent = existingPrepackingEventOpt.get();
      return prepackingToDto(existingPrepackingEvent);
    }
    return null;
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
   * Get a Prepacking event by user id.
   *
   * @param dateAuthorised prepacking event dateAuthorised.
   * @return a prepacking event.
   */
  public List<PrepackingEvent> getPrepackingEventsByDateAuthorised(ZonedDateTime dateAuthorised) {
    return prepackingEventsRepository.findByDateAuthorised(dateAuthorised);
  }

  /**
   * Get a Prepacking event by user id.
   *
   * @param userId prepacking event id.
   * @return a prepacking event.
   */
  public List<PrepackingEvent> getPrepackingEventsByUserId(UUID userId) {
    return prepackingEventsRepository.findByUserId(userId);
  }

  /**
   * Save or update prepacking.
   *
   * @param dto prepacking event dto.
   * @return the saved prepacking event.
   */
  public PrepackingEventDto updatePrepackingEvent(PrepackingEventDto dto, UUID id) {

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
    if (incomingPrepackingEvent.getUserId() != null) {
      existingPrepackingEvent.setUserId(incomingPrepackingEvent.getUserId());
    }
    if (incomingPrepackingEvent.getDateAuthorised() != null) {
      existingPrepackingEvent.setDateAuthorised(incomingPrepackingEvent.getDateAuthorised());
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
    if (incomingPrepackingEvent.getSupervisoryNodeId() != null) {
      existingPrepackingEvent.setSupervisoryNodeId(incomingPrepackingEvent.getSupervisoryNodeId());
    }
    if (incomingPrepackingEvent.getStatus() != null) {
      existingPrepackingEvent.setStatus(incomingPrepackingEvent.getStatus());
    }
    if (incomingPrepackingEvent.getLineItems() != null) {
      existingPrepackingEvent.setLineItems(incomingPrepackingEvent.getLineItems());
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
        .userId(prepackingEvent.getUserId())
        .dateAuthorised(prepackingEvent.getDateAuthorised())
        .facilityId(prepackingEvent.getFacilityId())
        .programId(prepackingEvent.getProgramId())
        .comments(prepackingEvent.getComments())
        .supervisoryNodeId(prepackingEvent.getSupervisoryNodeId())
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

    List<PrepackingEventLineItemDto> prepackingEventLineItemDtos;
    prepackingEventLineItemDtos = new ArrayList<>(prepackingEventLineItems.size());
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
        .lotCode(prepackingEventLineItem.getLotCode())
        .remarks(prepackingEventLineItem.getRemarks())
        .build();
  }

  /**
   * Authorize a Prepacking event.
   *
   * @param prepackingEventId prepacking event id.
   * @return a prepacking event dto.
   */
  public PrepackingEventDto authorizePrepack(UUID prepackingEventId) {
    //Fetch prepacking event 
    Optional<PrepackingEvent> prepackingEventOptional = 
        prepackingEventsRepository.findById(prepackingEventId);
    if (prepackingEventOptional.isPresent()) {
      PrepackingEvent prepackingEvent = prepackingEventOptional.get();

      
      //For each prepacking event line item
      for (PrepackingEventLineItem prepackingEventLineItem : prepackingEvent.getLineItems()) {
        // Get SOH - call
       
        List<StockCardSummaryDto> stockCardSummaries = stockCardSummariesStockManagementService
            .search(
              prepackingEvent.getProgramId(), 
              prepackingEvent.getFacilityId(), 
              Collections.singleton(prepackingEventLineItem.getOrderableId()), 
              LocalDate.now(), 
              prepackingEventLineItem.getLotCode());
        
        Integer quantityToPrepack = 
            prepackingEventLineItem.getPrepackSize() 
            * prepackingEventLineItem.getNumberOfPrepacks();      
        if ((!stockCardSummaries.isEmpty()) && (stockCardSummaries.get(0).getStockOnHand() 
            >= quantityToPrepack)) {
          //fetch orderable and duplicate it
          LOGGER.error("We have enough stock for product " 
              + prepackingEventLineItem.getOrderableId());
          OrderableDto orderable = orderableReferenceDataService
              .findOne(prepackingEventLineItem.getOrderableId());
          LOGGER.error("Original (Bulk) orderable: " + orderable.toString());
          orderable.setNetContent((long)prepackingEventLineItem.getPrepackSize());
          orderable.setFullProductName(orderable
              .getFullProductName() + "-" + prepackingEventLineItem.getPrepackSize());
          orderable.setProductCode(orderable
              .getProductCode() + "-" + prepackingEventLineItem.getPrepackSize());
          Map<String, Object> map = new HashMap<>();
          //find orderable
          RequestParameters parameters = RequestParameters.init()
              .set("code", orderable.getProductCode())
              .set("name", orderable.getFullProductName());
          List<OrderableDto> orderables = orderableReferenceDataService
              .getPage(parameters).getContent();
          if (orderables.isEmpty()) {
            //create new orderable
            LOGGER.error("Creating new product " 
                + orderable.toString());
            orderable.setId(UUID.randomUUID());
            Map<String, String> identifiers = new HashMap<>();
            identifiers.put("tradeItem", UUID.randomUUID().toString());
            orderable.setIdentifiers(identifiers);
            LOGGER.error("Prepacked Orderable: " + orderable.toString());
            orderableReferenceDataService.getPage("", map, 
              orderable, HttpMethod.PUT, OrderableDto.class).getContent();
          } else {
            LOGGER.error("Product " + orderable.toString()
                + "already exists");
          }
          // orderableReferenceDataService.getPage("", map, 
          //     orderable, HttpMethod.PUT, OrderableDto.class).getContent();
          
          String lotId = "73d14820-1759-4cd4-a4a2-cb84410b402d";
          //debit bulk orderable
          StockEventDto stockEventDebit = new StockEventDto();
          stockEventDebit.setFacilityId(prepackingEvent.getFacilityId());
          stockEventDebit.setProgramId(prepackingEvent.getProgramId());
          stockEventDebit.setUserId(prepackingEvent.getUserId());
          StockEventLineItemDto lineItemDebit = new StockEventLineItemDto(
              prepackingEventLineItem.getOrderableId(), 
              UUID.fromString(lotId),
              quantityToPrepack, 
              LocalDate.now(), 
              UUID.fromString(prepackingDebitReasonId)
          );
          stockEventDebit.setLineItems(Collections.singletonList(lineItemDebit));
          //submit stock event to stockmanagement service
          LOGGER.error("Submitting stockevent DR : " + stockEventDebit.toString());
          stockEventStockManagementService.submit(stockEventDebit);
          
          //credit prepackaged orderable
          StockEventDto stockEventCredit = new StockEventDto();
          stockEventCredit.setFacilityId(prepackingEvent.getFacilityId());
          stockEventCredit.setProgramId(prepackingEvent.getProgramId());
          stockEventCredit.setUserId(prepackingEvent.getUserId());
          StockEventLineItemDto lineItemCredit = new StockEventLineItemDto(
              orderables.isEmpty() ? orderable.getId() : orderables.get(0).getId(), 
              null,
              quantityToPrepack, 
              LocalDate.now(),
              UUID.fromString(prepackingCreditReasonId) 
          );
          stockEventCredit.setLineItems(Collections.singletonList(lineItemCredit));
          //submit stock event to stockmanagement service
          LOGGER.error("Submitting stockevent CR : " + stockEventCredit.toString());
          stockEventStockManagementService.submit(stockEventCredit);
          // stockEventStockManagementService.getPage("", map, 
          //     stockEventCredit, HttpMethod.POST, StockEventDto.class).getContent();

        }
      

      }
      return null;

    /* if SOH >= prepacksize * numberofPrepacks, continue on
          fetch orderable
          duplicate orderable and update netcontent = prepacksize, 
          append prepacksixe to the name
       */
      // otherwise status = unsuccessful

    } else {
      return null;
    }
  }

}
