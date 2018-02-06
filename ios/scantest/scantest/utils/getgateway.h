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
#include <sys/sysctl.h>
#include "getgateway.h"
#include "route.h"
#import <arpa/inet.h>
#include "TargetConditionals.h"

int getdefaultgateway(in_addr_t * addr);
char* getDefaultIPNumber();

#endif /* getgateway_h */
