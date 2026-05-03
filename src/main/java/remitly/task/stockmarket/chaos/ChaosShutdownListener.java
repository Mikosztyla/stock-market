package remitly.task.stockmarket.chaos;

import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class ChaosShutdownListener implements ApplicationListener<ChaosShutdownEvent> {

    @Async
    @Override
    public void onApplicationEvent(ChaosShutdownEvent event) {
        System.exit(0);
    }
}