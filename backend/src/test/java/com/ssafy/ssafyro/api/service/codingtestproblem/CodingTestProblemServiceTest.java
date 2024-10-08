package com.ssafy.ssafyro.api.service.codingtestproblem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.ssafy.ssafyro.IntegrationTestSupport;
import com.ssafy.ssafyro.api.service.codingtestproblem.response.CodingTestProblemResponse;
import com.ssafy.ssafyro.api.service.codingtestproblem.response.CodingTestProblemsResponse;
import com.ssafy.ssafyro.domain.codingtestproblem.CodingTestProblem;
import com.ssafy.ssafyro.domain.codingtestproblem.CodingTestProblemRepository;
import com.ssafy.ssafyro.domain.codingtestproblem.Difficulty;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class CodingTestProblemServiceTest extends IntegrationTestSupport {

    @Autowired
    private CodingTestProblemRepository codingTestProblemRepository;

    @Autowired
    private CodingTestProblemService codingTestProblemService;

    @DisplayName("코딩 테스트 문제 목록을 조회한다.")
    @Test
    void getProblems() {
        //given
        CodingTestProblem problem1 = createProblem("문제1");
        CodingTestProblem problem2 = createProblem("문제2");

        codingTestProblemRepository.saveAll(List.of(problem1, problem2));

        //when
        CodingTestProblemsResponse response = codingTestProblemService.getProblems(PageRequest.of(0, 10));

        //then
        assertThat(response.getProblemInfos())
                .extracting("id", "title")
                .containsExactlyInAnyOrder(
                        tuple(problem1.getId(), "문제1"),
                        tuple(problem2.getId(), "문제2")
                );
    }

    @DisplayName("코딩 테스트 문제를 상세 조회한다.")
    @Test
    void getProblem() {
        //given
        CodingTestProblem problem = createProblem("문제1");

        codingTestProblemRepository.save(problem);

        //when
        CodingTestProblemResponse response = codingTestProblemService.getProblem(problem.getId());

        //then
        assertThat(response)
                .extracting("id", "title", "difficulty", "correctRate", "recommendationCount", "problemUrl")
                .containsExactly(
                        problem.getId(), "문제1", Difficulty.D1, 100.0, 0, "https://example.com"
                );
    }

    private CodingTestProblem createProblem(String title) {
        return CodingTestProblem.builder()
                .title(title)
                .difficulty(Difficulty.D1)
                .correctRate(100.0)
                .recommendationCount(0)
                .problemUrl("https://example.com")
                .build();
    }
}