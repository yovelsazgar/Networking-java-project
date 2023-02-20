package bgu.spl.net.srv;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

import bgu.spl.net.impl.stomp.ClientDetails;


public interface Connections<T> {

    ConcurrentHashMap<String,LinkedList<ClientDetails>> channelSubscribe = new ConcurrentHashMap<>();
    ConcurrentHashMap<Integer,ClientDetails> connectedClientDetails = new ConcurrentHashMap<>();
    ConcurrentHashMap<String,ClientDetails> userToClientDetails = new ConcurrentHashMap<>();


    boolean send(int id, T msg);

    void send(String channel, T msg);

    void disconnect(int connectionId);
}
