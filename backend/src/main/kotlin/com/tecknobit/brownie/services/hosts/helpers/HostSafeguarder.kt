package com.tecknobit.brownie.services.hosts.helpers

import com.tecknobit.apimanager.apis.APIRequest
import com.tecknobit.apimanager.apis.APIRequest.SHA256_ALGORITHM
import com.tecknobit.brownie.services.hosts.dtos.RemoteHostData
import com.tecknobit.brownie.services.hosts.entities.BrownieHost
import com.tecknobit.equinoxcore.annotations.Assembler
import com.tecknobit.equinoxcore.annotations.Returner
import com.tecknobit.equinoxcore.annotations.Wrapper
import com.tecknobit.kassaforte.key.genspec.Algorithm.AES
import com.tecknobit.kassaforte.key.genspec.BlockMode.CBC
import com.tecknobit.kassaforte.key.genspec.EncryptionPadding.PKCS7
import com.tecknobit.kassaforte.key.genspec.KeySize.S256
import com.tecknobit.kassaforte.key.genspec.SymmetricKeyGenSpec
import com.tecknobit.kassaforte.key.usages.KeyPurposes
import com.tecknobit.kassaforte.services.KassaforteSymmetricService
import kotlinx.coroutines.runBlocking

/**
 * The `HostSafeguarder` object allows to handle the sensitive data of a remote host, such as `sshUser`, `sshPassword`, etc...
 *
 * This API is a functional wrapper for Java to handle the Kassaforte's library which works with coroutines and suspendable
 * methods
 *
 * @author N7ghtm4r3 - Tecknobit
 *
 * @since 1.0.4
 */
object HostSafeguarder {

    /**
     * Method used to generate a new secret key for a host
     *
     * @param hostId The identifier of the host
     * @param sessionId The identifier of the session where the host is attached
     */
    @Assembler
    @JvmStatic
    fun generateHostSecretKey(
        hostId: String,
        sessionId: String,
    ) {
        val keyAlias: String = resolveHostSecretKeyAlias(
            hostId = hostId,
            sessionId = sessionId
        )
        try {
            KassaforteSymmetricService.generateKey(
                alias = keyAlias,
                algorithm = AES,
                keyGenSpec = SymmetricKeyGenSpec(
                    keySize = S256,
                    encryptionPadding = PKCS7,
                    blockMode = CBC
                ),
                purposes = KeyPurposes(
                    canEncrypt = true,
                    canDecrypt = true
                )
            )
        } catch (_: IllegalAccessException) {
        }
    }

    /**
     * Method used to safeguard the sensitive data of a remote host
     *
     * @param hostId The identifier of the host
     * @param sessionId The identifier of the session owner of the host
     * @param sshUser The user to use for the SSH connection
     * @param sshPassword The password to use for the SSH connection
     * @param networkInterfaceDetails The details of the network interface of the remote host
     *
     * @return the sensitive data of the remote host encrypted as [RemoteHostData]
     */
    @Wrapper
    @Returner
    @JvmStatic
    fun safeguardRemoteHostData(
        hostId: String,
        sessionId: String,
        sshUser: String,
        sshPassword: String,
        networkInterfaceDetails: Pair<String, String>,
    ): RemoteHostData {
        return safeguardRemoteHostData(
            hostId = hostId,
            sessionId = sessionId,
            sshUser = sshUser,
            sshPassword = sshPassword,
            macAddress = networkInterfaceDetails.first,
            broadcastIp = networkInterfaceDetails.second
        )
    }

