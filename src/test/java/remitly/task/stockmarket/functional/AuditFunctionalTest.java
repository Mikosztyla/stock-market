package remitly.task.stockmarket.functional;

import io.qameta.allure.Description;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import remitly.task.stockmarket.steps.AuditSteps;
import remitly.task.stockmarket.steps.BankSteps;
import remitly.task.stockmarket.steps.WalletSteps;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AuditFunctionalTest extends BaseFunctionalTest {

    private static final String WALLET_ID = "alice";
    private static final String STOCK_NAME = "AAPL";
    private static final String OPERATION_BUY = "buy";
    private static final String OPERATION_SELL = "sell";
    private static final int INITIAL_BANK_QUANTITY = 10;

    @Autowired
    private BankSteps bankSteps;

    @Autowired
    private WalletSteps walletSteps;

    @Autowired
    private AuditSteps auditSteps;

    @Test
    @Description("GET /log should return empty log when no operations have been performed")
    void shouldReturnEmptyLogWhenNoOperationsPerformed() throws Exception {
        //when
        List<AuditSteps.AuditEntry> log = auditSteps.getLog();
        //then
        assertThat(log).isEmpty();
    }

    @Test
    @Description("GET /log should contain buy entry after buying a stock")
    void shouldContainBuyEntryAfterBuyingStock() throws Exception {
        //given
        bankSteps.seedBank(STOCK_NAME, INITIAL_BANK_QUANTITY);
        walletSteps.buyStock(WALLET_ID, STOCK_NAME);
        //when
        List<AuditSteps.AuditEntry> log = auditSteps.getLog();
        //then
        assertThat(log).hasSize(1);
        assertThat(log.get(0).type()).isEqualTo(OPERATION_BUY);
        assertThat(log.get(0).walletId()).isEqualTo(WALLET_ID);
        assertThat(log.get(0).stockName()).isEqualTo(STOCK_NAME);
    }

    @Test
    @Description("GET /log should contain sell entry after selling a stock")
    void shouldContainSellEntryAfterSellingStock() throws Exception {
        //given
        bankSteps.seedBank(STOCK_NAME, INITIAL_BANK_QUANTITY);
        walletSteps.buyStock(WALLET_ID, STOCK_NAME);
        walletSteps.sellStock(WALLET_ID, STOCK_NAME);
        //when
        List<AuditSteps.AuditEntry> log = auditSteps.getLog();
        //then
        assertThat(log).hasSize(2);
        assertThat(log.get(1).type()).isEqualTo(OPERATION_SELL);
    }

    @Test
    @Description("GET /log should return entries in chronological order")
    void shouldReturnEntriesInChronologicalOrder() throws Exception {
        //given
        bankSteps.seedBank(STOCK_NAME, INITIAL_BANK_QUANTITY);
        walletSteps.buyStock(WALLET_ID, STOCK_NAME);
        walletSteps.buyStock(WALLET_ID, STOCK_NAME);
        walletSteps.sellStock(WALLET_ID, STOCK_NAME);
        //when
        List<AuditSteps.AuditEntry> log = auditSteps.getLog();
        //then
        assertThat(log).hasSize(3);
        assertThat(log.get(0).type()).isEqualTo(OPERATION_BUY);
        assertThat(log.get(1).type()).isEqualTo(OPERATION_BUY);
        assertThat(log.get(2).type()).isEqualTo(OPERATION_SELL);
    }

    @Test
    @Description("GET /log should not contain entry when buy fails due to insufficient bank stock")
    void shouldNotLogEntryWhenBuyFailsDueToInsufficientBankStock() throws Exception {
        //given
        bankSteps.seedBank(STOCK_NAME, 0);
        walletSteps.buyStockExpectingError(WALLET_ID, STOCK_NAME);
        //when
        List<AuditSteps.AuditEntry> log = auditSteps.getLog();
        //then
        assertThat(log).isEmpty();
    }

    @Test
    @Description("GET /log should not contain entry when sell fails due to insufficient wallet stock")
    void shouldNotLogEntryWhenSellFailsDueToInsufficientWalletStock() throws Exception {
        //given
        bankSteps.seedBank(STOCK_NAME, INITIAL_BANK_QUANTITY);
        walletSteps.sellStockExpectingError(WALLET_ID, STOCK_NAME);
        //when
        List<AuditSteps.AuditEntry> log = auditSteps.getLog();
        //then
        assertThat(log).isEmpty();
    }
}