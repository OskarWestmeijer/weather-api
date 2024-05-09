package westmeijer.oskar.weatherapi.importjob.client;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import westmeijer.oskar.weatherapi.importjob.client.mapper.OpenWeatherApiMapper;
import westmeijer.oskar.weatherapi.importjob.exception.OpenWeatherApiRequestException;
import westmeijer.oskar.weatherapi.location.service.model.Location;
import westmeijer.oskar.weatherapi.openapi.client.api.GeneratedOpenWeatherApi;
import westmeijer.oskar.weatherapi.openapi.client.model.GeneratedOpenWeatherApiResponse;

@Component
@Slf4j
public class OpenWeatherApiClient {

  private final OpenWeatherApiMapper openWeatherApiMapper;

  private final String appId;

  private final GeneratedOpenWeatherApi generatedOpenWeatherApi;

  public OpenWeatherApiClient(OpenWeatherApiMapper openWeatherApiMapper,
      GeneratedOpenWeatherApi generatedOpenWeatherApi,
      @Value("${openweatherapi.appId}") String appId) {
    requireNonNull(appId, "appId is required");
    this.openWeatherApiMapper = openWeatherApiMapper;
    this.appId = appId;
    this.generatedOpenWeatherApi = generatedOpenWeatherApi;
  }

  public List<Location> requestWeatherForBatch(List<Location> locations) {
    checkArgument(CollectionUtils.isNotEmpty(locations), "locations are required");
    return locations.stream()
        .map(location -> {
          try {
            return requestWeather(location);
          } catch (Exception e) {
            // single failures should not stop the batch import
            log.error("Import failed for locationId: {}", location.locationId(), e);
            return null;
          }
        })
        .filter(Objects::nonNull)
        .toList();
  }

  public Location requestWeather(Location location) {
    requireNonNull(location, "location is required");
    var response = requireNonNull(request(location), "response is required");
    var body = requireNonNull(response.getBody(), "body is required");
    return openWeatherApiMapper.mapToLocation(body, location);
  }

  private ResponseEntity<GeneratedOpenWeatherApiResponse> request(Location location) {
    try {
      // throws checked WebClientResponseException
      return generatedOpenWeatherApi.getCurrentWeatherWithHttpInfo(
          location.latitude(), location.longitude(), "metric", appId).block();
    } catch (Exception e) {
      // rethrow as unchecked OpenWeatherApiRequestException, minimize exception handling
      throw new OpenWeatherApiRequestException("Exception during OpenWeatherApi request.", e);
    }
  }

}
