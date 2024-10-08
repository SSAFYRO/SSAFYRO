package com.ssafy.ssafyro.api.controller.essay;

import static com.ssafy.ssafyro.api.ApiUtils.success;

import com.ssafy.ssafyro.api.ApiUtils.ApiResult;
import com.ssafy.ssafyro.api.controller.essay.request.EssayReviewRequest;
import com.ssafy.ssafyro.api.controller.essay.request.EssaySaveRequest;
import com.ssafy.ssafyro.api.service.essay.EssayService;
import com.ssafy.ssafyro.api.service.essay.response.EssayDetailResponse;
import com.ssafy.ssafyro.api.service.essay.response.EssayReviewResponse;
import com.ssafy.ssafyro.api.service.essay.response.EssaySaveResponse;
import com.ssafy.ssafyro.api.service.essay.response.EssayUpdateResponse;
import com.ssafy.ssafyro.security.JwtAuthentication;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class EssayController {

    private final EssayService essayService;

    @PostMapping("/api/v1/essays/review")
    public ApiResult<EssayReviewResponse> reviewEssay(@Valid @RequestBody EssayReviewRequest request) {
        return success(essayService.reviewEssay(request.toServiceRequest()));
    }

    @PostMapping("/api/v1/essays")
    public ApiResult<EssaySaveResponse> createEssay(@AuthenticationPrincipal JwtAuthentication userInfo,
                                                    @Valid @RequestBody EssaySaveRequest request) {
        return success(essayService.createEssayBy(userInfo.id(), request.toServiceRequest()));
    }

    @GetMapping("/api/v1/essays")
    public ApiResult<EssayDetailResponse> findEssay(@RequestParam Long userId) {
        return success(essayService.findBy(userId));
    }

    @PutMapping("/api/v1/essays")
    public ApiResult<EssayUpdateResponse> updateEssay(@AuthenticationPrincipal JwtAuthentication userInfo,
                                                      @Valid @RequestBody EssaySaveRequest request) {
        return success(essayService.updateEssayBy(userInfo.id(), request.toServiceRequest()));
    }
}
