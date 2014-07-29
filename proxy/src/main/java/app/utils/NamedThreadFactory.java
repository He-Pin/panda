package app.utils;

import java.util.concurrent.ThreadFactory;

/**
 * Author: kerr
 */
public class NamedThreadFactory implements ThreadFactory {
    private final String name;

    public NamedThreadFactory(String name) {
        this.name = name;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setName(name);
        return thread;
    }
}
