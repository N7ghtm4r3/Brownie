package com.tecknobit.brownie.helpers.shell;

import com.jcraft.jsch.JSchException;
import com.tecknobit.brownie.services.hosts.entities.BrownieHost;
import com.tecknobit.brownie.services.hosts.services.HostsService;
import com.tecknobit.brownie.services.hostservices.entities.BrownieHostService;
import com.tecknobit.equinoxcore.annotations.Structure;
import com.tecknobit.equinoxcore.annotations.Wrapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.tecknobit.apimanager.apis.ResourcesUtils.getResourceStream;
import static com.tecknobit.equinoxbackend.environment.services.builtin.service.EquinoxItemsHelper.COMMA;
import static com.tecknobit.equinoxbackend.environment.services.builtin.service.EquinoxItemsHelper.SINGLE_QUOTE;

/**
 * The {@code ShellCommandsExecutor} class is used to execute the bash commands on the shells of the hosts physical machines
 *
 * @author N7ghtm4r3 - Tecknobit
 */
@Structure
public abstract class ShellCommandsExecutor {

    /**
     * {@code SERVICE_STARTER_SCRIPT} the pathname of the script used to start the services
     */
    private static final String SERVICE_STARTER_SCRIPT = "service-starter.sh";

    /**
     * {@code BASH_SCRIPT_OPTION} the bash option used to launch a script
     */
    protected static final String BASH_SCRIPT_OPTION = "-s";

    /**
     * {@code SUDO_SHUTDOWN_NOW} the bash command used to shut down the physical machine of the host
     */
    protected static final String SUDO_SHUTDOWN_NOW = "sudo shutdown now";

    /**
     * {@code SUDO_REBOOT} the bash command used to reboot the physical machine of the host
     */
    protected static final String SUDO_REBOOT = "sudo reboot";

    /**
     * {@code GET_CURRENT_HOST_STATS} the bash command used to retrieve the current stats of the physical machine of the
     * host
     */
    protected static final String GET_CURRENT_HOST_STATS = """
            echo -e "$(top -bn1 | grep 'Cpu(s)' | awk '{print 100 - $8}'),
            $(if ls /sys/devices/system/cpu/cpu*/cpufreq/scaling_cur_freq &>/dev/null; then
                freq=$(awk '{s+=$1} END {print s/NR/1000000}' /sys/devices/system/cpu/cpu*/cpufreq/scaling_cur_freq);
                printf "%.2f" $freq;
              else
                freq=$(lscpu | grep 'CPU MHz' | awk '{print $3/1000}');
                printf "%.2f" $freq;
              fi),
            $(free -b | awk '/Mem:/ {printf "%.2f/%.2f\\n", $3/1073741824, $2/1073741824}'),
            $(df --block-size=1G --total | awk '/total/ {printf "%d/%d\\n", $3, $2}'),
            $(if lsblk -d -o NAME | grep -q mmcblk; then echo "SD_CARD";
              elif lsblk -d -o NAME | grep -q nvme; then echo "SSD_NVMe";
              elif lsblk -d -o NAME | grep -q vda; then echo "VIRTUAL_DISK";
              elif lsblk -d -o ROTA | awk 'NR>1' | grep -q 0; then echo "SSD";
              else echo "HARD_DISK"; fi)"
            \s""";

    /**
     * {@code FIND_SERVICE_PATH} the bash command used to find the path of a service inside the filesystem of the host
     */
    protected static final String FIND_SERVICE_PATH = """
            find / -type f -name "%s" 2>/dev/null
            """;

    /**
     * {@code REMOVE_FILE_COMMAND} the bash command used to remove a file from the filesystem of the host
     */
    protected static final String REMOVE_FILE_COMMAND = "rm %s";

    /**
     * {@code NOHUP_OUT_FILE} the pathname of the generated nohup.out file
     */
    protected static final String NOHUP_OUT_FILE = "nohup.out";

    /**
     * {@code REMOVE_NOHUP_OUT_FILE_COMMAND} the bash command used to remove the {@link #NOHUP_OUT_FILE} from the host
     */
    protected static final String REMOVE_NOHUP_OUT_FILE_COMMAND = "rm -f %s";

    // TODO: 06/05/2025 TO DOCU
    protected static final String GREP_RUNNING_SERVICES_COMMAND = """
                ps -ef | grep -E %s | grep -v grep | awk '{print $2}' | paste -sd ','
            """;

    /**
     * {@code KILL_SERVICE} the bash command used to kill a service currently running on the host
     */
    protected static final String KILL_SERVICE = "kill %s";

