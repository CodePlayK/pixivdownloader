package com.pixivdownloader.core.entity.ranking;

import com.pixivdownloader.core.entity.Bookmark;

public class RankingPic extends Bookmark {
    private String illustId;
    private String rank;
    private String RANKING_TYPE;
    private String date;

    public RankingPic() {
    }

    public String getRATING_TYPE() {
        return RANKING_TYPE;
    }

    public void setRATING_TYPE(String RANKING_TYPE) {
        this.RANKING_TYPE = RANKING_TYPE;
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