    /**
     * Method used to safeguard the sensitive data of a remote host
     *
     * @param hostId The identifier of the host
     * @param sessionId The identifier of the session owner of the host
     * @param sshUser The user to use for the SSH connection
     * @param sshPassword The password to use for the SSH connection
     * @param macAddress    The physical mac address of the remote host network interface
     * @param broadcastIp   The ip address of the remote host network interface
     *
     * @return the sensitive data of the remote host encrypted as [RemoteHostData]
     */
    @Returner
    @JvmStatic
    fun safeguardRemoteHostData(
        hostId: String,
        sessionId: String,
        sshUser: String,
        sshPassword: String,
        macAddress: String,
        broadcastIp: String,
    ): RemoteHostData {
        val keyAlias = resolveHostSecretKeyAlias(hostId, sessionId)
        val encryptedUser = encryptData(
            keyAlias = keyAlias,
            data = sshUser
        )
        val encryptedPassword = encryptData(
            keyAlias = keyAlias,
            data = sshPassword
        )
        val encryptedMacAddress = encryptData(
            keyAlias = keyAlias,
            data = macAddress
        )
        val encryptedBroadcastIp = encryptData(
            keyAlias = keyAlias,
            data = broadcastIp
        )
        return RemoteHostData(
            sshUser = encryptedUser,
            sshPassword = encryptedPassword,
            macAddress = encryptedMacAddress,
            broadcastIp = encryptedBroadcastIp
        )
    }

    /**
     * Method used to encrypt a data of the remote host
     *
     * @param keyAlias The alias of the secret key of the host
     * @param data The data to encrypt
     *
     * @return the data encrypted as [String]
     */
    private fun encryptData(
        keyAlias: String,
        data: String,
    ): String {
        return runBlocking {
            KassaforteSymmetricService.encrypt(
                alias = keyAlias,
                blockMode = CBC,
                padding = PKCS7,
                data = data
            )
        }
    }

    /**
     * Method used to decrypt the remote host data previously encrypted
     *
     * @param host The remote host from decrypt the sensitive data
     *
     * @return the sensitive data of the remote host decrypted as [RemoteHostData]
     */
    @JvmStatic
    fun decryptRemoteHostData(
        host: BrownieHost,
    ): RemoteHostData {
        val hostId = host.id
        val sessionId = host.session.id
        val keyAlias = resolveHostSecretKeyAlias(hostId, sessionId)
        val sshUser = decryptHostData(
            keyAlias = keyAlias,
            encryptedData = host.sshUser
        )
        val sshPassword = decryptHostData(
            keyAlias = keyAlias,
            encryptedData = host.sshPassword
        )
        val macAddress = decryptHostData(
            keyAlias = keyAlias,
            encryptedData = host.macAddress
        )
        val broadcastIp = decryptHostData(
            keyAlias = keyAlias,
            encryptedData = host.broadcastIp
        )
        return RemoteHostData(
            sshUser = sshUser,
            sshPassword = sshPassword,
            macAddress = macAddress,
            broadcastIp = broadcastIp
        )
    }

    /**
     * Method used to decrypt a data of the remote host
     *
     * @param keyAlias The alias of the secret key of the host
     * @param encryptedData The encrypted data to decrypt
     *
     * @return the data decrypted as [String]
     */
    private fun decryptHostData(
        keyAlias: String,
        encryptedData: String,
    ): String {
        return runBlocking {
            KassaforteSymmetricService.decrypt(
                alias = keyAlias,
                blockMode = CBC,
                padding = PKCS7,
                data = encryptedData
            )
        }
    }

    /**
     * Method used to remove the alias of the secret key of a host
     *
     * @param hostId The identifier of the host
     * @param sessionId The identifier of the session owner of the host
     */
    @JvmStatic
    fun removeHostSecretKey(
        hostId: String,
        sessionId: String,
    ) {
        val keyAlias = resolveHostSecretKeyAlias(
            hostId = hostId,
            sessionId = sessionId
        )
        try {
            KassaforteSymmetricService.deleteKey(
                alias = keyAlias
            )
        } catch (_: RuntimeException) {
        }
    }

    /**
     * Method used to resolve the alias of the secret key of a host
     *
     * @param hostId The identifier of the host
     * @param sessionId The identifier of the session owner of the host
     *
     * @return the resolved alias as [String]
     */
    @Returner
    private fun resolveHostSecretKeyAlias(
        hostId: String,
        sessionId: String,
    ): String {
        val aliasData = hostId + sessionId
        return APIRequest.base64Digest(aliasData, SHA256_ALGORITHM)
    }

}