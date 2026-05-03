package remitly.task.stockmarket.e2e;

import com.fasterxml.jackson.core.type.TypeReference;
import io.qameta.allure.Description;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import remitly.task.stockmarket.dto.BankResponse;
import remitly.task.stockmarket.dto.StockDto;
import remitly.task.stockmarket.dto.StockOperationRequest;
import remitly.task.stockmarket.dto.WalletResponse;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FullScenarioE2ETest extends BaseE2ETest {

    private static final String WALLET_ALICE = "alice";
    private static final String WALLET_BOB = "bob";
    private static final String STOCK_AAPL = "AAPL";
    private static final String STOCK_GOOG = "GOOG";
    private static final String BUY = "buy";
    private static final String SELL = "sell";
    private static final int AAPL_INITIAL = 10;
    private static final int GOOG_INITIAL = 5;
    private static final int ZERO = 0;

    @Test
    @Description("Full scenario — seed bank, multiple wallets buy and sell, verify stock conservation and audit log")
    void shouldCorrectlyHandleFullTradingScenario() {
        //given
        client.post()
                .uri("/stocks")
                .bodyValue(new BankResponse(List.of(
                        new StockDto(STOCK_AAPL, AAPL_INITIAL),
                        new StockDto(STOCK_GOOG, GOOG_INITIAL)
                )))
                .exchange()
                .expectStatus().isOk();
        //when
        buy(WALLET_ALICE, STOCK_AAPL);
        buy(WALLET_ALICE, STOCK_AAPL);
        buy(WALLET_BOB, STOCK_AAPL);
        buy(WALLET_BOB, STOCK_GOOG);
        sell(WALLET_ALICE, STOCK_AAPL);
        //then — wallet states
        WalletResponse alice = getWallet(WALLET_ALICE);
        WalletResponse bob = getWallet(WALLET_BOB);
        assertThat(getQuantity(alice, STOCK_AAPL)).isEqualTo(1);
        assertThat(getQuantity(bob, STOCK_AAPL)).isEqualTo(1);
        assertThat(getQuantity(bob, STOCK_GOOG)).isEqualTo(1);
        //then — bank state
        assertThat(getBankQuantity(STOCK_AAPL)).isEqualTo(AAPL_INITIAL - 2);
        assertThat(getBankQuantity(STOCK_GOOG)).isEqualTo(GOOG_INITIAL - 1);
        //then — stock conservation
        assertThat(getBankQuantity(STOCK_AAPL) + getQuantity(alice, STOCK_AAPL) + getQuantity(bob, STOCK_AAPL))
                .isEqualTo(AAPL_INITIAL);
        assertThat(getBankQuantity(STOCK_GOOG) + getQuantity(bob, STOCK_GOOG))
                .isEqualTo(GOOG_INITIAL);
        //then — audit log
        assertThat(getLog()).hasSize(5);
    }

    @Test
    @Description("Bank state reset — setting new bank state should affect subsequent buy operations")
    void shouldReflectNewBankStateInSubsequentOperations() {
        //given
        client.post()
                .uri("/stocks")
                .bodyValue(new BankResponse(List.of(new StockDto(STOCK_AAPL, AAPL_INITIAL))))
                .exchange()
                .expectStatus().isOk();
        buy(WALLET_ALICE, STOCK_AAPL);
        //when — reset bank state
        client.post()
                .uri("/stocks")
                .bodyValue(new BankResponse(List.of(new StockDto(STOCK_AAPL, ZERO))))
                .exchange()
                .expectStatus().isOk();
        //then — further buys should fail
        client.post()
                .uri("/wallets/{walletId}/stocks/{stock}", WALLET_ALICE, STOCK_AAPL)
                .bodyValue(new StockOperationRequest(BUY))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @Description("Wallet sell back all — wallet selling all stocks should restore bank to initial quantity")
    void shouldRestoreBankQuantityWhenWalletSellsAllStocks() {
        //given
        client.post()
                .uri("/stocks")
                .bodyValue(new BankResponse(List.of(new StockDto(STOCK_AAPL, AAPL_INITIAL))))
                .exchange()
                .expectStatus().isOk();
        buy(WALLET_ALICE, STOCK_AAPL);
        buy(WALLET_ALICE, STOCK_AAPL);
        buy(WALLET_ALICE, STOCK_AAPL);
        //when
        sell(WALLET_ALICE, STOCK_AAPL);
        sell(WALLET_ALICE, STOCK_AAPL);
        sell(WALLET_ALICE, STOCK_AAPL);
        //then
        assertThat(getBankQuantity(STOCK_AAPL)).isEqualTo(AAPL_INITIAL);
        assertThat(getWalletQuantity(WALLET_ALICE, STOCK_AAPL)).isEqualTo(ZERO);
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

    private WalletResponse getWallet(String walletId) {
        return client.get()
                .uri("/wallets/{walletId}", walletId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(WalletResponse.class)
                .returnResult()
                .getResponseBody();
    }

    private int getQuantity(WalletResponse wallet, String stockName) {
        if (wallet == null) return ZERO;
        return wallet.stocks().stream()
                .filter(s -> s.name().equals(stockName))
                .map(StockDto::quantity)
                .findFirst()
                .orElse(ZERO);
    }

    private int getBankQuantity(String stockName) {
        BankResponse response = client.get()
                .uri("/stocks")
                .exchange()
                .expectStatus().isOk()
                .expectBody(BankResponse.class)
                .returnResult()
                .getResponseBody();
        if (response == null) return ZERO;
        return response.stocks().stream()
                .filter(s -> s.name().equals(stockName))
                .map(StockDto::quantity)
                .findFirst()
                .orElse(ZERO);
    }

    private int getWalletQuantity(String walletId, String stockName) {
        Integer result = client.get()
                .uri("/wallets/{walletId}/stocks/{stock}", walletId, stockName)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Integer.class)
                .returnResult()
                .getResponseBody();
        return result != null ? result : ZERO;
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