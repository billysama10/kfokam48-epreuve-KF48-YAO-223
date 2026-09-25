package fr.kfokam48.presences;

import java.time.Clock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class PresencesApplication {

    public static void main(String[] args) {
        SpringApplication.run(PresencesApplication.class, args);
    }

    /** Horloge UTC injectée partout où l'heure compte (RG1), remplaçable dans les tests. */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
