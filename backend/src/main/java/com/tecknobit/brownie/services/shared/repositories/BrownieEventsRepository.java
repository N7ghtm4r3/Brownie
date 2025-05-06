package com.tecknobit.brownie.services.shared.repositories;

import com.tecknobit.brownie.services.shared.entities.BrownieEvent;
import com.tecknobit.equinoxcore.annotations.Structure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@Structure
@NoRepositoryBean
public interface BrownieEventsRepository<E extends BrownieEvent> extends JpaRepository<E, String> {

    Long getLastUpEvent(String eventOwnerId);

    void registerEvent(String eventId, String type, long eventDate, String eventOwnerId);

    void registerEvent(String eventId, String type, long eventDate, String extra, String eventOwnerId);

}
