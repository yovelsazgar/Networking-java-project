#include <stdlib.h>
#include <thread>
#include "../include/ConnectionHandler.h"
#include "../include/StompProtocol.h"
#include "../include/ReadFromServer.h"
#include <vector>




int main(int argc, char *argv[]) {

// not enough arguments were sent to the server
 if (argc < 3) {
        std::cerr << "not enough arguments" << std::endl;
        return -1;
    }

  std::string host = argv[1];
  short port = atoi(argv[2]);


// creating a connection handler and attempt to connect
  ConnectionHandler connectionHandler(host, port);
  if (!connectionHandler.connect()) {
    std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
    return 1;
  }

  StompProtocol protocol;
  ReadFromServer task1(connectionHandler, protocol);

  std::thread serverSocketThread(&ReadFromServer::run,&task1);

  while (!protocol.shouldTerminate()) {
    const short bufsize = 1024;
    char buf[bufsize];
    std::cin.getline(buf, bufsize);
		std::string line(buf);
		std::vector<string> frame = protocol.handleRequest(line);
    for(unsigned i = 0; i < frame.size(); i++)
      std::cout << "frame sent to server is \n" + frame[i] << std::endl;
    for(unsigned i = 0; i < frame.size(); i++){
      if (!connectionHandler.sendLine(frame[i]))
      {
        std::cout << "Disconnected. Exiting...\n" << std::endl;
        break;
      }
    }
  }
  serverSocketThread.join();
}
