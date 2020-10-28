package com.magic.inmoney.viewmodel.statistics

import android.annotation.SuppressLint
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.fujianlian.klinechart.DataHelper
import com.github.fujianlian.klinechart.KLineEntity
import com.magic.inmoney.base.BaseApplication
import com.magic.inmoney.base.BaseViewModel
import com.magic.inmoney.model.DaysStockInfoModel
import com.magic.inmoney.model.StatisticsModel
import com.magic.inmoney.orm.LitePalDBase
import com.magic.inmoney.utilities.DateUtils
import com.magic.inmoney.utilities.ModelConversionUtils
import com.magic.inmoney.utilities.TestUtils
import com.magic.upcoming.games.utilities.Event
import com.rx2androidnetworking.Rx2AndroidNetworking
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class StatisticsViewModel : BaseViewModel() {

    var initData: ObservableField<Boolean> = ObservableField(true)
    var emptyStatus: ObservableField<Boolean> = ObservableField(false)
    var position = 0
    var stockCount = BaseApplication.instance?.stockItems?.size

    private val _LoadingStatus = MutableLiveData<Event<Boolean>>()
    val loadingStatus: LiveData<Event<Boolean>>
        get() = _LoadingStatus

    fun loadingStatus() {
        initData.set(false)
        _LoadingStatus.value = Event(true)
    }

    private val _LoadingProgress = MutableLiveData<Event<Int>>()
    val loadingProgress: LiveData<Event<Int>>
        get() = _LoadingProgress

    private val _StatisticsStockData = MutableLiveData<Event<StatisticsModel>>()
    val statisticsStockData: LiveData<Event<StatisticsModel>>
        get() = _StatisticsStockData

    fun updateStatisticsData() {

        if (stockCount == 0) {
            toast("没有股票基础数据，请先更新股票信息")
            return
        }

        if (initData.get()!!) {
            toast("正在更新中...请稍候")
            return
        }

        position = 0

        initData.set(true)

        syncStatisticsData()
    }

    private fun syncStatisticsData() {
        if (position == BaseApplication.instance?.stockItems?.size!!) {
            loadingStatus()
            return
        }

//        val stockHistories = LitePalDBase.queryStockHistoryItemsByCode(BaseApplication.instance?.stockItems?.get(position)?.stockCode!!)
//        if (stockHistories.size > 0){
//
//        }else
//        loadKLineData(
//            BaseApplication.instance?.stockItems?.get(position)?.stockCode!!,
//            BaseApplication.instance?.stockItems?.get(position)?.stockName!!
//        )

        loadLocalStockHistoryData()
    }

    @SuppressLint("CheckResult")
    fun loadKLineData(stockCode: String, stockName: String) {
        Rx2AndroidNetworking.get("https://q.stock.sohu.com/hisHq?code={code}&start={beforeDay}&end={today}&stat=1&order=D&period=d&rt=json")
            .addPathParameter("code", "cn_$stockCode")
            .addPathParameter("today", DateUtils.today())
            .addPathParameter("beforeDay", DateUtils.beforeFiveDay(300))
            .build()
            .getObjectListObservable(DaysStockInfoModel::class.java)
//            .stringObservable
            .subscribeOn(Schedulers.io())
            .doOnError { println("------------------>Error : $it") }
            .map {
                ArrayList<KLineEntity>().apply {

                    if (DateUtils.isCurrentInTimeScope() && DateUtils.isWorkday()) {
                        val stockItemModel =
                            LitePalDBase.queryStockByCode(stockCode.replace("cn_", ""))[0]
                        if (it[0].hq[0][0] != stockItemModel.dateTime) {
                            add(KLineEntity().apply {
                                Date = DateUtils.today("yyyy-MM-dd")
                                Open = stockItemModel.openPrice?.toFloat()!!
                                Close = stockItemModel.nowPrice?.toFloat()!!
                                Low = stockItemModel.todayMin?.toFloat()!!
                                High = stockItemModel.todayMax?.toFloat()!!
                                Volume = stockItemModel.tradeNum?.toFloat()!!
                            })
                        }
                    }

                    for (i in it[0].hq.indices) {
                        add(KLineEntity().apply {
                            Date = it[0].hq[i][0]
                            Volume = it[0].hq[i][7].toFloat() * 100
                            Open = it[0].hq[i][1].toFloat()
                            Close = it[0].hq[i][2].toFloat()
                            Low = it[0].hq[i][5].toFloat()
                            High = it[0].hq[i][6].toFloat()
                        })
                    }

                }
            }
            .doOnNext {
                it.reverse()
                DataHelper.calculate(it)
            }
            .map {
                TestUtils.judgeFiveSuccessRate(stockCode, stockName, it)
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                _StatisticsStockData.value = Event(it)
                position += 1
                _LoadingProgress.value = Event(position)
                syncStatisticsData()
            }, {
                toast("Error : ${it.message}")
                position += 1
                _LoadingProgress.postValue(Event(position))
                syncStatisticsData()
            })
    }

    @SuppressLint("CheckResult")
    private fun loadLocalStockHistoryData(){

        if (position == BaseApplication.instance?.stockItems?.size!!){
            toast("统计完成")
            loadingStatus()
            return
        }

        val stockHistories = LitePalDBase.queryStockHistoryItemsByCode(BaseApplication.instance?.stockItems?.get(position)?.stockCode!!)

            Flowable.just(stockHistories)
                .doOnError { println("------------------>Error : $it") }
                .subscribeOn(AndroidSchedulers.mainThread())
                .filter {
                    if (it.size == 0){
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
                    TestUtils.judgeFiveSuccessRate(BaseApplication.instance?.stockItems?.get(position)?.stockCode!!,
                        BaseApplication.instance?.stockItems?.get(position)?.stockName!!, it)
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