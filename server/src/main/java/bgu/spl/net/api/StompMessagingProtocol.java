package bgu.spl.net.api;

import bgu.spl.net.impl.stomp.ConnectionImpl;
import bgu.spl.net.srv.ConnectionHandler;

public interface StompMessagingProtocol<T>  {
	/**
	 * Used to initiate the current client protocol with it's personal connection ID and the connections implementation
	**/
    void start(int connectionid, ConnectionImpl<String> connections);
    
    void process(T message);
	
	/**
     * @return true if the connection should be terminated
     */
    boolean shouldTerminate();
}
