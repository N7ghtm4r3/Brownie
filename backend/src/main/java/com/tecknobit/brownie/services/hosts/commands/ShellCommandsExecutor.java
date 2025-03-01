package com.tecknobit.brownie.services.hosts.commands;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.tecknobit.brownie.services.hosts.entities.BrownieHost;
import com.tecknobit.equinoxcore.annotations.Wrapper;

import java.io.IOException;

public class ShellCommandsExecutor {

    private static final String STRICT_HOST_KEY_CHECKING_OPTION = "StrictHostKeyChecking";

    private static final String EXEC_CHANNEL_TYPE = "exec";

    private static final String SUDO_SHUTDOWN_NOW = "sudo shutdown now";

    private static final String SUDO_REBOOT = "sudo reboot";

    private static final String PING = "ping -c 2 -W 2 %s";

    private final JSch jSch;

    private final Session session;

    public ShellCommandsExecutor(BrownieHost host) throws JSchException {
        this.jSch = new JSch();
        session = jSch.getSession(host.getSshUser(), host.getHostAddress());
        session.setPassword(host.getSshPassword());
        session.setConfig(STRICT_HOST_KEY_CHECKING_OPTION, "no");
        session.setTimeout(2000);
        session.connect();
    }

    @Wrapper
    public void stopHost(OnCommandExecuted onCommandExecuted) throws Exception {
        execBashCommand(SUDO_SHUTDOWN_NOW, onCommandExecuted);
    }

    @Wrapper
    public void rebootHost(OnCommandExecuted onCommandExecuted) throws Exception {
        execBashCommand(SUDO_REBOOT, onCommandExecuted);
    }

    private void execBashCommand(String command, OnCommandExecuted onCommandExecuted) throws JSchException {
        ChannelExec channel = (ChannelExec) session.openChannel(EXEC_CHANNEL_TYPE);
        channel.setCommand(command);
        channel.connect();
        try {
            String errorMessage = new String(channel.getErrStream().readAllBytes());
            if (errorMessage.isEmpty())
                onCommandExecuted.afterExecution();
            else {
                errorMessage += "Exit status:" + channel.getExitStatus();
                throw new RuntimeException(errorMessage);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            channel.disconnect();
            session.disconnect();
        }
    }

    public interface OnCommandExecuted {

        void afterExecution();

    }

}
