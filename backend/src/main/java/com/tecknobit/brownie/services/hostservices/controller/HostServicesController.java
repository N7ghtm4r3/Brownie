package com.tecknobit.brownie.services.hostservices.controller;

import com.jcraft.jsch.JSchException;
import com.tecknobit.brownie.services.hosts.entities.BrownieHost;
import com.tecknobit.brownie.services.hostservices.entities.BrownieHostService;
import com.tecknobit.brownie.services.hostservices.services.HostServicesService;
import com.tecknobit.brownie.services.shared.controllers.DefaultBrownieController;
import com.tecknobit.equinoxbackend.environment.services.DefaultEquinoxController;
import com.tecknobit.equinoxbackend.environment.services.builtin.controller.EquinoxController;
import com.tecknobit.equinoxcore.annotations.RequestPath;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

import static com.tecknobit.browniecore.ConstantsKt.*;
import static com.tecknobit.browniecore.helpers.BrownieEndpoints.*;
import static com.tecknobit.browniecore.helpers.BrownieInputsValidator.INSTANCE;
import static com.tecknobit.equinoxcore.helpers.CommonKeysKt.*;
import static com.tecknobit.equinoxcore.helpers.InputsValidator.DEFAULT_LANGUAGE;
import static com.tecknobit.equinoxcore.helpers.InputsValidator.WRONG_NAME_MESSAGE;
import static com.tecknobit.equinoxcore.network.EquinoxBaseEndpointsSet.BASE_EQUINOX_ENDPOINT;
import static com.tecknobit.equinoxcore.network.RequestMethod.*;
import static com.tecknobit.equinoxcore.pagination.PaginatedResponse.*;

/**
 * The {@code HostServicesController} class is useful to manage all the {@link BrownieHostService} operations
 *
 * @author N7ghtm4r3 - Tecknobit
 * @see EquinoxController
 * @see DefaultEquinoxController
 * @see DefaultBrownieController
 */
@RestController
@RequestMapping(value = BASE_EQUINOX_ENDPOINT + SESSIONS_KEY + "/{" + IDENTIFIER_KEY + "}/" + HOSTS_KEY +
        "/{" + HOST_IDENTIFIER_KEY + "}/" + SERVICES_KEY)
public class HostServicesController extends DefaultBrownieController {

    /**
     * {@code service} the support service used to manage the services data
     */
    @Autowired
    private HostServicesService service;

    /**
     * Endpoint used to add a service to a host
     *
     * @param sessionId The identifier of the session
     * @param hostId    The identifier of the host
     * @param language  The language of the user who sent the request
     * @param payload   The payload of the request
     *                  <pre>
     *                                       {@code
     *                                               {
     *                                                   "name" : "the name of the service" -> [String],
     *                                                   "configuration": {
     *                                                       "program_arguments": "The arguments of the program" -> [String],
     *                                                       "purge_nohup_out_after_reboot": true/false -> [boolean],
     *                                                       "auto_run_after_host_reboot": true/false -> [boolean]
     *                                                   }
     *                                               }
     *                                       }
     *                                  </pre>
     * @return the response as {@link String}
     */
    @PutMapping
    @RequestPath(path = "/api/v1/sessions/{session_id}/hosts/{host_id}/services", method = PUT)
    public String addService(
            @PathVariable(IDENTIFIER_KEY) String sessionId,
            @PathVariable(HOST_IDENTIFIER_KEY) String hostId,
            @RequestParam(value = LANGUAGE_KEY, required = false, defaultValue = DEFAULT_LANGUAGE) String language,
            @RequestBody Map<String, Object> payload
    ) {
        setSessionLocale(language);
        BrownieHost host = getBrownieHostIfAllowed(sessionId, hostId);
        if (host == null)
            return failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        if (!host.isOnline())
            return failedResponse(WRONG_PROCEDURE_MESSAGE);
        loadJsonHelper(payload);
        String serviceName = jsonHelper.getString(NAME_KEY);
        if (!INSTANCE.isItemNameValid(serviceName))
            return failedResponse(WRONG_NAME_MESSAGE);
        try {
            hostsService.addService(host, serviceName, jsonHelper);
            return successResponse();
        } catch (JSchException e) {
            return failedResponse(SOMETHING_WENT_WRONG_MESSAGE);
        } catch (Exception e) {
            return failedResponse(WRONG_PROCEDURE_MESSAGE);
        }
    }

