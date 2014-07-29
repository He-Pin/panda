package app.local.actor;

import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.japi.Procedure;
import app.local.EventBus;
import app.message.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.io.IOException;

import static app.message.Message.Request;
import static app.message.Message.Response;

/**
 * Author: kerr
 */
public class LocalActor extends UntypedActor{
    private final ChannelHandlerContext ctx;
    private final EventBus eventBus;

    public LocalActor(ChannelHandlerContext ctx, EventBus eventBus) {
        this.ctx = ctx;
        this.eventBus = eventBus;
    }

    public static Props props(ChannelHandlerContext ctx, EventBus eventBus){
        return Props.create(LocalActor.class,ctx,eventBus);
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Request) {
            Request request = (Request) message;
            handleRequest(request);
            return;
        }
        if (message instanceof Response) {
            Response response = (Response) message;
            handleResponse(response);
            return;
        }
        unhandled(message);
    }

    private void handleRequest(Request request) throws IOException {

        final String uri = request.getUrl();
        System.out.println(request.getMethod() + " " + uri);
        //publish to eventBus
        eventBus.subscribe(request,getSelf());
    }

    private void handleResponse(Response response) {
        Message.ResponseType responseType = response.getType();
        switch (responseType){
            case THROWABLE:
                {
                    System.out.println("Error :"+response.getCause());
                    ctx.close();
                }
                break;
            case STATUS:
                {
                    getContext().become(new WaitComplete(response));
                }
                break;
            default:
                {
                    System.out.println("Default");
                }
                break;
        }
    }

    private class WaitComplete implements Procedure<Object>{
        private final DefaultFullHttpResponse uncompleteResponse;

        public WaitComplete(Response status) {
            uncompleteResponse = new DefaultFullHttpResponse(
                    HttpVersion.valueOf(status.getProtocolText()),
                    HttpResponseStatus.valueOf(status.getStatusCode())
            );
        }

        @Override
        public void apply(Object message) throws Exception {
            if (message instanceof Response) {
                Response response = (Response) message;
                Message.ResponseType responseType = response.getType();
                switch (responseType){
                    case THROWABLE:
                        {
                            System.out.println("Error :" + response.getCause());
                            ctx.close();
                        }
                    break;
                    case HEADERS:
                        {
                            //System.out.println("Headers");
                            DefaultHttpHeaders headers = new DefaultHttpHeaders();
                            for (Message.Header header : response.getHeadersList()){
                                headers.add(header.getKey(), header.getValue());
                            }
                            uncompleteResponse.headers().add(headers);
                        }
                    break;
                    case BODYPART:
                        {
                            //System.out.println("BodyPart");
                            uncompleteResponse.content().writeBytes(response.getBodyPart().asReadOnlyByteBuffer());
                        }
                    break;
                    case COMPLETE:
                    {
                        //System.out.println("Complete");
                        ctx.write(uncompleteResponse);
                        ctx.flush();
                        getContext().unbecome();
                    }
                    break;
                    default:
                    {
                        System.out.println("Default");
                    }
                    break;
                }
                return;
            }
            unhandled(message);
        }
    }

    @Override
    public void postStop() throws Exception {
        System.out.println("actor stop :"+getSelf());
        super.postStop();
    }

    @Override
    public void preStart() throws Exception {
        System.out.println("actor start :"+getSelf());
        super.preStart();
    }
}
