package remitly.task.stockmarket.functional;

import io.qameta.allure.Description;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import remitly.task.stockmarket.dto.StockDto;
import remitly.task.stockmarket.dto.WalletResponse;
import remitly.task.stockmarket.steps.BankSteps;
import remitly.task.stockmarket.steps.WalletSteps;

import static org.assertj.core.api.Assertions.assertThat;

class WalletQueryFunctionalTest extends BaseFunctionalTest {

    private static final String WALLET_ID = "alice";
    private static final String STOCK_NAME = "AAPL";
    private static final String ANOTHER_STOCK = "GOOG";
    private static final int INITIAL_BANK_QUANTITY = 10;
    private static final int ZERO = 0;

    @Autowired
    private BankSteps bankSteps;

    @Autowired
    private WalletSteps walletSteps;

    @Test
    @Description("GET /wallets/{walletId} should return empty stocks for non-existent wallet")
    void shouldReturnEmptyStocksForNonExistentWallet() throws Exception {
        //when
        WalletResponse response = walletSteps.getWallet(WALLET_ID);
        //then
        assertThat(response.stocks()).isEmpty();
    }

    @Test
    @Description("GET /wallets/{walletId} should return wallet with correct stocks after buy")
    void shouldReturnWalletWithCorrectStocksAfterBuy() throws Exception {
        //given
        bankSteps.seedBank(STOCK_NAME, INITIAL_BANK_QUANTITY);
        walletSteps.buyStock(WALLET_ID, STOCK_NAME);
        //when
        WalletResponse response = walletSteps.getWallet(WALLET_ID);
        //then
        assertThat(response.id()).isEqualTo(WALLET_ID);
        assertThat(response.stocks()).hasSize(1);
        assertThat(response.stocks().get(0).name()).isEqualTo(STOCK_NAME);
        assertThat(response.stocks().get(0).quantity()).isEqualTo(1);
    }

    @Test
    @Description("GET /wallets/{walletId}/stocks/{stock} should return 0 for stock not owned by wallet")
    void shouldReturnZeroForStockNotOwnedByWallet() throws Exception {
        //given
        bankSteps.seedBank(STOCK_NAME, INITIAL_BANK_QUANTITY);
        walletSteps.buyStock(WALLET_ID, STOCK_NAME);
        //when
        int quantity = walletSteps.getStockQuantity(WALLET_ID, ANOTHER_STOCK);
        //then
        assertThat(quantity).isEqualTo(ZERO);
    }

    @Test
    @Description("GET /wallets/{walletId}/stocks/{stock} should return correct quantity after multiple buys")
    void shouldReturnCorrectQuantityAfterMultipleBuys() throws Exception {
        //given
        bankSteps.seedBank(STOCK_NAME, INITIAL_BANK_QUANTITY);
        walletSteps.buyStock(WALLET_ID, STOCK_NAME);
        walletSteps.buyStock(WALLET_ID, STOCK_NAME);
        //when
        int quantity = walletSteps.getStockQuantity(WALLET_ID, STOCK_NAME);
        //then
        assertThat(quantity).isEqualTo(2);
    }

    @Test
    @Description("GET /wallets/{walletId} should return multiple stocks when wallet owns several")
    void shouldReturnMultipleStocksWhenWalletOwnsMultiple() throws Exception {
        //given
        bankSteps.seedBank(
                new StockDto(STOCK_NAME, INITIAL_BANK_QUANTITY),
                new StockDto(ANOTHER_STOCK, INITIAL_BANK_QUANTITY)
        );
        walletSteps.buyStock(WALLET_ID, STOCK_NAME);
        walletSteps.buyStock(WALLET_ID, ANOTHER_STOCK);
        //when
        WalletResponse response = walletSteps.getWallet(WALLET_ID);
        //then
        assertThat(response.stocks()).hasSize(2);
    }
}