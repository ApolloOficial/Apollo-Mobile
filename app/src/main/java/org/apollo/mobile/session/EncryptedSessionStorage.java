package org.apollo.mobile.session;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public final class EncryptedSessionStorage implements SessionStorage {

    private static final String PREFERENCES_NAME = "auth_session";
    private static final String SESSION_PAYLOAD_KEY = "encrypted_session";
    private static final String KEYSTORE_PROVIDER = "AndroidKeyStore";
    private static final String KEY_ALIAS = "apollo_mobile_auth_session";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";

    private final SharedPreferences sharedPreferences;
    private final Gson gson = new Gson();

    public EncryptedSessionStorage(Context context) {
        sharedPreferences = context.getApplicationContext()
                .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public synchronized UserSession read() {
        String encryptedPayload = sharedPreferences.getString(SESSION_PAYLOAD_KEY, null);
        if (encryptedPayload == null) {
            return null;
        }

        try {
            return gson.fromJson(decrypt(encryptedPayload), UserSession.class);
        } catch (RuntimeException | GeneralSecurityException exception) {
            clear();
            return null;
        }
    }

    @Override
    public synchronized void save(UserSession session) {
        try {
            String encryptedPayload = encrypt(gson.toJson(session));
            sharedPreferences.edit().putString(SESSION_PAYLOAD_KEY, encryptedPayload).apply();
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Não foi possível armazenar a sessão de login", exception);
        }
    }

    @Override
    public synchronized void clear() {
        sharedPreferences.edit().remove(SESSION_PAYLOAD_KEY).apply();
    }

    private String encrypt(String value) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey());
        byte[] encryptedBytes = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
        return Base64.encodeToString(cipher.getIV(), Base64.NO_WRAP)
                + "."
                + Base64.encodeToString(encryptedBytes, Base64.NO_WRAP);
    }

    private String decrypt(String encryptedPayload) throws GeneralSecurityException {
        String[] parts = encryptedPayload.split("\\.");
        if (parts.length != 2) {
            throw new GeneralSecurityException("Payload da sessão criptografada é inválido");
        }

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(
                Cipher.DECRYPT_MODE,
                getOrCreateKey(),
                new javax.crypto.spec.GCMParameterSpec(128, Base64.decode(parts[0], Base64.NO_WRAP))
        );
        return new String(cipher.doFinal(Base64.decode(parts[1], Base64.NO_WRAP)), StandardCharsets.UTF_8);
    }

    private SecretKey getOrCreateKey() throws GeneralSecurityException {
        KeyStore keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER);
        try {
            keyStore.load(null);
        } catch (java.io.IOException exception) {
            throw new GeneralSecurityException("Não foi possível carregar Android Keystore", exception);
        }

        if (keyStore.containsAlias(KEY_ALIAS)) {
            return ((KeyStore.SecretKeyEntry) keyStore.getEntry(KEY_ALIAS, null)).getSecretKey();
        }

        KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER);
        keyGenerator.init(new KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT
        )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build());
        return keyGenerator.generateKey();
    }
}
