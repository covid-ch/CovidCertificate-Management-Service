package ch.admin.bag.covidcertificate.authorization;

import ch.admin.bag.covidcertificate.api.Constants;
import ch.admin.bag.covidcertificate.api.exception.AuthorizationError;
import ch.admin.bag.covidcertificate.api.exception.AuthorizationException;
import ch.admin.bag.covidcertificate.authorization.config.AuthorizationConfig;
import ch.admin.bag.covidcertificate.authorization.config.LocalDateTimeConverter;
import ch.admin.bag.covidcertificate.authorization.config.RoleConfig;
import ch.admin.bag.covidcertificate.testutil.JeapAuthenticationTestTokenBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {AuthorizationInterceptor.class, AuthorizationService.class, AuthorizationConfig.class, RoleConfig.class, LocalDateTimeConverter.class})
@ActiveProfiles(profiles = {"test", "auth-test"})
@EnableConfigurationProperties
public class AuthorizationInterceptorTest {
    private final Object handler = new Object();

    @Autowired
    private AuthorizationInterceptor interceptor;
    @Mock
    private MockServletContext mockServletContext;
    @Mock
    private MockHttpServletResponse response;

    @Value("${cc-management-service.auth.allow-unauthenticated}")
    private String allowUnauthenticated;

    @Test
    public void testAllowUnauthenticated() {
        MockHttpServletRequest request = mockRequestWithClientId("/uriToNowhere", allowUnauthenticated);
        assertTrue(interceptor.preHandle(request, response, handler));
    }

    @Test
    public void testNoFunctionConfigured() {
        MockHttpServletRequest request = mockRequest("/uriToNowhere");
        assertError(request, Constants.NO_FUNCTION_CONFIGURED);
    }

    @Test
    public void testToManyFunctionConfigured() {
        MockHttpServletRequest request = mockRequest("/too-many-funcs");
        assertError(request, Constants.TOO_MANY_FUNCTIONS_CONFIGURED);
    }

    @Test
    public void testWrongHttpMethod() {
        MockHttpServletRequest request = mockRequest("/db", "WEB-USER");
        request.setMethod(HttpMethod.DELETE.name());
        assertError(request, Constants.FORBIDDEN);
    }

    @Test
    public void testCorrectHttpMethod() {
        MockHttpServletRequest request = mockRequest("/db", "ADMIN");
        request.setMethod(HttpMethod.DELETE.name());
        assertTrue(interceptor.preHandle(request, response, handler));
    }
    @Test
    public void testRoleMissing() {
        MockHttpServletRequest request = mockRequest("/only-web-user", "USELESS_ROLE");
        assertError(request, Constants.FORBIDDEN);
    }


    @Test
    public void testRolePresent() {
        MockHttpServletRequest request = mockRequest("/only-web-user", "WEB-USER");
        assertTrue(interceptor.preHandle(request, response, handler));
    }

    @Test
    public void testRefRoleMissing() {
        MockHttpServletRequest request = mockRequest("/web-user-and-admin", "ADMIN");
        assertError(request, Constants.FORBIDDEN);
    }

    @Test
    public void testRefRolePresent() {
        MockHttpServletRequest request = mockRequest("/web-user-and-admin", "WEB-USER", "ADMIN");
        assertTrue(interceptor.preHandle(request, response, handler));
    }

    @Test
    public void testChainRolesMissing() {
        MockHttpServletRequest request = mockRequest("/chain-web-and-admin", "CHAIN", "ADMIN");
        assertError(request, Constants.FORBIDDEN);
    }

    @Test
    public void testChainRolesPresent() {
        MockHttpServletRequest request = mockRequest("/chain-web-and-admin", "CHAIN", "ADMIN", "WEB-USER");
        assertTrue(interceptor.preHandle(request, response, handler));
    }

    @Test
    public void testAllowedInFuture() {
        MockHttpServletRequest request = mockRequest("/future", "WEB-USER");
        assertError(request, Constants.NO_FUNCTION_CONFIGURED);
    }


    @Test
    public void testAllowedInPast() {
        MockHttpServletRequest request = mockRequest("/past", "WEB-USER");
        assertError(request, Constants.NO_FUNCTION_CONFIGURED);
    }

    @Test
    public void userHinErpAuthorized() {
        MockHttpServletRequest request = mockRequest("/only-web-user", "WEB-USER", "bag-cc-hin-epr", "bag-cc-hincode", "bac-cc-personal");
        assertTrue(interceptor.preHandle(request, response, handler));
    }

    @Test
    public void userHinAuthorized() {
        MockHttpServletRequest request = mockRequest("/only-web-user", "WEB-USER", "bag-cc-hin", "bag-cc-hincode", "bac-cc-personal");
        assertTrue(interceptor.preHandle(request, response, handler));
    }

    @Test
    public void userHinNotAuthorized() {
        MockHttpServletRequest request = mockRequest("/only-web-user", "WEB-USER", "bag-cc-hin");
        assertError(request, Constants.ACCESS_DENIED_FOR_HIN_WITH_CH_LOGIN);
    }

    private MockHttpServletRequest mockRequest(String uri, String... roles) {
        return mockRequestWithClientId(uri, "cc-management-ui", roles);
    }

    private MockHttpServletRequest mockRequestWithClientId(String uri, String clientId, String... roles) {
        SecurityContextHolder.getContext()
                .setAuthentication(JeapAuthenticationTestTokenBuilder.create()
                        .withClaim("clientId", clientId)
                        .withUserRoles(roles)
                        .build());

        return get(uri).buildRequest(mockServletContext);
    }

    private void assertError(MockHttpServletRequest request, AuthorizationError error) {
        AuthorizationException exception = assertThrows(AuthorizationException.class,
                () -> interceptor.preHandle(request, response, handler));
        assertEquals(error.getErrorCode(), exception.getError().getErrorCode());
    }
}

