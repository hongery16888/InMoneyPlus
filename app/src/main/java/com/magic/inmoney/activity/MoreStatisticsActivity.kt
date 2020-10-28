package com.magic.inmoney.activity

import android.annotation.SuppressLint
import android.content.Intent
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.magic.inmoney.R
import com.magic.inmoney.adapter.Statistics5DayStockListAdapter
import com.magic.inmoney.base.BaseActivity
import com.magic.inmoney.base.BaseApplication
import com.magic.inmoney.const.StatisticsType
import com.magic.inmoney.const.ThroughType
import com.magic.inmoney.databinding.ActivityMoreStatisticsStockBinding
import com.magic.inmoney.model.StatisticsModel
import com.magic.inmoney.model.StatisticsTotalModel
import com.magic.inmoney.orm.LitePalDBase
import com.magic.inmoney.view.SpecialProgressBarView
import com.magic.inmoney.viewmodel.statistics.MoreStatisticsViewModel
import libs.mjn.prettydialog.PrettyDialog
import java.text.DecimalFormat
import kotlin.math.abs

class MoreStatisticsActivity :
    BaseActivity<ActivityMoreStatisticsStockBinding, MoreStatisticsViewModel>() {

    private lateinit var adapter: Statistics5DayStockListAdapter
    private var promptDialog: PrettyDialog? = null
    private var decimalFormat: DecimalFormat = DecimalFormat("#0.00")

    override val layoutId: Int
        get() = R.layout.activity_more_statistics_stock

    override fun createViewModel(): MoreStatisticsViewModel {
        return MoreStatisticsViewModel()
    }

    override fun initView() {
        binding?.lifecycleOwner = this
        binding?.viewModel = viewModel

        binding?.statisticsStockRecyclerView?.layoutManager = LinearLayoutManager(this)
        adapter = Statistics5DayStockListAdapter(this)
        binding?.statisticsStockRecyclerView?.adapter = adapter

        binding?.loadingProgress?.max = BaseApplication.instance?.stocks?.size!!

    }

    override fun setListener() {
        binding?.filterBtn?.setOnClickListener {
            startActivity(Intent(this, FilterActivity::class.java))
        }

        binding?.stop?.setOnClickListener {
            stopStatisticsData()
        }

        binding?.needThirdDay?.setOnCheckedChangeListener { _, isChecked ->
            viewModel?.needThirdDay?.set(isChecked)
        }

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
            binding?.loadingProgress?.max = BaseApplication.instance?.stocks?.size!!
        }

        binding?.save?.setOnClickListener {
            if (adapter.itemCount == 0) return@setOnClickListener

            LitePalDBase.updateStatisticsTotalStockItem(StatisticsTotalModel().apply {
                stockLineTypeName = viewModel?.statisticsType?.title
                stockCount = adapter.itemCount
                buyPoint = when(viewModel?.statisticsType?.line){
                    2 -> "上均线购买"
                    1 -> "中均线购买"
                    else -> "下均线购买"
                }
                needThirdDay = if (binding?.needThirdDay?.isChecked!!) "含第三天" else "不含第三天"
                throughType = when(BaseApplication.instance?.filterOptions?.throughType){
                    ThroughType.NormalThrough.type -> ThroughType.NormalThrough.tag
                    ThroughType.HighThrough.type -> ThroughType.HighThrough.tag
                    ThroughType.ThroughAndTrade.type -> ThroughType.ThroughAndTrade.tag
                    ThroughType.HighThroughAndTrade.type -> ThroughType.HighThroughAndTrade.tag
                    else -> ThroughType.NormalThrough.tag
                }
                adapter.items.forEach {
                    closeSuccessCount += it.successCloseCount
                    closeFailureCount += it.failureCloseCount
                    closeProfitRate += decimalFormat.format(it.successCloseProfit).toFloat()
                    closeLossRate += decimalFormat.format(it.failureCloseProfit).toFloat()
                }
            })

            toast("保存到总统计成功！！")
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
            promptDialog = PrettyDialog(this)
                .setIcon(R.drawable.ic_favorite_dialog)
                .setTitle("更多数据统计")
                .setMessage("请选择数据统计类型")
                .addButton(
                    "加载本地统计数据",
                    R.color.confirm_text,
                    R.color.red
                ) {
                    promptDialog?.dismiss()
                    loadStatisticsDialog()
                }
                .addButton(
                    "重新计算统计数据",
                    R.color.confirm_text,
                    R.color.red
                ) {
                    promptDialog?.dismiss()
                    resetStatisticsDialog()
                }
                .addButton(
                    "清空全部统计数据",
                    R.color.confirm_text,
                    R.color.red
                ) {
                    promptDialog?.dismiss()
                    clearStatisticsDialog()
                }

            promptDialog?.show()
        }

        binding?.totalInfo?.setOnClickListener {

            PrettyDialog(this)
                .setIcon(R.drawable.ic_favorite_dialog)
                .setTitle("总统计")
                .setMessage(formatTotalStatistics())
                .addButton(
                    "总统计对比详情",
                    R.color.confirm_text,
                    R.color.red
                ) {
                    startActivity(Intent(this, StatisticsTotalActivity::class.java))
                }
                .show()
        }
    }

    private fun loadStatisticsDialog() {
        promptDialog = PrettyDialog(this)
            .setIcon(R.drawable.ic_favorite_dialog)
            .setTitle("加载本地统计数据")
            .setMessage("请选择数据统计类型")
            .addButton(StatisticsType.StatisticsUpLine.title+ " (" + LitePalDBase.queryStatisticsStockItemCount(StatisticsType.StatisticsUpLine.type) + ")", R.color.confirm_text, R.color.red) {
                viewModel?.statisticsType = StatisticsType.StatisticsUpLine
                resetData(StatisticsType.StatisticsUpLine)
                promptDialog?.dismiss()
            }
            .addButton(StatisticsType.StatisticsMidLine.title+ " (" + LitePalDBase.queryStatisticsStockItemCount(StatisticsType.StatisticsMidLine.type) + ")", R.color.confirm_text, R.color.red) {
                viewModel?.statisticsType = StatisticsType.StatisticsMidLine
                resetData(StatisticsType.StatisticsMidLine)
                promptDialog?.dismiss()
            }
            .addButton(StatisticsType.StatisticsDownLine.title+ " (" + LitePalDBase.queryStatisticsStockItemCount(StatisticsType.StatisticsDownLine.type) + ")", R.color.confirm_text, R.color.red) {
                viewModel?.statisticsType = StatisticsType.StatisticsDownLine
                resetData(StatisticsType.StatisticsDownLine)
                promptDialog?.dismiss()
            }
            .addButton(StatisticsType.StatisticsThreeUpLine.title+ " (" + LitePalDBase.queryStatisticsStockItemCount(StatisticsType.StatisticsThreeUpLine.type) + ")", R.color.confirm_text, R.color.red) {
                viewModel?.statisticsType = StatisticsType.StatisticsThreeUpLine
                resetData(StatisticsType.StatisticsThreeUpLine)
                promptDialog?.dismiss()
            }
            .addButton(StatisticsType.StatisticsThreeMidLine.title+ " (" + LitePalDBase.queryStatisticsStockItemCount(StatisticsType.StatisticsThreeMidLine.type) + ")", R.color.confirm_text, R.color.red) {
                viewModel?.statisticsType = StatisticsType.StatisticsThreeMidLine
                resetData(StatisticsType.StatisticsThreeMidLine)
                promptDialog?.dismiss()
            }
            .addButton(StatisticsType.StatisticsThreeDownLine.title+ " (" + LitePalDBase.queryStatisticsStockItemCount(StatisticsType.StatisticsThreeDownLine.type) + ")", R.color.confirm_text, R.color.red) {
                viewModel?.statisticsType = StatisticsType.StatisticsThreeDownLine
                resetData(StatisticsType.StatisticsThreeDownLine)
                promptDialog?.dismiss()
            }
            .addButton(StatisticsType.StatisticsThreeSortUpLine.title+ " (" + LitePalDBase.queryStatisticsStockItemCount(StatisticsType.StatisticsThreeSortUpLine.type) + ")", R.color.confirm_text, R.color.red) {
                viewModel?.statisticsType = StatisticsType.StatisticsThreeSortUpLine
                resetData(StatisticsType.StatisticsThreeSortUpLine)
                promptDialog?.dismiss()
            }
            .addButton(StatisticsType.StatisticsThreeSortMidLine.title+ " (" + LitePalDBase.queryStatisticsStockItemCount(StatisticsType.StatisticsThreeSortMidLine.type) + ")", R.color.confirm_text, R.color.red) {
                viewModel?.statisticsType = StatisticsType.StatisticsThreeSortMidLine
                resetData(StatisticsType.StatisticsThreeSortMidLine)
                promptDialog?.dismiss()
            }
            .addButton(StatisticsType.StatisticsThreeSortDownLine.title+ " (" + LitePalDBase.queryStatisticsStockItemCount(StatisticsType.StatisticsThreeSortDownLine.type) + ")", R.color.confirm_text, R.color.red) {
                viewModel?.statisticsType = StatisticsType.StatisticsThreeSortDownLine
                resetData(StatisticsType.StatisticsThreeSortDownLine)
                promptDialog?.dismiss()
            }
            .addButton(StatisticsType.StatisticsDoubleDayUpLine.title+ " (" + LitePalDBase.queryStatisticsStockItemCount(StatisticsType.StatisticsDoubleDayUpLine.type) + ")", R.color.confirm_text, R.color.red) {
                viewModel?.statisticsType = StatisticsType.StatisticsDoubleDayUpLine
                resetData(StatisticsType.StatisticsDoubleDayUpLine)
                promptDialog?.dismiss()
            }
            .addButton(StatisticsType.StatisticsHighQualityUpLine.title+ " (" + LitePalDBase.queryStatisticsStockItemCount(StatisticsType.StatisticsHighQualityUpLine.type) + ")", R.color.confirm_text, R.color.red) {
                viewModel?.statisticsType = StatisticsType.StatisticsHighQualityUpLine
                resetData(StatisticsType.StatisticsHighQualityUpLine)
                promptDialog?.dismiss()
            }
            .addButton(StatisticsType.Statistics5813UpLine.title+ " (" + LitePalDBase.queryStatisticsStockItemCount(StatisticsType.Statistics5813UpLine.type) + ")", R.color.confirm_text, R.color.red) {
                viewModel?.statisticsType = StatisticsType.Statistics5813UpLine
                resetData(StatisticsType.Statistics5813UpLine)
                promptDialog?.dismiss()
            }
            .addButton(StatisticsType.Statistics3510UpLine.title+ " (" + LitePalDBase.queryStatisticsStockItemCount(StatisticsType.Statistics3510UpLine.type) + ")", R.color.confirm_text, R.color.red) {
                viewModel?.statisticsType = StatisticsType.Statistics3510UpLine
                resetData(StatisticsType.Statistics3510UpLine)
                promptDialog?.dismiss()
            }
            .addButton(StatisticsType.StatisticsRareMidLine.title+ " (" + LitePalDBase.queryStatisticsStockItemCount(StatisticsType.StatisticsRareMidLine.type) + ")", R.color.confirm_text, R.color.red) {
                viewModel?.statisticsType = StatisticsType.StatisticsRareMidLine
                resetData(StatisticsType.StatisticsRareMidLine)
                promptDialog?.dismiss()
            }
            .addButton(StatisticsType.StatisticsMACD.title+ " (" + LitePalDBase.queryStatisticsStockItemCount(StatisticsType.StatisticsMACD.type) + ")", R.color.confirm_text, R.color.red) {
                viewModel?.statisticsType = StatisticsType.StatisticsMACD
                resetData(StatisticsType.StatisticsMACD)
                promptDialog?.dismiss()
            }
        promptDialog?.show()
    }

    private fun resetStatisticsDialog() {
        promptDialog = PrettyDialog(this)
            .setIcon(R.drawable.ic_favorite_dialog)
            .setTitle("重新计算统计数据")
            .setMessage("请选择数据统计类型")
            .addButton(StatisticsType.StatisticsUpLine.title + " (" + LitePalDBase.queryStatisticsStockItemCount(StatisticsType.StatisticsUpLine.type) + ")", R.color.confirm_text, R.color.red) {
                adapter.items.clear()
                viewModel?.statisticsType = StatisticsType.StatisticsUpLine
                resetData(StatisticsType.StatisticsUpLine)
                viewModel?.updateStatisticsData()
                binding?.loadingProgress?.beginStarting()
                promptDialog?.dismiss()
            }
            .addButton(StatisticsType.StatisticsMidLine.title + " (" + LitePalDBase.queryStatisticsStockItemCount(StatisticsType.StatisticsMidLine.type) + ")", R.color.confirm_text, R.color.red) {
                adapter.items.clear()
                viewModel?.statisticsType = StatisticsType.StatisticsMidLine
                resetData(StatisticsType.StatisticsMidLine)
                viewModel?.updateStatisticsData()
                binding?.loadingProgress?.beginStarting()
                promptDialog?.dismiss()
            }
            .addButton(StatisticsType.StatisticsDownLine.title + " (" + LitePalDBase.queryStatisticsStockItemCount(StatisticsType.StatisticsDownLine.type) + ")", R.color.confirm_text, R.color.red) {
                adapter.items.clear()
                viewModel?.statisticsType = StatisticsType.StatisticsDownLine
                resetData(StatisticsType.StatisticsDownLine)
                viewModel?.updateStatisticsData()
                binding?.loadingProgress?.beginStarting()
                promptDialog?.dismiss()
            }
            .addButton(StatisticsType.StatisticsThreeUpLine.title + " (" + LitePalDBase.queryStatisticsStockItemCount(StatisticsType.StatisticsThreeUpLine.type) + ")", R.color.confirm_text, R.color.red) {
                adapter.items.clear()
                viewModel?.statisticsType = StatisticsType.StatisticsThreeUpLine
                resetData(StatisticsType.StatisticsThreeUpLine)
                viewModel?.updateStatisticsData()
                binding?.loadingProgress?.beginStarting()
                promptDialog?.dismiss()
            }
            .addButton(StatisticsType.StatisticsThreeMidLine.title + " (" + LitePalDBase.queryStatisticsStockItemCount(StatisticsType.StatisticsThreeMidLine.type) + ")", R.color.confirm_text, R.color.red) {
                adapter.items.clear()
                viewModel?.statisticsType = StatisticsType.StatisticsThreeMidLine
                resetData(StatisticsType.StatisticsThreeMidLine)
                viewModel?.updateStatisticsData()
                binding?.loadingProgress?.beginStarting()
                promptDialog?.dismiss()
            }
            .addButton(StatisticsType.StatisticsThreeDownLine.title + " (" + LitePalDBase.queryStatisticsStockItemCount(StatisticsType.StatisticsThreeDownLine.type) + ")", R.color.confirm_text, R.color.red) {
                adapter.items.clear()
                viewModel?.statisticsType = StatisticsType.StatisticsThreeDownLine
                resetData(StatisticsType.StatisticsThreeDownLine)
                viewModel?.updateStatisticsData()
                binding?.loadingProgress?.beginStarting()
                promptDialog?.dismiss()
            }.addButton(StatisticsType.StatisticsThreeSortUpLine.title + " (" + LitePalDBase.queryStatisticsStockItemCount(StatisticsType.StatisticsThreeSortUpLine.type) + ")", R.color.confirm_text, R.color.red) {
                adapter.items.clear()
                viewModel?.statisticsType = StatisticsType.StatisticsThreeSortUpLine
                resetData(StatisticsType.StatisticsThreeSortUpLine)
                viewModel?.updateStatisticsData()
                binding?.loadingProgress?.beginStarting()
                promptDialog?.dismiss()
            }
            .addButton(StatisticsType.StatisticsThreeSortMidLine.title + " (" + LitePalDBase.queryStatisticsStockItemCount(StatisticsType.StatisticsThreeSortMidLine.type) + ")", R.color.confirm_text, R.color.red) {
                adapter.items.clear()
                viewModel?.statisticsType = StatisticsType.StatisticsThreeSortMidLine
                resetData(StatisticsType.StatisticsThreeSortMidLine)
                viewModel?.updateStatisticsData()
                binding?.loadingProgress?.beginStarting()
                promptDialog?.dismiss()
            }
            .addButton(StatisticsType.StatisticsThreeSortDownLine.title + " (" + LitePalDBase.queryStatisticsStockItemCount(StatisticsType.StatisticsThreeSortDownLine.type) + ")", R.color.confirm_text, R.color.red) {
                adapter.items.clear()
                viewModel?.statisticsType = StatisticsType.StatisticsThreeSortDownLine
                resetData(StatisticsType.StatisticsThreeSortDownLine)
                viewModel?.updateStatisticsData()
                binding?.loadingProgress?.beginStarting()
                promptDialog?.dismiss()
            }
            .addButton(StatisticsType.StatisticsDoubleDayUpLine.title + " (" + LitePalDBase.queryStatisticsStockItemCount(StatisticsType.StatisticsDoubleDayUpLine.type) + ")", R.color.confirm_text, R.color.red) {
                adapter.items.clear()
                viewModel?.statisticsType = StatisticsType.StatisticsDoubleDayUpLine
                resetData(StatisticsType.StatisticsDoubleDayUpLine)
                viewModel?.updateStatisticsData()
                binding?.loadingProgress?.beginStarting()
                promptDialog?.dismiss()
            }
            .addButton(StatisticsType.StatisticsHighQualityUpLine.title + " (" + LitePalDBase.queryStatisticsStockItemCount(StatisticsType.StatisticsHighQualityUpLine.type) + ")", R.color.confirm_text, R.color.red) {
                adapter.items.clear()
                viewModel?.statisticsType = StatisticsType.StatisticsHighQualityUpLine
                resetData(StatisticsType.StatisticsHighQualityUpLine)
                viewModel?.updateStatisticsData()
                binding?.loadingProgress?.beginStarting()
                promptDialog?.dismiss()
            }
            .addButton(StatisticsType.Statistics5813UpLine.title + " (" + LitePalDBase.queryStatisticsStockItemCount(StatisticsType.Statistics5813UpLine.type) + ")", R.color.confirm_text, R.color.red) {
                adapter.items.clear()
                viewModel?.statisticsType = StatisticsType.Statistics5813UpLine
                resetData(StatisticsType.Statistics5813UpLine)
                viewModel?.updateStatisticsData()
                binding?.loadingProgress?.beginStarting()
                promptDialog?.dismiss()
            }
            .addButton(StatisticsType.Statistics3510UpLine.title + " (" + LitePalDBase.queryStatisticsStockItemCount(StatisticsType.Statistics3510UpLine.type) + ")", R.color.confirm_text, R.color.red) {
                adapter.items.clear()
                viewModel?.statisticsType = StatisticsType.Statistics3510UpLine
                resetData(StatisticsType.Statistics3510UpLine)
                viewModel?.updateStatisticsData()
                binding?.loadingProgress?.beginStarting()
                promptDialog?.dismiss()
            }
            .addButton(StatisticsType.StatisticsRareMidLine.title + " (" + LitePalDBase.queryStatisticsStockItemCount(StatisticsType.StatisticsRareMidLine.type) + ")", R.color.confirm_text, R.color.red) {
                adapter.items.clear()
                viewModel?.statisticsType = StatisticsType.StatisticsRareMidLine
                resetData(StatisticsType.StatisticsRareMidLine)
                viewModel?.updateStatisticsData()
                binding?.loadingProgress?.beginStarting()
                promptDialog?.dismiss()
            }
            .addButton(StatisticsType.StatisticsMACD.title + " (" + LitePalDBase.queryStatisticsStockItemCount(StatisticsType.StatisticsMACD.type) + ")", R.color.confirm_text, R.color.red) {
                adapter.items.clear()
                viewModel?.statisticsType = StatisticsType.StatisticsMACD
                resetData(StatisticsType.StatisticsMACD)
                viewModel?.updateStatisticsData()
                binding?.loadingProgress?.beginStarting()
                promptDialog?.dismiss()
            }

        promptDialog?.show()
    }

    private fun clearStatisticsDialog() {
        promptDialog = PrettyDialog(this)
            .setIcon(R.drawable.ic_favorite_dialog)
            .setTitle("清除统计数据")
            .setMessage("请选择清除统计数据类型")
            .addButton(StatisticsType.StatisticsUpLine.title + " (" + LitePalDBase.queryStatisticsStockItemCount(StatisticsType.StatisticsUpLine.type) + ")", R.color.confirm_text, R.color.red) {
                adapter.items.clear()
                LitePalDBase.deleteStatisticsStockItem(StatisticsType.StatisticsUpLine.type)
                promptDialog?.dismiss()
            }
            .addButton(StatisticsType.StatisticsMidLine.title + " (" + LitePalDBase.queryStatisticsStockItemCount(StatisticsType.StatisticsMidLine.type) + ")", R.color.confirm_text, R.color.red) {
                adapter.items.clear()
                LitePalDBase.deleteStatisticsStockItem(StatisticsType.StatisticsMidLine.type)
                promptDialog?.dismiss()
            }
            .addButton(StatisticsType.StatisticsDownLine.title + " (" + LitePalDBase.queryStatisticsStockItemCount(StatisticsType.StatisticsDownLine.type) + ")", R.color.confirm_text, R.color.red) {
                adapter.items.clear()
                LitePalDBase.deleteStatisticsStockItem(StatisticsType.StatisticsDownLine.type)
                promptDialog?.dismiss()
            }
            .addButton(StatisticsType.StatisticsThreeUpLine.title + " (" + LitePalDBase.queryStatisticsStockItemCount(StatisticsType.StatisticsThreeUpLine.type) + ")", R.color.confirm_text, R.color.red) {
                adapter.items.clear()
                LitePalDBase.deleteStatisticsStockItem(StatisticsType.StatisticsThreeUpLine.type)
                promptDialog?.dismiss()
            }
            .addButton(StatisticsType.StatisticsThreeMidLine.title + " (" + LitePalDBase.queryStatisticsStockItemCount(StatisticsType.StatisticsThreeMidLine.type) + ")", R.color.confirm_text, R.color.red) {
                adapter.items.clear()
                LitePalDBase.deleteStatisticsStockItem(StatisticsType.StatisticsThreeMidLine.type)
                promptDialog?.dismiss()
            }
            .addButton(StatisticsType.StatisticsThreeDownLine.title + " (" + LitePalDBase.queryStatisticsStockItemCount(StatisticsType.StatisticsThreeDownLine.type) + ")", R.color.confirm_text, R.color.red) {
                adapter.items.clear()
                LitePalDBase.deleteStatisticsStockItem(StatisticsType.StatisticsThreeDownLine.type)
                promptDialog?.dismiss()
            }
            .addButton(StatisticsType.StatisticsThreeSortUpLine.title + " (" + LitePalDBase.queryStatisticsStockItemCount(StatisticsType.StatisticsThreeSortUpLine.type) + ")", R.color.confirm_text, R.color.red) {
                adapter.items.clear()
                LitePalDBase.deleteStatisticsStockItem(StatisticsType.StatisticsThreeSortUpLine.type)
                promptDialog?.dismiss()
            }
            .addButton(StatisticsType.StatisticsThreeSortMidLine.title + " (" + LitePalDBase.queryStatisticsStockItemCount(StatisticsType.StatisticsThreeSortMidLine.type) + ")", R.color.confirm_text, R.color.red) {
                adapter.items.clear()
                LitePalDBase.deleteStatisticsStockItem(StatisticsType.StatisticsThreeSortMidLine.type)
                promptDialog?.dismiss()
            }
            .addButton(StatisticsType.StatisticsThreeSortDownLine.title + " (" + LitePalDBase.queryStatisticsStockItemCount(StatisticsType.StatisticsThreeSortDownLine.type) + ")", R.color.confirm_text, R.color.red) {
                adapter.items.clear()
                LitePalDBase.deleteStatisticsStockItem(StatisticsType.StatisticsThreeSortDownLine.type)
                promptDialog?.dismiss()
            }
            .addButton(StatisticsType.StatisticsDoubleDayUpLine.title + " (" + LitePalDBase.queryStatisticsStockItemCount(StatisticsType.StatisticsDoubleDayUpLine.type) + ")", R.color.confirm_text, R.color.red) {
                adapter.items.clear()
                LitePalDBase.deleteStatisticsStockItem(StatisticsType.StatisticsDoubleDayUpLine.type)
                promptDialog?.dismiss()
            }
            .addButton(StatisticsType.StatisticsHighQualityUpLine.title + " (" + LitePalDBase.queryStatisticsStockItemCount(StatisticsType.StatisticsHighQualityUpLine.type) + ")", R.color.confirm_text, R.color.red) {
                adapter.items.clear()
                LitePalDBase.deleteStatisticsStockItem(StatisticsType.StatisticsHighQualityUpLine.type)
                promptDialog?.dismiss()
            }
            .addButton(StatisticsType.Statistics5813UpLine.title + " (" + LitePalDBase.queryStatisticsStockItemCount(StatisticsType.Statistics5813UpLine.type) + ")", R.color.confirm_text, R.color.red) {
                adapter.items.clear()
                LitePalDBase.deleteStatisticsStockItem(StatisticsType.Statistics5813UpLine.type)
                promptDialog?.dismiss()
            }
            .addButton(StatisticsType.Statistics3510UpLine.title + " (" + LitePalDBase.queryStatisticsStockItemCount(StatisticsType.Statistics3510UpLine.type) + ")", R.color.confirm_text, R.color.red) {
                adapter.items.clear()
                LitePalDBase.deleteStatisticsStockItem(StatisticsType.Statistics3510UpLine.type)
                promptDialog?.dismiss()
            }
            .addButton(StatisticsType.StatisticsRareMidLine.title + " (" + LitePalDBase.queryStatisticsStockItemCount(StatisticsType.StatisticsRareMidLine.type) + ")", R.color.confirm_text, R.color.red) {
                adapter.items.clear()
                LitePalDBase.deleteStatisticsStockItem(StatisticsType.StatisticsRareMidLine.type)
                promptDialog?.dismiss()
            }
            .addButton(StatisticsType.StatisticsMACD.title + " (" + LitePalDBase.queryStatisticsStockItemCount(StatisticsType.StatisticsMACD.type) + ")", R.color.confirm_text, R.color.red) {
                adapter.items.clear()
                LitePalDBase.deleteStatisticsStockItem(StatisticsType.StatisticsMACD.type)
                promptDialog?.dismiss()
            }

        promptDialog?.show()
    }

    private fun stopStatisticsData(){
        promptDialog = PrettyDialog(this)
            .setIcon(R.drawable.ic_stop)
            .setTitle("停止统计数据")
            .setMessage("是否要停止统计数据？")
            .addButton("确定停止统计", R.color.confirm_text, R.color.red) {
                promptDialog?.dismiss()
                viewModel?.stopStatisticsData()
            }

        promptDialog?.show()
    }

    private fun resetData(statisticsType: StatisticsType) {
        adapter.items.clear()
        adapter.items.addAll(LitePalDBase.queryStatisticsStockItem(statisticsType.type))
        setTitle()
        if (adapter.items.size == 0) {
            toast("暂时莫有统计数据，请更新")
        }
        viewModel?.loadingStatus()
        viewModel?.emptyStatus?.set(adapter.items.size == 0)
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
        binding?.title?.text = when (viewModel?.statisticsType!!) {
            StatisticsType.StatisticsUpLine -> StatisticsType.StatisticsUpLine.title + "（" + adapter.items.size + "）"
            StatisticsType.StatisticsMidLine -> StatisticsType.StatisticsMidLine.title + "（" + adapter.items.size + "）"
            StatisticsType.StatisticsDownLine -> StatisticsType.StatisticsDownLine.title + "（" + adapter.items.size + "）"
            StatisticsType.StatisticsThreeUpLine -> StatisticsType.StatisticsThreeUpLine.title + "（" + adapter.items.size + "）"
            StatisticsType.StatisticsThreeMidLine -> StatisticsType.StatisticsThreeMidLine.title + "（" + adapter.items.size + "）"
            StatisticsType.StatisticsThreeDownLine -> StatisticsType.StatisticsThreeDownLine.title + "（" + adapter.items.size + "）"
            StatisticsType.StatisticsThreeSortUpLine -> StatisticsType.StatisticsThreeSortUpLine.title + "（" + adapter.items.size + "）"
            StatisticsType.StatisticsThreeSortMidLine -> StatisticsType.StatisticsThreeSortMidLine.title + "（" + adapter.items.size + "）"
            StatisticsType.StatisticsThreeSortDownLine -> StatisticsType.StatisticsThreeSortDownLine.title + "（" + adapter.items.size + "）"
            StatisticsType.StatisticsDoubleDayUpLine -> StatisticsType.StatisticsDoubleDayUpLine.title + "（" + adapter.items.size + "）"
            StatisticsType.StatisticsHighQualityUpLine -> StatisticsType.StatisticsHighQualityUpLine.title + "（" + adapter.items.size + "）"
            StatisticsType.Statistics5813UpLine -> StatisticsType.Statistics5813UpLine.title + "（" + adapter.items.size + "）"
            StatisticsType.Statistics3510UpLine -> StatisticsType.Statistics3510UpLine.title + "（" + adapter.items.size + "）"
            StatisticsType.StatisticsRareMidLine -> StatisticsType.StatisticsRareMidLine.title + "（" + adapter.items.size + "）"
            StatisticsType.StatisticsMACD -> StatisticsType.StatisticsMACD.title + "（" + adapter.items.size + "）"
        }
    }

    private fun formatTotalStatistics(): String {
        val statisticsModel = StatisticsModel().apply {
            adapter.items.forEach {
                successHighCount += it.successHighCount
                successCloseCount += it.successCloseCount
                success1PointCount += it.success1PointCount
                success2PointCount += it.success2PointCount
                success3PointCount += it.success3PointCount
                success4PointCount += it.success4PointCount
                success5PointCount += it.success5PointCount
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
                "收盘总盈利百分比：${decimalFormat.format(statisticsModel.successCloseProfit)}%\n\n" +
                "收盘总亏损百分比：${decimalFormat.format(statisticsModel.failureCloseProfit)}%\n\n" +
                "收盘量比：${decimalFormat.format(statisticsModel.successCloseCount.toFloat() * 1.00f / statisticsModel.failureCloseCount.toFloat())}\n\n" +
                "收盘利润比重：${decimalFormat.format(statisticsModel.successCloseProfit / abs(statisticsModel.failureCloseProfit))}\n\n" +
                "总最大盈利百分比：${decimalFormat.format(statisticsModel.maxProfit).toFloat()}%\n\n" +
                "总最大亏损百分比：${decimalFormat.format(statisticsModel.minProfit).toFloat()}%\n\n" +
                "总成功比例：" + decimalFormat.format(statisticsModel.successHighCount.toFloat() / statisticsModel.keyCount.toFloat() * 100f)
            .toString() + "%\n\n" +
                "总1%成功比例：" + decimalFormat.format(statisticsModel.success1PointCount.toFloat() / statisticsModel.keyCount.toFloat() * 100f)
            .toString() + "%\n\n" +
                "总2%成功比例：" + decimalFormat.format(statisticsModel.success2PointCount.toFloat() / statisticsModel.keyCount.toFloat() * 100f)
            .toString() + "%\n\n" +
                "总3%成功比例：" + decimalFormat.format(statisticsModel.success3PointCount.toFloat() / statisticsModel.keyCount.toFloat() * 100f)
            .toString() + "%\n\n" +
                "总4%成功比例：" + decimalFormat.format(statisticsModel.success4PointCount.toFloat() / statisticsModel.keyCount.toFloat() * 100f)
            .toString() + "%\n\n" +
                "总5%成功比例：" + decimalFormat.format(statisticsModel.success5PointCount.toFloat() / statisticsModel.keyCount.toFloat() * 100f)
            .toString() + "%\n\n"

    }
}