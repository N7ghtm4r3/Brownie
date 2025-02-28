package com.tecknobit.brownie.services.session.service;

import com.tecknobit.apimanager.apis.APIRequest;
import com.tecknobit.brownie.services.session.entity.BrownieSession;
import com.tecknobit.brownie.services.session.repository.BrownieSessionsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;

import static com.tecknobit.apimanager.apis.APIRequest.SHA256_ALGORITHM;

@Service
public class BrownieSessionsService {

    @Autowired
    private BrownieSessionsRepository sessionsRepository;

    public void createSession(String sessionId, String joinCode, String password) throws NoSuchAlgorithmException {
        password = hash(password);
        sessionsRepository.save(new BrownieSession(
                sessionId,
                joinCode,
                password
        ));
    }

    public BrownieSession connectToSession(String joinCode, String password) throws NoSuchAlgorithmException {
        password = hash(password);
        return sessionsRepository.validateSessionConnectionAttempt(joinCode, password);
    }

    public BrownieSession getBrownieSession(String sessionId) {
        return sessionsRepository.findById(sessionId).orElse(null);
    }

    public BrownieSession getBrownieSession(String sessionId, String password) throws NoSuchAlgorithmException {
        BrownieSession session = sessionsRepository.findById(sessionId).orElse(null);
        if (session == null)
            return null;
        password = hash(password);
        if (!session.getPassword().equals(password))
            return null;
        return session;
    }

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
