package com.magic.inmoney.adapter

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.magic.inmoney.R
import com.magic.inmoney.activity.ImportImageActivity
import com.magic.inmoney.activity.KLineActivity
import com.magic.inmoney.activity.KeyStockDetailActivity
import com.magic.inmoney.base.BaseApplication
import com.magic.inmoney.base.BaseBindingAdapter
import com.magic.inmoney.const.StockBuyStatus
import com.magic.inmoney.const.StockItemType
import com.magic.inmoney.databinding.KeyStockItemBinding
import com.magic.inmoney.listener.OnDateSelectListener
import com.magic.inmoney.model.KeyStockModel
import com.magic.inmoney.orm.LitePalDBase
import com.magic.inmoney.view.KeyStockDetailFilterDateDialog
import com.magic.inmoney.view.NumPickerDialog
import libs.mjn.prettydialog.PrettyDialog
import java.text.DecimalFormat

class KeyStockListAdapter(private val mContext: Context) :
    BaseBindingAdapter<KeyStockModel, KeyStockItemBinding>(mContext) {

    private var decimalFormat: DecimalFormat = DecimalFormat("#0.00")
    private lateinit var addDialog: PrettyDialog

    private var filterDateDialog: KeyStockDetailFilterDateDialog? = null

    override fun getLayoutResId(viewType: Int): Int {
        return R.layout.key_stock_item
    }

    override fun onBindItem(binding: KeyStockItemBinding?, item: KeyStockModel, position: Int) {
        binding?.item = item
        binding?.date = item.lastFivePointDateTime?.replace("2020-", "")
        if (item.stockCostPrice == 0f) {
            binding?.currentRate = 0f
            binding?.currentRateStatus = false
        } else {
            val currentRate =
                decimalFormat.format((item.nowPrice!! - item.stockCostPrice!!) / item.stockCostPrice!! * 100f)
                    .toFloat()
            binding?.currentRate = currentRate
            binding?.currentRateStatus = currentRate > 0
        }

        binding?.maxProfit = "0.0%"
        binding?.maxProfitStatus = false
        binding?.stockItem?.setOnClickListener {
            showDialog(item, position)
        }

        binding?.buyStatus?.setBackgroundColor(mContext.resources.getColor(android.R.color.transparent))
        binding?.buyStatus?.setTextColor(mContext.resources.getColor(R.color.black))

        if (item.dateTime == item.lastFivePointDateTime) {
            binding?.currentRate = 0.0f
            binding?.currentRateStatus = false
        }
        when (item.buyStatus) {
            StockBuyStatus.PromptBuy.buyStatus -> binding?.stockItem?.setBackgroundColor(
                mContext.resources.getColor(
                    R.color.prompt_buy_status
                )
            )
            StockBuyStatus.ProOrder.buyStatus -> binding?.stockItem?.setBackgroundColor(
                mContext.resources.getColor(
                    R.color.pro_order_status
                )
            )
            StockBuyStatus.Purchased.buyStatus -> {
                binding?.stockItem?.setBackgroundColor(mContext.resources.getColor(R.color.stock_buy_status))
                binding?.buyStatus?.setBackgroundColor(mContext.resources.getColor(R.color.red))
                binding?.buyStatus?.setTextColor(mContext.resources.getColor(R.color.white))
                binding?.maxProfit =
                    decimalFormat.format((item.todayMax!! - item.stockCostPrice!!) / item.stockCostPrice!! * 100f)
                        .toString() + "%"
                binding?.maxProfitStatus = item.todayMax!! - item.stockCostPrice!! > 0

                if (item.stockCostPrice == 0f) {
                    binding?.currentRate = 0f
                    binding?.currentRateStatus = false
                } else {
                    val currentRate =
                        decimalFormat.format((item.nowPrice!! - item.stockCostPrice!!) / item.stockCostPrice!! * 100f)
                            .toFloat()
                    binding?.currentRate = currentRate
                    binding?.currentRateStatus = currentRate > 0
                }
            }
            StockBuyStatus.ReachTargetPrice.buyStatus -> {
                binding?.stockItem?.setBackgroundColor(mContext.resources.getColor(R.color.stock_buy_status))
                binding?.maxProfit =
                    decimalFormat.format((item.todayMax!! - item.stockCostPrice!!) / item.stockCostPrice!! * 100f)
                        .toString() + "%"
                binding?.maxProfitStatus = item.todayMax!! - item.stockCostPrice!! > 0

                if (item.stockCostPrice == 0f) {
                    binding?.currentRate = 0f
                    binding?.currentRateStatus = false
                } else {
                    val currentRate =
                        decimalFormat.format((item.nowPrice!! - item.stockCostPrice!!) / item.stockCostPrice!! * 100f)
                            .toFloat()
                    binding?.currentRate = currentRate
                    binding?.currentRateStatus = currentRate > 0
                }
            }
            StockBuyStatus.BuyPrice.buyStatus -> {
                binding?.stockItem?.setBackgroundColor(mContext.resources.getColor(R.color.buy_price_status))
                binding?.maxProfit =
                    decimalFormat.format((item.todayMax!! - item.stockCostPrice!!) / item.stockCostPrice!! * 100f)
                        .toString() + "%"
                binding?.maxProfitStatus = item.todayMax!! - item.stockCostPrice!! > 0

                if (item.stockCostPrice == 0f) {
                    binding?.currentRate = 0f
                    binding?.currentRateStatus = false
                } else {
                    val currentRate =
                        decimalFormat.format((item.nowPrice!! - item.nowPrice!!) / item.nowPrice!! * 100f)
                            .toFloat()
                    binding?.currentRate = currentRate
                    binding?.currentRateStatus = currentRate > 0
                }
            }
            else -> {
                binding?.stockItem?.setBackgroundColor(mContext.resources.getColor(R.color.stock_buy_status))
            }
        }
    }

    private fun showDialog(item: KeyStockModel, position: Int) {

        addDialog = PrettyDialog(mContext)
            .setIcon(R.drawable.ic_choose)
            .setTitle("${item.level}星----${item.stockCode}----${item.stockName}")
            .setMessage("选择操作功能")

        if (item.buyStatus == StockBuyStatus.Purchased.buyStatus) {
            addDialog.addButton("计算买卖点位", R.color.confirm_text, R.color.purple) {
                addDialog.dismiss()
                val numPickDialog = NumPickerDialog(mContext)
                numPickDialog.setStockItem(item)
                numPickDialog.show()
            }
        }

        addDialog.addButton("进入实时操盘", R.color.confirm_text, R.color.confirm_button) {
            addDialog.dismiss()
            mContext.startActivity(Intent(mContext, KLineActivity::class.java).apply {
                BaseApplication.instance?.keyStockItems?.clear()
                BaseApplication.instance?.keyStockItems?.addAll(items)
                putExtra("position", position)
                putExtra("StockCode", item.stockCode)
                putExtra("lastPosition", itemCount)
                putExtra("SockItemType", StockItemType.KeyStockItem.itemType)
            })
        }
            .addButton("个股参数详情", R.color.confirm_text, R.color.confirm_button) {
                addDialog.dismiss()
                mContext.startActivity(Intent(mContext, KeyStockDetailActivity::class.java).apply {
                    putExtra("stockCode", items[position].stockCode)
                })
            }
            .addButton("删除个股", R.color.confirm_text, R.color.delete_button) {
                addDialog.dismiss()
                LitePalDBase.deleteKeyStockByCode(items[position].stockCode!!)
                items.remove(item)
            }
            .addButton("删除选择日期（不包含已购买）", R.color.confirm_text, R.color.delete_button) {
                if (filterDateDialog == null) {
                    filterDateDialog =
                        KeyStockDetailFilterDateDialog(mContext).setSelectDateListener(object :
                            OnDateSelectListener {
                            override fun callback(dates: ArrayList<String>) {
                                if (dates.isEmpty()) {
                                    Toast.makeText(mContext, "无效选择，请重新选择", Toast.LENGTH_LONG).show()
                                    return
                                }
                                deleteConfirmKeyStockByDate(dates)
                            }
                        })
                }

                filterDateDialog?.show()
                addDialog.dismiss()
            }
            .addButton("删除全部打击对象", R.color.confirm_text, R.color.delete_button) {
                addDialog.dismiss()
                deleteConfirmKeyStockAllData()
            }
            .addButton("生成导入列表", R.color.confirm_text, R.color.confirm_button) {
                addDialog.dismiss()
                mContext.startActivity(Intent(mContext, ImportImageActivity::class.java))
            }

        addDialog.show()
    }

    private fun deleteConfirmKeyStockByDate(dates: ArrayList<String>) {
        addDialog = PrettyDialog(mContext)
            .setIcon(R.drawable.ic_delete)
            .setTitle("删除选择日期全部数据")
            .setMessage("确定要删除日期为$dates" + "的全部数据吗, 但不会删除已经购买的打击对象")
            .addButton("确定删除", R.color.confirm_text, R.color.delete_button) {
                addDialog.dismiss()
                LitePalDBase.deleteKeyStockByDate(dates)
                val temp = ArrayList<KeyStockModel>().apply { addAll(items) }

                dates.forEach {
                    temp.forEach { item ->
                        if (item.lastFivePointDateTime == it && item.buyStatus != StockBuyStatus.Purchased.buyStatus) {
                            items.remove(item)
                        }
                    }
                }

            }

        addDialog.show()
    }

    private fun deleteConfirmKeyStockAllData() {
        addDialog = PrettyDialog(mContext)
            .setIcon(R.drawable.ic_delete)
            .setTitle("删除全部数据")
            .setMessage("确定要删除全部数据吗, 并包含已经购买的打击对象")
            .addButton("确定删除", R.color.confirm_text, R.color.delete_button) {
                addDialog.dismiss()
                LitePalDBase.deleteKeyStock()
                items.clear()
            }

        addDialog.show()
    }
}