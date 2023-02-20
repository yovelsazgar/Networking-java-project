#pragma once
#include <string>
#include "../include/ConnectionHandler.h"
#include <iostream>
#include <map>
#include <vector>
#include "../include/Event.h"
#include "../include/GameSummary.h"





using std::string;
using namespace std;

// TODO: implement the STOMP protocol
class StompProtocol
{
private:
  int receiptId;
  bool terminate;
  int subId;
  string user;
  std::map<std::string, std::string> receiptCommand;
  std::map<std::string, std::string> receiptToGameName;
  std::map<std::string, std::string> gameNameToReceipt;
  std::map<std::string, std::string> gameToSubId;
  std::map<string,std::map<string,GameSummary>> summaries;

public:
  StompProtocol();
  //string login(string& username , string& password);
  vector<string> split_string(const string& input, char x);
  string handleAns(string& answer);
  vector<string> handleRequest(string& answer);
  bool shouldTerminate();

  string decodeReceipt(std::vector<std::string>& lines);
  string decodeLogin(std::vector<std::string>& lines);
  string decodeJoin(std::vector<std::string>& lines);
  string decodeExit(std::vector<std::string>& lines);
  string decodeReport(names_and_events &parsed, int index);
  string decodeMessage(std::vector<std::string>& lines);

  string createSendFrame(names_and_events &parsed, int index);
  string toStringEvent(std::map<string,string> const &map);

};
