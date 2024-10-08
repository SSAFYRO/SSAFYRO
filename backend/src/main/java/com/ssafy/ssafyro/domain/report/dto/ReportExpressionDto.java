package com.ssafy.ssafyro.domain.report.dto;

import com.querydsl.core.annotations.QueryProjection;
import com.ssafy.ssafyro.api.service.report.Expression;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReportExpressionDto {

    private double happy;
    private double disgust;
    private double sad;
    private double surprise;
    private double fear;
    private double angry;
    private double neutral;

    @QueryProjection
    public ReportExpressionDto(double happy, double disgust, double sad, double surprise, double fear, double angry,
                               double neutral) {
        this.happy = happy;
        this.disgust = disgust;
        this.sad = sad;
        this.surprise = surprise;
        this.fear = fear;
        this.angry = angry;
        this.neutral = neutral;
    }

    public Map<Expression, Double> getTop3Expression() {
        Map<Expression, Double> expressions = new HashMap<>();
        expressions.put(Expression.ANGRY, angry);
        expressions.put(Expression.FEAR, fear);
        expressions.put(Expression.DISGUST, disgust);
        expressions.put(Expression.HAPPY, happy);
        expressions.put(Expression.NEUTRAL, neutral);
        expressions.put(Expression.SAD, sad);
        expressions.put(Expression.SURPRISE, surprise);

        return expressions.entrySet()
                .stream()
                .sorted(Map.Entry.<Expression, Double>comparingByValue().reversed())
                .limit(3)
                .collect(Collectors.toMap(
                        Entry::getKey,
                        Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }
}
