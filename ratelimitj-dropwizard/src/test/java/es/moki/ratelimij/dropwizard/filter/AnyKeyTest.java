package es.moki.ratelimij.dropwizard.filter;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.SecurityContext;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AnyKeyTest {

    private HttpServletRequest request = mock(HttpServletRequest.class);

    private ResourceInfo resource = mock(ResourceInfo.class);

    private SecurityContext securityContext = mock(SecurityContext.class);

    @BeforeEach
    void beforeEach() throws Exception {
        doReturn(Object.class).when(resource).getResourceClass();
        when(resource.getResourceMethod()).thenReturn(Object.class.getMethod("wait"));
    }

    @DisplayName("ANY key should start with 'rlj' prefix")
    @Test
    void shouldStartWithPrefix() {
        when(request.getRemoteAddr()).thenReturn("293.0.120.7");

        Optional<String> keyName = Key.ANY.create(request, resource, securityContext);

        assertThat(keyName.get()).startsWith("rlj:");
    }

    @DisplayName("ANY key should include Class and Method names in key")
    @Test
    void shouldIncludeResourceInKey() {
        when(request.getRemoteAddr()).thenReturn("293.0.120.7");

        Optional<String> keyName = Key.ANY.create(request, resource, securityContext);

        assertThat(keyName.get()).contains("java.lang.Object#wait");
    }

    @DisplayName("ANY key should include user id if available")
    @Test
    void shouldIncludeUserId() {
        when(securityContext.getUserPrincipal()).thenReturn(() -> "elliot");

        Optional<String> keyName = Key.ANY.create(request, resource, securityContext);

        assertThat(keyName.get()).contains("usr#elliot");
    }

    @DisplayName("ANY key should include X-Forwarded-For if available")
    @Test
    void shouldIncludeXForwardedForIfUserNull() {
        when(request.getHeader("X-Forwarded-For")).thenReturn("293.0.113.7,  211.1.16.2");

        Optional<String> keyName = Key.ANY.create(request, resource, securityContext);

        assertThat(keyName.get()).contains("xfwd4#293.0.113.7");
    }

    @DisplayName("ANY key should include remote IP if available and user and X-Forwarded-For not found")
    @Test
    void shouldIncludeRemoteIpIfUserAndXForwarded4Null() {
        when(request.getRemoteAddr()).thenReturn("293.0.120.7");

        Optional<String> keyName = Key.ANY.create(request, resource, securityContext);

        assertThat(keyName.get()).contains("ip#293.0.120.7");
    }

    @DisplayName("ANY key should return absent if no key available")
    @Test
    void shouldBeAbsent() {
        when(request.getRemoteAddr()).thenReturn(null);

        Optional<String> keyName = Key.ANY.create(request, resource, securityContext);

        assertThat(keyName).isNotPresent();
    }

}