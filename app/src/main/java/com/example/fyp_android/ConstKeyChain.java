package com.example.fyp_android;

import com.facebook.android.crypto.keychain.AndroidConceal;
import com.facebook.crypto.CryptoConfig;
import com.facebook.crypto.exception.KeyChainException;
import com.facebook.crypto.keychain.KeyChain;

import java.util.Arrays;

public class ConstKeyChain implements KeyChain {

    private final CryptoConfig mConfig;
    private byte[] mKey;

    public ConstKeyChain(byte[] key, CryptoConfig config) {
        if (key.length != config.keyLength) {
            throw new IllegalStateException("key must be the same length as the config states");
        }
        mConfig = config;
        mKey = key;
    }

    @Override
    public byte[] getCipherKey() throws KeyChainException {
        return mKey;
    }

    @Override
    public byte[] getMacKey() throws KeyChainException {
        throw new UnsupportedOperationException("implemented only for encryption, not mac");
    }

    // this is just a glorified "get me a new nonce"
    public byte[] getNewIV() {
        byte[] result = new byte[mConfig.ivLength];
        AndroidConceal.get().secureRandom.nextBytes(result);
        return result;
    }

    @Override
    public void destroyKeys() {
        Arrays.fill(mKey, (byte) 0);
        mKey = null;
    }
}