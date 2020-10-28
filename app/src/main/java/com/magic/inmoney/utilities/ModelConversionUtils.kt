package com.magic.inmoney.utilities

import com.github.fujianlian.klinechart.DataHelper
import com.github.fujianlian.klinechart.KLineEntity
import com.magic.inmoney.base.BaseApplication
import com.magic.inmoney.const.QualityType
import com.magic.inmoney.const.StockBuyStatus
import com.magic.inmoney.model.*
import com.magic.inmoney.orm.LitePalDBase
import java.text.DecimalFormat

object ModelConversionUtils {

    private val decimalFormat3: DecimalFormat = DecimalFormat("#0.00")
    private val decimalFormat: DecimalFormat = DecimalFormat("0")

    fun stockItemToFavoriteStock(
        stockItems: ArrayList<StockItemTodayModel>,
        qualityType: QualityType
    ): ArrayList<StockFavoriteModel> {
        return ArrayList<StockFavoriteModel>().apply {
            for (stockItem in stockItems) {
                add(StockFavoriteModel().apply {
                    stockCode = stockItem.stockCode
                    stockName = stockItem.stockName
                    stockRate = stockItem.stockRate
                    market = stockItem.market
                    turnoverRate = stockItem.turnoverRate
                    tradeAmount = stockItem.tradeAmount!!.toFloat()
                    tradeNum = stockItem.tradeNum!!.toFloat()
                    stockAddPrice = stockItem.nowPrice?.toFloat()
                    stockNowPrice = stockItem.nowPrice?.toFloat()
                    stockNextDayMaxPrice = stockItem.nowPrice?.toFloat()
                    stockNextDayNowPrice = stockItem.nowPrice?.toFloat()
                    stockAddTime = stockItem.dateTime
                    kLineType = qualityType.kLineType
                })
            }
        }
    }

    fun stockItemToKeyStock(
        stockItems: ArrayList<StockItemTodayModel>,
        qualityType: QualityType
    ): ArrayList<KeyStockModel> {
        return ArrayList<KeyStockModel>().apply {
            for (stockItem in stockItems) {
                add(KeyStockModel().apply {
                    stockCode = stockItem.stockCode
                    stockName = stockItem.stockName
                    stockRate = stockItem.stockRate
                    market = stockItem.market
                    turnoverRate = stockItem.turnoverRate
                    tradeAmount = stockItem.tradeAmount!!.toFloat()
                    tradeNum = stockItem.tradeNum!!.toFloat()
                    stockCostPrice = stockItem.nowPrice?.toFloat()
                    nowPrice = stockItem.nowPrice?.toFloat()
                    openPrice = stockItem.openPrice?.toFloat()
                    todayMax = stockItem.todayMax?.toFloat()
                    todayMin = stockItem.todayMin?.toFloat()
                    stockProfitRate = 0f
                    pinyin = stockItem.pinyin
                    loss = stockItem.loss
                    dateTime = stockItem.dateTime
                    addDateTime = stockItem.dateTime
                    buyStatus = StockBuyStatus.WaitTargetPrice.buyStatus
                    targetProfitRate = 1
                    targetLossRate = -1
                    promptStatus = false
                    promptTime = 0L
                    level = JudgeTrendUtils.judgeStockLevel(stockItem.stockCode!!)
                    blockRankName = LitePalDBase.stockBlockRankName(stockItem.stockCode!!)
                    blockRankRate = 0.0
                    lastFivePointDateTime =
                        if (stockItem.lastFivePointDateTime.isNullOrEmpty()) stockItem.dateTime
                        else stockItem.lastFivePointDateTime
                    secondPromptBuyPrice = 0f
                    naivePromptBuyPrice = 0f
                    debugId = stockItem.debugId
                })
            }
        }
    }

