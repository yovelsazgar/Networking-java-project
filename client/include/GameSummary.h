#pragma once
#include <string>
#include <iostream>
#include <map>
#include <vector>



using std::string;
using namespace std;

class GameSummary
{
private:
  string gameName;
  string teamAname;
  string teamBname;
  map<string,string> gameUpdates;
  map<string,string> teamAUpdates;
  map<string,string> teamBUpdates;
  map<string,string> reports;

public:
  GameSummary();
};
