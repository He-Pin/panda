package app.message;

import com.google.protobuf.ByteString;
import com.ning.http.client.HttpResponseHeaders;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;

import java.util.List;
import java.util.Map;

import static app.message.Message.*;

/**
 * Author: kerr
 */
public class MessageUtils {
    public static Request request(long requestId,FullHttpRequest request){
        HttpMethod httpMethod = request.getMethod();
        String uri = request.getUri();
        Request.Builder builder = Request.newBuilder();
        builder.setRequestId(requestId);
        for (Map.Entry<String, String> entry : request.headers()){
            Header header = Header.newBuilder()
                    .setKey(entry.getKey())
                    .setValue(entry.getValue())
                    .build();
            builder.addHeaders(header);
        }
        if (httpMethod.equals(HttpMethod.OPTIONS)){
            // do option
            builder.setMethod(Method.OPTIONS);
        } else if (httpMethod.equals(HttpMethod.GET)) {
            //do get
            builder.setMethod(Method.GET);
        } else if (httpMethod.equals(HttpMethod.HEAD)){
            //do head
            builder.setMethod(Method.HEAD);
        } else if (httpMethod.equals(HttpMethod.POST)){
            //do post
            builder.setMethod(Method.POST);
            // the following httpContent will forwarding to upstream
            int readable = request.content().readableBytes();
            byte[] bytes = new byte[readable];
            request.content().readBytes(bytes);
            builder.setBodyPart(ByteString.copyFrom(bytes));
        } else if (httpMethod.equals(HttpMethod.PUT)){
            //do put
            builder.setMethod(Method.PUT);
            // the following httpContent will forwarding to upstream
            int readable = request.content().readableBytes();
            byte[] bytes = new byte[readable];
            request.content().readBytes(bytes);
            builder.setBodyPart(ByteString.copyFrom(bytes));
        } else if (httpMethod.equals(HttpMethod.PATCH)){
            //do patch
            builder.setMethod(Method.PATCH);
        } else if (httpMethod.equals(HttpMethod.DELETE)){
            //do delete
            builder.setMethod(Method.DELETE);
        } else if (httpMethod.equals(HttpMethod.TRACE)){
            //do trace
            builder.setMethod(Method.TRACE);
        } else if (httpMethod.equals(HttpMethod.CONNECT)){
            //do connect
            builder.setMethod(Method.CONNECT);
        }
        return builder.setUrl(uri).build();
    }

    public static Response throwable(long requestId,Throwable throwable){
        return Response.newBuilder()
                .setRequestId(requestId)
                .setType(ResponseType.THROWABLE)
                .setCause(throwable.getMessage())
                .build();
    }

    public static Response status(long requestId,String protocolText,int statusCode){
        return Response.newBuilder()
                .setRequestId(requestId)
                .setType(ResponseType.STATUS)
                .setProtocolText(protocolText)
                .setStatusCode(statusCode)
                .build();
    }

    public static Response headers(long requestId,HttpResponseHeaders headers){
        Response.Builder builder = Response.newBuilder()
                .setRequestId(requestId)
                .setType(ResponseType.HEADERS);
        for (Map.Entry<String, List<String>> entry : headers.getHeaders()) {
            builder.addHeaders(Header.newBuilder()
                    .setKey(entry.getKey())
                    .setValue(entry.getValue().get(0)));
        }
        return builder.build();
    }

    public static Response complete(long requestId){
        return Response.newBuilder()
                .setRequestId(requestId)
                .setType(ResponseType.COMPLETE)
                .build();
    }

    public static Response bodyPart(long requestId,byte[] bytes){
        return Response.newBuilder()
                .setRequestId(requestId)
                .setType(ResponseType.BODYPART)
                .setBodyPart(ByteString.copyFrom(bytes))
                .build();
    }

}
