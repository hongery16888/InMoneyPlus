package com.magic.inmoney.viewmodel.key

import android.annotation.SuppressLint
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.magic.inmoney.base.BaseApplication
import com.magic.inmoney.base.BaseViewModel
import com.magic.inmoney.const.BuyPointPrompt
import com.magic.inmoney.const.StockBuyStatus
import com.magic.inmoney.model.BlockRankModel
import com.magic.inmoney.model.DaysStockInfoModel
import com.magic.inmoney.model.KeyStockModel
import com.magic.inmoney.orm.LitePalDBase
import com.magic.inmoney.utilities.DateUtils
import com.magic.inmoney.utilities.JudgeTrendUtils
import com.magic.upcoming.games.utilities.Event
import com.rx2androidnetworking.Rx2AndroidNetworking
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.max

class KeyStockViewModel : BaseViewModel() {

    var initData: ObservableField<Boolean> = ObservableField(true)
    var emptyStatus: ObservableField<Boolean> = ObservableField(false)
    private var stockItems = ArrayList<KeyStockModel>()
    var blockRanks = HashMap<String, Double>()
    private var decimalFormat2: DecimalFormat = DecimalFormat("#0.00")
    private var decimalFormat3: DecimalFormat = DecimalFormat("#0.000")
    var isRefresh: Boolean = true
    var showStatus = true

    private val _LoadingStatus = MutableLiveData<Event<Boolean>>()
    val loadingStatus: LiveData<Event<Boolean>>
        get() = _LoadingStatus

    fun loadingStatus() {
        initData.set(false)
        emptyStatus.set(stockItems.isEmpty())
        _LoadingStatus.postValue(Event(true))
    }

    private val _KeyStockData = MutableLiveData<Event<ArrayList<KeyStockModel>>>()
    val keyStockData: LiveData<Event<ArrayList<KeyStockModel>>>
        get() = _KeyStockData

