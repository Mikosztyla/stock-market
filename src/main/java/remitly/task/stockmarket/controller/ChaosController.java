package remitly.task.stockmarket.controller;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import remitly.task.stockmarket.chaos.ChaosShutdownEvent;

@RestController
public class ChaosController {

    private final ApplicationEventPublisher applicationEventPublisher;

    public ChaosController(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @PostMapping("/chaos")
    public ResponseEntity<Void> kill() {
        applicationEventPublisher.publishEvent(new ChaosShutdownEvent(this));
        return ResponseEntity.ok().build();
    }
}