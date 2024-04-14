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

package org.openlmis.prepacking.web;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.List;
import java.util.UUID;
import org.openlmis.prepacking.dto.PrepackingEventDto;
// import org.openlmis.prepacking.service.PermissionService;
import org.openlmis.prepacking.service.PrepackingEventProcessor;
import org.openlmis.prepacking.service.PrepackingService;
import org.openlmis.prepacking.web.BaseController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.profiler.Profiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Controller used to perform CRUD operations on prepacking event.
 */
@Controller
@RequestMapping("/api/prepackingEvents")
public class PrepackingController extends BaseController {
  public static final String ID_PATH_VARIABLE = "/{id}";
  private static final Logger LOGGER = LoggerFactory.getLogger(PrepackingController.class);

  // @Autowired
  // private PermissionService permissionService;

  @Autowired
  private PrepackingEventProcessor prepackingEventProcessor;

  @Autowired
  private PrepackingService prepackingService;

  /**
   * Create prepacking event.
   *
   * @param prepackingEventDto a prepacking event bound to request body.
   * @return created prepacking event's ID.
   */
  @Transactional
  @RequestMapping(method = POST)
  public ResponseEntity<UUID> createPrepackingEvent(
      @RequestBody PrepackingEventDto prepackingEventDto) {

    LOGGER.debug("Try to create a prepacking event");

    Profiler profiler = getProfiler("CREATE_PREPACKING_EVENT", prepackingEventDto);

    // checkPermission(prepackingEventDto,
    // profiler.startNested("CHECK_PERMISSION"));

    profiler.start("PROCESS");
    UUID createdPodId = prepackingEventProcessor.process(prepackingEventDto);

    profiler.start("CREATE_RESPONSE");
    ResponseEntity<UUID> response = new ResponseEntity<>(createdPodId, CREATED);

    return stopProfiler(profiler, response);
  }

  /**
   * List prepacking event.
   *
   * @param facilityId a destination facility id.
   * @return List of prepacking events.
   */
  @RequestMapping(method = GET)
  public ResponseEntity<List<PrepackingEventDto>> getPrepackingEvents(
      @RequestParam() UUID facilityId) {

    LOGGER.debug("Try to load prepacking events");

    List<PrepackingEventDto> prepacksToReturn;
    prepacksToReturn = prepackingService.getPrepackingEventsByFacilityId(facilityId);

    return new ResponseEntity<>(prepacksToReturn, OK);
    // Profiler profiler = getProfiler("LIST_PREPACKING_EVENTS",
    // prepackingEventDto);

    // checkPermission(prepackingEventDto,
    // profiler.startNested("CHECK_PERMISSION"));

    // profiler.start("PROCESS");
    // UUID createdPodId =
    // prepackingEventProcessor.process(prepackingEventDto);

    // profiler.start("CREATE_RESPONSE");
    // ResponseEntity<UUID> response = new ResponseEntity<>(createdPodId, CREATED);

    // return stopProfiler(profiler, response);
  }

  /**
   * Update a prepacking event.
   *
   * @param id  prepacking event id.
   * @param dto prepacking dto.
   * @return created prepacking dto.
   */
  @Transactional
  @PutMapping(ID_PATH_VARIABLE)
  @ResponseStatus(OK)
  @ResponseBody
  public ResponseEntity<PrepackingEventDto> updatePrepackingEvent(@PathVariable UUID id,
      @RequestBody PrepackingEventDto dto) {
    PrepackingEventDto updatedPodEvent = prepackingService
        .updatePrepackingEvent(dto, id);
    return new ResponseEntity<>(updatedPodEvent, OK);
  }

  /**
   * Delete a prepacking event.
   *
   * @param id prepacking event id.
   */
  @DeleteMapping(ID_PATH_VARIABLE)
  @ResponseStatus(NO_CONTENT)
  public void deletePrepackingEvent(@PathVariable UUID id) {
    prepackingService.deletePrepackingEvent(id);
  }

}
