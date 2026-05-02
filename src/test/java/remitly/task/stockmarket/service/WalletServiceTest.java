package remitly.task.stockmarket.service;

import io.qameta.allure.Description;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import remitly.task.stockmarket.exceptions.InsufficientStockException;
import remitly.task.stockmarket.model.Wallet;
import remitly.task.stockmarket.model.WalletStock;
import remitly.task.stockmarket.repository.WalletRepository;

import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    private static final String WALLET_ID = "alice";
    private static final String STOCK_NAME = "AAPL";
    private static final String BUY = "buy";
    private static final String SELL = "sell";
    private static final String UNKNOWN_TYPE = "transfer";
    private static final String BLANK = "   ";
    private static final int INITIAL_QUANTITY = 5;
    private static final int ZERO_QUANTITY = 0;
    private static final int ONE = 1;

    @Mock
    private WalletRepository walletRepo;

    @Mock
    private BankService bankService;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private WalletService walletService;

    @Test
    @Description("Executing an operation with null wallet id should throw IllegalArgumentException")
    void shouldThrowWhenWalletIdIsNull() {
        //when //then
        assertThatThrownBy(() -> walletService.execute(null, STOCK_NAME, BUY))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @Description("Executing an operation with blank wallet id should throw IllegalArgumentException")
    void shouldThrowWhenWalletIdIsBlank() {
        //when //then
        assertThatThrownBy(() -> walletService.execute(BLANK, STOCK_NAME, BUY))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @Description("Executing an operation with null stock name should throw IllegalArgumentException")
    void shouldThrowWhenStockNameIsNull() {
        //when //then
        assertThatThrownBy(() -> walletService.execute(WALLET_ID, null, BUY))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @Description("Executing an operation with blank stock name should throw IllegalArgumentException")
    void shouldThrowWhenStockNameIsBlank() {
        //when //then
        assertThatThrownBy(() -> walletService.execute(WALLET_ID, BLANK, BUY))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @Description("Executing an operation with an unknown type should throw IllegalArgumentException")
    void shouldThrowWhenOperationTypeIsUnknown() {
        //when //then
        assertThatThrownBy(() -> walletService.execute(WALLET_ID, STOCK_NAME, UNKNOWN_TYPE))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @Description("Executing an operation with null type should throw IllegalArgumentException")
    void shouldThrowWhenOperationTypeIsNull() {
        //when //then
        assertThatThrownBy(() -> walletService.execute(WALLET_ID, STOCK_NAME, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @Description("Buying a stock for a wallet that does not exist yet should create the wallet automatically")
    void shouldCreateWalletWhenBuyingStockForNonExistentWallet() {
        //given
        when(walletRepo.findById(WALLET_ID)).thenReturn(Optional.empty());
        //when
        walletService.execute(WALLET_ID, STOCK_NAME, BUY);
        //then
        ArgumentCaptor<Wallet> captor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepo).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(WALLET_ID);
    }

    @Test
    @Description("Buying a stock that already exists in the wallet should increment its quantity by exactly 1")
    void shouldIncreaseWalletStockQuantityByOneWhenBuying() {
        //given
        Wallet wallet = walletWithStock(STOCK_NAME, INITIAL_QUANTITY);
        when(walletRepo.findById(WALLET_ID)).thenReturn(Optional.of(wallet));
        //when
        walletService.execute(WALLET_ID, STOCK_NAME, BUY);
        //then
        ArgumentCaptor<Wallet> captor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepo).save(captor.capture());
        assertThat(captor.getValue().getStocks().get(0).getQuantity()).isEqualTo(INITIAL_QUANTITY + ONE);
    }

    @Test
    @Description("Buying a stock should call bankService.decrease with quantity 1")
    void shouldDecreaseBankStockByOneWhenBuying() {
        //given
        when(walletRepo.findById(WALLET_ID)).thenReturn(Optional.empty());
        //when
        walletService.execute(WALLET_ID, STOCK_NAME, BUY);
        //then
        verify(bankService).decrease(STOCK_NAME, ONE);
    }

    @Test
    @Description("Buying a stock that the wallet does not own yet should add a new entry with quantity 1")
    void shouldAddNewStockEntryToWalletWhenStockNotYetOwned() {
        //given
        Wallet wallet = emptyWallet();
        when(walletRepo.findById(WALLET_ID)).thenReturn(Optional.of(wallet));
        //when
        walletService.execute(WALLET_ID, STOCK_NAME, BUY);
        //then
        ArgumentCaptor<Wallet> captor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepo).save(captor.capture());
        assertThat(captor.getValue().getStocks()).hasSize(1);
        assertThat(captor.getValue().getStocks().get(0).getStockName()).isEqualTo(STOCK_NAME);
        assertThat(captor.getValue().getStocks().get(0).getQuantity()).isEqualTo(ONE);
    }

    @Test
    @Description("Selling a stock that the wallet has never owned should throw InsufficientStockException")
    void shouldThrowWhenSellingStockNotInWallet() {
        //given
        Wallet wallet = emptyWallet();
        when(walletRepo.findById(WALLET_ID)).thenReturn(Optional.of(wallet));
        //when //then
        assertThatThrownBy(() -> walletService.execute(WALLET_ID, STOCK_NAME, SELL))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    @Description("Selling a stock when the wallet holds zero units should throw InsufficientStockException")
    void shouldThrowWhenSellingStockWithZeroQuantityInWallet() {
        //given
        Wallet wallet = walletWithStock(STOCK_NAME, ZERO_QUANTITY);
        when(walletRepo.findById(WALLET_ID)).thenReturn(Optional.of(wallet));
        //when //then
        assertThatThrownBy(() -> walletService.execute(WALLET_ID, STOCK_NAME, SELL))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    @Description("Successfully selling a stock should reduce the wallet stock quantity by exactly 1")
    void shouldDecreaseWalletStockQuantityByOneWhenSelling() {
        //given
        Wallet wallet = walletWithStock(STOCK_NAME, INITIAL_QUANTITY);
        when(walletRepo.findById(WALLET_ID)).thenReturn(Optional.of(wallet));
        //when
        walletService.execute(WALLET_ID, STOCK_NAME, SELL);
        //then
        ArgumentCaptor<Wallet> captor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepo).save(captor.capture());
        assertThat(captor.getValue().getStocks().get(0).getQuantity()).isEqualTo(INITIAL_QUANTITY - ONE);
    }

    @Test
    @Description("Successfully selling a stock should call bankService.increase with quantity 1")
    void shouldIncreaseBankStockByOneWhenSelling() {
        //given
        Wallet wallet = walletWithStock(STOCK_NAME, INITIAL_QUANTITY);
        when(walletRepo.findById(WALLET_ID)).thenReturn(Optional.of(wallet));
        //when
        walletService.execute(WALLET_ID, STOCK_NAME, SELL);
        //then
        verify(bankService).increase(STOCK_NAME, ONE);
    }

    @Test
    @Description("A successful buy should produce an audit log entry with the correct type, wallet, and stock")
    void shouldLogBuyOperationToAudit() {
        //given
        when(walletRepo.findById(WALLET_ID)).thenReturn(Optional.empty());
        //when
        walletService.execute(WALLET_ID, STOCK_NAME, BUY);
        //then
        verify(auditService).log(BUY, WALLET_ID, STOCK_NAME);
    }

    @Test
    @Description("A successful sell should produce an audit log entry with the correct type, wallet, and stock")
    void shouldLogSellOperationToAudit() {
        //given
        Wallet wallet = walletWithStock(STOCK_NAME, INITIAL_QUANTITY);
        when(walletRepo.findById(WALLET_ID)).thenReturn(Optional.of(wallet));
        //when
        walletService.execute(WALLET_ID, STOCK_NAME, SELL);
        //then
        verify(auditService).log(SELL, WALLET_ID, STOCK_NAME);
    }

    @Test
    @Description("A failed sell due to insufficient stock should not produce any audit log entry")
    void shouldNotLogOperationWhenSellFails() {
        //given
        Wallet wallet = emptyWallet();
        when(walletRepo.findById(WALLET_ID)).thenReturn(Optional.of(wallet));
        //when //then
        assertThatThrownBy(() -> walletService.execute(WALLET_ID, STOCK_NAME, SELL))
                .isInstanceOf(InsufficientStockException.class);
        verify(auditService, never()).log(any(), any(), any());
    }

    @Test
    @Description("Getting a wallet with null id should throw IllegalArgumentException")
    void shouldThrowWhenGetCalledWithNullId() {
        //when //then
        assertThatThrownBy(() -> walletService.get(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @Description("Getting a wallet with blank id should throw IllegalArgumentException")
    void shouldThrowWhenGetCalledWithBlankId() {
        //when //then
        assertThatThrownBy(() -> walletService.get(BLANK))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @Description("Getting a wallet that does not exist should return an empty wallet rather than throwing")
    void shouldReturnEmptyWalletWhenWalletDoesNotExist() {
        //given
        when(walletRepo.findById(WALLET_ID)).thenReturn(Optional.empty());
        //when
        Wallet result = walletService.get(WALLET_ID);
        //then
        assertThat(result.getStocks()).isEmpty();
    }

    @Test
    @Description("Getting a wallet that exists should return it with all its stocks")
    void shouldReturnExistingWallet() {
        //given
        Wallet wallet = walletWithStock(STOCK_NAME, INITIAL_QUANTITY);
        when(walletRepo.findById(WALLET_ID)).thenReturn(Optional.of(wallet));
        //when
        Wallet result = walletService.get(WALLET_ID);
        //then
        assertThat(result.getStocks()).hasSize(1);
        assertThat(result.getStocks().get(0).getStockName()).isEqualTo(STOCK_NAME);
    }

    private Wallet emptyWallet() {
        Wallet wallet = new Wallet();
        wallet.setId(WALLET_ID);
        wallet.setStocks(new ArrayList<>());
        return wallet;
    }

    private Wallet walletWithStock(String stockName, int quantity) {
        WalletStock walletStock = new WalletStock();
        walletStock.setStockName(stockName);
        walletStock.setQuantity(quantity);
        Wallet wallet = new Wallet();
        wallet.setId(WALLET_ID);
        wallet.setStocks(new ArrayList<>());
        wallet.getStocks().add(walletStock);
        return wallet;
    }
}