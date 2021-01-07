package com.ium.WarehouseServer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoadDatabase {
    private static final Logger log = LogManager.getLogger(WarehouseServerApplication.class);

    @Bean
    CommandLineRunner initDatabase(InstrumentRepository repository) {
        return args -> {
            log.info("Preloading " + repository.save(new Instrument(1, "Dowina", "Puella D′12 acoustic guitar", (float) 350.00, 0, 3)));
            log.info("Preloading " + repository.save(new Instrument(2, "Yamaha", "Piano c3", (float) 5200.30, 3, 3)));
            log.info("Preloading " + repository.save(new Instrument(3, "Yamaha", "Fortepian 3000", (float) 33200.00, 1, 3)));
            log.info("Preloading " + repository.save(new Instrument(4, "HORA", " V100 Student Violin", (float) 1250.00, 15)));
            log.info("Preloading " + repository.save(new Instrument(5, "Medeli", "Perkusja DD-400", (float) 1600.0, 10, 1)));
        };
    }
}
