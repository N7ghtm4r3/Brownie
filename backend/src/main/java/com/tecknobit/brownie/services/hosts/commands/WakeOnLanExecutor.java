package com.tecknobit.brownie.services.hosts.commands;

import com.tecknobit.brownie.services.hosts.entities.BrownieHost;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * The {@code WakeOnLanExecutor} class is useful to execute the {@code Wake-on-Lan} routine to start a remote host
 *
 * @author N7ghtm4r3 - Tecknobit
 * @apiNote Credits to the original author of the code <a href="https://gist.github.com/jumar/9200840">jumar</a>
 */
public class WakeOnLanExecutor {

    /**
     * Method to execute the {@code Wake-on-Lan} routine to a remote host
     *
     * @param host The remote host to wake up
     * @throws IOException when an error occurred during the execution
     */
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
        socket.setBroadcast(true);
        socket.send(packet);
        socket.close();
    }

    /**
     * Method used to retrieve the bytes made up the MAC address of the remote host
     *
     * @param macStr The MAC address as {@link String}
     *
     * @return the bytes made up the MAC address as array of {@code byte}
     *
     * @throws IllegalArgumentException when the {@code macStr} is not valid
     */
    private byte[] getMacBytes(String macStr) throws IllegalArgumentException {
        byte[] bytes = new byte[6];
        String[] hex = macStr.split("([:\\-])");
        if (hex.length != 6)
            throw new IllegalArgumentException("Invalid MAC address");
        try {
            for (int i = 0; i < 6; i++)
                bytes[i] = (byte) Integer.parseInt(hex[i], 16);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid hex digit in MAC address");
        }
        return bytes;
    }

}