    /**
     * Endpoint used to get an existing service of a host
     *
     * @param sessionId The identifier of the session
     * @param hostId The identifier of the host
     * @param serviceId The identifier of the service
     * @param language The language of the user who sent the request
     *
     * @return the response as {@link T}
     *
     * @param <T> the type of the response
     */
    @GetMapping(
            path = "/{" + SERVICE_IDENTIFIER_KEY + "}"
    )
    @RequestPath(path = "/api/v1/sessions/{session_id}/hosts/{host_id}/services/{service_id}", method = GET)
    public <T> T getService(
            @PathVariable(IDENTIFIER_KEY) String sessionId,
            @PathVariable(HOST_IDENTIFIER_KEY) String hostId,
            @PathVariable(SERVICE_IDENTIFIER_KEY) String serviceId,
            @RequestParam(value = LANGUAGE_KEY, required = false, defaultValue = DEFAULT_LANGUAGE) String language
    ) {
        setSessionLocale(language);
        BrownieHost host = getBrownieHostIfAllowed(sessionId, hostId);
        BrownieHostService hostService = null;
        if (host != null && host.isOnline())
            hostService = host.getService(serviceId);
        if (hostService == null)
            return (T) failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        return (T) successResponse(hostService);
    }

    /**
     * Endpoint used to edit an existing service to a host
     *
     * @param sessionId The identifier of the session
     * @param hostId The identifier of the host
     * @param serviceId The identifier of the service
     * @param language The language of the user who sent the request
     * @param payload The payload of the request
     *                 <pre>
     *                      {@code
     *                              {
     *                                  "name" : "the name of the service" -> [String],
     *                                  "configuration": {
     *                                      "program_arguments": "The arguments of the program" -> [String],
     *                                      "purge_nohup_out_after_reboot": true/false -> [boolean],
     *                                      "auto_run_after_host_reboot": true/false -> [boolean]
     *                                  }
     *                              }
     *                      }
     *                 </pre>
     *
     * @return the response as {@link String}
     */
    @PatchMapping(
            path = "/{" + SERVICE_IDENTIFIER_KEY + "}"
    )
    @RequestPath(path = "/api/v1/sessions/{session_id}/hosts/{host_id}/services/{service_id}", method = PATCH)
    public String editService(
            @PathVariable(IDENTIFIER_KEY) String sessionId,
            @PathVariable(HOST_IDENTIFIER_KEY) String hostId,
            @PathVariable(SERVICE_IDENTIFIER_KEY) String serviceId,
            @RequestParam(value = LANGUAGE_KEY, required = false, defaultValue = DEFAULT_LANGUAGE) String language,
            @RequestBody Map<String, Object> payload
    ) {
        setSessionLocale(language);
        BrownieHost host = getBrownieHostIfAllowed(sessionId, hostId);
        if (host == null || !host.hasService(serviceId))
            return failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        if (!host.isOnline())
            return failedResponse(WRONG_PROCEDURE_MESSAGE);
        loadJsonHelper(payload);
        String serviceName = jsonHelper.getString(NAME_KEY);
        if (!INSTANCE.isItemNameValid(serviceName))
            return failedResponse(WRONG_NAME_MESSAGE);
        try {
            hostsService.editService(host, serviceId, serviceName, jsonHelper);
            return successResponse();
        } catch (JSchException e) {
            return failedResponse(SOMETHING_WENT_WRONG_MESSAGE);
        } catch (Exception e) {
            return failedResponse(WRONG_PROCEDURE_MESSAGE);
        }
    }

