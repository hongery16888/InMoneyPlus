package com.magic.inmoney.model

open class LocalDayStockItemModel(val stocks: ArrayList<StockItemModel>, val it: ArrayList<DaysStockInfoModel>){
    val stockItems = stocks
    val dayStockItems = it
}