package fr.kfokam48.presences;

import java.security.SecureRandom;
import java.time.Clock;
import java.util.random.RandomGenerator;

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

    /** Hasard du tirage du relecteur (RG9), remplaçable dans les tests. */
    @Bean
    public RandomGenerator hasard() {
        return new SecureRandom();
    }
}
