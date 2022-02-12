package com.pixivdownloader.core.constance;

public interface EntityPreset {
    enum Rating {
        R18("R18"),
        R18G("R18G"),
        R18GIF("R18GIF"),
        R18GGIF("R18GGIF"),
        RANKING("RANKING"),
        NONEH("NONEH");
        String rate;

        Rating(String rate) {
            this.rate = rate;
        }

        public String getRate() {
            return rate;
        }

        public void setRate(String rate) {
            this.rate = rate;
        }
    }

    enum RankingType {
        DAILY_R18("daily_r18"),
        //WEEKLY_R18("weekly_r18"),
        R18G("r18g");
        String rankingTyoe;

        RankingType(String daily_r18) {
            this.rankingTyoe = daily_r18;
        }

        public String getRankingTyoe() {
            return rankingTyoe;
        }

        public void setRankingTyoe(String rankingTyoe) {
            this.rankingTyoe = rankingTyoe;
        }
    }

    enum HttpEnum {
        GIFURL("https://i.pximg.net/img-zip-ugoira/img/"),
        PICURL("https://i.pximg.net/img-original/img/"),
        RANKINGURL("https://www.pixiv.net/touch/ajax/ranking/illust?type=all&lang=zh&mode="),
        SINGLEPICURL("https://www.pixiv.net/touch/ajax/illust/details?&ref=&lang=zh&illust_id="),
        MULTIPICDTLURL("https://www.pixiv.net/touch/ajax/illust/details/many?"),
        PIXIVDOMAIN("pixiv.net"),
        URLZIP("_ugoira600x600.zip"),
        REFERER("https://www.pixiv.net/artworks/94479068"),
        USERAGENT("Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.55 Mobile Safari/537.36 Edg/96.0.1054.41"),
        ;

        String url;

        HttpEnum(String url) {
            this.url = url;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    enum FileType {
        ZIP(".zip"),
        GIF(".gif"),
        JPG(".jpg"),
        PNG(".png"),
        ;
        String fileType;

        FileType(String fileType) {
            this.fileType = fileType;
        }

        public String getFileType() {
            return fileType;
        }

        public void setFileType(String fileType) {
            this.fileType = fileType;
        }
    }

    enum Urls {
        OPEN("/img/"),
        CLOSE1("_p0_"),
        CLOSE2("_square"),
        ;
        String mark;

        Urls(String mark) {
            this.mark = mark;
        }

        public String getMark() {
            return mark;
        }

        public void setMark(String mark) {
            this.mark = mark;
        }
    }

    enum BrowserPath {
        CHROME("C:\\Users\\Administrator\\AppData\\Local\\Google\\Chrome\\User Data\\Default\\Network\\Cookies");
        String path;

        BrowserPath(String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }


}