    /**
     * Endpoint used to retrieve the services of a host
     *
     * @param sessionId The identifier of the session
     * @param hostId The identifier of the host
     * @param keywords  The keywords used to filter the results
     * @param statuses  The statuses used to filter the results
     * @param language  The language of the user who sent the request
     * @param page      The page requested
     * @param pageSize  The size of the items to insert in the page
     * @param <T>       the type of the response
     * @return the response as {@link T}
     */
    @GetMapping
    @RequestPath(path = "/api/v1/sessions/{session_id}/hosts/{host_id}/services", method = GET)
    public <T> T getServices(
            @PathVariable(IDENTIFIER_KEY) String sessionId,
            @PathVariable(HOST_IDENTIFIER_KEY) String hostId,
            @RequestParam(name = KEYWORDS_KEY, defaultValue = "", required = false) Set<String> keywords,
            @RequestParam(
                    name = STATUSES_KEY,
                    defaultValue = "RUNNING, STOPPED, REBOOTING",
                    required = false
            ) JSONArray statuses,
            @RequestParam(value = LANGUAGE_KEY, required = false, defaultValue = DEFAULT_LANGUAGE) String language,
            @RequestParam(name = PAGE_KEY, defaultValue = DEFAULT_PAGE_HEADER_VALUE, required = false) int page,
            @RequestParam(name = PAGE_SIZE_KEY, defaultValue = DEFAULT_PAGE_SIZE_HEADER_VALUE, required = false) int pageSize
    ) {
        setSessionLocale(language);
        BrownieHost host = getBrownieHostIfAllowed(sessionId, hostId);
        if (host == null)
            return (T) failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        return (T) successResponse(service.getServices(hostId, keywords, statuses, page, pageSize));
    }

    /**
     * Endpoint used to retrieve the current status of the specified services
     *
     * @param sessionId The identifier of the session
     * @param hostId The identifier of the host
     * @param services The services to retrieve their current status
     * @param language The language of the user who sent the request
     *
     * @return the response as {@link T}
     *
     * @param <T> the type of the response
     */
    @GetMapping(
            path = "/" + STATUS_KEY
    )
    @RequestPath(path = "/api/v1/sessions/{session_id}/hosts/{host_id}/services/status", method = GET)
    public <T> T getServicesStatus(
            @PathVariable(IDENTIFIER_KEY) String sessionId,
            @PathVariable(HOST_IDENTIFIER_KEY) String hostId,
            @RequestParam(name = SERVICES_KEY, defaultValue = "[]") JSONArray services,
            @RequestParam(value = LANGUAGE_KEY, required = false, defaultValue = DEFAULT_LANGUAGE) String language
    ) {
        setSessionLocale(language);
        BrownieHost host = getBrownieHostIfAllowed(sessionId, hostId);
        if (host == null)
            return (T) failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        return (T) successResponse(service.getServicesStatus(services));
    }

    /**
     * Endpoint used to start a service
     *
     * @param sessionId The identifier of the session
     * @param hostId The identifier of the host
     * @param serviceId The identifier of the service
     * @param language The language of the user who sent the request
     *
     * @return the response as {@link String}
     */
    @PatchMapping(
            path = "/{" + SERVICE_IDENTIFIER_KEY + "}" + START_ENDPOINT
    )
    @RequestPath(path = "/api/v1/sessions/{session_id}/hosts/{host_id}/services/{service_id}/start", method = PATCH)
    public String startService(
            @PathVariable(IDENTIFIER_KEY) String sessionId,
            @PathVariable(HOST_IDENTIFIER_KEY) String hostId,
            @PathVariable(SERVICE_IDENTIFIER_KEY) String serviceId,
            @RequestParam(value = LANGUAGE_KEY, required = false, defaultValue = DEFAULT_LANGUAGE) String language
    ) {
        setSessionLocale(language);
        BrownieHost host = getBrownieHostIfAllowed(sessionId, hostId);
        if (host == null)
            return failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        if (!host.isOnline())
            return failedResponse(WRONG_PROCEDURE_MESSAGE);
        BrownieHostService brownieHostService = host.getService(serviceId);
        if (brownieHostService == null || !brownieHostService.isStopped())
            return failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        try {
            service.startService(host, brownieHostService);
        } catch (JSchException e) {
            return failedResponse(SOMETHING_WENT_WRONG_MESSAGE);
        } catch (Exception e) {
            return failedResponse(WRONG_PROCEDURE_MESSAGE);
        }
        return successResponse();
    }

