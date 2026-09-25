package fr.kfokam48.presences;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/**
 * #28 : V3 appliquée après V1 et les données de démonstration V2, sur une base dédiée.
 * Les données existantes survivent et le schéma accepte désormais deux relecteurs par exercice.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:migrationv3;DB_CLOSE_DELAY=-1",
        "spring.flyway.locations=classpath:db/migration,classpath:db/demo"})
@Transactional
class MigrationV3Test {

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void lesMigrationsV1V2V3SontAppliqueesDansLOrdre() {
        List<String> versions = jdbc.queryForList(
                "select \"version\" from \"flyway_schema_history\" where \"success\" and \"version\" is not null order by \"installed_rank\"", String.class);
        assertThat(versions).containsExactly("1", "2", "3");
    }

    @Test
    void lesDonneesDeDemonstrationSurviventAV3() {
        assertThat(jdbc.queryForObject("select count(*) from etudiant", Long.class)).isEqualTo(6);
        assertThat(jdbc.queryForObject("select count(*) from exercice", Long.class)).isEqualTo(4);
        assertThat(jdbc.queryForObject("select count(*) from relecture", Long.class)).isEqualTo(4);
        assertThat(jdbc.queryForList("select note from relecture where note is not null order by note", Integer.class))
                .containsExactly(12, 15);
    }

    @Test
    void unExercicePeutRecevoirUnSecondRelecteurMaisPasDeuxFoisLeMeme() {
        Long exercice = jdbc.queryForObject(
                "select x.id from exercice x join etudiant e on e.id = x.etudiant_id where e.nom = 'Carine Mbarga'", Long.class);
        Long awa = jdbc.queryForObject("select id from etudiant where nom = 'Awa Ngono'", Long.class);
        Long boris = jdbc.queryForObject("select id from etudiant where nom = 'Boris Kamga'", Long.class);

        // Awa relit déjà Carine (V2) : Boris peut devenir le second relecteur (RG8)
        jdbc.update("insert into relecture (exercice_id, relecteur_id, attribuee_at) values (?, ?, current_timestamp)",
                exercice, boris);
        assertThat(jdbc.queryForObject("select count(*) from relecture where exercice_id = ?", Long.class, exercice))
                .isEqualTo(2);

        // RG22 : Awa ne peut pas être attribuée une seconde fois au même exercice
        assertThatThrownBy(() -> jdbc.update(
                "insert into relecture (exercice_id, relecteur_id, attribuee_at) values (?, ?, current_timestamp)",
                exercice, awa)).isInstanceOf(DataIntegrityViolationException.class);
    }
}
