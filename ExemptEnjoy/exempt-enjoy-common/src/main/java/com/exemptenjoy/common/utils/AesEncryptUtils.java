package com.exemptenjoy.common.utils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AesEncryptUtils
{
    private static final String AES_ALGORITHM = "AES";
    private static final String AES_TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String DEFAULT_KEY = "ExemptEnjoy2026!";
    private static final String DEFAULT_IV = "1234567890abcdef";

    public static String encrypt(String data)
    {
        return encrypt(data, DEFAULT_KEY, DEFAULT_IV);
    }

    public static String decrypt(String data)
    {
        return decrypt(data, DEFAULT_KEY, DEFAULT_IV);
    }

    public static String encrypt(String data, String key, String iv)
    {
        try
        {
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), AES_ALGORITHM);
            IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8));
            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
            byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        }
        catch (Exception e)
        {
            throw new RuntimeException("加密失败", e);
        }
    }

    public static String decrypt(String data, String key, String iv)
    {
        try
        {
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), AES_ALGORITHM);
            IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8));
            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(data));
            return new String(decrypted, StandardCharsets.UTF_8);
        }
        catch (Exception e)
        {
            throw new RuntimeException("解密失败", e);
        }
    }
}