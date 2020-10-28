package com.magic.inmoney.model

import org.litepal.crud.LitePalSupport

class StockBaseInfo: LitePalSupport(){
    var stockCode: String? = null
    var stockName: String? = null
    var per: String? = null
    var pbr: String? = null
    var capitalization: String? = null
    var totalMarketValue: String? = null
    var industry: String? = null
}