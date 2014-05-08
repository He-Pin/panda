package panda.core.netty;

import akka.actor.ActorSystem;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import panda.core.application.Application;
import panda.core.logger.Logger;
import panda.core.server.Server;
import panda.core.server.ServerConfig;

/**
 * Author: kerr
 */
public class NettyServer implements Server{
    private final Application application;
    private final String host;
    private final int port;

    public NettyServer(Application application) {
        this.application = application;
        this.host = application.config().host();
        this.port = application.config().port();
    }

    @Override
    public ActorSystem system() {
        return ActorSystem.create("pandaSystem");
    }

    @Override
    public void preStart(ServerConfig config) {

    }

    @Override
    public void bootUp() {
        preStart(application.config());
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.option(ChannelOption.SO_BACKLOG,1024)
                .childOption(ChannelOption.TCP_NODELAY,true);
        final EventLoopGroup bossGroup = new NioEventLoopGroup();
        final EventLoopGroup childGroup = new NioEventLoopGroup();
        bootstrap.group(bossGroup,
                        childGroup)
                 .channel(NioServerSocketChannel.class)
                 .childHandler(new NettyChannelInitializer());
        try {
            final Channel serverChannel = bootstrap.bind(host, port).sync().channel();
            postStart(application);
            Logger.trace("server started at :"+serverChannel.localAddress());
            serverChannel.closeFuture().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    preStop(application);
                    application.system().shutdown();
                    bossGroup.shutdownGracefully();
                    childGroup.shutdownGracefully();
                    postStop();
                    Logger.trace("server stop cause exception at :"+serverChannel.localAddress());
                }
            });

        } catch (InterruptedException e) {
            preStop(application);
            application.system().shutdown();
            bossGroup.shutdownGracefully();
            childGroup.shutdownGracefully();
            postStop();
            Logger.trace("server stop cause exception :"+e.getCause());
        }

    }

    @Override
    public void postStart(Application application) {

    }

    @Override
    public void preStop(Application application) {
        Logger.trace("pre stop netty server :"+application);
    }

    @Override
    public void shutDown() {
        Logger.trace("shutdown netty server ......");
    }

    @Override
    public void postStop() {
        Logger.trace("post stop netty server");
    }
}
