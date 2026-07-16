package fit.iuh.se.hsauth.entity.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

/**
 * @author : user664dntp
 * @mailto : phatdang19052004@gmail.com
 * @created : 16/07/2026, Thursday
 **/

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum CookieType {
    REFRESH_TOKEN("refresh-token"),
    SESSION_ID("session-id");

    String type;

    CookieType(String type) {
        this.type = type;
    }
}
