package com.magic.inmoney.activity

import android.annotation.SuppressLint
import android.content.Intent
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.magic.inmoney.R
import com.magic.inmoney.adapter.ImportImageListAdapter
import com.magic.inmoney.adapter.Statistics5DayStockListAdapter
import com.magic.inmoney.adapter.StatisticsTotalStockListAdapter
import com.magic.inmoney.base.BaseActivity
import com.magic.inmoney.base.BaseApplication
import com.magic.inmoney.databinding.ActivityImportImageStockBinding
import com.magic.inmoney.databinding.ActivityStatisticsStockBinding
import com.magic.inmoney.databinding.ActivityStatisticsTotalStockBinding
import com.magic.inmoney.model.StatisticsModel
import com.magic.inmoney.orm.LitePalDBase
import com.magic.inmoney.view.SpecialProgressBarView
import com.magic.inmoney.viewmodel.statistics.ImportImageViewModel
import com.magic.inmoney.viewmodel.statistics.StatisticsTotalViewModel
import com.magic.inmoney.viewmodel.statistics.StatisticsViewModel
import libs.mjn.prettydialog.PrettyDialog
import java.text.DecimalFormat

class ImportImageActivity : BaseActivity<ActivityImportImageStockBinding, ImportImageViewModel>() {

    private lateinit var adapter: ImportImageListAdapter

    override val layoutId: Int
        get() = R.layout.activity_import_image_stock

    override fun createViewModel(): ImportImageViewModel {
        return ImportImageViewModel()
    }

    override fun initView() {
        binding?.lifecycleOwner = this
        binding?.viewModel = viewModel

        binding?.statisticsStockRecyclerView?.layoutManager = LinearLayoutManager(this)
        adapter = ImportImageListAdapter(this)
        binding?.statisticsStockRecyclerView?.adapter = adapter

        resetData()
    }

    override fun setListener() {

    }

    private fun resetData() {
        adapter.items.addAll(LitePalDBase.queryKeyStockItemForImport())
        if (adapter.items.size == 0) {
            toast("暂时莫有统计数据，请更新")
        }
        viewModel?.emptyStatus?.set(adapter.items.size == 0)
        viewModel?.loadingStatus()
    }
}