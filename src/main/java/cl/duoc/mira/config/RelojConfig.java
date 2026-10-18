package cl.duoc.mira.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Reloj inyectable: las pruebas fijan la hora en lugar de depender del momento en que corren. */
@Configuration
public class RelojConfig {

    @Bean
    Clock clock() {
        return Clock.system(ZoneId.of("America/Santiago"));
    }
}
