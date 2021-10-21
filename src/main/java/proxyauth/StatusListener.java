package proxyauth;

/**
 * Provides a target for threads to report when they are finished, and
 * whether they were successful
 *
 * @param <A> the type of thread
 */
public interface StatusListener<A extends Thread> {
    /**
     * Notify the listener that this thread has finished
     *
     * @param source    the thread that has finished
     * @param succeeded was the action this thread was performing successful
     */
    void finished(A source, boolean succeeded);
}
