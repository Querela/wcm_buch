package de.uni_leipzig.comprak.books.wcmbookserver.extract.utils;

/**
 * Interface as base for delay generators.
 * Created by Erik on 28.11.2014.
 */
public interface Delayer {
    /**
     * Generates a new delay in milliseconds.
     *
     * @return long
     */
    public long nextDelay();

    /**
     * Delays execution. Uses {@link #nextDelay()} for generating the time to sleep.
     */
    public void doDelay();

    /**
     * Lets the current thread <i>millis</i> milliseconds sleep.
     *
     * @param millis long - time to sleep
     */
    public void doDelay(long millis);
}
