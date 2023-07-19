package westmeijer.oskar.weatherapi.controller.model;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import westmeijer.oskar.weatherapi.entity.Location;
import westmeijer.oskar.weatherapi.entity.Weather;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;


public class WeatherMapperTest {

    @Test
    public void successfulMappingToResponse() {
        Location location = new Location("1234", "5678", "Luebeck", "Germany", Instant.now(), Instant.now());
        List<Weather> weatherList = List.of(new Weather(UUID.randomUUID(), 12.00d, 45, 10.55d, "1234", Instant.now(), Instant.now()),
                new Weather(UUID.randomUUID(), 5.00d, 30, 4.00d, "1234", Instant.now(), Instant.now()));

        WeatherResponse weatherResponse = WeatherMapper.INSTANCE.mapTo(location, weatherList);

        Assertions.assertThat(weatherResponse.getCityName()).isEqualTo(location.getCityName());
        Assertions.assertThat(weatherResponse.getCountry()).isEqualTo(location.getCountry());
        Assertions.assertThat(weatherResponse.getLocalZipCode()).isEqualTo(String.valueOf(location.getLocalZipCode()));
        Assertions.assertThat(weatherResponse.getResponseTimestamp()).isNotNull();
        Assertions.assertThat(weatherResponse.getTimeFormat()).isEqualTo("UTC");
    }

    @Test
    public void successfulMappingToWeatherDTO() {
        Weather weather = new Weather(UUID.randomUUID(), 12.00d, 45, 10.55d, "1234", Instant.now(), Instant.now());

        WeatherDTO weatherDTO = WeatherMapper.INSTANCE.mapTo(weather);

        Assertions.assertThat(weatherDTO.id()).isEqualTo(weather.getId());
        Assertions.assertThat(weatherDTO.humidity()).isEqualTo(weather.getHumidity());
        Assertions.assertThat(weatherDTO.recordedAt()).isEqualTo(weather.getRecordedAt().truncatedTo(ChronoUnit.SECONDS));
        Assertions.assertThat(weatherDTO.temperature()).isEqualTo(weather.getTemperature());
        Assertions.assertThat(weatherDTO.windSpeed()).isEqualTo(weather.getWindSpeed());
    }

}
