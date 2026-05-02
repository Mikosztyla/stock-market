package remitly.task.stockmarket.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;
import remitly.task.stockmarket.steps.AuditSteps;
import remitly.task.stockmarket.steps.BankSteps;
import remitly.task.stockmarket.steps.WalletSteps;

@TestConfiguration
public class TestConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    @Bean
    public BankSteps bankSteps(MockMvc mockMvc, ObjectMapper objectMapper) {
        return new BankSteps(mockMvc, objectMapper);
    }

    @Bean
    public WalletSteps walletSteps(MockMvc mockMvc, ObjectMapper objectMapper) {
        return new WalletSteps(mockMvc, objectMapper);
    }

    @Bean
    public AuditSteps auditSteps(MockMvc mockMvc, ObjectMapper objectMapper) {
        return new AuditSteps(mockMvc, objectMapper);
    }
}