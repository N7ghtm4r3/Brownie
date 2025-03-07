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

/**
 * The {@code RemoteHostWaiter} class is used to wait the remote host restart after a reboot to perform the post reboot
 * routines such restart the services, etc...
 *
 * @author N7ghtm4r3 - Tecknobit
 */
public class RemoteHostWaiter {

    /**
     * {@code MAX_RETRY_ATTEMPTS} the maximum attempts to retry to connect to the rebooted host
     */
    private static final int MAX_RETRY_ATTEMPTS = 10;

    /**
     * {@code MAX_RETRY_TIMEOUT} the maximum timeout to wait for a connection attempts
     */
    private static final int MAX_RETRY_TIMEOUT = Math.toIntExact(MINUTES.toMillis(2));

    /**
     * {@code CONNECTION_ERROR_MESSAGE} the error message to display when an error occurred during the connection attempts
     */
    private static final String CONNECTION_ERROR_MESSAGE = "Impossible reach the %s address, you need to restart manually as needed";

    /**
     * Method used to wait the restart of the host to perform the post reboot routines
     *
     * @param host          The rebooted host
     * @param attempts      The current attempts reached
     * @param onHostRestart The callback to invoke after the host restarted
     */
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

    /**
     * The {@code OnHostRestart} interface used as callback to perform routines after a host restarted
     *
     * @author N7ghtm4r3 - Tecknobit
     */
    public interface OnHostRestart {

        /**
         * Callback method to perform any routines after a host restarted
         *
         * @throws Exception when an error occurred during the post reboot routines execution
         */
        void onRestart() throws Exception;

    }

}
