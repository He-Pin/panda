package app.local;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import app.local.handler.LocalHttpProxyHandler;
import app.local.handler.LocalRemoteProxyHandler;
import app.message.Message;
import app.utils.NamedThreadFactory;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.udt.UdtChannel;
import io.netty.channel.udt.nio.NioUdtProvider;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.compression.SnappyFramedDecoder;
import io.netty.handler.codec.compression.SnappyFramedEncoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.util.concurrent.ThreadFactory;

/**
 * Author: kerr
 */
public class LocalServer {
    public static void main(String[] args) throws InterruptedException {
        ThreadFactory threadFactory = new NamedThreadFactory("local");
        int coreNum = Runtime.getRuntime().availableProcessors();
        Config config = ConfigFactory.load();
        final ActorSystem actorSystem = ActorSystem.create("local",config.getConfig("proxy.local.akka"));

        //below is the udt proxy
        ThreadFactory connector = new DefaultThreadFactory("connector");
        Bootstrap bootstrap = new Bootstrap();
        //the event bus
        final EventBus eventBus = new EventBus(actorSystem);

        final LocalRemoteProxyHandler localRemoteProxyHandler = new LocalRemoteProxyHandler(eventBus);
        bootstrap.group(new NioEventLoopGroup(coreNum, connector, NioUdtProvider.BYTE_PROVIDER))
                .channelFactory(NioUdtProvider.BYTE_CONNECTOR)
                .handler(new ChannelInitializer<UdtChannel>() {
                    @Override
                    protected void initChannel(UdtChannel ch) throws Exception {
                        ChannelPipeline channelPipeline = ch.pipeline();
                        channelPipeline.addLast("frame-encoder", new LengthFieldPrepender(4));
                        channelPipeline.addLast("frame-decoder", new LengthFieldBasedFrameDecoder(1024*1024,0,4,0,4));
                        channelPipeline.addLast("snappy-decoder",new SnappyFramedDecoder());
                        channelPipeline.addLast("snappy-encoder",new SnappyFramedEncoder());
                        channelPipeline.addLast("protobuf-encoder", new ProtobufEncoder());
                        channelPipeline.addLast("protobuf-decoder", new ProtobufDecoder(Message.Response.getDefaultInstance()));
                        channelPipeline.addLast("local-remote-handler",localRemoteProxyHandler);
                    }
                });
        ChannelFuture channelFuture = bootstrap.connect(config.getString("proxy.remote.addr"), config.getInt("proxy.remote.port"))
                .sync();
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(final ChannelFuture future) throws Exception {
                System.out.println("connected to remote server :"+future.channel());
                final Channel channel = future.channel();
                eventBus.addListener(new EventBus.EventBusListener() {
                    @Override
                    public void onSubscribe(Message.Request request, ActorRef actorRef) {
                        //write message to remote
                        channel.write(request);
                        channel.flush();
                    }

                    @Override
                    public void onPublish(Message.Response response) {

                    }
                });
            }
        });

        final LocalHttpProxyHandler localHttpProxyHandler = new LocalHttpProxyHandler(actorSystem,eventBus);

        ServerBootstrap serverBootstrap = new ServerBootstrap()
                .group(new NioEventLoopGroup(coreNum,threadFactory),
                        new NioEventLoopGroup(coreNum,threadFactory))
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline channelPipeline = ch.pipeline();
                        channelPipeline.addLast("http-codec", new HttpServerCodec());
                        channelPipeline.addLast("agenerator",new HttpObjectAggregator(1024*1024));
                        channelPipeline.addLast("handler", localHttpProxyHandler);
                    }
                });
        serverBootstrap.bind(config.getInt("proxy.local.port")).sync()
                .addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        System.out.println("localProxy server started at :"+future.channel());
                    }
                });


    }
}
