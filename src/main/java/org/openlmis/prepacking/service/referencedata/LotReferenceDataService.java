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
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.openlmis.prepacking.dto.LocalizedMessageDto;
import org.openlmis.prepacking.dto.referencedata.LotDto;
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
public class LotReferenceDataService extends BaseReferenceDataService<LotDto> {
  @Autowired
  private ObjectMapper objectMapper;

  @Override
  protected String getUrl() {
    return "/api/lots/";
  }

  @Override
  protected Class<LotDto> getResultClass() {
    return LotDto.class;
  }

  @Override
  protected Class<LotDto[]> getArrayResultClass() {
    return LotDto[].class;
  }

  /**
   * Saves the given lot to the referencedata service.
   *
   * @param lotDto  the lot to be created
   */
  @SuppressWarnings("PMD.PreserveStackTrace")
  public LotDto submit(LotDto lotDto) {
    String url = getServiceUrl() + getUrl();
    try {
      return runWithRetryAndTokenRetry(() ->
          restTemplate.exchange(
              createUri(url),
              HttpMethod.POST,
              RequestHelper.createEntity(lotDto, authService.obtainAccessToken()),
              LotDto.class
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

  /**
   * Search for lots under a specific trade item.
   *
   * @param tradeItemId trade item id.
   * @return found list of lots.
   */
  public List<LotDto> getAllLotsOf(UUID tradeItemId) {
    return getAllLotsMatching(tradeItemId, null);
  }

  /**
   * Search for lots expiring on a certain date.
   *
   * @param expirationDate expiration date.
   * @return found list of lots.
   */
  public List<LotDto> getAllLotsExpiringOn(LocalDate expirationDate) {
    return getAllLotsMatching(null, expirationDate);
  }
  
  private List<LotDto> getAllLotsMatching(UUID tradeItemId, LocalDate expirationDate) {
    HashMap<String, Object> params = new HashMap<>();

    if (null != tradeItemId) {
      params.put("tradeItemId", tradeItemId);
    }
    if (null != expirationDate) {
      params.put("expirationDate", expirationDate);
    }
    
    return getPage(params).getContent();
  }

  /**
   * Get Lot matching lotDto.
   *
   * @param lotDto lot.
   * @return Lot if lot exists and null otherwise.
   */
  public LotDto getLotMatching(LotDto lotDto) {
    HashMap<String, Object> params = new HashMap<>();

    if (null != lotDto.getTradeItemId()) {
      params.put("tradeItemId", lotDto.getTradeItemId());
    }
    if (null != lotDto.getLotCode()) {
      params.put("lotCode", lotDto.getLotCode());
    }
    
    List<LotDto> lots = getPage(params).getContent();

    return lots.isEmpty() ? null : lots.get(0);
  }


  /**
   * Search for lots expiring between certain dates.
   *
   * @param expirationDateFrom expiration date.
   * @param expirationDateTo expiration date.
   * @return found list of lots.
   */
  public List<LotDto> getAllLotsExpiringBetween(LocalDate expirationDateFrom, 
      LocalDate expirationDateTo) {
    return getAllLotsBetween(null, expirationDateFrom, expirationDateTo);
  }

  private List<LotDto> getAllLotsBetween(UUID tradeItemId, LocalDate expirationDateFrom, 
      LocalDate expirationDateTo) {
    HashMap<String, Object> params = new HashMap<>();

    if (null != tradeItemId) {
      params.put("tradeItemId", tradeItemId);
    }
    if (null != expirationDateFrom) {
      params.put("expirationDateFrom", expirationDateFrom);
    }
    if (null != expirationDateTo) {
      params.put("expirationDateTo", expirationDateTo);
    }
    
    return getPage(params).getContent();
  }
}
