package com.pixivdownloader.core.utils;

import com.pixivdownloader.core.constance.EntityPreset;
import com.pixivdownloader.core.entity.Bookmark;
import com.pixivdownloader.core.entity.ranking.RankingPic;
import com.pixivdownloader.core.properties.FilePathProperties;
import com.pixivdownloader.core.properties.PathProperties;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Component
public class FilesUtils {
    private final Logger LOGGER = LogManager.getLogger();
    private static final int MAX_FILE_NAME_LENGTH = 110;
    @Autowired
    private PathProperties pathProperties;
    @Autowired
    private FilePathProperties filePathProperties;

    /***
     * 创建目录
     * @param s
     * @param s1
     */
    private void createDir(String s, String s1) {
        File f = new File(StringUtils.substringAfter(s, "=") + "\\");
        if (!f.exists() && !f.isDirectory()) {
            try {
                f.mkdirs();
                LOGGER.info("创建[{}]目录[{}]成功!", s1, StringUtils.substringAfter(s, ":"));
            } catch (Exception e) {
                LOGGER.error("创建目录[{}]失败!", f.getAbsolutePath());
                e.printStackTrace();
            }
        }
    }

    public String getPath() {
        String path = FilesUtils.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        LOGGER.warn(path);
        if (System.getProperty("os.name").contains("dows")) {
            path = StringUtils.substringAfter(path, "file:/");
        }
        if (path.contains(".jar")) {
            path = path.substring(0, path.lastIndexOf(".jar"));
            path = path.substring(0, path.lastIndexOf("/")) + "/";
        }
        return path.replace("/", "\\");

    }

    public int getWordCount(String s) {
        return s.length();
    }

    /***
     * 获取并声称目录
     */
    public void getDir() {
        String LOCAL_PATH = pathProperties.getConfigFilePath();
        ApplicationHome ah = new ApplicationHome(FilesUtils.class);
        File file = new File(LOCAL_PATH + "config.txt");
        if (!file.exists()) {
            LOCAL_PATH = ah.getDir() + "\\";
        }
        file = new File(LOCAL_PATH + "config.txt");
        /*
        打包后在非工程目录下运行jar包时启用此方法
        */
        LOGGER.info("当前目录:{}", file.getAbsolutePath());
        HashMap<String, String> map = new HashMap<>();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
            String s = null;
            while ((s = br.readLine()) != null) {//使用readLine方法，一次读一行
                LOGGER.info(s);
                for (EntityPreset.RATING RATING : EntityPreset.RATING.values()) {
                    if (StringUtils.substringBefore(s, "=").equals(RATING.PATH_NAME)) {
                        map.put(RATING.NAME, StringUtils.substringAfter(s, "="));
                        createDir(s, RATING.PATH_NAME);
                        break;
                    }
                }
            }
            br.close();
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        for (EntityPreset.RATING RATING : EntityPreset.RATING.values()) {
            if (!map.containsKey(RATING.NAME)) {
                LOGGER.warn("config文件中未找到[{}}]目录配置,默认放于config文件同目录[{}]", RATING.PATH_NAME, LOCAL_PATH + RATING.NAME + "\\");
                map.put(RATING.NAME, LOCAL_PATH + RATING.NAME + "\\");
                createDir("=" + LOCAL_PATH + RATING.NAME + "\\", RATING.PATH_NAME);
            }
        }
        LOGGER.warn(map);
        filePathProperties.setR18_PATH(map.get("R18"));
        filePathProperties.setR18G_PATH(map.get("R18G"));
        filePathProperties.setR18_GIF_PATH(map.get("R18_GIF"));
        filePathProperties.setR18G_GIF_PATH(map.get("R18G_GIF"));
        filePathProperties.setR18_COMIC_PATH(map.get("R18_COMIC"));
        filePathProperties.setR18G_COMIC_PATH(map.get("R18G_COMIC"));
        filePathProperties.setNONEH_COMIC_PATH(map.get("NONEH_COMIC"));
        filePathProperties.setNONEH_PATH(map.get("NONEH"));
        filePathProperties.setRANKING(map.get("RANKING"));
        filePathProperties.setR18G_NOVEL_RANKING(map.get("R18G_NOVEL_RANKING"));
        filePathProperties.setR18_NOVEL_RANKING(map.get("R18_NOVEL_RANKING"));
        filePathProperties.setNOVEL_PATH(map.get("NOVEL_PATH"));
    }

    /**
     * 根据路径删除指定的目录或文件，无论存在与否
     *
     * @param sPath 要删除的目录或文件
     */
    public void deleteFolder(String sPath) {
        boolean flag = false;
        File file = new File(sPath);
        // 判断目录或文件是否存在
        if (!file.exists()) {  // 不存在返回 false
        } else {
            // 判断是否为文件
            if (file.isFile()) {  // 为文件时调用删除文件方法
                deleteFile(sPath);
            } else {  // 为目录时调用删除目录方法
                deleteDirectory(sPath);
            }
        }
    }

