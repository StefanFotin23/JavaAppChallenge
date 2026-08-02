package com.example.app.ekyc.shared.profiling;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class ProfilingRegistry {

    @Getter
    public static class MethodStats {
        private final AtomicLong callCount = new AtomicLong(0);
        private final AtomicLong totalDurationNs = new AtomicLong(0);

        public void record(long durationNs) {
            callCount.incrementAndGet();
            totalDurationNs.addAndGet(durationNs);
        }

        public long getCallCountVal() {
            return callCount.get();
        }

        public double getAvgDurationMs() {
            long count = callCount.get();
            if (count == 0) return 0.0;
            return (totalDurationNs.get() / 1_000_000.0) / count;
        }
    }

    private final Map<String, MethodStats> registry = new ConcurrentHashMap<>();

    public void recordExecution(String methodName, long durationNs) {
        registry.computeIfAbsent(methodName, k -> new MethodStats()).record(durationNs);
    }

    public Map<String, MethodStats> getStats() {
        return registry;
    }

    public void reset() {
        registry.clear();
    }
}
