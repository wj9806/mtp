package com.mtp.core.util;

import com.mtp.core.tp.MtpException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public final class Md5Util {

    private Md5Util() {
    }

    public static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new MtpException("MD5 calculation failed", e);
        }
    }

    public static String generateInstanceId(String applicationName, String ip, Integer port) {
        String key = applicationName + "@" + ip + ":" + port;
        return md5(key);
    }
}