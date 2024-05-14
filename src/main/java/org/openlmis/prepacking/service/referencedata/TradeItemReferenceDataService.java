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

package org.openlmis.prepacking.service.referencedata;

import static org.openlmis.prepacking.util.RequestHelper.createUri;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.openlmis.prepacking.dto.LocalizedMessageDto;
import org.openlmis.prepacking.dto.referencedata.TradeItemDto;
import org.openlmis.prepacking.exception.ExternalApiException;
import org.openlmis.prepacking.exception.ServerException;
import org.openlmis.prepacking.i18n.MessageKeys;
import org.openlmis.prepacking.util.RequestHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;

@Service
public class TradeItemReferenceDataService extends BaseReferenceDataService<TradeItemDto> {
  @Autowired
  private ObjectMapper objectMapper;

  @Override
  protected String getUrl() {
    return "/api/tradeItems/";
  }

  @Override
  protected Class<TradeItemDto> getResultClass() {
    return TradeItemDto.class;
  }

  @Override
  protected Class<TradeItemDto[]> getArrayResultClass() {
    return TradeItemDto[].class;
  }

  /**
   * Saves the given tradeItem to the referencedata service.
   *
   * @param tradeItemDto  the lot to be created
   */
  @SuppressWarnings("PMD.PreserveStackTrace")
  public TradeItemDto submit(TradeItemDto tradeItemDto) {
    String url = getServiceUrl() + getUrl();
    try {
      return runWithRetryAndTokenRetry(() ->
          restTemplate.exchange(
              createUri(url),
              HttpMethod.PUT,
              RequestHelper.createEntity(tradeItemDto, authService.obtainAccessToken()),
              TradeItemDto.class
          )).getBody();

    } catch (HttpStatusCodeException ex) {
      if (ex.getStatusCode() == HttpStatus.BAD_REQUEST) {
        try {
          LocalizedMessageDto localizedMessage =
              objectMapper.readValue(ex.getResponseBodyAsString(), LocalizedMessageDto.class);

          throw new ExternalApiException(ex, localizedMessage);
        } catch (IOException ex2) {
          throw new ServerException(ex2, MessageKeys.ERROR_IO, ex2.getMessage());
        }
      } else {
        throw buildDataRetrievalException(ex);
      }
    }
  }
}
