package panda.core.application;

import panda.core.application.impl.DefaultApplication;
import panda.core.logger.Logger;
import panda.core.netty.NettyServer;
import panda.core.server.Server;

/**
 * Author: kerr
 */
public class Booter {
    public void boot(){
        Logger.trace("booter is booting server");
        final Application application = new DefaultApplication();
        final Server server = new NettyServer(application);
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                Logger.trace("shutdown server now");
                server.preStop(application);
                server.shutDown();
                server.postStop();
            }
        }));
        server.bootUp();
    }
}
