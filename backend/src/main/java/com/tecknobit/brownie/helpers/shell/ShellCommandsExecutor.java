package com.tecknobit.brownie.helpers.shell;

import com.jcraft.jsch.JSchException;
import com.tecknobit.brownie.services.hosts.entities.BrownieHost;
import com.tecknobit.brownie.services.hosts.services.HostsService;
import com.tecknobit.brownie.services.hostservices.entity.BrownieHostService;
import com.tecknobit.brownie.services.hostservices.services.HostServicesService;
import com.tecknobit.browniecore.enums.HostStatus;
import com.tecknobit.equinoxcore.annotations.Structure;
import com.tecknobit.equinoxcore.annotations.Wrapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static com.tecknobit.apimanager.apis.ResourcesUtils.getResourceStream;
import static com.tecknobit.browniecore.enums.HostStatus.*;
import static com.tecknobit.equinoxbackend.environment.services.builtin.service.EquinoxItemsHelper.COMMA;

@Structure
public abstract class ShellCommandsExecutor {

    private static final String SERVICE_STARTER_SCRIPT = "service-starter.sh";

    protected static final String BASH_SCRIPT_OPTION = "-s";

    protected static final String SUDO_SHUTDOWN_NOW = "sudo shutdown now";

    protected static final String SUDO_REBOOT = "sudo reboot";

    protected static final String GET_CURRENT_HOST_STATS = """
            echo -e "$(top -bn1 | grep 'Cpu(s)' | awk '{print 100 - $8}'),
            $(if ls /sys/devices/system/cpu/cpu*/cpufreq/scaling_cur_freq &>/dev/null; then
                freq=$(awk '{s+=$1} END {print s/NR/1000000}' /sys/devices/system/cpu/cpu*/cpufreq/scaling_cur_freq);
                printf "%.2f" $freq;
              else
                freq=$(lscpu | grep 'CPU MHz' | awk '{print $3/1000}');
                printf "%.2f" $freq;
              fi),
            $(free -h | grep 'Mem' | awk '{print $3 "/" $2}' | sed 's/[A-Za-z]//g'),
            $(df -h --total | grep 'total' | awk '{print $3 "/" $2}' | sed 's/[A-Za-z]//g'),
            $(if lsblk -d -o NAME | grep -q mmcblk; then echo "SD_CARD";
              elif lsblk -d -o NAME | grep -q nvme; then echo "SSD_NVMe";
              elif lsblk -d -o NAME | grep -q vda; then echo "VIRTUAL_DISK";
              elif lsblk -d -o ROTA | awk 'NR>1' | grep -q 0; then echo "SSD";
              else echo "HARD_DISK"; fi)"
            \s""";

    protected static final String FIND_SERVICE_PATH = """
            find . -type f -name "%s"
            """;

    protected static final String REMOVE_FILE_COMMAND = "rm %s";

    protected static final String NOHUP_OUT_FILE = "nohup.out";

    protected static final String REMOVE_NOHUP_OUT_FILE_COMMAND = "rm -f %s";

    protected static final String KILL_SERVICE = "kill %s";

    @Wrapper
    public abstract void rebootHost(HostsService service, BrownieHost host) throws Exception;

    @Wrapper
    protected void setRebootingStatus(HostsService hostsService, BrownieHost host) {
        handleHostStatus(hostsService, host.getId(), REBOOTING);
        HostServicesService servicesService = hostsService.getServicesService();
        for (BrownieHostService service : host.getServices())
            servicesService.setServiceInRebooting(service.getId());
    }

    protected void handleServicesAfterReboot(HostsService hostsService, BrownieHost host) throws Exception {
        HostServicesService servicesService = hostsService.getServicesService();
        for (BrownieHostService service : host.getServices()) {
            if (service.getConfiguration().autoRunAfterHostReboot())
                servicesService.startService(host, service);
            else
                servicesService.setServiceAsStopped(service.getId());
        }
    }

    @Wrapper
    public abstract void stopHost(HostsService service, BrownieHost host) throws Exception;