    // TODO: 06/05/2025 TO DOCU
    protected static final String PIPE_CHARACTER = "|";

    // TODO: 06/05/2025 TO DOCU
    public Collection<Long> detectStoppedServices(BrownieHost host) throws Exception {
        List<String> serviceNames = host.listRunningServiceNames();
        String command = formatServicesMonitoringCommand(serviceNames);
        return retrieveStoppedServices(command, host);
    }

    // TODO: 06/05/2025 TO DOCU
    private String formatServicesMonitoringCommand(List<String> serviceNames) {
        if (serviceNames.isEmpty())
            return null;
        StringBuilder builder = new StringBuilder();
        builder.append(SINGLE_QUOTE);
        for (String serviceName : serviceNames)
            builder.append(serviceName).append(PIPE_CHARACTER);
        builder.deleteCharAt(builder.length() - 1);
        builder.append(SINGLE_QUOTE);
        return String.format(GREP_RUNNING_SERVICES_COMMAND, builder);
    }

    private Collection<Long> retrieveStoppedServices(String command, BrownieHost host) throws Exception {
        if (command == null)
            return Collections.EMPTY_LIST;
        String result = execBashCommand(command);
        HashSet<Long> servicePids = host.listRunningServicePids();
        if (result.isEmpty())
            return servicePids;
        List<Long> stoppedPids = new ArrayList<>();
        HashSet<String> runningPids = new HashSet<>(Arrays.stream(result.split(COMMA)).toList());
        for (long servicePid : servicePids)
            if (!runningPids.contains(String.valueOf(servicePid)))
                stoppedPids.add(servicePid);
        System.out.println(stoppedPids);
        return stoppedPids;
    }

    /**
     * Method used to reboot the host using the {@link #SUDO_REBOOT} command
     *
     * @param service The host service used to handle the host reboot procedure
     * @param host    The host to reboot
     * @throws Exception when an exception occurred during the process
     */
    @Wrapper
    public abstract void rebootHost(HostsService service, BrownieHost host) throws Exception;

    /**
     * Method used to stop the host using the {@link #SUDO_SHUTDOWN_NOW} command
     *
     * @param service The host service used to handle the host stop procedure
     * @param host The host to stop
     * @throws Exception when an exception occurred during the process
     */
    @Wrapper
    public abstract void stopHost(HostsService service, BrownieHost host) throws Exception;

    /**
     * Method used to get the current stats of the host with the {@link #GET_CURRENT_HOST_STATS} command
     *
     * @return the current stats of the host as array of {@link String}
     * @throws Exception when an exception occurred during the process
     */
    @Wrapper
    public String[] getCurrentHostStats() throws Exception {
        return execBashCommand(GET_CURRENT_HOST_STATS).replaceAll(" ", "").split(COMMA);
    }

