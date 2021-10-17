package westmeijer.oskar.carrentalservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;


@RestController
public class GarageController {

    Logger logger = LoggerFactory.getLogger(GarageController.class);

    private GarageService garageService;

    public GarageController(GarageService garageService) {
        this.garageService = garageService;
    }

    @GetMapping(value = "/garage/cars")
    public Collection<String> getCars() {
        logger.debug("Looking up all cars.");
        return garageService.getCars();
    }

}
