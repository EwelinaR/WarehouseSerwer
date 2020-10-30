package com.ium.WarehouseServer;

import com.ium.WarehouseServer.auth.JwtUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/instruments")
public class Controller {

    private static final Logger logger = LogManager.getLogger(WarehouseServerApplication.class);
    private final InstrumentRepository repository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    InMemoryUserDetailsManager inMemoryUserDetailsManager;

    public Controller(InstrumentRepository repository) {
        this.repository = repository;
    }

    @PostMapping("/signin")
    public ResponseEntity<?> getToken(@RequestBody AuthenticationUser user) throws Exception {
     //   return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    user.getUsername(), user.getPassword()));
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }

        final UserDetails userDetails = inMemoryUserDetailsManager.loadUserByUsername(user.getUsername());
        final String token = jwtUtil.generateToken(userDetails);
        return ResponseEntity.ok(token);
    }

    @GetMapping
    public List<Instrument> getInstruments() {
        List<Instrument> instruments = repository.findAll();
        logger.info("GET: " + instruments);
        return instruments;
    }

    @PutMapping
    public void editInstrument(@RequestBody Instrument newInstrument) {
        repository.findById(newInstrument.getId()).ifPresentOrElse(
                instrument -> updateInstrument(instrument, newInstrument),
                () -> logger.info("PUT: No instrument with id " + newInstrument.getId()));
    }

    @PutMapping("/increase/{id}/{amount}")
    public int increaseQuantity(@PathVariable long id, @PathVariable int amount) {
        return repository.findById(id).map(
                instrument -> {
                    safeIncreaseQuantity(instrument, amount);
                    return instrument.getQuantity();
                }).orElseGet(
                () -> {
                    logger.info("PUT (increase): Wrong id from request: " + id);
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
                    logger.info("PUT (decrease): Wrong id from request: " + id);
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
                }, () -> logger.info("DELETE: Wrong id from request: " + id)
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
