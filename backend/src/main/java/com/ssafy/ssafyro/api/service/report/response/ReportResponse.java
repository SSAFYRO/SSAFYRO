package com.ssafy.ssafyro.api.service.report.response;

import com.ssafy.ssafyro.api.service.report.Expression;
import com.ssafy.ssafyro.domain.interviewresult.InterviewResult;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ReportResponse {

    private final int qnaCount;
    private final List<ReportDetailInfo> reportDetails;

    public static ReportResponse of(List<InterviewResult> reportDetails) {
        return new ReportResponse(
                reportDetails.size(),
                reportDetails.stream()
                        .map(ReportDetailInfo::new)
                        .toList()
        );
    }

    public record ReportDetailInfo(String question,
                                   String answer,
                                   String feedback,
                                   int pronunciationScore,
                                   Map<Expression, Double> expression) {

        private ReportDetailInfo(InterviewResult interviewResult) {
            this(
                    interviewResult.getQuestion(),
                    interviewResult.getAnswer(),
                    interviewResult.getFeedback(),
                    interviewResult.getPronunciationScore(),
                    interviewResult.getTop3Expression()
            );
        }

    }
}
