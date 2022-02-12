package com.pixivdownloader.core.entity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AuthorDetails {
    private String userId;
    private String userName;
    private String userAccount;

    public AuthorDetails() {
    }

    public String getUserId() {
        if (null == this.userId || this.userId.isEmpty()) {
            return "000000";
        }
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        if (null == this.userName || this.userName.isEmpty()) {
            return "未知作者";
        }
        return userName;
    }

    public void setUserName(String userName) {
        Pattern pattern = Pattern.compile("[\\s\\\\/:\\*\\?\\\"<>\\|]");
        Matcher matcher = pattern.matcher(userName);
        userName = matcher.replaceAll(""); // 将匹配到的非法字符以空替换
        this.userName = userName;
    }

    public String getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(String userAccount) {
        this.userAccount = userAccount;
    }
}