    fun stockItemToKeyStock(stockItems: ArrayList<KeyStockModel>): ArrayList<KeyStockModel> {
        return ArrayList<KeyStockModel>().apply {
            for (stockItem in stockItems) {
                add(KeyStockModel().apply {
                    stockCode = stockItem.stockCode
                    stockName = stockItem.stockName
                    stockRate = stockItem.stockRate
                    stockProfitRate = stockItem.stockProfitRate
                    stockCostPrice = stockItem.stockCostPrice
                    openPrice = stockItem.openPrice
                    nowPrice = stockItem.nowPrice
                    yestClosePrice = stockItem.yestClosePrice
                    fiveAveragePrice = stockItem.fiveAveragePrice
                    buyTargetPrice = stockItem.buyTargetPrice
                    todayMax = stockItem.todayMax
                    todayMin = stockItem.todayMin
                    tradeNum = stockItem.tradeNum
                    tradeAmount = stockItem.tradeAmount
                    turnoverRate = stockItem.turnoverRate
                    pinyin = stockItem.pinyin
                    loss = stockItem.loss
                    market = stockItem.market
                    dateTime = stockItem.dateTime
                    addDateTime = stockItem.addDateTime
                    buyStatus = stockItem.buyStatus
                    targetProfitRate = stockItem.targetProfitRate
                    targetLossRate = stockItem.targetLossRate
                    averagePriceDateTime = stockItem.averagePriceDateTime
                    promptBuyPrice = stockItem.promptBuyPrice
                    promptStatus = stockItem.promptStatus
                    promptTime = stockItem.promptTime
                    buyDateTime = stockItem.buyDateTime
                    level = stockItem.level
                    blockRankName = stockItem.blockRankName
                    blockRankRate = stockItem.blockRankRate
                    lastFivePointDateTime = stockItem.lastFivePointDateTime
                    secondPromptBuyPrice = stockItem.secondPromptBuyPrice
                    naivePromptBuyPrice = stockItem.naivePromptBuyPrice
                    debugId = stockItem.debugId
                })
            }
        }
    }

    fun blockRankDataToBlockRank(blockRank: ArrayList<KeyStockModel>): ArrayList<KeyStockModel> {
        return ArrayList<KeyStockModel>().apply {
            for (stockItem in blockRank) {
                add(KeyStockModel().apply {
                    stockCode = stockItem.stockCode
                    stockName = stockItem.stockName
                    stockRate = stockItem.stockRate
                    stockProfitRate = stockItem.stockProfitRate
                    stockCostPrice = stockItem.stockCostPrice
                    openPrice = stockItem.openPrice
                    nowPrice = stockItem.nowPrice
                    yestClosePrice = stockItem.yestClosePrice
                    fiveAveragePrice = stockItem.fiveAveragePrice
                    buyTargetPrice = stockItem.buyTargetPrice
                    todayMax = stockItem.todayMax
                    todayMin = stockItem.todayMin
                    tradeNum = stockItem.tradeNum
                    tradeAmount = stockItem.tradeAmount
                    turnoverRate = stockItem.turnoverRate
                    pinyin = stockItem.pinyin
                    loss = stockItem.loss
                    market = stockItem.market
                    dateTime = stockItem.dateTime
                    addDateTime = stockItem.addDateTime
                    buyStatus = stockItem.buyStatus
                    targetProfitRate = stockItem.targetProfitRate
                    targetLossRate = stockItem.targetLossRate
                    averagePriceDateTime = stockItem.averagePriceDateTime
                    promptBuyPrice = stockItem.promptBuyPrice
                    promptStatus = stockItem.promptStatus
                    promptTime = stockItem.promptTime
                    buyDateTime = stockItem.buyDateTime
                    level = stockItem.level
                    blockRankName = stockItem.blockRankName
                    blockRankRate = stockItem.blockRankRate
                    lastFivePointDateTime = stockItem.lastFivePointDateTime
                    debugId = stockItem.debugId
                })
            }
        }
    }

