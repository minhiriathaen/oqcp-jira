package hu.minhiriathaen.oqcp;

import java.util.concurrent.Executor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * This configuration will be active as long as profile "non-async" is not (!) active.
 *
 * @see <a
 *     href="https://docs.oracle.com/javase/6/docs/api/java/util/concurrent/ThreadPoolExecutor.html">ThreadPoolExecutor
 *     class</a>
 * @see <a
 *     href="https://stackoverflow.com/questions/42438862/junit-testing-a-spring-async-void-service-method">JUnit-testing
 *     a Spring @Async void service method</a>
 */
@Slf4j
@EnableAsync
@Configuration
public class AsyncConfig implements AsyncConfigurer {

  @Value("${async.corePoolSize:7}")
  private transient int corePoolSize;

  @Value("${async.queueCapacity:11}")
  private transient int queueCapacity;

  @Value("${async:true}")
  private transient boolean asyncEnabled;

  @Override
  public Executor getAsyncExecutor() {
    if (!asyncEnabled) {
      log.info("Async is disabled");
      return new SyncTaskExecutor();
    }

    log.info("Setting CorePoolSize to {} and QueueCapacity to {}", corePoolSize, queueCapacity);

    final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setThreadNamePrefix("OQC-Jira-async-");
    executor.setCorePoolSize(corePoolSize);
    executor.setQueueCapacity(queueCapacity);
    executor.initialize();
    return executor;
  }
}
