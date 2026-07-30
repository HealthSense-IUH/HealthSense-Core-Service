package fit.iuh.se.hsapplication.config.security;

import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hschat.repository.ConsultationParticipantRepository;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    JwtDecoder jwtDecoder;
    ConsultationParticipantRepository participantRepository;

    @NonFinal
    @Value("${app.websocket.endpoint}")
    String endpoint;

    @NonFinal
    @Value("${app.websocket.allowed-origin-patterns}")
    String allowedOriginPatterns;

    @NonFinal
    @Value("${app.websocket.application-destination-prefix}")
    String applicationDestinationPrefix;

    @NonFinal
    @Value("${app.websocket.topic-prefix}")
    String topicPrefix;

    @NonFinal
    @Value("${app.websocket.queue-prefix}")
    String queuePrefix;

    @NonFinal
    @Value("${app.websocket.bearer-prefix}")
    String bearerPrefix;

    @NonFinal
    @Value("${app.websocket.access-token-type:access}")
    String accessTokenType;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(endpoint)
                .setAllowedOriginPatterns(splitCsv(allowedOriginPatterns))
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker(topicPrefix, queuePrefix);
        registry.setApplicationDestinationPrefixes(applicationDestinationPrefix);
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor == null)
                    return message;

                if (accessor.getCommand() == StompCommand.CONNECT) {
                    accessor.setUser(authenticate(accessor));
                    return message;
                }

                if (accessor.getCommand() == StompCommand.SUBSCRIBE)
                    authorizeSubscribe(accessor);

                return message;
            }
        });
    }

    private UsernamePasswordAuthenticationToken authenticate(StompHeaderAccessor accessor) {
        String authorizationHeader = accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader == null || !authorizationHeader.startsWith(bearerPrefix))
            throw new AccessDeniedException("Missing access token");

        try {
            Jwt jwt = jwtDecoder.decode(authorizationHeader.substring(bearerPrefix.length()));
            String tokenType = jwt.getClaimAsString("type");
            if (!accessTokenType.equals(tokenType))
                throw new AccessDeniedException("JWT is not an access token");

            UserRole role = UserRole.valueOf(jwt.getClaimAsString("role"));
            UserAuthentication principal = new UserAuthentication(
                    Long.valueOf(Objects.requireNonNull(jwt.getSubject())),
                    jwt.getClaimAsString("email"),
                    role
            );

            return new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
            );
        } catch (JwtException | IllegalArgumentException exception) {
            throw new AccessDeniedException("Invalid access token", exception);
        }
    }

    private void authorizeSubscribe(StompHeaderAccessor accessor) {
        Long sessionId = extractConsultationSessionId(accessor.getDestination());
        if (sessionId == null)
            return;

        UserAuthentication user = extractCurrentUser(accessor);
        boolean participant = participantRepository.existsBySessionIdAndUserIdAndActiveTrue(
                sessionId,
                user.getUserId()
        );

        if (!participant)
            throw new AccessDeniedException("You are not allowed to subscribe to this consultation session");
    }

    private UserAuthentication extractCurrentUser(StompHeaderAccessor accessor) {
        if (!(accessor.getUser() instanceof UsernamePasswordAuthenticationToken authentication)
                || !(authentication.getPrincipal() instanceof UserAuthentication user))
            throw new AccessDeniedException("Missing websocket authentication");

        return user;
    }

    private Long extractConsultationSessionId(String destination) {
        String prefix = topicPrefix + "/consultation-sessions/";
        if (destination == null || !destination.startsWith(prefix))
            return null;

        String sessionId = destination.substring(prefix.length());
        int nextSlash = sessionId.indexOf('/');
        if (nextSlash >= 0)
            sessionId = sessionId.substring(0, nextSlash);

        try {
            return Long.valueOf(sessionId);
        } catch (NumberFormatException exception) {
            throw new AccessDeniedException("Invalid consultation session topic", exception);
        }
    }

    private String[] splitCsv(String value) {
        return Stream.of(value.split(","))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .toArray(String[]::new);
    }
}
