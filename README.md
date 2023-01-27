## Pixiv Downloader

个人为防止猎巫活动对蓝p站的本地备份,下载p站个人收藏夹与7日内的排行榜,包括：

- 插画、漫画、GIF收藏
- 插画每日排行榜
- 小说收藏，小说排行榜

> 需要Chrome与自备vpn

### 使用

- 在config.txt文件中分别配置各图片下载目标路径,并存放在任意位置
- 在path.properties中配置config.txt的路径位置
- 在except.properties中配置要过滤的关键字(针对排行榜)
- 使用Chrome登录P站

>
需要保证使用的Chrome版本的cookie的储存目录为`C:\Users\"用户名"\AppData\Local\Google\Chrome\User Data\Default\Network\Cookies`

- 登录vpn
- 运行jar包

### 相关

[CookieMonster获取浏览器cookies与解析](https://github.com/benjholla/CookieMonster.git)

[Animated GIF Encoder图片转GIF](https://github.com/madmaw/animatedgifencoder.git)

