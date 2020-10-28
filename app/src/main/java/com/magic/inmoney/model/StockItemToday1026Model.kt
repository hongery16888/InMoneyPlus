package com.magic.inmoney.model

import org.litepal.crud.LitePalSupport

open class StockItemToday1026Model : LitePalSupport() {
    var stockCode: String? = null
    var stockName: String? = null
    var stockRate: Float? = null
    var stockPrice: String? = null
    var openPrice:String? = null
    var yestClosePrice:String? = null
    var nowPrice:String? = null
    var todayMax:String? = null
    var todayMin:String? = null
    var tradeNum:String? = null
    var tradeAmount:String? = null
    var turnoverRate:Float? = null
    var pinyin: String? = null
    var loss: Boolean = false
    var swing: String? = null
    var market:String? = null
    var dateTime:String? = null
    var lastFivePointDateTime: String? = null
    var debugId = 0
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
    var macd = 0f
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