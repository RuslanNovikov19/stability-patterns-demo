package ru.itfb.com.ratelimiter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@EnableAutoConfiguration
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RateLimiterApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void ratelimiterTest() {
        CopyOnWriteArrayList<Integer> statusList = new CopyOnWriteArrayList<>();
        IntStream.range(0, 20).forEach(i -> CompletableFuture.runAsync(
                () -> statusList.add(performRequest())
        ));
        await().atMost(1, TimeUnit.MINUTES).until(() -> statusList.size() == 20);
        assertThat(statusList.stream().filter(i -> i == 200).count()).isEqualTo(5);
        assertThat(statusList.stream().filter(i -> i == 429).count()).isEqualTo(15);
    }

    private int performRequest() {
        try {
            MvcResult mvcResult = mockMvc.perform(get("/ratelimiter/resource"))
                    .andDo(print())
                    .andReturn();
            return mvcResult.getResponse().getStatus();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
