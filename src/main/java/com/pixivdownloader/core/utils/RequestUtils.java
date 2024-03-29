package com.pixivdownloader.core.utils;

import com.pixivdownloader.core.constance.EntityPreset;
import com.pixivdownloader.core.entity.Bookmark;
import com.pixivdownloader.core.entity.ranking.RankingPic;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.Proxy;
import java.util.*;

@Component
public class RequestUtils {
    private final Logger LOGGER = LogManager.getLogger();
    @Autowired
    private CookieUtils cookieUtils;

    /**
     * 根据片数对获取到的收藏夹进行分片
     *
     * @param list    要分片的收藏夹
     * @param partNum 分片数目
     * @return 储存分片后的set
     */
    public Set<List<Bookmark>> divideListByPartNum(List<Bookmark> list, int partNum) {
        if (null == list || list.isEmpty()) {
            return new HashSet<>();
        }
        Set<List<Bookmark>> set = new HashSet<>();
        Map<Integer, List<Bookmark>> map = new HashMap<>();
        if (list.size() <= partNum) {
            for (int i = 0; i < partNum; i++) {
                set.add(list);
            }
            return set;
        }
        for (int i = 0; i < partNum; i++) {
            map.put(i, new ArrayList<>());
        }
        int j = 0;
        for (Bookmark bookmark : list) {
            if (j == partNum) {
                j = 0;
            }
            map.get(j).add(bookmark);
            j++;
        }
        set.addAll(map.values());
        return set;
    }

    public Set<List<RankingPic>> divideRankingListByPartNum(List<RankingPic> list, int partNum) {
        if (null == list || list.isEmpty()) {
            return new HashSet<>();
        }
        Set<List<RankingPic>> set = new HashSet<>();
        Map<Integer, List<RankingPic>> map = new HashMap<>();
        if (list.size() <= partNum) {
            for (int i = 0; i < partNum; i++) {
                set.add(list);
            }
            return set;
        }
        for (int i = 0; i < partNum; i++) {
            map.put(i, new ArrayList<>());
        }
        int j = 0;
        for (RankingPic bookmark : list) {
            if (j == partNum) {
                j = 0;
            }
            map.get(j).add(bookmark);
            j++;
        }
        set.addAll(map.values());
        return set;
    }

    /**
     * 切割一个范围数字等分为几片
     *
     * @param bgNum   起始数字
     * @param endNum  结束数字
     * @param partNum 片数
     * @return 结果
     */
    public Map<Integer, Integer> divideNumByPartNum(int bgNum, int endNum, int partNum) {
        HashMap<Integer, Integer> map = new HashMap<>();
        if (bgNum >= endNum || partNum < 0) {
            map.put(1, 1);
            return map;
        }
        if (endNum - bgNum <= partNum) {
            map.put(bgNum, endNum);
            return map;
        }
        int part = (endNum - bgNum) / partNum;
        for (int i = 0; i < partNum; i++) {
            if (i < partNum - 1) {
                map.put(bgNum + part * i, bgNum + part * (i + 1) - 1);
            } else {
                map.put(bgNum + part * i, endNum);
            }
        }
        return map;
    }


    /**
     * 从body中获取总收藏数与总页数
     *
     * @param body  源body
     * @param open  起始字符
     * @param close 结束字符
     * @return 中间数字
     */
    public int getBetween(String body, String open, String close) {
        int total = 0;
        if (null != StringUtils.substringBetween(body, open, close)) {
            total = Integer.parseInt(StringUtils.substringBetween(body, open, close));
        }
        return total;
    }

    /**
     * 获取收藏body
     *
     * @param body 源body
     * @return 收藏body
     */
    public String getBookmarkBody(String body) {
        return StringUtils.substringBetween(body, "\"bookmarks\":", "],\"total\"") + "]";
    }

    /**
     * 获取排行榜body
     *
     * @param body
     * @return
     */
    public String getRankingBody(String body) {
        return StringUtils.substringBetween(body, "\"ranking\":", ",\"ads\":{");
    }

    public String getStingBy3Pin(String source, String pin, String open, String close) {
        source = StringUtils.substringAfter(source, pin);
        source = StringUtils.substringAfter(source, open);
        return StringUtils.substringBefore(source, close);
    }

    public String getStingBy3PinIndex(String source, String pin, int index, String open, int indexOpen, String close, int indexClose) {
        for (int i = 1; i <= index; i++) {
            source = StringUtils.substringAfter(source, pin);
        }
        for (int i = 1; i <= indexOpen; i++) {
            source = StringUtils.substringAfter(source, open);
        }
        for (int i = 1; i <= indexClose; i++) {
            source = StringUtils.substringBeforeLast(source, close);
        }
        return source;
    }

    /**
     * 获取多图片body
     *
     * @param body
     * @return
     */
    public String getMultiPicBody(String body) {
        return StringUtils.substringBetween(body, "\"illust_details\":", "]}}") + "]";
    }


    /**
     * 获取指定url的body
     *
     * @param url
     * @return
     */
    public String getBodyByUrl(String url) {
        return requestPreset(url, HttpMethod.GET).getBody();
    }

