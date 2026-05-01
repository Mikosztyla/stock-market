package remitly.task.stockmarket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import remitly.task.stockmarket.model.Wallet;

public interface WalletRepository extends JpaRepository<Wallet, String> {}