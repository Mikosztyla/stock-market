package remitly.task.stockmarket.chaos;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChaosConfig {

    @Bean
    public SystemExiter systemExiter() {
        return System::exit;
    }
}