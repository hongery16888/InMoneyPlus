package com.magic.inmoney.viewmodel.allstock

import android.annotation.SuppressLint
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.magic.inmoney.base.BaseApplication
import com.magic.inmoney.base.BaseViewModel
import com.magic.inmoney.model.StockItemModel
import com.magic.inmoney.model.StockItemTodayModel
import com.magic.inmoney.model.StockModel
import com.magic.inmoney.orm.LitePalDBase
import com.magic.upcoming.games.utilities.Event
import com.rx2androidnetworking.Rx2AndroidNetworking
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.litepal.LitePal
import java.text.DecimalFormat

class AllStockViewModel : BaseViewModel() {

    var initData: ObservableField<Boolean> = ObservableField(true)
    var emptyStatus: ObservableField<Boolean> = ObservableField(false)
    var filterStatus: ObservableField<Boolean> = ObservableField(false)
    private var page = 0
    private var stockItems = ArrayList<StockItemTodayModel>()
    private var stocks = ArrayList<StockModel>()
    private var decimalFormat: DecimalFormat = DecimalFormat("#0.00")
    private var maxPage = 100
    private var currentCount = 0
    private var lastCount = 0
    private var dataCount = 0
    var isRefresh = false

    private val _LoadingStatus = MutableLiveData<Event<Boolean>>()
    val loadingStatus: LiveData<Event<Boolean>>
        get() = _LoadingStatus

    private fun loadingStatus() {
        initData.set(false)
        _LoadingStatus.value = Event(true)
    }

    private val _UpdateStatus = MutableLiveData<Event<Boolean>>()
    val updateStatus: LiveData<Event<Boolean>>
        get() = _UpdateStatus

    private val _StockList = MutableLiveData<Event<ArrayList<StockItemTodayModel>>>()
    val stockList: LiveData<Event<ArrayList<StockItemTodayModel>>>
        get() = _StockList

    fun reset(){
        currentCount = 0
        page = 0
        lastCount = 0
        dataCount = 0
        maxPage = 100
    }

    @SuppressLint("CheckResult")
    fun stockList() {

        stocks.clear()
        stockItems.clear()

        dataCount = BaseApplication.instance?.stocks?.size!!

        if ((LitePal.count(StockItemTodayModel::class.java) > 0 && !isRefresh)|| filterStatus.get()!!){

//        if ((BaseApplication.instance?.stockItemTodayModels?.size!! > 0 && !isRefresh) || filterStatus.get()!!){
            toast("加载股票完成")
//            stockItems.addAll(BaseApplication.instance?.stockItemTodayModels!!)
            stockItems.addAll(LitePalDBase.queryAllTodayStockItem())
            _StockList.value = Event(ArrayList<StockItemTodayModel>().apply {
                addAll(stockItems)
            })
            loadingStatus()
            filterStatus.set(false)
            return
        }

        if (dataCount > 0) {
            if (dataCount % 50 > 0) {
                maxPage = dataCount / 50 + 1
                lastCount = dataCount % 50
            } else {
                maxPage = dataCount / 50
                lastCount = 0
            }
        } else {
            toast("没有股票数据，新更新！")
            loadingStatus()
            return
        }

        if (page >= maxPage) {
            println("------------------>All Stock Load Complete!!!")
            toast("更新股票完成")
            _UpdateStatus.value = Event(true)
            loadingStatus()
            return
        }else{

            stocks.clear()

            if (dataCount - currentCount < 50){
                BaseApplication.instance?.stocks?.subList(page * 50, page * 50 + lastCount)?.let {
                    stocks.addAll(it)
                }
            }else{
                BaseApplication.instance?.stocks?.subList(page * 50, (page + 1) * 50)?.let {
                    stocks.addAll(it)
                }
            }
        }

        currentCount += stocks.size
        page += 1

        getStockListRate(stocks)

    }

    @SuppressLint("CheckResult")
    private fun getStockListRate(stocks: ArrayList<StockModel>) {
        stockItems.clear()
        var stocksListStr = ""

        for (stock in stocks) {
            stocksListStr += stock.market + stock.code + ","
        }

        Rx2AndroidNetworking.get("http://hq.sinajs.cn/format=text&list={codes}")
            .addPathParameter("codes", stocksListStr)
            .build()
            .stringObservable
            .subscribeOn(Schedulers.io())
            .doOnError { println("------------------>Error : $it") }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                val infos = it.split("\n")
                for (i in infos.indices) {
                    if (infos[i].isNullOrEmpty()) continue
                    val items = infos[i].split(",")
                    stockItems.add(StockItemTodayModel().apply {
                        stockCode = stocks[i].code
                        stockName = stocks[i].name
                        pinyin = stocks[i].pinyin
                        market = stocks[i].market
                        dateTime = items[30]
                        openPrice = items[1]
                        yestClosePrice = items[2]
                        nowPrice = items[3]
                        todayMax = items[4]
                        todayMin = items[5]
                        tradeNum = items[8]
                        tradeAmount = items[9]
                        turnoverRate = if (stocks[i].currcapital!!.isEmpty() || stocks[i].currcapital!!.toFloat() == 0f)  0.00f
                        else decimalFormat.format(items[8].toFloat() / 100 / stocks[i].currcapital!!.toFloat()).toFloat()
                        val rate =
                            (items[3].toFloat() - items[2].toFloat()) / items[2].toFloat() * 100
                        loss = rate < 0
                        stockRate = decimalFormat.format(rate).toFloat()
                        stockPrice = nowPrice
                    })
                }

                getStockInfo()
            }, {
                getStockInfo()
                println("------------------>Throwable : ${it.message}")
            })
    }

    private fun getStockInfo() {
        _StockList.value = Event(ArrayList<StockItemTodayModel>().apply {
            addAll(stockItems)
        })
        stockList()
    }
}