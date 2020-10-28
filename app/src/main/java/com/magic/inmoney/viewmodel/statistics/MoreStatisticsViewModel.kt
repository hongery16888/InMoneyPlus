package com.magic.inmoney.viewmodel.statistics

import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.magic.inmoney.base.BaseApplication
import com.magic.inmoney.base.BaseViewModel
import com.magic.inmoney.const.StatisticsType
import com.magic.inmoney.model.StatisticsModel
import com.magic.inmoney.model.StockItemModel
import com.magic.inmoney.orm.LitePalDBase
import com.magic.inmoney.utilities.ModelConversionUtils
import com.magic.inmoney.utilities.TestUtils
import com.magic.upcoming.games.utilities.Event
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.reactivestreams.Subscription

class MoreStatisticsViewModel : BaseViewModel() {

    var initData: ObservableField<Boolean> = ObservableField(false)
    var emptyStatus: ObservableField<Boolean> = ObservableField(true)
    var needThirdDay: ObservableField<Boolean> = ObservableField(false)
    var stopHide: ObservableField<Boolean> = ObservableField(true)
    var position = 0
    var stockCount = BaseApplication.instance?.stocks?.size
    var statisticsType = StatisticsType.StatisticsMidLine
    var mSubscription: Subscription? = null
    private var mDisposable: Disposable? = null
    private var stockItems = ArrayList<StockItemModel>()

    private val _LoadingStatus = MutableLiveData<Event<Boolean>>()
    val loadingStatus: LiveData<Event<Boolean>>
        get() = _LoadingStatus

    fun loadingStatus() {
        initData.set(false)
        _LoadingStatus.value = Event(true)
        stopHide.set(true)
    }

    private val _LoadingProgress = MutableLiveData<Event<Int>>()
    val loadingProgress: LiveData<Event<Int>>
        get() = _LoadingProgress

    private val _StatisticsStockData = MutableLiveData<Event<StatisticsModel>>()
    val statisticsStockData: LiveData<Event<StatisticsModel>>
        get() = _StatisticsStockData

    fun stopStatisticsData() {
        mSubscription?.cancel()
        mDisposable?.dispose()
        loadingStatus()
    }

    fun updateStatisticsData() {

        if (stockCount == 0) {
            toast("没有股票基础数据，请先更新股票信息")
            return
        }

        if (initData.get()!!) {
            toast("正在更新中...请稍候")
            return
        }

        position = when (statisticsType) {
            StatisticsType.StatisticsUpLine -> LitePalDBase.queryStatisticsStockItemCount(
                StatisticsType.StatisticsUpLine.type
            )
            StatisticsType.StatisticsMidLine -> LitePalDBase.queryStatisticsStockItemCount(
                StatisticsType.StatisticsMidLine.type
            )
            StatisticsType.StatisticsDownLine -> LitePalDBase.queryStatisticsStockItemCount(
                StatisticsType.StatisticsDownLine.type
            )
            StatisticsType.StatisticsThreeUpLine -> LitePalDBase.queryStatisticsStockItemCount(
                StatisticsType.StatisticsThreeUpLine.type
            )
            StatisticsType.StatisticsThreeMidLine -> LitePalDBase.queryStatisticsStockItemCount(
                StatisticsType.StatisticsThreeMidLine.type
            )
            StatisticsType.StatisticsThreeDownLine -> LitePalDBase.queryStatisticsStockItemCount(
                StatisticsType.StatisticsThreeDownLine.type
            )
            StatisticsType.StatisticsThreeSortUpLine -> LitePalDBase.queryStatisticsStockItemCount(
                StatisticsType.StatisticsThreeSortUpLine.type
            )
            StatisticsType.StatisticsThreeSortMidLine -> LitePalDBase.queryStatisticsStockItemCount(
                StatisticsType.StatisticsThreeSortMidLine.type
            )
            StatisticsType.StatisticsThreeSortDownLine -> LitePalDBase.queryStatisticsStockItemCount(
                StatisticsType.StatisticsThreeSortDownLine.type
            )
            StatisticsType.StatisticsDoubleDayUpLine -> LitePalDBase.queryStatisticsStockItemCount(
                StatisticsType.StatisticsDoubleDayUpLine.type
            )
            StatisticsType.StatisticsHighQualityUpLine -> LitePalDBase.queryStatisticsStockItemCount(
                StatisticsType.StatisticsHighQualityUpLine.type
            )
            StatisticsType.Statistics5813UpLine -> LitePalDBase.queryStatisticsStockItemCount(
                StatisticsType.Statistics5813UpLine.type
            )
            StatisticsType.Statistics3510UpLine -> LitePalDBase.queryStatisticsStockItemCount(
                StatisticsType.Statistics3510UpLine.type
            )
            StatisticsType.StatisticsRareMidLine -> LitePalDBase.queryStatisticsStockItemCount(
                StatisticsType.StatisticsRareMidLine.type
            )
            StatisticsType.StatisticsMACD -> LitePalDBase.queryStatisticsStockItemCount(
                StatisticsType.StatisticsMACD.type
            )
        }

        initData.set(true)
        stopHide.set(false)
        syncStatisticsData()
    }

