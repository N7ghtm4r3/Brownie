package com.tecknobit.brownie.services.hosts.commands;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.tecknobit.brownie.services.hosts.entities.BrownieHost;
import com.tecknobit.equinoxcore.annotations.Wrapper;
import kotlin.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ShellCommandsExecutor {

    private static final String STRICT_HOST_KEY_CHECKING_OPTION = "StrictHostKeyChecking";

    private static final String GET_MAC_ADDRESS_COMMAND = "ip link show | awk -F': ' '/^[0-9]+: e/{print $2}' | head -n 1 | xargs -I {} ip link show {} | awk '/ether/ {print $2}'";

    private static final String GET_BROADCAST_IP_ADDRESS_COMMAND = "ip -4 addr show | grep -E 'inet .*brd' | awk '{print $4}'";

    private static final String SUDO_SHUTDOWN_NOW = "sudo shutdown now";

    private static final String SUDO_REBOOT = "sudo reboot";

    private static final String EXEC_CHANNEL_TYPE = "exec";

    private final Session session;

    public ShellCommandsExecutor(BrownieHost host) throws JSchException {
        this(host.getSshUser(), host.getHostAddress(), host.getSshPassword());
    }

    public ShellCommandsExecutor(String sshUser, String hostAddress, String sshPassword) throws JSchException {
        JSch jSch = new JSch();
        session = jSch.getSession(sshUser, hostAddress);
        session.setPassword(sshPassword);
        session.setConfig(STRICT_HOST_KEY_CHECKING_OPTION, "no");
        session.setTimeout(2000);
        session.connect();
    }

    @Wrapper
    public Pair<String, String> getNetworkInterfaceDetails() throws Exception {
        String macAddress = execBashCommand(GET_MAC_ADDRESS_COMMAND, null, false);
        String broadcastIp = execBashCommand(GET_BROADCAST_IP_ADDRESS_COMMAND, null, true);
        return new Pair<>(macAddress, broadcastIp);
    }

    @Wrapper
    public void stopHost(OnCommandExecuted onCommandExecuted) throws Exception {
        execBashCommand(SUDO_SHUTDOWN_NOW, onCommandExecuted);
    }

    @Wrapper
    public void rebootHost(OnCommandExecuted onCommandExecuted) throws Exception {
        execBashCommand(SUDO_REBOOT, onCommandExecuted);
    }

    private String execBashCommand(String command, OnCommandExecuted onCommandExecuted) throws Exception {
        return execBashCommand(command, onCommandExecuted, true);
    }

    private String execBashCommand(String command, OnCommandExecuted onCommandExecuted, boolean closeSession) throws Exception {
        ChannelExec channel = (ChannelExec) session.openChannel(EXEC_CHANNEL_TYPE);
        channel.setInputStream(null);
        channel.setCommand(command);
        BufferedReader reader = new BufferedReader(new InputStreamReader(channel.getInputStream()));
        channel.connect();
        try {
            String errorMessage = new String(channel.getErrStream().readAllBytes());
            if (errorMessage.isEmpty()) {
                StringBuilder commandOutPUT = new StringBuilder();
                String resultLine;
                while ((resultLine = reader.readLine()) != null)
                    commandOutPUT.append(resultLine).append("\n");
                if (onCommandExecuted != null)
                    onCommandExecuted.afterExecution();
                return commandOutPUT.toString().replaceAll("\n", "");
            } else {
                errorMessage += "Exit status:" + channel.getExitStatus();
                throw new RuntimeException(errorMessage);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            channel.disconnect();
            if (closeSession)
                session.disconnect();
        }
    }

    public interface OnCommandExecuted {

        void afterExecution();

    }

}
