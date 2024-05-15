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
import org.openlmis.prepacking.domain.status.PrepackingEventStatus;
import org.openlmis.prepacking.dto.PrepackingEventDto;
import org.openlmis.prepacking.dto.PrepackingEventLineItemDto;
import org.openlmis.prepacking.dto.referencedata.ApprovedProductDto;
import org.openlmis.prepacking.dto.referencedata.FacilityTypeDto;
import org.openlmis.prepacking.dto.referencedata.LotDto;
import org.openlmis.prepacking.dto.referencedata.MetaDataDto;
import org.openlmis.prepacking.dto.referencedata.OrderableDto;
import org.openlmis.prepacking.dto.referencedata.ProgramDto;
import org.openlmis.prepacking.dto.referencedata.TradeItemDto;
import org.openlmis.prepacking.dto.stockmanagement.StockCardSummaryDto;
//import org.openlmis.prepacking.dto.stockmanagement.StockEventAdjustmentDto;
import org.openlmis.prepacking.dto.stockmanagement.StockEventDto;
import org.openlmis.prepacking.dto.stockmanagement.StockEventLineItemDto;
import org.openlmis.prepacking.exception.ResourceNotFoundException;
import org.openlmis.prepacking.repository.PrepackingEventsRepository;
import org.openlmis.prepacking.service.referencedata.FacilityReferenceDataService;
import org.openlmis.prepacking.service.referencedata.FacilityTypeApprovedProductReferenceDataService;
import org.openlmis.prepacking.service.referencedata.LotReferenceDataService;
import org.openlmis.prepacking.service.referencedata.OrderableReferenceDataService;
import org.openlmis.prepacking.service.referencedata.ProgramReferenceDataService;
import org.openlmis.prepacking.service.referencedata.TradeItemReferenceDataService;
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

  @Autowired
  private ProgramReferenceDataService programReferenceDataService;

  @Autowired
  FacilityTypeApprovedProductReferenceDataService facilityTypeApprovedProductReferenceDataService;

  @Autowired
  FacilityReferenceDataService facilityReferenceDataService;

  @Autowired
  LotReferenceDataService lotReferenceDataService;

  @Autowired
  TradeItemReferenceDataService tradeItemReferenceDataService;

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

      // Update the Existing PrepackingEvent object with values incoming DTO data
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
    if (incomingPrepackingEvent.getPrepackerUserId() != null) {
      existingPrepackingEvent.setPrepackerUserId(incomingPrepackingEvent.getPrepackerUserId());
    }
    if (incomingPrepackingEvent.getPrepackerUserNames() != null) {
      existingPrepackingEvent.setPrepackerUserNames(incomingPrepackingEvent.getPrepackerUserNames());
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
        .facilityId(prepackingEvent.getFacilityId())
        .programId(prepackingEvent.getProgramId())
        .comments(prepackingEvent.getComments())
        .prepackerUserId(prepackingEvent.getPrepackerUserId())
        .prepackerUserNames(prepackingEvent.getPrepackerUserNames())
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
        .lotId(prepackingEventLineItem.getLotId())
        .remarks(prepackingEventLineItem.getRemarks())
        .stockOnHand(prepackingEventLineItem.getStockOnHand())
        .status(prepackingEventLineItem.getStatus())
        .build();
  }

  /**
   * Authorize a Prepacking event.
   *
   * @param prepackingEventId prepacking event id.
   * @return a prepacking event dto.
   */
  public PrepackingEventDto authorizePrepack(UUID prepackingEventId) {
    // Fetch prepacking event
    Optional<PrepackingEvent> prepackingEventOptional = prepackingEventsRepository
        .findById(prepackingEventId);
    if (prepackingEventOptional.isPresent()) {
      PrepackingEvent prepackingEvent = prepackingEventOptional.get();
      // For each prepacking event line item
      for (PrepackingEventLineItem prepackingEventLineItem : prepackingEvent.getLineItems()) {
        // Get SOH - call
        LotDto bulkLot = lotReferenceDataService
            .findOne(prepackingEventLineItem.getLotId());
        List<StockCardSummaryDto> stockCardSummaries = stockCardSummariesStockManagementService
            .search(
                prepackingEvent.getProgramId(),
                prepackingEvent.getFacilityId(),
                Collections.singleton(prepackingEventLineItem.getOrderableId()),
                LocalDate.now(),
                bulkLot.getLotCode());
        Integer quantityToPrepack = prepackingEventLineItem.getPrepackSize()
            * prepackingEventLineItem.getNumberOfPrepacks();

        if (!stockCardSummaries.isEmpty()) {
          Integer stockOnHand = stockCardSummaries.get(0).getStockOnHand();
          if (quantityToPrepack <= stockOnHand) {
            LOGGER.info("We have enough stock for product "
                + prepackingEventLineItem.getOrderableId());
            OrderableDto bulkOrderable = orderableReferenceDataService
                .findOne(prepackingEventLineItem.getOrderableId());

            // Check if the prepackOrderable already exists
            String prepackOrderableCode = bulkOrderable.getProductCode()
                + "-" + prepackingEventLineItem.getPrepackSize();
            String prepackOrderableName = bulkOrderable.getFullProductName()
                + "-" + prepackingEventLineItem.getPrepackSize();
            RequestParameters orderableParameters = RequestParameters.init()
                .set("code", prepackOrderableCode)
                .set("name", prepackOrderableName);
            List<OrderableDto> orderables = orderableReferenceDataService
                .getPage(orderableParameters).getContent();
            OrderableDto prepackOrderable = null;
            LotDto childLot = null;
            if (orderables.isEmpty()) {
              // product does not exist, so create it             
              prepackOrderable = new OrderableDto();
              prepackOrderable.setId(UUID.randomUUID());
              prepackOrderable.setFullProductName(prepackOrderableName);
              prepackOrderable.setDescription(bulkOrderable.getDescription()
                  + "-" + prepackingEventLineItem.getPrepackSize());
              prepackOrderable.setProductCode(prepackOrderableCode);
              prepackOrderable.setNetContent((long) prepackingEventLineItem.getPrepackSize());
              prepackOrderable.setPrograms(bulkOrderable.getPrograms());
              prepackOrderable
                  .setPackRoundingThreshold(prepackingEventLineItem.getPrepackSize() / 2);
              Map<String, String> orderableIdentifiers = new HashMap<>();

              //first create tradeItem for this orderable since it is a tradeitem's orderable
              TradeItemDto tradeItem = new TradeItemDto();
              tradeItem.setManufacturerOfTradeItem("Local Manufacturer");
              UUID createdTradeItemUuid = tradeItemReferenceDataService.submit(tradeItem).getId();
              
              orderableIdentifiers.put("tradeItem", createdTradeItemUuid.toString());
              prepackOrderable.setIdentifiers(orderableIdentifiers);
              MetaDataDto meta = new MetaDataDto();
              meta.setLastUpdated(ZonedDateTime.now());
              meta.setVersionNumber(1L);
              prepackOrderable.setMeta(meta);
              prepackOrderable.setRoundToZero(bulkOrderable.getRoundToZero());
              prepackOrderable.setDispensable(bulkOrderable.getDispensable());

              orderableReferenceDataService.getPage("", new HashMap<>(),
                  prepackOrderable, HttpMethod.PUT, OrderableDto.class);
              // OrderableDto createdOrderable = null;
              // if (!createdOrderableList.isEmpty()) {
              //   createdOrderable = createdOrderableList.get(0);
              // }
              // To-do handle product creation failure
              
              //Create child lot
              childLot = new LotDto();
              childLot.setTradeItemId(createdTradeItemUuid);
              childLot.setLotCode(bulkLot.getLotCode() + "-" 
                  + prepackingEventLineItem.getPrepackSize());
              childLot.setExpirationDate(bulkLot.getExpirationDate());
              childLot.setManufactureDate(bulkLot.getManufactureDate());
              childLot.setActive(bulkLot.isActive());
              
              LotDto existingLot = lotReferenceDataService.getLotMatching(childLot);
              if (null == existingLot) {
                //create lot
                childLot = lotReferenceDataService.submit(childLot);
              } else {
                childLot = existingLot;
              }

            } else {
              // product exists
              prepackOrderable = orderables.get(0);
              //Create child lot
              childLot = new LotDto();
              childLot.setLotCode(bulkLot.getLotCode() + "-" 
                  + prepackingEventLineItem.getPrepackSize());
              childLot.setTradeItemId(
                  UUID.fromString(prepackOrderable.getIdentifiers().get("tradeItem")));
              childLot.setExpirationDate(bulkLot.getExpirationDate());
              childLot.setManufactureDate(bulkLot.getManufactureDate());
              childLot.setActive(bulkLot.isActive());

              LotDto existingLot = lotReferenceDataService.getLotMatching(childLot);
              if (null == existingLot) {
                //create lot
                childLot = lotReferenceDataService.submit(childLot);
              } else {
                childLot = existingLot;
              }
            }

            // Check
            FacilityTypeDto facilityType = facilityReferenceDataService
                .findOne(prepackingEvent.getFacilityId())
                .getType();
            ProgramDto program = programReferenceDataService
                .findOne(prepackingEvent.getProgramId());
            RequestParameters facilityTypeApprovedProductsParams = RequestParameters.init()
                .set("facilityType", facilityType.getCode())
                .set("program", program.getCode())
                .set("orderableId", prepackOrderable.getId().toString());
            List<ApprovedProductDto> approvedProducts 
                = facilityTypeApprovedProductReferenceDataService
                    .getPage(facilityTypeApprovedProductsParams).getContent();
            LOGGER.error("Approved products: " + approvedProducts.toString());
            if (approvedProducts.isEmpty()) {
              // Add product to facility type approved products
              ApprovedProductDto approvedProduct = new ApprovedProductDto(
                  prepackOrderable,
                  facilityType,
                  program,
                  1.0,
                  1.0,
                  1.0,
                  new MetaDataDto());
              facilityTypeApprovedProductReferenceDataService.getPage(
                  "",
                  new HashMap<>(),
                  approvedProduct);
            }
            // debit bulk orderable
            StockEventDto stockEventDebit = new StockEventDto();
            stockEventDebit.setFacilityId(prepackingEvent.getFacilityId());
            stockEventDebit.setProgramId(prepackingEvent.getProgramId());
            stockEventDebit.setUserId(prepackingEvent.getPrepackerUserId());
            StockEventLineItemDto lineItemDebit = new StockEventLineItemDto(
                bulkOrderable.getId(),
                prepackingEventLineItem.getLotId(),
                quantityToPrepack,
                LocalDate.now(),
                UUID.fromString(prepackingDebitReasonId));
            stockEventDebit.setLineItems(Collections.singletonList(lineItemDebit));
            // submit stock event to stockmanagement service
            LOGGER.error("Submitting stockevent DR : " + stockEventDebit.toString());
            stockEventStockManagementService.submit(stockEventDebit);

            // credit prepackaged orderable
            StockEventDto stockEventCredit = new StockEventDto();
            stockEventCredit.setFacilityId(prepackingEvent.getFacilityId());
            stockEventCredit.setProgramId(prepackingEvent.getProgramId());
            stockEventCredit.setUserId(prepackingEvent.getPrepackerUserId());
            StockEventLineItemDto lineItemCredit = new StockEventLineItemDto(
                prepackOrderable.getId(),
                childLot.getId(),
                quantityToPrepack,
                LocalDate.now(),
                UUID.fromString(prepackingCreditReasonId));
            stockEventCredit.setLineItems(Collections.singletonList(lineItemCredit));
            // submit stock event to stockmanagement service
            LOGGER.error("Child lot : " + childLot.toString());
            LOGGER.error("Submitting stockevent CR : " + stockEventCredit.toString());
            stockEventStockManagementService.submit(stockEventCredit);
            prepackingEventLineItem.setRemarks("Successful");
          } else {
            // inadequate stock
            LOGGER.info("Inadequate stock for product "
                + prepackingEventLineItem.getOrderableId()
                + " lot: " + prepackingEventLineItem.getLotId());
            prepackingEventLineItem.setRemarks("Unsuccessful - inadequate stock");
          }
        } else {
          // cannot find stockcard summary of the orderable
          // hm, does it exist?
          prepackingEventLineItem.setRemarks("Unsuccessful - orderable does not exist");
        }
      }
      // update prepackingevent status here
      prepackingEvent.setStatus(PrepackingEventStatus.AUTHORIZED);
      return prepackingToDto(prepackingEvent);

    } else {
      // prepackingevent cannot be found
      return null;
    }
  }

  /**
   * Reject prepacking event.
   *
   * @param prepackingEventId prepacking event id.
   * @return rejected dto.
   */
  public PrepackingEventDto rejectPrepack(UUID prepackingEventId) {
    //fetch prepacking event
    Optional<PrepackingEvent> prepackingEventOpt = prepackingEventsRepository
        .findById(prepackingEventId);
    
    if (prepackingEventOpt.isPresent()) {
      //update status and save
      PrepackingEvent prepackingEvent = prepackingEventOpt.get();
      prepackingEvent.setStatus(PrepackingEventStatus.REJECTED);
      prepackingEventsRepository.save(prepackingEvent);
      return prepackingToDto(prepackingEvent);
    }
    return null;
  }

}
