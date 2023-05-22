package com.pixivdownloader.core.entity;

import com.pixivdownloader.core.constance.EntityPreset;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Bookmark {
    private String id;
    private List<String> tags;
    private String title;
    private String comment;
    private int pageCount = 1;
    private String bookmarkId;
    private String filType;
    private AuthorDetails authorDetails;
    private String aiType;

    public String getAiType() {
        return aiType;
    }

    public void setAiType(String aiType) {
        if (aiType.equals("2")) {
            this.aiType = "_AI";
        } else {
            this.aiType = "";
        }
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    private String urlS;
    private String urlSm;
    private String urlSs;
    private String type;
    private String page;

    public Bookmark() {

    }


    public String getUrlSm() {
        return urlSm;
    }

    public void setUrlSm(String urlSm) {
        if (null != urlSm && !urlSm.isEmpty()) {
            String url = StringUtils.substringBetween(urlSm, EntityPreset.URLS.OPEN.MARK, EntityPreset.URLS.CLOSE1.MARK);
            if (null == url || url.isEmpty()) {
                url = StringUtils.substringBetween(urlSm, EntityPreset.URLS.OPEN.MARK, EntityPreset.URLS.CLOSE2.MARK);
            }
            this.urlS = url;
            this.filType = EntityPreset.FILE_TYPE.JPG.FILE_TYPE;
        }
    }

    public String getUrlSs() {
        return urlSs;
    }

    public void setUrlSs(String urlSs) {
        if (null != urlSs && !urlSs.isEmpty()) {
            String url = StringUtils.substringBetween(urlSs, EntityPreset.URLS.OPEN.MARK, EntityPreset.URLS.CLOSE1.MARK);
            if (null == url || url.isEmpty()) {
                url = StringUtils.substringBetween(urlSs, EntityPreset.URLS.OPEN.MARK, EntityPreset.URLS.CLOSE2.MARK);
            }
            this.urlS = url;
            this.filType = EntityPreset.FILE_TYPE.JPG.FILE_TYPE;
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        if (null == this.id || this.id.isEmpty()) {
            return "000000";
        }
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBookmarkId() {
        if (null == this.bookmarkId || this.bookmarkId.isEmpty()) {
            return "0000000";
        }
        return bookmarkId;
    }

    public void setBookmarkId(String bookmarkId) {
        if (bookmarkId.isEmpty()) {
            this.bookmarkId = ("0000000");
        }
        this.bookmarkId = StringUtils.substring(bookmarkId, 0, bookmarkId.length() - 1);
    }

    public String getFilType() {
        if (null == this.filType || this.filType.isEmpty()) {
            return EntityPreset.FILE_TYPE.JPG.FILE_TYPE;
        }
        return filType;
    }

    public void setFilType(String filType) {
        this.filType = filType;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public List<String> getTags() {
        if (null == this.tags || this.tags.get(0).isEmpty()) {
            List<String> strings = new ArrayList<>();
            strings.add("未知标签");
            return strings;
        }
        return tags;
    }

    public void setTags(List<String> tags) {
        final Pattern pattern = Pattern.compile("[\\s\\\\/:*?\"<>|]");
        List<String> tags1 = new ArrayList<>();
        if (tags.isEmpty()) {
            tags1.add("未知标签");
        } else {
            for (String tag : tags) {
                Matcher matcher = pattern.matcher(tag);
                tag = matcher.replaceAll(""); // 将匹配到的非法字符以空替换
                tags1.add(tag);
            }
        }
        this.tags = tags1;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        final Pattern pattern = Pattern.compile("[\\s\\\\/:*?\"<>|]");
        Matcher matcher = pattern.matcher(title);
        title = matcher.replaceAll(""); // 将匹配到的非法字符以空替换
        if (title.isEmpty()) {
            title = "未命名";
        }
        this.title = title;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUrlS() {
        return urlS;
    }

    public void setUrlS(String urlS) {
        if (null != urlS && !urlS.isEmpty()) {
            String url = StringUtils.substringBetween(urlS, EntityPreset.URLS.OPEN.MARK, EntityPreset.URLS.CLOSE1.MARK);
            if (null == url || url.isEmpty()) {
                url = StringUtils.substringBetween(urlS, EntityPreset.URLS.OPEN.MARK, EntityPreset.URLS.CLOSE2.MARK);
            }
            this.urlS = url;
        }
        this.filType = EntityPreset.FILE_TYPE.JPG.FILE_TYPE;
    }

    public AuthorDetails getAuthorDetails() {
        return authorDetails;
    }

    public void setAuthorDetails(AuthorDetails authorDetails) {
        this.authorDetails = authorDetails;
    }
}
