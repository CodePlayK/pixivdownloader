package com.pixivdownloader.core.service;

import com.alibaba.fastjson.JSON;
import com.pixivdownloader.core.constance.EntityPreset;
import com.pixivdownloader.core.entity.novel.Novel;
import com.pixivdownloader.core.entity.novel.NovelRanking;
import com.pixivdownloader.core.entity.novel.po.NovelPo;
import com.pixivdownloader.core.mapper.novel.NovelMapper;
import com.pixivdownloader.core.properties.FilePathProperties;
import com.pixivdownloader.core.utils.CookieUtils;
import com.pixivdownloader.core.utils.RequestUtils;
import com.vdurmont.emoji.EmojiManager;
import com.vdurmont.emoji.EmojiParser;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 小说下载服务
 *
 * @author hakace
 * @date 2022/10/27
 */
@Component

public class NovelService {
    private final Logger LOGGER = LogManager.getLogger();

    @Autowired
    private CookieUtils cookieUtils;
    @Autowired
    private RequestUtils requestUtils;
    @Autowired
    private FilePathProperties filePathProperties;
    @Autowired
    private NovelMapper novelMapper;


    /**
     * 处理
     *
     * @throws IOException ioexception
     */
    public void process() throws IOException {
        //下载排行榜小说
        Set<Integer> fileRankingSet = getFileRankingNovelIdSet();
        List<NovelPo> novelPos = novelMapper.queryAllRankingNovelId();
        Set<Integer> rankingSet = new HashSet<>();
        if (!novelPos.isEmpty()) {
            novelPos.forEach(a -> rankingSet.add(a.getNovelId()));
        }
        String url = EntityPreset.HttpEnum.R18_NOVEL_RANKING_URL.getUrl();
        String localPath = filePathProperties.getR18NOVELRANKING();
        getNovel(url, localPath, rankingSet, fileRankingSet, "\"ranking\":", ",\"ads\":", "RANKING");
        url = EntityPreset.HttpEnum.R18G_NOVEL_RANKING_URL.getUrl();
        localPath = filePathProperties.getR18GNOVELRANKING();
        getNovel(url, localPath, rankingSet, fileRankingSet, "\"ranking\":", ",\"ads\":", "RANKING");


        String favoriteUrl;
        String favoriteLocalPath = filePathProperties.getNOVELPATH();
        //本地已存在的小说
        Set<Integer> fileFavoriteSet = getFileFavoriteNovelIdSet();
        int page = getFavoritePage();
        List<NovelPo> favoriteNovelPos = novelMapper.queryAllFavoriteNovelId();
        //数据库中已存在的小说
        Set<Integer> favoriteSet = new HashSet<>();
        if (!favoriteNovelPos.isEmpty()) {
            favoriteNovelPos.forEach(a -> favoriteSet.add(a.getNovelId()));
        }
        for (int i = 1; i <= page; i++) {
            favoriteUrl = EntityPreset.HttpEnum.NOVEL_FAVORITE_URL.getUrl() + cookieUtils.getUSERID() + "&p=" + i;
            getNovel(favoriteUrl, favoriteLocalPath, favoriteSet, fileFavoriteSet, "\"bookmarks\":", ",\"total\"", "FAVORITE");
        }
    }

    /**
     * 获取收藏页数
     *
     * @return int
     */
    private int getFavoritePage() {
        ResponseEntity<String> responseEntity = requestUtils.requestPreset(EntityPreset.HttpEnum.NOVEL_FAVORITE_URL.getUrl() + cookieUtils.getUSERID(), HttpMethod.GET);
        return Integer.parseInt(Objects.requireNonNull(StringUtils.substringBetween(responseEntity.getBody(), ",\"lastPage\":", ",\"ads\"")));
    }


    /**
     * 获取文件排名小说id设置
     *
     * @return {@code Set<Integer>}
     */
    private Set<Integer> getFileRankingNovelIdSet() {
        Set<Integer> fileSet = new HashSet<>();
        File file1 = new File(filePathProperties.getR18NOVELRANKING());
        File file2 = new File(filePathProperties.getR18GNOVELRANKING());
        String[] l1 = file1.list();
        String[] l2 = file2.list();
        if (l1 == null) {
            l1 = new String[]{"NA"};
        }
        if (l2 == null) {
            l2 = new String[]{"NA"};
        }
        List<String> R18GFielList = Arrays.asList(Objects.requireNonNull(l1));
        List<String> R18FielList = Arrays.asList(Objects.requireNonNull(l2));
        List<String> list = new ArrayList<>(R18GFielList);
        list.addAll(R18FielList);
        if (!list.isEmpty()) {
            list.forEach(a -> fileSet.add(Integer.valueOf(StringUtils.substringBefore(a, "_"))));
        }
        return fileSet;
    }

