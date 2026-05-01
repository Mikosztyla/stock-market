package remitly.task.stockmarket.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class WalletStock {

    @Id
    @GeneratedValue
    private Long id;

    private String stockName;
    private int quantity;

    @ManyToOne
    private Wallet wallet;
}
