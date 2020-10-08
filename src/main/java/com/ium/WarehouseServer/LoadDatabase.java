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
            log.info("Preloading " + repository.save(new Instrument(1, "Yamaha", "Piano c3", (float) 1200.30, 3)));
            log.info("Preloading " + repository.save(new Instrument(2, "Yamaha", "Fortepian 3000", (float) 33200.30, 1)));
        };
    }
}
