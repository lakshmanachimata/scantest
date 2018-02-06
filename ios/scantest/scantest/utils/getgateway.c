


//#if TARGET_IPHONE_SIMULATOR
//#include <net/route.h>
//#define TypeEN    "en1"
//#else
//#endif
#include "getgateway.h"


#define CTL_NET         4               /* network, see socket.h */
#define TypeEN    "en0"


#if defined(BSD) || defined(__APPLE__)

#define ROUNDUP(a) \
((a) > 0 ? (1 + (((a) - 1) | (sizeof(long) - 1))) : sizeof(long))

char* getIPFromNumber(int inaddr){
    struct in_addr gatewayaddr;
    gatewayaddr.s_addr = inaddr;
    char* ipadd = inet_ntoa(gatewayaddr);
    return ipadd;
}

int getDefaultIPNumber(){
    struct in_addr gatewayaddr;
    int r = getdefaultgateway(&(gatewayaddr.s_addr));
    if(r > 0) {
        return 0;
    }
    return gatewayaddr.s_addr;
}

int getRouterDetails(){
    int addrs[2] = {0,0};
    struct in_addr gatewayaddr,subnetAddress;
    int r = getIpDetails(&gatewayaddr,&subnetAddress);
    if(r > 0) {
        return r;
    }
    subNetMaskAddress = gatewayaddr.s_addr;
    gatewayAddress = subnetAddress.s_addr;
    return 0;
}

int getSubNetMaskValue(){
    return subNetMaskAddress;
}

int getRouterIPAddress(){
    return gatewayAddress;
}

int getdefaultgateway(in_addr_t * addr)
{

    int mib[] = {CTL_NET, PF_ROUTE, 0, AF_INET,
        NET_RT_FLAGS, RTF_GATEWAY};
    size_t l;
    char * buf, * p;
    struct rt_msghdr * rt;
    struct sockaddr * sa;
    struct sockaddr * sa_tab[RTAX_MAX];
    int i;
    int r = -1;
    if(sysctl(mib, sizeof(mib)/sizeof(int), 0, &l, 0, 0) < 0) {
        return -1;
    }
    if(l>0) {
        buf = malloc(l);
        if(sysctl(mib, sizeof(mib)/sizeof(int), buf, &l, 0, 0) < 0) {
            return -1;
        }
        for(p=buf; p<buf+l; p+=rt->rtm_msglen) {
            rt = (struct rt_msghdr *)p;
            sa = (struct sockaddr *)(rt + 1);
            for(i=0; i<RTAX_MAX; i++) {
                if(rt->rtm_addrs & (1 << i)) {
                    sa_tab[i] = sa;
                    sa = (struct sockaddr *)((char *)sa + ROUNDUP(sa->sa_len));
                } else {
                    sa_tab[i] = NULL;
                }
            }
            
            if( ((rt->rtm_addrs & (RTA_DST|RTA_GATEWAY)) == (RTA_DST|RTA_GATEWAY))
               && sa_tab[RTAX_DST]->sa_family == AF_INET
               && sa_tab[RTAX_GATEWAY]->sa_family == AF_INET) {
                
                
                if(((struct sockaddr_in *)sa_tab[RTAX_DST])->sin_addr.s_addr == 0) {
                    char ifName[128];
                    if_indextoname(rt->rtm_index,ifName);
                    if(strcmp(TypeEN,ifName)==0){
                        *addr = ((struct sockaddr_in *)(sa_tab[RTAX_GATEWAY]))->sin_addr.s_addr;
                        r = 0;
                    }
                }
            }
        }
        free(buf);
    }

    return r;
}

int getIpDetails(struct in_addr * routerIPAddress, struct in_addr * subNetMask){
    struct ifaddrs *interfaces = NULL;
    struct ifaddrs *temp_addr = NULL;
    int success = 0;
    // retrieve the current interfaces - returns 0 on success
    success = getifaddrs(&interfaces);
    
    if (success == 0)
    {
        temp_addr = interfaces;
        while(temp_addr != NULL)
        {
            // check if interface is en0 which is the wifi connection on the iPhone
            if(temp_addr->ifa_addr->sa_family == AF_INET)
            {
                if(strcmp(TypeEN,temp_addr->ifa_name)==0)
                {
                    *routerIPAddress = ((struct sockaddr_in *)temp_addr->ifa_addr)->sin_addr;
                    *subNetMask = ((struct sockaddr_in *)temp_addr->ifa_netmask)->sin_addr;
                }
            }
            temp_addr = temp_addr->ifa_next;
        }
        freeifaddrs(interfaces);
    }
    return 0;

}

#endif
