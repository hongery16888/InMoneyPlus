package com.magic.inmoney.model

import org.litepal.crud.LitePalSupport

class StockFavoriteModel : LitePalSupport(){
    var stockCode: String? = null
    var stockName: String? = null
    var stockAddPrice: Float? = null
    var stockNextDayMaxPrice:Float? = null
    var stockNextDayNowPrice:Float? = null
    var stockNowPrice: Float? = null
    var stockAddTime: String? = null
    var kLineType: String? = null
    var market: String? = null
    var stockRate: Float? = null
    var tradeNum: Float? = null
    var turnoverRate:Float? = null
    var tradeAmount:Float? = null
}