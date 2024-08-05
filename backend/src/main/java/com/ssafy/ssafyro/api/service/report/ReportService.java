package com.ssafy.ssafyro.api.service.report;

import com.ssafy.ssafyro.api.service.interview.ChatGptResponseGenerator;
import com.ssafy.ssafyro.api.service.report.request.ReportCreateServiceRequest;
import com.ssafy.ssafyro.api.service.report.response.ReportCreateResponse;
import com.ssafy.ssafyro.api.service.report.response.ReportListResponse;
import com.ssafy.ssafyro.domain.article.Article;
import com.ssafy.ssafyro.domain.article.ArticleRepository;
import com.ssafy.ssafyro.domain.interview.InterviewInfos;
import com.ssafy.ssafyro.domain.interview.InterviewRedisRepository;
import com.ssafy.ssafyro.domain.interviewresult.InterviewResultRepository;
import com.ssafy.ssafyro.domain.report.PersonalityInterviewReport;
import com.ssafy.ssafyro.domain.report.PresentationInterviewReport;
import com.ssafy.ssafyro.api.service.report.response.ReportPresentationResponse;
import com.ssafy.ssafyro.api.service.report.response.ReportResponse;
import com.ssafy.ssafyro.api.service.report.response.ReportsResponse;
import com.ssafy.ssafyro.domain.article.Article;
import com.ssafy.ssafyro.domain.interviewresult.InterviewResult;
import com.ssafy.ssafyro.domain.interviewresult.InterviewResultRepository;
import com.ssafy.ssafyro.domain.report.Report;
import com.ssafy.ssafyro.domain.report.ReportRepository;
import com.ssafy.ssafyro.domain.room.entity.Room;
import com.ssafy.ssafyro.domain.room.entity.RoomRepository;
import com.ssafy.ssafyro.domain.user.User;
import com.ssafy.ssafyro.domain.user.UserRepository;
import com.ssafy.ssafyro.error.article.ArticleNotFoundException;
import com.ssafy.ssafyro.error.room.RoomNotFoundException;
import com.ssafy.ssafyro.error.report.ReportNotFoundException;
import com.ssafy.ssafyro.error.user.UserNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class ReportService {

    private final ChatGptResponseGenerator chatGptResponseGenerator;

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final ReportRepository reportRepository;
    private final ArticleRepository articleRepository;
    private final InterviewResultRepository interviewResultRepository;

    private final InterviewRedisRepository interviewRedisRepository;

    public ReportsResponse getReports(Long userId, Pageable pageable) {
        User user = getUser(userId);

        return ReportsResponse.of(
                reportRepository.findAllByUser(user, pageable).getContent()
        );
    }

    //TODO: User 검증 필요 (시큐리티 후에 작업 요망)
    public ReportResponse getReport(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(ReportNotFoundException::new);

        List<InterviewResult> interviewResult = interviewResultRepository.findByReportId(reportId);

        if (report.isPresentation()) {
            //TODO: 기사 저장 완료되면 수정
            Article article = getArticle();
            return ReportPresentationResponse.of(
                    interviewResult,
                    article
            );
        }

        return ReportResponse.of(interviewResult);
    }

    @Transactional
    public ReportCreateResponse createReport(ReportCreateServiceRequest request) {
        InterviewInfos interviewInfos = interviewRedisRepository.findBy(request.userId());

        Report report = createReportBy(request, interviewInfos);
        reportRepository.save(report);

        interviewResultRepository.saveAll(
                interviewInfos.generateInterviewResults(chatGptResponseGenerator, report)
        );

        return ReportCreateResponse.of(report);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    private Room getRoom(String roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException("Room not found"));
    }

    private Report createReportBy(ReportCreateServiceRequest request, InterviewInfos interviewInfos) {
        Room room = getRoom(request.roomId());
        User user = getUser(request.userId());

        if (PRESENTATION.equals(room.getType())) {
            return PresentationInterviewReport.builder()
                    .room(room)
                    .user(user)
                    .totalScore(request.totalScore())
                    .pronunciationScore(interviewInfos.getTotalPronunciationScore())
                    .article(getArticle(request.articleId()))
                    .build();
        }

        return PersonalityInterviewReport.builder()
                .room(room)
                .user(user)
                .totalScore(request.totalScore())
                .pronunciationScore(interviewInfos.getTotalPronunciationScore())
                .build();
    }

    private Article getArticle(Long id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new ArticleNotFoundException("Article not found"));
    }

    private Article getArticle() {
        return Article.builder()
                .title("기사 제목")
                .content("기사 내용")
                .question("기사 질문")
                .build();
    }
}
