package remitly.task.stockmarket.functional;

import io.qameta.allure.Description;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import remitly.task.stockmarket.dto.WalletResponse;
import remitly.task.stockmarket.steps.BankSteps;
import remitly.task.stockmarket.steps.WalletSteps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WalletBuyFunctionalTest extends BaseFunctionalTest {

    private static final String WALLET_ID = "alice";
    private static final String STOCK_NAME = "AAPL";
    private static final String UNKNOWN_STOCK = "UNKNOWN";
    private static final String UNKNOWN_TYPE = "transfer";
    private static final int INITIAL_BANK_QUANTITY = 10;
    private static final int ONE = 1;
    private static final int ZERO = 0;

    @Autowired
    private BankSteps bankSteps;

    @Autowired
    private WalletSteps walletSteps;

    @Test
    @Description("POST buy should decrease bank stock quantity by 1")
    void shouldDecreaseBankQuantityByOneWhenBuying() throws Exception {
        //given
        bankSteps.seedBank(STOCK_NAME, INITIAL_BANK_QUANTITY);
        //when
        walletSteps.buyStock(WALLET_ID, STOCK_NAME);
        //then
        assertThat(bankSteps.getStockQuantity(STOCK_NAME)).isEqualTo(INITIAL_BANK_QUANTITY - ONE);
    }

    @Test
    @Description("POST buy should increase wallet stock quantity by 1")
    void shouldIncreaseWalletQuantityByOneWhenBuying() throws Exception {
        //given
        bankSteps.seedBank(STOCK_NAME, INITIAL_BANK_QUANTITY);
        //when
        walletSteps.buyStock(WALLET_ID, STOCK_NAME);
        //then
        assertThat(walletSteps.getStockQuantity(WALLET_ID, STOCK_NAME)).isEqualTo(ONE);
    }

    @Test
    @Description("POST buy should create wallet automatically when it does not exist")
    void shouldCreateWalletAutomaticallyWhenItDoesNotExist() throws Exception {
        //given
        bankSteps.seedBank(STOCK_NAME, INITIAL_BANK_QUANTITY);
        //when
        walletSteps.buyStock(WALLET_ID, STOCK_NAME);
        //then
        WalletResponse wallet = walletSteps.getWallet(WALLET_ID);
        assertThat(wallet.id()).isEqualTo(WALLET_ID);
        assertThat(wallet.stocks()).hasSize(ONE);
    }

    @Test
    @Description("POST buy should return 404 when stock does not exist in bank")
    void shouldReturn404WhenBuyingNonExistentStock() throws Exception {
        //when //then
        walletSteps.buyStockExpectingError(WALLET_ID, UNKNOWN_STOCK)
                .andExpect(status().isNotFound());
    }

    @Test
    @Description("POST buy should return 400 when bank has no stock available")
    void shouldReturn400WhenBankHasNoStockAvailable() throws Exception {
        //given
        bankSteps.seedBank(STOCK_NAME, ZERO);
        //when //then
        walletSteps.buyStockExpectingError(WALLET_ID, STOCK_NAME)
                .andExpect(status().isBadRequest());
    }

    @Test
    @Description("POST buy should return 400 when operation type is unknown")
    void shouldReturn400WhenOperationTypeIsUnknown() throws Exception {
        //given
        bankSteps.seedBank(STOCK_NAME, INITIAL_BANK_QUANTITY);
        //when //then
        walletSteps.performOperation(WALLET_ID, STOCK_NAME, UNKNOWN_TYPE)
                .andExpect(status().isBadRequest());
    }

    @Test
    @Description("POST buy multiple times should accumulate wallet quantity correctly")
    void shouldAccumulateWalletQuantityAfterMultipleBuys() throws Exception {
        //given
        bankSteps.seedBank(STOCK_NAME, INITIAL_BANK_QUANTITY);
        //when
        walletSteps.buyStock(WALLET_ID, STOCK_NAME);
        walletSteps.buyStock(WALLET_ID, STOCK_NAME);
        walletSteps.buyStock(WALLET_ID, STOCK_NAME);
        //then
        assertThat(walletSteps.getStockQuantity(WALLET_ID, STOCK_NAME)).isEqualTo(3);
        assertThat(bankSteps.getStockQuantity(STOCK_NAME)).isEqualTo(INITIAL_BANK_QUANTITY - 3);
    }
}