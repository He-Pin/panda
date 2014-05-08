package panda.core.application;

import akka.actor.ActorSystem;
import panda.core.server.ServerConfig;

/**
 * Author: kerr
 */
public interface Application {
    ServerConfig config();
    ActorSystem system();
}
