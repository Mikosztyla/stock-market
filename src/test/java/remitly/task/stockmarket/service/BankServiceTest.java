package remitly.task.stockmarket.service;

import io.qameta.allure.Description;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import remitly.task.stockmarket.exceptions.InsufficientStockException;
import remitly.task.stockmarket.exceptions.StockNotFoundException;
import remitly.task.stockmarket.model.BankStock;
import remitly.task.stockmarket.repository.BankStockRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BankServiceTest {

    private static final String STOCK_NAME = "AAPL";
    private static final String BLANK = "   ";
    private static final int INITIAL_QUANTITY = 10;
    private static final int DECREASE_QUANTITY = 1;
    private static final int ZERO_QUANTITY = 0;
    private static final int NEGATIVE_QUANTITY = -1;

    @Mock
    private BankStockRepository bankRepo;

    @InjectMocks
    private BankService bankService;

    @Test
    @Description("Requesting a stock with null name should throw IllegalArgumentException")
    void shouldThrowWhenGetOrThrowCalledWithNullName() {
        //when //then
        assertThatThrownBy(() -> bankService.getOrThrow(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @Description("Requesting a stock with blank name should throw IllegalArgumentException")
    void shouldThrowWhenGetOrThrowCalledWithBlankName() {
        //when //then
        assertThatThrownBy(() -> bankService.getOrThrow(BLANK))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @Description("Requesting a stock that has never been seeded should throw StockNotFoundException")
    void shouldThrowWhenStockNotFoundInBank() {
        //given
        when(bankRepo.findById(STOCK_NAME)).thenReturn(Optional.empty());
        //when //then
        assertThatThrownBy(() -> bankService.getOrThrow(STOCK_NAME))
                .isInstanceOf(StockNotFoundException.class);
    }

    @Test
    @Description("Requesting a stock that exists in the bank should return it")
    void shouldReturnStockWhenItExistsInBank() {
        //given
        BankStock stock = bankStockOf(STOCK_NAME, INITIAL_QUANTITY);
        when(bankRepo.findById(STOCK_NAME)).thenReturn(Optional.of(stock));
        //when
        BankStock result = bankService.getOrThrow(STOCK_NAME);
        //then
        assertThat(result.getStockName()).isEqualTo(STOCK_NAME);
        assertThat(result.getQuantity()).isEqualTo(INITIAL_QUANTITY);
    }

    @Test
    @Description("Decreasing with null stock name should throw IllegalArgumentException")
    void shouldThrowWhenDecreaseCalledWithNullStockName() {
        //when //then
        assertThatThrownBy(() -> bankService.decrease(null, DECREASE_QUANTITY))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @Description("Decreasing with blank stock name should throw IllegalArgumentException")
    void shouldThrowWhenDecreaseCalledWithBlankStockName() {
        //when //then
        assertThatThrownBy(() -> bankService.decrease(BLANK, DECREASE_QUANTITY))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @Description("Decreasing with zero quantity should throw IllegalArgumentException")
    void shouldThrowWhenDecreaseCalledWithZeroQuantity() {
        //when //then
        assertThatThrownBy(() -> bankService.decrease(STOCK_NAME, ZERO_QUANTITY))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @Description("Decreasing with negative quantity should throw IllegalArgumentException")
    void shouldThrowWhenDecreaseCalledWithNegativeQuantity() {
        //when //then
        assertThatThrownBy(() -> bankService.decrease(STOCK_NAME, NEGATIVE_QUANTITY))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @Description("Decreasing a stock that has never been seeded should throw StockNotFoundException")
    void shouldThrowWhenDecreasingNonExistentStock() {
        //given
        when(bankRepo.findByIdWithLock(STOCK_NAME)).thenReturn(Optional.empty());
        //when //then
        assertThatThrownBy(() -> bankService.decrease(STOCK_NAME, DECREASE_QUANTITY))
                .isInstanceOf(StockNotFoundException.class);
    }

    @Test
    @Description("Decreasing a stock when bank quantity is 0 should throw InsufficientStockException")
    void shouldThrowWhenBankHasInsufficientQuantity() {
        //given
        BankStock stock = bankStockOf(STOCK_NAME, ZERO_QUANTITY);
        when(bankRepo.findByIdWithLock(STOCK_NAME)).thenReturn(Optional.of(stock));
        //when //then
        assertThatThrownBy(() -> bankService.decrease(STOCK_NAME, DECREASE_QUANTITY))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    @Description("Successfully decreasing a stock should reduce its quantity by exactly the requested amount")
    void shouldReduceQuantityWhenDecreasingStock() {
        //given
        BankStock stock = bankStockOf(STOCK_NAME, INITIAL_QUANTITY);
        when(bankRepo.findByIdWithLock(STOCK_NAME)).thenReturn(Optional.of(stock));
        //when
        bankService.decrease(STOCK_NAME, DECREASE_QUANTITY);
        //then
        ArgumentCaptor<BankStock> captor = ArgumentCaptor.forClass(BankStock.class);
        verify(bankRepo).save(captor.capture());
        assertThat(captor.getValue().getQuantity()).isEqualTo(INITIAL_QUANTITY - DECREASE_QUANTITY);
    }

    @Test
    @Description("Increasing with null stock name should throw IllegalArgumentException")
    void shouldThrowWhenIncreaseCalledWithNullStockName() {
        //when //then
        assertThatThrownBy(() -> bankService.increase(null, DECREASE_QUANTITY))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @Description("Increasing with blank stock name should throw IllegalArgumentException")
    void shouldThrowWhenIncreaseCalledWithBlankStockName() {
        //when //then
        assertThatThrownBy(() -> bankService.increase(BLANK, DECREASE_QUANTITY))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @Description("Increasing with zero quantity should throw IllegalArgumentException")
    void shouldThrowWhenIncreaseCalledWithZeroQuantity() {
        //when //then
        assertThatThrownBy(() -> bankService.increase(STOCK_NAME, ZERO_QUANTITY))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @Description("Increasing with negative quantity should throw IllegalArgumentException")
    void shouldThrowWhenIncreaseCalledWithNegativeQuantity() {
        //when //then
        assertThatThrownBy(() -> bankService.increase(STOCK_NAME, NEGATIVE_QUANTITY))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @Description("Increasing a stock that does not exist yet should create it with the given quantity")
    void shouldCreateStockWhenIncreasingNonExistentStock() {
        //given
        when(bankRepo.findByIdWithLock(STOCK_NAME)).thenReturn(Optional.empty());
        //when
        bankService.increase(STOCK_NAME, DECREASE_QUANTITY);
        //then
        ArgumentCaptor<BankStock> captor = ArgumentCaptor.forClass(BankStock.class);
        verify(bankRepo).save(captor.capture());
        assertThat(captor.getValue().getStockName()).isEqualTo(STOCK_NAME);
        assertThat(captor.getValue().getQuantity()).isEqualTo(DECREASE_QUANTITY);
    }

    @Test
    @Description("Increasing a stock that already exists should add the given quantity to its current amount")
    void shouldAddQuantityWhenIncreasingExistingStock() {
        //given
        BankStock stock = bankStockOf(STOCK_NAME, INITIAL_QUANTITY);
        when(bankRepo.findByIdWithLock(STOCK_NAME)).thenReturn(Optional.of(stock));
        //when
        bankService.increase(STOCK_NAME, DECREASE_QUANTITY);
        //then
        ArgumentCaptor<BankStock> captor = ArgumentCaptor.forClass(BankStock.class);
        verify(bankRepo).save(captor.capture());
        assertThat(captor.getValue().getQuantity()).isEqualTo(INITIAL_QUANTITY + DECREASE_QUANTITY);
    }

    @Test
    @Description("Setting bank state with null list should throw IllegalArgumentException")
    void shouldThrowWhenSetStateCalledWithNullList() {
        //when //then
        assertThatThrownBy(() -> bankService.setState(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @Description("Setting bank state should delete all existing stocks and save the new ones")
    void shouldReplaceAllStocksWhenSettingState() {
        //given
        List<BankStock> newStocks = List.of(bankStockOf(STOCK_NAME, INITIAL_QUANTITY));
        //when
        bankService.setState(newStocks);
        //then
        verify(bankRepo).deleteAll();
        verify(bankRepo).saveAll(newStocks);
    }

    private BankStock bankStockOf(String name, int quantity) {
        BankStock stock = new BankStock();
        stock.setStockName(name);
        stock.setQuantity(quantity);
        return stock;
    }
}