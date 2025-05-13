package greencity.security.handlers;

import greencity.dto.language.LanguageVO;
import greencity.dto.ownsecurity.OwnSecurityVO;
import greencity.dto.user.UserVO;
import greencity.enums.EmailNotification;
import greencity.enums.Role;
import greencity.enums.UserStatus;
import greencity.repository.LanguageRepo;
import greencity.security.jwt.JwtTool;
import greencity.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import static greencity.constant.AppConstant.DEFAULT_RATING;
import static greencity.entity.User_.refreshTokenKey;

@Slf4j
@Component
public class JsonAuthSuccessHandler implements AuthenticationSuccessHandler {
    private final LanguageRepo languageRepo;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final JwtTool jwtTool;

    public JsonAuthSuccessHandler(LanguageRepo languageRepo, ModelMapper modelMapper, PasswordEncoder passwordEncoder, UserService userService, JwtTool jwtTool) {
        this.languageRepo = languageRepo;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
        this.jwtTool = jwtTool;
    }

    @Transactional
    public UserVO createUserVO(String email, String name) {
        LanguageVO lang = modelMapper.map(Optional.of(languageRepo.findById(1L)), LanguageVO.class);

        UserVO userVO = UserVO.builder()
                .email(email)
                .name(name)
                .dateOfRegistration(LocalDateTime.now())
                .role(Role.ROLE_USER)
                .refreshTokenKey(String.valueOf(refreshTokenKey))
                .lastActivityTime(LocalDateTime.now())
                .userStatus(UserStatus.CREATED)
                .emailNotification(EmailNotification.DISABLED)
                .rating(DEFAULT_RATING)
                .languageVO(lang)
                .build();
        OwnSecurityVO dto = OwnSecurityVO.builder().user(userVO).password(passwordEncoder.encode(java.util.UUID.randomUUID().toString())).build();
        userVO.setOwnSecurity(dto);
        return userVO;
    }

    @Transactional
    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;

        String email = oauthToken.getPrincipal().getAttributes().get("email").toString();
        String name = oauthToken.getPrincipal().getAttributes().get("name").toString();
        UserVO userVO;
        if (!userService.existsByEmail(email)) {
            userVO = userService.save(createUserVO(email, name));
        } else {
            userVO = userService.findByEmail(email);
        }
        String accessToken = jwtTool.createAccessToken(userVO.getEmail(), userVO.getRole());
        String refreshToken = jwtTool.createRefreshToken(userVO);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{" +
                "\"userId\": " + userVO.getId() + "," +
                "\"accessToken\": \"" + accessToken + "\"," +
                "\"refreshToken\": \"" + refreshToken + "\"," +
                "\"name\": \"" + userVO.getName() + "\"" +
                "}");
    }

}

