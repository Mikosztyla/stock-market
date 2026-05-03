package remitly.task.stockmarket.chaos;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEvent;
import static org.assertj.core.api.Assertions.assertThat;

class ChaosShutdownEventTest {

    @Test
    void shouldCarryCorrectSource() {
        //given
        Object source = new Object();
        //when
        ChaosShutdownEvent event = new ChaosShutdownEvent(source);
        //then
        assertThat(event.getSource()).isSameAs(source);
    }

    @Test
    void shouldExtendApplicationEvent() {
        //given //when
        ChaosShutdownEvent event = new ChaosShutdownEvent(this);
        //then
        assertThat(event).isInstanceOf(ApplicationEvent.class);
    }
}