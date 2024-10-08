package com.ssafy.ssafyro.domain.interview;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class InterviewRedisRepository {

    private static final String INTERVIEW_PREFIX = "interview:";

    private final RedisTemplate<String, InterviewRedis> redisTemplate;

    public Long save(InterviewRedis interview) {
        String key = INTERVIEW_PREFIX + interview.getUserId();

        redisTemplate.opsForList().rightPush(key, interview);
        redisTemplate.expire(key, 600, TimeUnit.SECONDS);

        return interview.getUserId();
    }

    public List<InterviewRedis> findByUserId(Long userId) {
        String key = INTERVIEW_PREFIX + userId;

        return redisTemplate.opsForList().range(key, 0, -1);
    }

    public InterviewInfos findBy(Long userId) {
        String key = INTERVIEW_PREFIX + userId;

        return new InterviewInfos(redisTemplate.opsForList().range(key, 0, -1));
    }

    public Long scoreInterview(Long userId, int evaluationScore) {
        String key = INTERVIEW_PREFIX + userId;

        InterviewRedis interviewRedis = redisTemplate.opsForList().rightPop(key);

        interviewRedis.addScore(evaluationScore);

        redisTemplate.opsForList().rightPush(key, interviewRedis);

        return userId;
    }

    public void delete(Long userId) {
        String key = INTERVIEW_PREFIX + userId;
        redisTemplate.delete(key);
    }

    public void deleteAll() {
        Set<String> keys = redisTemplate.keys(INTERVIEW_PREFIX + "*");
        redisTemplate.delete(keys);
    }
}
