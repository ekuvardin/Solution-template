package httpServer;


import jdk.internal.vm.annotation.Contended;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.execchain.RequestAbortedException;
import org.openjdk.jmh.annotations.*;
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
public class HttpTestBenchmarks {
    private static final int putCount = 10;
    private static final int getCount = 10;
    private static final int threadCount = putCount + getCount;
    private static final int step = 100000;

    private HttpClientExample HttpClientExample;
    private static AtomicInteger puts = new AtomicInteger(putCount);
    private static AtomicInteger gets = new AtomicInteger(getCount);
    private static Wrapper[] array;

    //@Contended not work, surprising
    @Contended
    public static class Wrapper {
        volatile long long1, long2, long3, long4, long5, long6, long7, long8;
        volatile long lastInserted;
        volatile long long11, long12, long13, long14, long15, long16, long17, long18;

        Wrapper(long lastInserted) {
            this.lastInserted = lastInserted;
        }

        void fakeTouch(int fake) {
            long1 = long2 = long3 = long4 = long5 = long6 = long7 = long8 = fake;
            long11 = long12 = long13 = long14 = long15 = long16 = long17 = long18 = fake;
        }

        long fakeGet() {
            return long1 + long2 + long3 + long4 + long5 + long6 + long7 + long8 +
                    long11 + long12 + long13 + long14 + long15 + long16 + long17 + long18;
        }
    }

    @Setup
    public void setup() {
        HttpClientExample = new HttpClientExample();
        array = new Wrapper[putCount];
        for (int i = 0; i < array.length; i++) {
            array[i] = new Wrapper(step * i);
            array[i].fakeTouch(1);
        }
    }

    @TearDown
    public void shutdown() {
        for (int i = 0; i < array.length; i++) {
            array[i].fakeGet();
        }
    }

    @Benchmark
    @Group("HttpBenchmarks")
    @GroupThreads(putCount)
    public HttpEntity post(PostThreadScope postThreadScope) throws Exception {
        try (CloseableHttpResponse closable = HttpClientExample.sendPost(postThreadScope.getMap())) {
            return closable.getEntity();
        } catch (RequestAbortedException e) {
            throw new InterruptedException(e.getMessage());
        }
    }

    @Benchmark
    @Group("HttpBenchmarks")
    @GroupThreads(getCount)
    public HttpEntity get(GetThreadScope getThreadScope) throws Exception {
        try (CloseableHttpResponse closable = HttpClientExample.sendGet(getThreadScope.getUID())) {
            return closable.getEntity();
        } catch (RequestAbortedException e) {
            throw new InterruptedException(e.getMessage());
        }
    }

    @State(Scope.Thread)
    public static class GetThreadScope {
        private final int threadNum;
        private final Wrapper wrapper;
        private final int startIndex;
        private long localUid = 0l;

        public GetThreadScope() {
            threadNum = gets.decrementAndGet();
            wrapper = array[threadNum];
            startIndex = threadNum * step;
            System.out.println("get " + startIndex);
        }

        public long getUID() {
            if (startIndex + localUid > wrapper.lastInserted) {
                localUid = 0l;
            }
            return startIndex + localUid++;
        }
    }

    @State(Scope.Thread)
    public static class PostThreadScope {
        private Map<String, Object> map = new HashMap<>();
        private long uid;
        private final int threadNum;
        private final Wrapper wrapper;

        public PostThreadScope() {
            // Insert your fields
            map.put("smth", "smth");

            threadNum = puts.decrementAndGet();
            wrapper = array[threadNum];
            uid = threadNum * step;
            System.out.println("put " + uid);
        }

        public Map<String, Object> getMap() {
            map.put("UID", uid);
            wrapper.lastInserted = uid;
            uid++;

            return map;
        }
    }


    public static void main(String[] args) {
        Options opt = new OptionsBuilder()
                .include(HttpTestBenchmarks.class.getSimpleName())
                .warmupIterations(20)
                .measurementIterations(90)
                //.operationsPerInvocation(100000)
                .forks(1)
                .threads(threadCount)
                .timeout(TimeValue.seconds(6))
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

