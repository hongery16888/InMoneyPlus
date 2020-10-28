package com.magic.inmoney.model

import org.litepal.crud.LitePalSupport

class StockDayModel : LitePalSupport() {
    var dateTime: String? = null
    var openPrice: Float? = null
    var nowPrice: Float? = null
    var swing: Float? = null
    var stockRate: Float? = null
    var minPrice: Float? = null
    var maxPrice: Float? = null
    var tradeNum:Long? = null
    var tradeAmount:Float? = null
    var turnoverRate:Float? = null
    var yinyang: Boolean? = null
}