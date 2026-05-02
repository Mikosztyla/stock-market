package remitly.task.stockmarket.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import remitly.task.stockmarket.exceptions.InsufficientStockException;
import remitly.task.stockmarket.model.Wallet;
import remitly.task.stockmarket.model.WalletStock;
import remitly.task.stockmarket.repository.WalletRepository;

@Slf4j
@Service
public class WalletService {

    private final WalletRepository walletRepo;
    private final BankService bankService;
    private final AuditService auditService;

    public WalletService(WalletRepository walletRepo, BankService bankService, AuditService auditService) {
        this.walletRepo = walletRepo;
        this.bankService = bankService;
        this.auditService = auditService;
    }

    @Transactional
    public void execute(String walletId, String stock, String type) {
        if (walletId == null || walletId.isBlank()) {
            throw new IllegalArgumentException("Wallet id must not be null or blank");
        }
        if (stock == null || stock.isBlank()) {
            throw new IllegalArgumentException("Stock name must not be null or blank");
        }
        if (!"buy".equals(type) && !"sell".equals(type)) {
            throw new IllegalArgumentException("Operation type must be buy or sell");
        }

        log.debug("Executing operation: type={} walletId={} stock={}", type, walletId, stock);

        Wallet wallet = walletRepo.findById(walletId)
                .orElseGet(() -> {
                    log.info("Wallet not found, creating new one: walletId={}", walletId);
                    Wallet w = new Wallet();
                    w.setId(walletId);
                    return w;
                });

        WalletStock ws = wallet.getStocks().stream()
                .filter(s -> s.getStockName().equals(stock))
                .findFirst()
                .orElse(null);

        if ("buy".equals(type)) {
            log.debug("Processing BUY: walletId={} stock={}", walletId, stock);
            bankService.decrease(stock, 1);

            if (ws == null) {
                log.debug("Stock not yet in wallet, initialising entry: walletId={} stock={}", walletId, stock);
                ws = new WalletStock();
                ws.setStockName(stock);
                ws.setQuantity(0);
                ws.setWallet(wallet);
                wallet.getStocks().add(ws);
            }

            ws.setQuantity(ws.getQuantity() + 1);
            log.info("BUY successful: walletId={} stock={} newWalletQuantity={}", walletId, stock, ws.getQuantity());

        } else {
            log.debug("Processing SELL: walletId={} stock={}", walletId, stock);

            if (ws == null || ws.getQuantity() <= 0) {
                log.warn("SELL failed — insufficient stock in wallet: walletId={} stock={}", walletId, stock);
                throw new InsufficientStockException("No stock in wallet");
            }

            ws.setQuantity(ws.getQuantity() - 1);
            bankService.increase(stock, 1);
            log.info("SELL successful: walletId={} stock={} newWalletQuantity={}", walletId, stock, ws.getQuantity());
        }

        walletRepo.save(wallet);
        log.debug("Wallet saved: walletId={}", walletId);

        auditService.log(type, walletId, stock);
    }

    public Wallet get(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Wallet id must not be null or blank");
        }
        log.debug("Fetching wallet: walletId={}", id);
        return walletRepo.findById(id)
                .orElse(new Wallet());
    }
}