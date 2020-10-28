package com.magic.inmoney.activity

import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.magic.inmoney.R
import com.magic.inmoney.adapter.FavoriteStockListAdapter
import com.magic.inmoney.base.BaseActivity
import com.magic.inmoney.databinding.ActivityFavoriteBinding
import com.magic.inmoney.listener.OnDateSelectListener
import com.magic.inmoney.listener.OnKLineSelectListener
import com.magic.inmoney.model.StockFavoriteModel
import com.magic.inmoney.orm.LitePalDBase
import com.magic.inmoney.utilities.SqlFormat
import com.magic.inmoney.view.FilterDateDialog
import com.magic.inmoney.view.FilterKLineDialog
import com.magic.inmoney.viewmodel.favorite.FavoriteViewModel
import libs.mjn.prettydialog.PrettyDialog
import java.text.DecimalFormat


class FavoriteActivity: BaseActivity<ActivityFavoriteBinding, FavoriteViewModel>(){

    private lateinit var adapter: FavoriteStockListAdapter
    private var stockItems = ArrayList<StockFavoriteModel>()
    private var addKLine = ArrayList<String>()
    private var addDates = ArrayList<String>()
    private var decimalFormat: DecimalFormat = DecimalFormat("#0.0")
    private var filterDateDialog: FilterDateDialog? = null
    private var filterKLineDialog: FilterKLineDialog? = null
    private lateinit var infoDialog: PrettyDialog
    private lateinit var resetDialog: PrettyDialog
    private var totalCount = 0
    private var nextDayMaxCountPrice = 0
    private var nextDayNowCountPrice = 0
    private var totalPriceCount = 0

    override val layoutId: Int
        get() = R.layout.activity_favorite

    override fun createViewModel(): FavoriteViewModel {
        return FavoriteViewModel()
    }

    override fun initView() {
        binding?.lifecycleOwner = this

        binding?.filterStockRecyclerView?.layoutManager = LinearLayoutManager(this)
        adapter = FavoriteStockListAdapter(this)
        binding?.filterStockRecyclerView?.adapter = adapter

        stockItems.addAll(LitePalDBase.queryFavoriteStockItem())

        resetData()
    }

    override fun setListener() {
        viewModel?.loadingStatus?.observe(this, Observer {
            it.getContentIfNotHandled()?.let {
                binding?.refreshLayout?.finishRefresh()
                binding?.refreshLayout?.finishLoadMore()
            }
        })

        binding?.refreshLayout?.setOnRefreshListener {
            stockItems.clear()
            stockItems.addAll(LitePalDBase.queryFavoriteStockItem())
        }

        binding?.info?.setOnClickListener {
            resetInfo()
            infoDialog = PrettyDialog(this)
                .setIcon(R.drawable.ic_info)
                .setTitle("自选统计")
                .setMessage("次日最高价盈利数：（$nextDayMaxCountPrice / $totalCount）   |    盈利率 ：" + decimalFormat.format(nextDayMaxCountPrice * 100 / totalCount) + "%"+
                "\n\n次日收盘价盈利数：（$nextDayNowCountPrice / $totalCount）   |    盈利率 ：" + decimalFormat.format(nextDayNowCountPrice * 100 / totalCount) + "%" +
                        "\n\n至加入自选盈利数：（$totalPriceCount / $totalCount）   |    盈利率 ：" + decimalFormat.format(totalPriceCount * 100 / totalCount) + "%\n")
                .addButton("确认", R.color.confirm_text, R.color.confirm_button){
                    infoDialog.dismiss()
                }
            infoDialog.show()
        }

        binding?.kLine?.setOnClickListener {
            if (filterKLineDialog == null){
                filterKLineDialog = FilterKLineDialog(this).setSelectKLineListener(object : OnKLineSelectListener {
                    override fun callback(klines: ArrayList<String>) {
                        addKLine.clear()
                        addKLine.addAll(klines)
                        stockItems.clear()
                        stockItems.addAll(LitePalDBase.queryFavoriteStockItem(SqlFormat.sqlFormatKLineAndDate(addKLine, addDates)))
                        resetData()
                    }
                })
            }

            filterKLineDialog?.show()
        }

        binding?.calendar?.setOnClickListener {
            if (filterDateDialog == null){
                filterDateDialog = FilterDateDialog(this).setSelectDateListener(object : OnDateSelectListener{
                    override fun callback(dates: ArrayList<String>) {
                        addDates.clear()
                        addDates.addAll(dates)
                        SqlFormat.sqlFormatKLineAndDate(addKLine, addDates)
                        stockItems.clear()
                        stockItems.addAll(LitePalDBase.queryFavoriteStockItem(SqlFormat.sqlFormatKLineAndDate(addKLine, addDates)))
                        resetData()
                    }
                })
            }

            filterDateDialog?.show()
        }

        binding?.reset?.setOnClickListener {
            resetDialog = PrettyDialog(this)
                .setIcon(R.drawable.ic_reset)
                .setTitle("重置筛选条件或清空数据")
                .setMessage("重置K线类型与日期条件或清空对应K线类型或对应日期的数据")
                .addButton("重置K线类型与日期条件", R.color.confirm_text, R.color.confirm_button){
                    stockItems.clear()
                    stockItems.addAll(LitePalDBase.queryFavoriteStockItem())
                    resetData()
                    resetDialog.dismiss()
                }.addButton("清空数据", R.color.confirm_text, R.color.confirm_button){
                    stockItems.clear()
                    LitePalDBase.deleteFavoriteStockItem()
                    resetData()
                    resetDialog.dismiss()
                }

            resetDialog.show()
        }
    }

    private fun resetData(){
        adapter.items.clear()
        adapter.items.addAll(stockItems)

        if(stockItems.size == 0){
            toast("没有自选股票")
        }
    }

    private fun resetInfo(){
        nextDayMaxCountPrice = 0
        nextDayNowCountPrice = 0
        totalPriceCount = 0
        totalCount = adapter.items.size
        for (stock in stockItems){
            if (stock.stockNextDayMaxPrice!! - stock.stockAddPrice!! > 0){
                nextDayMaxCountPrice += 1
            }
            if (stock.stockNextDayNowPrice!! - stock.stockAddPrice!! > 0){
                nextDayNowCountPrice += 1
            }
            if (stock.stockNowPrice!! - stock.stockAddPrice!! > 0){
                totalPriceCount += 1
            }
        }
    }

}