package westmeijer.oskar.weatherapi.importjob.service;

import static java.util.Objects.requireNonNull;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import westmeijer.oskar.weatherapi.importjob.client.OpenWeatherApiClient;
import westmeijer.oskar.weatherapi.importjob.exception.OpenWeatherApiRequestException;
import westmeijer.oskar.weatherapi.location.service.LocationService;
import westmeijer.oskar.weatherapi.location.service.model.Location;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeatherImportJob {

  private final MeterRegistry meterRegistry;

  private final LocationService locationService;

  private final OpenWeatherApiClient openWeatherApiClient;

  @Scheduled(fixedDelay = 60000)
  @Transactional
  public void refreshWeather() {
    try {
      meterRegistry.counter("import.job", "import", "execution").increment();

      Location location = requireNonNull(locationService.getNextImportLocation(), "location is required");
      log.info("Import weather for location: {}, last_imported_at: {}", location.cityName(), location.lastImportAt());

      Location updatedLocation = openWeatherApiClient.requestWeather(location);

      locationService.save(updatedLocation);
    } catch (OpenWeatherApiRequestException requestException) {
      log.error("OpenWeatherApi request failed!", requestException);
      meterRegistry.counter("import.job", "error", "request").increment();
    } catch (Exception generalException) {
      log.error("Exception during Import job.", generalException);
      meterRegistry.counter("import.job", "error", "process").increment();
    }

  }

}