    fun kLineToStockHistoryData(
        stockCode: String,
        kLines: ArrayList<KLineEntity>,
        stockData: List<List<String>>
    ): ArrayList<StockHistoryModel> {
        if (kLines.size != stockData.size) return ArrayList()

        return ArrayList<StockHistoryModel>().apply {
            for (index in kLines.indices) {
                add(StockHistoryModel().apply {
                    this.stockCode = stockCode
                    dateTime = stockData[index][0]
                    openPrice = stockData[index][1].toFloat()
                    nowPrice = stockData[index][2].toFloat()
                    swing = stockData[index][3].toFloat()
                    stockRate = if (CommonUtils.isNumeric(stockData[index][4])) {
                        stockData[index][4].replace("%", "").toFloat()
                    } else
                        0.0f
                    minPrice = stockData[index][5].toFloat()
                    maxPrice = stockData[index][6].toFloat()
                    tradeNum = stockData[index][7].toFloat()
                    tradeAmount = stockData[index][8].toFloat()
                    turnoverRate = if (CommonUtils.isNumeric(stockData[index][9])) {
                        stockData[index][9].replace("%", "").toFloat()
                    } else
                        0.0f
                    yinyang = stockRate!! > 0
                    mA3Price = kLines[index].mA3Price
                    mA5Price = kLines[index].mA5Price
                    mA8Price = kLines[index].mA8Price
                    mA10Price = kLines[index].mA10Price
                    mA13Price = kLines[index].mA13Price
                    mA20Price = kLines[index].mA20Price
                    mA21Price = kLines[index].mA21Price
                    mA30Price = kLines[index].mA30Price
                    mA60Price = kLines[index].mA60Price
                    dea = kLines[index].dea
                    dif = kLines[index].dif
                    mAcd = kLines[index].macd
                    k = kLines[index].k
                    d = kLines[index].d
                    j = kLines[index].j
                    r = kLines[index].r
                    rsi = kLines[index].rsi
                    up = kLines[index].up
                    mb = kLines[index].mb
                    dn = kLines[index].dn
                    mA5Volume = kLines[index].mA5Volume
                    mA10Volume = kLines[index].mA10Volume
                })
            }
        }
    }

    fun stockHistoryToKLineEntity(stockHistories: ArrayList<StockHistoryModel>): ArrayList<KLineEntity> {
        return ArrayList<KLineEntity>().apply {
            stockHistories.forEach { history ->
                add(KLineEntity().apply {
                    Date = history.dateTime
                    Open = history.openPrice!!
                    High = history.maxPrice!!
                    Low = history.minPrice!!
                    Close = history.nowPrice!!
                    Volume = history.tradeNum!!
                    MA3Price = history.mA3Price
                    MA5Price = history.mA5Price
                    MA8Price = history.mA8Price
                    MA10Price = history.mA10Price
                    MA13Price = history.mA13Price
                    MA20Price = history.mA20Price
                    MA21Price = history.mA21Price
                    MA30Price = history.mA30Price
                    MA60Price = history.mA60Price
                    dea = history.dea
                    dif = history.dif
                    macd = history.mAcd
                    k = history.k
                    d = history.d
                    j = history.j
                    r = history.r
                    rsi = history.rsi
                    up = history.up
                    mb = history.mb
                    dn = history.dn
                    MA5Volume = history.mA5Volume
                    MA10Volume = history.mA10Volume
                    turnoverRate = history.turnoverRate!!
                })
            }
        }
    }

    fun kLineEntityToStockDayModel(
        currcapital: Float,
        kLines: ArrayList<KLineEntity>
    ): ArrayList<StockDayModel> {

        kLines.reverse()

        return ArrayList<StockDayModel>().apply {
            for (i in kLines.indices) {
                add(StockDayModel().apply {
                    dateTime = kLines[i].Date
                    openPrice = kLines[i].openPrice
                    nowPrice = kLines[i].closePrice
                    swing = kLines[i].closePrice - kLines[i].openPrice
                    stockRate = if (i == kLines.size - 1) {
                        0.00f
                    } else {
                        decimalFormat3.format((kLines[i].closePrice - kLines[i + 1].closePrice) / kLines[i + 1].closePrice * 100)
                            .toFloat()
                    }
                    minPrice = kLines[i].lowPrice
                    maxPrice = kLines[i].highPrice
                    tradeNum = kLines[i].volume.toLong()
                    tradeAmount = 0f
                    turnoverRate = decimalFormat3.format(kLines[i].volume / currcapital).toFloat()
                    yinyang = kLines[i].closePrice >= kLines[i].openPrice
                })
            }
        }
    }

    fun stockDayModelToKLineEntity(stockItems: ArrayList<StockDayModel>): ArrayList<KLineEntity> {

        val kLines = ArrayList<KLineEntity>().apply {
            stockItems.forEach { stockItem ->
                add(KLineEntity().apply {
                    Date = stockItem.dateTime
                    Open = stockItem.openPrice!!
                    Close = stockItem.nowPrice!!
                    Low = stockItem.minPrice!!
                    High = stockItem.maxPrice!!
                    Volume = stockItem.tradeNum?.toFloat()!!
                })
            }
        }

        kLines.reverse()
        DataHelper.calculate(kLines)
        kLines.reverse()

        return kLines
    }

