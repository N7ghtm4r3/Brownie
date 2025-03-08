package com.tecknobit.brownie.services.session.repository;

import com.tecknobit.brownie.services.session.entity.BrownieSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import static com.tecknobit.browniecore.ConstantsKt.JOIN_CODE_KEY;
import static com.tecknobit.browniecore.ConstantsKt.SESSIONS_KEY;
import static com.tecknobit.equinoxbackend.environment.services.builtin.service.EquinoxItemsHelper._WHERE_;
import static com.tecknobit.equinoxcore.helpers.CommonKeysKt.PASSWORD_KEY;

/**
 * The {@code BrownieSessionsRepository} interface is useful to manage the queries of the {@link BrownieSession}
 *
 * @author N7ghtm4r3 - Tecknobit
 * @see JpaRepository
 */
@Repository
public interface BrownieSessionsRepository extends JpaRepository<BrownieSession, String> {

    /**
     * Query used to validate the access attempt of session connection
     *
     * @param joinCode The join code to connect to the session
     * @param password The password used to protect the session accesses
     * @return the related session if authorized as {@link BrownieSession}, null otherwise
     */
    @Query(
            value = "SELECT * FROM " + SESSIONS_KEY + _WHERE_ +
                    JOIN_CODE_KEY + "=:" + JOIN_CODE_KEY + " AND " +
                    PASSWORD_KEY + "=:" + PASSWORD_KEY,
            nativeQuery = true
    )
    BrownieSession validateSessionConnectionAttempt(
            @Param(JOIN_CODE_KEY) String joinCode,
            @Param(PASSWORD_KEY) String password
    );

}
