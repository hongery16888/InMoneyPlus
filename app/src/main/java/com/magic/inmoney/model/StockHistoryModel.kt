package com.magic.inmoney.model

import org.litepal.crud.LitePalSupport

class StockHistoryModel: LitePalSupport(){
    var stockCode: String? = null
    var dateTime: String? = null
    var openPrice: Float? = null
    var nowPrice: Float? = null
    var swing: Float? = null
    var stockRate: Float? = null
    var minPrice: Float? = null
    var maxPrice: Float? = null
    var tradeNum:Float? = null
    var tradeAmount:Float? = null
    var turnoverRate:Float? = null
    var yinyang: Boolean? = null
    var mA3Price = 0f
    var mA5Price = 0f
    var mA8Price = 0f
    var mA10Price = 0f
    var mA13Price = 0f
    var mA20Price = 0f
    var mA21Price = 0f
    var mA30Price = 0f
    var mA60Price = 0f
    var dea = 0f
    var dif = 0f
    var mAcd = 0f
    var k = 0f
    var d = 0f
    var j = 0f
    var r = 0f
    var rsi = 0f
    var up = 0f
    var mb = 0f
    var dn = 0f
    var mA5Volume = 0f
    var mA10Volume = 0f
}