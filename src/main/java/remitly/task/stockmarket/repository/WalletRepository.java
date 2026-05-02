package remitly.task.stockmarket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import remitly.task.stockmarket.model.Wallet;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, String> {

    @Query("SELECT w FROM Wallet w LEFT JOIN FETCH w.stocks WHERE w.id = :id")
    Optional<Wallet> findByIdWithStocks(@Param("id") String id);
}