    @Wrapper
    protected void setOfflineStatus(HostsService hostsService, BrownieHost host) {
        handleHostStatus(hostsService, host.getId(), OFFLINE);
        HostServicesService servicesService = hostsService.getServicesService();
        for (BrownieHostService service : host.getServices())
            servicesService.setServiceAsStopped(service.getId());
    }

    @Wrapper
    protected void setOnlineStatus(HostsService hostsService, BrownieHost host) {
        handleHostStatus(hostsService, host.getId(), ONLINE);
    }

    protected void handleHostStatus(HostsService hostsService, String hostId, HostStatus status) {
        hostsService.getHostsRepository().handleHostStatus(hostId, status.name());
        hostsService.getEventsService().registerHostStatusChangedEvent(hostId, status);
    }

    @Wrapper
    public String[] getCurrentHostStats() throws Exception {
        return execBashCommand(GET_CURRENT_HOST_STATS).replaceAll(" ", "").split(COMMA);
    }

    @Wrapper
    public String findServicePath(String name) throws Exception {
        return execBashCommand(String.format(FIND_SERVICE_PATH, name));
    }

    public abstract long startService(BrownieHostService service) throws Exception;

    protected void purgeNohupOutIfRequired(BrownieHostService service) {
        String servicePath = service.getServicePath();
        if (service.getConfiguration().purgeNohupOutAfterReboot())
            removeNohupOut(service.getName(), servicePath);
    }

    protected void execServiceStarter(BrownieHostService service, OutputStream out) throws IOException {
        InputStream serviceStarterScript = getResourceStream(SERVICE_STARTER_SCRIPT, ShellCommandsExecutor.class);
        String serviceStarter = String.format(new String(serviceStarterScript.readAllBytes()), service.getServicePath(),
                service.getConfiguration().getProgramArguments());
        out.write(serviceStarter.getBytes(StandardCharsets.UTF_8));
        out.flush();
        out.close();
    }

    protected long extractServicePid(String commandResult) {
        return Long.parseLong(commandResult.trim());
    }

    private void removeNohupOut(String serviceName, String servicePath) {
        String nohupOutPath = String.format(REMOVE_NOHUP_OUT_FILE_COMMAND, servicePath.replace(serviceName, NOHUP_OUT_FILE));
        try {
            execBashCommand(nohupOutPath, false);
        } catch (Exception ignored) {
        }
    }

    public void rebootService(BrownieHostService service, OnCommandExecuted onCommandExecuted) throws Exception {
        stopService(service, false);
        long pid = startService(service);
        onCommandExecuted.afterExecution(pid);
    }

    @Wrapper
    public String stopService(BrownieHostService service) throws Exception {
        return stopService(service, true);
    }

    public String stopService(BrownieHostService service, boolean closeSession) throws Exception {
        return execBashCommand(String.format(KILL_SERVICE, service.getPid()), closeSession);
    }

    @Wrapper
    public String removeService(String filename, boolean closeSession) throws Exception {
        return execBashCommand(String.format(REMOVE_FILE_COMMAND, filename), closeSession);
    }

    @Wrapper
    protected String execBashCommand(String command) throws Exception {
        return execBashCommand(command, null, true);
    }

    @Wrapper
    protected String execBashCommand(String command, OnCommandExecuted onCommandExecuted) throws Exception {
        return execBashCommand(command, onCommandExecuted, true);
    }

    @Wrapper
    protected String execBashCommand(String command, boolean closeSession) throws Exception {
        return execBashCommand(command, null, closeSession);
    }

    protected abstract String execBashCommand(String command, OnCommandExecuted onCommandExecuted,
                                              boolean closeSession) throws Exception;

    protected String appendExitStatus(String errorMessage, int exitStatus) {
        return errorMessage + "Exit status:" + exitStatus;
    }

    public interface OnCommandExecuted {

        void afterExecution(Object... extra);

    }

    public static ShellCommandsExecutor getInstance(BrownieHost host) throws JSchException {
        if (host.isRemoteHost())
            return new RemoteShellCommandsExecutors(host);
        return new LocalShellCommandsExecutor();
    }

}
