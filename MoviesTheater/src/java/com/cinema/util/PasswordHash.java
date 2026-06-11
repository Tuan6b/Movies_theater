package com.cinema.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordHash {

    private static final String ALGORITHM = "SHA-256";

    public static String hash(String password) {
        try {
            byte[] salt = generateSalt();
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            md.update(salt);
            byte[] hashBytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
            byte[] hashWithSalt = new byte[salt.length + hashBytes.length];
            System.arraycopy(salt, 0, hashWithSalt, 0, salt.length);
            System.arraycopy(hashBytes, 0, hashWithSalt, salt.length, hashBytes.length);
            return Base64.getEncoder().encodeToString(hashWithSalt);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    public static boolean verify(String password, String storedHash) {
        try {
            byte[] hashWithSalt = Base64.getDecoder().decode(storedHash);
            if (hashWithSalt.length <= 16) {
                return false;
            }
            byte[] salt = new byte[16];
            byte[] storedHashBytes = new byte[hashWithSalt.length - 16];
            System.arraycopy(hashWithSalt, 0, salt, 0, 16);
            System.arraycopy(hashWithSalt, 16, storedHashBytes, 0, storedHashBytes.length);

            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            md.update(salt);
            byte[] computedHash = md.digest(password.getBytes(StandardCharsets.UTF_8));

            return MessageDigest.isEqual(computedHash, storedHashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        } catch (Exception e) {
            return false;
        }
    }

    private static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return salt;
    }
}
