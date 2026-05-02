package remitly.task.stockmarket.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import remitly.task.stockmarket.exceptions.InsufficientStockException;
import remitly.task.stockmarket.exceptions.StockNotFoundException;
import remitly.task.stockmarket.model.BankStock;
import remitly.task.stockmarket.repository.BankStockRepository;

import java.util.List;

@Slf4j
@Service
public class BankService {

    private final BankStockRepository bankRepo;

    public BankService(BankStockRepository bankRepo) {
        this.bankRepo = bankRepo;
    }

    public BankStock getOrThrow(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Stock name must not be null or blank");
        }
        log.debug("Looking up bank stock: stock={}", name);
        return bankRepo.findById(name)
                .orElseThrow(() -> {
                    log.warn("Stock not found in bank: stock={}", name);
                    return new StockNotFoundException("Stock not found in bank");
                });
    }

    public void decrease(String stock, int qty) {
        if (stock == null || stock.isBlank()) {
            throw new IllegalArgumentException("Stock name must not be null or blank");
        }
        if (qty <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        log.debug("Decreasing bank stock: stock={} qty={}", stock, qty);
        BankStock bs = getOrThrow(stock);

        if (bs.getQuantity() < qty) {
            log.warn("Insufficient bank stock: stock={} requested={} available={}", stock, qty, bs.getQuantity());
            throw new InsufficientStockException("No stock in bank");
        }

        int before = bs.getQuantity();
        bs.setQuantity(bs.getQuantity() - qty);
        bankRepo.save(bs);
        log.info("Bank stock decreased: stock={} before={} after={}", stock, before, bs.getQuantity());

        if (bs.getQuantity() <= 3) {
            log.warn("Bank stock running low: stock={} remaining={}", stock, bs.getQuantity());
        }
    }

    public void increase(String stock, int qty) {
        if (stock == null || stock.isBlank()) {
            throw new IllegalArgumentException("Stock name must not be null or blank");
        }
        if (qty <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        log.debug("Increasing bank stock: stock={} qty={}", stock, qty);
        BankStock bs = bankRepo.findById(stock)
                .orElse(new BankStock());

        int before = bs.getQuantity();
        bs.setStockName(stock);
        bs.setQuantity(bs.getQuantity() + qty);
        bankRepo.save(bs);
        log.info("Bank stock increased: stock={} before={} after={}", stock, before, bs.getQuantity());
    }

    public List<BankStock> getAll() {
        log.debug("Fetching all bank stocks");
        return bankRepo.findAll();
    }

    public void setState(List<BankStock> stocks) {
        if (stocks == null) {
            throw new IllegalArgumentException("Stocks list must not be null");
        }
        log.info("Setting bank state: stocks={}", stocks.stream()
                .map(s -> s.getStockName() + "=" + s.getQuantity())
                .toList());
        bankRepo.deleteAll();
        bankRepo.saveAll(stocks);
        log.info("Bank state updated successfully: totalStockTypes={}", stocks.size());
    }
}