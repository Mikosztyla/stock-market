package remitly.task.stockmarket.service;

import io.qameta.allure.Description;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import remitly.task.stockmarket.model.AuditLogEntry;
import remitly.task.stockmarket.repository.AuditRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import org.springframework.data.domain.Sort;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    private static final String OPERATION_BUY = "buy";
    private static final String OPERATION_SELL = "sell";
    private static final String WALLET_ID = "alice";
    private static final String STOCK_NAME = "AAPL";

    @Mock
    private AuditRepository repo;

    @InjectMocks
    private AuditService auditService;

    @Test
    @Description("Logging a buy operation should persist an entry with type set to buy")
    void shouldSaveAuditEntryWithCorrectType() {
        //when
        auditService.log(OPERATION_BUY, WALLET_ID, STOCK_NAME);
        //then
        ArgumentCaptor<AuditLogEntry> captor = ArgumentCaptor.forClass(AuditLogEntry.class);
        verify(repo).save(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(OPERATION_BUY);
    }

    @Test
    @Description("Logging an operation should persist an entry with the correct wallet id")
    void shouldSaveAuditEntryWithCorrectWalletId() {
        //when
        auditService.log(OPERATION_BUY, WALLET_ID, STOCK_NAME);
        //then
        ArgumentCaptor<AuditLogEntry> captor = ArgumentCaptor.forClass(AuditLogEntry.class);
        verify(repo).save(captor.capture());
        assertThat(captor.getValue().getWalletId()).isEqualTo(WALLET_ID);
    }

    @Test
    @Description("Logging an operation should persist an entry with the correct stock name")
    void shouldSaveAuditEntryWithCorrectStockName() {
        //when
        auditService.log(OPERATION_BUY, WALLET_ID, STOCK_NAME);
        //then
        ArgumentCaptor<AuditLogEntry> captor = ArgumentCaptor.forClass(AuditLogEntry.class);
        verify(repo).save(captor.capture());
        assertThat(captor.getValue().getStockName()).isEqualTo(STOCK_NAME);
    }

    @Test
    @Description("Logging an operation should persist an entry with a non-null timestamp")
    void shouldSaveAuditEntryWithTimestamp() {
        //when
        auditService.log(OPERATION_SELL, WALLET_ID, STOCK_NAME);
        //then
        ArgumentCaptor<AuditLogEntry> captor = ArgumentCaptor.forClass(AuditLogEntry.class);
        verify(repo).save(captor.capture());
        assertThat(captor.getValue().getTimestamp()).isNotNull();
    }

    @Test
    @Description("Getting all audit entries should return the full list from the repository in order")
    void shouldReturnAllAuditEntriesFromRepository() {
        //given
        AuditLogEntry first = auditEntryOf(OPERATION_BUY, WALLET_ID, STOCK_NAME);
        AuditLogEntry second = auditEntryOf(OPERATION_SELL, WALLET_ID, STOCK_NAME);
        when(repo.findAll(any(Sort.class))).thenReturn(List.of(first, second));
        //when
        List<AuditLogEntry> result = auditService.getAll();
        //then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getType()).isEqualTo(OPERATION_BUY);
        assertThat(result.get(1).getType()).isEqualTo(OPERATION_SELL);
    }

    private AuditLogEntry auditEntryOf(String type, String walletId, String stockName) {
        AuditLogEntry entry = new AuditLogEntry();
        entry.setType(type);
        entry.setWalletId(walletId);
        entry.setStockName(stockName);
        return entry;
    }
}