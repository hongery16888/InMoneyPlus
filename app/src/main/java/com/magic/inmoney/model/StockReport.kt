package com.magic.inmoney.model

import org.litepal.crud.LitePalSupport

class StockReport: LitePalSupport(){
    var stockCode: String? = null
    var stockName: String? = null
    var earningPerShare: String? = null
    var revenue: String? = null
    var onYearGrowthRevenue: String? = null
    var onMonthGrowthRevenue: String? = null
    var netProfit: String? = null
    var onYearGrowthNetProfit: String? = null
    var onMonthGrowthNetProfit: String? = null
    var netAssetValuePreShare: String? = null
    var onNetAssetsRate: String? = null
    var cashFlowPerShare: String? = null
    var grossProfitMarginRate: String? = null
    var profitDistribution: String? = null
    var industry: String? = null
}