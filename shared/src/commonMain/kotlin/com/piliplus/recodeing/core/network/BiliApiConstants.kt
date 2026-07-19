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
    const val VIDEO_LIKE = "/x/web-interface/archive/like"
    const val VIDEO_COIN = "/x/web-interface/coin/add"
    const val VIDEO_RELATION = "/x/web-interface/archive/relation"
    const val COMMENT_MAIN = "/x/v2/reply/wbi/main"
    const val COMMENT_LIST = "/x/v2/reply"
    const val COMMENT_REPLIES = "/x/v2/reply/reply"
    const val COMMENT_ACTION = "/x/v2/reply/action"
    const val COMMENT_HATE = "/x/v2/reply/hate"
    const val COMMENT_ADD = "/x/v2/reply/add"
    const val FAVORITE_FOLDERS = "/x/v3/fav/folder/created/list-all"
    const val FAVORITE_RESOURCE_DEAL = "/x/v3/fav/resource/deal"
    const val FAVORITE_FOLDER_ADD = "/x/v3/fav/folder/add"
    const val DYNAMIC_FEED = "/x/polymer/web-dynamic/v1/feed/all"
    const val USER_SPACE_PROFILE = "/x/space/wbi/acc/info"
    const val USER_RELATION_STAT = "/x/relation/stat"
    const val USER_SPACE_VIDEOS = "/x/space/wbi/arc/search"

    const val TV_QR_CREATE = "$PASSPORT_BASE_URL/x/passport-tv-login/qrcode/auth_code"
    const val TV_QR_POLL = "$PASSPORT_BASE_URL/x/passport-tv-login/qrcode/poll"

    const val OFFICIAL_LOGIN_URL = "$PASSPORT_BASE_URL/login"
    const val OFFICIAL_REGISTER_URL = "$PASSPORT_BASE_URL/register/phone.html"
}
