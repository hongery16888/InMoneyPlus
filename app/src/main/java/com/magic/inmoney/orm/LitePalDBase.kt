package com.magic.inmoney.orm

import android.content.Context
import com.magic.inmoney.base.BaseApplication
import com.magic.inmoney.const.QualityType
import com.magic.inmoney.const.StockBuyStatus
import com.magic.inmoney.const.StockType
import com.magic.inmoney.model.*
import com.magic.inmoney.utilities.CommonUtils
import com.magic.inmoney.utilities.DateUtils
import com.magic.inmoney.utilities.ModelConversionUtils
import org.litepal.LitePal
import org.litepal.LitePal.initialize
import org.litepal.extension.deleteAll
import org.litepal.extension.find
import org.litepal.extension.findAll
import org.litepal.extension.saveAll

object LitePalDBase {
    fun initDB(context: Context?) {
        initialize(context!!)

        loadStock()
        loadTodayStockItem()
        loadFilterOption()
    }

    fun loadStock() {
        BaseApplication.instance?.stocks?.clear()
        BaseApplication.instance?.stocks?.addAll(LitePal.findAll<StockModel>())
    }

    fun queryCurrcapital(stockCode: String): Float {
        val items = LitePal.where("code = '$stockCode'").find(StockModel::class.java)
        return if (items.size > 0) {
            items[0].currcapital!!.toFloat()
        } else
            0f
    }

    fun queryAllTodayStockItem(): ArrayList<StockItemTodayModel> {
        val todayModel = LitePal.findAll(StockItemTodayModel::class.java)
        return if (todayModel.size > 0) {
            ArrayList<StockItemTodayModel>().apply {
                addAll(todayModel)
            }
        } else
            ArrayList<StockItemTodayModel>()
    }

    fun queryTodayStockItem(stockCode: String): StockItemTodayModel {
        val todayModel =
            LitePal.where("stockCode = '$stockCode'").find(StockItemTodayModel::class.java)
        return if (todayModel.size > 0) {
            todayModel[0]
        } else
            StockItemTodayModel()
    }

    fun loadTodayStockItem(filter: String = "") {
        BaseApplication.instance?.stockItemTodayModels?.clear()

        val stockItem = LitePal.findFirst(StockItemTodayModel::class.java)

        if (filter.isNotEmpty()) {
            BaseApplication.instance?.stockItemTodayModels?.addAll(
                LitePal.where("stockCode like '%$filter%' or stockName like '%$filter%' or pinyin like '%$filter%' and dateTime = '${stockItem.dateTime}'")
                    .find(StockItemTodayModel::class.java)
            )
        } else {
            if (stockItem != null)
                BaseApplication.instance?.stockItemTodayModels?.addAll(
                    LitePal
                        .where(
                            "dateTime = ?",
                            stockItem.dateTime
                        ).find(StockItemTodayModel::class.java)
                )
        }
    }

    fun loadStockItem(filter: String = "") {
        BaseApplication.instance?.stockItems?.clear()

        val stockItem = LitePal.findFirst(StockItemModel::class.java)

        if (filter.isNotEmpty()) {
            BaseApplication.instance?.stockItems?.addAll(
                LitePal.where("stockCode like '%$filter%' or stockName like '%$filter%' or pinyin like '%$filter%' and dateTime = '${stockItem.dateTime}'")
                    .find()
            )
        } else {
            if (stockItem != null)
                BaseApplication.instance?.stockItems?.addAll(
                    LitePal
                        .select(
                            "stockCode",
                            "stockName",
                            "stockRate",
                            "loss",
                            "stockPrice",
                            "turnoverRate"
                        )
                        .where(
                            "dateTime = ?",
                            stockItem.dateTime
                        ).find(StockItemModel::class.java)
                )
        }
    }

    fun queryStockItemByCode(stockCode: String): ArrayList<StockItemModel> {
        val stockItemModels =
            LitePal.where("stockCode = '$stockCode'").order("dateTime")
                .find(StockItemModel::class.java)

        return if (stockItemModels.isNullOrEmpty()) ArrayList<StockItemModel>()
        else
            ArrayList<StockItemModel>().apply {
                addAll(stockItemModels)
            }
    }

