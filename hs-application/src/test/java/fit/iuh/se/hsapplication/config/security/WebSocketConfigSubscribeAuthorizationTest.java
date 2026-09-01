package fit.iuh.se.hsapplication.config.security;

import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hschat.repository.ConsultationParticipantRepository;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Gác quyền subscribe kênh thông báo realtime:
 * chỉ được nghe queue của chính mình và topic đúng vai của mình.
 */
@ExtendWith(MockitoExtension.class)
class WebSocketConfigSubscribeAuthorizationTest {

    @Mock
    JwtDecoder jwtDecoder;

    @Mock
    ConsultationParticipantRepository participantRepository;

    ChannelInterceptor interceptor;

    @BeforeEach
    void setUp() {
        WebSocketConfig config = new WebSocketConfig(jwtDecoder, participantRepository);
        ReflectionTestUtils.setField(config, "topicPrefix", "/topic");
        ReflectionTestUtils.setField(config, "queuePrefix", "/queue");

        ChannelRegistration registration = mock(ChannelRegistration.class);
        config.configureClientInboundChannel(registration);
        ArgumentCaptor<ChannelInterceptor> captor = ArgumentCaptor.forClass(ChannelInterceptor.class);
        verify(registration).interceptors(captor.capture());
        interceptor = captor.getValue();
    }

    private Message<byte[]> subscribeAs(Long userId, UserRole role, String destination) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination(destination);
        accessor.setUser(new UsernamePasswordAuthenticationToken(
                new UserAuthentication(userId, "user@test.dev", role), null, List.of()));
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    @Test
    void allowsSubscribingToOwnNotificationQueue() {
        Message<byte[]> message = subscribeAs(42L, UserRole.MEMBER, "/queue/notifications/42");
        assertThatCode(() -> interceptor.preSend(message, null)).doesNotThrowAnyException();
    }

    @Test
    void rejectsSubscribingToAnotherUsersNotificationQueue() {
        Message<byte[]> message = subscribeAs(42L, UserRole.MEMBER, "/queue/notifications/99");
        assertThatThrownBy(() -> interceptor.preSend(message, null))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void allowsSubscribingToOwnRoleTopic() {
        Message<byte[]> message = subscribeAs(7L, UserRole.DOCTOR, "/topic/notifications/roles/DOCTOR");
        assertThatCode(() -> interceptor.preSend(message, null)).doesNotThrowAnyException();
    }

    @Test
    void rejectsSubscribingToAnotherRolesTopic() {
        Message<byte[]> message = subscribeAs(7L, UserRole.MEMBER, "/topic/notifications/roles/ADMIN");
        assertThatThrownBy(() -> interceptor.preSend(message, null))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void rejectsNotificationSubscribeWithoutAuthentication() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/queue/notifications/42");
        accessor.setLeaveMutable(true);
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        assertThatThrownBy(() -> interceptor.preSend(message, null))
                .isInstanceOf(AccessDeniedException.class);
    }
}
