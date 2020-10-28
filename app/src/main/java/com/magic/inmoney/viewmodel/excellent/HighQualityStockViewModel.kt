package com.magic.inmoney.viewmodel.excellent

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Handler
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.magic.inmoney.base.BaseApplication
import com.magic.inmoney.base.BaseViewModel
import com.magic.inmoney.const.QualityType
import com.magic.inmoney.const.WeekDay
import com.magic.inmoney.model.*
import com.magic.inmoney.orm.LitePalDBase
import com.magic.inmoney.utilities.DateUtils
import com.magic.inmoney.utilities.JudgeTrendUtils
import com.magic.inmoney.utilities.ModelConversionUtils
import com.magic.upcoming.games.utilities.Event
import com.rx2androidnetworking.Rx2AndroidNetworking
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class HighQualityStockViewModel : BaseViewModel() {

    var initData: ObservableField<Boolean> = ObservableField(true)
    var emptyStatus: ObservableField<Boolean> = ObservableField(false)
    var beforeOneDay: ObservableField<Boolean> = ObservableField(false)
    var highQuality: ObservableField<Boolean> = ObservableField(false)
    private var debugID = 0
    private var readCount = 50
    lateinit var activity: Activity
    var highQualityStocks = ArrayList<StockItemTodayModel>()
    var qualityStocks = ArrayList<StockItemModel>()
    var tempStockItems = ArrayList<StockItemModel>()
    var selectType = QualityType.ThreeCannon
    var qualityStockCount = 0
    var judgeStockCount = 0
    var defaultJudgeCount = 20
    var lastStockCount = 0
    var localStockDayModels = ArrayList<LocalDayStockItemModel>()
    var localPage = 0
    var stockItemAllArray = ArrayList<ArrayList<StockItemCalculateModel>>()
    var stockDayModels = ArrayList<ArrayList<StockDayModel>>()
    var stockItems = ArrayList<StockItemCalculateModel>()

    private val _LoadingProgress = MutableLiveData<Event<Int>>()
    val loadingProgress: LiveData<Event<Int>>
        get() = _LoadingProgress

    private val _LoadingMaxProgress = MutableLiveData<Event<Int>>()
    val loadingMaxProgress: LiveData<Event<Int>>
        get() = _LoadingMaxProgress

    private val _LoadingStatus = MutableLiveData<Event<Boolean>>()
    val loadingStatus: LiveData<Event<Boolean>>
        get() = _LoadingStatus

    private val _QualityStockItem = MutableLiveData<Event<ArrayList<StockItemTodayModel>>>()
    val qualityStockItem: LiveData<Event<ArrayList<StockItemTodayModel>>>
        get() = _QualityStockItem

    private fun loadingStatus() {
        initData.set(false)
        emptyStatus.set(highQualityStocks.size == 0)
        _LoadingStatus.value = Event(true)
    }

    @SuppressLint("CheckResult")
    fun initStockItemCalculate() {

        _LoadingMaxProgress.value = Event(BaseApplication.instance?.stocks?.size!!)
        stockDayModels.clear()
        stockItemAllArray.clear()
        localPage = 0

        Flowable.just("")
            .subscribeOn(Schedulers.newThread())
            .doOnNext {
                BaseApplication.instance?.stocks?.forEach {
                    println("------------------>LocalPage : $localPage")
//                    if (localPage < 100){
                        var filterStatus = true
                        val prefixCode = it.code!!.substring(0, 3)
                        localPage += 1
                        for (prefix in BaseApplication.instance?.filterOptions?.stockTypes!!) {
                            if (prefix.contains(prefixCode)) {
                                filterStatus = false
                                break
                            }
                        }

                        if (!filterStatus) {
                            stockItems.clear()
                            stockItems = LitePalDBase.queryStockItemByCodeCalculate(it.code!!)
                            if (stockItems.size > 0) {
                                stockItemAllArray.add(ArrayList<StockItemCalculateModel>().apply {
                                    addAll(
                                        stockItems
                                    )
                                })

                                stockDayModels.add(
                                    ModelConversionUtils.stockItemToStockDayModel(
                                        stockItems
                                    )
                                )
                            }
                        }
//                    }
                    _LoadingProgress.postValue(Event(localPage))
                }
            }
            .doOnError {
                println("------------------>567890")
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                println("------------------>235679")
                loadingStatus()
            }, {
                println("------------------>6846358 : ${it.message}")
                loadingStatus()
            })
    }

    @SuppressLint("CheckResult")
    fun loadQualityStockModify(debugId: Int = 0, subCount: Int = 50) {

        if (initData.get()!!) {
            toast("稍等片刻，数据计算中。。。")
            return
        }

        debugID = debugId
        readCount = subCount
        initData.set(true)
        emptyStatus.set(false)

        qualityStocks.clear()
        highQualityStocks.clear()
        localPage = 0

        if (stockItemAllArray.size > 0) {
//            _LoadingMaxProgress.value = Event(stockItemAllArray.size)

            Flowable.just("")
                .subscribeOn(Schedulers.newThread())
                .doOnNext {
                    for (index in stockItemAllArray.indices) {
                        localPage += 1
                        val stockItemFist = stockItemAllArray[index]

                        val stockDayModels = if (beforeOneDay.get()!!) {
                            ArrayList<StockDayModel>().apply {
                                val temp = stockDayModels[index].subList(
                                    BaseApplication.instance?.filterOptions?.beforeDay!!,
                                    stockDayModels[index].size
                                )
                                addAll(temp)
                            }
                        } else {
                            ArrayList<StockDayModel>().apply {
                                val temp =
                                    stockDayModels[index].subList(0, stockDayModels[index].size)
                                addAll(temp)
                            }
                        }

                    var qualityStatus = when (selectType) {
                        QualityType.ThreeSoldier -> {
                            JudgeTrendUtils.judgeThreeSoldierStock(
                                stockItemAllArray[index][0].stockCode!!,
                                stockDayModels
                            )
                        }
                        QualityType.FriendCounterattack -> {
                            JudgeTrendUtils.judgeFriendCounterattackStock(
                                stockItemAllArray[index][0].stockCode!!,
                                stockDayModels
                            )
                        }
                        QualityType.DawnFlush -> {
                            JudgeTrendUtils.judgeDawnFlushStock(
                                stockItemAllArray[index][0].stockCode!!,
                                stockDayModels
                            )
                        }
                        QualityType.ThreeCannonPlus -> {
                            JudgeTrendUtils.judgeCannonStock(
                                selectType,
                                stockDayModels
                            )
                        }
                        QualityType.CrossStar -> {
                            JudgeTrendUtils.judgeCrossStarStock(
                                stockDayModels,
                                stockItemAllArray[index][0].stockCode!!
                            )
                        }
                        QualityType.UpHammer -> {
                            JudgeTrendUtils.judgeUpHammerStock(
                                stockItemAllArray[index][0].stockCode!!,
                                stockDayModels
                            )
                        }
                        QualityType.Engulf -> {
                            JudgeTrendUtils.judgeEngulfStock(
                                stockItemAllArray[index][0].stockCode!!,
                                stockDayModels
                            )
                        }
                        QualityType.FlatBottom -> {
                            JudgeTrendUtils.judgeFlatBottomsStock(
                                stockItemAllArray[index][0].stockCode!!,
                                stockDayModels
                            )
                        }
                        QualityType.Pregnant -> {
                            JudgeTrendUtils.judgePregnantStock(
                                stockItemAllArray[index][0].stockCode!!,
                                stockDayModels
                            )
                        }
                        QualityType.PregnantPlus -> {
                            JudgeTrendUtils.judgePregnantPlusStock(
                                stockItemAllArray[index][0].stockCode!!,
                                stockDayModels
                            )
                        }
                        QualityType.FallEnd -> {
                            JudgeTrendUtils.judgeFallEndStock(
                                stockItemAllArray[index][0].stockCode!!,
                                stockDayModels
                            )
                        }
                        QualityType.DoubleNeedle -> {
                            JudgeTrendUtils.judgeDoubleNeedleStock(
                                stockItemAllArray[index][0].stockCode!!,
                                stockDayModels
                            )
                        }
                        QualityType.HighTrade -> {
                            JudgeTrendUtils.judgeHighTradingStock(
                                stockItemAllArray[index][0].stockCode!!,
                                stockDayModels
                            )
                        }
                        QualityType.Resonate -> {
                            JudgeTrendUtils.judgeResonateStock(
                                stockItemAllArray[index][0].stockCode!!,
                                ArrayList<StockItemCalculateModel>().apply {
                                    addAll(stockItemAllArray[index])
                                },
                                beforeOneDay.get()!!
                            )
                        }
                        QualityType.Debug -> {
                            JudgeTrendUtils.judgeDebugById(
                                stockItemAllArray[index][0].stockCode!!,
                                debugID,
                                stockDayModels
                            )
                        }
                        else -> {
                            JudgeTrendUtils.judgeCannonStock(
                                selectType,
                                stockDayModels,
                                stockItemAllArray[index][0].stockCode!!
                            )
                        }
                    }

                    if (qualityStatus && highQuality.get()!!) {
                        qualityStatus = JudgeTrendUtils.judgeDebugById(
                            stockItemFist[0].stockCode!!,
                            2,
                            ArrayList()
                        )
                    }

                    if (qualityStatus &&
                        stockItemFist[0].stockRate!! > BaseApplication.instance?.filterOptions?.startRate!! &&
                        stockItemFist[0].stockRate!! < BaseApplication.instance?.filterOptions?.endRate!!)
                        highQualityStocks.add(LitePalDBase.queryTodayStockItem(stockItemFist[0].stockCode!!))

//                        _LoadingProgress.postValue(Event(localPage))
                }
        }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                loadingStatus()
                toast("检查股票完成")
                _QualityStockItem.value = Event(highQualityStocks)
                emptyStatus.set(highQualityStocks.size == 0)
                println("------------------>Local Judge Stock Complete")
            }, {
                println("------------------>Local Throwable : ${it.message}")
            })
    }
}
}
//
//        if (localStockDayModels.size > 0) {
//            _LoadingMaxProgress.value = Event(localStockDayModels.size)
//            Flowable.just(localPage)
//                .subscribeOn(Schedulers.newThread())
//                .doOnNext {
//                    if (localPage <= localStockDayModels.size) {
//                        val temp =
//                            ArrayList<LocalDayStockItemModel>().apply { addAll(localStockDayModels) }
//                        for (index in 1..localPage) {
//                            temp.removeAt(0)
//                        }
//                        for (page in temp.indices) {
//                            checkStockType(temp[page].stockItems, temp[page].dayStockItems)
//                            localPage += 1
//
//                            handler.sendEmptyMessage(localPage)
//                        }
//                    }
//                }
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe({
//                    loadingStatus()
//                    toast("检查股票完成")
//                    _QualityStockItem.value = Event(highQualityStocks)
//                    emptyStatus.set(highQualityStocks.size == 0)
//                    println("------------------>Local Judge Stock Complete")
//                }, {
//                    println("------------------>Local Throwable : ${it.message}")
//                })
//
//            return
//        }
//
//        localStockDayModels.clear()
//
//        qualityStocks.addAll(LitePalDBase.queryFilterStockItem())
//        qualityStockCount = qualityStocks.size
//        if (qualityStockCount == 0) {
//            loadingStatus()
//            emptyStatus.set(true)
//            toast("没有优质股票")
//            return
//        }
//
//        _LoadingMaxProgress.value = Event(qualityStockCount)
//
//        lastStockCount = qualityStockCount % defaultJudgeCount
//        cycleJudgeStock(0)
//    }
//
//    @SuppressLint("CheckResult")
//    fun loadQualityStock(isRefresh: Boolean = true, debugId: Int = 0) {
//
//        debugID = debugId
//        initData.set(true)
//        emptyStatus.set(false)
//
//        qualityStocks.clear()
//        highQualityStocks.clear()
//        qualityStockCount = 0
//        judgeStockCount = 0
//        lastStockCount = 0
//
//        if (!isRefresh && localStockDayModels.size > 0) {
//            _LoadingMaxProgress.value = Event(localStockDayModels.size)
//            Flowable.just(localPage)
//                .subscribeOn(Schedulers.newThread())
//                .doOnNext {
//                    if (localPage <= localStockDayModels.size) {
//                        val temp =
//                            ArrayList<LocalDayStockItemModel>().apply { addAll(localStockDayModels) }
//                        for (index in 1..localPage) {
//                            temp.removeAt(0)
//                        }
//                        for (page in temp.indices) {
//                            checkStockType(temp[page].stockItems, temp[page].dayStockItems)
//                            localPage += 1
//
//                            handler.sendEmptyMessage(localPage)
//                        }
//                    }
//                }
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe({
//                    loadingStatus()
//                    toast("检查股票完成")
//                    _QualityStockItem.value = Event(highQualityStocks)
//                    emptyStatus.set(highQualityStocks.size == 0)
//                    localPage = 0
//                    println("------------------>Local Judge Stock Complete")
//                }, {
//                    loadQualityStock(false)
//                    localPage += 1
//                    handler.sendEmptyMessage(localPage)
//                    println("------------------>Local Throwable : ${it.message}")
//                })
//
//            return
//        }
//
//        localStockDayModels.clear()
//
//        qualityStocks.addAll(LitePalDBase.queryFilterStockItem())
//        qualityStockCount = qualityStocks.size
//        if (qualityStockCount == 0) {
//            loadingStatus()
//            emptyStatus.set(true)
//            toast("没有优质股票")
//            return
//        }
//
//        _LoadingMaxProgress.value = Event(qualityStockCount)
//
//        lastStockCount = qualityStockCount % defaultJudgeCount
//        cycleJudgeStock(0)
//    }

//    private fun cycleJudgeStock(startIndex: Int) {
//
//        if (judgeStockCount >= qualityStockCount) {
//            loadingStatus()
//            toast("检查股票完成")
//            _QualityStockItem.value = Event(highQualityStocks)
//            emptyStatus.set(highQualityStocks.size == 0)
//            println("------------------>Judge Stock Complete")
//            return
//        }
//
//        _LoadingProgress.value = Event(judgeStockCount)
//
//        if (qualityStockCount - startIndex > defaultJudgeCount)
//            selectHighQualityStock(ArrayList<StockItemModel>().apply {
//                addAll(qualityStocks.subList(startIndex, startIndex + defaultJudgeCount))
//            })
//        else
//            selectHighQualityStock(ArrayList<StockItemModel>().apply {
//                addAll(qualityStocks.subList(startIndex, startIndex + lastStockCount))
//            })
//
//        judgeStockCount = startIndex + defaultJudgeCount
//    }

//    @SuppressLint("CheckResult")
//    private fun selectHighQualityStock(stocks: ArrayList<StockItemModel>) {
//
//        tempStockItems.clear()
//        tempStockItems.addAll(stocks)
//
//        var stocksListStr = ""
//
//        for (stock in stocks) {
//            if (stock.stockCode != "300284" || stock.stockCode != "000043")
//                stocksListStr += "cn_" + stock.stockCode + ","
//            else
//                tempStockItems.remove(stock)
//        }
//
//        stocks.clear()
//        stocks.addAll(tempStockItems)
//
//        Rx2AndroidNetworking.get("https://q.stock.sohu.com/hisHq?code={code}&start={beforeDay}&end={today}&stat=1&order=D&period=d&rt=json")
//            .addPathParameter("code", stocksListStr)
//            .addPathParameter("today", DateUtils.today())
//            .addPathParameter("beforeDay", DateUtils.beforeFiveDay(selectType.day))
//            .build()
//            .getObjectListObservable(DaysStockInfoModel::class.java)
//            .subscribeOn(Schedulers.io())
//            .doOnError { println("------------------>Error : $it") }
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe({
//
//                localStockDayModels.add(
//                    LocalDayStockItemModel(
//                        stocks,
//                        ArrayList<DaysStockInfoModel>().apply { addAll(it) })
//                )
//
//                checkStockType(stocks, ArrayList<DaysStockInfoModel>().apply { addAll(it) })
//                cycleJudgeStock(judgeStockCount)
//            }, {
//                println("------------------>Throwable : ${it.message}")
//                cycleJudgeStock(judgeStockCount)
//            })
//    }

//    private fun checkStockType(
//        stocks: ArrayList<StockItemModel>,
//        it: ArrayList<DaysStockInfoModel>
//    ) {
//        when (selectType) {
//            QualityType.ThreeSoldier -> {
//                for (i in it.indices) {
//                    val judgeQuality =
//                        JudgeTrendUtils.judgeThreeSoldierStock(
//                            stocks[i].stockCode!!,
//                            ArrayList<StockDayModel>().apply {
//
//                                if (DateUtils.isCurrentInTimeScope()) {
//                                    val stockItemModel =
//                                        LitePalDBase.queryStockByCode(stocks[i].stockCode!!)[0]
//                                    add(StockDayModel().apply {
//                                        dateTime = stockItemModel.dateTime
//                                        openPrice = stockItemModel.openPrice?.toFloat()
//                                        nowPrice = stockItemModel.nowPrice?.toFloat()
//                                        swing =
//                                            stockItemModel.nowPrice?.toFloat()!! - stockItemModel.yestClosePrice?.toFloat()!!
//                                        stockRate = stockItemModel.stockRate
//                                        minPrice = stockItemModel.todayMin?.toFloat()
//                                        maxPrice = stockItemModel.todayMax?.toFloat()
//                                        tradeNum = stockItemModel.tradeNum?.toLong()
//                                        tradeAmount = stockItemModel.tradeAmount?.toFloat()
//                                        turnoverRate = stockItemModel.turnoverRate
//                                        yinyang = openPrice!! < nowPrice!!
//                                    })
//                                }
//
//                                for (dayStock in it[i].hq) {
//                                    add(StockDayModel().apply {
//                                        dateTime = dayStock[0]
//                                        openPrice = dayStock[1].toFloat()
//                                        nowPrice = dayStock[2].toFloat()
//                                        swing = dayStock[3].toFloat()
//                                        stockRate = dayStock[4].replace("%", "").toFloat()
//                                        minPrice = dayStock[5].toFloat()
//                                        maxPrice = dayStock[6].toFloat()
//                                        tradeNum = dayStock[7].toLong() * 100
//                                        tradeAmount = dayStock[8].toFloat() * 10000
//                                        turnoverRate = if (dayStock[9] == "-")
//                                            0.00f
//                                        else
//                                            dayStock[9].replace("%", "").toFloat()
//                                        yinyang = openPrice!! < nowPrice!!
//                                    })
//                                }
//                                when (get(0).dateTime?.let { it1 ->
//                                    DateUtils.checkWorkday(it1)
//                                }) {
//                                    WeekDay.Saturday -> removeAt(0)
//                                    WeekDay.Sunday -> {
//                                        removeAt(1)
//                                        removeAt(0)
//                                    }
//                                    else -> {
//                                    }
//                                }
//                                if (beforeOneDay.get()!!)
//                                    for (index in 1..BaseApplication.instance?.filterOptions?.beforeDay!!) {
//                                        removeAt(0)
//                                    }
//                            })
//
//                    if (judgeQuality) {
//                        if (highQuality.get()!!) {
//                            if (JudgeTrendUtils.judgeDebugById(
//                                    stocks[i].stockCode!!,
//                                    2,
//                                    ArrayList()
//                                )
//                            ) {
//                                highQualityStocks.add(stocks[i])
//                            } else println("------------------>Low Qulity : ${it[i].code}")
//                        } else highQualityStocks.add(stocks[i])
//                        println("------------------>High Qulity : ${it[i].code}")
//                    } else {
//                        println("------------------>Low Qulity : ${it[i].code}")
//                    }
//                }
//            }
//            QualityType.FriendCounterattack -> {
//                for (i in it.indices) {
//                    val judgeQuality =
//                        JudgeTrendUtils.judgeFriendCounterattackStock(ArrayList<StockDayModel>().apply {
//
//                            if (DateUtils.isCurrentInTimeScope()) {
//                                val stockItemModel =
//                                    LitePalDBase.queryStockByCode(stocks[i].stockCode!!)[0]
//                                add(StockDayModel().apply {
//                                    dateTime = DateUtils.today("yyyy-MM-dd")
//                                    openPrice = stockItemModel.openPrice?.toFloat()
//                                    nowPrice = stockItemModel.nowPrice?.toFloat()
//                                    swing =
//                                        stockItemModel.nowPrice?.toFloat()!! - stockItemModel.yestClosePrice?.toFloat()!!
//                                    stockRate = stockItemModel.stockRate
//                                    minPrice = stockItemModel.todayMin?.toFloat()
//                                    maxPrice = stockItemModel.todayMax?.toFloat()
//                                    tradeNum = stockItemModel.tradeNum?.toLong()
//                                    tradeAmount = stockItemModel.tradeAmount?.toFloat()
//                                    turnoverRate = stockItemModel.turnoverRate
//                                    yinyang = openPrice!! < nowPrice!!
//                                })
//                            }
//
//                            for (dayStock in it[i].hq) {
//                                add(StockDayModel().apply {
//                                    dateTime = dayStock[0]
//                                    openPrice = dayStock[1].toFloat()
//                                    nowPrice = dayStock[2].toFloat()
//                                    swing = dayStock[3].toFloat()
//                                    stockRate = dayStock[4].replace("%", "").toFloat()
//                                    minPrice = dayStock[5].toFloat()
//                                    maxPrice = dayStock[6].toFloat()
//                                    tradeNum = dayStock[7].toLong() * 100
//                                    tradeAmount = dayStock[8].toFloat() * 10000
//                                    turnoverRate = if (dayStock[9] == "-")
//                                        0.00f
//                                    else
//                                        dayStock[9].replace("%", "").toFloat()
//                                    yinyang = openPrice!! < nowPrice!!
//                                })
//                            }
//                            when (get(0).dateTime?.let { it1 ->
//                                DateUtils.checkWorkday(
//                                    it1
//                                )
//                            }) {
//                                WeekDay.Saturday -> removeAt(0)
//                                WeekDay.Sunday -> {
//                                    removeAt(1)
//                                    removeAt(0)
//                                }
//                                else -> {
//                                }
//                            }
//                            if (beforeOneDay.get()!!)
//                                for (index in 1..BaseApplication.instance?.filterOptions?.beforeDay!!) {
//                                    removeAt(0)
//                                }
//                        })
//
//                    if (judgeQuality) {
//                        if (highQuality.get()!!) {
//                            if (JudgeTrendUtils.judgeDebugById(
//                                    stocks[i].stockCode!!,
//                                    2,
//                                    ArrayList()
//                                )
//                            )
//                                highQualityStocks.add(stocks[i])
//                        } else highQualityStocks.add(stocks[i])
//                        println("------------------>High Qulity : ${it[i].code}")
//                    } else {
//                        println("------------------>Low Qulity : ${it[i].code}")
//                    }
//                }
//            }
//            QualityType.DawnFlush -> {
//                for (i in it.indices) {
//                    val judgeQuality =
//                        JudgeTrendUtils.judgeDawnFlushStock(ArrayList<StockDayModel>().apply {
//
//                            if (DateUtils.isCurrentInTimeScope()) {
//                                val stockItemModel =
//                                    LitePalDBase.queryStockByCode(stocks[i].stockCode!!)[0]
//                                add(StockDayModel().apply {
//                                    dateTime = DateUtils.today("yyyy-MM-dd")
//                                    openPrice = stockItemModel.openPrice?.toFloat()
//                                    nowPrice = stockItemModel.nowPrice?.toFloat()
//                                    swing =
//                                        stockItemModel.nowPrice?.toFloat()!! - stockItemModel.yestClosePrice?.toFloat()!!
//                                    stockRate = stockItemModel.stockRate
//                                    minPrice = stockItemModel.todayMin?.toFloat()
//                                    maxPrice = stockItemModel.todayMax?.toFloat()
//                                    tradeNum = stockItemModel.tradeNum?.toLong()
//                                    tradeAmount = stockItemModel.tradeAmount?.toFloat()
//                                    turnoverRate = stockItemModel.turnoverRate
//                                    yinyang = openPrice!! < nowPrice!!
//                                })
//                            }
//
//                            for (dayStock in it[i].hq) {
//                                add(StockDayModel().apply {
//                                    dateTime = dayStock[0]
//                                    openPrice = dayStock[1].toFloat()
//                                    nowPrice = dayStock[2].toFloat()
//                                    swing = dayStock[3].toFloat()
//                                    stockRate = dayStock[4].replace("%", "").toFloat()
//                                    minPrice = dayStock[5].toFloat()
//                                    maxPrice = dayStock[6].toFloat()
//                                    tradeNum = dayStock[7].toLong() * 100
//                                    tradeAmount = dayStock[8].toFloat() * 10000
//                                    turnoverRate = if (dayStock[9] == "-")
//                                        0.00f
//                                    else
//                                        dayStock[9].replace("%", "").toFloat()
//                                    yinyang = openPrice!! < nowPrice!!
//                                })
//                            }
//                            when (get(0).dateTime?.let { it1 ->
//                                DateUtils.checkWorkday(
//                                    it1
//                                )
//                            }) {
//                                WeekDay.Saturday -> removeAt(0)
//                                WeekDay.Sunday -> {
//                                    removeAt(1)
//                                    removeAt(0)
//                                }
//                                else -> {
//                                }
//                            }
//                            if (beforeOneDay.get()!!)
//                                for (index in 1..BaseApplication.instance?.filterOptions?.beforeDay!!) {
//                                    removeAt(0)
//                                }
//                        })
//
//                    if (judgeQuality) {
//                        if (highQuality.get()!!) {
//                            if (JudgeTrendUtils.judgeDebugById(
//                                    stocks[i].stockCode!!,
//                                    2,
//                                    ArrayList()
//                                )
//                            )
//                                highQualityStocks.add(stocks[i])
//                        } else highQualityStocks.add(stocks[i])
//                        println("------------------>High Qulity : ${it[i].code}")
//                    } else {
//                        println("------------------>Low Qulity : ${it[i].code}")
//                    }
//                }
//            }
//            QualityType.ThreeCannonPlus -> {
//                for (i in it.indices) {
//                    val judgeQuality = JudgeTrendUtils.judgeCannonStock(
//                        selectType,
//                        ArrayList<StockDayModel>().apply {
//
//                            if (DateUtils.isCurrentInTimeScope()) {
//                                val stockItemModel =
//                                    LitePalDBase.queryStockByCode(stocks[i].stockCode!!)[0]
//                                add(StockDayModel().apply {
//                                    dateTime = DateUtils.today("yyyy-MM-dd")
//                                    openPrice = stockItemModel.openPrice?.toFloat()
//                                    nowPrice = stockItemModel.nowPrice?.toFloat()
//                                    swing =
//                                        stockItemModel.nowPrice?.toFloat()!! - stockItemModel.yestClosePrice?.toFloat()!!
//                                    stockRate = stockItemModel.stockRate
//                                    minPrice = stockItemModel.todayMin?.toFloat()
//                                    maxPrice = stockItemModel.todayMax?.toFloat()
//                                    tradeNum = stockItemModel.tradeNum?.toLong()
//                                    tradeAmount = stockItemModel.tradeAmount?.toFloat()
//                                    turnoverRate = stockItemModel.turnoverRate
//                                    yinyang = openPrice!! < nowPrice!!
//                                })
//                            }
//
//                            for (dayStock in it[i].hq) {
//                                add(StockDayModel().apply {
//                                    dateTime = dayStock[0]
//                                    openPrice = dayStock[1].toFloat()
//                                    nowPrice = dayStock[2].toFloat()
//                                    swing = dayStock[3].toFloat()
//                                    stockRate = dayStock[4].replace("%", "").toFloat()
//                                    minPrice = dayStock[5].toFloat()
//                                    maxPrice = dayStock[6].toFloat()
//                                    tradeNum = dayStock[7].toLong() * 100
//                                    tradeAmount = dayStock[8].toFloat() * 10000
//                                    turnoverRate = if (dayStock[9] == "-")
//                                        0.00f
//                                    else
//                                        dayStock[9].replace("%", "").toFloat()
//                                    yinyang = openPrice!! < nowPrice!!
//                                })
//                            }
//                            when (get(0).dateTime?.let { it1 ->
//                                DateUtils.checkWorkday(
//                                    it1
//                                )
//                            }) {
//                                WeekDay.Saturday -> removeAt(0)
//                                WeekDay.Sunday -> {
//                                    removeAt(1)
//                                    removeAt(0)
//                                }
//                                else -> {
//                                }
//                            }
//                            if (beforeOneDay.get()!!)
//                                for (index in 1..BaseApplication.instance?.filterOptions?.beforeDay!!) {
//                                    removeAt(0)
//                                }
//                        })
//
//                    if (judgeQuality) {
//                        if (highQuality.get()!!) {
//                            if (JudgeTrendUtils.judgeDebugById(
//                                    stocks[i].stockCode!!,
//                                    2,
//                                    ArrayList()
//                                )
//                            )
//                                highQualityStocks.add(stocks[i])
//                        } else highQualityStocks.add(stocks[i])
//                        println("------------------>High Qulity : ${it[i].code}")
//                    } else {
//                        println("------------------>Low Qulity : ${it[i].code}")
//                    }
//                }
//            }
//            QualityType.CrossStar -> {
//                for (i in it.indices) {
//                    val judgeQuality =
//                        JudgeTrendUtils.judgeCrossStarStock(ArrayList<StockDayModel>().apply {
//
//                            if (DateUtils.isCurrentInTimeScope()) {
//                                val stockItemModel =
//                                    LitePalDBase.queryStockByCode(stocks[i].stockCode!!)[0]
//                                add(StockDayModel().apply {
//                                    dateTime = DateUtils.today("yyyy-MM-dd")
//                                    openPrice = stockItemModel.openPrice?.toFloat()
//                                    nowPrice = stockItemModel.nowPrice?.toFloat()
//                                    swing =
//                                        stockItemModel.nowPrice?.toFloat()!! - stockItemModel.yestClosePrice?.toFloat()!!
//                                    stockRate = stockItemModel.stockRate
//                                    minPrice = stockItemModel.todayMin?.toFloat()
//                                    maxPrice = stockItemModel.todayMax?.toFloat()
//                                    tradeNum = stockItemModel.tradeNum?.toLong()
//                                    tradeAmount = stockItemModel.tradeAmount?.toFloat()
//                                    turnoverRate = stockItemModel.turnoverRate
//                                    yinyang = openPrice!! < nowPrice!!
//                                })
//                            }
//
//                            for (dayStock in it[i].hq) {
//                                add(StockDayModel().apply {
//                                    dateTime = dayStock[0]
//                                    openPrice = dayStock[1].toFloat()
//                                    nowPrice = dayStock[2].toFloat()
//                                    swing = dayStock[3].toFloat()
//                                    stockRate = dayStock[4].replace("%", "").toFloat()
//                                    minPrice = dayStock[5].toFloat()
//                                    maxPrice = dayStock[6].toFloat()
//                                    tradeNum = dayStock[7].toLong() * 100
//                                    tradeAmount = dayStock[8].toFloat() * 10000
//                                    turnoverRate = if (dayStock[9] == "-")
//                                        0.00f
//                                    else
//                                        dayStock[9].replace("%", "").toFloat()
//                                    yinyang = openPrice!! < nowPrice!!
//                                })
//                            }
//                            when (get(0).dateTime?.let { it1 ->
//                                DateUtils.checkWorkday(
//                                    it1
//                                )
//                            }) {
//                                WeekDay.Saturday -> removeAt(0)
//                                WeekDay.Sunday -> {
//                                    removeAt(1)
//                                    removeAt(0)
//                                }
//                                else -> {
//                                }
//                            }
//                            if (beforeOneDay.get()!!)
//                                for (index in 1..BaseApplication.instance?.filterOptions?.beforeDay!!) {
//                                    removeAt(0)
//                                }
//                        })
//
//                    if (judgeQuality) {
//                        if (highQuality.get()!!) {
//                            if (JudgeTrendUtils.judgeDebugById(
//                                    stocks[i].stockCode!!,
//                                    2,
//                                    ArrayList()
//                                )
//                            )
//                                highQualityStocks.add(stocks[i])
//                        } else highQualityStocks.add(stocks[i])
//                        println("------------------>High Qulity : ${it[i].code}")
//                    } else {
//                        println("------------------>Low Qulity : ${it[i].code}")
//                    }
//                }
//            }
//            QualityType.UpHammer -> {
//                for (i in it.indices) {
//                    val judgeQuality =
//                        JudgeTrendUtils.judgeUpHammerStock(ArrayList<StockDayModel>().apply {
//
//                            if (DateUtils.isCurrentInTimeScope()) {
//                                val stockItemModel =
//                                    LitePalDBase.queryStockByCode(stocks[i].stockCode!!)[0]
//                                add(StockDayModel().apply {
//                                    dateTime = DateUtils.today("yyyy-MM-dd")
//                                    openPrice = stockItemModel.openPrice?.toFloat()
//                                    nowPrice = stockItemModel.nowPrice?.toFloat()
//                                    swing =
//                                        stockItemModel.nowPrice?.toFloat()!! - stockItemModel.yestClosePrice?.toFloat()!!
//                                    stockRate = stockItemModel.stockRate
//                                    minPrice = stockItemModel.todayMin?.toFloat()
//                                    maxPrice = stockItemModel.todayMax?.toFloat()
//                                    tradeNum = stockItemModel.tradeNum?.toLong()
//                                    tradeAmount = stockItemModel.tradeAmount?.toFloat()
//                                    turnoverRate = stockItemModel.turnoverRate
//                                    yinyang = nowPrice!! > openPrice!!
//                                })
//                            }
//
//                            for (dayStock in it[i].hq) {
//                                add(StockDayModel().apply {
//                                    dateTime = dayStock[0]
//                                    openPrice = dayStock[1].toFloat()
//                                    nowPrice = dayStock[2].toFloat()
//                                    swing = dayStock[3].toFloat()
//                                    stockRate = dayStock[4].replace("%", "").toFloat()
//                                    minPrice = dayStock[5].toFloat()
//                                    maxPrice = dayStock[6].toFloat()
//                                    tradeNum = dayStock[7].toLong() * 100
//                                    tradeAmount = dayStock[8].toFloat() * 10000
//                                    turnoverRate = if (dayStock[9] == "-")
//                                        0.00f
//                                    else
//                                        dayStock[9].replace("%", "").toFloat()
//                                    yinyang = openPrice!! < nowPrice!!
//                                })
//                            }
//                            when (get(0).dateTime?.let { it1 ->
//                                DateUtils.checkWorkday(
//                                    it1
//                                )
//                            }) {
//                                WeekDay.Saturday -> removeAt(0)
//                                WeekDay.Sunday -> {
//                                    removeAt(1)
//                                    removeAt(0)
//                                }
//                                else -> {
//                                }
//                            }
//                            if (beforeOneDay.get()!!)
//                                for (index in 1..BaseApplication.instance?.filterOptions?.beforeDay!!) {
//                                    removeAt(0)
//                                }
//                        })
//
//                    if (judgeQuality) {
//                        if (highQuality.get()!!) {
//                            if (JudgeTrendUtils.judgeDebugById(
//                                    stocks[i].stockCode!!,
//                                    2,
//                                    ArrayList()
//                                )
//                            )
//                                highQualityStocks.add(stocks[i])
//                        } else highQualityStocks.add(stocks[i])
//                        println("------------------>High Qulity : ${it[i].code}")
//                    } else {
//                        println("------------------>Low Qulity : ${it[i].code}")
//                    }
//                }
//            }
//            QualityType.Engulf -> {
//                for (i in it.indices) {
//                    val judgeQuality = JudgeTrendUtils.judgeEngulfStock(
//                        stocks[i].stockCode!!,
//                        ArrayList<StockDayModel>().apply {
//
//                            if (DateUtils.isCurrentInTimeScope()) {
//                                val stockItemModel =
//                                    LitePalDBase.queryStockByCode(stocks[i].stockCode!!)[0]
//                                add(StockDayModel().apply {
//                                    dateTime = DateUtils.today("yyyy-MM-dd")
//                                    openPrice = stockItemModel.openPrice?.toFloat()
//                                    nowPrice = stockItemModel.nowPrice?.toFloat()
//                                    swing =
//                                        stockItemModel.nowPrice?.toFloat()!! - stockItemModel.yestClosePrice?.toFloat()!!
//                                    stockRate = stockItemModel.stockRate
//                                    minPrice = stockItemModel.todayMin?.toFloat()
//                                    maxPrice = stockItemModel.todayMax?.toFloat()
//                                    tradeNum = stockItemModel.tradeNum?.toLong()
//                                    tradeAmount = stockItemModel.tradeAmount?.toFloat()
//                                    turnoverRate = stockItemModel.turnoverRate
//                                    yinyang = nowPrice!! > openPrice!!
//                                })
//                            }
//
//                            for (dayStock in it[i].hq) {
//                                add(StockDayModel().apply {
//                                    dateTime = dayStock[0]
//                                    openPrice = dayStock[1].toFloat()
//                                    nowPrice = dayStock[2].toFloat()
//                                    swing = dayStock[3].toFloat()
//                                    stockRate = dayStock[4].replace("%", "").toFloat()
//                                    minPrice = dayStock[5].toFloat()
//                                    maxPrice = dayStock[6].toFloat()
//                                    tradeNum = dayStock[7].toLong() * 100
//                                    tradeAmount = dayStock[8].toFloat() * 10000
//                                    turnoverRate = if (dayStock[9] == "-")
//                                        0.00f
//                                    else
//                                        dayStock[9].replace("%", "").toFloat()
//                                    yinyang = openPrice!! < nowPrice!!
//                                })
//                            }
//                            when (get(0).dateTime?.let { it1 ->
//                                DateUtils.checkWorkday(
//                                    it1
//                                )
//                            }) {
//                                WeekDay.Saturday -> removeAt(0)
//                                WeekDay.Sunday -> {
//                                    removeAt(1)
//                                    removeAt(0)
//                                }
//                                else -> {
//                                }
//                            }
//                            if (beforeOneDay.get()!!)
//                                for (index in 1..BaseApplication.instance?.filterOptions?.beforeDay!!) {
//                                    removeAt(0)
//                                }
//                        })
//
//                    if (judgeQuality) {
//                        if (highQuality.get()!!) {
//                            if (JudgeTrendUtils.judgeDebugById(
//                                    stocks[i].stockCode!!,
//                                    2,
//                                    ArrayList()
//                                )
//                            )
//                                highQualityStocks.add(stocks[i])
//                        } else highQualityStocks.add(stocks[i])
//                        println("------------------>High Qulity : ${it[i].code}")
//                    } else {
//                        println("------------------>Low Qulity : ${it[i].code}")
//                    }
//                }
//            }
//            QualityType.FlatBottom -> {
//                for (i in it.indices) {
//                    val judgeQuality = JudgeTrendUtils.judgeFlatBottomsStock(
//                        stocks[i].stockCode!!,
//                        ArrayList<StockDayModel>().apply {
//
//                            if (DateUtils.isCurrentInTimeScope()) {
//                                val stockItemModel =
//                                    LitePalDBase.queryStockByCode(stocks[i].stockCode!!)[0]
//                                add(StockDayModel().apply {
//                                    dateTime = DateUtils.today("yyyy-MM-dd")
//                                    openPrice = stockItemModel.openPrice?.toFloat()
//                                    nowPrice = stockItemModel.nowPrice?.toFloat()
//                                    swing =
//                                        stockItemModel.nowPrice?.toFloat()!! - stockItemModel.yestClosePrice?.toFloat()!!
//                                    stockRate = stockItemModel.stockRate
//                                    minPrice = stockItemModel.todayMin?.toFloat()
//                                    maxPrice = stockItemModel.todayMax?.toFloat()
//                                    tradeNum = stockItemModel.tradeNum?.toLong()
//                                    tradeAmount = stockItemModel.tradeAmount?.toFloat()
//                                    turnoverRate = stockItemModel.turnoverRate
//                                    yinyang = nowPrice!! > openPrice!!
//                                })
//                            }
//
//                            for (dayStock in it[i].hq) {
//                                add(StockDayModel().apply {
//                                    dateTime = dayStock[0]
//                                    openPrice = dayStock[1].toFloat()
//                                    nowPrice = dayStock[2].toFloat()
//                                    swing = dayStock[3].toFloat()
//                                    stockRate = dayStock[4].replace("%", "").toFloat()
//                                    minPrice = dayStock[5].toFloat()
//                                    maxPrice = dayStock[6].toFloat()
//                                    tradeNum = dayStock[7].toLong() * 100
//                                    tradeAmount = dayStock[8].toFloat() * 10000
//                                    turnoverRate = if (dayStock[9] == "-")
//                                        0.00f
//                                    else
//                                        dayStock[9].replace("%", "").toFloat()
//                                    yinyang = openPrice!! < nowPrice!!
//                                })
//                            }
//
//                            when (get(0).dateTime?.let { it1 -> DateUtils.checkWorkday(it1) }) {
//                                WeekDay.Saturday -> removeAt(0)
//                                WeekDay.Sunday -> {
//                                    removeAt(1)
//                                    removeAt(0)
//                                }
//                                else -> {
//                                }
//                            }
//                            if (beforeOneDay.get()!!)
//                                for (index in 1..BaseApplication.instance?.filterOptions?.beforeDay!!) {
//                                    removeAt(0)
//                                }
//
//                        })
//                    if (judgeQuality) {
//                        if (highQuality.get()!!) {
//                            if (JudgeTrendUtils.judgeDebugById(
//                                    stocks[i].stockCode!!,
//                                    2,
//                                    ArrayList()
//                                )
//                            )
//                                highQualityStocks.add(stocks[i])
//                        } else highQualityStocks.add(stocks[i])
//                        println("------------------>High Qulity : ${it[i].code}")
//                    } else {
//                        println("------------------>Low Qulity : ${it[i].code}")
//                    }
//                }
//            }
//            QualityType.Pregnant -> {
//                for (i in it.indices) {
//                    val judgeQuality = JudgeTrendUtils.judgePregnantStock(
//                        stocks[i].stockCode!!,
//                        ArrayList<StockDayModel>().apply {
//
//                            if (DateUtils.isCurrentInTimeScope()) {
//                                val stockItemModel =
//                                    LitePalDBase.queryStockByCode(stocks[i].stockCode!!)[0]
//                                add(StockDayModel().apply {
//                                    dateTime = DateUtils.today("yyyy-MM-dd")
//                                    openPrice = stockItemModel.openPrice?.toFloat()
//                                    nowPrice = stockItemModel.nowPrice?.toFloat()
//                                    swing =
//                                        stockItemModel.nowPrice?.toFloat()!! - stockItemModel.yestClosePrice?.toFloat()!!
//                                    stockRate = stockItemModel.stockRate
//                                    minPrice = stockItemModel.todayMin?.toFloat()
//                                    maxPrice = stockItemModel.todayMax?.toFloat()
//                                    tradeNum = stockItemModel.tradeNum?.toLong()
//                                    tradeAmount = stockItemModel.tradeAmount?.toFloat()
//                                    turnoverRate = stockItemModel.turnoverRate
//                                    yinyang = nowPrice!! > openPrice!!
//                                })
//                            }
//
//                            for (dayStock in it[i].hq) {
//                                add(StockDayModel().apply {
//                                    dateTime = dayStock[0]
//                                    openPrice = dayStock[1].toFloat()
//                                    nowPrice = dayStock[2].toFloat()
//                                    swing = dayStock[3].toFloat()
//                                    stockRate = dayStock[4].replace("%", "").toFloat()
//                                    minPrice = dayStock[5].toFloat()
//                                    maxPrice = dayStock[6].toFloat()
//                                    tradeNum = dayStock[7].toLong() * 100
//                                    tradeAmount = dayStock[8].toFloat() * 10000
//                                    turnoverRate = if (dayStock[9] == "-")
//                                        0.00f
//                                    else
//                                        dayStock[9].replace("%", "").toFloat()
//                                    yinyang = openPrice!! < nowPrice!!
//                                })
//                            }
//                            when (get(0).dateTime?.let { it1 ->
//                                DateUtils.checkWorkday(
//                                    it1
//                                )
//                            }) {
//                                WeekDay.Saturday -> removeAt(0)
//                                WeekDay.Sunday -> {
//                                    removeAt(1)
//                                    removeAt(0)
//                                }
//                                else -> {
//                                }
//                            }
//                            if (beforeOneDay.get()!!)
//                                for (index in 1..BaseApplication.instance?.filterOptions?.beforeDay!!) {
//                                    removeAt(0)
//                                }
//                        })
//                    if (judgeQuality) {
//                        if (highQuality.get()!!) {
//                            if (JudgeTrendUtils.judgeDebugById(
//                                    stocks[i].stockCode!!,
//                                    2,
//                                    ArrayList()
//                                )
//                            )
//                                highQualityStocks.add(stocks[i])
//                        } else highQualityStocks.add(stocks[i])
//                        println("------------------>High Qulity : ${it[i].code}")
//                    } else {
//                        println("------------------>Low Qulity : ${it[i].code}")
//                    }
//                }
//            }
//            QualityType.PregnantPlus -> {
//                for (i in it.indices) {
//                    val judgeQuality = JudgeTrendUtils.judgePregnantPlusStock(
//                        stocks[i].stockCode!!,
//                        ArrayList<StockDayModel>().apply {
//
//                            if (DateUtils.isCurrentInTimeScope()) {
//                                val stockItemModel =
//                                    LitePalDBase.queryStockByCode(stocks[i].stockCode!!)[0]
//                                add(StockDayModel().apply {
//                                    dateTime = DateUtils.today("yyyy-MM-dd")
//                                    openPrice = stockItemModel.openPrice?.toFloat()
//                                    nowPrice = stockItemModel.nowPrice?.toFloat()
//                                    swing =
//                                        stockItemModel.nowPrice?.toFloat()!! - stockItemModel.yestClosePrice?.toFloat()!!
//                                    stockRate = stockItemModel.stockRate
//                                    minPrice = stockItemModel.todayMin?.toFloat()
//                                    maxPrice = stockItemModel.todayMax?.toFloat()
//                                    tradeNum = stockItemModel.tradeNum?.toLong()
//                                    tradeAmount = stockItemModel.tradeAmount?.toFloat()
//                                    turnoverRate = stockItemModel.turnoverRate
//                                    yinyang = nowPrice!! > openPrice!!
//                                })
//                            }
//
//                            for (dayStock in it[i].hq) {
//                                add(StockDayModel().apply {
//                                    dateTime = dayStock[0]
//                                    openPrice = dayStock[1].toFloat()
//                                    nowPrice = dayStock[2].toFloat()
//                                    swing = dayStock[3].toFloat()
//                                    stockRate = dayStock[4].replace("%", "").toFloat()
//                                    minPrice = dayStock[5].toFloat()
//                                    maxPrice = dayStock[6].toFloat()
//                                    tradeNum = dayStock[7].toLong() * 100
//                                    tradeAmount = dayStock[8].toFloat() * 10000
//                                    turnoverRate = if (dayStock[9] == "-")
//                                        0.00f
//                                    else
//                                        dayStock[9].replace("%", "").toFloat()
//                                    yinyang = openPrice!! < nowPrice!!
//                                })
//                            }
//                            when (get(0).dateTime?.let { it1 ->
//                                DateUtils.checkWorkday(
//                                    it1
//                                )
//                            }) {
//                                WeekDay.Saturday -> removeAt(0)
//                                WeekDay.Sunday -> {
//                                    removeAt(1)
//                                    removeAt(0)
//                                }
//                                else -> {
//                                }
//                            }
//
//                            if (beforeOneDay.get()!!)
//                                for (index in 1..BaseApplication.instance?.filterOptions?.beforeDay!!) {
//                                    removeAt(0)
//                                }
//                        })
//                    if (judgeQuality) {
//                        if (highQuality.get()!!) {
//                            if (JudgeTrendUtils.judgeDebugById(
//                                    stocks[i].stockCode!!,
//                                    2,
//                                    ArrayList()
//                                )
//                            )
//                                highQualityStocks.add(stocks[i])
//                        } else highQualityStocks.add(stocks[i])
//                        println("------------------>High Qulity : ${it[i].code}")
//                    } else {
//                        println("------------------>Low Qulity : ${it[i].code}")
//                    }
//                }
//            }
//            QualityType.FallEnd -> {
//                for (i in it.indices) {
//                    val judgeQuality = JudgeTrendUtils.judgeFallEndStock(
//                        stocks[i].stockCode!!,
//                        ArrayList<StockDayModel>().apply {
//
//                            if (DateUtils.isCurrentInTimeScope()) {
//                                val stockItemModel =
//                                    LitePalDBase.queryStockByCode(stocks[i].stockCode!!)[0]
//                                add(StockDayModel().apply {
//                                    dateTime = DateUtils.today("yyyy-MM-dd")
//                                    openPrice = stockItemModel.openPrice?.toFloat()
//                                    nowPrice = stockItemModel.nowPrice?.toFloat()
//                                    swing =
//                                        stockItemModel.nowPrice?.toFloat()!! - stockItemModel.yestClosePrice?.toFloat()!!
//                                    stockRate = stockItemModel.stockRate
//                                    minPrice = stockItemModel.todayMin?.toFloat()
//                                    maxPrice = stockItemModel.todayMax?.toFloat()
//                                    tradeNum = stockItemModel.tradeNum?.toLong()
//                                    tradeAmount = stockItemModel.tradeAmount?.toFloat()
//                                    turnoverRate = stockItemModel.turnoverRate
//                                    yinyang = nowPrice!! > openPrice!!
//                                })
//                            }
//
//                            for (dayStock in it[i].hq) {
//                                add(StockDayModel().apply {
//                                    dateTime = dayStock[0]
//                                    openPrice = dayStock[1].toFloat()
//                                    nowPrice = dayStock[2].toFloat()
//                                    swing = dayStock[3].toFloat()
//                                    stockRate = dayStock[4].replace("%", "").toFloat()
//                                    minPrice = dayStock[5].toFloat()
//                                    maxPrice = dayStock[6].toFloat()
//                                    tradeNum = dayStock[7].toLong() * 100
//                                    tradeAmount = dayStock[8].toFloat() * 10000
//                                    turnoverRate = if (dayStock[9] == "-")
//                                        0.00f
//                                    else
//                                        dayStock[9].replace("%", "").toFloat()
//                                    yinyang = openPrice!! < nowPrice!!
//                                })
//                            }
//                            when (get(0).dateTime?.let { it1 ->
//                                DateUtils.checkWorkday(
//                                    it1
//                                )
//                            }) {
//                                WeekDay.Saturday -> removeAt(0)
//                                WeekDay.Sunday -> {
//                                    removeAt(1)
//                                    removeAt(0)
//                                }
//                                else -> {
//                                }
//                            }
//                            if (beforeOneDay.get()!!)
//                                for (index in 1..BaseApplication.instance?.filterOptions?.beforeDay!!) {
//                                    removeAt(0)
//                                }
//                        })
//                    if (judgeQuality) {
//                        if (highQuality.get()!!) {
//                            if (JudgeTrendUtils.judgeDebugById(
//                                    stocks[i].stockCode!!,
//                                    2,
//                                    ArrayList()
//                                )
//                            )
//                                highQualityStocks.add(stocks[i])
//                        } else highQualityStocks.add(stocks[i])
//                        println("------------------>High Qulity : ${it[i].code}")
//                    } else {
//                        println("------------------>Low Qulity : ${it[i].code}")
//                    }
//                }
//            }
//            QualityType.DoubleNeedle -> {
//                for (i in it.indices) {
//                    val judgeQuality = JudgeTrendUtils.judgeDoubleNeedleStock(
//                        stocks[i].stockCode!!,
//                        ArrayList<StockDayModel>().apply {
//
//                            if (DateUtils.isCurrentInTimeScope()) {
//                                val stockItemModel =
//                                    LitePalDBase.queryStockByCode(stocks[i].stockCode!!)[0]
//                                add(StockDayModel().apply {
//                                    dateTime = DateUtils.today("yyyy-MM-dd")
//                                    openPrice = stockItemModel.openPrice?.toFloat()
//                                    nowPrice = stockItemModel.nowPrice?.toFloat()
//                                    swing =
//                                        stockItemModel.nowPrice?.toFloat()!! - stockItemModel.yestClosePrice?.toFloat()!!
//                                    stockRate = stockItemModel.stockRate
//                                    minPrice = stockItemModel.todayMin?.toFloat()
//                                    maxPrice = stockItemModel.todayMax?.toFloat()
//                                    tradeNum = stockItemModel.tradeNum?.toLong()
//                                    tradeAmount = stockItemModel.tradeAmount?.toFloat()
//                                    turnoverRate = stockItemModel.turnoverRate
//                                    yinyang = nowPrice!! > openPrice!!
//                                })
//                            }
//
//                            for (dayStock in it[i].hq) {
//                                add(StockDayModel().apply {
//                                    dateTime = dayStock[0]
//                                    openPrice = dayStock[1].toFloat()
//                                    nowPrice = dayStock[2].toFloat()
//                                    swing = dayStock[3].toFloat()
//                                    stockRate = dayStock[4].replace("%", "").toFloat()
//                                    minPrice = dayStock[5].toFloat()
//                                    maxPrice = dayStock[6].toFloat()
//                                    tradeNum = dayStock[7].toLong() * 100
//                                    tradeAmount = dayStock[8].toFloat() * 10000
//                                    turnoverRate = if (dayStock[9] == "-")
//                                        0.00f
//                                    else
//                                        dayStock[9].replace("%", "").toFloat()
//                                    yinyang = openPrice!! < nowPrice!!
//                                })
//                            }
//                            when (get(0).dateTime?.let { it1 ->
//                                DateUtils.checkWorkday(
//                                    it1
//                                )
//                            }) {
//                                WeekDay.Saturday -> removeAt(0)
//                                WeekDay.Sunday -> {
//                                    removeAt(1)
//                                    removeAt(0)
//                                }
//                                else -> {
//                                }
//                            }
//                            if (beforeOneDay.get()!!)
//                                for (index in 1..BaseApplication.instance?.filterOptions?.beforeDay!!) {
//                                    removeAt(0)
//                                }
//                        })
//
//                    if (judgeQuality) {
//                        if (highQuality.get()!!) {
//                            if (JudgeTrendUtils.judgeDebugById(
//                                    stocks[i].stockCode!!,
//                                    2,
//                                    ArrayList()
//                                )
//                            )
//                                highQualityStocks.add(stocks[i])
//                        } else highQualityStocks.add(stocks[i])
//                        println("------------------>High Qulity : ${it[i].code}")
//                    } else {
//                        println("------------------>Low Qulity : ${it[i].code}")
//                    }
//                }
//            }
//            QualityType.HighTrade -> {
//                for (i in it.indices) {
//                    val judgeQuality = JudgeTrendUtils.judgeHighTradingStock(
//                        stocks[i].stockCode!!,
//                        ArrayList<StockDayModel>().apply {
//
//                            if (DateUtils.isCurrentInTimeScope()) {
//                                val stockItemModel =
//                                    LitePalDBase.queryStockByCode(stocks[i].stockCode!!)[0]
//                                add(StockDayModel().apply {
//                                    dateTime = DateUtils.today("yyyy-MM-dd")
//                                    openPrice = stockItemModel.openPrice?.toFloat()
//                                    nowPrice = stockItemModel.nowPrice?.toFloat()
//                                    swing =
//                                        stockItemModel.nowPrice?.toFloat()!! - stockItemModel.yestClosePrice?.toFloat()!!
//                                    stockRate = stockItemModel.stockRate
//                                    minPrice = stockItemModel.todayMin?.toFloat()
//                                    maxPrice = stockItemModel.todayMax?.toFloat()
//                                    tradeNum = stockItemModel.tradeNum?.toLong()
//                                    tradeAmount = stockItemModel.tradeAmount?.toFloat()
//                                    turnoverRate = stockItemModel.turnoverRate
//                                    yinyang = nowPrice!! > openPrice!!
//                                })
//                            }
//
//                            for (dayStock in it[i].hq) {
//                                add(StockDayModel().apply {
//                                    dateTime = dayStock[0]
//                                    openPrice = dayStock[1].toFloat()
//                                    nowPrice = dayStock[2].toFloat()
//                                    swing = dayStock[3].toFloat()
//                                    stockRate = dayStock[4].replace("%", "").toFloat()
//                                    minPrice = dayStock[5].toFloat()
//                                    maxPrice = dayStock[6].toFloat()
//                                    tradeNum = dayStock[7].toLong() * 100
//                                    tradeAmount = dayStock[8].toFloat() * 10000
//                                    turnoverRate = if (dayStock[9] == "-")
//                                        0.00f
//                                    else
//                                        dayStock[9].replace("%", "").toFloat()
//                                    yinyang = openPrice!! < nowPrice!!
//                                })
//                            }
//                            when (get(0).dateTime?.let { it1 ->
//                                DateUtils.checkWorkday(
//                                    it1
//                                )
//                            }) {
//                                WeekDay.Saturday -> removeAt(0)
//                                WeekDay.Sunday -> {
//                                    removeAt(1)
//                                    removeAt(0)
//                                }
//                                else -> {
//                                }
//                            }
//                            if (beforeOneDay.get()!!)
//                                for (index in 1..BaseApplication.instance?.filterOptions?.beforeDay!!) {
//                                    removeAt(0)
//                                }
//                        })
//
//                    if (judgeQuality) {
//                        if (highQuality.get()!!) {
//                            if (JudgeTrendUtils.judgeDebugById(
//                                    stocks[i].stockCode!!,
//                                    2,
//                                    ArrayList()
//                                )
//                            )
//                                highQualityStocks.add(stocks[i])
//                        } else highQualityStocks.add(stocks[i])
//                        println("------------------>High Qulity : ${it[i].code}")
//                    } else {
//                        println("------------------>Low Qulity : ${it[i].code}")
//                    }
//                }
//            }
//            QualityType.Resonate -> {
//                for (i in it.indices) {
//                    val judgeQuality = JudgeTrendUtils.judgeResonateStock(
//                        stocks[i].stockCode!!,
//                        ArrayList<StockDayModel>().apply {
//
//                            if (DateUtils.isCurrentInTimeScope()) {
//                                val stockItemModel =
//                                    LitePalDBase.queryStockByCode(stocks[i].stockCode!!)[0]
//                                add(StockDayModel().apply {
//                                    dateTime = DateUtils.today("yyyy-MM-dd")
//                                    openPrice = stockItemModel.openPrice?.toFloat()
//                                    nowPrice = stockItemModel.nowPrice?.toFloat()
//                                    swing =
//                                        stockItemModel.nowPrice?.toFloat()!! - stockItemModel.yestClosePrice?.toFloat()!!
//                                    stockRate = stockItemModel.stockRate
//                                    minPrice = stockItemModel.todayMin?.toFloat()
//                                    maxPrice = stockItemModel.todayMax?.toFloat()
//                                    tradeNum = stockItemModel.tradeNum?.toLong()
//                                    tradeAmount = stockItemModel.tradeAmount?.toFloat()
//                                    turnoverRate = stockItemModel.turnoverRate
//                                    yinyang = nowPrice!! > openPrice!!
//                                })
//                            }
//
//                            for (dayStock in it[i].hq) {
//                                add(StockDayModel().apply {
//                                    dateTime = dayStock[0]
//                                    openPrice = dayStock[1].toFloat()
//                                    nowPrice = dayStock[2].toFloat()
//                                    swing = dayStock[3].toFloat()
//                                    stockRate = dayStock[4].replace("%", "").toFloat()
//                                    minPrice = dayStock[5].toFloat()
//                                    maxPrice = dayStock[6].toFloat()
//                                    tradeNum = dayStock[7].toLong() * 100
//                                    tradeAmount = dayStock[8].toFloat() * 10000
//                                    turnoverRate = if (dayStock[9] == "-")
//                                        0.00f
//                                    else
//                                        dayStock[9].replace("%", "").toFloat()
//                                    yinyang = openPrice!! < nowPrice!!
//                                })
//                            }
//                            when (get(0).dateTime?.let { it1 ->
//                                DateUtils.checkWorkday(
//                                    it1
//                                )
//                            }) {
//                                WeekDay.Saturday -> removeAt(0)
//                                WeekDay.Sunday -> {
//                                    removeAt(1)
//                                    removeAt(0)
//                                }
//                                else -> {
//                                }
//                            }
//                            if (beforeOneDay.get()!!)
//                                for (index in 1..BaseApplication.instance?.filterOptions?.beforeDay!!) {
//                                    removeAt(0)
//                                }
//                        })
//
//                    if (judgeQuality) {
//                        if (highQuality.get()!!) {
//                            if (JudgeTrendUtils.judgeDebugById(
//                                    stocks[i].stockCode!!,
//                                    2,
//                                    ArrayList()
//                                )
//                            )
//                                highQualityStocks.add(stocks[i])
//                        } else highQualityStocks.add(stocks[i])
//                        println("------------------>High Qulity : ${it[i].code}")
//                    } else {
//                        println("------------------>Low Qulity : ${it[i].code}")
//                    }
//                }
//            }
//            QualityType.Debug -> {
//                for (i in it.indices) {
//                    val judgeQuality = JudgeTrendUtils.judgeDebugById(
//                        stocks[i].stockCode!!,
//                        debugID,
//                        ArrayList<StockDayModel>().apply {
//
//                            if (DateUtils.checkWorkday(
//                                    DateUtils.longToString(
//                                        System.currentTimeMillis(),
//                                        "yyyy-MM-dd"
//                                    )!!
//                                ) == WeekDay.Workday
//                            )
//                                if (DateUtils.isCurrentInTimeScope()) {
//                                    val stockItemModel =
//                                        LitePalDBase.queryStockByCode(stocks[i].stockCode!!)[0]
//                                    add(StockDayModel().apply {
//                                        dateTime = DateUtils.today("yyyy-MM-dd")
//                                        openPrice = stockItemModel.openPrice?.toFloat()
//                                        nowPrice = stockItemModel.nowPrice?.toFloat()
//                                        swing =
//                                            stockItemModel.nowPrice?.toFloat()!! - stockItemModel.yestClosePrice?.toFloat()!!
//                                        stockRate = stockItemModel.stockRate
//                                        minPrice = stockItemModel.todayMin?.toFloat()
//                                        maxPrice = stockItemModel.todayMax?.toFloat()
//                                        tradeNum = stockItemModel.tradeNum?.toLong()
//                                        tradeAmount = stockItemModel.tradeAmount?.toFloat()
//                                        turnoverRate = stockItemModel.turnoverRate
//                                        yinyang = nowPrice!! > openPrice!!
//                                    })
//                                }
//
//                            for (dayStock in it[i].hq) {
//                                add(StockDayModel().apply {
//                                    dateTime = dayStock[0]
//                                    openPrice = dayStock[1].toFloat()
//                                    nowPrice = dayStock[2].toFloat()
//                                    swing = dayStock[3].toFloat()
//                                    stockRate = dayStock[4].replace("%", "").toFloat()
//                                    minPrice = dayStock[5].toFloat()
//                                    maxPrice = dayStock[6].toFloat()
//                                    tradeNum = dayStock[7].toLong() * 100
//                                    tradeAmount = dayStock[8].toFloat() * 10000
//                                    turnoverRate = if (dayStock[9] == "-")
//                                        0.00f
//                                    else
//                                        dayStock[9].replace("%", "").toFloat()
//                                    yinyang = openPrice!! < nowPrice!!
//                                })
//                            }
//
//                            when (get(0).dateTime?.let { it1 -> DateUtils.checkWorkday(it1) }) {
//                                WeekDay.Saturday -> removeAt(0)
//                                WeekDay.Sunday -> {
//                                    removeAt(1)
//                                    removeAt(0)
//                                }
//                                else -> {
//                                }
//                            }
//
//                            if (beforeOneDay.get()!!)
//                                for (index in 1..BaseApplication.instance?.filterOptions?.beforeDay!!) {
//                                    removeAt(0)
//                                }
//
//                            stocks[i].lastFivePointDateTime = this.get(0).dateTime
//                        })
//
//                    stocks[i].debugId = debugID
//
//                    if (judgeQuality) {
//                        if (highQuality.get()!!) {
//                            if (JudgeTrendUtils.judgeDebugById(
//                                    stocks[i].stockCode!!,
//                                    2,
//                                    ArrayList()
//                                )
//                            )
//                                highQualityStocks.add(stocks[i])
//                        } else highQualityStocks.add(stocks[i])
//
//                        println("------------------>High Qulity : ${it[i].code}")
//                    } else {
//                        println("------------------>Low Qulity : ${it[i].code}")
//                    }
//                }
//            }
//            else -> {
//                for (i in it.indices) {
//                    val judgeQuality = JudgeTrendUtils.judgeCannonStock(
//                        selectType,
//                        ArrayList<StockDayModel>().apply {
//
//                            if (DateUtils.isCurrentInTimeScope()) {
//                                val stockItemModel =
//                                    LitePalDBase.queryStockByCode(stocks[i].stockCode!!)[0]
//                                add(StockDayModel().apply {
//                                    dateTime = DateUtils.today("yyyy-MM-dd")
//                                    openPrice = stockItemModel.openPrice?.toFloat()
//                                    nowPrice = stockItemModel.nowPrice?.toFloat()
//                                    swing =
//                                        stockItemModel.nowPrice?.toFloat()!! - stockItemModel.yestClosePrice?.toFloat()!!
//                                    stockRate = stockItemModel.stockRate
//                                    minPrice = stockItemModel.todayMin?.toFloat()
//                                    maxPrice = stockItemModel.todayMax?.toFloat()
//                                    tradeNum = stockItemModel.tradeNum?.toLong()
//                                    tradeAmount = stockItemModel.tradeAmount?.toFloat()
//                                    turnoverRate = stockItemModel.turnoverRate
//                                    yinyang = openPrice!! < nowPrice!!
//                                })
//                            }
//
//                            for (dayStock in it[i].hq) {
//                                add(StockDayModel().apply {
//                                    dateTime = dayStock[0]
//                                    openPrice = dayStock[1].toFloat()
//                                    nowPrice = dayStock[2].toFloat()
//                                    swing = dayStock[3].toFloat()
//                                    stockRate = dayStock[4].replace("%", "").toFloat()
//                                    minPrice = dayStock[5].toFloat()
//                                    maxPrice = dayStock[6].toFloat()
//                                    tradeNum = dayStock[7].toLong() * 100
//                                    tradeAmount = dayStock[8].toFloat() * 10000
//                                    turnoverRate = if (dayStock[9] == "-")
//                                        0.00f
//                                    else
//                                        dayStock[9].replace("%", "").toFloat()
//                                    yinyang = openPrice!! < nowPrice!!
//                                })
//                            }
//
//                            when (get(0).dateTime?.let { it1 -> DateUtils.checkWorkday(it1) }) {
//                                WeekDay.Saturday -> removeAt(0)
//                                WeekDay.Sunday -> {
//                                    removeAt(1)
//                                    removeAt(0)
//                                }
//                                else -> {
//                                }
//                            }
//
//                            if (beforeOneDay.get()!!)
//                                for (index in 1..BaseApplication.instance?.filterOptions?.beforeDay!!) {
//                                    removeAt(0)
//                                }
//                        })
//
//                    if (judgeQuality) {
//                        if (highQuality.get()!!) {
//                            if (JudgeTrendUtils.judgeDebugById(
//                                    stocks[i].stockCode!!,
//                                    2,
//                                    ArrayList()
//                                )
//                            )
//                                highQualityStocks.add(stocks[i])
//                        } else highQualityStocks.add(stocks[i])
//                        println("------------------>High Qulity : ${it[i].code}")
//                    } else {
//                        println("------------------>Low Qulity : ${it[i].code}")
//                    }
//                }
//            }
//        }
//    }
//}