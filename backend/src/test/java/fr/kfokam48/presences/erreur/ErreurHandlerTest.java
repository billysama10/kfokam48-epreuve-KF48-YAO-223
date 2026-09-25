package fr.kfokam48.presences.erreur;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

/** ENF4 : même une route inconnue répond au format { code, message }. */
@SpringBootTest
@AutoConfigureMockMvc
class ErreurHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void uneRouteInconnueRenvoieLeFormatImpose() throws Exception {
        mockMvc.perform(get("/api/inexistant"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESSOURCE_INTROUVABLE"))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.trace").doesNotExist());
    }
}
