package com.magic.inmoney.activity

import android.annotation.SuppressLint
import android.content.Intent
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.magic.inmoney.R
import com.magic.inmoney.adapter.Statistics5DayStockListAdapter
import com.magic.inmoney.base.BaseActivity
import com.magic.inmoney.base.BaseApplication
import com.magic.inmoney.databinding.ActivityStatisticsStockBinding
import com.magic.inmoney.model.StatisticsModel
import com.magic.inmoney.orm.LitePalDBase
import com.magic.inmoney.view.SpecialProgressBarView
import com.magic.inmoney.viewmodel.statistics.StatisticsViewModel
import libs.mjn.prettydialog.PrettyDialog
import org.litepal.LitePal
import java.text.DecimalFormat

class StatisticsActivity : BaseActivity<ActivityStatisticsStockBinding, StatisticsViewModel>() {

    private lateinit var adapter: Statistics5DayStockListAdapter
    private var promptDialog: PrettyDialog? = null
    private var decimalFormat: DecimalFormat = DecimalFormat("#0.00")

    override val layoutId: Int
        get() = R.layout.activity_statistics_stock

    override fun createViewModel(): StatisticsViewModel {
        return StatisticsViewModel()
    }

    override fun initView() {
        binding?.lifecycleOwner = this
        binding?.viewModel = viewModel

        binding?.statisticsStockRecyclerView?.layoutManager = LinearLayoutManager(this)
        adapter = Statistics5DayStockListAdapter(this)
        binding?.statisticsStockRecyclerView?.adapter = adapter

        binding?.loadingProgress?.max = BaseApplication.instance?.stockItems?.size!!
        println("------------------>" + BaseApplication.instance?.filterOptions?.buyPoint)
        resetData()
    }

    override fun setListener() {
        viewModel?.loadingStatus?.observe(this, Observer {
            it.getContentIfNotHandled()?.let {
                binding?.refreshLayout?.finishRefresh()
                binding?.refreshLayout?.finishLoadMore()
            }
        })

        viewModel?.statisticsStockData?.observe(this, Observer {
            it.getContentIfNotHandled()?.let { model ->
                resetData(model)
            }
        })

        viewModel?.loadingProgress?.observe(this, Observer {
            it.getContentIfNotHandled()?.let { progress ->
                binding?.loadingProgress?.progress = progress
            }
        })

        binding?.loadingProgress?.setOnAnimationEndListener {
            binding?.loadingProgress?.max = BaseApplication.instance?.stockItems?.size!!
        }

        binding?.loadingProgress?.setOntextChangeListener(object :
            SpecialProgressBarView.OntextChangeListener {
            override fun onProgressTextChange(
                specialProgressBarView: SpecialProgressBarView?,
                max: Int,
                progress: Int
            ): String {
                return (progress * 100 / max).toString() + "%"
            }

            override fun onErrorTextChange(
                specialProgressBarView: SpecialProgressBarView?,
                max: Int,
                progress: Int
            ): String {
                return "error"
            }

            override fun onSuccessTextChange(
                specialProgressBarView: SpecialProgressBarView?,
                max: Int,
                progress: Int
            ): String {
                return "done"
            }
        })

        binding?.update?.setOnClickListener {
            if (promptDialog == null) {
                promptDialog = PrettyDialog(this)
                    .setIcon(R.drawable.ic_favorite_dialog)
                    .setTitle("更新突破5日均线反抽数据统计")
                    .setMessage("再次确认是否需要更新！")
                    .addButton("确定更新", R.color.confirm_text, R.color.red) {
                        LitePalDBase.deleteStatisticsStockItem()
                        adapter.items.clear()
                        viewModel?.updateStatisticsData()
                        binding?.loadingProgress?.beginStarting()
                        promptDialog?.dismiss()
                    }
            }

            promptDialog?.show()
        }

        binding?.more?.setOnClickListener {
            startActivity(Intent(this, MoreStatisticsActivity::class.java))
        }

        binding?.totalInfo?.setOnClickListener {

            PrettyDialog(this)
                .setIcon(R.drawable.ic_favorite_dialog)
                .setTitle("总统计")
                .setMessage(formatTotalStatistics())
                .show()
        }
    }

    private fun resetData() {
        adapter.items.addAll(LitePalDBase.queryStatisticsStockItem())
        setTitle()
        if (adapter.items.size == 0) {
            toast("暂时莫有统计数据，请更新")
        }
        viewModel?.emptyStatus?.set(adapter.items.size == 0)
        viewModel?.loadingStatus()
    }

    private fun resetData(item: StatisticsModel) {
        adapter.items.add(item)
        setTitle()
        if (adapter.items.size == 0) {
            toast("暂时莫有统计数据，请更新")
        }
        viewModel?.emptyStatus?.set(adapter.items.size == 0)
    }

    @SuppressLint("SetTextI18n")
    private fun setTitle() {
        binding?.title?.text = "突破5日均线回调统计 (" + adapter.items.size + ")"
    }

    private fun formatTotalStatistics(): String {
        val statisticsModel = StatisticsModel().apply {
            adapter.items.forEach {
                successHighCount += it.successHighCount
                successCloseCount += it.successCloseCount
                success1PointCount += it.success1PointCount
                success3PointCount += it.success3PointCount
                successCloseProfit += it.successCloseProfit
                failureCloseProfit += it.failureCloseProfit
                failureHighCount += it.failureHighCount
                failureCloseCount += it.failureCloseCount
                failure1PointCount += it.failure1PointCount
                failure3PointCount += it.failure3PointCount
                keyCount += it.keyCount
                maxProfit += it.maxProfit
                minProfit += it.minProfit
            }
        }

        return "总达标突然反抽次数：${statisticsModel.keyCount}\n\n" +
                "总创新高次数：${statisticsModel.successHighCount}\n\n" +
                "总收盘获利次数：${statisticsModel.successCloseCount}\n\n" +
                "总盈利1%次数：${statisticsModel.success1PointCount}\n\n" +
                "总盈利3%次数：${statisticsModel.success3PointCount}\n\n" +
                "总创新高失败次数：${statisticsModel.failureHighCount}\n\n" +
                "总收盘亏损次数：${statisticsModel.failureCloseCount}\n\n" +
                "总亏损1%次数：${statisticsModel.failure1PointCount}\n\n" +
                "总亏损3%次数：${statisticsModel.failure3PointCount}\n\n" +
                "收盘总盈利百分比：${decimalFormat.format(statisticsModel.successCloseProfit)}\n\n" +
                "收盘总亏损百分比：${decimalFormat.format(statisticsModel.failureCloseProfit)}\n\n" +
                "总最大盈利百分比：${decimalFormat.format(statisticsModel.maxProfit).toFloat()}%\n\n" +
                "总最大亏损百分比：${decimalFormat.format(statisticsModel.minProfit).toFloat()}%\n\n" +
                "总成功比例：" + decimalFormat.format(statisticsModel.successHighCount.toFloat() / (statisticsModel.successHighCount.toFloat() + statisticsModel.failureHighCount.toFloat()) * 100f)
            .toString() + "%\n\n" +
                "总1%成功比例：" + decimalFormat.format(statisticsModel.success1PointCount.toFloat() / (statisticsModel.success1PointCount.toFloat() + statisticsModel.failure1PointCount.toFloat()) * 100f)
            .toString() + "%"

    }
}