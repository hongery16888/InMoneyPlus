package com.magic.inmoney.adapter

import android.content.Context
import android.content.Intent
import com.magic.inmoney.R
import com.magic.inmoney.activity.KLineActivity
import com.magic.inmoney.base.BaseApplication
import com.magic.inmoney.base.BaseBindingAdapter
import com.magic.inmoney.const.StockItemType
import com.magic.inmoney.databinding.AllStockItemBinding
import com.magic.inmoney.databinding.FavoriteStockItemBinding
import com.magic.inmoney.model.StockFavoriteModel
import com.magic.inmoney.model.StockItemModel
import com.magic.inmoney.orm.LitePalDBase
import java.text.DecimalFormat

class FavoriteStockListAdapter(private val mContext: Context) : BaseBindingAdapter<StockFavoriteModel, FavoriteStockItemBinding>(mContext) {

    private var decimalFormat: DecimalFormat = DecimalFormat("#0.00")

    override fun getLayoutResId(viewType: Int): Int {
        return R.layout.favorite_stock_item
    }

    override fun onBindItem(binding: FavoriteStockItemBinding?, item: StockFavoriteModel, position: Int) {
        binding?.item = item

        binding?.nextMaxPriceRate = decimalFormat.format((item.stockNextDayMaxPrice!! - item.stockAddPrice!!) / item.stockAddPrice!! * 100).toFloat()
        binding?.nextNowPriceRate = decimalFormat.format((item.stockNextDayNowPrice!! - item.stockAddPrice!!) / item.stockAddPrice!! * 100).toFloat()
        binding?.nextTotalPriceRate = decimalFormat.format((item.stockNowPrice!! - item.stockAddPrice!!) / item.stockAddPrice!! * 100).toFloat()

        binding?.delete?.setOnClickListener {
            LitePalDBase.deleteFavoriteStockByCode(item.stockCode!!)
            items.remove(item)
        }

        binding?.stockCode?.setOnClickListener {
            mContext.startActivity(Intent(mContext, KLineActivity::class.java).apply {
                BaseApplication.instance?.favoriteItems?.clear()
                BaseApplication.instance?.favoriteItems?.addAll(items)
                putExtra("position", position)
                putExtra("StockCode", item.stockCode)
                putExtra("lastPosition", itemCount)
                putExtra("SockItemType", StockItemType.FavoriteItem.itemType)
            })
        }

        binding?.stockName?.setOnClickListener {
            mContext.startActivity(Intent(mContext, KLineActivity::class.java).apply {
                BaseApplication.instance?.favoriteItems?.clear()
                BaseApplication.instance?.favoriteItems?.addAll(items)
                putExtra("position", position)
                putExtra("StockCode", item.stockCode)
                putExtra("lastPosition", itemCount)
                putExtra("SockItemType", StockItemType.FavoriteItem.itemType)
            })
        }
    }

}