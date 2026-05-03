package remitly.task.stockmarket.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import remitly.task.stockmarket.config.E2ETestConfig;
import remitly.task.stockmarket.repository.AuditRepository;
import remitly.task.stockmarket.repository.BankStockRepository;
import remitly.task.stockmarket.repository.WalletRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("int")
@Import(E2ETestConfig.class)
public abstract class BaseE2ETest {

    @LocalServerPort
    protected int port;

    protected WebTestClient client;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private BankStockRepository bankStockRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private AuditRepository auditRepository;

    @BeforeEach
    void setUp() {
        client = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
        auditRepository.deleteAll();
        walletRepository.deleteAll();
        bankStockRepository.deleteAll();
    }
}