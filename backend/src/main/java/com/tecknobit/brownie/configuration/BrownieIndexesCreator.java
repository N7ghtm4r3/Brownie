package com.tecknobit.brownie.configuration;

import com.tecknobit.equinoxbackend.configuration.IndexesCreator;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.tecknobit.browniecore.ConstantsKt.*;
import static com.tecknobit.equinoxcore.helpers.CommonKeysKt.NAME_KEY;

@Component
public class BrownieIndexesCreator extends IndexesCreator {

    @Override
    @PostConstruct
    public void createIndexes() {
        createFullTextIndex(HOSTS_KEY, "name_address_idx", List.of(NAME_KEY, HOST_ADDRESS_KEY));
        createFullTextIndex(SERVICES_KEY, "name_pid_idx", List.of(NAME_KEY, PID_KEY));
    }

}
