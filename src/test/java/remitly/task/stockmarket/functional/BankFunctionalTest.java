package remitly.task.stockmarket.functional;

import io.qameta.allure.Description;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import remitly.task.stockmarket.dto.BankResponse;
import remitly.task.stockmarket.steps.BankSteps;

import static org.assertj.core.api.Assertions.assertThat;

class BankFunctionalTest extends BaseFunctionalTest {

    private static final String STOCK_NAME = "AAPL";
    private static final String ANOTHER_STOCK_NAME = "GOOG";
    private static final int INITIAL_QUANTITY = 10;
    private static final int ANOTHER_QUANTITY = 5;
    private static final int ZERO_QUANTITY = 0;

    @Autowired
    private BankSteps bankSteps;

    @Test
    @Description("GET /stocks on empty bank should return empty list")
    void shouldReturnEmptyListWhenBankHasNoStocks() throws Exception {
        //when
        BankResponse response = bankSteps.getBankState();
        //then
        assertThat(response.stocks()).isEmpty();
    }

    @Test
    @Description("POST /stocks should set the bank state with provided stocks")
    void shouldSetBankStateWithProvidedStocks() throws Exception {
        //given //when
        bankSteps.seedBank(STOCK_NAME, INITIAL_QUANTITY);
        //then
        assertThat(bankSteps.getStockQuantity(STOCK_NAME)).isEqualTo(INITIAL_QUANTITY);
    }

    @Test
    @Description("POST /stocks should replace existing bank state entirely")
    void shouldReplaceExistingBankStateWhenSettingNewState() throws Exception {
        //given
        bankSteps.seedBank(STOCK_NAME, INITIAL_QUANTITY);
        //when
        bankSteps.seedBank(ANOTHER_STOCK_NAME, ANOTHER_QUANTITY);
        //then
        BankResponse response = bankSteps.getBankState();
        assertThat(response.stocks()).hasSize(1);
        assertThat(response.stocks().get(0).name()).isEqualTo(ANOTHER_STOCK_NAME);
        assertThat(response.stocks().get(0).quantity()).isEqualTo(ANOTHER_QUANTITY);
    }

    @Test
    @Description("GET /stocks should return zero quantity for unknown stock")
    void shouldReturnZeroQuantityForUnknownStock() throws Exception {
        //given
        bankSteps.seedBank(STOCK_NAME, INITIAL_QUANTITY);
        //when
        int quantity = bankSteps.getStockQuantity(ANOTHER_STOCK_NAME);
        //then
        assertThat(quantity).isEqualTo(ZERO_QUANTITY);
    }
}