package nguyenduonghuy.usermanagement.constant;

import java.util.Arrays;
import java.util.List;

public class SecurityConstant {
	public static final long EXPIRATION_TIME = 432_000_000; // 5 days expessed in millisecond
	public static final String TOKEN_PREFIX = "Bearer ";
	public static final String JWT_TOKEN_HEADER = "Jwt-Token";
	public static final String TOKEN_CANNOT_BE_VERIFIED = "Token cannot be verified";
	public static final String NGUYEN_DUONG_HUY = "Nguyen Duong Huy";
	public static final String ADMINISTRATION = "User Management Portal";
	public static final String AUTHORITIES = "authorities";
	public static final String FORBIDEN_MESSAGE = "You need to login to access this page";
	public static final String ACCESS_DENIED_MESSAGE = "You do not have permission to access this page";
	public static final String OPTIONS_HTTP_METHOD = "OPTIONS";
//	public static final List<String> PUBLIC_URL = Arrays.asList("/api/users/login", "/api/users/register", "/api/users/reset-password/**", "/api/users/image/**");
	public static final List<String> PUBLIC_URL = Arrays.asList("**");
	public static final String ERROR_PATH = "/error";
}