    fun stockItemCalculate(stockItems: ArrayList<StockItemModel>): ArrayList<StockItemModel> {
        val kLines = ArrayList<KLineEntity>().apply {
            stockItems.forEach { stockItem ->
                add(KLineEntity().apply {
                    Date = stockItem.dateTime
                    Open = stockItem.openPrice!!.toFloat()
                    Close = stockItem.nowPrice!!.toFloat()
                    Low = stockItem.todayMin!!.toFloat()
                    High = stockItem.todayMax!!.toFloat()
                    Volume = stockItem.tradeNum?.toFloat()!!
                })
            }
        }

        DataHelper.calculate(kLines)

        for (index in stockItems.indices) {
            stockItems[index].mA3Price = kLines[index].mA3Price
            stockItems[index].mA5Price = kLines[index].mA5Price
            stockItems[index].mA8Price = kLines[index].mA8Price
            stockItems[index].mA10Price = kLines[index].mA10Price
            stockItems[index].mA13Price = kLines[index].mA13Price
            stockItems[index].mA20Price = kLines[index].mA20Price
            stockItems[index].mA21Price = kLines[index].mA21Price
            stockItems[index].mA30Price = kLines[index].mA30Price
            stockItems[index].mA60Price = kLines[index].mA60Price
            stockItems[index].dea = kLines[index].dea
            stockItems[index].dif = kLines[index].dif
            stockItems[index].macd = kLines[index].macd
            stockItems[index].k = kLines[index].k
            stockItems[index].d = kLines[index].d
            stockItems[index].j = kLines[index].j
            stockItems[index].r = kLines[index].r
            stockItems[index].rsi = kLines[index].rsi
            stockItems[index].up = kLines[index].up
            stockItems[index].mb = kLines[index].mb
            stockItems[index].dn = kLines[index].dn
            stockItems[index].mA5Volume = kLines[index].mA5Volume
            stockItems[index].mA10Volume = kLines[index].mA10Volume
        }

        stockItems.reverse()

        return stockItems
    }

    fun stockRecentItemCalculate(stockItems: ArrayList<StockItemRecentModel>): ArrayList<StockItemRecentModel> {
        val kLines = ArrayList<KLineEntity>().apply {
            stockItems.forEach { stockItem ->
                add(KLineEntity().apply {
                    Date = stockItem.dateTime
                    Open = stockItem.openPrice!!.toFloat()
                    Close = stockItem.nowPrice!!.toFloat()
                    Low = stockItem.todayMin!!.toFloat()
                    High = stockItem.todayMax!!.toFloat()
                    Volume = stockItem.tradeNum?.toFloat()!!
                })
            }
        }

        DataHelper.calculate(kLines)

        for (index in stockItems.indices) {
            stockItems[index].mA3Price = kLines[index].mA3Price
            stockItems[index].mA5Price = kLines[index].mA5Price
            stockItems[index].mA8Price = kLines[index].mA8Price
            stockItems[index].mA10Price = kLines[index].mA10Price
            stockItems[index].mA13Price = kLines[index].mA13Price
            stockItems[index].mA20Price = kLines[index].mA20Price
            stockItems[index].mA21Price = kLines[index].mA21Price
            stockItems[index].mA30Price = kLines[index].mA30Price
            stockItems[index].mA60Price = kLines[index].mA60Price
            stockItems[index].dea = kLines[index].dea
            stockItems[index].dif = kLines[index].dif
            stockItems[index].macd = kLines[index].macd
            stockItems[index].k = kLines[index].k
            stockItems[index].d = kLines[index].d
            stockItems[index].j = kLines[index].j
            stockItems[index].r = kLines[index].r
            stockItems[index].rsi = kLines[index].rsi
            stockItems[index].up = kLines[index].up
            stockItems[index].mb = kLines[index].mb
            stockItems[index].dn = kLines[index].dn
            stockItems[index].mA5Volume = kLines[index].mA5Volume
            stockItems[index].mA10Volume = kLines[index].mA10Volume
        }

        stockItems.reverse()

        return stockItems
    }

