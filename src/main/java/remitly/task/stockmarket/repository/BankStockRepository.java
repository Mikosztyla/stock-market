package remitly.task.stockmarket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import remitly.task.stockmarket.model.BankStock;

public interface BankStockRepository extends JpaRepository<BankStock, String> {}
