package remitly.task.stockmarket.chaos;

import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class ChaosShutdownListener implements ApplicationListener<ChaosShutdownEvent> {

    private final SystemExiter systemExiter;

    public ChaosShutdownListener(SystemExiter systemExiter) {
        this.systemExiter = systemExiter;
    }

    @Async
    @Override
    public void onApplicationEvent(ChaosShutdownEvent event) {
        systemExiter.exit(0);
    }
}