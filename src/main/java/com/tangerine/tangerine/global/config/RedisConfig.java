package com.tangerine.tangerine.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * [추가] RedisConfig
 *
 * Spring Boot가 application.properties의 Redis 설정을 읽어서
 * RedisConnectionFactory를 자동으로 만들어줘요.
 * 여기서는 그 연결을 이용해서 RedisTemplate을 설정해요.
 *
 * RedisTemplate: Redis에 데이터를 읽고 쓰는 핵심 도구예요.
 * JPA의 EntityManager와 비슷한 역할이에요.
 *
 * StringRedisSerializer: Redis에 저장할 때 key와 value를
 * 문자열로 직렬화해요. 기본 설정은 바이트 배열이라
 * DBeaver처럼 Redis 관리 도구에서 값을 확인할 때
 * 알아볼 수 없는 형태로 저장돼요.
 * 문자열로 저장하면 "refresh:user@test.com" → "토큰값"처럼
 * 사람이 읽을 수 있는 형태로 저장돼요.
 */
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // key와 value 모두 문자열로 직렬화해요
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());

        return template;
    }
}
