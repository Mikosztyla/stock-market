package remitly.task.stockmarket.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import remitly.task.stockmarket.dto.StockDto;
import remitly.task.stockmarket.dto.StockOperationRequest;
import remitly.task.stockmarket.dto.WalletResponse;
import remitly.task.stockmarket.model.Wallet;
import remitly.task.stockmarket.model.WalletStock;
import remitly.task.stockmarket.service.WalletService;

import java.util.List;

@RestController
@RequestMapping("/wallets")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping("/{walletId}/stocks/{stock}")
    public ResponseEntity<Void> op(
            @PathVariable String walletId,
            @PathVariable String stock,
            @RequestBody StockOperationRequest req) {
        walletService.execute(walletId, stock, req.type());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{walletId}")
    public WalletResponse getWallet(@PathVariable String walletId) {
        Wallet w = walletService.get(walletId);

        List<StockDto> stocks = w.getStocks().stream()
                .map(s -> new StockDto(s.getStockName(), s.getQuantity()))
                .toList();

        return new WalletResponse(w.getId(), stocks);
    }

    @GetMapping("/{walletId}/stocks/{stock}")
    public int getStock(@PathVariable String walletId,
                        @PathVariable String stock) {

        return walletService.get(walletId).getStocks().stream()
                .filter(s -> s.getStockName().equals(stock))
                .map(WalletStock::getQuantity)
                .findFirst()
                .orElse(0);
    }
}
