package com.tecknobit.brownie.services.session.service;

import com.tecknobit.apimanager.apis.APIRequest;
import com.tecknobit.brownie.services.session.entity.BrownieSession;
import com.tecknobit.brownie.services.session.repository.BrownieSessionsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;

import static com.tecknobit.apimanager.apis.APIRequest.SHA256_ALGORITHM;

/**
 * The {@code BrownieSessionsService} class is useful to manage all the {@link BrownieSession} database operations
 *
 * @author N7ghtm4r3 - Tecknobit
 */
@Service
public class BrownieSessionsService {

    /**
     * {@code sessionsRepository} instance used to access to the {@link SESSIONS_KEY} table
     */
    @Autowired
    private BrownieSessionsRepository sessionsRepository;

    /**
     * Method used to create a new session
     *
     * @param sessionId The identifier of the new created session
     * @param joinCode  The join code to connect to the session
     * @param password  The password used to protect the session accesses
     * @return the instance of the new created session as {@link BrownieSession}
     */
    public BrownieSession createSession(String sessionId, String joinCode, String password) throws NoSuchAlgorithmException {
        password = hash(password);
        BrownieSession session = new BrownieSession(sessionId, joinCode, password);
        sessionsRepository.save(session);
        return session;
    }

    /**
     * Method used to connect to an existing session
     *
     * @param joinCode The join code to connect to the session
     * @param password The password used to protect the session accesses
     *
     * @return the instance of the existing session as {@link BrownieSession}, null if not authorized
     */
    public BrownieSession connectToSession(String joinCode, String password) throws NoSuchAlgorithmException {
        password = hash(password);
        return sessionsRepository.validateSessionConnectionAttempt(joinCode, password);
    }

    /**
     * Method to get an existing Brownie's session
     *
     * @param sessionId The identifier of the session to retrieve
     *
     * @return the instance of the existing session as {@link BrownieSession}, null if not exists
     */
    public BrownieSession getBrownieSession(String sessionId) {
        return sessionsRepository.findById(sessionId).orElse(null);
    }

    /**
     * Method to get an existing Brownie's session to validate its deletion
     *
     * @param sessionId The identifier of the session to retrieve
     * @param password The password used to protect the session accesses
     *
     * @return the instance of the existing session as {@link BrownieSession}, null if authorized
     */
    public BrownieSession getBrownieSession(String sessionId, String password) throws NoSuchAlgorithmException {
        BrownieSession session = sessionsRepository.findById(sessionId).orElse(null);
        if (session == null)
            return null;
        password = hash(password);
        if (!session.getPassword().equals(password))
            return null;
        return session;
    }

    /**
     * Method used to delete an existing session
     *
     * @param sessionId The identifier of the session to delete
     */
    public void deleteSession(String sessionId) {
        sessionsRepository.deleteById(sessionId);
    }

    /**
     * Method to hash a sensitive user data
     *
     * @param secret The user value to hash
     * @throws NoSuchAlgorithmException when the hash of the user value fails
     */
    protected String hash(String secret) throws NoSuchAlgorithmException {
        return APIRequest.base64Digest(secret, SHA256_ALGORITHM);
    }

}