    fun stockItemToStockDayModel(
        stockItems: ArrayList<StockItemCalculateModel>
    ): ArrayList<StockDayModel> {
        return ArrayList<StockDayModel>().apply {
            stockItems.forEach { stockItem ->
                add(StockDayModel().apply {
                    dateTime = stockItem.dateTime
                    openPrice = stockItem.openPrice!!.toFloat()
                    nowPrice = stockItem.nowPrice!!.toFloat()
                    stockRate = stockItem.stockRate
                    minPrice = stockItem.todayMin!!.toFloat()
                    maxPrice = stockItem.todayMax!!.toFloat()
                    tradeNum = stockItem.tradeNum!!.toLong()
                    tradeAmount = stockItem.tradeAmount!!.toFloat()
                    turnoverRate = stockItem.turnoverRate
                    yinyang = nowPrice!! > openPrice!!
                })
            }
        }
    }

    fun stockItemToRecent(
        stockItems: ArrayList<StockItemModel>
    ): ArrayList<StockItemRecentModel> {
        return ArrayList<StockItemRecentModel>().apply {
            stockItems.forEach { stockItem ->
                add(StockItemRecentModel().apply {
                    stockCode = stockItem.stockCode
                    stockName = stockItem.stockName
                    stockRate = stockItem.stockRate
                    stockPrice = stockItem.stockPrice
                    openPrice = stockItem.openPrice
                    yestClosePrice = stockItem.yestClosePrice
                    nowPrice = stockItem.nowPrice
                    todayMax = stockItem.todayMax
                    todayMin = stockItem.todayMin
                    tradeNum = stockItem.tradeNum
                    tradeAmount = stockItem.tradeAmount
                    turnoverRate = stockItem.turnoverRate
                    pinyin = stockItem.pinyin
                    loss = stockItem.loss
                    swing = stockItem.swing
                    market = stockItem.market
                    dateTime = stockItem.dateTime
                    lastFivePointDateTime = stockItem.lastFivePointDateTime
                    debugId = stockItem.debugId
                    mA3Price = stockItem.mA3Price
                    mA5Price = stockItem.mA5Price
                    mA8Price = stockItem.mA8Price
                    mA10Price = stockItem.mA10Price
                    mA13Price = stockItem.mA13Price
                    mA20Price = stockItem.mA20Price
                    mA21Price = stockItem.mA21Price
                    mA30Price = stockItem.mA30Price
                    mA60Price = stockItem.mA60Price
                    dea = stockItem.dea
                    dif = stockItem.dif
                    macd = stockItem.macd
                    k = stockItem.k
                    d = stockItem.d
                    j = stockItem.j
                    r = stockItem.r
                    rsi = stockItem.rsi
                    up = stockItem.up
                    mb = stockItem.mb
                    dn = stockItem.dn
                    mA5Volume = stockItem.mA5Volume
                    mA10Volume = stockItem.mA10Volume
                })
            }
        }
    }

    fun stockItemTodayToRecent(
        stockItems: ArrayList<StockItemTodayModel>
    ): ArrayList<StockItemRecentModel> {
        return ArrayList<StockItemRecentModel>().apply {
            stockItems.forEach { stockItem ->
                add(StockItemRecentModel().apply {
                    stockCode = stockItem.stockCode
                    stockName = stockItem.stockName
                    stockRate = stockItem.stockRate
                    stockPrice = stockItem.stockPrice
                    openPrice = stockItem.openPrice
                    yestClosePrice = stockItem.yestClosePrice
                    nowPrice = stockItem.nowPrice
                    todayMax = stockItem.todayMax
                    todayMin = stockItem.todayMin
                    tradeNum = stockItem.tradeNum
                    tradeAmount = stockItem.tradeAmount
                    turnoverRate = stockItem.turnoverRate
                    pinyin = stockItem.pinyin
                    loss = stockItem.loss
                    market = stockItem.market
                    dateTime = stockItem.dateTime
                    lastFivePointDateTime = stockItem.lastFivePointDateTime
                    debugId = stockItem.debugId
                    mA3Price = stockItem.mA3Price
                    mA5Price = stockItem.mA5Price
                    mA8Price = stockItem.mA8Price
                    mA10Price = stockItem.mA10Price
                    mA13Price = stockItem.mA13Price
                    mA20Price = stockItem.mA20Price
                    mA21Price = stockItem.mA21Price
                    mA30Price = stockItem.mA30Price
                    mA60Price = stockItem.mA60Price
                    dea = stockItem.dea
                    dif = stockItem.dif
                    macd = stockItem.macd
                    k = stockItem.k
                    d = stockItem.d
                    j = stockItem.j
                    r = stockItem.r
                    rsi = stockItem.rsi
                    up = stockItem.up
                    mb = stockItem.mb
                    dn = stockItem.dn
                    mA5Volume = stockItem.mA5Volume
                    mA10Volume = stockItem.mA10Volume
                })
            }
        }
    }

