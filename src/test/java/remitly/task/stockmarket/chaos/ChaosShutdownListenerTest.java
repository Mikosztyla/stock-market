package remitly.task.stockmarket.chaos;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ChaosShutdownListenerTest {

    private static final int EXIT_CODE_SUCCESS = 0;
    private static final int ONCE = 1;

    @Mock
    private SystemExiter systemExiter;

    @InjectMocks
    private ChaosShutdownListener chaosShutdownListener;

    @Test
    void shouldCallExitWithCodeZero() {
        //given
        ChaosShutdownEvent event = new ChaosShutdownEvent(this);
        //when
        chaosShutdownListener.onApplicationEvent(event);
        //then
        verify(systemExiter, times(ONCE)).exit(EXIT_CODE_SUCCESS);
    }
}