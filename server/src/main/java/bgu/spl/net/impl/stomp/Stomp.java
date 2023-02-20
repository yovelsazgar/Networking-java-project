package bgu.spl.net.impl.stomp;

import bgu.spl.net.api.StompMessagingProtocol;


public class Stomp implements StompMessagingProtocol<String> {

    private boolean shouldTerminate = false;
    private int id;
    private ConnectionImpl<String> connection;


    @Override
    public void start(int connectionId, ConnectionImpl<String> connections){
      this.connection = connections;
      this.id = connectionId;
      System.out.println("START");
    }

    public void process(String msg) {
      String[] lines = msg.split("\n", 2);
      String answer = "";
      switch(lines[0]){
        case "CONNECT":
          answer = decodeConnect(lines);
          System.out.println(answer);
          connection.send(id, answer);
          break;
        case "DISCONNECT":
          answer = decodeDisconnect(lines);
          break;
        case "SUBSCRIBE":
          answer = decodeSubscribe(lines);
          System.out.println(answer);
          connection.send(id, answer);
          break;
        case "UNSUBSCRIBE":
          answer = decodeUnSubscribe(lines);
          System.out.println("answer sending to server: " + answer);
          connection.send(id, answer);
          break;
        case "SEND":
          answer = decodeSend(lines);
          if(!answer.equalsIgnoreCase("SEND"))
            connection.send(id, answer);
          break;
        default:
          answer = "ERROR";
      }
    }

    //******** CASES ***********/

    public String caseConnect(String version,String host ,String username ,String password){
      // System.out.println(id);
      if(connection.isConnected(id)){
        return "ERROR\nAlready connected\n\n";
      }
      else if(connection.isInAll(id)){
        if(connection.isPasswordIsMatch(id, password)){
          connection.addToConnects(id, username);
        }
        else{ 
          connection.disconnect(id);
          return "ERROR\nPassword is not match\n\n";
        }
      }
      else {
        if(!version.equalsIgnoreCase("1.2")){
          connection.disconnect(id);
          return "ERROR\nVersion is not valid\n\n";
        }
        if(!host.equalsIgnoreCase("stomp.cs.bgu.ac.il")){
          connection.disconnect(id);
          return "ERROR\nHost is not valid\n\n";
        }
        if(connection.isUsernameIsUsed(username)){
          connection.disconnect(id);
          return "ERROR\nUsername already used\n\n";
        }
      }
      System.out.println("connecting");
      connection.addToAll(id, password);
      connection.addToConnects(id, username);
      return "CONNECTED\nVersion:1.2\n\n";
    }
    
    public String caseSend(String channel ,String body){
      if(!connection.isSubscribedToChannel(channel, id)){
        connection.disconnect(id);
        return "ERROR\n you are not subscribed to this channel\n\n";
      }
      else{
        body = "MESSAGE\n" + body;
        connection.send(channel, body);
      }
      return "SEND";
    }

    public String caseSubscribe(String subId,String destination, String receipt){
      if(connection.isChannelExist(destination)){
        connection.addToChannel(destination, id, subId);
      }
      else{
        connection.createChannel(destination, id, subId);
      }
      return "RECEIPT\nreceipt-id:" + receipt + "\n\n";

    }

    public String caseUnsubscribe(String subId, String receipt){
      connection.deleteSubId(subId,id);
      return "RECEIPT\nreceipt-id:" + receipt + "\n\n";
    }

    public String caseDisconnect(String receipt){
      connection.disconnect(id);
      return "RECEIPT\nreceipt-id:" + receipt + "\n\n";
    }

    public String splitString(String [] lines, String header){
      for(int i = 0 ; i < lines.length; i ++){
        if(lines[i].contains(header)){
          return (lines[i].substring(header.length()));
        }
      }
      return "ERROR";
    }

    
    //******** DECODE ***********/
    public String decodeConnect (String [] lines){
      String[] headers = lines[1].split("\n");
      String version , host, username , password ;
      version = splitString(headers, "accept-version:");
      host = splitString(headers, "host:");
      username = splitString(headers, "login:");
      password = splitString(headers, "passcode:");
      return caseConnect(version ,host, username, password);
    }

    public String decodeSend (String [] lines){
      String[] headers = lines[1].split("\n", 3);
      String channel , body;
      channel = splitString(headers, "destination:/");
      body = lines[1];
      return caseSend(channel, body);
    }

    public String decodeSubscribe (String [] lines){
      String[] headers = lines[1].split("\n");
      String subId , destination , receipt;
      subId = splitString(headers, "id:");
      destination = splitString(headers, "destination:/");
      receipt = splitString(headers, "receipt:");
      return caseSubscribe(subId, destination,receipt);
    }

    public String decodeUnSubscribe (String [] lines){
      String[] headers = lines[1].split("\n");
      String id, receipt;
      id = splitString(headers, "id:");
      receipt = splitString(headers, "receipt:");
      return caseUnsubscribe(id, receipt);
    }

    public String decodeDisconnect (String [] lines){
      String[] headers = lines[1].split("\n");
      String receipt;
      receipt = splitString(headers, "receipt:");
      return caseDisconnect(receipt);
    }
    
    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }
}

