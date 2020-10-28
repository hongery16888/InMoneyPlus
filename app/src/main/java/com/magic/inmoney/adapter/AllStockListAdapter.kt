package com.magic.inmoney.adapter

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.magic.inmoney.R
import com.magic.inmoney.activity.KLineActivity
import com.magic.inmoney.base.BaseApplication
import com.magic.inmoney.base.BaseBindingAdapter
import com.magic.inmoney.const.QualityType
import com.magic.inmoney.const.StockItemType
import com.magic.inmoney.databinding.AllStockItemBinding
import com.magic.inmoney.model.StockItemModel
import com.magic.inmoney.model.StockItemTodayModel
import com.magic.inmoney.orm.LitePalDBase
import libs.mjn.prettydialog.PrettyDialog

class AllStockListAdapter(private val mContext: Context) :
    BaseBindingAdapter<StockItemTodayModel, AllStockItemBinding>(mContext) {

    private lateinit var addDialog: PrettyDialog

    override fun getLayoutResId(viewType: Int): Int {
        return R.layout.all_stock_item
    }

    override fun onBindItem(binding: AllStockItemBinding?, item: StockItemTodayModel, position: Int) {
        binding?.item = item
        binding?.position = position.toString()
        binding?.stockItem?.setOnClickListener {
            mContext.startActivity(Intent(mContext, KLineActivity::class.java).apply {
                BaseApplication.instance?.stockItemTodayModels?.clear()
                BaseApplication.instance?.stockItemTodayModels?.addAll(items)
                putExtra("position", position)
                putExtra("StockCode", item.stockCode)
                putExtra("lastPosition", itemCount)
                putExtra("SockItemType", StockItemType.AllStockItem.itemType)
            })
        }
        binding?.stockItem?.setOnLongClickListener {

            showAddDialog(item)

            return@setOnLongClickListener true
        }
    }

    private fun showAddDialog(item: StockItemTodayModel) {
        addDialog = PrettyDialog(mContext)
            .setIcon(R.drawable.ic_favorite_dialog)
            .setTitle("添加重点打击对象")
            .setMessage("( ${item.stockCode} : ${item.stockName} )添加到打击对象")
            .addButton("添加到重点打击对象", R.color.confirm_text, R.color.confirm_button) {
                LitePalDBase.addKeyStock(ArrayList<StockItemTodayModel>().apply {
                    add(item)
                }, QualityType.Debug)
                Toast.makeText(mContext, "已添加到重点打击对象里", Toast.LENGTH_LONG).show()
                addDialog.dismiss()
            }
//            .addButton("删除${item.stockName}股票", R.color.confirm_text, R.color.confirm_button) {
//                LitePalDBase.deleteStockItems(item.stockCode!!)
//                BaseApplication.instance?.stockItemTodayModels?.clear()
//                BaseApplication.instance?.stockItemTodayModels?.addAll(items)
//                items.remove(item)
//                Toast.makeText(mContext, "删除成功", Toast.LENGTH_LONG).show()
//                addDialog.dismiss()
//            }

        addDialog.show()
    }
}