    private fun syncStatisticsData() {
        if (position == BaseApplication.instance?.stocks?.size!!) {
            loadingStatus()
            return
        }

        if (statisticsType == StatisticsType.StatisticsMACD){
            loadLocalStockForMACD()
        }else
            loadLocalStockHistoryData()
    }

    private fun loadLocalStockForMACD(){

        if (position == BaseApplication.instance?.stocks?.size!!) {
            toast("统计完成")
            loadingStatus()
            return
        }

        mDisposable?.dispose()
        mSubscription?.cancel()

        mDisposable = Flowable.just(BaseApplication.instance?.stocks!![position].code!!)
            .doOnSubscribe {
                mSubscription = it
            }
            .filter { stockCode ->
                val status = stockCode.startsWith("600") ||
                        stockCode.startsWith("601") ||
                        stockCode.startsWith("603") ||
                        stockCode.startsWith("605") ||
                        stockCode.startsWith("000") ||
                        stockCode.startsWith("002")

                if (!status){
                    position += 1
                    _LoadingProgress.postValue(Event(position))
                    loadLocalStockForMACD()
                }
                status
            }
            .doOnError { println("------------------>Error : $it") }
            .subscribeOn(Schedulers.newThread())
            .doOnNext {stockCode ->
                stockItems.clear()
                stockItems = LitePalDBase.queryStockItemByCode(stockCode)
            }
            .filter {
                if (stockItems.size == 0) {
                    position += 1
                    _LoadingProgress.postValue(Event(position))
                    loadLocalStockForMACD()
                }

                stockItems.size > 0
            }
            .map { stockCode ->
                TestUtils.judgeFiveSuccessRateForMACD(stockCode,
                    BaseApplication.instance?.stocks!![position].name!!,
                    stockItems, StatisticsType.StatisticsMACD.type, 0, needThirdDay.get()!!)
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                _StatisticsStockData.value = Event(it)
                position += 1
                _LoadingProgress.postValue(Event(position))
                loadLocalStockForMACD()
            }, {
                println("------------------>Error : ${it.message}")
                position += 1
                _LoadingProgress.postValue(Event(position))
                loadLocalStockForMACD()
            })

    }

    private fun loadLocalStockHistoryData() {

        if (position == BaseApplication.instance?.stockItems?.size!!) {
            toast("统计完成")
            loadingStatus()
            return
        }

//        val stockHistories = LitePalDBase.queryStockHistoryItemsByCode4Limit(
//            BaseApplication.instance?.stockItems?.get(position)?.stockCode!!,
//            when (statisticsType) {
//                StatisticsType.StatisticsUpLine -> StatisticsType.StatisticsUpLine.count
//                StatisticsType.StatisticsMidLine -> StatisticsType.StatisticsMidLine.count
//                StatisticsType.StatisticsDownLine -> StatisticsType.StatisticsDownLine.count
//                StatisticsType.StatisticsThreeUpLine -> StatisticsType.StatisticsThreeUpLine.count
//                StatisticsType.StatisticsThreeMidLine -> StatisticsType.StatisticsThreeMidLine.count
//                StatisticsType.StatisticsThreeDownLine -> StatisticsType.StatisticsThreeDownLine.count
//                StatisticsType.StatisticsThreeSortUpLine -> StatisticsType.StatisticsThreeSortUpLine.count
//                StatisticsType.StatisticsThreeSortMidLine -> StatisticsType.StatisticsThreeSortMidLine.count
//                StatisticsType.StatisticsThreeSortDownLine -> StatisticsType.StatisticsThreeSortDownLine.count
//                StatisticsType.StatisticsDoubleDayUpLine -> StatisticsType.StatisticsDoubleDayUpLine.count
//                StatisticsType.StatisticsHighQualityUpLine -> StatisticsType.StatisticsHighQualityUpLine.count
//                StatisticsType.StatisticsTodayUpLine -> StatisticsType.StatisticsTodayUpLine.count
//                else -> 300
//            }
//        )

        mDisposable?.dispose()
        mSubscription?.cancel()

        mDisposable = Flowable.just("")
            .doOnSubscribe {
                mSubscription = it
            }
            .doOnError { println("------------------>Error : $it") }
            .subscribeOn(Schedulers.newThread())
            .map {
                LitePalDBase.queryStockHistoryItemsByCode4Limit(
                    BaseApplication.instance?.stockItems?.get(position)?.stockCode!!,
                    when (statisticsType) {
                        StatisticsType.StatisticsUpLine -> StatisticsType.StatisticsUpLine.count
                        StatisticsType.StatisticsMidLine -> StatisticsType.StatisticsMidLine.count
                        StatisticsType.StatisticsDownLine -> StatisticsType.StatisticsDownLine.count
                        StatisticsType.StatisticsThreeUpLine -> StatisticsType.StatisticsThreeUpLine.count
                        StatisticsType.StatisticsThreeMidLine -> StatisticsType.StatisticsThreeMidLine.count
                        StatisticsType.StatisticsThreeDownLine -> StatisticsType.StatisticsThreeDownLine.count
                        StatisticsType.StatisticsThreeSortUpLine -> StatisticsType.StatisticsThreeSortUpLine.count
                        StatisticsType.StatisticsThreeSortMidLine -> StatisticsType.StatisticsThreeSortMidLine.count
                        StatisticsType.StatisticsThreeSortDownLine -> StatisticsType.StatisticsThreeSortDownLine.count
                        StatisticsType.StatisticsDoubleDayUpLine -> StatisticsType.StatisticsDoubleDayUpLine.count
                        StatisticsType.StatisticsHighQualityUpLine -> StatisticsType.StatisticsHighQualityUpLine.count
                        StatisticsType.Statistics5813UpLine -> StatisticsType.Statistics5813UpLine.count
                        StatisticsType.Statistics3510UpLine -> StatisticsType.Statistics3510UpLine.count
                        StatisticsType.StatisticsRareMidLine -> StatisticsType.StatisticsRareMidLine.count
                        else -> 300
                    })
            }
            .filter {
                if (it.size == 0) {
                    position += 1
                    _LoadingProgress.postValue(Event(position))
                    loadLocalStockHistoryData()
                }

                it.size > 0
            }
            .subscribeOn(Schedulers.newThread())
            .map {
                ModelConversionUtils.stockHistoryToKLineEntity(it)
            }
            .doOnNext {
                it.reverse()
            }
            .map {
                if (statisticsType == StatisticsType.StatisticsUpLine ||
                    statisticsType == StatisticsType.StatisticsMidLine ||
                    statisticsType == StatisticsType.StatisticsDownLine
                ) {
                    TestUtils.judgeFiveSuccessRateForMinLine(
                        BaseApplication.instance?.stockItems?.get(position)?.stockCode!!,
                        BaseApplication.instance?.stockItems?.get(position)?.stockName!!,
                        it, when (statisticsType) {
                            StatisticsType.StatisticsUpLine -> StatisticsType.StatisticsUpLine.type
                            StatisticsType.StatisticsMidLine -> StatisticsType.StatisticsMidLine.type
                            StatisticsType.StatisticsDownLine -> StatisticsType.StatisticsDownLine.type
                            else -> "test"
                        },
                        when (statisticsType) {
                            StatisticsType.StatisticsUpLine -> StatisticsType.StatisticsUpLine.line
                            StatisticsType.StatisticsMidLine -> StatisticsType.StatisticsMidLine.line
                            StatisticsType.StatisticsDownLine -> StatisticsType.StatisticsDownLine.line
                            else -> 0
                        }, needThirdDay.get()!!
                    )
                } else if (statisticsType == StatisticsType.StatisticsThreeUpLine ||
                    statisticsType == StatisticsType.StatisticsThreeMidLine ||
                    statisticsType == StatisticsType.StatisticsThreeDownLine
                ) {
                    TestUtils.judgeFiveSuccessRateForMinLine3Three(
                        BaseApplication.instance?.stockItems?.get(position)?.stockCode!!,
                        BaseApplication.instance?.stockItems?.get(position)?.stockName!!,
                        it, when (statisticsType) {
                            StatisticsType.StatisticsThreeUpLine -> StatisticsType.StatisticsThreeUpLine.type
                            StatisticsType.StatisticsThreeMidLine -> StatisticsType.StatisticsThreeMidLine.type
                            StatisticsType.StatisticsThreeDownLine -> StatisticsType.StatisticsThreeDownLine.type
                            else -> "test"
                        },
                        when (statisticsType) {
                            StatisticsType.StatisticsThreeUpLine -> StatisticsType.StatisticsThreeUpLine.line
                            StatisticsType.StatisticsThreeMidLine -> StatisticsType.StatisticsThreeMidLine.line
                            StatisticsType.StatisticsThreeDownLine -> StatisticsType.StatisticsThreeDownLine.line
                            else -> 0
                        }, needThirdDay.get()!!
                    )
                }else if (statisticsType == StatisticsType.StatisticsDoubleDayUpLine){
                    TestUtils.judgeFiveSuccessRateForDoubleDay(
                        BaseApplication.instance?.stockItems?.get(position)?.stockCode!!,
                        BaseApplication.instance?.stockItems?.get(position)?.stockName!!,
                        it, StatisticsType.StatisticsDoubleDayUpLine.type,
                        StatisticsType.StatisticsDoubleDayUpLine.line, needThirdDay.get()!!
                    )
                }else if (statisticsType == StatisticsType.StatisticsHighQualityUpLine){
                    TestUtils.judgeFiveSuccessRateForHighQuality(
                        BaseApplication.instance?.stockItems?.get(position)?.stockCode!!,
                        BaseApplication.instance?.stockItems?.get(position)?.stockName!!,
                        it, StatisticsType.StatisticsHighQualityUpLine.type,
                        StatisticsType.StatisticsHighQualityUpLine.line, needThirdDay.get()!!
                    )
                }else if (statisticsType == StatisticsType.Statistics5813UpLine){
                    TestUtils.judgeFiveSuccessRateFor5813New(
                        BaseApplication.instance?.stockItems?.get(position)?.stockCode!!,
                        BaseApplication.instance?.stockItems?.get(position)?.stockName!!,
                        it, StatisticsType.Statistics5813UpLine.type,
                        StatisticsType.Statistics5813UpLine.line, needThirdDay.get()!!
                    )
                } else if (statisticsType == StatisticsType.Statistics3510UpLine){
                    TestUtils.judgeFiveSuccessRateForLow(
                        BaseApplication.instance?.stockItems?.get(position)?.stockCode!!,
                        BaseApplication.instance?.stockItems?.get(position)?.stockName!!,
                        it, StatisticsType.Statistics3510UpLine.type,
                        StatisticsType.Statistics3510UpLine.line, needThirdDay.get()!!
                    )
                } else if (statisticsType == StatisticsType.StatisticsRareMidLine){
                    TestUtils.judgeFiveSuccessRateForLowPrice(
                        BaseApplication.instance?.stockItems?.get(position)?.stockCode!!,
                        BaseApplication.instance?.stockItems?.get(position)?.stockName!!,
                        it, StatisticsType.StatisticsRareMidLine.type,
                        StatisticsType.StatisticsRareMidLine.line, needThirdDay.get()!!
                    )
                } else {
                    TestUtils.judgeFiveSuccessRateForMinLine3ThreeSort(
                        BaseApplication.instance?.stockItems?.get(position)?.stockCode!!,
                        BaseApplication.instance?.stockItems?.get(position)?.stockName!!,
                        it, when (statisticsType) {
                            StatisticsType.StatisticsThreeSortUpLine -> StatisticsType.StatisticsThreeSortUpLine.type
                            StatisticsType.StatisticsThreeSortMidLine -> StatisticsType.StatisticsThreeSortMidLine.type
                            StatisticsType.StatisticsThreeSortDownLine -> StatisticsType.StatisticsThreeSortDownLine.type
                            else -> "test"
                        },
                        when (statisticsType) {
                            StatisticsType.StatisticsThreeSortUpLine -> StatisticsType.StatisticsThreeSortUpLine.line
                            StatisticsType.StatisticsThreeSortMidLine -> StatisticsType.StatisticsThreeSortMidLine.line
                            StatisticsType.StatisticsThreeSortDownLine -> StatisticsType.StatisticsThreeSortDownLine.line
                            else -> 0
                        }, needThirdDay.get()!!
                    )
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                _StatisticsStockData.value = Event(it)
                position += 1
                _LoadingProgress.postValue(Event(position))
                loadLocalStockHistoryData()
            }, {
                println("------------------>Error : ${it.message}")
                position += 1
                _LoadingProgress.postValue(Event(position))
                loadLocalStockHistoryData()
            })
    }
}