    /**
     * 删除单个文件
     *
     * @param sPath 被删除文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    private boolean deleteFile(String sPath) {
        boolean flag = false;
        File file = new File(sPath);
        // 路径为文件且不为空则进行删除
        if (file.isFile() && file.exists()) {
            file.delete();
            flag = true;
        }
        return flag;
    }

    /**
     * 删除目录（文件夹）以及目录下的文件
     *
     * @param sPath 被删除目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
    private boolean deleteDirectory(String sPath) {
        //如果sPath不以文件分隔符结尾，自动添加文件分隔符
        if (!sPath.endsWith(File.separator)) {
            sPath = sPath + File.separator;
        }
        File dirFile = new File(sPath);
        //如果dir对应的文件不存在，或者不是一个目录，则退出
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        boolean flag = true;
        //删除文件夹下的所有文件(包括子目录)
        File[] files = dirFile.listFiles();
        for (int i = 0; i < Objects.requireNonNull(files).length; i++) {
            //删除子文件
            if (files[i].isFile()) {
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag) {
                    break;
                }
            } //删除子目录
            else {
                flag = deleteDirectory(files[i].getAbsolutePath());
                if (!flag) {
                    break;
                }
            }
        }
        if (!flag) {
            return false;
        }
        //删除当前目录
        return dirFile.delete();
    }

    /**
     * @param fileName 文件名
     * @param bookmark 书签
     * @param pageNum  页数
     * @param fileType 文件类型
     * @return 缩短后的文件名
     */
    public String cutFileName(String fileName, Bookmark bookmark, int pageNum, String fileType) {
        StringBuilder temptags = new StringBuilder();
        if (getWordCount(fileName) >= MAX_FILE_NAME_LENGTH) {
            temptags.delete(0, temptags.length());
            LOGGER.info("文件名过长！修建标签长度……");
            int count1 = getWordCount(bookmark.getBookmarkId() + "_" + bookmark.getTags().get(0) + "_" + bookmark.getTitle()
                    + "_" + bookmark.getAuthorDetails().getUserName() + "_p" + pageNum + "_" + bookmark.getId() + "_" + bookmark.getAuthorDetails().getUserId() + "_");
            for (String tag : bookmark.getTags()) {
                if (getWordCount(temptags.toString()) <= (MAX_FILE_NAME_LENGTH - count1 - getWordCount(tag))) {
                    temptags.append("_").append(tag);
                } else {
                    break;
                }
            }
            fileName =
                    bookmark.getBookmarkId() + "_" + bookmark.getTags().get(0) + "_" + bookmark.getTitle()
                            + "_" + bookmark.getAuthorDetails().getUserName() + "_p" + pageNum + "_" + bookmark.getId() + "_" + bookmark.getAuthorDetails().getUserId() + "_" + temptags + fileType;
        }
        return fileName;
    }

    /**
     * @param fileName 文件名
     * @param bookmark 书签
     * @param pageNum  页数
     * @param fileType 文件类型
     * @return 缩短后的文件名
     */
    public String cutRankingFileName(String fileName, RankingPic bookmark, int pageNum, String fileType) {
        StringBuilder temptags = new StringBuilder();
        if (getWordCount(fileName) >= MAX_FILE_NAME_LENGTH) {
            temptags.delete(0, temptags.length());
            LOGGER.info("文件名过长！修建标签长度……");
            int count1 = getWordCount(bookmark.getDate() + "_" + bookmark.getRank() + "_" + bookmark.getTags().get(0) + "_" + bookmark.getTitle()
                    + "_p0" + "_" + bookmark.getId() + "_" + bookmark.getAuthorDetails().getUserId() + "_");
            for (String tag : bookmark.getTags()) {
                if (getWordCount(temptags.toString()) <= (MAX_FILE_NAME_LENGTH - count1 - getWordCount(tag))) {
                    temptags.append("_").append(tag);
                } else {
                    break;
                }
            }
            fileName =
                    bookmark.getDate() + "_" + bookmark.getRank() + "_" + bookmark.getTags().get(0) + "_" + bookmark.getTitle()
                            + "_p" + pageNum + "_" + bookmark.getId() + "_" + bookmark.getAuthorDetails().getUserId() + "_" + temptags + bookmark.getFilType();
        }
        return fileName;
    }

    /**
     * 获取目录下所有文件地址
     *
     * @param dir 目录
     * @return 文件地址
     */
    public String[] findFileList(File dir) {
        if (!dir.exists() || !dir.isDirectory()) {// 判断是否存在目录
            return null;
        }
        List<String> fileList = new ArrayList<>();
        String[] files = dir.list();// 读取目录下的所有目录文件信息
        assert files != null;
        for (String string : files) {
            fileList.add(dir.getPath() + "\\" + string);
        }
        return fileList.toArray(files);
    }
}
