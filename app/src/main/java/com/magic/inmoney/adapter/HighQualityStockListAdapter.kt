package com.magic.inmoney.adapter

import android.content.Context
import android.content.Intent
import com.magic.inmoney.R
import com.magic.inmoney.activity.KLineActivity
import com.magic.inmoney.base.BaseApplication
import com.magic.inmoney.base.BaseBindingAdapter
import com.magic.inmoney.const.StockItemType
import com.magic.inmoney.databinding.AllStockItemBinding
import com.magic.inmoney.model.StockItemModel
import com.magic.inmoney.model.StockItemTodayModel

class HighQualityStockListAdapter(private val mContext: Context) : BaseBindingAdapter<StockItemTodayModel, AllStockItemBinding>(mContext) {

    override fun getLayoutResId(viewType: Int): Int {
        return R.layout.all_stock_item
    }

    override fun onBindItem(binding: AllStockItemBinding?, item: StockItemTodayModel, position: Int) {
        binding?.item = item
        binding?.position = position.toString()
        binding?.stockItem?.setOnClickListener {
            mContext.startActivity(Intent(mContext, KLineActivity::class.java).apply {
                BaseApplication.instance?.highQualityStockItems?.clear()
                BaseApplication.instance?.highQualityStockItems?.addAll(items)
                putExtra("position", position)
                putExtra("StockCode", item.stockCode)
                putExtra("lastPosition", itemCount)
                putExtra("SockItemType", StockItemType.HighQualityItem.itemType)
            })
        }
    }

}