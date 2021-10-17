package local.tux;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.concurrent.atomic.AtomicInteger;

@Data
public class Stat {
    private final AtomicInteger bytesOut = new AtomicInteger();
    private final AtomicInteger holding = new AtomicInteger();
    private final AtomicInteger currentBytesReceived = new AtomicInteger();


    public void incrementReceived(int bytes) {
        currentBytesReceived.getAndAdd(bytes);
    }

    public void incrementSent(int bytes) {
        bytesOut.getAndAdd(bytes);
    }

    public void incrementBytesInBuffers(int bytes) {
        holding.getAndAdd(bytes);
    }

    public InstantStat getStateSinceLastCalled() {
        return InstantStat.builder()
            .bytesReceived(currentBytesReceived.getAndSet(0))
            .bytesSent((bytesOut.getAndSet(0)))
            .bytesInBuffer(holding.getAndSet(0))
            .build();
    }


    @Getter
    @AllArgsConstructor
    @Builder
    public static class InstantStat {
        private int bytesSent;
        private int bytesReceived;
        private int bytesInBuffer;

        public int delta() {
            return bytesReceived - bytesSent;
        }
    }
}
