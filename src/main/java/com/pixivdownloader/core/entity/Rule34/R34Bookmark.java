package com.pixivdownloader.core.entity.Rule34;

import lombok.Builder;

@Builder
public class R34Bookmark {
    String favoriteId;
    String imgId;
    String url;
    String copyright;
    String artist;
    String character;
}
