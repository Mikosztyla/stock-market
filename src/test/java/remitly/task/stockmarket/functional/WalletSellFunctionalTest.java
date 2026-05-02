package remitly.task.stockmarket.functional;

import io.qameta.allure.Description;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import remitly.task.stockmarket.steps.BankSteps;
import remitly.task.stockmarket.steps.WalletSteps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WalletSellFunctionalTest extends BaseFunctionalTest {

    private static final String WALLET_ID = "alice";
    private static final String STOCK_NAME = "AAPL";
    private static final int INITIAL_BANK_QUANTITY = 10;
    private static final int ONE = 1;

    @Autowired
    private BankSteps bankSteps;

    @Autowired
    private WalletSteps walletSteps;

    @Test
    @Description("POST sell should return 200 when wallet has sufficient stock")
    void shouldReturn200WhenSellingStockWithSufficientWalletQuantity() throws Exception {
        //given
        bankSteps.seedBank(STOCK_NAME, INITIAL_BANK_QUANTITY);
        walletSteps.buyStock(WALLET_ID, STOCK_NAME);
        //when //then
        walletSteps.sellStock(WALLET_ID, STOCK_NAME);
    }

    @Test
    @Description("POST sell should increase bank stock quantity by 1")
    void shouldIncreaseBankQuantityByOneWhenSelling() throws Exception {
        //given
        bankSteps.seedBank(STOCK_NAME, INITIAL_BANK_QUANTITY);
        walletSteps.buyStock(WALLET_ID, STOCK_NAME);
        //when
        walletSteps.sellStock(WALLET_ID, STOCK_NAME);
        //then
        assertThat(bankSteps.getStockQuantity(STOCK_NAME)).isEqualTo(INITIAL_BANK_QUANTITY);
    }

    @Test
    @Description("POST sell should decrease wallet stock quantity by 1")
    void shouldDecreaseWalletQuantityByOneWhenSelling() throws Exception {
        //given
        bankSteps.seedBank(STOCK_NAME, INITIAL_BANK_QUANTITY);
        walletSteps.buyStock(WALLET_ID, STOCK_NAME);
        walletSteps.buyStock(WALLET_ID, STOCK_NAME);
        //when
        walletSteps.sellStock(WALLET_ID, STOCK_NAME);
        //then
        assertThat(walletSteps.getStockQuantity(WALLET_ID, STOCK_NAME)).isEqualTo(ONE);
    }

    @Test
    @Description("POST sell should return 400 when wallet has no stock")
    void shouldReturn400WhenWalletHasNoStock() throws Exception {
        //given
        bankSteps.seedBank(STOCK_NAME, INITIAL_BANK_QUANTITY);
        //when //then
        walletSteps.sellStockExpectingError(WALLET_ID, STOCK_NAME)
                .andExpect(status().isBadRequest());
    }

    @Test
    @Description("POST sell should return 400 when wallet stock quantity is zero")
    void shouldReturn400WhenWalletStockQuantityIsZero() throws Exception {
        //given
        bankSteps.seedBank(STOCK_NAME, INITIAL_BANK_QUANTITY);
        walletSteps.buyStock(WALLET_ID, STOCK_NAME);
        walletSteps.sellStock(WALLET_ID, STOCK_NAME);
        //when //then
        walletSteps.sellStockExpectingError(WALLET_ID, STOCK_NAME)
                .andExpect(status().isBadRequest());
    }

    @Test
    @Description("POST sell should not affect bank when it fails")
    void shouldNotAffectBankQuantityWhenSellFails() throws Exception {
        //given
        bankSteps.seedBank(STOCK_NAME, INITIAL_BANK_QUANTITY);
        //when
        walletSteps.sellStockExpectingError(WALLET_ID, STOCK_NAME)
                .andExpect(status().isBadRequest());
        //then
        assertThat(bankSteps.getStockQuantity(STOCK_NAME)).isEqualTo(INITIAL_BANK_QUANTITY);
    }
}