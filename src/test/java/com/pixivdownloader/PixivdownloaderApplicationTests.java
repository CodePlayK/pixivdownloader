package com.pixivdownloader;

import com.alibaba.fastjson.JSON;
import com.pixivdownloader.core.constance.EntityPreset;
import com.pixivdownloader.core.entity.novel.Novel;
import com.pixivdownloader.core.entity.novel.NovelRanking;
import com.pixivdownloader.core.entity.novel.po.NovelPo;
import com.pixivdownloader.core.mapper.novel.NovelMapper;
import com.pixivdownloader.core.properties.FilePathProperties;
import com.pixivdownloader.core.service.PicService;
import com.pixivdownloader.core.utils.RequestUtils;
import com.vdurmont.emoji.EmojiManager;
import com.vdurmont.emoji.EmojiParser;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

@SpringBootTest
class PixivdownloaderApplicationTests {
    private final Logger LOGGER = LogManager.getLogger();

    @Autowired
    private PicService picService;
    @Autowired
    private RequestUtils requestUtils;
    @Autowired
    private FilePathProperties filePathProperties;
    @Autowired
    private NovelMapper novelMapper;

    @Test
    void contextLoads() throws IOException {
    }

    @Test
    void test1() throws IOException {
        Set<Integer> fileSet = new HashSet<>();
        List<String> list = getFileNovelId();
        list.forEach(a -> fileSet.add(Integer.valueOf(StringUtils.substringBefore(a, "_"))));
        List<NovelPo> novelPos = novelMapper.queryAllNovelId();
        Set<Integer> set = new HashSet<>();
        if (!novelPos.isEmpty()) {
            novelPos.forEach(a -> set.add(a.getNovelId()));
        }

        String url = EntityPreset.HttpEnum.R18_NOVEL_RANKING_URL.URL;
        String localPath = filePathProperties.getR18_NOVEL_RANKING();
        getNovel(url, localPath, set, fileSet, "R18");
        url = EntityPreset.HttpEnum.R18G_NOVEL_RANKING_URL.URL;
        localPath = filePathProperties.getR18G_NOVEL_RANKING();
        getNovel(url, localPath, set, fileSet, "R18G");

    }

    private List<String> getFileNovelId() {
        File file1 = new File(filePathProperties.getR18_NOVEL_RANKING());
        File file2 = new File(filePathProperties.getR18G_NOVEL_RANKING());
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
        return list;
    }

    private void getNovel(String url, String localPath, Set<Integer> set, Set<Integer> fileSet, String novelType) throws IOException {
        ResponseEntity<String> responseEntity = requestUtils.requestPreset(url, HttpMethod.GET);
        String body0 = StringUtils.substringBetween(responseEntity.getBody(), "\"ranking\":", ",\"ads\":");
        List<NovelRanking> novelRankings = JSON.parseArray(body0, NovelRanking.class);
        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder stringBuilder1 = new StringBuilder();
        for (NovelRanking novelRanking : Objects.requireNonNull(novelRankings)) {
            if (!set.contains(novelRanking.getNovelId())) {
                writeSaveNovel(localPath, stringBuilder, stringBuilder1, novelRanking, fileSet, novelType);
            } else {
                LOGGER.warn("[{}]已存在数据库中,跳过!", novelRanking.getNovelId());
            }
        }
    }

