package fr.kfokam48.presences;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Le contexte ne démarre que si les migrations Flyway correspondent exactement
 * aux entités JPA (ddl-auto=validate) : garde-fou de la cohérence D2 / V1.
 */
@SpringBootTest
class SchemaFlywayTest {

    @Test
    void lesMigrationsCorrespondentAuxEntites() {
    }
}
