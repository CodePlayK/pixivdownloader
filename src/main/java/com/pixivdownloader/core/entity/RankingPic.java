package com.pixivdownloader.core.entity;

import lombok.Data;

public class RankingPic extends Bookmark {
    private String illustId;
    private String rank;
    private String rankingType;
    private String date;

    public RankingPic() {
    }

    public String getRankingType() {
        return rankingType;
    }

    public void setRankingType(String rankingType) {
        this.rankingType = rankingType;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getIllustId() {
        return illustId;
    }

    public void setIllustId(String illustId) {
        this.illustId = illustId;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

}
