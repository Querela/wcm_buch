package de.uni_leipzig.wcmprak.books.wcmbookserver.extract.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * Simple class for generating random delays in a distinct range. It will check how much time since its last call has
 * passed and adjust the delay accordingly. (e. g. that means the delay can be shorter are non-existant)
 * Created by Erik on 28.11.2014.
 */
public class RandomRemainingDelayGenerator implements Delayer {
    private final static Logger log = LoggerFactory.getLogger(RandomRemainingDelayGenerator.class);
    private long minDelay = 0L;
    private long maxDelay = 0L;
    private long diffDelay = 0L;
    private long timeLastDelay = 0L;
    private Random random;

    /**
     * New random delay generator with initialization parameters.
     *
     * @param min long - minimum time to sleep, for each delay
     * @param max long - maximum time to sleep per delay
     */
    public RandomRemainingDelayGenerator(long min, long max) {
        this.minDelay = (min < 0) ? 0 : min;
        this.maxDelay = (max < this.minDelay) ? this.minDelay : max;
        this.diffDelay = this.maxDelay - this.minDelay;
        this.timeLastDelay = System.currentTimeMillis();
        this.random = new Random();
    }

    @Override
    public long nextDelay() {
        return minDelay + (long) (random.nextFloat() * diffDelay);
    }

    @Override
    public void doDelay() {
        long newDelay = nextDelay() + (timeLastDelay - System.currentTimeMillis());
        // Only do delay if not enough time has passed
        if (newDelay > 0L) {
            doDelay(newDelay);
        } // if
        timeLastDelay = System.currentTimeMillis();
    }

    @Override
    public void doDelay(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            log.error(e.getLocalizedMessage());
        } // try-catch
    }
}
