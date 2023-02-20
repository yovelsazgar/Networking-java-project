#pragma once

#include "../include/ConnectionHandler.h"
#include "StompProtocol.h"
#include "../include/event.h"
#include "../src/event.cpp"
#include <vector>


StompProtocol::StompProtocol():
  receiptId(0), terminate(false), subId(0),user(), receiptCommand(), receiptToGameName(),
  gameNameToReceipt(), gameToSubId(), summaries()
{
}

bool StompProtocol::shouldTerminate()
{
    return terminate;
}

// string StompProtocol::login(string& username , string& password)
// {
//   return "CONNECT\naccept-version:1.2\nhost:stomp.cs.bgu.ac.il\nlogin:" + username + "\npasscode:" + password + "\n\n^@";
// }

string StompProtocol::handleAns(string& answer){
  std::vector<std::string> lines = split_string(answer, '\n');
  string ans = "";
  std::cout << "answer from server is:\n" + answer << std::endl;
  if(lines[0] == "CONNECTED"){
    std::cout << "login successful" << std::endl;
  }
  else if(lines[0] == "RECEIPT"){
    ans = decodeReceipt(lines);
    std::cout << ans << std::endl;
  }
  else if(lines[0] == "ERROR"){
    ans = lines[1];
    std::cout << ans << std::endl;
  }
  else if(lines[0] == "MESSAGE")
    ans = decodeMessage(lines);
  return ans;
}

vector<string> StompProtocol::handleRequest(string& answer){
  std::vector<std::string> lines = split_string(answer, ' ');
  std::vector<std::string> ansvector;
  string ans = "";
  if(lines[0] == "login"){
    ans =  decodeLogin(lines);
    ansvector.push_back(ans);
  }
  else if(lines[0] == "join"){
    ans = decodeJoin(lines);
    ansvector.push_back(ans);
    std::cout << ans << std::endl;
  }
  else if(lines[0] == "exit"){
    ans = decodeExit(lines);
    ansvector.push_back(ans);
    std::cout << ans << std::endl;
  }
  else if(lines[0] == "logout"){
    string receipt = to_string(receiptId);
    receiptId++;
    ans = "DISCONNECT\nreceipt-id:" + receipt;
    receiptCommand.insert(std::map<std::string, std::string>::value_type(receipt, lines[0]));
    ansvector.push_back(ans);
    std::cout << ans << std::endl;
  }
  else if(lines[0] == "report"){
    string file = "./data/" + lines[1];
    names_and_events parsed = parseEventsFile(file);
    for(unsigned i = 0; i < parsed.events.size(); i++)
      ansvector.push_back(createSendFrame(parsed, i));
  }
  return ansvector;
}

std::vector<std::string> StompProtocol::split_string(const std::string& input , char x) {
    std::vector<std::string> lines;
    std::string line;
    for (char c : input) {
        if (c == x) {
            lines.push_back(line);
            line.clear();
        } else {
            line += c;
        }
    }
    if (!line.empty()) {
        lines.push_back(line);
    }
    return lines;
}

//DECODERS
string StompProtocol::decodeReceipt(std::vector<std::string>&lines){
  std::vector<std::string> ans = split_string(lines[1] , ':');
  string receiptId=ans[1];
  if(receiptCommand[receiptId] == "join"){
    return "Joined channel " + receiptToGameName[receiptId];
  }
  else if(receiptCommand[receiptId] == "logout"){
    terminate = true;
    return "Disconnected\n";
  }
  else if(receiptCommand[receiptId] == "exit"){
    string game = receiptToGameName[receiptId];
    string toDelete = gameNameToReceipt[game];
    receiptToGameName.erase(toDelete);
    gameNameToReceipt.erase(game);
    return "Exited channel " + game;
  }
  else
    return "ERROR";
}

string StompProtocol::decodeLogin(std::vector<std::string>& lines){
  user = lines[1];
  return "CONNECT\naccept-version:1.2\nhost:stomp.cs.bgu.ac.il\nlogin:" + lines[1]
  + "\npasscode:" + lines[2] + "\n\n";
}

string StompProtocol::decodeJoin(std::vector<std::string>& lines){
  string receipt = to_string(receiptId);
  string id = to_string(subId);
  string game = lines[1];
  receiptId++;
  subId++;
  receiptCommand.insert(std::map<std::string, std::string>::value_type(receipt, lines[0]));
  receiptToGameName.insert(std::map<std::string, std::string>::value_type(receipt, game));
  gameNameToReceipt.insert(std::map<std::string, std::string>::value_type(game, receipt));
  gameToSubId.insert(std::map<std::string, std::string>::value_type(game, id));
  return "SUBSCRIBE\ndestination:/" + game + "\nid:" + id + "\nreceipt:" +  receipt + "\n\n";
}

string StompProtocol::decodeExit(std::vector<std::string>& lines){
  string game = lines[1];
  string receipt = to_string(receiptId);
  string id = gameToSubId[game];
  receiptId++;
  receiptCommand.insert(std::map<std::string, std::string>::value_type(receipt, lines[0]));
  receiptToGameName.insert(std::map<std::string, std::string>::value_type(receipt, game));
  return "UNSUBSCRIBE\nid:" + id + "\nreceipt:" +  receipt + "\n\n";
}

string StompProtocol::decodeReport(names_and_events &parsed, int index){
  string game_update = toStringEvent(parsed.events[index].get_game_updates());
  string team_a_updates = toStringEvent(parsed.events[index].get_team_a_updates());
  string team_b_updates = toStringEvent(parsed.events[index].get_team_b_updates());

  return "event name:" + parsed.events[index].get_name() + "\n" + "time:" + to_string(parsed.events[index].get_time()) + "\n" +
         "general game updates:\n " + game_update + "\n" + "team a updates:\n" + team_a_updates + "\n" +
         "\n" + "team b updates:\n" + team_b_updates + "\n" + "\n" + "description:\n" + parsed.events[index].get_discription() + "\n";
}

string StompProtocol::createSendFrame(names_and_events &parsed, int index){
  string teamA = parsed.team_a_name;
  string teamB = parsed.team_b_name;
  std::vector<Event> event = parsed.events;

  return "SEND\ndestination:/" + teamA + "_" + teamB + "\n\nuser:" + user + "\nteam a:" + teamA + "\nteam b:" +
          teamB + "\n" + decodeReport(parsed, index);
}

string StompProtocol::toStringEvent(std::map<string,string> const &map){
  string to_return;
  for (const auto& element : map)
      to_return += element.first + ": " + element.second + "\n";
  return to_return;
}

string StompProtocol::decodeMessage(std::vector<std::string>& lines){
  std::cout << "lines in message: " << std::endl;
  for(unsigned i = 0; i < lines.size(); i++)
      std::cout << lines[i] << std::endl;
  return "end decode message";
}
