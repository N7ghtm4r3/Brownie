package com.tecknobit.brownie.helpers;

import com.tecknobit.brownie.services.hostservices.entity.BrownieHostService;

import java.io.IOException;
import java.io.InputStream;

public class LocalShellCommandsExecutor extends ShellCommandsExecutor {

    private static final String BASH = "bash";

    private static final String BASH_COMMAND_OPTION = "-c";

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

    @Override
    protected String execBashCommand(String command, OnCommandExecuted onCommandExecuted, boolean closeSession) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(BASH, BASH_COMMAND_OPTION, command);
        Process process = processBuilder.start();
        String commandResult = formatCommandResult(process.getInputStream());
        String error = new String(process.getErrorStream().readAllBytes());
        int exitStatus = process.waitFor();
        if (exitStatus != 0)
            throw new RuntimeException(appendExitStatus(error, exitStatus));
        System.out.println(commandResult);
        return commandResult;
    }

    private String formatCommandResult(InputStream rawResult) throws IOException {
        return new String(rawResult.readAllBytes())
                .replaceAll("\n", "")
                .trim();
    }

}
