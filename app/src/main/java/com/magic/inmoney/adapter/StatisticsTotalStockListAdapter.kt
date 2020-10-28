package com.magic.inmoney.adapter

import android.content.Context
import com.magic.inmoney.R
import com.magic.inmoney.base.BaseBindingAdapter
import com.magic.inmoney.databinding.StatisticsTotalStockItemBinding
import com.magic.inmoney.model.StatisticsTotalModel
import com.magic.inmoney.orm.LitePalDBase
import libs.mjn.prettydialog.PrettyDialog
import java.text.DecimalFormat
import kotlin.math.abs

class StatisticsTotalStockListAdapter(private val mContext: Context) : BaseBindingAdapter<StatisticsTotalModel, StatisticsTotalStockItemBinding>(mContext) {

    private var decimalFormat: DecimalFormat = DecimalFormat("#0.00")
    private lateinit var deleteDialog: PrettyDialog

    override fun getLayoutResId(viewType: Int): Int {
        return R.layout.statistics_total_stock_item
    }

    override fun onBindItem(binding: StatisticsTotalStockItemBinding?, item: StatisticsTotalModel, position: Int) {
        binding?.item = item
        binding?.countRate = decimalFormat.format(item.closeSuccessCount * 1.00f / item.closeFailureCount.toFloat()).toString()
        val profitRate = decimalFormat.format(item.closeProfitRate / abs(item.closeLossRate)).toFloat()
        binding?.profitRate = profitRate.toString()
        binding?.star = when{
            profitRate >= 3.1f -> "牛逼Plus"
            profitRate >= 3.0f -> "荣耀王者"
            profitRate >= 2.9f -> "王者"
            profitRate >= 2.8f -> "星耀"
            profitRate >= 2.7f -> "黄金"
            profitRate >= 2.6f -> "十星"
            profitRate >= 2.5f -> "九星"
            profitRate >= 2.4f -> "八星"
            profitRate >= 2.3f -> "七星"
            profitRate >= 2.2f -> "六星"
            profitRate >= 2.1f -> "五星"
            profitRate >= 2.0f -> "四星"
            profitRate >= 1.9f -> "三星"
            profitRate >= 1.8f -> "二星"
            profitRate >= 1.7f -> "一星"
            else -> "零星"
        }

        binding?.mainCardView?.setOnLongClickListener {
            deleteStatisticsTotalData(item)
            return@setOnLongClickListener true
        }
    }

    private fun deleteStatisticsTotalData(item: StatisticsTotalModel) {
        deleteDialog = PrettyDialog(mContext)
            .setIcon(R.drawable.ic_delete)
            .setTitle("删除${item.stockLineTypeName}")
            .setMessage("确定要删除${item.stockLineTypeName}全部数据吗")
            .addButton("确定删除", R.color.confirm_text, R.color.delete_button) {
                deleteDialog.dismiss()
                LitePalDBase.deleteStatisticsTotalStockItem(item)
                items.remove(item)
            }
            .addButton("删除全部总统计数据", R.color.confirm_text, R.color.delete_button) {
                deleteDialog.dismiss()
                LitePalDBase.deleteStatisticsTotalStockAllData()
                items.clear()
            }

        deleteDialog.show()
    }
}