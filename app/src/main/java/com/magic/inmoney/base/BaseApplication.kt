package com.magic.inmoney.base

import android.app.Application
import com.iflytek.cloud.SpeechConstant
import com.iflytek.cloud.SpeechUtility
import com.magic.inmoney.model.*
import com.magic.inmoney.orm.LitePalDBase
import com.tencent.mmkv.MMKV


open class BaseApplication : Application() {

    var stocks = ArrayList<StockModel>()
    var stockItemTodayModels = ArrayList<StockItemTodayModel>()
    var stockItems = ArrayList<StockItemModel>()
    var highQualityStockItems  = ArrayList<StockItemTodayModel>()
    var favoriteItems = ArrayList<StockFavoriteModel>()
    var keyStockItems = ArrayList<KeyStockModel>()
    var filterOptions: FilterOptions? = null

    override fun onCreate() {
        super.onCreate()

        instance = this

        LitePalDBase.initDB(this)

        MMKV.initialize(this)

        initXF()
    }

    private fun initXF(){
        SpeechUtility.createUtility(
            this, SpeechConstant.APPID + "=5f6c30c3" + "," +
                    SpeechConstant.ENGINE_MODE + "=" + SpeechConstant.MODE_MSC + "," +
                    SpeechConstant.NET_CHECK + "=false"
        )
    }

    companion object {

        var instance: BaseApplication? = null
            private set
    }

}
