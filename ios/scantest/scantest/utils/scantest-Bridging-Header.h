//
//  Use this file to import your target's public headers that you would like to expose to Swift.
//

#include "getgateway.h"
unsigned int getDefaultIPNumber();
char* getIPFromNumber(unsigned int inaddr);
unsigned int getRouterDetails();
unsigned int getSubNetMaskValue();
unsigned int getRouterIPAddress();
unsigned int getNumberFromIP(char* ip);
