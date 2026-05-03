package remitly.task.stockmarket.functional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import remitly.task.stockmarket.chaos.SystemExiter;
import remitly.task.stockmarket.config.ChaosTestConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(ChaosTestConfig.class)
@DirtiesContext
class ChaosFunctionalTest extends BaseFunctionalTest {

    private static final String CHAOS_ENDPOINT = "/chaos";
    private static final int EXIT_CODE_SUCCESS = 0;
    private static final int ONCE = 1;
    private static final long ASYNC_WAIT_MS = 200L;

    @Autowired
    private SystemExiter systemExiter;

    @BeforeEach
    void resetMock() {
        Mockito.reset(systemExiter);
    }

    @Test
    void shouldReturnHttp200() throws Exception {
        //when
        mockMvc.perform(post(CHAOS_ENDPOINT))
                .andExpect(status().isOk());
    }

    @Test
    void shouldCallSystemExiterWithCodeZeroExactlyOnce() throws Exception {
        //when
        mockMvc.perform(post(CHAOS_ENDPOINT))
                .andExpect(status().isOk());
        Thread.sleep(ASYNC_WAIT_MS);
        //then
        verify(systemExiter, times(ONCE)).exit(EXIT_CODE_SUCCESS);
    }

    @Test
    void shouldReturnEmptyBody() throws Exception {
        //when
        mockMvc.perform(post(CHAOS_ENDPOINT))
                .andExpect(status().isOk())
                //then
                .andExpect(result -> assertThat(result.getResponse().getContentAsString()).isEmpty());
    }
}