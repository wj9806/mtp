package com.mtp.config.center.auth;

import com.mtp.config.center.util.JwtUtil;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

public class WebUtil {

    public static HttpServletRequest currentRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return Objects.requireNonNull(attributes).getRequest();
    }

    public static String getHeader(String name) {
        return currentRequest().getHeader(name);
    }

    public static String getBearerToken() {
        return getHeader("Authorization").replace("Bearer ", "");
    }

    public static String currentUsername() {
        return JwtUtil.getUsernameFromToken(getBearerToken());
    }
}
