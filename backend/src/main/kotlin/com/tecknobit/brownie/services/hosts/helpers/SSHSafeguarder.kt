package com.tecknobit.brownie.services.hosts.helpers

import com.tecknobit.apimanager.apis.APIRequest
import com.tecknobit.apimanager.apis.APIRequest.SHA256_ALGORITHM
import com.tecknobit.brownie.services.hosts.dtos.RemoteHostData
import com.tecknobit.equinoxcore.annotations.Assembler
import com.tecknobit.equinoxcore.annotations.Returner
import com.tecknobit.kassaforte.key.genspec.Algorithm.AES
import com.tecknobit.kassaforte.key.genspec.BlockMode.CBC
import com.tecknobit.kassaforte.key.genspec.EncryptionPadding.PKCS7
import com.tecknobit.kassaforte.key.genspec.KeySize.S256
import com.tecknobit.kassaforte.key.genspec.SymmetricKeyGenSpec
import com.tecknobit.kassaforte.key.usages.KeyPurposes
import com.tecknobit.kassaforte.services.KassaforteSymmetricService
import kotlinx.coroutines.runBlocking

object SSHSafeguarder {

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
    @Returner
    @JvmStatic
    fun safeguardRemoteHostData(
        hostId: String,
        sessionId: String,
        sshUser: String,
        sshPassword: String,
        networkInterfaceDetails: Pair<String, String>,
    ): RemoteHostData {
        val keyAlias = resolveHostSecretKeyAlias(hostId, sessionId)
        val encryptedUser = encryptCredential(
            keyAlias = keyAlias,
            data = sshUser
        )
        val encryptedPassword = encryptCredential(
            keyAlias = keyAlias,
            data = sshPassword
        )
        val encryptedMacAddress = encryptCredential(
            keyAlias = keyAlias,
            data = networkInterfaceDetails.first
        )
        val encryptedBroadcastIp = encryptCredential(
            keyAlias = keyAlias,
            data = networkInterfaceDetails.second
        )
        return RemoteHostData(
            sshUser = encryptedUser,
            sshPassword = encryptedPassword,
            macAddress = encryptedMacAddress,
            broadcastIp = encryptedBroadcastIp
        )
    }

    private fun encryptCredential(
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

    @Returner // TODO: 23/10/2025 TO DOCU SINCE
    @JvmStatic
    fun resolveHostSecretKeyAlias(
        hostId: String,
        sessionId: String,
    ): String {
        val aliasData = hostId + sessionId
        return APIRequest.base64Digest(aliasData, SHA256_ALGORITHM)
    }

}