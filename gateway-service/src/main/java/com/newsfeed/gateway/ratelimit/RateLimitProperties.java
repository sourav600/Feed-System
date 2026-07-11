package com.newsfeed.gateway.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.rate-limit")
public record RateLimitProperties(BucketSpec login, BucketSpec posts) {

    public record BucketSpec(long capacity, long refillTokens, long refillPeriodSeconds) {
    }
}
