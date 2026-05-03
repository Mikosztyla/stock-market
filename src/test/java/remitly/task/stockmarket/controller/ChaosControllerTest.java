package remitly.task.stockmarket.controller;

import io.qameta.allure.Description;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ChaosControllerTest {

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private ChaosController chaosController;

    @Test
    @Description("POST /chaos should return HTTP 200")
    void shouldReturn200() {
        // when
        ResponseEntity<Void> response = chaosController.kill();
        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @Description("POST /chaos should publish a ChaosShutdownEvent")
    void shouldPublishChaosShutdownEvent() {
        // when
        chaosController.kill();
        // then
        ArgumentCaptor<ChaosShutdownEvent> captor = ArgumentCaptor.forClass(ChaosShutdownEvent.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue()).isInstanceOf(ChaosShutdownEvent.class);
    }

    @Test
    @Description("POST /chaos should publish exactly one event")
    void shouldPublishExactlyOneEvent() {
        // when
        chaosController.kill();
        // then
        verify(applicationEventPublisher, times(1))
                .publishEvent(org.mockito.ArgumentMatchers.any(ChaosShutdownEvent.class));
    }
}