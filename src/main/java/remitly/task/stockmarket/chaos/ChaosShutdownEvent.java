package remitly.task.stockmarket.chaos;

import org.springframework.context.ApplicationEvent;

public class ChaosShutdownEvent extends ApplicationEvent {
    public ChaosShutdownEvent(Object source) {
        super(source);
    }
}