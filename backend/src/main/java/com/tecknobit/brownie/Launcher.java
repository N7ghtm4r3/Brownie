package com.tecknobit.brownie;

import com.tecknobit.apimanager.apis.ServerProtector;
import com.tecknobit.apimanager.exceptions.SaveData;
import com.tecknobit.brownie.services.session.controller.BrownieSessionController;
import com.tecknobit.equinoxbackend.resourcesutils.ResourcesProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.security.NoSuchAlgorithmException;

import static com.tecknobit.brownie.services.session.controller.BrownieSessionController.brownieServerProtector;
import static com.tecknobit.equinoxbackend.resourcesutils.ResourcesProvider.CUSTOM_CONFIGURATION_FILE_PATH;
import static com.tecknobit.equinoxbackend.resourcesutils.ResourcesProvider.DEFAULT_CONFIGURATION_FILE_PATH;

/**
 * The {@code Launcher} class is useful to launch <b>Brownie's backend service</b>
 *
 * @author N7ghtm4r3 - Tecknobit
 */
@EnableAutoConfiguration
@PropertySources({
        @PropertySource(value = "classpath:" + DEFAULT_CONFIGURATION_FILE_PATH),
        @PropertySource(value = "file:" + CUSTOM_CONFIGURATION_FILE_PATH, ignoreResourceNotFound = true)
})
@EnableJpaRepositories(
        value = {"com.tecknobit.brownie.*"}
)
@EntityScan(
        value = {"com.tecknobit.brownie.*"}
)
@SpringBootApplication
public class Launcher {

    /**
     * Main method to start the backend, will be created also the resources directories if not exist invoking the
     * {@link ResourcesProvider} routine
     *
     * @param args: custom arguments to share with {@link SpringApplication} and with the {@link BrownieSessionController#brownieServerProtector}
     * @apiNote the arguments scheme:
     * <ul>
     *     <li>
     *         {@link BrownieSessionController#brownieServerProtector} ->
     *         <ul>
     *          <li>
     *             <b>rss</b> -> launch your java application with "rss" to recreate the server secret <br>
     *                       e.g java -jar Brownie.jar rss
     *             </li>
     *              <li>
     *                  <b>dss</b> -> launch your java application with "dss" to delete the current server secret <br>
     *                       e.g java -jar Brownie.jar dss
     *              </li>
     *              <li>
     *                  <b>dssi</b> -> launch your java application with "dssi" to delete the current server secret and interrupt
     *                        the current workflow of the server <br>
     *                        e.g java -jar Brownie.jar dssi
     *              </li>
     *          </ul>
     *     </li>
     *     <li>
     *         {@link SpringApplication} -> see the allowed arguments <a href="https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html">here</a>
     *     </li>
     * </ul>
     */
    public static void main(String[] args) throws NoSuchAlgorithmException, SaveData {
        brownieServerProtector = new ServerProtector(
                "tecknobit/brownie/backend",
                " to correctly connect or create a session in the Brownie system "
        );
        brownieServerProtector.launch(args);
        SpringApplication.run(Launcher.class, args);
    }

}
