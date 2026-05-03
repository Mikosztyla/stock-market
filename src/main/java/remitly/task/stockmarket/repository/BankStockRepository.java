package remitly.task.stockmarket.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import remitly.task.stockmarket.model.BankStock;

import java.util.Optional;

public interface BankStockRepository extends JpaRepository<BankStock, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM BankStock b WHERE b.stockName = :name")
    Optional<BankStock> findByIdWithLock(@Param("name") String name);
}