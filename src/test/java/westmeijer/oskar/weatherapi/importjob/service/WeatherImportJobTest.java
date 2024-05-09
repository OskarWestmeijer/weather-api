package westmeijer.oskar.weatherapi.importjob.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import westmeijer.oskar.weatherapi.importjob.client.OpenWeatherApiClient;
import westmeijer.oskar.weatherapi.importjob.exception.OpenWeatherApiRequestException;
import westmeijer.oskar.weatherapi.location.service.LocationService;
import westmeijer.oskar.weatherapi.location.service.model.Location;

@ExtendWith(MockitoExtension.class)
public class WeatherImportJobTest {

  private final MeterRegistry meterRegistry = new SimpleMeterRegistry();

  private final OpenWeatherApiClient openWeatherApiClient = mock(OpenWeatherApiClient.class);

  private final LocationService locationService = mock(LocationService.class);

  private WeatherImportJob weatherImportJob;

  @BeforeEach
  public void init() {
    meterRegistry.clear();
    weatherImportJob = new WeatherImportJob(meterRegistry, locationService, openWeatherApiClient);
  }

  @Test
  public void shouldImportWeather() {
    Location importLocation = mock(Location.class);
    given(locationService.getNextImportLocation()).willReturn(importLocation);

    Location updatedLocation = mock(Location.class);
    given(openWeatherApiClient.requestWeather(importLocation)).willReturn(updatedLocation);

    Location savedLocation = mock(Location.class);
    given(locationService.save(updatedLocation)).willReturn(savedLocation);

    weatherImportJob.refreshWeather();

    assertThat(meterRegistry.counter("import.job", "import", "execution").count()).isEqualTo(1.00d);
    assertThat(meterRegistry.counter("import.job", "error", "request").count()).isEqualTo(0.00d);
    assertThat(meterRegistry.counter("import.job", "error", "process").count()).isEqualTo(0.00d);

    then(locationService).should().getNextImportLocation();
    then(openWeatherApiClient).should().requestWeather(importLocation);
    then(locationService).should().save(updatedLocation);
  }

  @Test
  public void throwsOnApiRequestException() {
    Location importLocation = mock(Location.class);

    given(locationService.getNextImportLocation()).willReturn(importLocation);
    given(openWeatherApiClient.requestWeather(importLocation)).willThrow(OpenWeatherApiRequestException.class);

    weatherImportJob.refreshWeather();

    assertThat(meterRegistry.counter("import.job", "import", "execution").count()).isEqualTo(1.00d);
    assertThat(meterRegistry.counter("import.job", "error", "request").count()).isEqualTo(1.00d);
    assertThat(meterRegistry.counter("import.job", "error", "process").count()).isEqualTo(0.00d);

    then(locationService).should().getNextImportLocation();
    then(openWeatherApiClient).should().requestWeather(importLocation);
    then(locationService).should(times(0)).save(any());
  }

  @Test
  public void throwsOnNpe() {
    given(locationService.getNextImportLocation()).willReturn(null);

    weatherImportJob.refreshWeather();

    assertThat(meterRegistry.counter("import.job", "import", "execution").count()).isEqualTo(1.00d);
    assertThat(meterRegistry.counter("import.job", "error", "request").count()).isEqualTo(0.00d);
    assertThat(meterRegistry.counter("import.job", "error", "process").count()).isEqualTo(1.00d);

    then(locationService).should().getNextImportLocation();
    then(openWeatherApiClient).shouldHaveNoInteractions();
    then(locationService).should(times(0)).save(any());
  }


}