    fun stockItemToday1026ToRecent(
        stockItems: ArrayList<StockItemToday1026Model>
    ): ArrayList<StockItemRecentModel> {
        return ArrayList<StockItemRecentModel>().apply {
            stockItems.forEach { stockItem ->
                add(StockItemRecentModel().apply {
                    stockCode = stockItem.stockCode
                    stockName = stockItem.stockName
                    stockRate = stockItem.stockRate
                    stockPrice = stockItem.stockPrice
                    openPrice = stockItem.openPrice
                    yestClosePrice = stockItem.yestClosePrice
                    nowPrice = stockItem.nowPrice
                    todayMax = stockItem.todayMax
                    todayMin = stockItem.todayMin
                    tradeNum = stockItem.tradeNum
                    tradeAmount = stockItem.tradeAmount
                    turnoverRate = stockItem.turnoverRate
                    pinyin = stockItem.pinyin
                    loss = stockItem.loss
                    swing = stockItem.swing
                    market = stockItem.market
                    dateTime = stockItem.dateTime
                    lastFivePointDateTime = stockItem.lastFivePointDateTime
                    debugId = stockItem.debugId
                    mA3Price = stockItem.mA3Price
                    mA5Price = stockItem.mA5Price
                    mA8Price = stockItem.mA8Price
                    mA10Price = stockItem.mA10Price
                    mA13Price = stockItem.mA13Price
                    mA20Price = stockItem.mA20Price
                    mA21Price = stockItem.mA21Price
                    mA30Price = stockItem.mA30Price
                    mA60Price = stockItem.mA60Price
                    dea = stockItem.dea
                    dif = stockItem.dif
                    macd = stockItem.macd
                    k = stockItem.k
                    d = stockItem.d
                    j = stockItem.j
                    r = stockItem.r
                    rsi = stockItem.rsi
                    up = stockItem.up
                    mb = stockItem.mb
                    dn = stockItem.dn
                    mA5Volume = stockItem.mA5Volume
                    mA10Volume = stockItem.mA10Volume
                })
            }
        }
    }

    fun stockItemTodayToToday(
        stockItems: ArrayList<StockItemTodayModel>
    ): ArrayList<StockItemToday1026Model> {
        return ArrayList<StockItemToday1026Model>().apply {
            stockItems.forEach { stockItem ->
                add(StockItemToday1026Model().apply {
                    stockCode = stockItem.stockCode
                    stockName = stockItem.stockName
                    stockRate = stockItem.stockRate
                    stockPrice = stockItem.stockPrice
                    openPrice = stockItem.openPrice
                    yestClosePrice = stockItem.yestClosePrice
                    nowPrice = stockItem.nowPrice
                    todayMax = stockItem.todayMax
                    todayMin = stockItem.todayMin
                    tradeNum = stockItem.tradeNum
                    tradeAmount = stockItem.tradeAmount
                    turnoverRate = stockItem.turnoverRate
                    pinyin = stockItem.pinyin
                    loss = stockItem.loss
                    market = stockItem.market
                    dateTime = stockItem.dateTime
                    lastFivePointDateTime = stockItem.lastFivePointDateTime
                    debugId = stockItem.debugId
                    mA3Price = stockItem.mA3Price
                    mA5Price = stockItem.mA5Price
                    mA8Price = stockItem.mA8Price
                    mA10Price = stockItem.mA10Price
                    mA13Price = stockItem.mA13Price
                    mA20Price = stockItem.mA20Price
                    mA21Price = stockItem.mA21Price
                    mA30Price = stockItem.mA30Price
                    mA60Price = stockItem.mA60Price
                    dea = stockItem.dea
                    dif = stockItem.dif
                    macd = stockItem.macd
                    k = stockItem.k
                    d = stockItem.d
                    j = stockItem.j
                    r = stockItem.r
                    rsi = stockItem.rsi
                    up = stockItem.up
                    mb = stockItem.mb
                    dn = stockItem.dn
                    mA5Volume = stockItem.mA5Volume
                    mA10Volume = stockItem.mA10Volume
                })
            }
        }
    }

