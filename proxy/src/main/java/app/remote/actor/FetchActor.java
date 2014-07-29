package app.remote.actor;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import app.message.Message;
import com.ning.http.client.*;

import java.io.IOException;

import static app.message.MessageUtils.*;

/**
 * Author: kerr
 */
public class FetchActor extends UntypedActor {
    private final AsyncHttpClient asyncHttpClient;

    public FetchActor(AsyncHttpClient asyncHttpClient) {
        this.asyncHttpClient = asyncHttpClient;
    }

    public static Props props(AsyncHttpClient asyncHttpClient){
        return Props.create(FetchActor.class,asyncHttpClient);
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Message.Request) {
            Message.Request request = (Message.Request) message;
            handleRequest(request);
            return;
        }
        unhandled(message);
    }

    private void handleRequest(final Message.Request request) throws IOException {
        final ActorRef sender = getSender();
        final String uri = request.getUrl();
        final long requestId = request.getRequestId();
        System.out.println(request.getMethod() + " " + uri);
        FluentCaseInsensitiveStringsMap headers = new FluentCaseInsensitiveStringsMap();
        for (Message.Header header : request.getHeadersList()){
            headers.add(header.getKey(),header.getValue());
        }
        Message.Method method = request.getMethod();
        AsyncHttpClient.BoundRequestBuilder requestBuilder;
        switch (method){
            case OPTIONS:
                {
                    requestBuilder = asyncHttpClient.prepareOptions(uri);
                }
                break;
            case GET:
                {
                    requestBuilder = asyncHttpClient.prepareGet(uri);
                }
                break;
            case HEAD:
                {
                    requestBuilder = asyncHttpClient.prepareHead(uri);
                }
                break;
            case POST:
                {
                    requestBuilder = asyncHttpClient.preparePost(uri);
                    requestBuilder.setBody(request.getBodyPart().toByteArray());
                }
                break;
            case PUT:
                {
                    requestBuilder = asyncHttpClient.preparePut(uri);
                    requestBuilder.setBody(request.getBodyPart().toByteArray());
                }
                break;
            case PATCH:
                {
                    requestBuilder = asyncHttpClient.preparePut(uri).setMethod("PATCH");
                }
                break;
            case DELETE:
                {
                    requestBuilder = asyncHttpClient.prepareDelete(uri);
                }
                break;
            case TRACE:
                {
                    requestBuilder = asyncHttpClient.prepareGet(uri).setMethod("TRACE");
                }
                break;
            case CONNECT:
                {
                    requestBuilder = asyncHttpClient.prepareConnect(uri);
                }
                break;
            default:
                //do nothing here
                requestBuilder = null;
                break;
        }
        requestBuilder.setHeaders(headers).execute(new FetcherHandler(sender,requestId));
    }

    private class FetcherHandler implements AsyncHandler<Message.Response>{
        private final ActorRef sender;
        private final long requestId;

        private FetcherHandler(ActorRef sender, long requestId) {
            this.sender = sender;
            this.requestId = requestId;
        }

        @Override
        public void onThrowable(Throwable t) {
            t.printStackTrace();
            sender.tell(throwable(requestId,t), getSelf());
        }

        @Override
        public STATE onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception {
            sender.tell(bodyPart(requestId,bodyPart.getBodyPartBytes()), getSelf());
            return STATE.CONTINUE;
        }

        @Override
        public STATE onStatusReceived(HttpResponseStatus responseStatus) throws Exception {
            sender.tell(status(requestId,responseStatus.getProtocolText(), responseStatus.getStatusCode()), getSelf());
            return STATE.CONTINUE;
        }

        @Override
        public STATE onHeadersReceived(HttpResponseHeaders headers) throws Exception {
            sender.tell(headers(requestId,headers), getSelf());
            return STATE.CONTINUE;
        }

        @Override
        public Message.Response onCompleted() throws Exception {
            Message.Response response = complete(requestId);
            sender.tell(response, getSelf());
            return response;
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
