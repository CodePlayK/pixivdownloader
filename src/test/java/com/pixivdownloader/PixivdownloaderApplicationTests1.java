package com.pixivdownloader;

import com.pixivdownloader.core.utils.CookieUtils;
import com.pixivdownloader.core.utils.RequestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@SpringBootTest
public class PixivdownloaderApplicationTests1 {
    private static final String WEB_LOGIN_HEAD = "https://app-api.pixiv.net/web/v1/login?code_challenge=";
    private static final String WEB_LOGIN_END = "&code_challenge_method=S256&client=pixiv-android";
    private static final String CODE_VERIFY = "hpmUY8cTHpw6IQqOHZfJkwj8LGu0zETdyOBxLSMFdx3MndKlQ0cSWPReVkMagLC4tvNWtRUQV.jJCQhYinRdSfNvGfGhR0e1HOSy0jVzqY";
    private static final String REDIRECT_URL = "https://app-api.pixiv.net/web/v1/users/auth/pixiv/callback";
    @Autowired
    private RequestUtils requestUtils;
    @Autowired
    private CookieUtils cookieUtils;

    @Test
    void test3() {
        requestUtils.requestPreset("https://app-api.pixiv.net", HttpMethod.POST);

    }

    @Test
    void test1() {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", "MOBrBDS8blbauoSck0ZfDbtuzpyT");
        map.add("client_secret", "lsACyCD94FhDUtGTXi3QzcFE2uU1hqtDaKeqrdwj");
        map.add("redirect_uri", REDIRECT_URL);
        map.add("code", "PjO3FgG81DoUxuzmXJlOCLV9ZKHzs0x2mCoyBA3Jqso");
        map.add("include_policy", "true");
        map.add("grant_type", "authorization_code");
        map.add("code_verifier", CODE_VERIFY);
        //map.add("get_secure_url", "true");
        //map.add("username", "hakace");
        //map.add("refresh_token", "PjO3FgG81DoUxuzmXJlOCLV9ZKHzs0x2mCoyBA3Jqso");
        StringBuilder builder = new StringBuilder("https://oauth.secure.pixiv.net/auth/token");
        requestUtils.requestPreset("https://app-api.pixiv.net/v1/search/illust?word=gif", HttpMethod.POST);
    }

    @Test
    void test2() {
        String url = getLoginUrl();
        url = "https://accounts.pixiv.net/account-selected";
        MultiValueMap<String, String> map = getLoginBody();
        System.out.println();
    }

    MultiValueMap<String, String> getRefreshTokenBody() {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", "MOBrBDS8blbauoSck0ZfDbtuzpyT");
        map.add("client_secret", "lsACyCD94FhDUtGTXi3QzcFE2uU1hqtDaKeqrdwj");
        map.add("redirect_uri", REDIRECT_URL);
        map.add("code", "Iepywqm59RWhrCwxSDrRHTxexgdByvnf4zgFn1rEVwg");
        map.add("include_policy", "true");
        map.add("grant_type", "authorization_code");
        map.add("code_verifier", CODE_VERIFY);
        return map;
    }

    MultiValueMap<String, String> getLoginBody() {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        return map;
    }

    String getLoginUrl() {
        return WEB_LOGIN_HEAD + "k-PwEqCIbvGHsTa317LXIFvU7cADUos8p8ytpe0yEEI" + WEB_LOGIN_END;
    }
}