    /***
     * 一般请求
     * @param url
     * @param httpMethod
     * @return
     */
    public ResponseEntity<String> requestPreset(String url, HttpMethod httpMethod) {
        String PHPSESSID = "PHPSESSID=" + cookieUtils.getPHPSESSID() + "; Path=/; Domain=pixiv.net; Secure; HttpOnly;";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.COOKIE, PHPSESSID);
        httpHeaders.add("user-agent", EntityPreset.HttpEnum.USERAGENT.URL);
        SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        simpleClientHttpRequestFactory.setProxy(new Proxy(Proxy.Type.HTTP, cookieUtils.getInetSocketAddress()));
        restTemplate.setRequestFactory(simpleClientHttpRequestFactory);
        HttpEntity<Object> httpEntity = new HttpEntity<>(httpHeaders);
        LOGGER.info(url);
        return restTemplate.exchange(url, httpMethod, httpEntity, String.class);
    }

    /***
     * 下载请求
     * @param url
     * @param httpMethod
     * @return
     */
    public ResponseEntity<byte[]> requestStreamPreset(String url, HttpMethod httpMethod) {
        String PHPSESSID = "PHPSESSID=" + cookieUtils.getPHPSESSID() + "; Path=/; Domain=pixiv.net; Secure; HttpOnly;";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("referer", EntityPreset.HttpEnum.REFERER.URL);
        httpHeaders.add(HttpHeaders.COOKIE, PHPSESSID);
        httpHeaders.add("user-agent", EntityPreset.HttpEnum.USERAGENT.URL);
        SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        simpleClientHttpRequestFactory.setProxy(new Proxy(Proxy.Type.HTTP, cookieUtils.getInetSocketAddress()));
        restTemplate.setRequestFactory(simpleClientHttpRequestFactory);
        HttpEntity<Object> httpEntity = new HttpEntity<>(httpHeaders);
        LOGGER.info(url);
        return restTemplate.exchange(url, HttpMethod.GET, httpEntity, byte[].class);
    }

    /***
     * 下载请求
     * @param url
     * @param httpMethod
     * @return
     */
    public ResponseEntity<byte[]> requestStream34Preset(String url, HttpMethod httpMethod) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("user-agent", EntityPreset.HttpEnum.USERAGENT.URL);
        SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        simpleClientHttpRequestFactory.setProxy(new Proxy(Proxy.Type.HTTP, cookieUtils.getInetSocketAddress()));
        restTemplate.setRequestFactory(simpleClientHttpRequestFactory);
        HttpEntity<Object> httpEntity = new HttpEntity<>(httpHeaders);
        LOGGER.info(url);
        return restTemplate.exchange(url, HttpMethod.GET, httpEntity, byte[].class);
    }

    /**
     * 收藏某个用户
     *
     * @param userid
     */
    public void addBookmarkUser(String userid) {
        String url = "https://www.pixiv.net/touch/ajax_api/ajax_api.php";
        String PHPSESSID = "PHPSESSID=" + cookieUtils.getPHPSESSID() + "; Path=/; Domain=pixiv.net; Secure; HttpOnly;";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.COOKIE, PHPSESSID);
        httpHeaders.add("user-agent", EntityPreset.HttpEnum.USERAGENT1.URL);
        httpHeaders.add("Referer", "https://www.pixiv.net/users/" + userid);
        httpHeaders.add("Origin", "https://www.pixiv.net");
        //需要浏览器中手动关注一次用户，从devtool中获取X-Csrf-Token
        httpHeaders.add("X-Csrf-Token", "aa04c8bd88a56ff7ec1386b61ef2e04c");
        httpHeaders.add("Sec-Ch-Ua", "Google Chrome\";v=\"113\", \"Chromium\";v=\"113\", \"Not-A.Brand\";v=\"24");
        httpHeaders.add("Sec-Ch-Ua-Mobile", "?1");
        httpHeaders.add("Sec-Ch-Ua-Platform", "?Android");
        httpHeaders.add("Sec-Fetch-Dest", "empty");
        httpHeaders.add("Sec-Fetch-Mode", "cors");
        httpHeaders.add("Sec-Fetch-Site", "same-origin");
        httpHeaders.add("DNT", "1");
        httpHeaders.add("Content-Type", "multipart/form-data");
        MultiValueMap<Object, String> body = new LinkedMultiValueMap<Object, String>() {
        };
        body.add("mode", "add_bookmark_user");
        body.add("restrict", "0");
        body.add("user_id", userid);
        SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        simpleClientHttpRequestFactory.setProxy(new Proxy(Proxy.Type.HTTP, cookieUtils.getInetSocketAddress()));
        restTemplate.setRequestFactory(simpleClientHttpRequestFactory);
        HttpEntity<Object> httpEntity = new HttpEntity<>(body, httpHeaders);
        LOGGER.info(url);
        try {
            restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);
        } catch (RestClientException e) {
            throw new RuntimeException(e);
        }
    }


}
