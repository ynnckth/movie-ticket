package com.zuehlke.movieticketservice.api.ratingservice;

import com.zuehlke.movieticketservice.api.RestClientFactory;
import com.zuehlke.movieticketservice.domain.Rating;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Service
@EnableCircuitBreaker
public class RatingServiceAdapter implements HealthIndicator {

    private final RatingServiceApi ratingServiceApi;
    private final String url;

    public RatingServiceAdapter(String url) {
        this.url = url;
        this.ratingServiceApi =
                RestClientFactory.createClientWithFallback(url, RatingServiceApi.class, getRatingServiceApiFallback());
    }

    public List<Rating> getRatings(int movieId) {
        return ratingServiceApi.getRatingsByMovieId(movieId).stream()
                .map(RatingResponse::toRating)
                .collect(Collectors.toList());
    }

    @Override
    public Health health() {
        try {
            ratingServiceApi.getHealthStatus();
            return Health.up()
                    .withDetail("Endpoint", url)
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("Endpoint", url)
                    .build();
        }
    }

    /**
     * This fallback API mock will be used if the real service fails.
     */
    private RatingServiceApi getRatingServiceApiFallback() {
        // This fallback mock will be used if the real rating-service fails.
        // The fallback service returns an empty list instead of the ratings.
        return new RatingServiceApi() {
            @Override
            public List<RatingResponse> getRatingsByMovieId(int id) {
                return Collections.emptyList();
            }
            @Override
            public void getHealthStatus() { }
        };
    }
}
