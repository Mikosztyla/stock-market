package remitly.task.stockmarket.e2e;

import io.qameta.allure.Description;
import org.junit.jupiter.api.Test;
import remitly.task.stockmarket.dto.BankResponse;
import remitly.task.stockmarket.dto.StockDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BankE2ETest extends BaseE2ETest {

    private static final String STOCK_NAME = "AAPL";
    private static final String ANOTHER_STOCK_NAME = "GOOG";
    private static final int INITIAL_QUANTITY = 10;
    private static final int ANOTHER_QUANTITY = 5;
    private static final int ZERO_QUANTITY = 0;

    @Test
    @Description("GET /stocks should return empty list when bank has no stocks")
    void shouldReturnEmptyStocksWhenBankIsEmpty() {
        //when
        BankResponse response = client.get()
                .uri("/stocks")
                .exchange()
                .expectStatus().isOk()
                .expectBody(BankResponse.class)
                .returnResult()
                .getResponseBody();
        //then
        assertThat(response).isNotNull();
        assertThat(response.stocks()).isEmpty();
    }

    @Test
    @Description("POST /stocks should set bank state and GET /stocks should return it")
    void shouldSetAndReturnBankState() {
        //given
        BankResponse request = new BankResponse(List.of(new StockDto(STOCK_NAME, INITIAL_QUANTITY)));
        //when
        client.post()
                .uri("/stocks")
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk();
        //then
        BankResponse response = client.get()
                .uri("/stocks")
                .exchange()
                .expectStatus().isOk()
                .expectBody(BankResponse.class)
                .returnResult()
                .getResponseBody();
        assertThat(response).isNotNull();
        assertThat(response.stocks()).hasSize(1);
        assertThat(response.stocks().get(0).name()).isEqualTo(STOCK_NAME);
        assertThat(response.stocks().get(0).quantity()).isEqualTo(INITIAL_QUANTITY);
    }

    @Test
    @Description("POST /stocks should replace existing bank state entirely")
    void shouldReplaceExistingBankStateWhenNewStateIsSet() {
        //given
        client.post()
                .uri("/stocks")
                .bodyValue(new BankResponse(List.of(new StockDto(STOCK_NAME, INITIAL_QUANTITY))))
                .exchange()
                .expectStatus().isOk();
        //when
        client.post()
                .uri("/stocks")
                .bodyValue(new BankResponse(List.of(new StockDto(ANOTHER_STOCK_NAME, ANOTHER_QUANTITY))))
                .exchange()
                .expectStatus().isOk();
        //then
        BankResponse response = client.get()
                .uri("/stocks")
                .exchange()
                .expectStatus().isOk()
                .expectBody(BankResponse.class)
                .returnResult()
                .getResponseBody();
        assertThat(response).isNotNull();
        assertThat(response.stocks()).hasSize(1);
        assertThat(response.stocks().get(0).name()).isEqualTo(ANOTHER_STOCK_NAME);
    }

    @Test
    @Description("POST /stocks should accept zero quantity stock")
    void shouldAcceptZeroQuantityWhenSettingBankState() {
        //given
        BankResponse request = new BankResponse(List.of(new StockDto(STOCK_NAME, ZERO_QUANTITY)));
        //when
        client.post()
                .uri("/stocks")
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk();
        //then
        BankResponse response = client.get()
                .uri("/stocks")
                .exchange()
                .expectStatus().isOk()
                .expectBody(BankResponse.class)
                .returnResult()
                .getResponseBody();
        assertThat(response).isNotNull();
        assertThat(response.stocks().get(0).quantity()).isEqualTo(ZERO_QUANTITY);
    }

    @Test
    @Description("POST /stocks should accept multiple stocks at once")
    void shouldSetMultipleStocksAtOnce() {
        //given
        BankResponse request = new BankResponse(List.of(
                new StockDto(STOCK_NAME, INITIAL_QUANTITY),
                new StockDto(ANOTHER_STOCK_NAME, ANOTHER_QUANTITY)
        ));
        //when
        client.post()
                .uri("/stocks")
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk();
        //then
        BankResponse response = client.get()
                .uri("/stocks")
                .exchange()
                .expectStatus().isOk()
                .expectBody(BankResponse.class)
                .returnResult()
                .getResponseBody();
        assertThat(response).isNotNull();
        assertThat(response.stocks()).hasSize(2);
    }
}