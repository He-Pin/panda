package app.local;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import app.message.Message;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Author: kerr
 */
public class EventBus {
    private final ConcurrentHashMap<Long,ActorRef> httpProxyBus = new ConcurrentHashMap<>();
    private final ActorSystem actorSystem;
    private final CopyOnWriteArrayList<EventBusListener> listeners;

    public EventBus(ActorSystem actorSystem) {
        this.actorSystem = actorSystem;
        this.listeners = new CopyOnWriteArrayList<>();
    }

    public void subscribe(Message.Request request,ActorRef actorRef){
        httpProxyBus.put(request.getRequestId(),actorRef);
        for (EventBusListener listener : listeners){
            listener.onSubscribe(request,actorRef);
        }
    }

    public void publish(Message.Response response){
        ActorRef actorRef = httpProxyBus.get(response.getRequestId());
        if (actorRef != null){
            actorRef.tell(response, ActorRef.noSender());
        }
        for (EventBusListener listener : listeners){
            listener.onPublish(response);
        }
        if (response.getType().equals(Message.ResponseType.COMPLETE)){
            //need remove the listener
            httpProxyBus.remove(response.getRequestId());
        }
    }

    public void addListener(final EventBusListener eventBusListener){
        listeners.add(eventBusListener);
    }

    public interface EventBusListener {
        void onSubscribe(Message.Request request,ActorRef actorRef);
        void onPublish(Message.Response response);
    }
}
