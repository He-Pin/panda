package panda.core.server;

import akka.actor.ActorSystem;
import panda.core.application.Application;

/**
 * Author: kerr
 */
public interface Server {
    ActorSystem system();
    void preStart(ServerConfig config);
    void bootUp();
    void postStart(Application application);
    void preStop(Application application);
    void shutDown();
    void postStop();
}
