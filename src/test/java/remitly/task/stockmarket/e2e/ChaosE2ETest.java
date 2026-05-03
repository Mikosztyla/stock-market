package remitly.task.stockmarket.e2e;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import remitly.task.stockmarket.chaos.SystemExiter;
import remitly.task.stockmarket.config.ChaosTestConfig;
import remitly.task.stockmarket.dto.BankResponse;
import remitly.task.stockmarket.dto.StockDto;
import remitly.task.stockmarket.dto.StockOperationRequest;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Import(ChaosTestConfig.class)
@DirtiesContext
class ChaosE2ETest extends BaseE2ETest {

    private static final String CHAOS_ENDPOINT = "/chaos";
    private static final String STOCKS_ENDPOINT = "/stocks";
    private static final String WALLET_STOCK_ENDPOINT = "/wallets/{walletId}/stocks/{stock}";
    private static final String STOCK_NAME = "AAPL";
    private static final String WALLET_ID = "alice";
    private static final String BUY = "buy";
    private static final int INITIAL_QUANTITY = 5;
    private static final int EXPECTED_WALLET_QUANTITY = 1;
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
    void shouldReturnHttp200() {
        //when
        client.post()
                .uri(CHAOS_ENDPOINT)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void shouldCallSystemExiterWithCodeZeroExactlyOnce() throws InterruptedException {
        //when
        client.post()
                .uri(CHAOS_ENDPOINT)
                .exchange()
                .expectStatus().isOk();
        Thread.sleep(ASYNC_WAIT_MS);
        //then
        verify(systemExiter, times(ONCE)).exit(EXIT_CODE_SUCCESS);
    }

    @Test
    void shouldReturnEmptyBody() {
        //when
        byte[] body = client.post()
                .uri(CHAOS_ENDPOINT)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .returnResult()
                .getResponseBody();
        //then
        assertThat(body).isNullOrEmpty();
    }

    @Test
    void shouldNotAffectExistingStateBeforeExit() {
        //given
        client.post()
                .uri(STOCKS_ENDPOINT)
                .bodyValue(new BankResponse(List.of(new StockDto(STOCK_NAME, INITIAL_QUANTITY))))
                .exchange()
                .expectStatus().isOk();
        client.post()
                .uri(WALLET_STOCK_ENDPOINT, WALLET_ID, STOCK_NAME)
                .bodyValue(new StockOperationRequest(BUY))
                .exchange()
                .expectStatus().isOk();
        //when
        client.post()
                .uri(CHAOS_ENDPOINT)
                .exchange()
                .expectStatus().isOk();
        //then
        Integer quantity = client.get()
                .uri(WALLET_STOCK_ENDPOINT, WALLET_ID, STOCK_NAME)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Integer.class)
                .returnResult()
                .getResponseBody();
        assertThat(quantity).isEqualTo(EXPECTED_WALLET_QUANTITY);
    }
}