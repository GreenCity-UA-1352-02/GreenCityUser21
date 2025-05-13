package greencity.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

@Configuration
public class OAuth2ClientConfig {
    /**
     * Provides a {@link ClientRegistrationRepository} bean configured with an in-memory client registration
     * for the Google OAuth2 provider. The client registration includes details such as client ID, client
     * secret, scopes, authorization grant type, redirect URI, and URIs for authorization, token, userInfo,
     * and jwkSet.
     * Authentification URL: http://localhost:8060/oauth2/authorization/google
     * @return a configured instance of {@code ClientRegistrationRepository}.
     */
    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        ClientRegistration google = ClientRegistration.withRegistrationId("google")
                .clientId("959492496977-ev00ibp7mlq37i88dhhln8hftpo39dv5.apps.googleusercontent.com")
                .clientSecret("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")
                .scope("openid", "profile", "email")
                .authorizationGrantType(org.springframework.security.oauth2.core.AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://localhost:8060/login/oauth2/code/{registrationId}")
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .tokenUri("https://oauth2.googleapis.com/token")
                .userInfoUri("https://openidconnect.googleapis.com/v1/userinfo")
                .jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
                .userNameAttributeName("sub")
                .build();
        return new InMemoryClientRegistrationRepository(google);
    }
}
