package com.tecknobit.brownie.helpers;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.tecknobit.brownie.services.hosts.entities.BrownieHost;
import com.tecknobit.brownie.services.hostservices.entity.BrownieHostService;
import com.tecknobit.equinoxcore.annotations.Wrapper;
import kotlin.Pair;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static com.tecknobit.apimanager.apis.ResourcesUtils.getResourceStream;
import static com.tecknobit.equinoxbackend.environment.services.builtin.service.EquinoxItemsHelper.COMMA;

public class ShellCommandsExecutor {

    private static final String SERVICE_STARTER_SCRIPT = "service-starter.sh";

    private static final String STRICT_HOST_KEY_CHECKING_OPTION = "StrictHostKeyChecking";

    private static final String EXEC_CHANNEL_TYPE = "exec";

    private static final String GET_MAC_ADDRESS_COMMAND = """
            ip link show | awk -F': ' '/^[0-9]+: e/{print $2}' | head -n \
            1 | xargs -I {} ip link show {} | awk '/ether/ {print $2}'""";

    private static final String GET_BROADCAST_IP_ADDRESS_COMMAND = """
            ip -4 addr show | grep -E 'inet .*brd' | awk '{print $4}'""";

    private static final String SUDO_SHUTDOWN_NOW = "sudo shutdown now";

    private static final String SUDO_REBOOT = "sudo reboot";

    private static final String GET_CURRENT_HOST_STATS = """
            echo -e "$(top -bn1 | grep 'Cpu(s)' | awk '{print 100 - $8}'),\
            $(awk '{s+=$1} END {print s/NR/1000000}' /sys/devices/system/cpu/cpu*/cpufreq/scaling_cur_freq),\
            $(free -h | grep 'Mem' | awk '{print $3 "/" $2}' | sed 's/[A-Za-z]//g'),\
             $(df -h --total | grep 'total' | awk '{print $3 "/" $2}' | sed 's/[A-Za-z]//g'),\
             $(if lsblk -d -o NAME | grep -q mmcblk; then echo "SD_CARD";\s
             elif lsblk -d -o ROTA | awk 'NR>1' | grep -q 0;\s
             then echo "SSD"; else echo "HARD_DISK"; fi)"
            \s""";

    private static final String FIND_SERVICE_PATH = """
            find . -type f -name "%s"
            """;

    private static final String EXECUTE_BASH_SCRIPT = "bash -s";

    private static final String REMOVE_FILE_COMMAND = "rm %s";

    private static final String REMOVE_NOHUP_OUT_FILE_COMMAND = "rm -f nohup.out";

    private static final String KILL_SERVICE = "kill %s";

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

    public Pair<String, String> getNetworkInterfaceDetails() throws Exception {
        String macAddress = execBashCommand(GET_MAC_ADDRESS_COMMAND, false);
        String broadcastIp = execBashCommand(GET_BROADCAST_IP_ADDRESS_COMMAND, true);
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

    @Wrapper
    public String[] getCurrentHostStats() throws Exception {
        return execBashCommand(GET_CURRENT_HOST_STATS).replaceAll(" ", "").split(COMMA);
    }

    @Wrapper
    public String findServicePath(String name) throws Exception {
        return execBashCommand(String.format(FIND_SERVICE_PATH, name));
    }

    public long startService(BrownieHostService service) throws Exception {
        if (service.getConfiguration().purgeNohupOutAfterReboot())
            removeNohupOut();
        ChannelExec channel = (ChannelExec) session.openChannel(EXEC_CHANNEL_TYPE);
        channel.setInputStream(null);
        channel.setCommand(EXECUTE_BASH_SCRIPT);
        InputStream commandResultStream = channel.getInputStream();
        channel.connect();
        InputStream serviceStarterScript = getResourceStream(SERVICE_STARTER_SCRIPT, ShellCommandsExecutor.class);
        String serviceStarter = String.format(new String(serviceStarterScript.readAllBytes()), service.getServicePath(),
                service.getConfiguration().getProgramArguments());
        OutputStream out = channel.getOutputStream();
        out.write(serviceStarter.getBytes(StandardCharsets.UTF_8));
        out.flush();
        out.close();
        String commandResult = new String(commandResultStream.readAllBytes()).trim();
        if (commandResult.isEmpty())
            return -1;
        long pid = Long.parseLong(commandResult);
        channel.disconnect();
        session.disconnect();
        return pid;
    }

    public void removeNohupOut() {
        try {
            execBashCommand(REMOVE_NOHUP_OUT_FILE_COMMAND, false);
        } catch (Exception ignored) {
        }
    }

    public String stopService(BrownieHostService service) throws Exception {
        return execBashCommand(String.format(KILL_SERVICE, service.getPid()));
    }

    public String removeService(String filename, boolean closeSession) throws Exception {
        return execBashCommand(String.format(REMOVE_FILE_COMMAND, filename), closeSession);
    }

    @Wrapper
    private String execBashCommand(String command) throws Exception {
        return execBashCommand(command, null, true);
    }

    @Wrapper
    private String execBashCommand(String command, OnCommandExecuted onCommandExecuted) throws Exception {
        return execBashCommand(command, onCommandExecuted, true);
    }

    @Wrapper
    private String execBashCommand(String command, boolean closeSession) throws Exception {
        return execBashCommand(command, null, closeSession);
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
                StringBuilder commandOutput = new StringBuilder();
                String resultLine;
                while ((resultLine = reader.readLine()) != null)
                    commandOutput.append(resultLine).append("\n");
                if (onCommandExecuted != null)
                    onCommandExecuted.afterExecution();
                return commandOutput.toString().replaceAll("\n", "");
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
