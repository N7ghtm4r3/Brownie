package com.tecknobit.brownie.helpers;

import com.jcraft.jsch.JSchException;
import com.tecknobit.brownie.services.hosts.entities.BrownieHost;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.TimeUnit.MINUTES;

public class RemoteHostWaiter {

    private static final int MAX_RETRY_ATTEMPTS = 10;

    private static final int MAX_RETRY_TIMEOUT = Math.toIntExact(MINUTES.toMillis(2));

    private static final String CONNECTION_ERROR_MESSAGE = "Impossible reach the %s address, you need to restart manually as needed";

    public static void waitForHostRestart(BrownieHost host, AtomicInteger attempts,
                                          OnHostRestart onHostRestart) {
        if (attempts.intValue() >= MAX_RETRY_ATTEMPTS)
            throw new IllegalStateException(String.format(CONNECTION_ERROR_MESSAGE, host.getHostAddress()));
        Executors.newCachedThreadPool().execute(() -> {
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(host.getHostAddress(), 22), MAX_RETRY_TIMEOUT);
                onHostRestart.onRestart();
            } catch (ConnectException | JSchException e) {
                attempts.incrementAndGet();
                waitForHostRestart(host, attempts, onHostRestart);
            } catch (IOException e) {
                throw new IllegalStateException(String.format(CONNECTION_ERROR_MESSAGE, host.getHostAddress()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public interface OnHostRestart {

        void onRestart() throws Exception;

    }

}
