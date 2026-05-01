package remitly.task.stockmarket.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChaosController {

    @PostMapping("/chaos")
    public void kill() {
        System.exit(0);
    }
}
