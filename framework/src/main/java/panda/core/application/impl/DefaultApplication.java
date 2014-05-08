package panda.core.application.impl;

import akka.actor.ActorSystem;
import panda.core.application.Application;
import panda.core.server.DefaultServerConfig;
import panda.core.server.ServerConfig;

/**
 * Author: kerr
 */
public class DefaultApplication implements Application{
    @Override
    public ServerConfig config() {
        return new DefaultServerConfig();
    }

    @Override
    public ActorSystem system() {
        return ActorSystem.create("pandaApplication");
    }
}
