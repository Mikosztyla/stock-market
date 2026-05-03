package remitly.task.stockmarket.e2e;

import io.qameta.allure.Description;
import org.junit.jupiter.api.Test;
import remitly.task.stockmarket.dto.BankResponse;
import remitly.task.stockmarket.dto.StockDto;
import remitly.task.stockmarket.dto.StockOperationRequest;
import remitly.task.stockmarket.dto.WalletResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WalletE2ETest extends BaseE2ETest {

    private static final String WALLET_ID = "alice";
    private static final String ANOTHER_WALLET_ID = "bob";
    private static final String STOCK_NAME = "AAPL";
    private static final String ANOTHER_STOCK_NAME = "GOOG";
    private static final String UNKNOWN_STOCK_NAME = "UNKNOWN";
    private static final String BUY = "buy";
    private static final String SELL = "sell";
    private static final String UNKNOWN_TYPE = "transfer";
    private static final int INITIAL_BANK_QUANTITY = 10;
    private static final int ZERO = 0;
    private static final int ONE = 1;
    private static final int TWO = 2;
    private static final int THREE = 3;

    @Test
    @Description("POST /wallets/{walletId}/stocks/{stock} buy should return 200 and decrease bank stock by 1")
    void shouldDecreaseBankStockByOneWhenBuying() {
        //given
        seedBank(STOCK_NAME, INITIAL_BANK_QUANTITY);
        //when
        buy(WALLET_ID, STOCK_NAME);
        //then
        assertThat(getBankQuantity(STOCK_NAME)).isEqualTo(INITIAL_BANK_QUANTITY - ONE);
    }

    @Test
    @Description("POST /wallets/{walletId}/stocks/{stock} buy should increase wallet stock by 1")
    void shouldIncreaseWalletStockByOneWhenBuying() {
        //given
        seedBank(STOCK_NAME, INITIAL_BANK_QUANTITY);
        //when
        buy(WALLET_ID, STOCK_NAME);
        //then
        assertThat(getWalletQuantity(WALLET_ID, STOCK_NAME)).isEqualTo(ONE);
    }

    @Test
    @Description("POST /wallets/{walletId}/stocks/{stock} buy should create wallet when it does not exist")
    void shouldCreateWalletAutomaticallyOnFirstBuy() {
        //given
        seedBank(STOCK_NAME, INITIAL_BANK_QUANTITY);
        //when
        buy(WALLET_ID, STOCK_NAME);
        //then
        WalletResponse wallet = getWallet(WALLET_ID);
        assertThat(wallet.id()).isEqualTo(WALLET_ID);
        assertThat(wallet.stocks()).hasSize(ONE);
    }

    @Test
    @Description("POST /wallets/{walletId}/stocks/{stock} buy should return 404 when stock does not exist in bank")
    void shouldReturn404WhenBuyingNonExistentStock() {
        //when //then
        client.post()
                .uri("/wallets/{walletId}/stocks/{stock}", WALLET_ID, UNKNOWN_STOCK_NAME)
                .bodyValue(new StockOperationRequest(BUY))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Description("POST /wallets/{walletId}/stocks/{stock} buy should return 400 when bank has no stock available")
    void shouldReturn400WhenBankHasInsufficientStock() {
        //given
        seedBank(STOCK_NAME, ZERO);
        //when //then
        client.post()
                .uri("/wallets/{walletId}/stocks/{stock}", WALLET_ID, STOCK_NAME)
                .bodyValue(new StockOperationRequest(BUY))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @Description("POST /wallets/{walletId}/stocks/{stock} sell should return 200 and increase bank stock by 1")
    void shouldIncreaseBankStockByOneWhenSelling() {
        //given
        seedBank(STOCK_NAME, INITIAL_BANK_QUANTITY);
        buy(WALLET_ID, STOCK_NAME);
        //when
        sell(WALLET_ID, STOCK_NAME);
        //then
        assertThat(getBankQuantity(STOCK_NAME)).isEqualTo(INITIAL_BANK_QUANTITY);
    }

    @Test
    @Description("POST /wallets/{walletId}/stocks/{stock} sell should decrease wallet stock by 1")
    void shouldDecreaseWalletStockByOneWhenSelling() {
        //given
        seedBank(STOCK_NAME, INITIAL_BANK_QUANTITY);
        buy(WALLET_ID, STOCK_NAME);
        buy(WALLET_ID, STOCK_NAME);
        //when
        sell(WALLET_ID, STOCK_NAME);
        //then
        assertThat(getWalletQuantity(WALLET_ID, STOCK_NAME)).isEqualTo(ONE);
    }

    @Test
    @Description("POST /wallets/{walletId}/stocks/{stock} sell should return 400 when wallet has no stock")
    void shouldReturn400WhenWalletHasNoStockToSell() {
        //given
        seedBank(STOCK_NAME, INITIAL_BANK_QUANTITY);
        //when //then
        client.post()
                .uri("/wallets/{walletId}/stocks/{stock}", WALLET_ID, STOCK_NAME)
                .bodyValue(new StockOperationRequest(SELL))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @Description("POST /wallets/{walletId}/stocks/{stock} should return 400 when operation type is unknown")
    void shouldReturn400WhenOperationTypeIsUnknown() {
        //given
        seedBank(STOCK_NAME, INITIAL_BANK_QUANTITY);
        //when //then
        client.post()
                .uri("/wallets/{walletId}/stocks/{stock}", WALLET_ID, STOCK_NAME)
                .bodyValue(new StockOperationRequest(UNKNOWN_TYPE))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @Description("GET /wallets/{walletId} should return empty stocks for non-existent wallet")
    void shouldReturnEmptyStocksForNonExistentWallet() {
        //when
        WalletResponse response = getWallet(WALLET_ID);
        //then
        assertThat(response.stocks()).isEmpty();
    }

    @Test
    @Description("GET /wallets/{walletId} should return all stocks owned by wallet")
    void shouldReturnAllStocksOwnedByWallet() {
        //given
        seedBank(STOCK_NAME, INITIAL_BANK_QUANTITY, ANOTHER_STOCK_NAME, INITIAL_BANK_QUANTITY);
        buy(WALLET_ID, STOCK_NAME);
        buy(WALLET_ID, ANOTHER_STOCK_NAME);
        //when
        WalletResponse response = getWallet(WALLET_ID);
        //then
        assertThat(response.id()).isEqualTo(WALLET_ID);
        assertThat(response.stocks()).hasSize(TWO);
    }

    @Test
    @Description("GET /wallets/{walletId}/stocks/{stock} should return 0 for stock not owned by wallet")
    void shouldReturnZeroForStockNotOwnedByWallet() {
        //when
        int quantity = getWalletQuantity(WALLET_ID, STOCK_NAME);
        //then
        assertThat(quantity).isEqualTo(ZERO);
    }

    @Test
    @Description("GET /wallets/{walletId}/stocks/{stock} should return correct quantity after multiple buys")
    void shouldReturnCorrectQuantityAfterMultipleBuys() {
        //given
        seedBank(STOCK_NAME, INITIAL_BANK_QUANTITY);
        buy(WALLET_ID, STOCK_NAME);
        buy(WALLET_ID, STOCK_NAME);
        buy(WALLET_ID, STOCK_NAME);
        //when
        int quantity = getWalletQuantity(WALLET_ID, STOCK_NAME);
        //then
        assertThat(quantity).isEqualTo(THREE);
    }

    @Test
    @Description("Multiple wallets should be independent — buying in one should not affect another")
    void shouldKeepWalletsIndependentOfEachOther() {
        //given
        seedBank(STOCK_NAME, INITIAL_BANK_QUANTITY);
        buy(WALLET_ID, STOCK_NAME);
        buy(WALLET_ID, STOCK_NAME);
        buy(ANOTHER_WALLET_ID, STOCK_NAME);
        //when
        int aliceQuantity = getWalletQuantity(WALLET_ID, STOCK_NAME);
        int bobQuantity = getWalletQuantity(ANOTHER_WALLET_ID, STOCK_NAME);
        //then
        assertThat(aliceQuantity).isEqualTo(TWO);
        assertThat(bobQuantity).isEqualTo(ONE);
    }

    @Test
    @Description("Stock conservation invariant — bank + all wallets should always equal initial quantity")
    void shouldConserveStockQuantityAcrossBankAndWallets() {
        //given
        seedBank(STOCK_NAME, INITIAL_BANK_QUANTITY);
        buy(WALLET_ID, STOCK_NAME);
        buy(WALLET_ID, STOCK_NAME);
        buy(ANOTHER_WALLET_ID, STOCK_NAME);
        sell(WALLET_ID, STOCK_NAME);
        //when
        int bankQuantity = getBankQuantity(STOCK_NAME);
        int aliceQuantity = getWalletQuantity(WALLET_ID, STOCK_NAME);
        int bobQuantity = getWalletQuantity(ANOTHER_WALLET_ID, STOCK_NAME);
        //then
        assertThat(bankQuantity + aliceQuantity + bobQuantity).isEqualTo(INITIAL_BANK_QUANTITY);
    }

    private void seedBank(String stockName, int quantity) {
        client.post()
                .uri("/stocks")
                .bodyValue(new BankResponse(List.of(new StockDto(stockName, quantity))))
                .exchange()
                .expectStatus().isOk();
    }

    private void seedBank(String stock1, int qty1, String stock2, int qty2) {
        client.post()
                .uri("/stocks")
                .bodyValue(new BankResponse(List.of(
                        new StockDto(stock1, qty1),
                        new StockDto(stock2, qty2)
                )))
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

    private WalletResponse getWallet(String walletId) {
        return client.get()
                .uri("/wallets/{walletId}", walletId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(WalletResponse.class)
                .returnResult()
                .getResponseBody();
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
}