    /**
     * 获取文件最喜欢小说id设置
     *
     * @return {@link Set}<{@link Integer}>
     */
    private Set<Integer> getFileFavoriteNovelIdSet() {
        Set<Integer> set = new HashSet<>();
        File file1 = new File(filePathProperties.getNOVELPATH());
        String[] l1 = file1.list();
        if (l1 == null) {
            l1 = new String[]{"NA"};
        }
        List<String> list = Arrays.asList(Objects.requireNonNull(l1));
        if (!list.isEmpty()) {
            list.forEach(a -> set.add(Integer.parseInt(StringUtils.substringBetween(a, "_", "_"))));
        }
        return set;
    }

    /**
     * 让小说
     *
     * @param url       url
     * @param localPath 本地路径
     * @param set       集
     * @param fileSet   文件集
     * @param open      切割起点
     * @param close     切割终点
     * @param fileType  文件类型
     * @throws IOException ioexception
     */
    private void getNovel(String url, String localPath, Set<Integer> set, Set<Integer> fileSet, String open, String close, String fileType) throws IOException {
        ResponseEntity<String> responseEntity = requestUtils.requestPreset(url, HttpMethod.GET);
        String body0 = StringUtils.substringBetween(responseEntity.getBody(), open, close);
        List<NovelRanking> novelRankings = JSON.parseArray(body0, NovelRanking.class);
        if (novelRankings != null && novelRankings.size() > 0 && novelRankings.get(0).getNovelId() == null) {
            novelRankings.forEach(a -> a.setNovelId(a.getId()));
        }
        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder stringBuilder1 = new StringBuilder();
        for (NovelRanking novelRanking : Objects.requireNonNull(novelRankings)) {
            if (!set.contains(novelRanking.getNovelId())) {
                writeSaveNovel(localPath, stringBuilder, stringBuilder1, novelRanking, fileSet, fileType, true);
            } else {
                writeSaveNovel(localPath, stringBuilder, stringBuilder1, novelRanking, fileSet, fileType, false);
                LOGGER.warn("[{}]已存在数据库中,跳过!", novelRanking.getNovelId());
            }
        }
    }

    /**
     * 将小说写入本地与数据库
     *
     * @param localPath      本地路径
     * @param stringBuilder  字符串生成器
     * @param stringBuilder1 字符串builder1
     * @param novelRanking   小说排行榜
     * @param fileSet        文件集
     * @param fileType       文件类型
     * @param saveFlag       保存标记
     * @throws IOException ioexception
     */
    private void writeSaveNovel(String localPath, StringBuilder stringBuilder, StringBuilder stringBuilder1, NovelRanking novelRanking, Set<Integer> fileSet, String fileType, boolean saveFlag) throws IOException {
        if (fileSet.contains(novelRanking.getNovelId())) {
            LOGGER.warn("[{}]TXT已存在于本地文件,跳过!", novelRanking.getNovelId());
            return;
        }
        String body = "";
        try {
            ResponseEntity<String> responseEntity = requestUtils.requestPreset(EntityPreset.HttpEnum.NOVEL_DETAIL_URL.getUrl() + novelRanking.getNovelId(), HttpMethod.GET);
            body = responseEntity.getBody();
        } catch (Exception e) {
            LOGGER.error("该小说已被和谐=.=||[{}]", novelRanking.getNovelId());
            LOGGER.error(e.getMessage());
            LOGGER.error(e.getStackTrace());
            return;
        }
        String body1 = "[" + StringUtils.substringBetween(body, "\"novel_details\":", "},\"author_details") + "}]";
        List<Novel> novels = JSON.parseArray(body1, Novel.class);
        Novel novel = novels.get(0);
        novel.setText(stringBuilder.append(novel.getTitle()).append("\r\n\r\n").append(novel.getTags().toString())
                .append("\r\n\r\n").append(novel.getUserName()).append("\r\n\r\n").append(novel.getText()).toString());
        stringBuilder.setLength(0);
        NovelPo novelpo = new NovelPo(novel);
        if (novelpo.getTags().contains("R-18G")) {
            novelpo.setNovelType("R18G");
        } else {
            novelpo.setNovelType("R18");

        }
        novelpo.setFileType(fileType);
        novelpo.setFavoriteId(novelRanking.getBookmarkId());
        novelpo.setCreateTime(new Date());
        writeFile(localPath, stringBuilder, stringBuilder1, novel, novelpo);
        try {
            encodeEmoji(novelpo);
            if (saveFlag) {
                novelMapper.insert(novelpo);
                LOGGER.info("保存数据库成功[{}]", novelpo.getTitle());
            }
        } catch (Exception e) {
            LOGGER.error("插入数据库失败![{}]", novelpo.getTitle());
            LOGGER.error(e.getMessage());
            LOGGER.error(e.getStackTrace());
        }
    }


