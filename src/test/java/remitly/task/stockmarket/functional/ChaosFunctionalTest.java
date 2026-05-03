package remitly.task.stockmarket.functional;

import io.qameta.allure.Description;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import remitly.task.stockmarket.chaos.SystemExiter;
import remitly.task.stockmarket.config.TestConfig;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@AutoConfigureMockMvc
@ActiveProfiles("int")
@Import({TestConfig.class, ChaosFunctionalTest.ChaosTestConfig.class})
@DirtiesContext
class ChaosFunctionalTest extends BaseFunctionalTest {

    @Autowired
    private SystemExiter systemExiter;

    @BeforeEach
    void resetMock() {
        Mockito.reset(systemExiter);
    }

    @Test
    @Description("POST /chaos should return HTTP 200")
    void shouldReturn200() throws Exception {
        mockMvc.perform(post("/chaos"))
                .andExpect(status().isOk());
    }

    @Test
    @Description("POST /chaos should call SystemExiter.exit(0) exactly once")
    void shouldCallExitOnce() throws Exception {
        mockMvc.perform(post("/chaos"))
                .andExpect(status().isOk());
        Thread.sleep(200);
        verify(systemExiter, times(1)).exit(0);
    }

    @Test
    @Description("POST /chaos should return an empty body")
    void shouldReturnEmptyBody() throws Exception {
        mockMvc.perform(post("/chaos"))
                .andExpect(status().isOk())
                .andExpect(result ->
                        org.assertj.core.api.Assertions
                                .assertThat(result.getResponse().getContentAsString()).isEmpty());
    }

    @TestConfiguration
    static class ChaosTestConfig {
        @Bean
        @Primary
        SystemExiter systemExiter() {
            return Mockito.mock(SystemExiter.class);
        }
    }
}