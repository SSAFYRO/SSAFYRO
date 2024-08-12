package com.ssafy.ssafyro.api.service.interviewresult;

import com.ssafy.ssafyro.api.service.interviewresult.response.InterviewResultsResponse;
import com.ssafy.ssafyro.domain.interviewresult.InterviewResultDocument;
import com.ssafy.ssafyro.domain.interviewresult.InterviewResultDocumentRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class InterviewResultService {

    private final InterviewResultDocumentRepository interviewResultDocumentRepository;

    public InterviewResultsResponse getRecommendInterviewResultsFor(Long userId, Pageable pageable) {
        List<String> tags = getWorstQuestionTagsFor(userId);

        return InterviewResultsResponse.of(
                interviewResultDocumentRepository.findBestInterviewResultBy(tags, userId, pageable)
        );
    }

    public InterviewResultsResponse getInterviewResultsBy(Long userId, Pageable pageable) {
        return InterviewResultsResponse.of(
                interviewResultDocumentRepository.findBy(userId, pageable)
        );
    }

    private List<String> getWorstQuestionTagsFor(Long userId) {
        return interviewResultDocumentRepository.findTop5ByUserIdOrderByEvaluationScore(userId).stream()
                .map(InterviewResultDocument::getQuestionTags)
                .flatMap(List::stream)
                .toList();
    }
}
