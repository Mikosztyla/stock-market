package remitly.task.stockmarket.chaos;

import io.qameta.allure.Description;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEvent;

import static org.assertj.core.api.Assertions.assertThat;

class ChaosShutdownEventTest {

    @Test
    @Description("ChaosShutdownEvent should carry the correct source")
    void shouldCarryCorrectSource() {
        // given
        Object source = new Object();
        // when
        ChaosShutdownEvent event = new ChaosShutdownEvent(source);
        // then
        assertThat(event.getSource()).isSameAs(source);
    }

    @Test
    @Description("ChaosShutdownEvent should extend ApplicationEvent")
    void shouldExtendApplicationEvent() {
        // when
        ChaosShutdownEvent event = new ChaosShutdownEvent(this);
        // then
        assertThat(event).isInstanceOf(ApplicationEvent.class);
    }
}