    fun queryStockItemByCodeLimit(stockCode: String, limit: Int = 50): ArrayList<StockItemModel> {
        val stockItemModels =
            LitePal.where("stockCode = '$stockCode'").limit(limit).order("dateTime desc")
                .find(StockItemModel::class.java)

        return if (stockItemModels.isNullOrEmpty()) ArrayList<StockItemModel>()
        else
            ArrayList<StockItemModel>().apply {
                addAll(stockItemModels)
            }
    }

    fun updateStockItem(dateTime: String) {
        if (dateTime.isNotEmpty()){

            val recentModel = ArrayList<StockItemRecentModel>().apply {
                addAll(LitePal.where("dateTime = '$dateTime'").find(StockItemRecentModel::class.java))
            }

            if (recentModel.size > 0) {
                LitePal.deleteAll<StockItemModel>("dateTime = '$dateTime'")
                val sss = ModelConversionUtils.stockItemRecentToStockItem(recentModel)
                sss.saveAll()
            }
        }
    }

    fun queryStockItemByCodeRecent(
        stockCode: String,
        limit: Int = 50
    ): ArrayList<StockItemRecentModel> {
        val stockItemModels =
            LitePal.where("stockCode = '$stockCode'").limit(limit).order("dateTime desc")
                .find(StockItemRecentModel::class.java)

        return if (stockItemModels.isNullOrEmpty()) ArrayList<StockItemRecentModel>()
        else
            ArrayList<StockItemRecentModel>().apply {
                addAll(stockItemModels)
            }
    }

    fun queryStockItemByCodeCalculate(
        stockCode: String
    ): ArrayList<StockItemCalculateModel> {
        val stockItemModels =
            LitePal.where("stockCode = '$stockCode'").order("dateTime desc")
                .find(StockItemCalculateModel::class.java)

        return if (stockItemModels.isNullOrEmpty()) ArrayList<StockItemCalculateModel>()
        else
            ArrayList<StockItemCalculateModel>().apply {
                addAll(stockItemModels)
            }
    }

    fun queryStockItemByCodeAllRecent(stockCode: String): ArrayList<StockItemRecentModel> {
        val stockItemModels = LitePal.where("stockCode = '$stockCode'").order("dateTime asc")
            .find(StockItemRecentModel::class.java)

        return if (stockItemModels.isNullOrEmpty()) ArrayList<StockItemRecentModel>()
        else
            ArrayList<StockItemRecentModel>().apply {
                addAll(stockItemModels)
            }
    }

    fun updateStockItemTodayToRecent(items: ArrayList<StockItemTodayModel>) {
        if (items.size > 0){
            LitePal.deleteAll<StockItemRecentModel>("dateTime = ?", items[0].dateTime)
            ModelConversionUtils.stockItemTodayToRecent(items).saveAll()
        }
    }

    fun deleteStockItemByCodeAllRecent(stockCode: String, items: ArrayList<StockItemRecentModel>) {
        if (items.size <= 200){
            LitePal.deleteAll<StockItemRecentModel>("stockCode = '$stockCode'")
            items.saveAll()
        }
    }

    fun deleteStockItemByCodeRecentLast(stockCode: String, dateTime: String){
        if (LitePal.where("stockCode = '$stockCode' and dateTime = '$dateTime'").count(StockItemRecentModel::class.java) > 0){
            LitePal.deleteAll<StockItemRecentModel>("stockCode = '$stockCode' and dateTime = '$dateTime'")
        }
    }

    fun queryRecentStockItemByCode(stockCode: String): ArrayList<StockItemRecentModel> {
        val stockItemModels =
            LitePal.where("stockCode = '$stockCode'").order("dateTime")
                .find(StockItemRecentModel::class.java)

        return if (stockItemModels.isNullOrEmpty()) ArrayList<StockItemRecentModel>()
        else
            ArrayList<StockItemRecentModel>().apply {
                addAll(stockItemModels)
            }
    }

    fun queryStockItem(stockCode: String): StockItemTodayModel {
        return LitePal.where("stockCode = '$stockCode'").findFirst(StockItemTodayModel::class.java)
    }

    private fun loadFilterOption() {
        val filterOptions = LitePal.findAll<FilterOptions>()

        if (filterOptions.size > 0) {
            BaseApplication.instance?.filterOptions = filterOptions[0]
        } else {
            val filterOption = FilterOptions().apply {
                stockTypes.addAll(ArrayList<String>().apply {
                    add(StockType.SHA.prefix)
                    add(StockType.SZA.prefix)
                    add(StockType.ZXB.prefix)
                })
                startRate = 2
                endRate = 10
                sortType = "stockRate"
                sortDirection = "desc"
                promptType = "music"
                buyPoint = "any"
                throughType = "normalThrough"
            }
            BaseApplication.instance?.filterOptions = filterOption
            filterOption.save()
        }
    }

