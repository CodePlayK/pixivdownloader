package com.pixivdownloader.core.constance;

public interface EntityPreset {

    enum RATING implements EntityPreset {
        R18("R18", "R18图片保存路径"),
        R18_AI("R18_AI", "R18_AI图片保存路径"),
        R18_COMIC("R18_COMIC", "R18-COMIC图片保存路径"),
        R18G_COMIC("R18G_COMIC", "R18G-COMIC图片保存路径"),
        R18G("R18G", "R18G图片保存路径"),
        R18_GIF("R18_GIF", "R18-GIF图片保存路径"),
        R18G_GIF("R18G_GIF", "R18G-GIF图片保存路径"),
        RANKING("RANKING", "排行榜图片路径"),
        R18_NOVEL_RANKING("R18_NOVEL_RANKING", "R18小说排行榜保存路径"),
        R18G_NOVEL_RANKING("R18G_NOVEL_RANKING", "R18G小说排行榜保存路径"),
        NOVEL_PATH("NOVEL_PATH", "小说收藏保存路径"),
        NONEH("NONEH", "非涩图保存路径"),
        R34("R34", "R34视频收藏保存路径"),
        NONEH_COMIC("NONEH_COMIC", "NONEH-COMIC图片保存路径");
        public String NAME;
        public String PATH_NAME;

        RATING(String NAME, String PATH_NAME) {
            this.NAME = NAME;
            this.PATH_NAME = PATH_NAME;
        }
    }

    enum RATING_TYPE {
        DAILY_R18("daily_r18"),
        DAILY_R18_AI("daily_r18_ai"),
        R18G("r18g");
        public String RANKING_TYPE;

        RATING_TYPE(String daily_r18) {
            this.RANKING_TYPE = daily_r18;
        }

    }

    enum HttpEnum {
        BOOKMARK_LIST_URL_BEGIN("https://www.pixiv.net/touch/ajax/user/bookmarks?id="),
        BOOKMARK_LIST_URL_END("&type=illust&lang=zh&offset=0&limit=48&p="),

        R18_NOVEL_RANKING_URL("https://www.pixiv.net/touch/ajax/ranking/novel?mode=weekly_r18&page=1&work_lang=zh-cn&lang=zh"),
        R18G_NOVEL_RANKING_URL("https://www.pixiv.net/touch/ajax/ranking/novel?mode=r18g&page=1&work_lang=zh-cn&lang=zh"),
        NOVEL_DETAIL_URL("https://www.pixiv.net/touch/ajax/novel/details?ref=&lang=zh&novel_id="),
        NOVEL_FAVORITE_URL("https://www.pixiv.net/touch/ajax/user/bookmarks?type=novel&lang=zh&id="),
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

        public String URL;

        HttpEnum(String url) {
            this.URL = url;
        }

        public String getUrl() {
            return URL;
        }

        public void setUrl(String url) {
            this.URL = url;
        }
    }

    enum FILE_TYPE {
        JPG(".jpg"),
        PNG(".png"),
        GIF(".gif"),
        ZIP(".zip"),
        ;
        public String FILE_TYPE;

        FILE_TYPE(String fileType) {
            this.FILE_TYPE = fileType;
        }


    }

    enum URLS {
        OPEN("/img/"),
        CLOSE1("_p0_"),
        CLOSE2("_square"),
        ;
        public String MARK;

        URLS(String mark) {
            this.MARK = mark;
        }

    }

    enum BROWSER_PATH {
        CHROME("C:\\Users\\" + System.getProperties().getProperty("user.name") + "\\AppData\\Local\\Google\\Chrome\\User Data\\Default\\Network\\Cookies");
        public String PATH;

        BROWSER_PATH(String path) {
            this.PATH = path;
        }
    }

    enum RULE34 {

    }

}
