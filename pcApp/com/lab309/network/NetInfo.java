package com.lab309.network;

import com.lab309.general.ByteArrayConverter;
import com.lab309.general.SizeConstants;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.InterfaceAddress;

import java.util.Enumeration;
import java.util.List;
import java.util.Iterator;

import java.io.IOException;

public class NetInfo {

	/*METHODS*/
	public static byte[] machineMacByIp (InetAddress ip) throws IOException {
		return NetworkInterface.getByInetAddress(ip).getHardwareAddress();
	}

	public static InetAddress broadcastIp () throws IOException {

		Enumeration<NetworkInterface> interfaces;
		List<InterfaceAddress> addresses;
		Iterator<InterfaceAddress> i;
		InetAddress broadcastIp = null;

		interfaces = NetworkInterface.getNetworkInterfaces();
		while (interfaces.hasMoreElements() && broadcastIp == null) {

			addresses = interfaces.nextElement().getInterfaceAddresses();

			i = addresses.iterator();
			while (broadcastIp == null && i.hasNext()) {
				broadcastIp = i.next().getBroadcast();
			}

		}

		return broadcastIp;

		/*
		int broadcastIp;
		DhcpInfo dhcp = wifi.getDhcpInfo();

		broadcastIp = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;

		return InetAddress.getByAddress( ByteArrayConverter.intToArray(broadcastIp, new byte[SizeConstants.sizeOfInt], 0) );
		*/

	}

	public static InetAddress thisMachineIpv4 () throws IOException {
		Enumeration<NetworkInterface> interfaces;
		List<InterfaceAddress> addresses;
		Iterator<InterfaceAddress> i;
		InetAddress ipv4 = null;

		interfaces = NetworkInterface.getNetworkInterfaces();
		while (interfaces.hasMoreElements() && ipv4 == null) {

			addresses = interfaces.nextElement().getInterfaceAddresses();

			i = addresses.iterator();
			while (ipv4 == null && i.hasNext()) {
				ipv4 = i.next().getAddress();
				if (!ipv4.isSiteLocalAddress()) {
					ipv4 = null;
				}
			}

		}

		return ipv4;

	}

}