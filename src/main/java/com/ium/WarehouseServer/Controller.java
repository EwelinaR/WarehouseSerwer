package com.ium.WarehouseServer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/instruments")
public class Controller {

    private static final Logger logger = LogManager.getLogger(WarehouseServerApplication.class);
    private final InstrumentRepository repository;

    public Controller(InstrumentRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Instrument> getInstruments() {
        List<Instrument> instruments = repository.findAll();
        logger.info("GET: " + instruments);
        return instruments;
    }

    @PutMapping("/v2/{id}")
    public ResponseEntity<String> editInstrumentWithTimestamp(@PathVariable long id, @RequestBody Instrument newInstrument) {
        Instrument instrument = repository.findById(id).orElse(null);
        if (instrument == null) {
            logger.info("PUT: No instrument found");
            return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        }
        String conflictMessage  = updateInstrument(instrument, newInstrument);
        if (conflictMessage.isEmpty()) return new ResponseEntity<>("", HttpStatus.OK);
        else {
            System.out.println(conflictMessage);
            return new ResponseEntity<> (conflictMessage, HttpStatus.ALREADY_REPORTED);
        }
    }

    @PutMapping("/increase/{id}/{amount}")
    public ResponseEntity<String> increaseQuantity(@PathVariable long id, @PathVariable int amount) {
        Instrument instrument = repository.findById(id).orElse(null);
        if (instrument == null) {
            logger.info("PUT (increase): No instrument with id " + id);
            return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        }
        if (safeIncreaseQuantity(instrument, amount)) return new ResponseEntity<>("", HttpStatus.OK);
        else return new ResponseEntity<>("", HttpStatus.ALREADY_REPORTED);
    }

    @PutMapping("/decrease/{id}/{amount}")
    public ResponseEntity<String> decreaseQuantity(@PathVariable long id, @PathVariable int amount) {
        Instrument instrument = repository.findById(id).orElse(null);
        if (instrument == null) {
            logger.info("PUT (decrease): No instrument with id " + id);
            return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        }
        if (safeDecreaseQuantity(instrument, amount)) return new ResponseEntity<>("", HttpStatus.OK);
        else return new ResponseEntity<>("", HttpStatus.ALREADY_REPORTED);
    }

    @PostMapping
    public ResponseEntity<String> newInstrument(@RequestBody Instrument instrument) {
        if (instrument.getPrice() <= 0) {
            logger.info("POST: Price is negative (or 0). Aborting. " + instrument);
            return null;
        }
        instrument.setQuantity(0);
        instrument.setId(0);
        repository.save(instrument);
        logger.info("POST: " + instrument);
        return new ResponseEntity<>(String.valueOf(instrument.getId()), HttpStatus.OK);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<String> deleteInstrument(@PathVariable long id) {
        Instrument instrument = repository.findById(id).orElse(null);
        if (instrument == null) {
            logger.info("DELETE: No instrument with id " + id);
            return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        }
        logger.info("DELETE: " + instrument);
        repository.deleteById(id);
        return new ResponseEntity<>("", HttpStatus.OK);
    }

    private String updateInstrument(Instrument instrument, Instrument newInstrument) {
        String message = "";
        String FIELD_SEPARATOR = ";";

        if (!newInstrument.getManufacturer().equals(instrument.getManufacturer())) {
            if (instrument.getManufacturerTimestamp() > newInstrument.getManufacturerTimestamp()) {
                message += instrument.getManufacturer();
            } else {
                instrument.setManufacturer(newInstrument.getManufacturer());
                instrument.setManufacturerTimestamp(newInstrument.getManufacturerTimestamp());
            }
        }
        message += FIELD_SEPARATOR;
        if (!newInstrument.getModel().equals(instrument.getModel())) {
            if (instrument.getModelTimestamp() > newInstrument.getModelTimestamp()) {
                message += instrument.getModel();
            } else {
                instrument.setModel(newInstrument.getModel());
                instrument.setModelTimestamp(newInstrument.getModelTimestamp());
            }
        }
        message += FIELD_SEPARATOR;
        if (newInstrument.getPrice() != instrument.getPrice()) {
            if (instrument.getPriceTimestamp() > newInstrument.getPriceTimestamp()) {
                message += instrument.getPrice();
            } else {
                instrument.setPrice(newInstrument.getPrice());
                instrument.setPriceTimestamp(newInstrument.getPriceTimestamp());
            }
        }
        message += FIELD_SEPARATOR;
        if (newInstrument.getCategory() != instrument.getCategory() && newInstrument.getCategoryTimestamp() > 0) {
            if (instrument.getCategoryTimestamp() > newInstrument.getCategoryTimestamp()) {
                message += instrument.getCategory();
            } else {
                instrument.setCategory(newInstrument.getCategory());
                instrument.setCategoryTimestamp(newInstrument.getCategoryTimestamp());
            }
        }
        logger.info("PUT: " + instrument);
        repository.save(instrument);
        if (message.equals(FIELD_SEPARATOR + FIELD_SEPARATOR + FIELD_SEPARATOR)) {
            return "";
        }
        return message;
    }

    private boolean safeIncreaseQuantity(Instrument instrument, int amount) {
        if (amount < 0) {
            logger.info("PUT (increase): Amount is negative: " + amount);
            return false;
        }
        instrument.increaseQuantity(amount);
        repository.save(instrument);
        logger.info("PUT (increase): " + instrument);
        return true;
    }

    private boolean safeDecreaseQuantity(Instrument instrument, int amount) {
        if (amount < 0) {
            logger.info("PUT (decrease): Amount is negative: " + amount);
            return false;
        }
        if (instrument.getQuantity() > 0) {
            if (instrument.decreaseQuantity(amount)) {
                repository.save(instrument);
                logger.info("PUT (decrease): " + instrument);
                return true;
            } else {
                logger.info("PUT (decrease): New quantity is lower than 0! " + instrument);
            }
        } else {
            logger.info("PUT (decrease): Quantity is 0! " + instrument);
        }
        return false;
    }
}
