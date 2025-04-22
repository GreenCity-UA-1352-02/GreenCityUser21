package greencity.message;

import greencity.constant.ValidationConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public final class SendChangePlaceStatusEmailMessage implements Serializable {
    @NotBlank
    private String authorFirstName;
    @NotBlank
    private String placeName;
    @NotBlank
    private String placeStatus;
    @Email(message = ValidationConstants.INVALID_EMAIL)
    @NotBlank
    private String authorEmail;
}
