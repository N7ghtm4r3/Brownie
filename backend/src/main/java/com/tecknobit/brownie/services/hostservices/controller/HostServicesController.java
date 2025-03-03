package com.tecknobit.brownie.services.hostservices.controller;

import com.jcraft.jsch.JSchException;
import com.tecknobit.brownie.services.hosts.entities.BrownieHost;
import com.tecknobit.brownie.services.shared.controllers.DefaultBrownieController;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.tecknobit.browniecore.ConstantsKt.*;
import static com.tecknobit.browniecore.helpers.BrownieInputsValidator.INSTANCE;
import static com.tecknobit.equinoxcore.helpers.CommonKeysKt.IDENTIFIER_KEY;
import static com.tecknobit.equinoxcore.helpers.CommonKeysKt.NAME_KEY;
import static com.tecknobit.equinoxcore.helpers.InputsValidator.WRONG_NAME_MESSAGE;
import static com.tecknobit.equinoxcore.network.EquinoxBaseEndpointsSet.BASE_EQUINOX_ENDPOINT;

@RestController
@RequestMapping(value = BASE_EQUINOX_ENDPOINT + SESSIONS_KEY + "/{" + IDENTIFIER_KEY + "}/" + HOSTS_KEY +
        "/{" + HOST_IDENTIFIER_KEY + "}/" + SERVICES_KEY)
public class HostServicesController extends DefaultBrownieController {

    @PutMapping
    public String addService(
            @PathVariable(IDENTIFIER_KEY) String sessionId,
            @PathVariable(HOST_IDENTIFIER_KEY) String hostId,
            @RequestBody Map<String, Object> payload
    ) {
        BrownieHost brownieHost = getBrownieHostIfAllowed(sessionId, hostId);
        if (brownieHost == null)
            return failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        if (!brownieHost.isOnline())
            return failedResponse(WRONG_PROCEDURE_MESSAGE);
        loadJsonHelper(payload);
        String serviceName = jsonHelper.getString(NAME_KEY);
        if (!INSTANCE.isItemNameValid(serviceName))
            return failedResponse(WRONG_NAME_MESSAGE);
        try {
            hostsService.addService(brownieHost, serviceName, jsonHelper);
            return successResponse();
        } catch (JSchException e) {
            return failedResponse(SOMETHING_WENT_WRONG_MESSAGE);
        } catch (Exception e) {
            return failedResponse(WRONG_PROCEDURE_MESSAGE);
        }
    }

    @PatchMapping(
            path = "/{" + SERVICE_IDENTIFIER_KEY + "}"
    )
    public String editService(
            @PathVariable(IDENTIFIER_KEY) String sessionId,
            @PathVariable(HOST_IDENTIFIER_KEY) String hostId,
            @PathVariable(SERVICE_IDENTIFIER_KEY) String serviceId,
            @RequestBody Map<String, Object> payload
    ) {
        BrownieHost brownieHost = getBrownieHostIfAllowed(sessionId, hostId);
        if (brownieHost == null || !brownieHost.hasService(serviceId))
            return failedResponse(NOT_AUTHORIZED_OR_WRONG_DETAILS_MESSAGE);
        if (!brownieHost.isOnline())
            return failedResponse(WRONG_PROCEDURE_MESSAGE);
        loadJsonHelper(payload);
        String serviceName = jsonHelper.getString(NAME_KEY);
        if (!INSTANCE.isItemNameValid(serviceName))
            return failedResponse(WRONG_NAME_MESSAGE);
        try {
            hostsService.editService(brownieHost, serviceId, serviceName, jsonHelper);
            return successResponse();
        } catch (JSchException e) {
            return failedResponse(SOMETHING_WENT_WRONG_MESSAGE);
        } catch (Exception e) {
            return failedResponse(WRONG_PROCEDURE_MESSAGE);
        }
    }

}
