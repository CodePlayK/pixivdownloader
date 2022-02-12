package com.pixivdownloader;

import com.alibaba.fastjson.JSON;
import com.pixivdownloader.core.entity.RankingPic;
import com.pixivdownloader.core.utils.RequestUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class test {
    @Test
    void moveNonehPic() throws IOException {
        File f = new File("D:\\onedrive\\Pixiv\\R18\\");
        for (String s : f.list()) {
            if (!s.contains("R-18")) {
                Path p1 = Paths.get("D:\\onedrive\\Pixiv\\R18\\" + s);
                Path p2 = Paths.get("D:\\onedrive\\Pixiv\\NONEH\\" + s);
                Files.move(p1, p2, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    @Test
    void test1() {
        RequestUtils requestUtils = new RequestUtils();
        String s1 = requestUtils.getRankingBody("{\"error\":false,\"message\":\"\",\"body\":{\"rankingDate\":\"2022-02-04\",\"ranking\":[{\"illustId\":\"95968575\",\"rank\":1},{\"illustId\":\"95991906\",\"rank\":2},{\"illustId\":\"95958507\",\"rank\":3},{\"illustId\":\"95966047\",\"rank\":4},{\"illustId\":\"95958614\",\"rank\":5},{\"illustId\":\"95997089\",\"rank\":6},{\"illustId\":\"95966119\",\"rank\":7},{\"illustId\":\"95958561\",\"rank\":8},{\"illustId\":\"95968905\",\"rank\":9},{\"illustId\":\"95976051\",\"rank\":10},{\"illustId\":\"95958929\",\"rank\":11},{\"illustId\":\"95981774\",\"rank\":12},{\"illustId\":\"95979860\",\"rank\":13},{\"illustId\":\"95981823\",\"rank\":14},{\"illustId\":\"95981820\",\"rank\":15},{\"illustId\":\"95977101\",\"rank\":16},{\"illustId\":\"95958487\",\"rank\":17},{\"illustId\":\"95997842\",\"rank\":18}],\"ads\":{\"ad_logo\":{\"zone\":\"t_logo_side\",\"url\":\"https:\\/\\/pixon.ads-pixiv.net\\/show?zone_id=t_logo_side\\u0026format=js\\u0026s=0\\u0026up=0\\u0026ng=g\\u0026l=zh\\u0026os=and\\u0026uri=%2Ftouch%2Fajax%2Franking%2Fillust\\u0026is_spa=1\\u0026ab_test_digits_first=40\\u0026uab=\\u0026yuid=GJVkcXk\\u0026suid=Pgz2rn3mygelp7af3\\u0026num=61fe440434\",\"ng\":\"g\",\"height\":32,\"width\":32,\"geta\":false},\"ad_above_header\":{\"zone\":\"t_topmost_header\",\"url\":\"https:\\/\\/pixon.ads-pixiv.net\\/show?zone_id=t_topmost_header\\u0026format=js\\u0026s=0\\u0026up=0\\u0026ng=g\\u0026l=zh\\u0026os=and\\u0026uri=%2Ftouch%2Fajax%2Franking%2Fillust\\u0026is_spa=1\\u0026ab_test_digits_first=40\\u0026uab=\\u0026yuid=GJVkcXk\\u0026suid=Pgz2rn3myjpcwuxp6\\u0026num=61fe4404256\",\"ng\":\"g\",\"height\":50,\"width\":320,\"geta\":false},\"ad_below_header\":{\"zone\":\"t_header\",\"url\":\"https:\\/\\/pixon.ads-pixiv.net\\/show?zone_id=t_header\\u0026format=js\\u0026s=0\\u0026up=0\\u0026ng=g\\u0026l=zh\\u0026os=and\\u0026uri=%2Ftouch%2Fajax%2Franking%2Fillust\\u0026is_spa=1\\u0026ab_test_digits_first=40\\u0026uab=\\u0026yuid=GJVkcXk\\u0026suid=Pgz2rn3mylu204lsv\\u0026num=61fe4404917\",\"ng\":\"g\",\"height\":\"auto\",\"width\":320,\"geta\":false},\"ad_overlay\":{\"zone\":\"t_overray2\",\"url\":\"https:\\/\\/pixon.ads-pixiv.net\\/show?zone_id=t_overray2\\u0026format=js\\u0026s=0\\u0026up=0\\u0026ng=g\\u0026l=zh\\u0026os=and\\u0026uri=%2Ftouch%2Fajax%2Franking%2Fillust\\u0026is_spa=1\\u0026ab_test_digits_first=40\\u0026uab=\\u0026yuid=GJVkcXk\\u0026suid=Pgz2rn3mynxvoxc8x\\u0026num=61fe4404522\",\"ng\":\"g\",\"height\":50,\"width\":320,\"geta\":true},\"ad_in_feed\":{\"zone\":\"t_footer\",\"url\":\"https:\\/\\/pixon.ads-pixiv.net\\/show?zone_id=t_footer\\u0026format=js\\u0026s=0\\u0026up=0\\u0026ng=g\\u0026l=zh\\u0026os=and\\u0026uri=%2Ftouch%2Fajax%2Franking%2Fillust\\u0026is_spa=1\\u0026ab_test_digits_first=40\\u0026uab=\\u0026yuid=GJVkcXk\\u0026suid=Pgz2rn3mypznnktdo\\u0026num=61fe4404852\",\"ng\":\"g\",\"height\":\"auto\",\"width\":\"auto\",\"geta\":false},\"ad_grid\":{\"zone\":\"t_native_grid\",\"url\":\"https:\\/\\/pixon.ads-pixiv.net\\/show?zone_id=t_native_grid\\u0026format=js\\u0026s=0\\u0026up=0\\u0026ng=g\\u0026l=zh\\u0026os=and\\u0026uri=%2Ftouch%2Fajax%2Franking%2Fillust\\u0026is_spa=1\\u0026ab_test_digits_first=40\\u0026uab=\\u0026yuid=GJVkcXk\\u0026suid=Pgz2rn3mys0tl1yvm\\u0026num=61fe4404769\",\"ng\":\"g\",\"height\":128,\"width\":128,\"geta\":false}}}}");
        List<RankingPic> ranking = JSON.parseArray(s1, RankingPic.class);
    }
    @Test
    void test2() {
        String[] split = "1657544,4180846".split(",");

    }
}
