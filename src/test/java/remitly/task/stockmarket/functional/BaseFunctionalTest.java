package remitly.task.stockmarket.functional;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import remitly.task.stockmarket.config.TestConfig;
import remitly.task.stockmarket.repository.AuditRepository;
import remitly.task.stockmarket.repository.BankStockRepository;
import remitly.task.stockmarket.repository.WalletRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("int")
@Import(TestConfig.class)
public abstract class BaseFunctionalTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private BankStockRepository bankStockRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private AuditRepository auditRepository;

    @BeforeEach
    void cleanDatabase() {
        auditRepository.deleteAll();
        walletRepository.deleteAll();
        bankStockRepository.deleteAll();
    }
}