/*
 *  Copyright (c) 2023 TELUS Communications Inc.,
 *  All Rights Reserved.
 *
 *  This document contains proprietary information that shall be
 *  distributed or routed only within TELUS, and its authorized
 *  clients, except with written permission of TELUS.
 *
 * $Id$
 */


package com.telus.cis.common.core.utils;

import static com.telus.cis.common.core.utils.ApiUtils.validateSecretVariable;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.springframework.http.HttpStatus;

import com.telus.cis.common.core.domain.KeyStoreConfig;
import com.telus.cis.common.core.exception.ApiError;
import com.telus.cis.common.core.exception.ApiException;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class EncrypterDecrypter
{
    private Cipher decryptCipher;
    private Cipher encryptCipher;



    public EncrypterDecrypter(KeyStoreConfig config)
    {
        try (InputStream inputStream = new FileInputStream( config.getKeyStorePath() )) {
            
            String keyAlias = validateSecretVariable( config.getKeyAlias() );
            String keyPassword = validateSecretVariable( config.getKeyPassword() );
            String keyStorePassword = validateSecretVariable( config.getKeyStorePassword() );
            
            KeyStore keystore = KeyStore.getInstance( config.getKeyStoreType() );
            keystore.load( inputStream, keyStorePassword.toCharArray() );

            PrivateKey privateKey = (PrivateKey) keystore.getKey( keyAlias, keyPassword.toCharArray() );
            log.debug( "private key in use: [{}]", privateKey );
            decryptCipher = Cipher.getInstance( "RSA" );
            decryptCipher.init( Cipher.DECRYPT_MODE, privateKey );

            PublicKey publicKey = keystore.getCertificate( keyAlias ).getPublicKey();
            log.debug( "public key in use: [{}]", publicKey );
            encryptCipher = Cipher.getInstance( "RSA" );
            encryptCipher.init( Cipher.ENCRYPT_MODE, publicKey );

        }
        catch ( KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException | UnrecoverableKeyException
                | NoSuchPaddingException | InvalidKeyException ex ) {
            log.warn( "EncrypterDecrypter exception {}", ex.getClass().getSimpleName() );
            log.error( "EncrypterDecrypter error", ex );
            ApiError apiError = ApiError.builder()
                                    .code( HttpStatus.INTERNAL_SERVER_ERROR.toString() )
                                    .status( HttpStatus.INTERNAL_SERVER_ERROR.value() )
                                    .reason( HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase() )
                                    .message( ex.getLocalizedMessage() ).build();
            throw new ApiException( apiError, ex );
        }
    }


    public synchronized String decrypt(String content)
    {
        byte[] decryptedBytes;
        try {
            decryptedBytes = decryptCipher.doFinal( new BigInteger( content, 16 ).toByteArray() );
            return new String( decryptedBytes, StandardCharsets.UTF_8 );
        }
        catch ( IllegalBlockSizeException | BadPaddingException ex ) {
            ApiError apiError = ApiError.builder()
                                    .code( HttpStatus.INTERNAL_SERVER_ERROR.toString() )
                                    .status( HttpStatus.INTERNAL_SERVER_ERROR.value() )
                                    .reason( HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase() )
                                    .message( ex.getLocalizedMessage() ).build();
            throw new ApiException( apiError, ex );
        }
    }


    public synchronized String encrypt(String content)
    {
        byte[] encryptedBytes;
        try {
            encryptedBytes = encryptCipher.doFinal( content.getBytes() );
            return new BigInteger( encryptedBytes ).toString( 16 );
        }
        catch ( IllegalBlockSizeException | BadPaddingException ex ) {
            ApiError apiError = ApiError.builder().code( HttpStatus.INTERNAL_SERVER_ERROR.toString() )
                    .status( HttpStatus.INTERNAL_SERVER_ERROR.value() ).reason( HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase() )
                    .message( ex.getLocalizedMessage() ).build();
            throw new ApiException( apiError, ex );
        }
    }

}