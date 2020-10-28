package com.magic.inmoney.model

import org.litepal.crud.LitePalSupport

class KeyStockModel: LitePalSupport(){
    var stockCode: String? = null
    var stockName: String? = null
    var stockRate: Float? = null
    var stockProfitRate: Float? = null
    var stockCostPrice: Float? = null
    var openPrice:Float? = null
    var nowPrice:Float? = null
    var yestClosePrice: Float? = null
    var fiveAveragePrice: Float? = null
    var eightAveragePrice: Float? = null
    var thirteenAveragePrice: Float? = null
    var buyTargetPrice: Float? = null
    var todayMax:Float? = null
    var todayMin:Float? = null
    var tradeNum:Float? = null
    var tradeAmount:Float? = null
    var turnoverRate:Float? = null
    var pinyin: String? = null
    var loss: Boolean = false
    var market:String? = null
    var dateTime:String? = null
    var addDateTime:String? = null
    var buyStatus: String? = null
    var targetProfitRate: Int? = null
    var targetLossRate: Int? = null
    var averagePriceDateTime: String? = null
    var promptStatus: Boolean? = null
    var promptBuyPrice: Float? = null
    var promptTime: Long? = null
    var buyDateTime: String? = null
    var level: String = "0"
    var dust: Boolean = false
    var blockRankName: String? = null
    var blockRankRate: Double? = null
    var lastFivePointDateTime: String? = null
    var secondPromptBuyPrice: Float? = null
    var naivePromptBuyPrice: Float? = null
    var needPrompt = true
    var topPrice: Float? = null
    var debugId: Int? = null
    var trendPrice: Float = 0f
}