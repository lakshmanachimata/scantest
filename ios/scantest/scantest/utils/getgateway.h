//
//  getgateway.h
//  scantest
//
//  Created by lakshmana on 06/02/18.
//  Copyright © 2018 nearhop. All rights reserved.
//

#ifndef getgateway_h
#define getgateway_h
#include <stdio.h>
#include <net/if.h>
#include <string.h>
#include <netinet/in.h>
#include <stdlib.h>
#include <ifaddrs.h>
#include <sys/sysctl.h>
#include "getgateway.h"
#include "route.h"
#import <arpa/inet.h>
#include "TargetConditionals.h"

static unsigned int gatewayAddress = 0;
static unsigned int subNetMaskAddress = 0;

unsigned int getdefaultgateway(in_addr_t * addr);
unsigned int getDefaultIPNumber();
char* getIPFromNumber(unsigned int inaddr);
unsigned int getRouterDetails();
unsigned int getSubNetMaskValue();
unsigned int getRouterIPAddress();
unsigned int getIpDetails(struct in_addr * routerIPAddress, struct in_addr * subNetMask);
unsigned int getNumberFromIP(char* ip);

#endif /* getgateway_h */
