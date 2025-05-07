package com.tecknobit.brownie.helpers.shell;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.tecknobit.brownie.services.hosts.entities.BrownieHost;
import com.tecknobit.brownie.services.hosts.services.HostsService;
import com.tecknobit.brownie.services.hostservices.entities.BrownieHostService;
import kotlin.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicInteger;

import static com.tecknobit.brownie.helpers.RemoteHostWaiter.waitForHostRestart;

/**
 * The {@code RemoteShellCommandsExecutor} class is used to execute the bash commands on a remote host using the SSH
 * as way to communicate
 *
 * @author N7ghtm4r3 - Tecknobit
 * @see ShellCommandsExecutor
 */
public class RemoteShellCommandsExecutor extends ShellCommandsExecutor {

    /**
     * {@code STRICT_HOST_KEY_CHECKING_OPTION} strict host key checking option
     */
    private static final String STRICT_HOST_KEY_CHECKING_OPTION = "StrictHostKeyChecking";

    /**
     * {@code EXEC_CHANNEL_TYPE} exec type of the channel used to execute the commands
     */
    private static final String EXEC_CHANNEL_TYPE = "exec";

    /**
     * {@code GET_MAC_ADDRESS_COMMAND} bash command used to retrieve the mac address of the network interface used to
     * execute the Wake-on-Lan start
     */
    private static final String GET_MAC_ADDRESS_COMMAND = """
            ip link show | awk -F': ' '/^[0-9]+: e/{print $2}' | head -n \
            1 | xargs -I {} ip link show {} | awk '/ether/ {print $2}'""";

    /**
     * {@code GET_BROADCAST_IP_ADDRESS_COMMAND} bash command used to retrieve the broadcast ip address of the network
     * interface used to execute the Wake-on-Lan start
     */
    private static final String GET_BROADCAST_IP_ADDRESS_COMMAND = """
            ip -4 addr show | grep -E 'inet .*brd' | awk '{print $4}'""";

    /**
     * {@code EXECUTE_BASH_SCRIPT} bash command to execute a script
     */
    private static final String EXECUTE_BASH_SCRIPT = "bash " + BASH_SCRIPT_OPTION;

    /**
     * {@code session} current SSH session
     */
    private final Session session;

    /**
     * Constructor to instantiate the object
     *
     * @param host The host where open the remote communication and where execute the bash commands
     * @throws JSchException when an error occurred during the creation of the SSH session
     */
    public RemoteShellCommandsExecutor(BrownieHost host) throws JSchException {
        this(host.getSshUser(), host.getHostAddress(), host.getSshPassword());
    }

    /**
     * Constructor to instantiate the object
     *
     * @param sshUser The user to use for the SSH connection
     * @param hostAddress The address of the host to reach
     * @param sshPassword The password of the SSH user
     *
     * @throws JSchException when an error occurred during the creation of the SSH session
     */
    public RemoteShellCommandsExecutor(String sshUser, String hostAddress, String sshPassword) throws JSchException {
        JSch jSch = new JSch();
        session = jSch.getSession(sshUser, hostAddress);
        session.setPassword(sshPassword);
        session.setConfig(STRICT_HOST_KEY_CHECKING_OPTION, "no");
        session.setTimeout(2000);
        session.connect();
    }

    /**
     * Method to retrieve the network interface details useful to execute the Wake-on-Lan
     *
     * @return the network interface details as {@link Pair} of {@link String}
     * @throws Exception when an exception occurred during the process
     */
    public Pair<String, String> getNetworkInterfaceDetails() throws Exception {
        String macAddress = execBashCommand(GET_MAC_ADDRESS_COMMAND, false);
        String broadcastIp = execBashCommand(GET_BROADCAST_IP_ADDRESS_COMMAND, true);
        return new Pair<>(macAddress, broadcastIp);
    }

    /**
     * Method used to start a service
     *
     * @param service The service to start
     *
     * @return the pid of the started process
     *
     * @throws Exception when an exception occurred during the process
     */
    @Override
    public long startService(BrownieHostService service) throws Exception {
        purgeNohupOutIfRequired(service);
        ChannelExec channel = (ChannelExec) session.openChannel(EXEC_CHANNEL_TYPE);
        channel.setInputStream(null);
        channel.setCommand(EXECUTE_BASH_SCRIPT);
        InputStream commandResultStream = channel.getInputStream();
        channel.connect();
        execServiceStarter(service, channel.getOutputStream());
        String commandResult = new String(commandResultStream.readAllBytes());
        if (commandResult.isEmpty())
            return -1;
        long pid = extractServicePid(commandResult);
        channel.disconnect();
        session.disconnect();
        return pid;
    }

    /**
     * Method used to reboot the host using the {@link #SUDO_REBOOT} command
     *
     * @param service The host service used to handle the host reboot procedure
     * @param host    The host to reboot
     * @throws Exception when an exception occurred during the process
     */
    @Override
    public void rebootHost(HostsService service, BrownieHost host) throws Exception {
        execBashCommand(SUDO_REBOOT, extra -> {
            service.setRebootingStatus(host);
            waitForHostRestart(host, new AtomicInteger(0), () -> service.restartHost(host));
        });
    }

    /**
     * Method used to stop the host using the {@link #SUDO_SHUTDOWN_NOW} command
     *
     * @param service The host service used to handle the host stop procedure
     * @param host The host to stop
     * @throws Exception when an exception occurred during the process
     */
    @Override
    public void stopHost(HostsService service, BrownieHost host) throws Exception {
        execBashCommand(SUDO_SHUTDOWN_NOW, extra -> service.setOfflineStatus(host));
    }

    /**
     * Method used to execute a bash command
     * @param command The bash command to execute
     * @param onCommandExecuted The callback to execute when the command has been executed
     * @param closeSession Whether close the current bash session (like {@link Runtime} or SSH if remote host)
     *
     * @return the result of the command
     * @throws Exception when an exception occurred during the process
     */
    @Override
    protected String execBashCommand(String command, OnCommandExecuted onCommandExecuted, boolean closeSession) throws Exception {
        ChannelExec channel = (ChannelExec) session.openChannel(EXEC_CHANNEL_TYPE);
        channel.setInputStream(null);
        channel.setCommand(command);
        BufferedReader reader = new BufferedReader(new InputStreamReader(channel.getInputStream()));
        channel.connect();
        try {
            String errorMessage = new String(channel.getErrStream().readAllBytes());
            if (errorMessage.isEmpty()) {
                StringBuilder commandOutput = new StringBuilder();
                String resultLine;
                while ((resultLine = reader.readLine()) != null)
                    commandOutput.append(resultLine).append("\n");
                if (onCommandExecuted != null)
                    onCommandExecuted.afterExecution();
                return commandOutput.toString().replaceAll("\n", "");
            } else
                throw new RuntimeException(appendExitStatus(errorMessage, channel.getExitStatus()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            channel.disconnect();
            if (closeSession)
                session.disconnect();
        }
    }

}
