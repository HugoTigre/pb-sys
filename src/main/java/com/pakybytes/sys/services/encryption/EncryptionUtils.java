package com.pakybytes.sys.services.encryption;

import org.abstractj.kalium.crypto.SecretBox;
import org.abstractj.kalium.encoders.Encoder;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.Map;

import static org.abstractj.kalium.NaCl.Sodium.CRYPTO_SECRETBOX_XSALSA20POLY1305_KEYBYTES;


/**
 * Encryption/Decryption/Hashing helper methods.
 * <p>
 * Note that for every service, there should be specified a different crypto service with it's own keys.
 * Keeping distinct keys per service is known as the "key separation principle".
 * If same key is used, then an attacker can cross reference between the encrypted values and reconstruct the key.
 * This rule applies even if you are sharing the same key for hashing and encryption.
 * <p>
 * <b>Warning</b>: this service as <a href="https://github.com/abstractj/kalium">kalium</a> as a dependency,
 * which itself needs <a href="https://github.com/jedisct1/libsodium">libsodium</a> instaled
 * directly in the operating system for this to work. For more on this please read
 * the Kalium documentation.
 * </p>
 *
 * @author Hugo Tigre
 * @since 1.0.2, 2017/11/08
 */
public class EncryptionUtils {

    private final SecureRandom random;

    /**
     * Use this if random nonce per request is overkill
     */
    private final byte[] repetableNonce;

    private Encoder encoder = Encoder.HEX;


    public EncryptionUtils() {
        random = new SecureRandom();
        repetableNonce = "965478932154856987652145".getBytes(StandardCharsets.UTF_8);
    }


    private SecretBox box(byte[] secretKey) {
        return new SecretBox(secretKey);
    }


    /**
     * Creates and returns a new Secret Key
     *
     * @return the new key
     */
    public byte[] newSecretKey() {
        // Key must be 32 bytes for secretbox
        byte[] buf = new byte[CRYPTO_SECRETBOX_XSALSA20POLY1305_KEYBYTES];
        random.nextBytes(buf);
        return buf;
    }


    /**
     * Encrypts a String.
     * Uses a different Nonce for every encryption.
     *
     * @param secretKey The secret key
     * @param data      The object to be encrypted
     * @return A Map with the used nonce and the encrypted String
     */
    public Map<String, String> encryptWithNonce(byte[] secretKey,
                                                String data) {

        Nonce nonce = new Nonce.Builder().build();

        byte[] rawData = data.getBytes(StandardCharsets.UTF_8);
        byte[] cipherText = box(secretKey).encrypt(nonce.getRaw(), rawData);

        String nonceHex = encoder.encode(nonce.getRaw());
        String cipherHex = encoder.encode(cipherText);

        return Collections.singletonMap(nonceHex, cipherHex);
    }


    /**
     * Encrypts a String.
     * Always uses this class fixed {@link EncryptionUtils#repetableNonce}
     *
     * @param secretKey The secret Key
     * @param data      The object to be encrypted
     * @return The encrypted String
     */
    public String encrypt(byte[] secretKey,
                          String data) {

        byte[] rawData = data.getBytes(StandardCharsets.UTF_8);
        byte[] cipherText = box(secretKey).encrypt(repetableNonce, rawData);

        return encoder.encode(cipherText);
    }


    /**
     * Decrypts a String.
     *
     * @param secretKey The secret Key
     * @param data      A Map with the Nonce value and the encrypted String
     */
    public String decryptWithNonce(byte[] secretKey,
                                   Map<String, String> data) {

        Map.Entry<String, String> entry =
                data.entrySet().iterator().next();

        Nonce nonce = new Nonce.Builder()
                .fromBytes(encoder.decode(entry.getKey()))
                .build();

        String cipherTextHex = data.get(entry.getValue());
        byte[] cipherText = encoder.decode(cipherTextHex);
        byte[] rawData = box(secretKey).decrypt(nonce.getRaw(), cipherText);

        return new String(rawData, StandardCharsets.UTF_8);
    }


    /**
     * Decrypts a String.
     * Always uses this class fixed {@link EncryptionUtils#repetableNonce}, as when encrypting with {@link EncryptionUtils#encrypt}.
     *
     * @param secretKey The secret Key
     * @param data      The String to decrypt
     * @return The decrypted String
     */
    public String decrypt(byte[] secretKey, String data) {

        byte[] cipherText = encoder.decode(data);
        byte[] rawData = box(secretKey).decrypt(repetableNonce, cipherText);
        return new String(rawData, StandardCharsets.UTF_8);
    }
}
