package com.magic.inmoney.model

import org.litepal.crud.LitePalSupport

class StatisticsModel: LitePalSupport() {
    var stockCode:String? = null
    var stockName: String? = null
    var successHighCount: Int = 0
    var successCloseCount: Int = 0
    var successCloseProfit: Float = 0f
    var success1PointCount: Int = 0
    var success2PointCount: Int = 0
    var success3PointCount: Int = 0
    var success4PointCount: Int = 0
    var success5PointCount: Int = 0
    var failureHighCount: Int = 0
    var failureCloseCount: Int = 0
    var failureCloseProfit: Float = 0f
    var failure1PointCount: Int = 0
    var failure3PointCount: Int = 0
    var keyCount: Int = 0
    var maxProfit : Float = 0f
    var minProfit : Float = 0f
    var statisticsType = ""
    var successDate: String? = null
    var failureDate: String? = null
}