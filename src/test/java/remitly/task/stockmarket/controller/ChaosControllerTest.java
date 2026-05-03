package remitly.task.stockmarket.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import remitly.task.stockmarket.chaos.ChaosShutdownEvent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ChaosControllerTest {

    private static final int ONCE = 1;
    private static final HttpStatus EXPECTED_STATUS = HttpStatus.OK;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private ChaosController chaosController;

    @Test
    void shouldReturnHttp200() {
        //when
        ResponseEntity<Void> response = chaosController.kill();
        //then
        assertThat(response.getStatusCode()).isEqualTo(EXPECTED_STATUS);
    }

    @Test
    void shouldPublishChaosShutdownEvent() {
        //when
        chaosController.kill();
        //then
        ArgumentCaptor<ChaosShutdownEvent> captor = ArgumentCaptor.forClass(ChaosShutdownEvent.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue()).isInstanceOf(ChaosShutdownEvent.class);
    }

    @Test
    void shouldPublishExactlyOneEvent() {
        //when
        chaosController.kill();
        //then
        verify(applicationEventPublisher, times(ONCE)).publishEvent(any(ChaosShutdownEvent.class));
    }
}