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

    const val OFFICIAL_LOGIN_URL = "$PASSPORT_BASE_URL/login"
    const val OFFICIAL_REGISTER_URL = "$PASSPORT_BASE_URL/register/phone.html"
}
