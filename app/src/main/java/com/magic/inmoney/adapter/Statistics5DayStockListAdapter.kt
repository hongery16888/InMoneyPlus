package com.magic.inmoney.adapter

import android.content.Context
import android.content.Intent
import com.magic.inmoney.R
import com.magic.inmoney.activity.KLineActivity
import com.magic.inmoney.base.BaseApplication
import com.magic.inmoney.base.BaseBindingAdapter
import com.magic.inmoney.const.StockItemType
import com.magic.inmoney.databinding.AllStockItemBinding
import com.magic.inmoney.databinding.Statistics5DayLineItemBinding
import com.magic.inmoney.model.StatisticsModel
import com.magic.inmoney.model.StatisticsTotalModel
import com.magic.inmoney.model.StockItemModel
import com.magic.inmoney.orm.LitePalDBase
import libs.mjn.prettydialog.PrettyDialog
import java.text.DecimalFormat
import kotlin.math.abs

class Statistics5DayStockListAdapter(private val mContext: Context) : BaseBindingAdapter<StatisticsModel, Statistics5DayLineItemBinding>(mContext) {

    private var decimalFormat: DecimalFormat = DecimalFormat("#0.0")
    private lateinit var deleteDialog: PrettyDialog

    override fun getLayoutResId(viewType: Int): Int {
        return R.layout.statistics_5_day_line_item
    }

    override fun onBindItem(binding: Statistics5DayLineItemBinding?, item: StatisticsModel, position: Int) {
        binding?.item = item
        binding?.successRate = decimalFormat.format(item.successHighCount.toFloat() / (item.successHighCount.toFloat() + item.failureHighCount.toFloat()) * 100f).toString() + "%"
        binding?.successCloseProfit = decimalFormat.format(item.successCloseProfit).toString() + "%"
        binding?.failureCloseProfit = decimalFormat.format(item.failureCloseProfit).toString() + "%"
        binding?.profitRate = decimalFormat.format(item.successCloseProfit / abs(item.failureCloseProfit)).toString()

        binding?.stockItem?.setOnClickListener {
            showStatisticsDate(item)
        }
    }

    private fun showStatisticsDate(item: StatisticsModel) {
        deleteDialog = PrettyDialog(mContext)
            .setIcon(R.drawable.ic_date)
            .setTitle("成功与失败日期")
            .setMessage("显示${item.stockName}匹配成功与失败日期")
            .addButton("匹配成功日期为：\n${item.successDate}", R.color.confirm_text, R.color.delete_button) {

            }
            .addButton("匹配失败日期为：\n${item.failureDate}", R.color.confirm_text, R.color.delete_button) {

            }

        deleteDialog.show()
    }

}