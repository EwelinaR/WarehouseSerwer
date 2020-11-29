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

    @Deprecated
    @PutMapping
    public void editInstrument(@RequestBody Instrument newInstrument) {
        repository.findById(newInstrument.getId()).ifPresentOrElse(
                instrument -> updateInstrument(instrument, newInstrument),
                () -> logger.info("PUT: No instrument with id " + newInstrument.getId()));
    }

    @PutMapping("/v2")
    public ResponseEntity<String> editInstrumentWithTimestamp(@RequestBody Instrument newInstrument,
                                                              @RequestParam("timestamp") Long timestamp) {
        Instrument instrument = repository.findById(newInstrument.getId()).orElse(null);
        if (instrument == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        String conflictMessage  = updateInstrument(instrument, newInstrument, timestamp);
        return new ResponseEntity<>(conflictMessage, HttpStatus.OK);
    }

    @PutMapping("/increase/{id}/{amount}")
    public int increaseQuantity(@PathVariable long id, @PathVariable int amount) {
        return repository.findById(id).map(
                instrument -> {
                    safeIncreaseQuantity(instrument, amount);
                    return instrument.getQuantity();
                }).orElseGet(
                () -> {
                    logger.info("PUT (increase): No instrument with id " + id);
                    return -1;
                });
    }

    @PutMapping("/decrease/{id}/{amount}")
    public int decreaseQuantity(@PathVariable long id, @PathVariable int amount) {
        return repository.findById(id).map(
                instrument -> {
                    safeDecreaseQuantity(instrument, amount);
                    return instrument.getQuantity();
                }).orElseGet(
                () -> {
                    logger.info("PUT (decrease): No instrument with id " + id);
                    return -1;
                });
    }

    @PostMapping
    public void newInstrument(@RequestBody Instrument instrument) {
        if (instrument.getPrice() <= 0) {
            logger.info("POST: Price is negative (or 0). Aborting. " + instrument);
            return;
        }
        instrument.setQuantity(0);
        instrument.setId(0);
        repository.save(instrument);
        logger.info("POST: " + instrument);
    }

    @DeleteMapping("{id}")
    public void deleteInstrument(@PathVariable long id) {
        repository.findById(id).ifPresentOrElse(
                instrument -> {
                    repository.deleteById(id);
                    logger.info("DELETE: " + instrument);
                }, () -> logger.info("DELETE: No instrument with id " + id)
        );
    }

    private void updateInstrument(Instrument instrument, Instrument newInstrument) {
        if (newInstrument.getPrice() <= 0) {
            logger.info("PUT: Price is negative (or 0). Aborting. " + newInstrument);
            return;
        }
        instrument.setManufacturer(newInstrument.getManufacturer());
        instrument.setModel(newInstrument.getModel());
        instrument.setPrice(newInstrument.getPrice());
        logger.info("PUT: " + instrument);
        repository.save(instrument);
    }

    private String updateInstrument(Instrument instrument, Instrument newInstrument, Long newTimestamp) {
        System.out.println("New: "+newTimestamp);
        System.out.println("Old: "+instrument.getModelTimestamp());
        String message = "";
        String FIELD_SEPARATOR = ";";
        String PARAMETER_SEPARATOR = ":";

        if (newInstrument.getManufacturer() != null) {
            if (instrument.getManufacturerTimestamp() > newTimestamp) {
                message += "1" + PARAMETER_SEPARATOR + newInstrument.getManufacturer()
                        + PARAMETER_SEPARATOR + instrument.getManufacturer();
            } else {
                instrument.setManufacturer(newInstrument.getManufacturer());
                instrument.setManufacturerTimestamp(newTimestamp);
            }
        }
        message += FIELD_SEPARATOR;
        if (newInstrument.getModel() != null) {
            if (instrument.getModelTimestamp() > newTimestamp) {
                message += "2" + PARAMETER_SEPARATOR + newInstrument.getModel()
                        + PARAMETER_SEPARATOR + instrument.getModel();
            } else {
                instrument.setModel(newInstrument.getModel());
                instrument.setModelTimestamp(newTimestamp);
            }
        }
        message += FIELD_SEPARATOR;
        if (newInstrument.getPrice() > 0) {
            if (instrument.getPriceTimestamp() > newTimestamp) {
                message += "1" + PARAMETER_SEPARATOR + newInstrument.getPrice()
                        + PARAMETER_SEPARATOR + instrument.getPrice();
            } else {
                instrument.setPrice(newInstrument.getPrice());
                instrument.setPriceTimestamp(newTimestamp);
            }
        }
        logger.info("PUTv2: " + instrument);
        repository.save(instrument);
        return message;
    }

    private void safeIncreaseQuantity(Instrument instrument, int amount) {
        if (amount < 0) {
            logger.info("PUT (increase): Amount is negative: " + amount);
            return;
        }
        instrument.increaseQuantity(amount);
        repository.save(instrument);
        logger.info("PUT (increase): " + instrument);
    }

    private void safeDecreaseQuantity(Instrument instrument, int amount) {
        if (amount < 0) {
            logger.info("PUT (decrease): Amount is negative: " + amount);
            return;
        }
        if (instrument.getQuantity() > 0) {
            instrument.decreaseQuantity(amount);
            repository.save(instrument);
            logger.info("PUT (decrease): " + instrument);
        } else {
            logger.info("PUT (decrease): Quantity is 0! " + instrument);
        }
    }
}