    /**
     * 写文件
     *
     * @param localPath      本地路径
     * @param stringBuilder  字符串生成器
     * @param stringBuilder1 字符串builder1
     * @param novel          小说
     * @param novelpo        novelpo
     * @throws IOException ioexception
     */
    private void writeFile(String localPath, StringBuilder stringBuilder, StringBuilder stringBuilder1, Novel novel, NovelPo novelpo) throws IOException {
        String fileName = getFileName(stringBuilder, stringBuilder1, novel, localPath, novelpo);
        FileWriter fileWriter = new FileWriter(fileName);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.write(novelpo.getText());
        bufferedWriter.flush();
        bufferedWriter.close();
        fileWriter.close();
        LOGGER.info("[{}]写入TXT文件成功!", fileName);

    }

    /**
     * 获取文件名称
     *
     * @param stringBuilder  字符串生成器
     * @param stringBuilder1 字符串builder1
     * @param novel1         novel1
     * @param localPath      本地路径
     * @param novelpo        novelpo
     * @return {@link String}
     * @throws IOException ioexception
     */
    private String getFileName(StringBuilder stringBuilder, StringBuilder stringBuilder1, Novel novel1, String localPath, NovelPo novelpo) throws IOException {
        final Pattern pattern = Pattern.compile("[\\s\\\\/:*?\"<>|]");
        String fileName;
        String fileName1;
        if (novelpo.getFavoriteId() != null && novelpo.getFavoriteId() != 0) {
            stringBuilder.append(novelpo.getFavoriteId()).append("_");
        }
        fileName1 = String.valueOf(stringBuilder.append(novel1.getId()).append("_").append(novel1.getBookmarkCount()).append("_")
                .append(novel1.getTitle()));
        for (int i = 0; i < novel1.getTags().size(); i++) {
            if (novel1.getTags().get(i).length() + fileName1.length() + localPath.length() + stringBuilder1.length() + 5 < 250) {
                stringBuilder1.append("_").append(StringUtils.substringBefore(novel1.getTags().get(i), "/"));
            }
        }
        stringBuilder1.append(".txt");
        fileName1 = fileName1 + stringBuilder1;
        Matcher matcher = pattern.matcher(fileName1);
        fileName1 = matcher.replaceAll("");
        fileName = localPath + fileName1;
        stringBuilder.setLength(0);
        stringBuilder1.setLength(0);

        return fileName;
    }

    /**
     * 代理形式处理String属性中可能含有的emoji
     *
     * @param object 对象
     */
    public void encodeEmoji(Object object) {
        // 获取实体类的所有属性，返回Field数组
        Field[] field = object.getClass().getDeclaredFields();
        try {
            // 遍历所有属性
            for (Field item : field) {
                // 获取属性的名字
                String name = item.getName();
                // 将属性的首字符大写，方便构造get，set方法
                name = name.substring(0, 1).toUpperCase() + name.substring(1);
                // 获取属性的类型
                String type = item.getGenericType().toString();
                // 如果type是类类型，则前面包含"class "，后面跟类名
                if ("class java.lang.String".equals(type)) {
                    // 调用getter方法获取属性值
                    Method m = object.getClass().getMethod("get" + name);
                    String value = (String) m.invoke(object);
                    //.....处理开始........
                    if (EmojiManager.containsEmoji(value)) {
                        m = object.getClass().getMethod("set" + name, String.class);
                        m.invoke(object, EmojiParser.parseToAliases(value));
                        LOGGER.info("[{}]字段种含有emoji,成功转换~", name);
                    }
                    //.....处理结束........
                }
            }
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException |
                 InvocationTargetException e) {
            LOGGER.error("emoji转换异常!");
            LOGGER.error(e.getMessage());
            LOGGER.error(e.getStackTrace());
        }

    }
}
