package westmeijer.oskar.weatherapi.openweatherapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import westmeijer.oskar.weatherapi.entity.Location;
import westmeijer.oskar.weatherapi.entity.Weather;
import westmeijer.oskar.weatherapi.openweatherapi.model.OpenWeatherApiMapper;
import westmeijer.oskar.weatherapi.openweatherapi.model.OpenWeatherApiResponse;

@Component
public class OpenWeatherApiClient {

    private final WebClient webClient;

    private final String urlPathTemplate;
    private final String appId;

    private static final Logger logger = LoggerFactory.getLogger(OpenWeatherApiClient.class);

    public OpenWeatherApiClient(WebClient webClient, @Value("${openweatherapi.appId}") String appId, @Value("${openweatherapi.urlPathTemplate}") String urlPathTemplate) {
        this.webClient = webClient;
        this.urlPathTemplate = urlPathTemplate;
        this.appId = appId;
    }

    /**
     * Requests current weather from OpenWeatherApi.
     *
     * @param location
     * @return
     */
    public Weather requestWeather(Location location) {
        try {
            String urlPath = buildUrlPath(location);
            logger.debug("Built urlPath: {}", urlPath);
            OpenWeatherApiResponse response = webClient.get().uri(urlPath).retrieve().bodyToMono(OpenWeatherApiResponse.class).block();
            logger.debug("OpenWeatherApiResponse: {}", response);
            return OpenWeatherApiMapper.map(response, location.getLocalZipCode());
        } catch (Exception e) {
            throw new OpenWeatherApiRequestException("Exception during OpenWeatherApi request.", e);
        }
    }

    private String buildUrlPath(Location location) {
        String path = urlPathTemplate.replace(RequestParamPlaceholders.LOCATION_CODE_CHAR_SEQUENCE.getValue(), String.valueOf(location.getLocationCode()));
        return path.replace(RequestParamPlaceholders.APP_ID_CHAR_SEQUENCE.getValue(), appId);
    }

}
