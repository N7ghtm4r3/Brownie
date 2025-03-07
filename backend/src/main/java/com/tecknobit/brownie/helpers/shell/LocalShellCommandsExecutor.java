package com.tecknobit.brownie.helpers.shell;

import com.tecknobit.brownie.helpers.LocalEventsHandler;
import com.tecknobit.brownie.services.hosts.entities.BrownieHost;
import com.tecknobit.brownie.services.hosts.services.HostsService;
import com.tecknobit.brownie.services.hostservices.entity.BrownieHostService;

import java.io.IOException;
import java.io.InputStream;

/**
 * The {@code LocalShellCommandsExecutor} class is used to execute the bash commands on the same physical machine
 * where the backend instance is running
 *
 * @author N7ghtm4r3 - Tecknobit
 * @see ShellCommandsExecutor
 */
public class LocalShellCommandsExecutor extends ShellCommandsExecutor {

    /**
     * {@code BASH} constant of bash value
     */
    private static final String BASH = "bash";

    /**
     * {@code BASH_COMMAND_OPTION} the bash option used to execute a bash command
     */
    private static final String BASH_COMMAND_OPTION = "-c";

    /**
     * Method used to start a service
     *
     * @param service The service to start
     * @return the pid of the started process
     * @throws Exception when an exception occurred during the process
     */
    @Override
    public long startService(BrownieHostService service) throws Exception {
        purgeNohupOutIfRequired(service);
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(BASH, BASH_SCRIPT_OPTION);
        Process process = processBuilder.start();
        execServiceStarter(service, process.getOutputStream());
        String commandResult = formatCommandResult(process.getInputStream());
        String error = new String(process.getErrorStream().readAllBytes());
        int exitStatus = process.waitFor();
        if (exitStatus != 0 || commandResult.isEmpty() || !error.isEmpty())
            return -1;
        return extractServicePid(commandResult);
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
        LocalEventsHandler localEventsHandler = LocalEventsHandler.getInstance();
        try {
            service.setRebootingStatus(host);
            localEventsHandler.registerHostSuspendedEvent(host);
            execBashCommand(SUDO_REBOOT);
        } catch (Exception e) {
            localEventsHandler.unregisterHostSuspendedEvent();
            service.setOnlineStatus(host);
            throw e;
        }
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
        LocalEventsHandler localEventsHandler = LocalEventsHandler.getInstance();
        try {
            service.setOfflineStatus(host);
            localEventsHandler.registerHostSuspendedEvent(host);
            execBashCommand(SUDO_SHUTDOWN_NOW);
        } catch (Exception e) {
            localEventsHandler.unregisterHostSuspendedEvent();
            service.setOnlineStatus(host);
            throw e;
        }
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
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(BASH, BASH_COMMAND_OPTION, command);
        Process process = processBuilder.start();
        String commandResult = formatCommandResult(process.getInputStream());
        String error = new String(process.getErrorStream().readAllBytes());
        int exitStatus = process.waitFor();
        if (exitStatus != 0 && exitStatus != 137 && exitStatus != 143)
            throw new RuntimeException(appendExitStatus(error, exitStatus));
        return commandResult;
    }

    /**
     * Method used to format a raw result of command execution
     *
     * @param rawResult The raw command execution result
     * @return the formatted command execution as {@link String}
     * @throws IOException when an error occurred during the reading of the bytes made up the result
     */
    private String formatCommandResult(InputStream rawResult) throws IOException {
        return new String(rawResult.readAllBytes())
                .replaceAll("\n", "")
                .trim();
    }

}