    fun updateStockItems(items: ArrayList<StockItemTodayModel>) {
        if (items.size > 0) {
            LitePal.deleteAll(StockItemTodayModel::class.java)
            items.saveAll()
//            LitePal.deleteAll<StockItemToday1026Model>("dateTime = '2020-10-26'")
//            ModelConversionUtils.stockItemTodayToToday(items).saveAll()
//            println("------------------>1026Model : " + LitePal.count(StockItemToday1026Model::class.java))
            updateRecentStockItems(items)
        }
    }

    private fun updateRecentStockItems(items: ArrayList<StockItemTodayModel>) {

        if (LitePal.count(StockItemTodayModel::class.java) == BaseApplication.instance?.stocks?.size){
            LitePal.deleteAll<StockItemRecentModel>("dateTime = ?", items[0].dateTime)

            ModelConversionUtils.stockItemTodayToRecent(ArrayList<StockItemTodayModel>().apply {
                addAll(LitePal.findAll<StockItemTodayModel>())
            }).saveAll()
        }

    }

    fun updateStockItemsByCode(stockCode: String, items: ArrayList<StockItemModel>) {
        if (items.size > 0 && LitePal.deleteAll<StockItemModel>("stockCode = '$stockCode'") == items.size) {
//            println("------------------>" + items.saveAll())
            println(
                "------------------>Code : " + stockCode +
                        "------${LitePal.where("stockCode = '$stockCode'")
                            .count(StockItemModel::class.java)}"
            )
        }
    }

    fun deleteStockItems(stockCode: String) {
        LitePal.deleteAll<StockItemModel>("stockCode = '$stockCode'")
    }

    fun deleteAllStockItem() {
        LitePal.deleteAll(StockItemModel::class.java)
    }

    fun updateFilterOption() {
        LitePal.deleteAll(FilterOptions::class.java)
        val filterOptions = FilterOptions().apply {
            stockTypes.addAll(BaseApplication.instance?.filterOptions?.stockTypes!!)
            startRate = BaseApplication.instance?.filterOptions?.startRate!!
            endRate = BaseApplication.instance?.filterOptions?.endRate!!
            sortType = BaseApplication.instance?.filterOptions?.sortType!!
            sortDirection = BaseApplication.instance?.filterOptions?.sortDirection!!
            beforeDay = BaseApplication.instance?.filterOptions?.beforeDay!!
            promptType = BaseApplication.instance?.filterOptions?.promptType!!
            buyPoint = BaseApplication.instance?.filterOptions?.buyPoint!!
            throughType = BaseApplication.instance?.filterOptions?.throughType!!
            volumeStartRate = BaseApplication.instance?.filterOptions?.volumeStartRate!!
            volumeEndRate = BaseApplication.instance?.filterOptions?.volumeEndRate!!
            turnoverStartRate = BaseApplication.instance?.filterOptions?.turnoverStartRate!!
            turnoverEndRate = BaseApplication.instance?.filterOptions?.turnoverEndRate!!
            needTrendPrompt = BaseApplication.instance?.filterOptions?.needTrendPrompt!!
        }
        filterOptions.save()
        loadFilterOption()
    }

    fun deleteStockHistoryItems() {
        LitePal.deleteAll(StockHistoryModel::class.java)
    }

    fun queryStockHistoryItems(): ArrayList<StockHistoryModel> {
        return ArrayList<StockHistoryModel>().apply {
            addAll(LitePal.findAll(StockHistoryModel::class.java))
        }
    }

    fun queryStockHistoryItemsByCode(stockCode: String): ArrayList<StockHistoryModel> {
        val stockHistoryModels =
            LitePal.where("stockCode = ?", stockCode).find(StockHistoryModel::class.java)
        return if (stockHistoryModels.size > 0)
            ArrayList<StockHistoryModel>().apply {
                addAll(stockHistoryModels)
            }
        else
            ArrayList()
    }

    fun queryStockHistoryItemsByCode4Limit(
        stockCode: String,
        count: Int
    ): ArrayList<StockHistoryModel> {
        val stockHistoryModels = LitePal.where("stockCode = ?", stockCode).limit(count)
            .find(StockHistoryModel::class.java)
        return if (stockHistoryModels.size > 0)
            ArrayList<StockHistoryModel>().apply {
                addAll(stockHistoryModels)
            }
        else
            ArrayList()
    }

    fun queryFilterStockItem(): ArrayList<StockItemTodayModel> {

        return ArrayList<StockItemTodayModel>().apply {
            addAll(
                LitePal.where(splitSql())
                    .order("${BaseApplication.instance?.filterOptions?.sortType} ${BaseApplication.instance?.filterOptions?.sortDirection}")
                    .find(StockItemTodayModel::class.java)
            )
        }
    }

    fun queryKeyStockItem(): ArrayList<KeyStockModel> {
        return ArrayList<KeyStockModel>().apply {
            addAll(LitePal.findAll<KeyStockModel>())
        }
    }

    fun queryKeyStockItemForImport(): ArrayList<KeyStockModel> {
        return ArrayList<KeyStockModel>().apply {
            addAll(
                LitePal.where("buyStatus != ?", StockBuyStatus.Purchased.buyStatus)
                    .find(KeyStockModel::class.java)
            )
        }
    }

    fun queryKeyStockItem(stockCode: String): KeyStockModel? {
        val item = LitePal.where("stockCode = ?", stockCode).find(KeyStockModel::class.java)
        return if (item.size > 0)
            item[0]
        else null
    }

    fun updateKeyStock(data: ArrayList<KeyStockModel>) {
        if (data.isEmpty()) return
        LitePal.deleteAll(KeyStockModel::class.java)

        ModelConversionUtils.stockItemToKeyStock(data).saveAll()
    }

    fun updateKeyStock(stockItem: KeyStockModel): Boolean {
        return stockItem.updateAll("stockCode =?", stockItem.stockCode) == 1
    }

    fun deleteKeyStockByDate(dates: ArrayList<String>) {
        println("------------------>$dates")
        val prefixSql = StringBuilder()
        prefixSql.append("(")

        dates.forEach {
            prefixSql.append("lastFivePointDateTime = '$it'").append(" or ")
        }

        prefixSql.delete(prefixSql.count() - 4, prefixSql.count() - 1).append(") ")

        prefixSql.append(" and buyStatus not like '已购买'")

        LitePal.deleteAll<KeyStockModel>(prefixSql.toString())
    }

    fun updateKeyStockForPrompt(code: String) {
        val item = LitePal.where("stockCode = ?", code).find(KeyStockModel::class.java)[0]
        item.promptStatus = true
        item.promptTime = System.currentTimeMillis()
        item.updateAll("stockCode = ?", code)
    }

    fun queryStockByCode(code: String): ArrayList<StockItemModel> {
        return ArrayList<StockItemModel>().apply {
            addAll(LitePal.where("stockCode = ?", code).find(StockItemModel::class.java))
        }
    }

    private fun splitSql(): String {
        val filterOptions = BaseApplication.instance?.filterOptions
        val prefixSql = StringBuilder()
        prefixSql.append("(")
        for (prefix in filterOptions?.stockTypes!!) {
            val prefixs = prefix.split("|")
            if (prefix.length > 1) {
                for (pref in prefixs) {
                    prefixSql.append("stockCode like '$pref%' or ")
                }
            } else
                prefixSql.append("stockCode like '$prefix%' or ")
        }

        prefixSql.delete(prefixSql.count() - 4, prefixSql.count() - 1).append(")")

        prefixSql.append(" and stockRate >= '${filterOptions.startRate}' and stockRate <= '${filterOptions.endRate}'")

        val stockItem = LitePal.findFirst(StockItemTodayModel::class.java)

        if (stockItem != null)
            prefixSql.append(" and dateTime = '${stockItem.dateTime}'")

        prefixSql.append(" and stockName not like '*%' and stockName not like 'ST%' and stockName not like '退%' and stockName not like '%退'")

        return prefixSql.toString()
    }

    fun queryFavoriteStockItem(kLineTypeAndDate: String = ""): ArrayList<StockFavoriteModel> {
        return ArrayList<StockFavoriteModel>().apply {
            addAll(
                LitePal.where(kLineTypeAndDate).order("stockAddTime desc")
                    .find(StockFavoriteModel::class.java)
            )
        }
    }

    fun queryStatisticsStockItem(statisticsType: String = ""): ArrayList<StatisticsModel> {
        return ArrayList<StatisticsModel>().apply {
            addAll(
                LitePal.where("statisticsType = ?", statisticsType)
                    .find(StatisticsModel::class.java)
            )
        }
    }

    fun queryStatisticsStockItemCount(statisticsType: String = ""): Int {
        return LitePal.where("statisticsType = ?", statisticsType)
            .count(StatisticsModel::class.java)
    }

    fun deleteStatisticsStockItem(statisticsType: String = "") {
        LitePal.deleteAll<StatisticsModel>("statisticsType = ?", statisticsType)
    }

    fun queryStatisticsTotalStockItem(): ArrayList<StatisticsTotalModel> {
        return ArrayList<StatisticsTotalModel>().apply {
            addAll(
                LitePal.findAll(StatisticsTotalModel::class.java)
            )
        }
    }

    fun updateStatisticsTotalStockItem(item: StatisticsTotalModel) {
        LitePal.deleteAll<StatisticsTotalModel>(
            "stockLineTypeName = ? and buyPoint = ? and throughType = ? and needThirdDay = ?",
            item.stockLineTypeName, item.buyPoint, item.throughType, item.needThirdDay
        )
        item.save()
    }

    fun deleteStatisticsTotalStockItem(item: StatisticsTotalModel) {
        LitePal.deleteAll<StatisticsTotalModel>(
            "stockLineTypeName = ? and buyPoint = ? and throughType = ? and needThirdDay = ?",
            item.stockLineTypeName, item.buyPoint, item.throughType, item.needThirdDay
        )
    }

    fun deleteStatisticsTotalStockAllData() {
        LitePal.deleteAll<StatisticsTotalModel>()
    }

    fun deleteFavoriteStockByCode(stockCode: String) {
        LitePal.deleteAll<StockFavoriteModel>("stockCode = ?", stockCode)
    }

    fun deleteFavoriteStockItem(
        kLineType: String = "",
        dateTime: String = ""
    ): ArrayList<StockFavoriteModel> {
        return ArrayList<StockFavoriteModel>().apply {
            LitePal.deleteAll<StockFavoriteModel>(kLineType + dateTime)
        }
    }

    fun updateFavoriteStockForNowPrice(stockItems: ArrayList<StockItemModel>) {
        for (i in stockItems.indices) {
            val favoriteStocks =
                LitePal.where("stockCode = ?", stockItems[i].stockCode).find<StockFavoriteModel>()
            if (favoriteStocks.isNotEmpty()) {

                println(
                    "------------------>Date 1 : " + DateUtils.longToString(
                        System.currentTimeMillis() - 24 * 60 * 60 * 1000,
                        "yyyy-MM-dd"
                    )
                )

                println(
                    "------------------>Date 1 : " + DateUtils.longToString(
                        System.currentTimeMillis(),
                        "yyyy-MM-dd"
                    )
                )

                if (favoriteStocks[0].stockAddTime == DateUtils.longToString(
                        System.currentTimeMillis() - 24 * 60 * 60 * 1000,
                        "yyyy-MM-dd"
                    ) ||
                    favoriteStocks[0].stockAddTime == DateUtils.longToString(
                        System.currentTimeMillis(),
                        "yyyy-MM-dd"
                    )
                ) {
                    favoriteStocks[0].stockNextDayNowPrice = stockItems[i].nowPrice!!.toFloat()
                    favoriteStocks[0].stockNextDayMaxPrice = stockItems[i].todayMax!!.toFloat()
                    favoriteStocks[0].stockNowPrice = stockItems[i].nowPrice!!.toFloat()
                    println(
                        "------------------>Update Status : " + favoriteStocks[0].updateAll(
                            "stockCode = ?",
                            stockItems[i].stockCode
                        )
                    )
                }
            }
        }
    }

    fun addFavoriteStock(stockItems: ArrayList<StockItemTodayModel>, qualityType: QualityType) {
        LitePal.deleteAll<StockFavoriteModel>(
            "stockAddTime = ? and kLineType = ?",
            stockItems[0].dateTime,
            qualityType.kLineType
        )

        ModelConversionUtils.stockItemToFavoriteStock(stockItems, qualityType).saveAll()
    }

    fun addKeyStock(stockItems: ArrayList<StockItemTodayModel>, qualityType: QualityType) {
        val tempKeyStock = queryKeyStockItem()
        val tempStockItem = ArrayList<StockItemTodayModel>().apply { addAll(stockItems) }

        tempStockItem.forEach { stockItem ->
            tempKeyStock.forEach { keyStock ->
                if (keyStock.stockCode == stockItem.stockCode) {
                    stockItems.remove(stockItem)
                }
            }
        }

        ModelConversionUtils.stockItemToKeyStock(stockItems, qualityType).saveAll()
    }

    fun deleteKeyStockByCode(stockCode: String) {
        LitePal.deleteAll<KeyStockModel>("stockCode = ?", stockCode)
    }

    fun deleteKeyStock() {
        LitePal.deleteAll<KeyStockModel>()
    }

    fun addSingleFavoriteStock(stockItem: StockItemTodayModel, qualityType: QualityType) {
        LitePal.deleteAll<StockFavoriteModel>(
            "stockAddTime = ? and kLineType = ? and stockCode = ?",
            stockItem.dateTime,
            qualityType.kLineType,
            stockItem.stockCode
        )

        ModelConversionUtils.stockItemToFavoriteStock(ArrayList<StockItemTodayModel>().apply {
            add(stockItem)
        }, qualityType).saveAll()
    }

    fun queryStockInfo(stockCode: String): StockBaseInfo {
        val stockBaseInfo =
            LitePal.where("stockCode = ?", stockCode).find(StockBaseInfo::class.java)
        return if (stockBaseInfo.size > 0) stockBaseInfo[0]
        else StockBaseInfo().apply { stockName = "-1" }
    }

    fun queryStockReport(stockCode: String): StockReport {
        val stockReport = LitePal.where("stockCode = ?", stockCode).find(StockReport::class.java)
        return if (stockReport.size > 0) stockReport[0]
        else StockReport().apply { stockName = "-1" }
    }

    fun checkLowPREStock(
        stockCode: String,
        peRate: Int = 30,
        pbRate: Int = 10,
        lowRate: Int = 0
    ): Boolean {
        val stockBaseInfo =
            LitePal.where("stockCode = ?", stockCode).find(StockBaseInfo::class.java)
        if (stockBaseInfo.size > 0) {
            if (!CommonUtils.isNumeric(stockBaseInfo[0].per!!))
                return false
            if (!CommonUtils.isNumeric(stockBaseInfo[0].pbr!!)) {
                return false
            }
            return stockBaseInfo[0].per!!.toFloat() < peRate && stockBaseInfo[0].per!!.toFloat() > lowRate && stockBaseInfo[0].pbr!!.toFloat() < pbRate && stockBaseInfo[0].pbr!!.toFloat() > lowRate
        } else
            return false
    }

    fun checkHighRevenueStock(stockCode: String, rate: Int = 0): Boolean {
        val stockReport = LitePal.where("stockCode = ?", stockCode).find(StockReport::class.java)
        if (stockReport.size > 0) {
            if (!CommonUtils.isNumeric(stockReport[0].onYearGrowthRevenue!!))
                return false
            if (!CommonUtils.isNumeric(stockReport[0].onYearGrowthNetProfit!!)) {
                return false
            }
            return stockReport[0].onYearGrowthRevenue!!.toFloat() > rate && stockReport[0].onYearGrowthNetProfit!!.toFloat() > rate
        } else
            return false
    }

    fun checkNetAssetsRateStock(stockCode: String, rate: Int = 10): Boolean {
        val stockReport = LitePal.where("stockCode = ?", stockCode).find(StockReport::class.java)
        if (stockReport.size > 0) {
            if (!CommonUtils.isNumeric(stockReport[0].onNetAssetsRate!!))
                return false
            if (!CommonUtils.isNumeric(stockReport[0].onNetAssetsRate!!)) {
                return false
            }
            return stockReport[0].onNetAssetsRate!!.toFloat() > rate
        } else
            return false
    }

    fun stockBlockRankName(stockCode: String): String {
        val stockReport = LitePal.where("stockCode = ?", stockCode).find(StockReport::class.java)
        return if (stockReport.size > 0) {
            stockReport[0].industry!!
        } else ""
    }

    fun queryBlockRankData(): ArrayList<BlockModel> {

        return ArrayList<BlockModel>().apply {
            addAll(LitePal.findAll(BlockModel::class.java))
        }
    }

    fun updateBlockRankData(data: ArrayList<BlockModel>) {

        LitePal.deleteAll(BlockModel::class.java)

        data.saveAll()
    }
}