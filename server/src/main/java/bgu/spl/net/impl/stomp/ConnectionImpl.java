package bgu.spl.net.impl.stomp;

import java.util.concurrent.ConcurrentHashMap;
import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;

import java.io.IOException;
import java.util.LinkedList;

public class ConnectionImpl<T> implements Connections<T> {
  
  protected ConcurrentHashMap<Integer,ConnectionHandler<String>> allClientsIdToConnectionHandler = new ConcurrentHashMap<>();
  protected ConcurrentHashMap<Integer,String> allIdToPassword = new ConcurrentHashMap<>();
  protected ConcurrentHashMap<Integer,ConnectionHandler<String>> IdToConnectionHandler = new ConcurrentHashMap<>();
  protected ConcurrentHashMap<Integer,String> IdToUsername = new ConcurrentHashMap<>();
  protected ConcurrentHashMap<String,LinkedList<Integer>> channelToId = new ConcurrentHashMap<>();
  protected ConcurrentHashMap<String,LinkedList<String>> channelToSubId = new ConcurrentHashMap<>();
  protected ConcurrentHashMap<Integer,ConnectionHandler<String>> connectedToServer = new ConcurrentHashMap<>();

  private Integer connectionCounter= 0;

  public void addToServer(int id, ConnectionHandler<String> handler){
    // System.out.println("im in addToServer");
    connectedToServer.put(id, handler);
    connectionCounter = id;
  }
  
  public Integer getId(){
    // System.out.println("client " + connectionCounter + "is in getId");
    return connectionCounter;
  }

  public boolean isConnected(int id){
    // System.out.println("im in isConnected");
    if(!IdToConnectionHandler.isEmpty()){
      if(IdToConnectionHandler.containsKey(id))
        return true;
    }
    return false;
  }

  public boolean isInAll(int id){
    // System.out.println("im in isInAll");
    if(allClientsIdToConnectionHandler.containsKey(id))
      return true;
    return false;
  }

  public boolean isUsernameIsUsed(String username){
    // System.out.println("im in  isUsernameIsUsed");
    if(IdToUsername.contains(username))
      return true;
    return false;
  }

  public boolean isPasswordIsMatch(int id,String password){
    // System.out.println("im in  isPassword match");
    if(allIdToPassword.get(id)==password)
      return true;
    return false;
  }

  public void addToConnects(int id, String username){
    // System.out.println("im in addToConnects");
    IdToConnectionHandler.put(id, connectedToServer.get(id));
    IdToUsername.put(id, username);
  }

  public void addToAll(int id, String password){
    // System.out.println("im in addToAll");
    allClientsIdToConnectionHandler.put(id, connectedToServer.get(id));
    allIdToPassword.put(id, password);
  }

  public boolean isChannelExist(String channel){
    // System.out.println("im in isChannelExist");
    if(channelToId.containsKey(channel))
      return true;
    return false;
  }

  public void deleteSubId(String subId, int id){
    System.out.println("im in deleteSubId");
    for (String key : channelToSubId.keySet()) {
      if(channelToSubId.get(key).contains(subId)){
        channelToSubId.get(key).remove(subId);
      }
    }
    for (String key : channelToId.keySet()) {
      if(channelToId.get(key).contains(id)){
        channelToId.get(key).remove(id);
      }
    }
  }

  public boolean isSubscribedToChannel(String channel, int id){
    // System.out.println("im in isSubscribedToChannel");
    if(channelToId.get(channel).contains(id))
      return true;
    return false;
  }

  public void createChannel(String channel ,int id, String subId ){
    // System.out.println("im in createChannel");

    channelToId.put(channel, new LinkedList<>());
    channelToId.get(channel).add(id);
    channelToSubId.put(channel, new LinkedList<>());
    channelToSubId.get(channel).add(subId);
  }

  public void addToChannel(String channel , int id , String subId){
    // System.out.println("im in addToChannel");

    channelToId.get(channel).add(id);
    channelToSubId.get(channel).add(subId);
  }

  public boolean send(int id, T msg){
    // System.out.println("im in send regular");
    if(IdToConnectionHandler.get(id) != null)
      IdToConnectionHandler.get(id).send((String)msg);
    else
      connectedToServer.get(id).send((String) msg);
    return false;
  }

  public void send(String channel, T msg){
    System.out.println("im in send channel");
    if(channelToId.containsKey(channel)){
      for(int i : channelToId.get(channel)){
        System.out.println("message sent\n");
        IdToConnectionHandler.get(i).send((String)msg);
      }
    }
  }

  public void disconnect(int id){
    if(IdToConnectionHandler.containsKey(id)){
      ConnectionHandler<String> handler = IdToConnectionHandler.get(id);
    
      IdToConnectionHandler.remove(id);
      IdToUsername.remove(id);
      for (String key : channelToId.keySet()) {
        if(channelToId.get(key).contains(id)){
          channelToId.get(key).remove(id);
        }
      }
    
    try {
      handler.close();
    } catch (IOException ignored) {
    }
  }
    else{
      ConnectionHandler<String> handler = connectedToServer.get(id);
      try {
        handler.close();
      } catch (IOException ignored) {
      }
    }
  }
}
