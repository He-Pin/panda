package app.remote;

import akka.actor.ActorSystem;
import app.message.Message;
import app.remote.handler.RemoteProxyHandler;
import app.utils.NamedThreadFactory;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.udt.UdtChannel;
import io.netty.channel.udt.nio.NioUdtProvider;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

import java.util.concurrent.ThreadFactory;

/**
 * Author: kerr
 */
public class RemoteServer {
    public static void main(String args[]) throws InterruptedException {
        Config config = ConfigFactory.load();
        final ActorSystem actorSystem = ActorSystem.create("remote",config.getConfig("proxy.remote.akka"));
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        ThreadFactory acceptor = new NamedThreadFactory("acceptor");
        ThreadFactory connector = new NamedThreadFactory("local");
        int coreNum = Runtime.getRuntime().availableProcessors();
        final RemoteProxyHandler remoteProxyHandler = new RemoteProxyHandler(actorSystem);
        serverBootstrap.group(new NioEventLoopGroup(coreNum,acceptor, NioUdtProvider.BYTE_PROVIDER),
                              new NioEventLoopGroup(coreNum,connector,NioUdtProvider.BYTE_PROVIDER))
                        .channelFactory(NioUdtProvider.BYTE_ACCEPTOR)
                        .childHandler(new ChannelInitializer<UdtChannel>() {
                            @Override
                            protected void initChannel(UdtChannel ch) throws Exception {
                                ChannelPipeline channelPipeline = ch.pipeline();
                                channelPipeline.addLast("frame-encoder",new ProtobufVarint32LengthFieldPrepender());
                                channelPipeline.addLast("frame-decoder",new ProtobufVarint32FrameDecoder());
                                channelPipeline.addLast("protobuf-encoder",new ProtobufEncoder());
                                channelPipeline.addLast("protobuf-decoder",new ProtobufDecoder(Message.Request.getDefaultInstance()));
                                channelPipeline.addLast("proxy-handler",remoteProxyHandler);

                            }
                        });

        serverBootstrap.bind(config.getInt("proxy.remote.port")).sync().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                System.out.println("remoteProxy server started at :"+future.channel());
            }
        });
    }
}
