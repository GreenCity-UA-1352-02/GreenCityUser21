package greencity.dto.violation;

import greencity.constant.ValidationConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class UserViolationMailDto {
    @NotNull
    private String name;
    @NotNull
    @Email(message = ValidationConstants.INVALID_EMAIL)
    private String email;
    @NotNull
    private String language;
    private String violationDescription;
}
