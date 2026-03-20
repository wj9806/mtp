package com.mtp.core.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * 网络工具类，提供获取本地IP地址等网络相关功能
 */
public class NetworkUtil {

    /**
     * 获取本地IP地址，排除回环地址、虚拟地址和IPv6地址
     * @return 本机IP地址，如果获取失败则返回127.0.0.1
     */
    public static String getLocalIp() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                if (networkInterface.isLoopback()|| networkInterface.isVirtual() || !networkInterface.isUp()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (address.isLoopbackAddress() || !(address instanceof Inet4Address)) {
                        continue;
                    }
                    String hostAddress = address.getHostAddress();
                    if (hostAddress != null && !hostAddress.contains(":")) {
                        return hostAddress;
                    }
                }
            }
        } catch (Exception e) {
        }
        return "127.0.0.1";
    }
}