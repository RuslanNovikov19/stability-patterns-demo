package ru.itfb.com.bulkhead;

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
public class BulkheadApplicationTests {
    @Autowired
    private TestRestTemplate testRestTemplate;

    ExecutorService smallExecutorService = Executors.newFixedThreadPool(12);

    ExecutorService largeExecutorService = Executors.newFixedThreadPool(100);

    @Test
    void getThirdResourceConcurrentlyWithNumberOrThreadsOverwhelmingTomcatWorkersCountExpectServerError() {
        CopyOnWriteArrayList<Integer> statusList = new CopyOnWriteArrayList<>();
        IntStream.range(0, 100).forEach(i -> largeExecutorService.execute(
                () -> statusList.add(performRequest("/bulkhead/resource/third", i))
        ));
        await().atMost(3
                , TimeUnit.MINUTES).until(() -> statusList.size() == 100);
        assertThat(statusList.stream().filter(i -> i == 400).count()).isPositive();
    }

    @Test
    void getFirstAndSecondBulkheadResourcesLessTimesThanLimitedByBulkheadsExpectOk() {
        CopyOnWriteArrayList<Integer> firstResourceResponseStatuses = new CopyOnWriteArrayList<>();
        CopyOnWriteArrayList<Integer> secondResourceResponseStatuses = new CopyOnWriteArrayList<>();
        IntStream.range(0, 6).forEach(i -> {
                    smallExecutorService.execute(() ->
                            firstResourceResponseStatuses.add(performRequest("/bulkhead/resource/first", i)
                            )
                    );
                    largeExecutorService.execute(() ->
                            secondResourceResponseStatuses.add(performRequest("/bulkhead/resource/second", i)
                            )
                    );
                }
        );
        await().atMost(1, TimeUnit.MINUTES).until(() ->
                secondResourceResponseStatuses.size() == 6 && firstResourceResponseStatuses.size() == 6
        );
        assertThat(secondResourceResponseStatuses.stream().filter(i -> i == 200).count()).isEqualTo(6);
        assertThat(firstResourceResponseStatuses.stream().filter(i -> i == 200).count()).isEqualTo(6);
    }

    @Test
    void getFirstBulkheadResourceMoreTimesThanLimitedAndSecondResourceLessTimesThanLimitedExpectOkForSecondResourceCalls() throws InterruptedException {
        CopyOnWriteArrayList<Integer> secondResourceResponseStatuses = new CopyOnWriteArrayList<>();
        CopyOnWriteArrayList<Integer> firstResourceResponseStatuses = new CopyOnWriteArrayList<>();
        IntStream.range(0, 15).forEach(i -> smallExecutorService.execute(
                () -> firstResourceResponseStatuses.add(performRequest("/bulkhead/resource/first", i))
        ));

        Thread.sleep(3000L);

        IntStream.range(0, 8).forEach(i ->

                    largeExecutorService.execute(() ->
                            secondResourceResponseStatuses.add(performRequest("/bulkhead/resource/second", i)
                            )
                    )
        );

        await().atMost(1, TimeUnit.MINUTES).until(() -> firstResourceResponseStatuses.size() == 15);
        assertThat(firstResourceResponseStatuses.stream().filter(i -> i == 200).count()).isEqualTo(8);
        assertThat(firstResourceResponseStatuses.stream().filter(i -> i == 429).count()).isEqualTo(7);
        await().atMost(1, TimeUnit.MINUTES).until(() -> secondResourceResponseStatuses.size() == 8);
        assertThat(secondResourceResponseStatuses.stream().filter(i -> i == 200).count()).isEqualTo(8);
    }


    @Test
    void getFirstAndSecondBulkheadResourcesExpectMaxSuccessfulConcurrentCallsForBothResourcesNotMoreThanLimitedByBulkheads() {
        CopyOnWriteArrayList<Integer> firstResourceResponseStatuses = new CopyOnWriteArrayList<>();
        CopyOnWriteArrayList<Integer> secondResourceResponseStatuses = new CopyOnWriteArrayList<>();
        IntStream.range(0, 10).forEach(i -> {
                    smallExecutorService.execute(() ->
                            firstResourceResponseStatuses.add(performRequest("/bulkhead/resource/first", i)
                            )
                    );
                    largeExecutorService.execute(() ->
                            secondResourceResponseStatuses.add(performRequest("/bulkhead/resource/second", i)
                            )
                    );
                }
        );
        await().atMost(1, TimeUnit.MINUTES).until(() ->
                secondResourceResponseStatuses.size() == 10
                        &&
                firstResourceResponseStatuses.size() == 10
        );
        assertThat(secondResourceResponseStatuses.stream().filter(i -> i == 200).count()).isEqualTo(8);
        assertThat(secondResourceResponseStatuses.stream().filter(i -> i == 429).count()).isEqualTo(2);
        assertThat(firstResourceResponseStatuses.stream().filter(i -> i == 200).count()).isEqualTo(8);
        assertThat(firstResourceResponseStatuses.stream().filter(i -> i == 429).count()).isEqualTo(2);
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