    /**
     * Method to find the path of the service using the {@link #FIND_SERVICE_PATH} command
     *
     * @param name The name of the service to find
     *
     * @return the path name of the service as {@link String}
     * @throws Exception when an exception occurred during the process
     */
    @Wrapper
    public String findServicePath(String name) throws Exception {
        return execBashCommand(String.format(FIND_SERVICE_PATH, name));
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
    public abstract long startService(BrownieHostService service) throws Exception;

    /**
     * Method used to purge the {@link #NOHUP_OUT_FILE} when required
     *
     * @param service The service to delete its related {@link #NOHUP_OUT_FILE}
     */
    protected void purgeNohupOutIfRequired(BrownieHostService service) {
        String servicePath = service.getServicePath();
        if (service.getConfiguration().purgeNohupOutAfterReboot())
            removeNohupOut(service.getName(), servicePath);
    }

    /**
     * Method used to execute the {@link #SERVICE_STARTER_SCRIPT} script
     *
     * @param service The service to start
     * @param out The output stream used to write the script to execute
     *
     * @throws IOException when an exception occurred during the process
     */
    protected void execServiceStarter(BrownieHostService service, OutputStream out) throws IOException {
        InputStream serviceStarterScript = getResourceStream(SERVICE_STARTER_SCRIPT, ShellCommandsExecutor.class);
        String serviceStarter = String.format(new String(serviceStarterScript.readAllBytes()), service.getServicePath(),
                service.getConfiguration().getProgramArguments());
        out.write(serviceStarter.getBytes(StandardCharsets.UTF_8));
        out.flush();
        out.close();
    }

    /**
     * Method used to extract from the shell result the pid of the started process
     *
     * @param commandResult The result of the command
     *
     * @return the value of the pid as {@code long}
     */
    protected long extractServicePid(String commandResult) {
        return Long.parseLong(commandResult.trim());
    }

    /**
     * Method used to remove the {@link #NOHUP_OUT_FILE}
     *
     * @param serviceName The name of the service
     * @param servicePath The path of the service
     */
    private void removeNohupOut(String serviceName, String servicePath) {
        String nohupOutPath = String.format(REMOVE_NOHUP_OUT_FILE_COMMAND, servicePath.replace(serviceName, NOHUP_OUT_FILE));
        try {
            execBashCommand(nohupOutPath, false);
        } catch (Exception ignored) {
        }
    }

    /**
     * Method used to reboot a service
     *
     * @param service The service to reboot
     * @param onCommandExecuted The callback to execute the service has been rebooted
     * @throws Exception when an exception occurred during the process
     */
    public void rebootService(BrownieHostService service, OnCommandExecuted onCommandExecuted) throws Exception {
        stopService(service, false);
        long pid = startService(service);
        onCommandExecuted.afterExecution(pid);
    }

    /**
     * Method used to stop a service
     *
     * @param service The service to stop
     * @return the result of the command
     * @throws Exception when an exception occurred during the process
     */
    @Wrapper
    public String stopService(BrownieHostService service) throws Exception {
        return stopService(service, true);
    }

    /**
     * Method used to stop a service
     *
     * @param service The service to stop
     * @param closeSession Whether close the current bash session (like {@link Runtime} or SSH if remote host)
     *
     * @return the result of the command
     * @throws Exception when an exception occurred during the process
     */
    public String stopService(BrownieHostService service, boolean closeSession) throws Exception {
        return execBashCommand(String.format(KILL_SERVICE, service.getPid()), closeSession);
    }

    /**
     * Method to simply remove a service from a host
     *
     * @param serviceName The name of the service to remove
     * @param closeSession Whether close the current bash session (like {@link Runtime} or SSH if remote host)
     *
     * @return the result of the command
     * @throws Exception when an exception occurred during the process
     */
    @Wrapper
    public String removeService(String serviceName, boolean closeSession) throws Exception {
        return execBashCommand(String.format(REMOVE_FILE_COMMAND, serviceName), closeSession);
    }

    /**
     * Method used to execute a bash command
     * @param command The bash command to execute
     *
     * @return the result of the command
     * @throws Exception when an exception occurred during the process
     */
    @Wrapper
    protected String execBashCommand(String command) throws Exception {
        return execBashCommand(command, null, true);
    }

    /**
     * Method used to execute a bash command
     * @param command The bash command to execute
     * @param onCommandExecuted The callback to execute when the command has been executed
     *
     * @return the result of the command
     * @throws Exception when an exception occurred during the process
     */
    @Wrapper
    protected String execBashCommand(String command, OnCommandExecuted onCommandExecuted) throws Exception {
        return execBashCommand(command, onCommandExecuted, true);
    }

    /**
     * Method used to execute a bash command
     * @param command The bash command to execute
     * @param closeSession Whether close the current bash session (like {@link Runtime} or SSH if remote host)
     *
     * @return the result of the command
     * @throws Exception when an exception occurred during the process
     */
    @Wrapper
    protected String execBashCommand(String command, boolean closeSession) throws Exception {
        return execBashCommand(command, null, closeSession);
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
    protected abstract String execBashCommand(String command, OnCommandExecuted onCommandExecuted,
                                              boolean closeSession) throws Exception;

    /**
     * Method used to append the exit status of the command to the error result of the same command
     *
     * @param errorMessage The error message retrieved from the shell
     * @param exitStatus The exit status of the command
     *
     * @return the error message with the exit status as {@link String}
     */
    protected String appendExitStatus(String errorMessage, int exitStatus) {
        return errorMessage + "Exit status:" + exitStatus;
    }

    /**
     * The {@code OnCommandExecuted} interface used as callback to execute extra actions after a bash command executed
     *
     * @author N7ghtm4r3 - Tecknobit
     */
    public interface OnCommandExecuted {

        /**
         * Callback method invoked after a bash command executed
         *
         * @param extra The extra arguments can be useful for a custom implementation
         */
        void afterExecution(Object... extra);

    }

    /**
     * Method used to obtain a specific instance to correctly execute bash commands on hosts shells
     *
     * @param host The host where execute the bash command
     * @return the dedicated shell executor to execute the bash commands as {@link ShellCommandsExecutor}
     * @throws JSchException when an error occurred on remote host SSH connection
     */
    public static ShellCommandsExecutor getInstance(BrownieHost host) throws JSchException {
        if (host.isRemoteHost())
            return new RemoteShellCommandsExecutors(host);
        return new LocalShellCommandsExecutor();
    }

}
