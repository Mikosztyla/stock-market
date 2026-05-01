package remitly.task.stockmarket.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import remitly.task.stockmarket.exceptions.InsufficientStockException;
import remitly.task.stockmarket.exceptions.StockNotFoundException;
import remitly.task.stockmarket.model.BankStock;
import remitly.task.stockmarket.repository.BankStockRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BankServiceTest {

    @Mock
    private BankStockRepository bankRepo;

    @InjectMocks
    private BankService bankService;

    @Test
    void decrease_shouldThrow_whenStockNotFound() {
        when(bankRepo.findById("AAPL")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bankService.decrease("AAPL", 1))
                .isInstanceOf(StockNotFoundException.class);
    }

    @Test
    void decrease_shouldThrow_whenInsufficientQuantity() {
        BankStock stock = new BankStock();
        stock.setStockName("AAPL");
        stock.setQuantity(0);
        when(bankRepo.findById("AAPL")).thenReturn(Optional.of(stock));

        assertThatThrownBy(() -> bankService.decrease("AAPL", 1))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void decrease_shouldReduceQuantity_whenSufficientStock() {
        BankStock stock = new BankStock();
        stock.setStockName("AAPL");
        stock.setQuantity(10);
        when(bankRepo.findById("AAPL")).thenReturn(Optional.of(stock));

        bankService.decrease("AAPL", 1);

        assertThat(stock.getQuantity()).isEqualTo(9);
    }
}