    fun stockItemRecentToStockItem(
        stockItems: ArrayList<StockItemRecentModel>
    ): ArrayList<StockItemModel> {
        return ArrayList<StockItemModel>().apply {
            stockItems.forEach { stockItem ->
                add(StockItemModel().apply {
                    stockCode = stockItem.stockCode
                    stockName = stockItem.stockName
                    stockRate = stockItem.stockRate
                    stockPrice = stockItem.stockPrice
                    openPrice = stockItem.openPrice
                    yestClosePrice = stockItem.yestClosePrice
                    nowPrice = stockItem.nowPrice
                    todayMax = stockItem.todayMax
                    todayMin = stockItem.todayMin
                    tradeNum = stockItem.tradeNum
                    tradeAmount = stockItem.tradeAmount
                    turnoverRate = stockItem.turnoverRate
                    pinyin = stockItem.pinyin
                    loss = stockItem.loss
                    market = stockItem.market
                    dateTime = stockItem.dateTime
                    lastFivePointDateTime = stockItem.lastFivePointDateTime
                    debugId = stockItem.debugId
                    mA3Price = stockItem.mA3Price
                    mA5Price = stockItem.mA5Price
                    mA8Price = stockItem.mA8Price
                    mA10Price = stockItem.mA10Price
                    mA13Price = stockItem.mA13Price
                    mA20Price = stockItem.mA20Price
                    mA21Price = stockItem.mA21Price
                    mA30Price = stockItem.mA30Price
                    mA60Price = stockItem.mA60Price
                    dea = stockItem.dea
                    dif = stockItem.dif
                    macd = stockItem.macd
                    k = stockItem.k
                    d = stockItem.d
                    j = stockItem.j
                    r = stockItem.r
                    rsi = stockItem.rsi
                    up = stockItem.up
                    mb = stockItem.mb
                    dn = stockItem.dn
                    mA5Volume = stockItem.mA5Volume
                    mA10Volume = stockItem.mA10Volume
                })
            }
        }
    }

    fun stockItemRecentToCalculateModel(
        stockItems: ArrayList<StockItemRecentModel>
    ): ArrayList<StockItemCalculateModel> {
        return ArrayList<StockItemCalculateModel>().apply {
            stockItems.forEach { stockItem ->
                add(StockItemCalculateModel().apply {
                    stockCode = stockItem.stockCode
                    stockName = stockItem.stockName
                    stockRate = stockItem.stockRate
                    stockPrice = stockItem.stockPrice
                    openPrice = stockItem.openPrice
                    yestClosePrice = stockItem.yestClosePrice
                    nowPrice = stockItem.nowPrice
                    todayMax = stockItem.todayMax
                    todayMin = stockItem.todayMin
                    tradeNum = stockItem.tradeNum
                    tradeAmount = stockItem.tradeAmount
                    turnoverRate = stockItem.turnoverRate
                    pinyin = stockItem.pinyin
                    loss = stockItem.loss
                    market = stockItem.market
                    dateTime = stockItem.dateTime
                    lastFivePointDateTime = stockItem.lastFivePointDateTime
                    debugId = stockItem.debugId
                    mA3Price = stockItem.mA3Price
                    mA5Price = stockItem.mA5Price
                    mA8Price = stockItem.mA8Price
                    mA10Price = stockItem.mA10Price
                    mA13Price = stockItem.mA13Price
                    mA20Price = stockItem.mA20Price
                    mA21Price = stockItem.mA21Price
                    mA30Price = stockItem.mA30Price
                    mA60Price = stockItem.mA60Price
                    dea = stockItem.dea
                    dif = stockItem.dif
                    macd = stockItem.macd
                    k = stockItem.k
                    d = stockItem.d
                    j = stockItem.j
                    r = stockItem.r
                    rsi = stockItem.rsi
                    up = stockItem.up
                    mb = stockItem.mb
                    dn = stockItem.dn
                    mA5Volume = stockItem.mA5Volume
                    mA10Volume = stockItem.mA10Volume
                })
            }
        }
    }
}