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
import org.openlmis.prepacking.dto.referencedata.MetaDataDto;
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

        if (!stockCardSummaries.isEmpty()) {
          Integer stockOnHand = stockCardSummaries.get(0).getStockOnHand();
          if (quantityToPrepack <= stockOnHand) {
            LOGGER.info("We have enough stock for product " 
                + prepackingEventLineItem.getOrderableId());
            OrderableDto bulkOrderable = orderableReferenceDataService
                .findOne(prepackingEventLineItem.getOrderableId());
            
            //Check if the prepackOrderable already exists  
            String prepackOrderableCode = bulkOrderable.getProductCode() 
                + "-" + prepackingEventLineItem.getPrepackSize(); 
            String prepackOrderableName = bulkOrderable.getFullProductName()
                + "-" +  prepackingEventLineItem.getPrepackSize();
            RequestParameters parameters = RequestParameters.init()
                .set("code", prepackOrderableCode)
                .set("name", prepackOrderableName);
            List<OrderableDto> orderables = orderableReferenceDataService
                .getPage(parameters).getContent();
            OrderableDto prepackOrderable = null;
            if (orderables.isEmpty()) {
              //product does not exist, so create it
              prepackOrderable = new OrderableDto();
              prepackOrderable.setId(UUID.randomUUID());
              prepackOrderable.setFullProductName(prepackOrderableName);
              prepackOrderable.setProductCode(prepackOrderableCode);
              prepackOrderable.setNetContent((long)prepackingEventLineItem.getPrepackSize());
              prepackOrderable.setPrograms(bulkOrderable.getPrograms());
              prepackOrderable
                  .setPackRoundingThreshold(prepackingEventLineItem.getPrepackSize() / 2);
              Map<String, String> identifiers = new HashMap<>();
              identifiers.put("tradeItem", UUID.randomUUID().toString());
              prepackOrderable.setIdentifiers(identifiers);
              MetaDataDto meta = new MetaDataDto();
              meta.setLastUpdated(ZonedDateTime.now());
              meta.setVersionNumber(1L);
              prepackOrderable.setMeta(meta);
              prepackOrderable.setRoundToZero(bulkOrderable.getRoundToZero());
              prepackOrderable.setDispensable(bulkOrderable.getDispensable());
              
              orderableReferenceDataService.getPage("", new HashMap<>(), 
                  prepackOrderable, HttpMethod.PUT, OrderableDto.class);
              //To-do handle product creation failure

              //Add product to facility type approved products
              
            } else {
              //product exists
              prepackOrderable = orderables.get(0);
            }

            String lotId = "73d14820-1759-4cd4-a4a2-cb84410b402d";
            //debit bulk orderable
            StockEventDto stockEventDebit = new StockEventDto();
            stockEventDebit.setFacilityId(prepackingEvent.getFacilityId());
            stockEventDebit.setProgramId(prepackingEvent.getProgramId());
            stockEventDebit.setUserId(prepackingEvent.getUserId());
            StockEventLineItemDto lineItemDebit = new StockEventLineItemDto(
                bulkOrderable.getId(), 
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
                prepackOrderable.getId(), 
                null,
                quantityToPrepack, 
                LocalDate.now(),
                UUID.fromString(prepackingCreditReasonId) 
            );
            stockEventCredit.setLineItems(Collections.singletonList(lineItemCredit));
            //submit stock event to stockmanagement service
            LOGGER.error("Submitting stockevent CR : " + stockEventCredit.toString());
            stockEventStockManagementService.submit(stockEventCredit);
            prepackingEventLineItem.setRemarks("Successful");
          } else {
            //inadequate stock
            LOGGER.info("Inadequate stock for product " 
                + prepackingEventLineItem.getOrderableId()
                + " lot: " + prepackingEventLineItem.getLotCode());
            prepackingEventLineItem.setRemarks("Unsuccessful - inadequate stock");
          }
        } else {
          //cannot find stockcard summary of the orderable
          //hm, does it exist?
          prepackingEventLineItem.setRemarks("Unsuccessful - orderable does not exist");
        } 
      }
      //update prepackingevent status here
      prepackingEvent.setStatus("Processed");
      return prepackingToDto(prepackingEvent);

    } else {
      //prepackingevent cannot be found
      return null;
    }
  }

}
