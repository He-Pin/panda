package panda.core.server;

/**
 * Author: kerr
 */
public class DefaultServerConfig implements ServerConfig {
    @Override
    public String host() {
        return "0.0.0.0";
    }

    @Override
    public int port() {
        return 8000;
    }
}
