package com.stenden.inf2j.alarmering.server.auth.session.sql;

import io.netty.util.CharsetUtil;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Optional;

public class CryptUtils {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String CRYPT_TYPE = "AES";
    private static final String CRYPT_ALGORITHM = "AES/CBC/PKCS5Padding";

    public byte[] encrypt(byte[] data, String key) throws GeneralSecurityException {
        byte[] bkey = make16ByteKey(key);
        byte[] encrypted = encryptWithoutSeal(data, bkey);
        return seal(encrypted, bkey);
    }

    public Optional<byte[]> decrypt(byte[] data, String key) {
        byte[] bkey = make16ByteKey(key);
        return unseal(data, bkey).flatMap(data2 -> {
            try{
                return Optional.of(decryptWithoutSeal(data2, bkey));
            }catch(GeneralSecurityException e){
                return Optional.empty();
            }
        });
    }

    private byte[] hmacSha256(byte[] data, byte[] key) throws GeneralSecurityException {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        mac.init(new SecretKeySpec(key, HMAC_ALGORITHM));
        return mac.doFinal(data);
    }

    private byte[] make16ByteKey(String key) {
        // AES requires a 16 byte key
        try{
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.reset();
            md5.update(key.getBytes(CharsetUtil.UTF_8));
            return md5.digest();
        }catch(NoSuchAlgorithmException e){
            throw new Error("MD5 does not exist on this JRE");
        }
    }

    //TODO: IV should be constant throughout an session. Randomize it between sessions
    private byte[] createIV(int size){
        return new byte[size];
    }

    private byte[] encryptWithoutSeal(byte[] data, byte[] key) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(CRYPT_ALGORITHM);
        SecretKeySpec secretKey = new SecretKeySpec(key, CRYPT_TYPE);
        byte[] iv = createIV(cipher.getBlockSize());

        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));
        return cipher.doFinal(data);
    }

    private byte[] decryptWithoutSeal(byte[] data, byte[] key) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(CRYPT_ALGORITHM);
        SecretKeySpec secretKey = new SecretKeySpec(key, CRYPT_TYPE);
        byte[] iv = createIV(cipher.getBlockSize());

        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
        return cipher.doFinal(data);
    }

    // Anatomy of the result: <length of data><data><hmac of data>
    private byte[] seal(byte[] data, byte[] key) throws GeneralSecurityException {
        int dataLength = data.length;
        byte[] hmac = hmacSha256(data, key);
        ByteBuffer buffer = ByteBuffer.allocate(4 + dataLength + hmac.length);
        buffer.putInt(dataLength);
        buffer.put(data);
        buffer.put(hmac);
        return buffer.array();
    }

    private Optional<byte[]> unseal(byte[] data, byte[] key){
        if(data.length <= 4){
            return Optional.empty();
        }

        ByteBuffer buffer = ByteBuffer.wrap(data);
        int blen = buffer.getInt();
        if(blen <= 0 || blen >= data.length - 4){
            return Optional.empty();
        }

        byte[] innerData = new byte[blen];
        buffer.get(innerData);
        byte[] hmac = new byte[data.length - 4 - blen];
        buffer.get(hmac);

        try{
            byte[] h2 = hmacSha256(innerData, key);
            if(Arrays.equals(h2, hmac)){
                return Optional.of(innerData);
            }else{
                return Optional.empty();
            }
        }catch(Exception e){
            return Optional.empty();
        }
    }
}
