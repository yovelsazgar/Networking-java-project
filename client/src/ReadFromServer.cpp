#include "../include/ReadFromServer.h"
#include "../include/StompProtocol.h"

ReadFromServer::ReadFromServer (ConnectionHandler &connectionHandler, StompProtocol &protocol):
    connectionHandler(connectionHandler), protocol(protocol) {}

void ReadFromServer::run(){
  while (!protocol.shouldTerminate())
    {
        std::string answer;
        // Get back an answer: by using the expected number of bytes (len bytes + newline delimiter)
        // We could also use: connectionHandler.getline(answer) and then get the answer without the newline char at the end
        if (!connectionHandler.getLine(answer))
        {
            std::cout << "Disconnected. Exiting... here here\n"
                      << std::endl;
            break;
        }
        int len = answer.length();
        // A C string must end with a 0 char delimiter.  When we filled the answer buffer from the socket
        // we filled up to the \n char - we must make sure now that a 0 char is also present. So we truncate last character.
        
        answer.resize(len - 1);
        protocol.handleAns(answer);
    }
}