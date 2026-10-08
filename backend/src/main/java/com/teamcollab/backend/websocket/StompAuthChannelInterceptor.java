package com.teamcollab.backend.websocket;

import com.teamcollab.backend.entity.Role;
import com.teamcollab.backend.entity.User;
import com.teamcollab.backend.repository.ProjectRepository;
import com.teamcollab.backend.security.JwtService;
import com.teamcollab.backend.security.UserPrincipal;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The regular JwtAuthenticationFilter only runs on normal HTTP requests - it never sees traffic
 * on this WebSocket connection after the initial upgrade. So authentication/authorization for
 * STOMP frames has to be done here instead, at the two points that matter:
 *   - CONNECT: the client presents its JWT once, up front, to establish who they are.
 *   - SUBSCRIBE: before letting them listen to any /topic/projects/{id}/... topic (chat,
 *     task-update events, and anything added later), check they're a member of that project.
 * (SEND is intentionally left for the relevant @MessageMapping method / REST controller -
 * each already needs its own membership check to persist whatever it's handling.)
 */
@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private static final Pattern PROJECT_TOPIC = Pattern.compile("/topic/projects/(\\d+)/.+");

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final ProjectRepository projectRepository;

    public StompAuthChannelInterceptor(JwtService jwtService, UserDetailsService userDetailsService,
                                        ProjectRepository projectRepository) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.projectRepository = projectRepository;
    }

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            authenticateConnect(accessor);
        } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            authorizeSubscribe(accessor);
        }

        return message;
    }

    private void authenticateConnect(StompHeaderAccessor accessor) {
        String header = accessor.getFirstNativeHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new MessagingException("Missing Authorization header on CONNECT");
        }

        String token = header.substring(7);
        try {
            String email = jwtService.extractEmail(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            if (!jwtService.isTokenValid(token, userDetails)) {
                throw new MessagingException("Invalid or expired token");
            }
            var authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            accessor.setUser(authentication);
        } catch (MessagingException e) {
            throw e;
        } catch (Exception e) {
            throw new MessagingException("Could not authenticate WebSocket connection", e);
        }
    }

    private void authorizeSubscribe(StompHeaderAccessor accessor) {
        Principal principal = accessor.getUser();
        if (principal == null) {
            throw new MessagingException("Not authenticated");
        }

        String destination = accessor.getDestination();
        if (destination == null) {
            return;
        }
        Matcher matcher = PROJECT_TOPIC.matcher(destination);
        if (!matcher.matches()) {
            return; // not a project chat topic - nothing for this interceptor to check
        }

        Long projectId = Long.valueOf(matcher.group(1));
        User user = resolveUser(principal);

        boolean isMember = user.getRole() == Role.ADMIN || projectRepository.isMember(projectId, user.getId());
        if (!isMember) {
            throw new MessagingException("Not a member of project " + projectId);
        }
    }

    static User resolveUser(Principal principal) {
        var authentication = (UsernamePasswordAuthenticationToken) principal;
        return ((UserPrincipal) authentication.getPrincipal()).getUser();
    }
}
