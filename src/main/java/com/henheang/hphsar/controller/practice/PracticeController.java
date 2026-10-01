package com.henheang.hphsar.controller.practice;

import com.henheang.hphsar.common.api.ApiResponse;
import com.henheang.hphsar.common.api.Code;
import com.henheang.hphsar.controller.BaseController;
import com.henheang.hphsar.model.practice.CurrentUserResponse;
import com.henheang.hphsar.service.PracticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Practice endpoints for learning the h-phsar request flow.
 * <p>
 * Not under a role base path, so any authenticated user may call it ({@code anyRequest().authenticated()}).
 *
 * @author Hen-Heang
 * @since 2026.09.28
 * @version 1.0
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Practice Controller")
@RequestMapping("/api/v1/practice")
@SecurityRequirement(name = "bearerAuth")
public class PracticeController extends BaseController {

    private final PracticeService practiceService;

    /**
     * Returns the authenticated user's non-sensitive details.
     *
     * @return {@link CurrentUserResponse} wrapped in {@link ApiResponse} with {@link Code#FETCHED}
     */
    @Operation(summary = "Get the authenticated user's safe details")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<CurrentUserResponse>> getCurrentUser() {
        return ok(Code.FETCHED, practiceService.getCurrentUser());
    }
}
