package it.echoeswar.management.security.session.config;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "security.refresh-token.cookie")
public record RefreshTokenCookieProperties(

        @NotBlank
        String name,

        @NotBlank
        @Pattern(regexp = "/.*")
        String path,

        @NotNull
        Boolean secure,

        @NotBlank
        @Pattern(regexp = "Strict|Lax|None")
        String sameSite
) {

    @AssertTrue(
            message = "SameSite=None requires a secure refresh token cookie."
    )
    public boolean isSameSiteCompatibleWithSecure() {
        return !"None".equals(sameSite) || Boolean.TRUE.equals(secure);
    }
}
