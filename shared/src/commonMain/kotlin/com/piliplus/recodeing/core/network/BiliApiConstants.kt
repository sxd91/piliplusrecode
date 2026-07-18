package com.piliplus.recodeing.core.network

object BiliApiConstants {
    const val WEB_BASE_URL = "https://www.bilibili.com"
    const val API_BASE_URL = "https://api.bilibili.com"
    const val PASSPORT_BASE_URL = "https://passport.bilibili.com"

    const val RECOMMEND_LIST_WEB = "/x/web-interface/wbi/index/top/feed/rcmd"
    const val HOT_LIST = "/x/web-interface/popular"
    const val USER_INFO = "/x/web-interface/nav"
    const val SEARCH_DEFAULT = "/x/web-interface/wbi/search/default"
    const val SEARCH_ALL = "/x/web-interface/wbi/search/all/v2"
    const val SEARCH_SUGGEST_URL = "https://s.search.bilibili.com/main/suggest"

    const val VIDEO_VIEW = "/x/web-interface/view"
    const val VIDEO_RELATED = "/x/web-interface/archive/related"
    const val VIDEO_PLAY_URL = "/x/player/wbi/playurl"
    const val VIDEO_PLAY_INFO = "/x/player/wbi/v2"
    const val VIDEO_ONLINE_TOTAL = "/x/player/online/total"
    const val VIDEO_AI_CONCLUSION = "/x/web-interface/view/conclusion/get"
    const val VIDEO_SHOT = "/x/player/videoshot"

    const val OFFICIAL_LOGIN_URL = "$PASSPORT_BASE_URL/login"
    const val OFFICIAL_REGISTER_URL = "$PASSPORT_BASE_URL/register/phone.html"
}