    @SuppressLint("CheckResult")
    fun startMonitor() {
        Flowable.interval(5, 10, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.newThread())
            .filter { showStatus }
            .filter { (DateUtils.isCurrentInTimeScope() && DateUtils.isWorkday()) || isRefresh }
            .doOnNext {
                stockItems.clear()
                stockItems.addAll(LitePalDBase.queryKeyStockItem())
            }
            .filter {
                val empty = stockItems.isNotEmpty()
                if (!empty) {
                    loadingStatus()
                    _KeyStockData.postValue(Event(stockItems))
                }
                empty
            }
            .subscribeOn(Schedulers.newThread())
            .doOnNext {
                if ((it).toInt() % 10 == 0 || blockRanks.size == 0) {
                    loadBlockRankData()
                }
                syncStock()
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                println("------------------>Cycle Count : $it")
            }, {
                println("------------------>Monitor Error : ${it.message}")
            })
    }

    @SuppressLint("CheckResult")
    private fun syncStock() {

        var stocksListStr = ""

        for (stock in stockItems) {
            stocksListStr += stock.market + stock.stockCode + ","
        }

        Rx2AndroidNetworking.get("http://hq.sinajs.cn/format=text&list={codes}")
            .addPathParameter("codes", stocksListStr)
            .build()
            .stringObservable
            .subscribeOn(Schedulers.newThread())
            .doOnNext {
                println("------------------>Sync Data : $it")
                val infos = it.split("\n")
                for (i in infos.indices) {
                    if (infos[i].isEmpty()) continue
                    val items = infos[i].split(",")
                    stockItems[i].openPrice = items[1].toFloat()
                    stockItems[i].yestClosePrice = items[2].toFloat()
                    stockItems[i].nowPrice = items[3].toFloat()
                    stockItems[i].todayMax = items[4].toFloat()
                    stockItems[i].todayMin = items[5].toFloat()
                    stockItems[i].tradeNum = items[8].toFloat()
                    stockItems[i].tradeAmount = items[9].toFloat()
                    stockItems[i].dateTime = items[30]
                    var rate = 0f
                    if (items[2].toFloat() != 0f)
                        rate = (items[3].toFloat() - items[2].toFloat()) / items[2].toFloat() * 100
                    stockItems[i].loss = rate < 0
                    stockItems[i].stockRate = decimalFormat2.format(rate).toFloat()
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                syncAveragePrice()
            }, {
                println("------------------>Sync Error : ${it.message}")
            })
    }

    @SuppressLint("CheckResult")
    private fun syncAveragePrice() {

        var stocksListStr = ""

        for (stock in stockItems) {
            stocksListStr += "cn_" + stock.stockCode + ","
        }

        Rx2AndroidNetworking.get("https://q.stock.sohu.com/hisHq?code={code}&start={beforeDay}&end={today}&stat=1&order=D&period=d&rt=json")
            .addPathParameter("code", stocksListStr)
            .addPathParameter("today", DateUtils.today())
            .addPathParameter("beforeDay", DateUtils.beforeFiveDay(30))
            .build()
            .getObjectListObservable(DaysStockInfoModel::class.java)
            .subscribeOn(Schedulers.newThread())
            .doOnNext {
                for (i in it.indices) {
                    var totalThreePrice = 0f
                    var totalFivePrice = 0f
                    var totalEightPrice = 0f
                    var totalTenPrice = 0f
                    var totalThirteenPrice = 0f
                    var decimal = 1

                    if (it[i].hq[1][4].replace("%", "").toFloat() > 5 && stockItems[i].loss &&
                            DateUtils.isCurrentInTimeScope(10, 30, 15, 0)) {
                        stockItems[i].dust =
                            JudgeTrendUtils.fictitiousTradingVolume(it[i].hq[0][7].toLong()) > it[i].hq[1][7].toLong()
                    } else if (it[i].hq[2][4].replace("%", "")
                            .toFloat() > 5 && stockItems[i].loss
                        && DateUtils.isCurrentInTimeScope(10, 30, 15, 0)
                    ) {
                        stockItems[i].dust =
                            JudgeTrendUtils.fictitiousTradingVolume(it[i].hq[0][7].toLong()) > it[i].hq[2][7].toLong()
                    }

                    if (stockItems[i].dateTime!! != it[i].hq[0][0]) {

                        totalThreePrice += stockItems[i].nowPrice!!
                        for (index in 0..1) {
                            totalThreePrice += it[i].hq[index][2].toFloat()
                        }

                        totalFivePrice += stockItems[i].nowPrice!!
                        for (index in 0..3) {
                            totalFivePrice += it[i].hq[index][2].toFloat()
                            decimal = max(decimal, it[i].hq[index][2].split(".")[1].length)
                        }

                        totalEightPrice += stockItems[i].nowPrice!!
                        for (index in 0..6) {
                            totalEightPrice += it[i].hq[index][2].toFloat()
                        }

                        totalTenPrice += stockItems[i].nowPrice!!
                        for (index in 0..8) {
                            totalTenPrice += it[i].hq[index][2].toFloat()
                        }

                        totalThirteenPrice += stockItems[i].nowPrice!!
                        for (index in 0..11) {
                            totalThirteenPrice += it[i].hq[index][2].toFloat()
                        }

                        if (it[i].hq.size > 6 && stockItems[i].trendPrice == 0f){
                            val speed3AveragePrice = decimalFormat2.format((it[i].hq[1][2].toFloat() + it[i].hq[2][2].toFloat() + it[i].hq[3][2].toFloat()) / 3f).toDouble()
                            val speed2AveragePrice = decimalFormat2.format((it[i].hq[0][2].toFloat() + it[i].hq[1][2].toFloat() + it[i].hq[2][2].toFloat()) / 3f).toDouble()
                            val speed1AveragePrice = decimalFormat2.format((stockItems[i].nowPrice!! + it[i].hq[0][2].toFloat() + it[i].hq[1][2].toFloat()) / 3f).toDouble()
                            val speed2Rate = decimalFormat3.format((speed2AveragePrice - speed3AveragePrice) / speed3AveragePrice * 100f).toDouble()
                            val speed1Rate = decimalFormat3.format((speed1AveragePrice - speed2AveragePrice) / speed2AveragePrice * 100f).toDouble()

                            val canSpeed = when {
                                speed2Rate.toFloat() == 0f -> {
                                    speed1Rate.toFloat()
                                }
                                speed1Rate.toFloat() == 0f -> 0f
                                else -> decimalFormat3.format(speed1Rate / speed2Rate * speed1Rate /100f).toFloat()
                            }

                            val buyPrice = speed1AveragePrice * (1f + canSpeed)

                            stockItems[i].trendPrice = decimalFormat3.format(buyPrice).toFloat()

                            if (stockItems[i].stockCode == "600260"){
                                println("------------------>600260 : " + buyPrice)
                            }
                        }
                    } else {

                        for (index in 0..2) {
                            totalThreePrice += it[i].hq[index][2].toFloat()
                        }

                        for (index in 0..4) {
                            totalFivePrice += it[i].hq[index][2].toFloat()
                            decimal = max(decimal, it[i].hq[index][2].split(".")[1].length)
                        }

                        for (index in 0..7) {
                            totalEightPrice += it[i].hq[index][2].toFloat()
                        }

                        for (index in 0..9) {
                            totalTenPrice += it[i].hq[index][2].toFloat()
                        }

                        for (index in 0..12) {
                            totalThirteenPrice += it[i].hq[index][2].toFloat()
                        }

                        if (it[i].hq.size > 6 && stockItems[i].trendPrice == 0f){
                            val speed3AveragePrice = decimalFormat2.format((it[i].hq[2][2].toFloat() + it[i].hq[3][2].toFloat() + it[i].hq[4][2].toFloat()) / 3f).toDouble()
                            val speed2AveragePrice = decimalFormat2.format((it[i].hq[1][2].toFloat() + it[i].hq[2][2].toFloat() + it[i].hq[3][2].toFloat()) / 3f).toDouble()
                            val speed1AveragePrice = decimalFormat2.format((it[i].hq[0][2].toFloat() + it[i].hq[1][2].toFloat() + it[i].hq[2][2].toFloat()) / 3f).toDouble()
                            val speed2Rate = decimalFormat3.format((speed2AveragePrice - speed3AveragePrice) / speed3AveragePrice * 100f).toDouble()
                            val speed1Rate = decimalFormat3.format((speed1AveragePrice - speed2AveragePrice) / speed2AveragePrice * 100f).toDouble()

                            val canSpeed = when {
                                speed2Rate.toFloat() == 0f -> {
                                    speed1Rate.toFloat()
                                }
                                speed1Rate.toFloat() == 0f -> 0f
                                else -> decimalFormat3.format(speed1Rate / speed2Rate * speed1Rate /100f).toFloat()
                            }

                            val buyPrice = speed1AveragePrice * (1f + canSpeed)

                            stockItems[i].trendPrice = decimalFormat3.format(buyPrice).toFloat()

                            if (stockItems[i].stockCode == "600260"){
                                println("------------------>600260 : " + buyPrice)
                            }
                        }

                    }

                    if (decimal == 3) {

                        if (stockItems[i].debugId == 21 || stockItems[i].debugId == 22){
                            stockItems[i].fiveAveragePrice =
                                decimalFormat3.format(totalThreePrice / 3).toFloat()
                            stockItems[i].eightAveragePrice =
                                decimalFormat3.format(totalFivePrice / 5).toFloat()
                            stockItems[i].thirteenAveragePrice =
                                decimalFormat3.format(totalTenPrice / 10).toFloat()
                        }else{
                            stockItems[i].fiveAveragePrice =
                                decimalFormat3.format(totalFivePrice / 5).toFloat()
                            stockItems[i].eightAveragePrice =
                                decimalFormat3.format(totalEightPrice / 8).toFloat()
                            stockItems[i].thirteenAveragePrice =
                                decimalFormat3.format(totalThirteenPrice / 13).toFloat()
                        }

//                        stockItems[i].fiveAveragePrice =
//                            decimalFormat3.format(totalFivePrice / 5).toFloat()
//                        stockItems[i].eightAveragePrice =
//                            decimalFormat3.format(totalEightPrice / 8).toFloat()
//                        stockItems[i].thirteenAveragePrice =
//                            decimalFormat3.format(totalThirteenPrice / 13).toFloat()

                        val sortAverage = ArrayList<Float>().apply {
                            add(stockItems[i].fiveAveragePrice!!)
                            add(stockItems[i].eightAveragePrice!!)
                            add(stockItems[i].thirteenAveragePrice!!)
                        }

                        sortAverage.sort()

                        if (stockItems[i].openPrice!! < sortAverage[2]) {
                            sortAverage[2] = sortAverage[1]
                        } else if (stockItems[i].openPrice!! < sortAverage[1]) {
                            sortAverage[1] = sortAverage[0]
                        }

                        when (BaseApplication.instance?.filterOptions?.buyPoint) {
                            BuyPointPrompt.UpLine.point -> {
                                stockItems[i].buyTargetPrice =
                                    decimalFormat3.format(sortAverage[2]).toFloat()
                                stockItems[i].promptBuyPrice =
                                    stockItems[i].buyTargetPrice!! * 1.01f

                                stockItems[i].secondPromptBuyPrice = decimalFormat3.format(
                                    sortAverage[1]
                                ).toFloat()
                                stockItems[i].naivePromptBuyPrice = decimalFormat3.format(
                                    sortAverage[0]
                                ).toFloat()

                                stockItems[i].topPrice = sortAverage[2]
                            }
                            BuyPointPrompt.MidLine.point -> {
                                stockItems[i].buyTargetPrice = decimalFormat3.format(
                                    sortAverage[1]
                                ).toFloat()

                                stockItems[i].promptBuyPrice =
                                    stockItems[i].buyTargetPrice!! * 1.01f

                                stockItems[i].secondPromptBuyPrice =
                                    decimalFormat3.format(sortAverage[0]).toFloat()
                                stockItems[i].naivePromptBuyPrice =
                                    decimalFormat3.format(sortAverage[0] * 0.99f).toFloat()

                                stockItems[i].topPrice = sortAverage[1]
                            }
                            BuyPointPrompt.DownLine.point -> {
                                stockItems[i].buyTargetPrice = decimalFormat3.format(
                                    sortAverage[0]
                                ).toFloat()
                                stockItems[i].promptBuyPrice =
                                    stockItems[i].buyTargetPrice!! * 1.01f
                                stockItems[i].secondPromptBuyPrice =
                                    decimalFormat3.format(sortAverage[0] * 0.99f).toFloat()
                                stockItems[i].naivePromptBuyPrice =
                                    decimalFormat3.format(sortAverage[0] * 0.98f).toFloat()

                                stockItems[i].topPrice = sortAverage[0]
                            }
                            else -> {
                                when {
                                    stockItems[i].nowPrice!! < sortAverage[0] -> {
                                        stockItems[i].promptBuyPrice =
                                            decimalFormat3.format(stockItems[i].nowPrice!! * 0.99f)
                                                .toFloat()
                                        stockItems[i].secondPromptBuyPrice =
                                            decimalFormat3.format(stockItems[i].nowPrice!! * 0.98f)
                                                .toFloat()
                                        stockItems[i].naivePromptBuyPrice =
                                            decimalFormat3.format(stockItems[i].nowPrice!! * 0.97f)
                                                .toFloat()
                                    }
                                    stockItems[i].nowPrice!! < sortAverage[1] -> {
                                        stockItems[i].buyTargetPrice = decimalFormat3.format(
                                            sortAverage[0]
                                        ).toFloat()
                                        stockItems[i].promptBuyPrice =
                                            stockItems[i].buyTargetPrice!! * 1.01f
                                        stockItems[i].secondPromptBuyPrice =
                                            decimalFormat3.format(sortAverage[0] * 0.99f).toFloat()
                                        stockItems[i].naivePromptBuyPrice =
                                            decimalFormat3.format(sortAverage[0] * 0.98f).toFloat()
                                    }
                                    stockItems[i].nowPrice!! < sortAverage[2] -> {
                                        stockItems[i].buyTargetPrice = decimalFormat3.format(
                                            sortAverage[1]
                                        ).toFloat()

                                        stockItems[i].promptBuyPrice =
                                            stockItems[i].buyTargetPrice!! * 1.01f

                                        stockItems[i].secondPromptBuyPrice =
                                            decimalFormat3.format(sortAverage[0]).toFloat()
                                        stockItems[i].naivePromptBuyPrice =
                                            decimalFormat3.format(sortAverage[0] * 0.99f).toFloat()
                                    }
                                    else -> {
                                        stockItems[i].buyTargetPrice =
                                            decimalFormat3.format(sortAverage[2]).toFloat()
                                        stockItems[i].promptBuyPrice =
                                            stockItems[i].buyTargetPrice!! * 1.01f

                                        stockItems[i].secondPromptBuyPrice = decimalFormat3.format(
                                            sortAverage[1]
                                        ).toFloat()
                                        stockItems[i].naivePromptBuyPrice = decimalFormat3.format(
                                            sortAverage[0]
                                        ).toFloat()
                                    }
                                }

                                stockItems[i].topPrice = sortAverage[2]
                            }
                        }
                    } else {
                        if (stockItems[i].debugId == 21){
                            stockItems[i].fiveAveragePrice =
                                decimalFormat2.format(totalThreePrice / 3).toFloat()
                            stockItems[i].eightAveragePrice =
                                decimalFormat2.format(totalFivePrice / 5).toFloat()
                            stockItems[i].thirteenAveragePrice =
                                decimalFormat2.format(totalTenPrice / 10).toFloat()
                        }else{
                            stockItems[i].fiveAveragePrice =
                                decimalFormat2.format(totalFivePrice / 5).toFloat()
                            stockItems[i].eightAveragePrice =
                                decimalFormat2.format(totalEightPrice / 8).toFloat()
                            stockItems[i].thirteenAveragePrice =
                                decimalFormat2.format(totalThirteenPrice / 13).toFloat()
                        }

//                        stockItems[i].fiveAveragePrice =
//                            decimalFormat2.format(totalFivePrice / 5).toFloat()
//                        stockItems[i].eightAveragePrice =
//                            decimalFormat2.format(totalEightPrice / 8).toFloat()
//                        stockItems[i].thirteenAveragePrice =
//                            decimalFormat2.format(totalThirteenPrice / 13).toFloat()

                        val sortAverage = ArrayList<Float>().apply {
                            add(stockItems[i].fiveAveragePrice!!)
                            add(stockItems[i].eightAveragePrice!!)
                            add(stockItems[i].thirteenAveragePrice!!)
                        }

                        sortAverage.sort()

                        if (stockItems[i].openPrice!! < sortAverage[2]) {
                            sortAverage[2] = sortAverage[1]
                        } else if (stockItems[i].openPrice!! < sortAverage[1]) {
                            sortAverage[1] = sortAverage[0]
                        }

                        when (BaseApplication.instance?.filterOptions?.buyPoint) {
                            BuyPointPrompt.UpLine.point -> {
                                stockItems[i].buyTargetPrice =
                                    decimalFormat2.format(sortAverage[2]).toFloat()
                                stockItems[i].promptBuyPrice =
                                    stockItems[i].buyTargetPrice!! * 1.01f

                                stockItems[i].secondPromptBuyPrice = decimalFormat2.format(
                                    sortAverage[1]
                                ).toFloat()
                                stockItems[i].naivePromptBuyPrice = decimalFormat2.format(
                                    sortAverage[0]
                                ).toFloat()

                                stockItems[i].topPrice = sortAverage[2]
                            }
                            BuyPointPrompt.MidLine.point -> {
                                stockItems[i].buyTargetPrice = decimalFormat2.format(
                                    sortAverage[1]
                                ).toFloat()
                                stockItems[i].promptBuyPrice =
                                    stockItems[i].buyTargetPrice!! * 1.01f
                                stockItems[i].secondPromptBuyPrice =
                                    decimalFormat2.format(sortAverage[0]).toFloat()
                                stockItems[i].naivePromptBuyPrice =
                                    decimalFormat2.format(sortAverage[0] * 0.99f).toFloat()

                                stockItems[i].topPrice = sortAverage[1]
                            }
                            BuyPointPrompt.DownLine.point -> {
                                stockItems[i].buyTargetPrice = decimalFormat2.format(
                                    sortAverage[0]
                                ).toFloat()
                                stockItems[i].promptBuyPrice =
                                    stockItems[i].buyTargetPrice!! * 1.01f
                                stockItems[i].secondPromptBuyPrice =
                                    decimalFormat2.format(sortAverage[0] * 0.99f).toFloat()
                                stockItems[i].naivePromptBuyPrice =
                                    decimalFormat2.format(sortAverage[0] * 0.98f).toFloat()

                                stockItems[i].topPrice = sortAverage[0]
                            }
                            else -> {
                                when {
                                    stockItems[i].nowPrice!! < sortAverage[0] -> {
                                        stockItems[i].promptBuyPrice =
                                            decimalFormat2.format(stockItems[i].nowPrice!! * 0.99f)
                                                .toFloat()
                                        stockItems[i].secondPromptBuyPrice =
                                            decimalFormat2.format(stockItems[i].nowPrice!! * 0.98f)
                                                .toFloat()
                                        stockItems[i].naivePromptBuyPrice =
                                            decimalFormat2.format(stockItems[i].nowPrice!! * 0.97f)
                                                .toFloat()
                                    }
                                    stockItems[i].nowPrice!! < sortAverage[1] -> {
                                        stockItems[i].buyTargetPrice = decimalFormat2.format(
                                            sortAverage[0]
                                        ).toFloat()
                                        stockItems[i].promptBuyPrice =
                                            stockItems[i].buyTargetPrice!! * 1.01f
                                        stockItems[i].secondPromptBuyPrice =
                                            decimalFormat2.format(sortAverage[0] * 0.99f).toFloat()
                                        stockItems[i].naivePromptBuyPrice =
                                            decimalFormat2.format(sortAverage[0] * 0.98f).toFloat()
                                    }
                                    stockItems[i].nowPrice!! < sortAverage[2] -> {
                                        stockItems[i].buyTargetPrice = decimalFormat2.format(
                                            sortAverage[1]
                                        ).toFloat()
                                        stockItems[i].promptBuyPrice =
                                            stockItems[i].buyTargetPrice!! * 1.01f
                                        stockItems[i].secondPromptBuyPrice =
                                            decimalFormat2.format(sortAverage[0]).toFloat()
                                        stockItems[i].naivePromptBuyPrice =
                                            decimalFormat2.format(sortAverage[0] * 0.99f).toFloat()
                                    }
                                    else -> {
                                        stockItems[i].buyTargetPrice =
                                            decimalFormat2.format(sortAverage[2]).toFloat()
                                        stockItems[i].promptBuyPrice =
                                            stockItems[i].buyTargetPrice!! * 1.01f

                                        stockItems[i].secondPromptBuyPrice = decimalFormat2.format(
                                            sortAverage[1]
                                        ).toFloat()
                                        stockItems[i].naivePromptBuyPrice = decimalFormat2.format(
                                            sortAverage[0]
                                        ).toFloat()
                                    }
                                }

                                stockItems[i].topPrice = sortAverage[2]
                            }
                        }
                    }
                    stockItems[i].averagePriceDateTime = stockItems[i].dateTime

                    if (stockItems[i].buyStatus != StockBuyStatus.Purchased.buyStatus && stockItems[i].buyStatus != StockBuyStatus.Sold.buyStatus) {
                        if (stockItems[i].nowPrice!!  < stockItems[i].buyTargetPrice!! * 1.005f) {
                            stockItems[i].buyStatus = StockBuyStatus.ProOrder.buyStatus
                        } else if (stockItems[i].nowPrice!! < stockItems[i].buyTargetPrice!! * 1.01f) {
                            stockItems[i].buyStatus = StockBuyStatus.PromptBuy.buyStatus
                            stockItems[i].stockCostPrice = stockItems[i].nowPrice
                        } else if (stockItems[i].todayMax!! > stockItems[i].buyTargetPrice!! && stockItems[i].todayMin!! < stockItems[i].buyTargetPrice!!) {
                            stockItems[i].buyStatus = StockBuyStatus.ReachTargetPrice.buyStatus
                            stockItems[i].stockCostPrice = stockItems[i].buyTargetPrice
                        } else {
                            stockItems[i].buyStatus = StockBuyStatus.WaitTargetPrice.buyStatus
                            stockItems[i].stockCostPrice = stockItems[i].nowPrice
                        }

                        if (blockRanks.size > 0) {
                            stockItems[i].blockRankRate = blockRanks[stockItems[i].blockRankName]
                        }

                        if (stockItems[i].dateTime == stockItems[i].lastFivePointDateTime || stockItems[i].dust)
                            stockItems[i].buyStatus = StockBuyStatus.WaitTargetPrice.buyStatus

                        if (stockItems[i].nowPrice!! < stockItems[i].topPrice!!){
                            stockItems[i].buyStatus = StockBuyStatus.BuyPrice.buyStatus
                            stockItems[i].stockCostPrice = stockItems[i].nowPrice
                        }

                    }
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                _KeyStockData.value = Event(ArrayList<KeyStockModel>().apply {
                    addAll(stockItems)
                })
                loadingStatus()
            }, {
                println("------------------>Sync Error : ${it.message}")
                loadingStatus()
            })
    }

    @SuppressLint("CheckResult")
    fun loadBlockRankData() {

        if (!DateUtils.isCurrentInTimeScope() || !DateUtils.isWorkday()) {
            val temp = LitePalDBase.queryBlockRankData()
            if (temp.isNotEmpty()) {
                blockRanks.clear()
                temp.forEach { bean ->
                    blockRanks[bean.blockName!!] = bean.pxChangeRate
                }
            } else
                syncBlockRankData()
        } else
            syncBlockRankData()

    }

    @SuppressLint("CheckResult")
    private fun syncBlockRankData() {
        Rx2AndroidNetworking.get("https://stockapi.market.alicloudapi.com/hs_blockrank")
            .addQueryParameter("appcode", "2a50041f822048d6ad358a7a8904d335")
            .addQueryParameter("limit", "100")
            .addQueryParameter("page", "1")
            .addQueryParameter("type", "2")
            .addHeaders("Authorization", "APPCODE 2a50041f822048d6ad358a7a8904d335")
            .build()
            .getObjectObservable(BlockRankModel::class.java)
            .subscribeOn(Schedulers.newThread())
            .doOnNext {
                if (it.data != null) {
                    blockRanks.clear()
                    it.data.candle.forEach { bean ->
                        blockRanks[bean.blockName!!] = bean.pxChangeRate
                    }

                    LitePalDBase.updateBlockRankData(it.data.candle)
                }

            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                println("------------------>BlockRank : $it")
            }, {
                println("------------------>Sync Error : ${it.message}")
            })
    }
}