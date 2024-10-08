package com.ssafy.ssafyro.api.controller.itknowledge;

import static com.ssafy.ssafyro.api.ApiUtils.success;

import com.ssafy.ssafyro.api.ApiUtils.ApiResult;
import com.ssafy.ssafyro.api.controller.itknowledge.request.ItKnowledgeDetailRequest;
import com.ssafy.ssafyro.api.service.itknowledge.ItKnowledgeService;
import com.ssafy.ssafyro.api.service.itknowledge.response.ItKnowledgeDetailResponse;
import com.ssafy.ssafyro.api.service.itknowledge.response.ItKnowledgeListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ItKnowledgeController {

    private final ItKnowledgeService itKnowledgeService;

    @GetMapping("/api/v1/it-knowledge/{id}")
    public ApiResult<ItKnowledgeDetailResponse> getItKnowledgeDetail(@ModelAttribute ItKnowledgeDetailRequest request) {
        return success(itKnowledgeService.getItKnowledgeDetail(request.toServiceRequest()));
    }

    @GetMapping("/api/v1/it-knowledge")
    public ApiResult<ItKnowledgeListResponse> getItKnowledgeList(Pageable pageable) {
        return success(itKnowledgeService.getItKnowledgeList(pageable));
    }
}
