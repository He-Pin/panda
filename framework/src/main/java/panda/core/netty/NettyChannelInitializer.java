package panda.core.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;

/**
 * Author: kerr
 */
public class NettyChannelInitializer extends ChannelInitializer<SocketChannel>{
    private final NettyChannelHandler nettyChannelHandler =
            new NettyChannelHandler();
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast("codec-http", new HttpServerCodec());
        pipeline.addLast("handler",nettyChannelHandler);
    }
}