    private void writeSaveNovel(String localPath, StringBuilder stringBuilder, StringBuilder stringBuilder1, NovelRanking novelRanking, Set<Integer> fileSet, String novelType) throws IOException {
        ResponseEntity<String> responseEntity;
        responseEntity = requestUtils.requestPreset(EntityPreset.HttpEnum.NOVEL_DETAIL_URL.URL + novelRanking.getNovelId(), HttpMethod.GET);
        String body = responseEntity.getBody();
        String body1 = "[" + StringUtils.substringBetween(body, "\"novel_details\":", "},\"author_details") + "}]";
        List<Novel> novels = JSON.parseArray(body1, Novel.class);
        Novel novel = novels.get(0);
        novel.setText(stringBuilder.append(novel.getTitle()).append("\r\n\r\n").append(novel.getTags().toString())
                .append("\r\n\r\n").append(novel.getUserName()).append("\r\n\r\n").append(novel.getText()).toString());
        stringBuilder.setLength(0);
        NovelPo novelpo = new NovelPo(novel);
        novelpo.setNovelType(novelType);
        writeFile(localPath, stringBuilder, stringBuilder1, novel, novelpo, fileSet);
        try {
            encodeEmoji(novelpo);
            novelMapper.insert(novelpo);
            LOGGER.info("保存数据库成功[{}]", novelpo.getTitle());
        } catch (Exception e) {
            LOGGER.error("插入数据库失败![{}]", novelpo.getTitle());
            LOGGER.error(e.getMessage());
            LOGGER.error(e.getStackTrace());
        }
    }


    private void writeFile(String localPath, StringBuilder stringBuilder, StringBuilder stringBuilder1, Novel novel, NovelPo novelpo, Set<Integer> fileSet) throws IOException {
        String fileName = getFileName(stringBuilder, stringBuilder1, novel, localPath);
        if (fileSet.contains(novelpo.getNovelId())) {
            LOGGER.warn("[{}]TXT已存在于本地文件,跳过!", fileName);
            return;
        }
        FileWriter fileWriter = new FileWriter(fileName);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.write(novelpo.getText());
        bufferedWriter.flush();
        bufferedWriter.close();
        fileWriter.close();
        LOGGER.info("[{}]写入TXT文件成功!", fileName);

    }

    private String getFileName(StringBuilder stringBuilder, StringBuilder stringBuilder1, Novel novel1, String localPath) throws IOException {
        String fileName;
        for (int i = 0; i < novel1.getTags().size(); i++) {
            if (i == novel1.getTags().size()) {
                stringBuilder1.append(StringUtils.substringBefore(novel1.getTags().get(i), "/"));
            } else {
                stringBuilder1.append(StringUtils.substringBefore(novel1.getTags().get(i), "/")).append("_");
            }
        }
        fileName = localPath + stringBuilder.append(novel1.getId()).append("_").append(novel1.getBookmarkCount()).append("_")
                .append(novel1.getTitle()).append("_").append(stringBuilder1).append(".txt");
        stringBuilder.setLength(0);
        stringBuilder1.setLength(0);
        return fileName;
    }

    public void encodeEmoji(Object object) {
        Field[] field = object.getClass().getDeclaredFields(); // 获取实体类的所有属性，返回Field数组
        try {
            for (Field item : field) { // 遍历所有属性
                String name = item.getName(); // 获取属性的名字
                name = name.substring(0, 1).toUpperCase() + name.substring(1); // 将属性的首字符大写，方便构造get，set方法
                String type = item.getGenericType().toString(); // 获取属性的类型
                if (type.equals("class java.lang.String")) { // 如果type是类类型，则前面包含"class "，后面跟类名
                    Method m = object.getClass().getMethod("get" + name);
                    String value = (String) m.invoke(object); // 调用getter方法获取属性值
                    //.....处理开始........
                    if (EmojiManager.containsEmoji(value)) {
                        m = object.getClass().getMethod("set" + name, String.class);
                        m.invoke(object, EmojiParser.parseToAliases(value));
                        LOGGER.info("[{}]字段种含有emoji,成功转换~", name);
                    }
                    //.....处理结束........
                }
                // 如果有需要,可以仿照上面继续进行扩充,再增加对其它类型的判断
            }
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException |
                 InvocationTargetException e) {
            LOGGER.error("emoji转换异常!");
            LOGGER.error(e.getMessage());
            LOGGER.error(e.getStackTrace());
        }

    }
}
