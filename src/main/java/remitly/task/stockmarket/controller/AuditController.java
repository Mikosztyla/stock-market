package remitly.task.stockmarket.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import remitly.task.stockmarket.service.AuditService;

import java.util.Map;

@RestController
@RequestMapping("/log")
public class AuditController {

    @Autowired
    private AuditService auditService;

    @GetMapping
    public Map<String, Object> getLog() {
        return Map.of("log", auditService.getAll());
    }
}
