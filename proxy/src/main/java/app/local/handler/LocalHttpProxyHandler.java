package app.local.handler;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import app.local.EventBus;
import app.local.actor.LocalActor;
import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpRequest;

import java.util.concurrent.atomic.AtomicLong;

import static app.message.MessageUtils.request;

/**
 * Author: kerr
 */
@ChannelHandler.Sharable
public class LocalHttpProxyHandler extends SimpleChannelInboundHandler<FullHttpRequest>{
    private final ActorSystem actorSystem;
    private final AtomicLong requestId = new AtomicLong(0L);
    private final EventBus eventBus;

    public LocalHttpProxyHandler(ActorSystem actorSystem, EventBus eventBus) {
        this.actorSystem = actorSystem;
        this.eventBus = eventBus;
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        //here forwarding to an actor to handle it
        long id = requestId.incrementAndGet();
        final ActorRef actor = actorSystem.actorOf(LocalActor.props(ctx,eventBus),"local-"+id);
        actor.tell(request(id,msg), ActorRef.noSender());

        ctx.channel().closeFuture().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                System.out.println("channel closed :"+future.channel());
                actor.tell(PoisonPill.getInstance(), ActorRef.noSender());
            }
        });
    }
}
