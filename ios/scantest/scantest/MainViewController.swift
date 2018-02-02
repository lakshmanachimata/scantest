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
    @IBOutlet weak var wifiname: UILabel!
    override func viewDidLoad()
    {
        super.viewDidLoad()
        
        let etap = UITapGestureRecognizer(target: self, action: #selector(MainViewController.scanit))
        scanem.isUserInteractionEnabled = true
        scanem.addGestureRecognizer(etap)
    }
    
    @objc func scanit(){
        let ssid = self.getWiFiName()
        wifiname.text = "WIFI NAME IS : " + (ssid?.capitalized)!
    }
    
    func getWiFiName() -> String? {
        var ssid: String?
        if let interfaces = CNCopySupportedInterfaces() as NSArray? {
            for interface in interfaces {
                if let interfaceInfo = CNCopyCurrentNetworkInfo(interface as! CFString) as NSDictionary? {
                    ssid = interfaceInfo[kCNNetworkInfoKeySSID as String] as? String
                    break
                }
            }
        }
        return ssid
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }


}

