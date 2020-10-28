package com.magic.inmoney.viewmodel.update

import android.annotation.SuppressLint
import android.content.Context
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.fujianlian.klinechart.DataHelper
import com.github.fujianlian.klinechart.KLineEntity
import com.magic.inmoney.base.BaseApplication
import com.magic.inmoney.base.BaseViewModel
import com.magic.inmoney.const.StockConsts
import com.magic.inmoney.model.*
import com.magic.inmoney.model.BaseAllStockModel
import com.magic.inmoney.orm.LitePalDBase
import com.magic.inmoney.orm.StockInfoDB
import com.magic.inmoney.utilities.CommonUtils
import com.magic.inmoney.utilities.DateUtils
import com.magic.inmoney.utilities.ModelConversionUtils
import com.magic.inmoney.utilities.TestUtils
import com.magic.upcoming.games.utilities.Event
import com.rx2androidnetworking.Rx2AndroidNetworking
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.litepal.LitePal
import org.litepal.extension.saveAll

class UpdateViewModel : BaseViewModel() {

    var initData: ObservableField<Boolean> = ObservableField(false)
    var loadIndex: ObservableField<String> = ObservableField("")
    var calculateIndex: ObservableField<String> = ObservableField("")

    private var page = 1
    var maxPage = 100
    private var stockModels = ArrayList<StockModel>()
    private var index = 0
    private var kLines = ArrayList<KLineEntity>()
    private var historyNetwork = ArrayList<ArrayList<String>>()
    private var stockHistories = ArrayList<StockHistoryModel>()
    private var stockitemModels = ArrayList<StockItemModel>()

    private val _LoadingStatus = MutableLiveData<Event<Boolean>>()
    val loadingStatus: LiveData<Event<Boolean>>
        get() = _LoadingStatus

    private val _SHProgress = MutableLiveData<Event<Float>>()
    val shProgress: LiveData<Event<Float>>
        get() = _SHProgress

    private val _SZProgress = MutableLiveData<Event<Float>>()
    val szProgress: LiveData<Event<Float>>
        get() = _SZProgress

    private val _HistoryProgress = MutableLiveData<Event<Float>>()
    val historyProgress: LiveData<Event<Float>>
        get() = _HistoryProgress

    private val _CalculateProgress = MutableLiveData<Event<Float>>()
    val calculateProgress: LiveData<Event<Float>>
        get() = _CalculateProgress

    private val _InfoProgress = MutableLiveData<Event<Boolean>>()
    val infoProgress: LiveData<Event<Boolean>>
        get() = _InfoProgress

    private val _ReportProgress = MutableLiveData<Event<Boolean>>()
    val reportProgress: LiveData<Event<Boolean>>
        get() = _ReportProgress

    fun loadingStatus() {
        initData.set(false)
        _LoadingStatus.value = Event(true)
    }

