package com.magic.inmoney.viewmodel.kline

import android.annotation.SuppressLint
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.fujianlian.klinechart.DataHelper
import com.github.fujianlian.klinechart.KLineEntity
import com.magic.inmoney.base.BaseViewModel
import com.magic.inmoney.orm.LitePalDBase
import com.magic.upcoming.games.utilities.Event
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class KLineViewModel : BaseViewModel() {

    var initData: ObservableField<Boolean> = ObservableField(false)
    var emptyStatus: ObservableField<Boolean> = ObservableField(false)
    var sr3 = 0f
    var sr6 = 0f
    var tr3 = 0f
    var tr6 = 0f

    private val _LoadingStatus = MutableLiveData<Event<Boolean>>()
    val loadingStatus: LiveData<Event<Boolean>>
        get() = _LoadingStatus

    private val _KLineData = MutableLiveData<Event<ArrayList<KLineEntity>>>()
    val kLineData: LiveData<Event<ArrayList<KLineEntity>>>
        get() = _KLineData

    private val _StockRate3 = MutableLiveData<Event<Float>>()
    val stockRate3: LiveData<Event<Float>>
        get() = _StockRate3

    private val _StockRate6 = MutableLiveData<Event<Float>>()
    val stockRate6: LiveData<Event<Float>>
        get() = _StockRate6

    private val _TurnoverRate3 = MutableLiveData<Event<Float>>()
    val turnoverRate3: LiveData<Event<Float>>
        get() = _TurnoverRate3

    private val _TurnoverRate6 = MutableLiveData<Event<Float>>()
    val turnoverRate6: LiveData<Event<Float>>
        get() = _TurnoverRate6

    private fun loadingStatus() {
        initData.set(false)
        _LoadingStatus.value = Event(true)
    }

    @SuppressLint("CheckResult")
    fun loadKLineData(stockCode: String) {

//        val stockItemModels = LitePalDBase.queryStockItemByCode(stockCode)

        Flowable.just(stockCode)
            .subscribeOn(Schedulers.newThread())
            .map {
                LitePalDBase.queryStockItemByCode(it)
            }
            .map { stockItemModels ->
                ArrayList<KLineEntity>().apply {
                    stockItemModels.forEach {stockItemModel->
                        add(KLineEntity().apply {
//                            Date = stockItemModel.dateTime
//                            Open = stockItemModel.openPrice?.toFloat()!!
//                            Close = stockItemModel.nowPrice?.toFloat()!!
//                            Low = stockItemModel.todayMin?.toFloat()!!
//                            High = stockItemModel.todayMax?.toFloat()!!
//                            Volume = stockItemModel.tradeNum?.toFloat()!!
                            Date = stockItemModel.dateTime
                            Open = stockItemModel.openPrice?.toFloat()!!
                            Close = stockItemModel.nowPrice?.toFloat()!!
                            Low = stockItemModel.todayMin?.toFloat()!!
                            High = stockItemModel.todayMax?.toFloat()!!
                            Volume = stockItemModel.tradeNum?.toFloat()!!
                            MA3Price = stockItemModel.mA3Price
                            MA5Price = stockItemModel.mA5Price
                            MA8Price = stockItemModel.mA8Price
                            MA10Price = stockItemModel.mA10Price
                            MA13Price = stockItemModel.mA13Price
                            MA20Price = stockItemModel.mA20Price
                            MA21Price = stockItemModel.mA21Price
                            MA30Price = stockItemModel.mA30Price
                            MA60Price = stockItemModel.mA60Price
                            dea = stockItemModel.dea
                            dif = stockItemModel.dif
                            macd = stockItemModel.macd
                            k = stockItemModel.k
                            d = stockItemModel.d
                            j = stockItemModel.j
                            r = stockItemModel.r
                            rsi = stockItemModel.rsi
                            up = stockItemModel.up
                            mb = stockItemModel.mb
                            dn = stockItemModel.dn
                            MA5Volume = stockItemModel.mA5Volume
                            MA10Volume = stockItemModel.mA10Volume
                            turnoverRate = stockItemModel.turnoverRate!!
                        })
                    }
                }
            }
//            .doOnNext {
//                DataHelper.calculate(it)
//            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.size > 0)
                    _KLineData.value = Event(it)

                _StockRate3.value = Event(sr3)
                _StockRate6.value = Event(sr6)
                _TurnoverRate3.value = Event(tr3)
                _TurnoverRate6.value = Event(tr6)
                println("------------------>Data : ${it.size}")
            }, {
                toast("Error : ${it.message}")
                println("------------------>Throwable : ${it.message}")
            })


//        Rx2AndroidNetworking.get("https://q.stock.sohu.com/hisHq?code={code}&start={beforeDay}&end={today}&stat=1&order=D&period=d&rt=json")
//            .addPathParameter("code", stockCode)
//            .addPathParameter("today", DateUtils.today())
//            .addPathParameter("beforeDay", DateUtils.beforeFiveDay(300))
//            .build()
//            .getObjectListObservable(DaysStockInfoModel::class.java)
////            .stringObservable
//            .subscribeOn(Schedulers.io())
//            .doOnError { println("------------------>Error : $it") }
//            .map {
//                ArrayList<KLineEntity>().apply {
//
//                    if (DateUtils.isCurrentInTimeScope() && DateUtils.isWorkday()) {
//                        val stockItemModel =
//                            LitePalDBase.queryStockByCode(stockCode.replace("cn_", ""))[0]
//                        if (it[0].hq[0][0] != stockItemModel.dateTime) {
//                            add(KLineEntity().apply {
//                                Date = DateUtils.today("yyyy-MM-dd")
//                                Open = stockItemModel.openPrice?.toFloat()!!
//                                Close = stockItemModel.nowPrice?.toFloat()!!
//                                Low = stockItemModel.todayMin?.toFloat()!!
//                                High = stockItemModel.todayMax?.toFloat()!!
//                                Volume = stockItemModel.tradeNum?.toFloat()!!
//                            })
//                        }
//                    }
//
//                    for (i in it[0].hq.indices){
//
//                        if (i < 3){
//                            sr3 += it[0].hq[i][4].replace("%","").toFloat()
//                            tr3 += it[0].hq[i][9].replace("%","").toFloat()
//                        }
//
//                        if (i < 6){
//                            sr6 += it[0].hq[i][4].replace("%","").toFloat()
//                            tr6 += it[0].hq[i][9].replace("%","").toFloat()
//                        }
//
//                        add(KLineEntity().apply {
//                            Date = it[0].hq[i][0]
//                            Volume = it[0].hq[i][7].toFloat() * 100
//                            Open = it[0].hq[i][1].toFloat()
//                            Close = it[0].hq[i][2].toFloat()
//                            Low = it[0].hq[i][5].toFloat()
//                            High = it[0].hq[i][6].toFloat()
//                        })
//                    }
//
//                }
//            }
//            .doOnNext {
//                it.reverse()
//                DataHelper.calculate(it)
//            }
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe({
//                if (it.size > 0)
//                    _KLineData.value = Event(it)
//
//                _StockRate3.value = Event(sr3)
//                _StockRate6.value = Event(sr6)
//                _TurnoverRate3.value = Event(tr3)
//                _TurnoverRate6.value = Event(tr6)
//                println("------------------>Data : ${it.size}")
//            }, {
//                toast("Error : ${it.message}")
//                println("------------------>Throwable : ${it.message}")
//            })
    }
}