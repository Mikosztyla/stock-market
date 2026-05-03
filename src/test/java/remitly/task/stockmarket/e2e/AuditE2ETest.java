package remitly.task.stockmarket.e2e;

import com.fasterxml.jackson.core.type.TypeReference;
import io.qameta.allure.Description;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import remitly.task.stockmarket.dto.BankResponse;
import remitly.task.stockmarket.dto.StockDto;
import remitly.task.stockmarket.dto.StockOperationRequest;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AuditE2ETest extends BaseE2ETest {

    private static final String WALLET_ID = "alice";
    private static final String ANOTHER_WALLET_ID = "bob";
    private static final String STOCK_NAME = "AAPL";
    private static final String BUY = "buy";
    private static final String SELL = "sell";
    private static final int INITIAL_BANK_QUANTITY = 10;
    private static final int ZERO = 0;

    @Test
    @Description("GET /log should return empty log when no operations have been performed")
    void shouldReturnEmptyLogWhenNoOperationsPerformed() {
        //when
        List<Map<String, Object>> log = getLog();
        //then
        assertThat(log).isEmpty();
    }

    @Test
    @Description("GET /log should contain buy entry with correct fields after buying a stock")
    void shouldContainBuyEntryWithCorrectFieldsAfterBuying() {
        //given
        seedBank(STOCK_NAME, INITIAL_BANK_QUANTITY);
        buy(WALLET_ID, STOCK_NAME);
        //when
        List<Map<String, Object>> log = getLog();
        //then
        assertThat(log).hasSize(1);
        assertThat(log.get(0).get("type")).isEqualTo(BUY);
        assertThat(log.get(0).get("walletId")).isEqualTo(WALLET_ID);
        assertThat(log.get(0).get("stockName")).isEqualTo(STOCK_NAME);
    }

    @Test
    @Description("GET /log should contain sell entry with correct fields after selling a stock")
    void shouldContainSellEntryWithCorrectFieldsAfterSelling() {
        //given
        seedBank(STOCK_NAME, INITIAL_BANK_QUANTITY);
        buy(WALLET_ID, STOCK_NAME);
        sell(WALLET_ID, STOCK_NAME);
        //when
        List<Map<String, Object>> log = getLog();
        //then
        assertThat(log).hasSize(2);
        assertThat(log.get(1).get("type")).isEqualTo(SELL);
        assertThat(log.get(1).get("walletId")).isEqualTo(WALLET_ID);
        assertThat(log.get(1).get("stockName")).isEqualTo(STOCK_NAME);
    }

    @Test
    @Description("GET /log should return entries in order of occurrence")
    void shouldReturnEntriesInOrderOfOccurrence() {
        //given
        seedBank(STOCK_NAME, INITIAL_BANK_QUANTITY);
        buy(WALLET_ID, STOCK_NAME);
        buy(WALLET_ID, STOCK_NAME);
        sell(WALLET_ID, STOCK_NAME);
        //when
        List<Map<String, Object>> log = getLog();
        //then
        assertThat(log).hasSize(3);
        assertThat(log.get(0).get("type")).isEqualTo(BUY);
        assertThat(log.get(1).get("type")).isEqualTo(BUY);
        assertThat(log.get(2).get("type")).isEqualTo(SELL);
    }

    @Test
    @Description("GET /log should not contain entry when buy fails due to insufficient bank stock")
    void shouldNotLogFailedBuyOperation() {
        //given
        seedBank(STOCK_NAME, ZERO);
        client.post()
                .uri("/wallets/{walletId}/stocks/{stock}", WALLET_ID, STOCK_NAME)
                .bodyValue(new StockOperationRequest(BUY))
                .exchange()
                .expectStatus().isBadRequest();
        //when
        List<Map<String, Object>> log = getLog();
        //then
        assertThat(log).isEmpty();
    }

    @Test
    @Description("GET /log should not contain entry when sell fails due to insufficient wallet stock")
    void shouldNotLogFailedSellOperation() {
        //given
        seedBank(STOCK_NAME, INITIAL_BANK_QUANTITY);
        client.post()
                .uri("/wallets/{walletId}/stocks/{stock}", WALLET_ID, STOCK_NAME)
                .bodyValue(new StockOperationRequest(SELL))
                .exchange()
                .expectStatus().isBadRequest();
        //when
        List<Map<String, Object>> log = getLog();
        //then
        assertThat(log).isEmpty();
    }

    @Test
    @Description("GET /log should log operations from multiple wallets")
    void shouldLogOperationsFromMultipleWallets() {
        //given
        seedBank(STOCK_NAME, INITIAL_BANK_QUANTITY);
        buy(WALLET_ID, STOCK_NAME);
        buy(ANOTHER_WALLET_ID, STOCK_NAME);
        //when
        List<Map<String, Object>> log = getLog();
        //then
        assertThat(log).hasSize(2);
        assertThat(log.get(0).get("walletId")).isEqualTo(WALLET_ID);
        assertThat(log.get(1).get("walletId")).isEqualTo(ANOTHER_WALLET_ID);
    }

    @Test
    @Description("GET /log should contain no more than 10000 entries as per requirements")
    void shouldHandleUpToTenThousandLogEntries() {
        //given
        seedBank(STOCK_NAME, INITIAL_BANK_QUANTITY);
        buy(WALLET_ID, STOCK_NAME);
        sell(WALLET_ID, STOCK_NAME);
        buy(WALLET_ID, STOCK_NAME);
        sell(WALLET_ID, STOCK_NAME);
        buy(WALLET_ID, STOCK_NAME);
        //when
        List<Map<String, Object>> log = getLog();
        //then
        assertThat(log.size()).isLessThanOrEqualTo(10000);
    }

    private void seedBank(String stockName, int quantity) {
        client.post()
                .uri("/stocks")
                .bodyValue(new BankResponse(List.of(new StockDto(stockName, quantity))))
                .exchange()
                .expectStatus().isOk();
    }

    private void buy(String walletId, String stockName) {
        client.post()
                .uri("/wallets/{walletId}/stocks/{stock}", walletId, stockName)
                .bodyValue(new StockOperationRequest(BUY))
                .exchange()
                .expectStatus().isOk();
    }

    private void sell(String walletId, String stockName) {
        client.post()
                .uri("/wallets/{walletId}/stocks/{stock}", walletId, stockName)
                .bodyValue(new StockOperationRequest(SELL))
                .exchange()
                .expectStatus().isOk();
    }

    private List<Map<String, Object>> getLog() {
        String body = client.get()
                .uri("/log")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();
        try {
            Map<String, List<Map<String, Object>>> response = objectMapper.readValue(
                    body, new TypeReference<>() {});
            return response.get("log");
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse log response", e);
        }
    }
}