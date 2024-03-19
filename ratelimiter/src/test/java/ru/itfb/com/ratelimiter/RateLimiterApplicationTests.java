package ru.itfb.com.ratelimiter;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.ResourceAccessException;

import java.net.URI;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
@Log4j2
class RateLimiterApplicationTests {
    @Autowired
    private TestRestTemplate testRestTemplate;

    static ExecutorService smallExecutorService = Executors.newFixedThreadPool(120);

    static ExecutorService largeExecutorService = Executors.newFixedThreadPool(300);

    @Test
    void getSecondResourceManyTimesWithShortIntervalExpectConnectionException() {
        CopyOnWriteArrayList<Integer> statusList = new CopyOnWriteArrayList<>();
        IntStream.range(0, 300).forEach(i -> largeExecutorService.execute(
                () -> statusList.add(performRequest("/ratelimiter/resource/second", i))
        ));
        await().atMost(1, TimeUnit.MINUTES).until(() -> statusList.size() == 300);
        assertThat(statusList.stream().filter(i -> i == 400).count()).isPositive();
    }

    @Test
    void getFirstResourceMoreOftenThanMaxAttemptsForPeriodExpectTooManyRequestsErrorForRedundantRequests() {
        CopyOnWriteArrayList<Integer> statusList = new CopyOnWriteArrayList<>();
        IntStream.range(0, 20).forEach(i -> smallExecutorService.execute(
                () -> statusList.add(performRequest("/ratelimiter/resource/first", i))
        ));
        await().atMost(1, TimeUnit.MINUTES).until(() -> statusList.size() == 20);
        assertThat(statusList.stream().filter(i -> i == 200).count()).isEqualTo(5);
        assertThat(statusList.stream().filter(i -> i == 429).count()).isEqualTo(15);
    }

    private int performRequest(String uri, int requestNumber) {
        try {
            ResponseEntity<String> mvcResult = testRestTemplate.getForEntity(URI.create(uri), String.class);
            return mvcResult.getStatusCode().value();
        } catch (ResourceAccessException e) {
            log.error("Connection refused: request number {} ", requestNumber);
            return 400;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
