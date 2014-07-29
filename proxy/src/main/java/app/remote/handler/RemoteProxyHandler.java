package app.remote.handler;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import app.message.Message;
import app.remote.actor.RemoteActor;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import io.netty.channel.*;

import java.util.concurrent.Executors;

/**
 * Author: kerr
 */
@ChannelHandler.Sharable
public class RemoteProxyHandler extends SimpleChannelInboundHandler<Message.Request> {
    private final ActorSystem actorSystem;
    private final AsyncHttpClient asyncHttpClient;

    public RemoteProxyHandler(ActorSystem actorSystem) {
        this.actorSystem = actorSystem;
        this.asyncHttpClient = new AsyncHttpClient(
                new AsyncHttpClientConfig.Builder()
                        .setAllowPoolingConnection(true)
                        .setMaxRequestRetry(10)
                        .setFollowRedirects(true)
                        .setCompressionEnabled(true)
                        .setMaximumConnectionsPerHost(100)
                        .setMaximumConnectionsTotal(10000)
                        .setMaximumNumberOfRedirects(100)
                        .setExecutorService(Executors.newCachedThreadPool())
                        .build());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message.Request msg) throws Exception {
        //call the remote actor to handler it
        System.out.println(msg.getMethod() + " " + msg.getUrl());
        final ActorRef remoteActor = actorSystem.actorOf(RemoteActor.props(ctx,asyncHttpClient));
        remoteActor.tell(msg, ActorRef.noSender());
        ctx.channel().closeFuture().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                remoteActor.tell(PoisonPill.getInstance(),ActorRef.noSender());
            }
        });
    }
}
