package app.remote.actor;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import app.message.Message;
import com.ning.http.client.AsyncHttpClient;
import io.netty.channel.ChannelHandlerContext;

/**
 * Author: kerr
 */
public class RemoteActor extends UntypedActor{
    private final ChannelHandlerContext ctx;
    private final AsyncHttpClient asyncHttpClient;

    public RemoteActor(ChannelHandlerContext ctx, AsyncHttpClient asyncHttpClient) {
        this.ctx = ctx;
        this.asyncHttpClient = asyncHttpClient;
    }

    public static Props props(ChannelHandlerContext ctx, AsyncHttpClient asyncHttpClient){
        return Props.create(RemoteActor.class,ctx,asyncHttpClient);
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Message.Request) {
            Message.Request request = (Message.Request) message;
            handleRequest(request);
            return;
        }
        if (message instanceof Message.Response) {
            Message.Response response = (Message.Response) message;
            handleResponse(response);
            return;
        }
        unhandled(message);
    }

    private void handleResponse(Message.Response response) {
        //get the response and write to the channel
        ctx.write(response);
        ctx.flush();
    }

    private void handleRequest(Message.Request request) {
        ActorRef fetcher = getContext().actorOf(FetchActor.props(asyncHttpClient));
        fetcher.tell(request,getSelf());
    }
}
