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
    private int pageCount;
    private String bookmarkId;
    private String filType;
    private AuthorDetails authorDetails;
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
            String url = StringUtils.substringBetween(urlSm, EntityPreset.Urls.OPEN.getMark(), EntityPreset.Urls.CLOSE1.getMark());
            if (null == url || url.isEmpty()) {
                url = StringUtils.substringBetween(urlSm, EntityPreset.Urls.OPEN.getMark(), EntityPreset.Urls.CLOSE2.getMark());
            }
            this.urlS = url;
            this.filType = EntityPreset.FileType.JPG.getFileType();
        }
    }

    public String getUrlSs() {
        return urlSs;
    }

    public void setUrlSs(String urlSs) {
        if (null != urlSs && !urlSs.isEmpty()) {
            String url = StringUtils.substringBetween(urlSs, EntityPreset.Urls.OPEN.getMark(), EntityPreset.Urls.CLOSE1.getMark());
            if (null == url || url.isEmpty()) {
                url = StringUtils.substringBetween(urlSs, EntityPreset.Urls.OPEN.getMark(), EntityPreset.Urls.CLOSE2.getMark());
            }
            this.urlS = url;
            this.filType = EntityPreset.FileType.JPG.getFileType();
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
        String newIndex = StringUtils.substring(bookmarkId, 0, bookmarkId.length() - 1);
        this.bookmarkId = newIndex;
    }

    public String getFilType() {
        if (null == this.filType || this.filType.isEmpty()) {
            return EntityPreset.FileType.JPG.getFileType();
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
            strings.add("????????????");
            return strings;
        }
        return tags;
    }

    public void setTags(List<String> tags) {
        Pattern pattern = Pattern.compile("[\\s\\\\/:\\*\\?\\\"<>\\|]");
        List<String> tags1 = new ArrayList<>();
        if (tags.isEmpty()) {
            tags1.add("????????????");
        } else {
            for (String tag : tags) {
                Matcher matcher = pattern.matcher(tag);
                tag = matcher.replaceAll(""); // ???????????????????????????????????????
                tags1.add(tag);
            }
        }
        this.tags = tags1;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        Pattern pattern = Pattern.compile("[\\s\\\\/:\\*\\?\\\"<>\\|]");
        Matcher matcher = pattern.matcher(title);
        title = matcher.replaceAll(""); // ???????????????????????????????????????
        if (title.isEmpty()) {
            title = "?????????";
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
            String url = StringUtils.substringBetween(urlS, EntityPreset.Urls.OPEN.getMark(), EntityPreset.Urls.CLOSE1.getMark());
            if (null == url || url.isEmpty()) {
                url = StringUtils.substringBetween(urlS, EntityPreset.Urls.OPEN.getMark(), EntityPreset.Urls.CLOSE2.getMark());
            }
            this.urlS = url;
        }
        this.filType = EntityPreset.FileType.JPG.getFileType();
    }

    public AuthorDetails getAuthorDetails() {
        return authorDetails;
    }

    public void setAuthorDetails(AuthorDetails authorDetails) {
        this.authorDetails = authorDetails;
    }
}
