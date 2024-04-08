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

package org.openlmis.pointofdelivery.web;

import static java.lang.String.format;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.Test;
import org.openlmis.pointofdelivery.exception.PermissionMessageException;
import org.openlmis.pointofdelivery.service.JasperReportService;
import org.openlmis.pointofdelivery.service.PermissionService;
import org.openlmis.pointofdelivery.util.Message;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.ResultActions;

public class ReportsControllerIntegrationTest extends BaseWebTest {

  private static final String CARD_REPORT = "/api/stockCards/%s/print";
  private static final String CARD_SUMMARY_REPORT = "/api/stockCardSummaries/print";

  @MockBean
  private JasperReportService reportService;

  @MockBean
  private PermissionService permissionService;

  @Test
  public void return200WhenStockCardReportGenerated() throws Exception {
    //given
    UUID stockCardId = UUID.randomUUID();
    when(reportService.generateStockCardReport(stockCardId))
        .thenReturn(new byte[1]);

    //when
    ResultActions resultActions = mvc.perform(get(format(CARD_REPORT, stockCardId.toString()))
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE));

    //then
    resultActions.andExpect(status().isOk());
  }

  @Test
  public void return200WhenStockCardSummaryReportGenerated() throws Exception {
    //given
    UUID program = UUID.randomUUID();
    UUID facility = UUID.randomUUID();
    when(reportService.generateStockCardSummariesReport(program, facility))
        .thenReturn(new byte[1]);

    //when
    ResultActions resultActions = mvc.perform(get(CARD_SUMMARY_REPORT)
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
        .param("program", program.toString())
        .param("facility", facility.toString()));

    //then
    resultActions.andExpect(status().isOk());
    verify(permissionService, times(1)).canViewStockCard(program, facility);
  }

  @Test
  public void return403WhenUserHasNoPermissionToViewStockCard() throws Exception {
    //given
    UUID programId = UUID.randomUUID();
    UUID facilityId = UUID.randomUUID();
    doThrow(new PermissionMessageException(new Message("key"))).when(permissionService)
        .canViewStockCard(programId, facilityId);

    //when
    ResultActions resultActions = mvc.perform(get(CARD_SUMMARY_REPORT)
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
        .param("program", programId.toString())
        .param("facility", facilityId.toString()));

    //then
    resultActions.andExpect(status().isForbidden());
  }
}