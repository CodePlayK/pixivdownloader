package com.pixivdownloader.core.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pixivdownloader.core.constance.EntityPreset;
import com.pixivdownloader.core.entity.Cookie;
import com.pixivdownloader.core.entity.DecryptedCookie;
import com.pixivdownloader.core.entity.EncryptedCookie;
import com.sun.jna.platform.win32.Crypt32Util;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
import java.sql.*;
import java.util.Date;
import java.util.*;


/**
 * Cookie工具箱
 *
 * @author hakace
 * @date 2022/10/27
 */
@Component
@Data
public class CookieUtils {
    private static CookieUtils cookieUtils = new CookieUtils();
    private final Logger LOGGER = LogManager.getLogger();
    private final String chromeKeyringPassword = null;
    File cookieStoreCopy = new File(".cookies.db");
    private String PHPSESSID;
    private String COOKIES_ALL;
    private String DEVICETOKEN;
    private String USERID;
    private byte[] windowsMasterKey;
    private InetSocketAddress inetSocketAddress;

    private CookieUtils() {

    }

    public static CookieUtils getInstance() {
        return cookieUtils;
    }

    /**
     * 获取系统代理
     */
    private void getSysProxy() {
        inetSocketAddress = new InetSocketAddress(0);
        System.setProperty("java.net.useSystemProxies", "true");
        List<Proxy> l = null;
        try {
            l = ProxySelector.getDefault().select(new URI("https://www.pixiv.net/"));
        } catch (URISyntaxException e) {
            LOGGER.error(e.getMessage());
        }
        for (Iterator<Proxy> iter = l.iterator(); iter.hasNext(); ) {
            Proxy proxy = iter.next();
            InetSocketAddress addr = (InetSocketAddress) proxy.address();
            if (null == addr || null == addr.getHostName()) {
                LOGGER.error("系统无代理,无法连接Pixiv!");
            } else {
                LOGGER.warn("当前代理地址:{}:{}", addr.getHostName(), addr.getPort());
                inetSocketAddress = addr;
            }
        }
    }

    /**
     * 从谷歌浏览器中获取PixivCookie的phpid
     *
     * @return PHPSESSID
     */
    public void getCookies() {
        getSysProxy();
        String path = EntityPreset.BROWSER_PATH.CHROME.PATH;
        String phpSessid = "";
        StringBuilder builder = new StringBuilder();
        Set<Cookie> cookies = processCookies(new File(EntityPreset.BROWSER_PATH.CHROME.PATH), EntityPreset.HttpEnum.PIXIVDOMAIN.URL);
        for (Cookie cookie : cookies) {
            if ("PHPSESSID".equals(cookie.getName())) {
                phpSessid = cookie.getValue();
                if (null == phpSessid) {
                    LOGGER.error("获取PHPSESSID失败!请确认是否已经在谷歌浏览器登录过Pixiv并允许储存cookie!");
                } else {
                    LOGGER.warn("获取到的PHPSESSID:{}", phpSessid);
                    PHPSESSID = phpSessid;
                }
                String userid = StringUtils.substringBefore(phpSessid, "_");
                if (null == phpSessid) {
                    LOGGER.error("获取从PHPSESSID中获取USERID失败!");
                } else {
                    LOGGER.warn("获取到的USERID:{}", userid);
                    USERID = userid;
                }
            }
            if ("device_token".equals(cookie.getName())) {
                DEVICETOKEN = cookie.getValue();
            }
            builder.append(cookie.getName()).append("=").append(cookie.getValue()).append("; ");
        }
        COOKIES_ALL = builder.toString();
    }

    /**
     * 过程饼干
     *
     * @param cookieStore  Cookie商店
     * @param domainFilter 域滤波器
     * @return {@link Set}<{@link Cookie}>
     */
    public Set<Cookie> processCookies(File cookieStore, String domainFilter) {
        getWindowsKey();
        HashSet<Cookie> cookies = new HashSet<>();
        if (cookieStore.exists()) {
            Connection connection = null;
            try {
                cookieStoreCopy.delete();
                Files.copy(cookieStore.toPath(), cookieStoreCopy.toPath());
                // load the sqlite-JDBC driver using the current class loader
                Class.forName("org.sqlite.JDBC");
                // create a database connection
                connection = DriverManager.getConnection("jdbc:sqlite:" + cookieStoreCopy.getAbsolutePath());
                Statement statement = connection.createStatement();
                statement.setQueryTimeout(30); // set timeout to 30 seconds
                ResultSet result;
                if (domainFilter == null || domainFilter.isEmpty()) {
                    result = statement.executeQuery("select * from cookies");
                } else {
                    result = statement.executeQuery("select * from cookies where host_key like \"%" + domainFilter + "%\"");
                }
                while (result.next()) {
                    String name = result.getString("name");
                    parseCookieFromResult(cookieStore, name, cookies, result);
                }
            } catch (Exception e) {
                e.printStackTrace();
                // if the error message is "out of memory",
                // it probably means no database file is found
            } finally {
                try {
                    if (connection != null) {
                        connection.close();
                    }
                } catch (SQLException e) {
                    // connection close failed
                }
            }
        }
        return cookies;
    }

