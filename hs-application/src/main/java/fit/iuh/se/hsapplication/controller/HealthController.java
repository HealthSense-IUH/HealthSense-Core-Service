package fit.iuh.se.hsapplication.controller;

import fit.iuh.se.hsshared.dto.response.ApiResponse;
import fit.iuh.se.hsuser.dto.response.AuthenticationUser;
import fit.iuh.se.hsuser.entity.enums.AccountStatus;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : user664dntp
 * @mailto : phatdang19052004@gmail.com
 * @created : 14/07/2026, Tuesday
 **/

@RestController
@RequestMapping("/api/health")
public class HealthController {
    @GetMapping
    public ApiResponse<AuthenticationUser> test() {
        return new ApiResponse<AuthenticationUser>(
                new AuthenticationUser(
                        1L,
                        "John Doe",
                        "john.doe@example.com",
                        UserRole.ADMIN,
                        AccountStatus.ACTIVE
                )
        );
    }
}
