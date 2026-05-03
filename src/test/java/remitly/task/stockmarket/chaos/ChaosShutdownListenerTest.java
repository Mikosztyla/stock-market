package remitly.task.stockmarket.chaos;

import io.qameta.allure.Description;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ChaosShutdownListenerTest {

    @Mock
    private SystemExiter systemExiter;

    @InjectMocks
    private ChaosShutdownListener chaosShutdownListener;

    @Test
    @Description("onApplicationEvent should call SystemExiter.exit with status 0")
    void shouldCallExitWithZero() {
        // given
        ChaosShutdownEvent event = new ChaosShutdownEvent(this);
        // when
        chaosShutdownListener.onApplicationEvent(event);
        // then
        verify(systemExiter, times(1)).exit(0);
    }
}