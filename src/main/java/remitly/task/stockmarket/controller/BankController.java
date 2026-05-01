package remitly.task.stockmarket.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import remitly.task.stockmarket.dto.BankResponse;
import remitly.task.stockmarket.dto.StockDto;
import remitly.task.stockmarket.model.BankStock;
import remitly.task.stockmarket.service.BankService;

@RestController
@RequestMapping("/stocks")
public class BankController {

    private final BankService bankService;

    public BankController(BankService bankService) {
        this.bankService = bankService;
    }

    @GetMapping
    public BankResponse get() {
        return new BankResponse(
                bankService.getAll().stream()
                        .map(s -> new StockDto(s.getStockName(), s.getQuantity()))
                        .toList()
        );
    }

    @PostMapping
    public ResponseEntity<Void> set(@RequestBody BankResponse req) {
        bankService.setState(
                req.stocks().stream()
                        .map(s -> {
                            BankStock bs = new BankStock();
                            bs.setStockName(s.name());
                            bs.setQuantity(s.quantity());
                            return bs;
                        }).toList()
        );
        return ResponseEntity.ok().build();
    }
}
