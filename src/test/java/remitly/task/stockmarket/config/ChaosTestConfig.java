package remitly.task.stockmarket.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import remitly.task.stockmarket.chaos.ChaosShutdownListener;
import remitly.task.stockmarket.chaos.SystemExiter;

@TestConfiguration
public class ChaosTestConfig {

    @Bean
    public SystemExiter systemExiter() {
        return Mockito.mock(SystemExiter.class);
    }

    @Bean
    public ChaosShutdownListener chaosShutdownListener(SystemExiter systemExiter) {
        return new ChaosShutdownListener(systemExiter);
    }
}