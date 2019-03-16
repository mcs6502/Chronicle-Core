package net.openhft.chronicle.core.threads;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class JitterSamplerTest {

    @Test
    public void takeSnapshot() throws InterruptedException {
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(1);

        Thread t = new Thread(() -> {
            JitterSampler.atStage("started");
            startLatch.countDown();
            JitterSampler.sleepSilently(60);
            await(finishLatch);
            JitterSampler.atStage("finishing");
            JitterSampler.sleepSilently(60);
            JitterSampler.finished();
        });
        t.start();

        // wait for JitterSampler.desc to be primed as code below depends on it
        await(startLatch);

        for (int i = 0; i < 10; i++) {
            JitterSampler.sleepSilently(10);
            String s = JitterSampler.takeSnapshot(10_000_000);
            System.out.println(s);
            if ("finishing".equals(JitterSampler.desc)) {
                if (s != null && s.contains("finish"))
                    break;
            } else {
                assertTrue("started".equals(JitterSampler.desc));
            }
            finishLatch.countDown(); // release if not already released
        }

        t.join();
        String s = JitterSampler.takeSnapshot(10_000_000);
        assertNull(s);
    }

    private void await(CountDownLatch latch) {
        boolean ok;
        try {
            ok = latch.await(5, SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        assertTrue(ok);
    }
}