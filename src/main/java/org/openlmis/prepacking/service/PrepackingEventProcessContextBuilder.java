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

import static org.slf4j.LoggerFactory.getLogger;
import static org.slf4j.ext.XLoggerFactory.getXLogger;

import java.util.UUID;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import org.openlmis.prepacking.dto.PrepackingEventDto;
import org.openlmis.prepacking.dto.referencedata.FacilityDto;
import org.openlmis.prepacking.service.referencedata.FacilityReferenceDataService;
import org.openlmis.prepacking.util.AuthenticationHelper;
import org.openlmis.prepacking.util.LazyResource;
import org.openlmis.prepacking.util.PrepackingEventProcessContext;
import org.openlmis.prepacking.util.ReferenceDataSupplier;

import org.slf4j.Logger;
import org.slf4j.ext.XLogger;
import org.slf4j.profiler.Profiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;

/**
 * Before we process a pod event, this class will run first, to get all things we need from
 * reference data. So that network traffic will be concentrated at one place rather than scattered
 * all around the place.
 */
@Service
@NoArgsConstructor
@AllArgsConstructor
public class PrepackingEventProcessContextBuilder {

  private static final Logger LOGGER = getLogger(
          PrepackingEventProcessContextBuilder.class);
  private static final XLogger XLOGGER = getXLogger(
          PrepackingEventProcessContextBuilder.class);
  
  @Autowired
  private AuthenticationHelper authenticationHelper;
  
  @Autowired
  private FacilityReferenceDataService facilityService;
  
  /**
   * Before processing events, put all needed ref data into context so
   * we don't have to do frequent network requests.
   *
   * @param pointOfDeliveryEventDto pointOfDeliveryEventDto.
   * @return a context object that includes all needed ref data.
   */
  public PrepackingEventProcessContext buildContext(
          PrepackingEventDto pointOfDeliveryEventDto) {
    XLOGGER.entry(pointOfDeliveryEventDto);
    Profiler profiler = new Profiler("BUILD_CONTEXT");
    profiler.setLogger(XLOGGER);

    LOGGER.info("build stock event process context");
    PrepackingEventProcessContext context = new PrepackingEventProcessContext();

    profiler.start("CREATE_LAZY_USER");
    OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder
        .getContext()
        .getAuthentication();

    Supplier<UUID> userIdSupplier;
    Supplier<String> userNamesSupplier;

    if (authentication.isClientOnly()) {
      userIdSupplier = pointOfDeliveryEventDto::getReceivedByUserId;
      userNamesSupplier = pointOfDeliveryEventDto::getReceivedByUserNames;
    } else {
      userIdSupplier = () -> authenticationHelper.getCurrentUser().getId();
      userNamesSupplier = () -> authenticationHelper.getCurrentUser().getFirstName() 
          + ", " + authenticationHelper.getCurrentUser().getLastName();
    }

    LazyResource<UUID> userId = new LazyResource<>(userIdSupplier);
    context.setCurrentUserId(userId);

    LazyResource<String> userNames = new LazyResource<>(userNamesSupplier);
    context.setCurrentUserNames(userNames);

    profiler.start("CREATE_LAZY_FACILITY");
    UUID facilityId = pointOfDeliveryEventDto.getDestinationId();
    Supplier<FacilityDto> facilitySupplier = new ReferenceDataSupplier<>(
        facilityService, facilityId
    );
    LazyResource<FacilityDto> facility = new LazyResource<>(facilitySupplier);
    context.setFacility(facility);

    return context;
  }

}
