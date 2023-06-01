package com.pixivdownloader.core.entity;

import lombok.Data;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Table(name = "nano_save")
public class AuthorPo {
    @Id
    @KeySql(useGeneratedKeys = true)
    private Integer seq;

    private String userId;
    private String userName;

    public AuthorPo(AuthorDetails authorDetails) {
        this.userId = authorDetails.getUserId();
        this.userName = authorDetails.getUserName();
    }

    public AuthorPo(Integer seq, String userId, String userName) {
        this.seq = seq;
        this.userId = userId;
        this.userName = userName;
    }
}
