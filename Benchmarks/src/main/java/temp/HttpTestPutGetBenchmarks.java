package temp;


import jdk.internal.vm.annotation.Contended;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.execchain.RequestAbortedException;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@State(Scope.Group)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 4)
@Measurement(iterations = 5)
@Fork(1)
@Timeout(time = 60)
@Threads(20)
public class HttpTestPutGetBenchmarks {
    private static final int threadCount = 20;
    private static final int step = 100000;

    private HttpClientExample HttpClientExample;
    private static AtomicInteger puts = new AtomicInteger(20);

    @Setup
    public void setup() {
        HttpClientExample = new HttpClientExample();
    }

    @Benchmark
    @Group("HttpBenchmarks")
    @GroupThreads(threadCount)
    public HttpEntity post(PostThreadScope postThreadScope, Blackhole bh) throws Exception {
        try (CloseableHttpResponse closable = HttpClientExample.sendPost(postThreadScope.getMap())) {
            bh.consume(closable.getEntity());
        } catch (RequestAbortedException e) {
            throw new InterruptedException(e.getMessage());
        }

        try (CloseableHttpResponse closable = HttpClientExample.sendGet(postThreadScope.uid - 1)) {
            return closable.getEntity();
        } catch (RequestAbortedException e) {
            throw new InterruptedException(e.getMessage());
        }
    }

    @State(Scope.Thread)
    public static class PostThreadScope {
        private Map<String, Object> map = new HashMap<>();
        private long uid;
        private final int threadNum;

        public PostThreadScope() {
            map.put("sapNumber", "2");
            map.put("title", "3");
            map.put("firstName", "4");
            map.put("lastName", "5");
            map.put("functionCode", "9");
            map.put("function", "8");
            map.put("department", "9");
            map.put("email", "test@test.ru");
            map.put("emailMarketing", "true");
            map.put("authorizedStatus", "true");
            map.put("birthDate", "2000-01-27T00:00:00");
            map.put("lastExport", "2000-01-27T00:00:00");
            map.put("creationDate", "2000-01-27T00:00:00");

            threadNum = puts.decrementAndGet();
            uid = threadNum * step;
            System.out.println("put " + uid);
        }

        public Map<String, Object> getMap() {
            map.put("UID", uid);
            uid++;

            return map;
        }
    }


    public static void main(String[] args) {
        Options opt = new OptionsBuilder()
                .include(HttpTestPutGetBenchmarks.class.getSimpleName())
                .warmupIterations(20)
                .measurementIterations(120)
                //.operationsPerInvocation(100000)
                .forks(1)
                .threads(threadCount)
                .timeout(TimeValue.seconds(3))
                //.shouldDoGC(true)
                .syncIterations(true)
                .jvmArgs("-server")
                .build();
        try {
            new Runner(opt).run();
        } catch (RunnerException e) {
            e.printStackTrace();
        }
    }

}

