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

// TODO: TO REMOVE THE ALIAS WHEN THE HOST DISCONNECTED OR CHANGE FROM REMOTE TO LOCAL 
object HostSafeguarder {

    @Assembler
    @JvmStatic
    fun generateHostSecretKey(hostId: String, sessionId: String) {
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

    // TODO: 23/10/2025 TO DOCU SINCE
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

    @Returner // TODO: 23/10/2025 TO DOCU SINCE
    private fun resolveHostSecretKeyAlias(
        hostId: String,
        sessionId: String,
    ): String {
        val aliasData = hostId + sessionId
        return APIRequest.base64Digest(aliasData, SHA256_ALGORITHM)
    }

}