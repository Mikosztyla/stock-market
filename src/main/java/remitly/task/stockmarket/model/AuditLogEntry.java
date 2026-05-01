package remitly.task.stockmarket.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AuditLogEntry {

    @Id
    @GeneratedValue
    private Long id;

    private String type;
    private String walletId;
    private String stockName;

    private LocalDateTime timestamp;
}
