package com.ssafy.ssafyro.api.controller.essay.request;

import com.ssafy.ssafyro.api.service.essay.request.EssaySaveServiceRequest;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record EssaySaveRequest(@NotNull Long essayQuestionId,
                               @NotEmpty String content) {

    public EssaySaveServiceRequest toServiceRequest() {
        return new EssaySaveServiceRequest(essayQuestionId, content);
    }
}
