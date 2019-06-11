package com.pakybytes.sys.services.encryption;

import org.abstractj.kalium.crypto.Random;

import static org.abstractj.kalium.NaCl.Sodium.CRYPTO_SECRETBOX_XSALSA20POLY1305_NONCEBYTES;


/**
 * A Nonce is used to ensure that encryption is completely random.
 * They should be generated once per encryption.
 * <p>
 * Nonce's can be displayed, they are not confidential but
 * should never be reused, ever.
 *
 * @author Hugo Tigre
 */
public class Nonce {

    private static Random random = new Random(); // No real advantage over java.secure.SecureRandom
    private byte[] raw;


    private Nonce(byte[] raw) {
        this.raw = raw;
    }


    public byte[] getRaw() {
        return this.raw;
    }


    public static class Builder {

        private byte[] bytes;


        /**
         * Set the bytes of the Nonce, if not specified, a random set of 24 bytes
         * will be generated.
         */
        public Builder fromBytes(byte[] data) {

            if (data == null) throw new NullPointerException();

            if (data.length != CRYPTO_SECRETBOX_XSALSA20POLY1305_NONCEBYTES) {
                throw new IllegalArgumentException("This nonce has an invalid size: " + data.length);
            }

            bytes = data;
            return this;
        }


        /**
         * Creates a random nonce value of 24 bytes.
         */
        public Nonce build() {
            return bytes == null ?
                    new Nonce(random.randomBytes(CRYPTO_SECRETBOX_XSALSA20POLY1305_NONCEBYTES)) :
                    new Nonce(bytes);
        }
    }
}