    /**
     * 从结果解析Cookie
     *
     * @param cookieStore Cookie商店
     * @param name        名字
     * @param cookies     饼干
     * @param result      结果
     * @throws SQLException sqlexception异常
     */
    private void parseCookieFromResult(File cookieStore, String name, HashSet<Cookie> cookies, ResultSet result) throws SQLException {
        byte[] encryptedBytes = result.getBytes("encrypted_value");
        String path = result.getString("path");
        String domain = result.getString("host_key");
        boolean secure = determineSecure(result);
        boolean httpOnly = determineHttpOnly(result);
        Date expires = result.getDate("expires_utc");

        EncryptedCookie encryptedCookie = new EncryptedCookie(name,
                encryptedBytes,
                expires,
                path,
                domain,
                secure,
                httpOnly,
                cookieStore);

        DecryptedCookie decryptedCookie = decrypt(encryptedCookie);

        if (decryptedCookie != null) {
            cookies.add(decryptedCookie);
        } else {
            cookies.add(encryptedCookie);
        }
        cookieStoreCopy.delete();
    }

    private boolean determineSecure(ResultSet result) throws SQLException {
        boolean secure;
        try {
            secure = result.getBoolean("secure");
        } catch (SQLException e) {
            secure = result.getBoolean("is_secure");
        }
        return secure;
    }

    private boolean determineHttpOnly(ResultSet result) throws SQLException {
        boolean secure;
        try {
            secure = result.getBoolean("is_httponly");
        } catch (SQLException e) {
            secure = result.getBoolean("httponly");
        }
        return secure;
    }

    /**
     * Decrypts an encrypted cookie
     */
    protected DecryptedCookie decrypt(EncryptedCookie encryptedCookie) {
        byte[] decryptedBytes = null;

        // Separate prefix (v10), nonce and ciphertext/tag
        byte[] nonce = Arrays.copyOfRange(encryptedCookie.getEncryptedValue(), 3, 3 + 12);
        byte[] ciphertextTag = Arrays.copyOfRange(encryptedCookie.getEncryptedValue(), 3 + 12,
                encryptedCookie.getEncryptedValue().length);

        // Decrypt
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, nonce);
            SecretKeySpec keySpec = new SecretKeySpec(windowsMasterKey, "AES");

            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);
            decryptedBytes = cipher.doFinal(ciphertextTag);
        } catch (Exception e) {
            throw new IllegalStateException("Error decrypting", e);
        }


        if (decryptedBytes == null) {
            return null;
        } else {
            return new DecryptedCookie(encryptedCookie.getName(),
                    encryptedCookie.getEncryptedValue(),
                    new String(decryptedBytes),
                    encryptedCookie.getExpires(),
                    encryptedCookie.getPath(),
                    encryptedCookie.getDomain(),
                    encryptedCookie.isSecure(),
                    encryptedCookie.isHttpOnly(),
                    encryptedCookie.getCookieStore());
        }
    }

    public void getWindowsKey() {
        // Inspired by https://stackoverflow.com/a/65953409/1631104
        // Get encrypted master key
        String pathLocalState = System.getProperty("user.home") + "/AppData/Local/Google/Chrome/User Data/Local State";
        File localStateFile = new File(pathLocalState);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(localStateFile);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load JSON from Chrome Local State file", e);
        }

        String encryptedMasterKeyWithPrefixB64 = jsonNode.at("/os_crypt/encrypted_key").asText();

        // Remove prefix (DPAPI)
        byte[] encryptedMasterKeyWithPrefix = Base64.getDecoder().decode(encryptedMasterKeyWithPrefixB64);
        byte[] encryptedMasterKey = Arrays.copyOfRange(encryptedMasterKeyWithPrefix, 5, encryptedMasterKeyWithPrefix.length);

        // Decrypt and store the master key for use later
        this.windowsMasterKey = Crypt32Util.cryptUnprotectData(encryptedMasterKey);
    }
}