    /**
     * Endpoint used to reboot a service
     *
     * @param sessionId The identifier of the session
     * @param hostId The identifier of the host
     * @param serviceId The identifier of the service
     * @param language The language of the user who sent the request
     *
     * @return the response as {@link String}
     */
    @PatchMapping(
            path = "/{" + SERVICE_IDENTIFIER_KEY + "}" + REBOOT_ENDPOINT
    )
    @RequestPath(path = "/api/v1/sessions/{session_id}/hosts/{host_id}/services/{service_id}/reboot", method = PATCH)
    public String rebootService(
            @PathVariable(IDENTIFIER_KEY) String sessionId,
            @PathVariable(HOST_IDENTIFIER_KEY) String hostId,
            @PathVariable(SERVICE_IDENTIFIER_KEY) String serviceId,
            @RequestParam(value = LANGUAGE_KEY, required = false, defaultValue = DEFAULT_LANGUAGE) String language
    ) {
        setSessionLocale(language);
        BrownieHost host = getBrownieHostIfAllowed(sessionId, hostId);
        if (host == null)
            return failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        if (!host.isOnline())
            return failedResponse(WRONG_PROCEDURE_MESSAGE);
        BrownieHostService brownieHostService = host.getService(serviceId);
        if (brownieHostService == null || !brownieHostService.isRunning())
            return failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        try {
            service.rebootService(host, brownieHostService);
        } catch (JSchException e) {
            return failedResponse(SOMETHING_WENT_WRONG_MESSAGE);
        } catch (Exception e) {
            return failedResponse(WRONG_PROCEDURE_MESSAGE);
        }
        return successResponse();
    }

    /**
     * Endpoint used to stop a service
     *
     * @param sessionId The identifier of the session
     * @param hostId The identifier of the host
     * @param serviceId The identifier of the service
     * @param language The language of the user who sent the request
     *
     * @return the response as {@link String}
     */
    @PatchMapping(
            path = "/{" + SERVICE_IDENTIFIER_KEY + "}" + STOP_ENDPOINT
    )
    @RequestPath(path = "/api/v1/sessions/{session_id}/hosts/{host_id}/services/{service_id}/stop", method = PATCH)
    public String stopService(
            @PathVariable(IDENTIFIER_KEY) String sessionId,
            @PathVariable(HOST_IDENTIFIER_KEY) String hostId,
            @PathVariable(SERVICE_IDENTIFIER_KEY) String serviceId,
            @RequestParam(value = LANGUAGE_KEY, required = false, defaultValue = DEFAULT_LANGUAGE) String language
    ) {
        setSessionLocale(language);
        BrownieHost host = getBrownieHostIfAllowed(sessionId, hostId);
        if (host == null)
            return failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        if (!host.isOnline())
            return failedResponse(WRONG_PROCEDURE_MESSAGE);
        BrownieHostService brownieHostService = host.getService(serviceId);
        if (brownieHostService == null || !brownieHostService.isRunning())
            return failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        try {
            service.stopService(host, brownieHostService);
        } catch (JSchException e) {
            return failedResponse(SOMETHING_WENT_WRONG_MESSAGE);
        } catch (Exception e) {
            return failedResponse(WRONG_PROCEDURE_MESSAGE);
        }
        return successResponse();
    }

    /**
     * Endpoint used to remove a service
     *
     * @param sessionId The identifier of the session
     * @param hostId The identifier of the host
     * @param serviceId The identifier of the service
     * @param language The language of the user who sent the request
     * @param removeFromTheHost Whether the removing include also the removing from the filesystem of the host
     *
     * @return the response as {@link String}
     */
    @DeleteMapping(
            path = "/{" + SERVICE_IDENTIFIER_KEY + "}"
    )
    @RequestPath(path = "/api/v1/sessions/{session_id}/hosts/{host_id}/services/{service_id}", method = DELETE)
    public String removeService(
            @PathVariable(IDENTIFIER_KEY) String sessionId,
            @PathVariable(HOST_IDENTIFIER_KEY) String hostId,
            @PathVariable(SERVICE_IDENTIFIER_KEY) String serviceId,
            @RequestParam(value = LANGUAGE_KEY, required = false, defaultValue = DEFAULT_LANGUAGE) String language,
            @RequestParam(
                    value = REMOVE_FROM_THE_HOST_KEY,
                    required = false,
                    defaultValue = "false"
            ) boolean removeFromTheHost
    ) {
        setSessionLocale(language);
        BrownieHost host = getBrownieHostIfAllowed(sessionId, hostId);
        if (host == null)
            return failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        if (!host.isOnline())
            return failedResponse(WRONG_PROCEDURE_MESSAGE);
        BrownieHostService brownieHostService = host.getService(serviceId);
        if (brownieHostService == null)
            return failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        try {
            service.removeService(host, brownieHostService, removeFromTheHost);
        } catch (JSchException e) {
            return failedResponse(SOMETHING_WENT_WRONG_MESSAGE);
        } catch (Exception e) {
            return failedResponse(WRONG_PROCEDURE_MESSAGE);
        }
        return successResponse();
    }

}
