package greencity.security.handlers;

import greencity.dto.language.LanguageVO;
import greencity.dto.user.UserVO;
import greencity.entity.Language;
import greencity.enums.EmailNotification;
import greencity.enums.Role;
import greencity.enums.UserStatus;
import greencity.repository.LanguageRepo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class JsonAuthSuccessHandlerTest {
    @Mock
    private LanguageRepo languageRepo;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private JsonAuthSuccessHandler jsonAuthSuccessHandler;

    @Test
    public void testCreateUserVO() {
        String email = "test@example.com";
        String name = "Test User";
        Long langId = 1L;

        Language language = new Language();
        LanguageVO languageVO = new LanguageVO();

        when(modelMapper.map(language, LanguageVO.class)).thenReturn(languageVO);
        when(languageRepo.findById(langId)).thenReturn(Optional.of(language));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");

        UserVO result = jsonAuthSuccessHandler.createUserVO(email, name);

        Assertions.assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertEquals(name, result.getName());
        assertEquals(Role.ROLE_USER, result.getRole());
        assertEquals(UserStatus.CREATED, result.getUserStatus());
        assertEquals(EmailNotification.DISABLED, result.getEmailNotification());
        Assertions.assertNotNull(result.getDateOfRegistration());
        Assertions.assertNotNull(result.getLastActivityTime());
        Assertions.assertNotNull(result.getOwnSecurity());
        assertEquals("encoded-password", result.getOwnSecurity().getPassword());

        verify(languageRepo).findById(langId);
        verify(passwordEncoder).encode(anyString());
    }
}
