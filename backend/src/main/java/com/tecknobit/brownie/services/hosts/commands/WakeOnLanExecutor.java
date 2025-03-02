package com.tecknobit.brownie.services.hosts.commands;

import com.tecknobit.brownie.services.hosts.entities.BrownieHost;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class WakeOnLanExecutor {

    // TODO: 01/03/2025 CREDITS TO https://gist.github.com/jumar/9200840 
    public void execWoL(BrownieHost host) throws IOException {
        byte[] macBytes = getMacBytes(host.getMacAddress());
        byte[] magicPacket = new byte[102];
        for (int i = 0; i < 6; i++)
            magicPacket[i] = (byte) 0xFF;
        for (int i = 6; i < magicPacket.length; i += macBytes.length)
            System.arraycopy(macBytes, 0, magicPacket, i, macBytes.length);
        InetAddress address = InetAddress.getByName(host.getBroadcastIp());
        DatagramPacket packet = new DatagramPacket(magicPacket, magicPacket.length, address, 9);
        DatagramSocket socket = new DatagramSocket();
        socket.send(packet);
        socket.close();
    }

    private static byte[] getMacBytes(String macStr) throws IllegalArgumentException {
        byte[] bytes = new byte[6];
        String[] hex = macStr.split("([:\\-])");
        if (hex.length != 6)
            throw new IllegalArgumentException("Invalid MAC address.");
        try {
            for (int i = 0; i < 6; i++)
                bytes[i] = (byte) Integer.parseInt(hex[i], 16);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid hex digit in MAC address.");
        }
        return bytes;
    }

}
