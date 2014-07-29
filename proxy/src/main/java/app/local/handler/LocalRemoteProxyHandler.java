package app.local.handler;

import app.local.EventBus;
import app.message.Message;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Author: kerr
 */
@ChannelHandler.Sharable
public class LocalRemoteProxyHandler extends SimpleChannelInboundHandler<Message.Response> {
    private final EventBus eventBus;

    public LocalRemoteProxyHandler(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message.Response msg) throws Exception {
        //get the response from the remote peer,forward to actor
        eventBus.publish(msg);
    }
}
