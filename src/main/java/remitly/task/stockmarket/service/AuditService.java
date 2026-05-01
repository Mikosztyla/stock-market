package remitly.task.stockmarket.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import remitly.task.stockmarket.model.AuditLogEntry;
import remitly.task.stockmarket.repository.AuditRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class AuditService {

    private final AuditRepository repo;

    public AuditService(AuditRepository repo) {
        this.repo = repo;
    }

    public void log(String type, String wallet, String stock) {
        log.debug("Writing audit log entry: type={} walletId={} stock={}", type, wallet, stock);

        AuditLogEntry entry = new AuditLogEntry();
        entry.setType(type);
        entry.setWalletId(wallet);
        entry.setStockName(stock);
        entry.setTimestamp(LocalDateTime.now());
        repo.save(entry);

        log.debug("Audit log entry saved: type={} walletId={} stock={}", type, wallet, stock);
    }

    public List<AuditLogEntry> getAll() {
        log.debug("Fetching all audit log entries");
        List<AuditLogEntry> entries = repo.findAll(Sort.by("timestamp"));
        log.debug("Audit log fetched: totalEntries={}", entries.size());
        return entries;
    }
}