//
//  ViewController.swift
//  scantest
//
//  Created by lakshmana on 02/02/18.
//  Copyright © 2018 nearhop. All rights reserved.
//

import UIKit
import Foundation
import SystemConfiguration.CaptiveNetwork

class MainViewController: UIViewController {

    @IBOutlet weak var scanem: UIButton!
    override func viewDidLoad()
    {
        super.viewDidLoad()
        
        let etap = UITapGestureRecognizer(target: self, action: #selector(MainViewController.scanit))
        scanem.isUserInteractionEnabled = true
        scanem.addGestureRecognizer(etap)
    }
    
    @objc func scanit(){
        let ssid = self.getWiFiName()
        if( ssid != nil){
            let routerAddress = String(cString: getDefaultIPNumber())
            print(routerAddress);
            print(getWiFiAddress())
        }
    }
    
    func getWiFiName() -> String? {
        var ssid: String?
        if let interfaces = CNCopySupportedInterfaces() as NSArray? {
            for interface in interfaces {
                if let interfaceInfo = CNCopyCurrentNetworkInfo(interface as! CFString) as NSDictionary? {
//                    for key in interfaceInfo.allKeys {
//                        let sKey = key as! String
//                        print(sKey)
//                    }
                    ssid = interfaceInfo[kCNNetworkInfoKeySSID as String] as? String
                    break
                }
            }
        }
        return ssid
    }
    
    func getWiFiAddress() -> String? {
        var address: String?
        var ifaddr: UnsafeMutablePointer<ifaddrs>? = nil
        if getifaddrs(&ifaddr) == 0 {
            var ptr = ifaddr
            while ptr != nil {
                defer { ptr = ptr?.pointee.ifa_next }
                
                let interface = ptr?.pointee
                let addrFamily = interface?.ifa_addr.pointee.sa_family
                if addrFamily == UInt8(AF_INET) || addrFamily == UInt8(AF_INET6) {
                    
                    if let name: String = String(cString: (interface?.ifa_name)!), name == "en0" {
                        var hostname = [CChar](repeating: 0, count: Int(NI_MAXHOST))
                        getnameinfo(interface?.ifa_addr, socklen_t((interface?.ifa_addr.pointee.sa_len)!), &hostname, socklen_t(hostname.count), nil, socklen_t(0), NI_NUMERICHOST)
                        address = String(cString: hostname)
                    }
                }
            }
            freeifaddrs(ifaddr)
        }
        return address
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }


}

