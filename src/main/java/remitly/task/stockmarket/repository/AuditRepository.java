package remitly.task.stockmarket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import remitly.task.stockmarket.model.AuditLogEntry;

public interface AuditRepository extends JpaRepository<AuditLogEntry, Long> {}