    @SuppressLint("CheckResult")
    fun updateSHList() {

        initData.set(true)
        stockModels.clear()

        if (page > maxPage) {
            println("------------------>All Stock Load Complete!!!")
            loadingStatus()
            LitePalDBase.loadStock()
            page = 0
            return
        }

        Rx2AndroidNetworking.get("https://route.showapi.com/131-53?market=sh&page={page}&showapi_appid={app_id}&showapi_timestamp={time}&showapi_sign={secret}")
            .addPathParameter("app_id", StockConsts.APP_ID)
            .addPathParameter("secret", StockConsts.SECRET_KEY)
            .addPathParameter("page", page.toString())
            .addPathParameter(
                "time",
                DateUtils.longToString(System.currentTimeMillis(), "yyyyMMddHHmmss")
            )
            .build()
            .getObjectObservable(BaseAllStockModel::class.java)
            .subscribeOn(Schedulers.io())
            .doOnError { println("------------------>Error : $it") }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                println("------------------>Page : $page     MaxPage : $maxPage")
                page += 1

                maxPage = it.resBody?.allPages!!
                _SHProgress.value = Event(page.toFloat() / maxPage.toFloat() * 100)

                for (stock in it.resBody?.contentlist!!) {
                    if (!stock.name?.contains("退")!! && !stock.name?.contains("*")!! && !stock.name?.contains(
                            "ST"
                        )!!
                    )
                        stockModels.add(stock)
                }

                stockModels.saveAll()

                updateSHList()
            }, {
                println("------------------>Throwable : ${it.message}")
                loadingStatus()
            })
    }

    @SuppressLint("CheckResult")
    fun updateSZList() {

        initData.set(true)
        stockModels.clear()

        if (page > maxPage) {
            println("------------------>All Stock Load Complete!!!")
            loadingStatus()
            LitePalDBase.loadStock()
            page = 0
            return
        }

        Rx2AndroidNetworking.get("https://route.showapi.com/131-53?market=sz&page={page}&showapi_appid={app_id}&showapi_timestamp={time}&showapi_sign={secret}")
            .addPathParameter("app_id", StockConsts.APP_ID)
            .addPathParameter("secret", StockConsts.SECRET_KEY)
            .addPathParameter("page", page.toString())
            .addPathParameter(
                "time",
                DateUtils.longToString(System.currentTimeMillis(), "yyyyMMddHHmmss")
            )
            .build()
            .getObjectObservable(BaseAllStockModel::class.java)
            .subscribeOn(Schedulers.io())
            .doOnError { println("------------------>Error : $it") }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                println("------------------>Page : $page     MaxPage : $maxPage")
                page += 1

                maxPage = it.resBody?.allPages!!
                _SZProgress.value = Event(page.toFloat() / maxPage.toFloat() * 100)

                for (stock in it.resBody?.contentlist!!) {
                    if (!stock.name?.endsWith("退")!! && stock.state == 1)
                        stockModels.add(stock)
                }

                stockModels.saveAll()
                updateSZList()
            }, {
                println("------------------>Throwable : ${it.message}")
                loadingStatus()
            })
    }

    @SuppressLint("CheckResult")
    fun updateStockInfo(context: Context) {
        Flowable.just("")
            .subscribeOn(Schedulers.io())
            .doOnError { println("------------------>Error : $it") }
            .map {
                StockInfoDB.updateStockInfo(context)
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                loadingStatus()
                _InfoProgress.value = Event(false)
                if (it)
                    toast("更新股票基本信息成功")
                else
                    toast("更新股票基本信息失败")
            }, {
                println("------------------>Update Stock Info Throwable : ${it.message}")
            })
    }

    @SuppressLint("CheckResult")
    fun updateStockReport(context: Context) {
        Flowable.just("")
            .subscribeOn(Schedulers.io())
            .doOnError { println("------------------>Error : $it") }
            .map {
                StockInfoDB.updateStockReport(context)
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                loadingStatus()
                _ReportProgress.value = Event(false)
                if (it)
                    toast("更新年报成功")
                else
                    toast("更新年报失败")
            }, {
                println("------------------>Update Stock Info Throwable : ${it.message}")
            })
    }

    fun updateStockHistory(count: Int = 0) {
        if (BaseApplication.instance?.stocks?.size == 0) {
            toast("请先同步数据或重启启动APP")
            loadingStatus()
            return
        }

        if (count == 0) {
            LitePalDBase.deleteAllStockItem()
            index = 0
        } else {
            LitePalDBase.deleteStockItems(BaseApplication.instance?.stocks!![count - 1].code!!)
            index = count - 1
        }

        page = 0
        kLines.clear()
        historyNetwork.clear()
        stockHistories.clear()
        syncHistoryStockInfo()
    }

    @SuppressLint("CheckResult")
    private fun syncHistoryStockInfo() {

        if (index == BaseApplication.instance?.stocks?.size) {
            _HistoryProgress.value = Event(100f)
            toast("更新历史数据已完成")
            loadingStatus()
            return
        }

        if (index == 99 || index == 50) {
            println("------------------>2345780=")
        }

        Rx2AndroidNetworking.get("https://q.stock.sohu.com/hisHq?code={code}&start={startDate}&end={endDate}&stat=1&order=D&period=d&rt=json")
            .addPathParameter(
                "code",
                "cn_" + BaseApplication.instance?.stocks!![index].code!!
            )
            .addPathParameter(
                "endDate",
                DateUtils.beforeFiveDay(300L * page + 1)
            )
            .addPathParameter("startDate", DateUtils.beforeFiveDay(300L * (page + 1)))
            .build()
            .getObjectListObservable(DaysStockInfoModel::class.java)
            .subscribeOn(Schedulers.newThread())
            .doOnNext {
                if (stockitemModels.size == 0)
                    println("------------------>DataBase Count---" + LitePal.count(StockItemModel::class.java) + "---Index : $index---Code : ${BaseApplication.instance?.stocks!![index].code!!}")
            }
            .doOnError { println("------------------>Error : $it") }
            .map {
                stockitemModels.let { data ->
                    for (i in it[0].hq.indices) {
                        data.add(StockItemModel().apply {
                            stockCode = BaseApplication.instance?.stocks!![index].code!!
                            stockName = BaseApplication.instance?.stocks!![index].name!!
                            stockRate =
                                if (CommonUtils.isNumeric(it[0].hq[i][4].replace("%", ""))) {
                                    it[0].hq[i][4].replace("%", "").toFloat()
                                } else
                                    0.0f
                            stockPrice = null
                            openPrice = it[0].hq[i][1]
                            swing = it[0].hq[i][3]
                            nowPrice = it[0].hq[i][2]
                            stockPrice = nowPrice!!
                            todayMax = it[0].hq[i][6]
                            todayMin = it[0].hq[i][5]
                            tradeNum = (it[0].hq[i][7].toFloat() * 100).toInt().toString()
                            tradeAmount = (it[0].hq[i][8].toFloat() * 10000).toInt().toString()
                            turnoverRate =
                                if (CommonUtils.isNumeric(it[0].hq[i][9].replace("%", ""))) {
                                    it[0].hq[i][9].replace("%", "").toFloat()
                                } else
                                    0.0f
                            pinyin = BaseApplication.instance?.stocks!![index].pinyin!!
                            loss = stockRate!! < 0
                            market = BaseApplication.instance?.stocks!![index].market!!
                            dateTime = it[0].hq[i][0]
                        })
                    }
                }

                historyNetwork.addAll(it[0].hq)

                page += 1
                page == 1000
            }
            .filter {
                if (!it) {
                    syncHistoryStockInfo()
                }
                it
            }
            .doOnNext {
                if (stockitemModels.size != 0) {
                    stockitemModels.saveAll()
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                println("------------------>Index : $index -----Stock Size : ${stockitemModels.size}-----Code Size : ${BaseApplication.instance?.stocks!![index].code}")
                index += 1
                page = 0
                kLines.clear()
                historyNetwork.clear()
                stockHistories.clear()
                _HistoryProgress.value = Event(index.toFloat())
                syncHistoryStockInfo()
            }, {
                println("------------------>Error : ${it.message}")
                if (it.message!!.contains("timeout")) {
                    syncHistoryStockInfo()
                } else {

                    if (stockitemModels.size != 0) {
                        stockitemModels.saveAll()
                    }

                    println("------------------>Index : $index -----Stock Size : ${stockitemModels.size}-----Code : ${BaseApplication.instance?.stocks!![index].code}")
                    index += 1
                    page = 0

                    kLines.clear()
                    stockitemModels.clear()
                    loadIndex.set("更新数据中...$index / " + BaseApplication.instance?.stocks?.size)
                    _HistoryProgress.postValue(Event(index.toFloat()))
                    syncHistoryStockInfo()
                }
            })
    }

    var stockItemAllArray = ArrayList<ArrayList<StockItemModel>>()
    var stockDayModels = ArrayList<ArrayList<StockDayModel>>()
    var stockItems = ArrayList<StockItemModel>()
    var stockRecentItems = ArrayList<StockItemRecentModel>()

    @SuppressLint("CheckResult")
    fun initStockItemCalculate(count: Int = 0) {

        stockDayModels.clear()
        stockItemAllArray.clear()

        Flowable.just("")
            .subscribeOn(Schedulers.newThread())
            .doOnNext {
                for (page in count..BaseApplication.instance?.stocks?.size!!) {
                    stockItems.clear()
                    stockItems =
                        LitePalDBase.queryStockItemByCode(BaseApplication.instance?.stocks!![page].code!!)
                    if (stockItems.size > 0) {
                        val stockItemModels =
                            ModelConversionUtils.stockItemCalculate(stockItems)

                        stockItemModels.saveAll()
                    }

                    _CalculateProgress.postValue(Event(page.toFloat()))
                    calculateIndex.set("计算数据中...$page / " + BaseApplication.instance?.stocks?.size)

                    println(
                        "------------------>Code : " + BaseApplication.instance?.stocks!![page].code +
                                "------${LitePal.where(
                                    "stockCode = ?",
                                    BaseApplication.instance?.stocks!![page].code
                                ).count(StockItemModel::class.java)}"
                    )
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
    fun copyRecentData(count: Int = 0) {
        stockDayModels.clear()
        stockItemAllArray.clear()

        Flowable.just("")
            .subscribeOn(Schedulers.newThread())
            .doOnNext {

                for (page in count..BaseApplication.instance?.stocks?.size!!) {
                    stockItems.clear()
                    stockItems =
                        LitePalDBase.queryStockItemByCodeLimit(
                            BaseApplication.instance?.stocks!![page].code!!,
                            200
                        )
                    if (stockItems.size > 0) {
                        LitePalDBase.deleteStockItemByCodeAllRecent(BaseApplication.instance?.stocks!![page].code!!,
                            ModelConversionUtils.stockItemToRecent(stockItems))
                    }

                    _CalculateProgress.postValue(Event(page.toFloat()))
                    calculateIndex.set("计算数据中...$page / " + BaseApplication.instance?.stocks?.size)

//                    println("------------------>Code : " + BaseApplication.instance?.stocks!![page].code + "   ---   " + LitePal.count(StockItemRecentModel::class.java))

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
    fun calculateRecentStockItem(count: Int = 0) {

        stockDayModels.clear()
        stockItemAllArray.clear()

        Flowable.just("")
            .subscribeOn(Schedulers.newThread())
            .map {
                val todayModel = ArrayList<StockItemTodayModel>().apply {
                    addAll(LitePal.findAll(StockItemTodayModel::class.java))
                }
                LitePalDBase.updateStockItemTodayToRecent(todayModel)

                if (todayModel.size > 0) {
                    LitePal.deleteAll(StockItemCalculateModel::class.java)
                    todayModel[0].dateTime!!
                }else
                    "1000-01-01"
            }
            .doOnNext {
                for (page in 0 until BaseApplication.instance?.stocks?.size!!) {
                    stockRecentItems.clear()
                    stockRecentItems =
                        LitePalDBase.queryStockItemByCodeAllRecent(BaseApplication.instance?.stocks!![page].code!!)
                    if (stockRecentItems.size > 0) {
                        val stockItemModels =
                            ModelConversionUtils.stockRecentItemCalculate(stockRecentItems)

                        if (stockItemModels.size == 201) {
                            val lastItem = stockItemModels[200]
                            LitePalDBase.deleteStockItemByCodeRecentLast(lastItem.stockCode!!, lastItem.dateTime!!)
                        }

                        if (stockItemModels.size > 60){
                            ModelConversionUtils.stockItemRecentToCalculateModel(ArrayList<StockItemRecentModel>().apply {
                                addAll(stockItemModels.subList(0, 60))
                            }).saveAll()
                        }

                        if (stockItemModels.size > 0)
                            stockItemModels[0].save()
                    }

                    _CalculateProgress.postValue(Event(page.toFloat()))
                    calculateIndex.set("计算数据中...$page / " + BaseApplication.instance?.stocks?.size)

//                    println(
//                        "------------------>Code : " + BaseApplication.instance?.stocks!![page].code +
//                                "------${LitePal.where(
//                                    "stockCode = ?",
//                                    BaseApplication.instance?.stocks!![page].code
//                                ).count(StockItemRecentModel::class.java)}------" +
//                                "${LitePal.where(
//                                    "stockCode = ?",
//                                    BaseApplication.instance?.stocks!![page].code
//                                ).count(StockItemCalculateModel::class.java)}"
//                    )
                }

                LitePalDBase.updateStockItem(it)
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
}