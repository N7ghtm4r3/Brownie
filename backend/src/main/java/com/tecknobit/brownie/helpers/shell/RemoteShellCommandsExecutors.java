package com.tecknobit.brownie.helpers.shell;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.tecknobit.brownie.services.hosts.entities.BrownieHost;
import com.tecknobit.brownie.services.hosts.services.HostsService;
import com.tecknobit.brownie.services.hostservices.entity.BrownieHostService;
import kotlin.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.TimeUnit.MINUTES;

public class RemoteShellCommandsExecutors extends ShellCommandsExecutor {

    private static final int MAX_RETRY_ATTEMPTS = 10;

    private static final int MAX_RETRY_TIMEOUT = Math.toIntExact(MINUTES.toMillis(2));

    private static final String STRICT_HOST_KEY_CHECKING_OPTION = "StrictHostKeyChecking";

    private static final String EXEC_CHANNEL_TYPE = "exec";

    private static final String GET_MAC_ADDRESS_COMMAND = """
            ip link show | awk -F': ' '/^[0-9]+: e/{print $2}' | head -n \
            1 | xargs -I {} ip link show {} | awk '/ether/ {print $2}'""";

    private static final String GET_BROADCAST_IP_ADDRESS_COMMAND = """
            ip -4 addr show | grep -E 'inet .*brd' | awk '{print $4}'""";

    private static final String EXECUTE_BASH_SCRIPT = "bash " + BASH_SCRIPT_OPTION;

    private final Session session;

    public RemoteShellCommandsExecutors(BrownieHost host) throws JSchException {
        this(host.getSshUser(), host.getHostAddress(), host.getSshPassword());
    }

    public RemoteShellCommandsExecutors(String sshUser, String hostAddress, String sshPassword) throws JSchException {
        JSch jSch = new JSch();
        session = jSch.getSession(sshUser, hostAddress);
        session.setPassword(sshPassword);
        session.setConfig(STRICT_HOST_KEY_CHECKING_OPTION, "no");
        session.setTimeout(2000);
        session.connect();
    }

    public Pair<String, String> getNetworkInterfaceDetails() throws Exception {
        String macAddress = execBashCommand(GET_MAC_ADDRESS_COMMAND, false);
        String broadcastIp = execBashCommand(GET_BROADCAST_IP_ADDRESS_COMMAND, true);
        return new Pair<>(macAddress, broadcastIp);
    }

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

    @Override
    public void rebootHost(HostsService service, BrownieHost host) throws Exception {
        AtomicInteger attempts = new AtomicInteger(0);
        execBashCommand(SUDO_REBOOT, extra -> {
            service.setRebootingStatus(host);
            waitHostRestarted(service, host, attempts);
        });
    }

    private void waitHostRestarted(HostsService service, BrownieHost host, AtomicInteger attempts) {
        if (attempts.intValue() >= MAX_RETRY_ATTEMPTS)
            throw new IllegalStateException("Impossible reach the " + host.getHostAddress() + " address, you need to restart manually as needed");
        Executors.newCachedThreadPool().execute(() -> {
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(host.getHostAddress(), 22), MAX_RETRY_TIMEOUT);
                try {
                    Thread.sleep(new Random().nextInt(5) * 1000);
                } catch (InterruptedException ignored) {
                } finally {
                    service.restartHost(host);
                }
            } catch (ConnectException e) {
                attempts.incrementAndGet();
                waitHostRestarted(service, host, attempts);
            } catch (IOException e) {
                throw new IllegalStateException("Impossible reach the " + host.getHostAddress() + " address, you need to restart manually as needed");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void stopHost(HostsService service, BrownieHost host) throws Exception {
        execBashCommand(SUDO_SHUTDOWN_NOW, extra -> service.setOfflineStatus(host));
    }

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
