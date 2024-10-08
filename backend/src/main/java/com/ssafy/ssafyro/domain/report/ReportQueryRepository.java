package com.ssafy.ssafyro.domain.report;

import com.ssafy.ssafyro.domain.report.dto.ReportAllScoreAverageDto;
import com.ssafy.ssafyro.domain.report.dto.ReportExpressionDto;
import com.ssafy.ssafyro.domain.report.dto.ReportScoreDto;
import com.ssafy.ssafyro.domain.report.dto.ReportUserAverageDto;
import com.ssafy.ssafyro.domain.room.RoomType;
import com.ssafy.ssafyro.domain.user.User;
import java.util.List;
import java.util.Optional;

public interface ReportQueryRepository {

    List<Report> findReports();

    int countReportsType(RoomType type, User user);

    Optional<ReportUserAverageDto> findTotalAvgBy(RoomType type, User user);

    Optional<ReportAllScoreAverageDto> findAllAvgScoreBy(RoomType type);

    List<ReportScoreDto> findScoreBy(RoomType type, User user);

    Optional<ReportExpressionDto> findAvgExpressionBy(RoomType type, User user);
}
