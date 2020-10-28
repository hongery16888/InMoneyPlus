package com.magic.inmoney.model

import org.litepal.crud.LitePalSupport

class StatisticsTotalModel : LitePalSupport() {
    var stockLineTypeName: String? = null
    var stockCount: Int? = null
    var buyPoint: String? = null
    var throughType: String? = null
    var needThirdDay: String? = null
    var closeSuccessCount : Int = 0
    var closeFailureCount: Int = 0
    var closeProfitRate: Float = 0f
    var closeLossRate: Float = 0f
}