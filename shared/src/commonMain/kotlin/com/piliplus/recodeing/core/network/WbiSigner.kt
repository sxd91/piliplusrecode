package com.piliplus.recodeing.core.network

/**
 * WBI 签名入口。阶段 1 先保留参数注入点；后续会根据 nav 返回的 img/sub key
 * 生成 mixin_key 并计算 w_rid。这样匿名接口可以先以非签名参数运行，签名逻辑集中替换。
 */
class WbiSigner {
    suspend fun sign(params: Map<String, Any?>): Map<String, Any?> {
        return params.filterValues { it != null }
    }
}
