package com.tecknobit.brownie.events;

import com.tecknobit.equinoxbackend.events.EquinoxApplicationEvent;

// TODO: 06/05/2025 TO DOCU
public class BrownieApplicationEvent extends EquinoxApplicationEvent<BrownieApplicationEventType> {

    public BrownieApplicationEvent(Object source, BrownieApplicationEventType eventType, Object... extra) {
        super(source, eventType, extra);
    }

}
