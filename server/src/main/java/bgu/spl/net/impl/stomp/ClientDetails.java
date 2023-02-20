package bgu.spl.net.impl.stomp;

import java.util.concurrent.ConcurrentHashMap;
import bgu.spl.net.srv.ConnectionHandler;

public class ClientDetails{
  protected int id;
  protected String username;
  protected String password;
  protected ConnectionHandler<String> handler;
  protected boolean isConnected;
  protected ConcurrentHashMap<String,String> channelToId = new ConcurrentHashMap<>();
  protected ConcurrentHashMap<String,String> idToChannel = new ConcurrentHashMap<>();

  public ClientDetails(int id, String username , String password, ConnectionHandler<String> handler){
    this.id = id;
    this.username = username;
    this.password = password;
    this.handler = handler;
    this.isConnected = false;
    this.channelToId = new ConcurrentHashMap<>();
    this.idToChannel = new ConcurrentHashMap<>();
  }
}