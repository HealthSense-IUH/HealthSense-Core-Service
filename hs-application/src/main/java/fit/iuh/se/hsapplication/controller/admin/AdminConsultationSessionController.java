package fit.iuh.se.hsapplication.controller.admin;

import fit.iuh.se.hschat.dto.response.ConsultationSessionResponse;
import fit.iuh.se.hschat.service.session.ConsultationSessionService;
import fit.iuh.se.hsshared.dto.response.ApiResponse;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/admin/consultation-sessions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminConsultationSessionController {

    ConsultationSessionService consultationSessionService;

    @GetMapping
    public ApiResponse<PageResponse<ConsultationSessionResponse>> getSessionsForAdmin(
            @RequestParam(name = "page", defaultValue = "1") @Min(1) int page,
            @RequestParam(name = "size", defaultValue = "10") @Min(1) int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return new ApiResponse<>(consultationSessionService.getSessionsForAdmin(pageable));
    }
}
