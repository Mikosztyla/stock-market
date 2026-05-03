package remitly.task.stockmarket.e2e;

import io.qameta.allure.Description;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import remitly.task.stockmarket.chaos.SystemExiter;
import remitly.task.stockmarket.config.E2ETestConfig;
import remitly.task.stockmarket.dto.BankResponse;
import remitly.task.stockmarket.dto.StockDto;
import remitly.task.stockmarket.dto.StockOperationRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.main.allow-bean-definition-overriding=true"
)
@ActiveProfiles("int")
@Import({E2ETestConfig.class, ChaosE2ETest.ChaosTestConfig.class})
@DirtiesContext
class ChaosE2ETest extends BaseE2ETest {

    @Autowired
    private SystemExiter systemExiter;

    @BeforeEach
    void resetMock() {
        Mockito.reset(systemExiter);
    }

    @Test
    @Description("POST /chaos should return HTTP 200")
    void shouldReturn200() {
        client.post()
                .uri("/chaos")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @Description("POST /chaos should call SystemExiter.exit(0) exactly once")
    void shouldCallExitOnce() throws InterruptedException {
        client.post()
                .uri("/chaos")
                .exchange()
                .expectStatus().isOk();
        Thread.sleep(200);
        verify(systemExiter, times(1)).exit(0);
    }

    @Test
    @Description("POST /chaos should return empty body")
    void shouldReturnEmptyBody() {
        byte[] body = client.post()
                .uri("/chaos")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .returnResult()
                .getResponseBody();
        assertThat(body).isNullOrEmpty();
    }

    @Test
    @Description("Application state should be intact before exit fires")
    void shouldNotAffectStateBeforeExit() {
        // given
        client.post()
                .uri("/stocks")
                .bodyValue(new BankResponse(List.of(new StockDto("AAPL", 5))))
                .exchange()
                .expectStatus().isOk();
        client.post()
                .uri("/wallets/{walletId}/stocks/{stock}", "alice", "AAPL")
                .bodyValue(new StockOperationRequest("buy"))
                .exchange()
                .expectStatus().isOk();
        // when
        client.post()
                .uri("/chaos")
                .exchange()
                .expectStatus().isOk();
        // then
        Integer quantity = client.get()
                .uri("/wallets/{walletId}/stocks/{stock}", "alice", "AAPL")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Integer.class)
                .returnResult()
                .getResponseBody();
        assertThat(quantity).isEqualTo(1);
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