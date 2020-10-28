package com.magic.inmoney.activity

import android.annotation.SuppressLint
import android.content.Intent
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.magic.inmoney.R
import com.magic.inmoney.adapter.Statistics5DayStockListAdapter
import com.magic.inmoney.adapter.StatisticsTotalStockListAdapter
import com.magic.inmoney.base.BaseActivity
import com.magic.inmoney.base.BaseApplication
import com.magic.inmoney.databinding.ActivityStatisticsStockBinding
import com.magic.inmoney.databinding.ActivityStatisticsTotalStockBinding
import com.magic.inmoney.model.StatisticsModel
import com.magic.inmoney.orm.LitePalDBase
import com.magic.inmoney.view.SpecialProgressBarView
import com.magic.inmoney.viewmodel.statistics.StatisticsTotalViewModel
import com.magic.inmoney.viewmodel.statistics.StatisticsViewModel
import libs.mjn.prettydialog.PrettyDialog
import java.text.DecimalFormat

class StatisticsTotalActivity : BaseActivity<ActivityStatisticsTotalStockBinding, StatisticsTotalViewModel>() {

    private lateinit var adapter: StatisticsTotalStockListAdapter

    override val layoutId: Int
        get() = R.layout.activity_statistics_total_stock

    override fun createViewModel(): StatisticsTotalViewModel {
        return StatisticsTotalViewModel()
    }

    override fun initView() {
        binding?.lifecycleOwner = this
        binding?.viewModel = viewModel

        binding?.statisticsStockRecyclerView?.layoutManager = LinearLayoutManager(this)
        adapter = StatisticsTotalStockListAdapter(this)
        binding?.statisticsStockRecyclerView?.adapter = adapter

        resetData()
    }

    override fun setListener() {

    }

    private fun resetData() {
        adapter.items.addAll(LitePalDBase.queryStatisticsTotalStockItem())
        if (adapter.items.size == 0) {
            toast("暂时莫有统计数据，请更新")
        }
        viewModel?.emptyStatus?.set(adapter.items.size == 0)
        viewModel?.loadingStatus()
    }
}