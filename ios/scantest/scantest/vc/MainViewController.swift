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
import SwiftSocket

class MainViewController: UIViewController {

    let host = ""
    let port = 80
    var client: TCPClient?
    
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
            let rAddrs  = getRouterDetails();
            if(rAddrs == 0){
                let subnet = getSubNetMaskValue();
                let rIP = getRouterIPAddress();
                let subnetAddr = String(cString: getIPFromNumber(subnet))
                let IPAddr = String(cString: getIPFromNumber(rIP))
                print(subnetAddr)
                print(IPAddr)
            }
            DispatchQueue.global(qos:.userInteractive).async {
                
                DispatchQueue.main.async {
                    
                }
            }
        }
    }
    
    func intToBinaryString( _ value:Int32 ) -> String {
        
        var result = ""
        
        var _value = value
        
        while _value > 0 {
            
            result = ( _value % 2 != 0 ? "1" : "0" ) + result
            _value /= 2
        }
        
//        while result.characters.count % 8 != 0 {
//
//            result = "0" + result
//        }
//
        return result
    }

    
    func checkPort(routerAddress : String){
        client = TCPClient(address: routerAddress, port: Int32(port))
        switch client?.connect(timeout: 10) {
        case .success?:
            print("routerAddress has open 80")
            break;
        case .failure(let error)?:
            break;
        case .none:
            break;
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

