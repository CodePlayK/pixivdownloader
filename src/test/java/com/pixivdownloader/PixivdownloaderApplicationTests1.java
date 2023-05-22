package com.pixivdownloader;

import com.pixivdownloader.core.service.BookMarkListService;
import com.pixivdownloader.core.utils.CookieUtils;
import com.pixivdownloader.core.utils.FilesUtils;
import com.pixivdownloader.core.utils.RequestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

@SpringBootTest
public class PixivdownloaderApplicationTests1 {
    private final Logger LOGGER = LogManager.getLogger();

    private static final String WEB_LOGIN_HEAD = "https://app-api.pixiv.net/web/v1/login?code_challenge=";
    private static final String WEB_LOGIN_END = "&code_challenge_method=S256&client=pixiv-android";
    private static final String CODE_VERIFY = "hpmUY8cTHpw6IQqOHZfJkwj8LGu0zETdyOBxLSMFdx3MndKlQ0cSWPReVkMagLC4tvNWtRUQV.jJCQhYinRdSfNvGfGhR0e1HOSy0jVzqY";
    private static final String REDIRECT_URL = "https://app-api.pixiv.net/web/v1/users/auth/pixiv/callback";
    @Autowired
    private RequestUtils requestUtils;
    @Autowired
    private FilesUtils filesUtils;
    @Autowired
    private CookieUtils cookieUtils;
    @Autowired
    private BookMarkListService bookMarkListService;


    @Test
    void test5() throws IOException {
        HashMap<String, Path> map = new HashMap<>();
        Files.walk(Paths.get("E:\\Pixiv\\RANKING")).filter(Files::isRegularFile)
                .filter(a -> a.getFileName().toString().contains("_p0_"))
                .filter(a -> !a.getFileName().toString().contains("AI"))
                .forEach(
                        a -> map.put(filesUtils.getPicIdbyPath(a.getFileName().toString()), a.getParent())
                );
        System.out.println();


    }// size = 25749

    @Test
    void test4() {
        ResponseEntity<String> response = requestUtils.requestPreset("https://rule34.xxx/index.php?page=favorites&s=view&id=2376350&pid=0", HttpMethod.GET);
        for (int i = 1; i <= 50; i++) {
            String imgSrc = requestUtils.getStingBy3PinIndex(response.getBody(), "<![CDATA[", i, "https://wimg.rule34.xxx/thumbnails//", 1, "\"", 1);
            String favorateId = StringUtils.substringBefore(imgSrc, "/");
            String urlId = StringUtils.substringAfter(imgSrc, "thumbnail_");
            String imgId = StringUtils.substringAfter(imgSrc, "?");
            urlId = StringUtils.substringBefore(urlId, ".");
            String url = "https://ahrimp4.rule34.xxx//images/" + favorateId + "/" + urlId + ".mp4";
            String url_img = "https://rule34.xxx/index.php?page=post&s=view&id=" + imgId;
            ResponseEntity<String> response1 = requestUtils.requestPreset(url_img, HttpMethod.GET);
            String copyright = requestUtils.getStingBy3PinIndex(response1.getBody(), "Copyright", 1, "\">", 3, "<", 1);
            String Character = requestUtils.getStingBy3PinIndex(response1.getBody(), "Character", 1, "\">", 3, "<", 1);
            String Artist = requestUtils.getStingBy3PinIndex(response1.getBody(), "h6>Artist", 1, "\">", 3, "<", 1);
            File file = new File("E:\\Pixiv\\R34\\" + copyright + "_" + Character + "_" + Artist + "_" + imgId + ".mp4");
            ResponseEntity<byte[]> responseEntity = requestUtils.requestStream34Preset(url, HttpMethod.GET);
            try (FileOutputStream out = new FileOutputStream(file)) {
                out.write(Objects.requireNonNull(responseEntity.getBody()), 0, responseEntity.getBody().length);
                out.flush();
            } catch (Exception e) {
            }

        }
        LOGGER.info("test 结束");
    }

    @Test
    void test3() {
        DecimalFormat df = new DecimalFormat("0.00");
        String s = df.format((float) 6789 / 6789 * 100);
        double v = Math.round((6789) / (6789) / 100.0);
        LocalDateTime time = LocalDateTime.now();
        LocalDate date = LocalDate.now();
        LOGGER.warn(
                "P站涩图下载结束!!!{},共收藏:{}张,跳过:{}张,需下载{}张,下载成功:{}成功率:{}",
                date.toString() + time, "");
        //Map<Integer, Integer> map = requestUtils.divideNumByPartNum(3, 12, 4);
        System.out.println();
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

    @Test
    void testDeleteMulti() {
        deleteMulti("E:\\Pixiv\\R18\\");
    }

    void deleteMulti(String path) {
        HashSet<String> set = new HashSet<>();
        File f = new File(path);
        String[] flist = f.list();
        String picid = "";
        for (String s : flist) {
            picid = StringUtils.substringBefore(s, "_") + "_" + StringUtils.substringBetween(s, "_p", "_");
            if (!set.add(picid)) {
                File file = new File(path + StringUtils.substringBeforeLast(s, ".") + ".png");
                File file1 = new File(path + StringUtils.substringBeforeLast(s, ".") + ".jpg");
                System.out.println(file.getPath() + "-->" + file.delete());
                System.out.println(file1.getPath() + "-->" + file1.delete());

            }
        }

    }
}
