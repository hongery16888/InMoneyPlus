package com.magic.inmoney.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.view.View
import android.widget.TextView
import androidx.lifecycle.Observer
import com.github.fujianlian.klinechart.KLineChartAdapter
import com.github.fujianlian.klinechart.KLineEntity
import com.github.fujianlian.klinechart.draw.Status
import com.github.fujianlian.klinechart.formatter.DateFormatter
import com.magic.inmoney.R
import com.magic.inmoney.activity.KLineActivity
import com.magic.inmoney.base.BaseActivity
import com.magic.inmoney.base.BaseApplication
import com.magic.inmoney.base.BaseFragment
import com.magic.inmoney.const.StockItemType
import com.magic.inmoney.databinding.ActivityKLineBinding
import com.magic.inmoney.databinding.FragmentKLineBinding
import com.magic.inmoney.model.StockBaseInfo
import com.magic.inmoney.model.StockItemModel
import com.magic.inmoney.model.StockItemTodayModel
import com.magic.inmoney.model.StockReport
import com.magic.inmoney.orm.LitePalDBase
import com.magic.inmoney.utilities.TestUtils
import com.magic.inmoney.viewmodel.kline.KLineViewModel
import java.text.DecimalFormat
import java.util.ArrayList

class KLineFragment: BaseFragment<FragmentKLineBinding, KLineViewModel>() {

    private val adapter by lazy { KLineChartAdapter() }
    private var position = 0
    private var stockCode = ""
    private var lastPosition = 0
    private var decimalFormat: DecimalFormat = DecimalFormat("#0.0")
    private var decimalFormat2: DecimalFormat = DecimalFormat("#0.00")
    private lateinit var stockInfo: StockBaseInfo
    private lateinit var stockReport: StockReport
    private lateinit var stockItemModel: StockItemTodayModel

    private val subTexts: ArrayList<TextView> = ArrayList()
    // 主图指标下标
    private var mainIndex = 0
    // 副图指标下标
    private var subIndex = -1

    override val layoutId: Int
        get() = R.layout.fragment_k_line

    override fun createFragmentViewModel(): KLineViewModel {
        return KLineViewModel()
    }

    @SuppressLint("SetTextI18n")
    override fun initView() {
        subTexts.add(binding?.macdText!!)
        subTexts.add(binding?.kdjText!!)
        subTexts.add(binding?.rsiText!!)
        subTexts.add(binding?.wrText!!)
        binding?.kLineChartView?.adapter = adapter
        binding?.kLineChartView?.dateTimeFormatter = DateFormatter()
        binding?.kLineChartView?.setGridRows(4)
        binding?.kLineChartView?.setGridColumns(4)

        binding?.kLineChartView2?.adapter = adapter
        binding?.kLineChartView2?.dateTimeFormatter = DateFormatter()
        binding?.kLineChartView2?.setGridRows(4)
        binding?.kLineChartView2?.setGridColumns(4)

        position = activity?.intent?.getIntExtra("position", 0)!!
        lastPosition = activity?.intent?.getIntExtra("lastPosition", 0)!!
        stockCode = activity?.intent?.getStringExtra("StockCode")!!

        binding?.kLineChartView?.justShowLoading()

        stockItemModel = LitePalDBase.queryStockItem(stockCode)
        stockInfo = LitePalDBase.queryStockInfo(stockCode)
        stockReport = LitePalDBase.queryStockReport(stockCode)

        binding?.nowPrice?.text = "当前价 : ${stockItemModel.nowPrice}"
        binding?.stockRate?.text = "涨跌幅 : ${stockItemModel.stockRate}%"
        binding?.tradeNum?.text = "成交量: " +decimalFormat2.format(stockItemModel.tradeNum!!.toFloat() / 1000000) + "万手"
        binding?.turnoverRate?.text = "换手率: ${stockItemModel.turnoverRate}%"
        setTradeAmount(stockItemModel.tradeAmount!!.toFloat())

        if (stockInfo.stockName != "-1"){
            binding?.stockPer?.text = "：${stockInfo.per}"
            binding?.stockPbr?.text = "：${stockInfo.pbr}"
            binding?.totalMarketValue?.text = "：${stockInfo.totalMarketValue}"
            binding?.industry?.text = "：${stockInfo.industry}"
        }

        if (stockReport.stockName != "-1"){
            setTextColor(binding?.stockYearGrowth!!, stockReport.onYearGrowthRevenue!!)
            setTextColor(binding?.stockMonthGrowth!!, stockReport.onYearGrowthNetProfit!!)
            setTextColor(binding?.stockRoe!!, stockReport.onNetAssetsRate!!)
            binding?.stockRevenue?.text = "：${stockReport.revenue}"
        }

        if (stockInfo.stockName == "-1" && stockReport.stockName == "-1")
            binding?.reportPlane?.visibility = View.GONE

        viewModel?.loadKLineData(stockCode)

//        when(activity?.intent?.getStringExtra("SockItemType")){
//            StockItemType.AllStockItem.itemType ->{
//                viewModel?.loadKLineData("cn_" + BaseApplication.instance?.stockItems?.get(position)?.stockCode)
//                lastPosition = BaseApplication.instance?.stockItems?.size!!
//                binding?.nowPrice?.text = "当前价 : " + BaseApplication.instance?.stockItems?.get(position)?.nowPrice
//                setColor(binding?.nowPrice!!, BaseApplication.instance?.stockItems?.get(position)?.stockRate!!)
//                binding?.stockRate?.text = "涨跌幅 : " + BaseApplication.instance?.stockItems?.get(position)?.stockRate + "%"
//                setColor(binding?.stockRate!!, BaseApplication.instance?.stockItems?.get(position)?.stockRate!!)
//                binding?.tradeNum?.text = "成交量: " + (BaseApplication.instance?.stockItems?.get(position)?.tradeNum?.toLong()!! / 1000000) + "万手"
//                binding?.turnoverRate?.text = "换手率: " + BaseApplication.instance?.stockItems?.get(position)?.turnoverRate + "%"
//                setTradeAmount(BaseApplication.instance?.stockItems?.get(position)?.tradeAmount!!.toFloat())
//                stockInfo = LitePalDBase.queryStockInfo(BaseApplication.instance?.stockItems?.get(position)?.stockCode!!)!!
//                stockReport = LitePalDBase.queryStockReport(BaseApplication.instance?.stockItems?.get(position)?.stockCode!!)!!
//            }
//
//            StockItemType.HighQualityItem.itemType ->{
//                viewModel?.loadKLineData("cn_" + BaseApplication.instance?.highQualityStockItems?.get(position)?.stockCode)
//                lastPosition = BaseApplication.instance?.highQualityStockItems?.size!!
//
//                binding?.nowPrice?.text = "当前价 : " + BaseApplication.instance?.highQualityStockItems?.get(position)?.nowPrice
//                setColor(binding?.nowPrice!!, BaseApplication.instance?.highQualityStockItems?.get(position)?.stockRate!!)
//                binding?.stockRate?.text = "涨跌幅 : " + BaseApplication.instance?.highQualityStockItems?.get(position)?.stockRate + "%"
//                setColor(binding?.stockRate!!, BaseApplication.instance?.highQualityStockItems?.get(position)?.stockRate!!)
//                binding?.tradeNum?.text = "成交量: " + (BaseApplication.instance?.highQualityStockItems?.get(position)?.tradeNum?.toLong()!! / 1000000) + "万手"
//                binding?.turnoverRate?.text = "换手率: " + BaseApplication.instance?.highQualityStockItems?.get(position)?.turnoverRate + "%"
//                setTradeAmount(BaseApplication.instance?.highQualityStockItems?.get(position)?.tradeAmount!!.toFloat())
//                stockInfo = LitePalDBase.queryStockInfo(BaseApplication.instance?.highQualityStockItems?.get(position)?.stockCode!!)!!
//                stockReport = LitePalDBase.queryStockReport(BaseApplication.instance?.highQualityStockItems?.get(position)?.stockCode!!)!!
//            }
//
//            StockItemType.FavoriteItem.itemType ->{
//                viewModel?.loadKLineData("cn_" + BaseApplication.instance?.favoriteItems?.get(position)?.stockCode)
//                lastPosition = BaseApplication.instance?.favoriteItems?.size!!
//
//                binding?.nowPrice?.text = "当前价 : " + BaseApplication.instance?.favoriteItems?.get(position)?.stockNowPrice
//                setColor(binding?.nowPrice!!, BaseApplication.instance?.favoriteItems?.get(position)?.stockRate!!)
//                binding?.stockRate?.text = "涨跌幅 : " + BaseApplication.instance?.favoriteItems?.get(position)?.stockRate + "%"
//                setColor(binding?.stockRate!!, BaseApplication.instance?.favoriteItems?.get(position)?.stockRate!!)
//                binding?.tradeNum?.text = "成交量: " + (BaseApplication.instance?.favoriteItems?.get(position)?.tradeNum?.toLong()!! / 1000000) + "万手"
//                binding?.turnoverRate?.text = "换手率: " + BaseApplication.instance?.favoriteItems?.get(position)?.turnoverRate + "%"
//                setTradeAmount(BaseApplication.instance?.favoriteItems?.get(position)?.tradeAmount!!.toFloat())
//                stockInfo = LitePalDBase.queryStockInfo(BaseApplication.instance?.favoriteItems?.get(position)?.stockCode!!)!!
//                stockReport = LitePalDBase.queryStockReport(BaseApplication.instance?.favoriteItems?.get(position)?.stockCode!!)!!
//            }
//
//            StockItemType.KeyStockItem.itemType ->{
//                viewModel?.loadKLineData("cn_" + BaseApplication.instance?.keyStockItems?.get(position)?.stockCode)
//                lastPosition = BaseApplication.instance?.keyStockItems?.size!!
//
//                binding?.nowPrice?.text = "当前价 : " + BaseApplication.instance?.keyStockItems?.get(position)?.nowPrice
//                setColor(binding?.nowPrice!!, BaseApplication.instance?.keyStockItems?.get(position)?.stockRate!!)
//                binding?.stockRate?.text = "涨跌幅 : " + BaseApplication.instance?.keyStockItems?.get(position)?.stockRate + "%"
//                setColor(binding?.stockRate!!, BaseApplication.instance?.keyStockItems?.get(position)?.stockRate!!)
//                binding?.tradeNum?.text = "成交量: " + (BaseApplication.instance?.keyStockItems?.get(position)?.tradeNum?.toLong()!! / 1000000) + "万手"
//                binding?.turnoverRate?.text = "换手率: " + BaseApplication.instance?.keyStockItems?.get(position)?.turnoverRate + "%"
//                setTradeAmount(BaseApplication.instance?.keyStockItems?.get(position)?.tradeAmount!!.toFloat())
//                stockInfo = LitePalDBase.queryStockInfo(BaseApplication.instance?.keyStockItems?.get(position)?.stockCode!!)!!
//                stockReport = LitePalDBase.queryStockReport(BaseApplication.instance?.keyStockItems?.get(position)?.stockCode!!)!!
//            }
//        }

//        if (stockInfo.stockName != "-1"){
//            binding?.stockPer?.text = "：${stockInfo.per}"
//            binding?.stockPbr?.text = "：${stockInfo.pbr}"
//            binding?.totalMarketValue?.text = "：${stockInfo.totalMarketValue}"
//            binding?.industry?.text = "：${stockInfo.industry}"
//        }
//
//        if (stockReport.stockName != "-1"){
//            setTextColor(binding?.stockYearGrowth!!, stockReport.onYearGrowthRevenue!!)
//            setTextColor(binding?.stockMonthGrowth!!, stockReport.onYearGrowthNetProfit!!)
//            setTextColor(binding?.stockRoe!!, stockReport.onNetAssetsRate!!)
//            binding?.stockRevenue?.text = "：${stockReport.revenue}"
//        }
//
//        if (stockInfo.stockName == "-1" && stockReport.stockName == "-1")
//            binding?.reportPlane?.visibility = View.GONE
    }

    @SuppressLint("SetTextI18n")
    override fun setListener() {
        viewModel?.kLineData?.observe(this, Observer {
            it.getContentIfNotHandled()?.let {data->
                adapter.addFooterData(data)
                adapter.notifyDataSetChanged()
                binding?.kLineChartView?.startAnimation()
                binding?.kLineChartView?.refreshEnd()

                binding?.kLineChartView2?.startAnimation()
                binding?.kLineChartView2?.refreshEnd()

                binding?.macdText?.performClick()

                binding?.kLineChartView2?.hideSelectData()
                binding?.kLineChartView2?.setChildDraw(1)

                TestUtils.judgeFiveSuccessRate(stockInfo.stockCode!!, stockInfo.stockName!!, data)
            }
        })

        viewModel?.stockRate3?.observe(this, Observer {
            it.getContentIfNotHandled()?.let {data->
                setTextColor(binding?.stockRate3!!, decimalFormat2.format(data).toString())
            }
        })

        viewModel?.stockRate6?.observe(this, Observer {
            it.getContentIfNotHandled()?.let {data->
                setTextColor(binding?.stockRate6!!, decimalFormat2.format(data).toString())
            }
        })

        viewModel?.turnoverRate3?.observe(this, Observer {
            it.getContentIfNotHandled()?.let {data->
                binding?.stockTurnoverRate3!!.text = "：${decimalFormat2.format(data)}%"
            }
        })

        viewModel?.turnoverRate6?.observe(this, Observer {
            it.getContentIfNotHandled()?.let {data->
                binding?.stockTurnoverRate6!!.text = "：${decimalFormat2.format(data)}%"
            }
        })

        binding?.maText?.setOnClickListener {
            if (mainIndex != 0) {
                binding?.kLineChartView?.hideSelectData()
                mainIndex = 0
                binding?.maText?.setTextColor(Color.parseColor("#eeb350"))
                binding?.bollText?.setTextColor(Color.WHITE)
                binding?.kLineChartView?.changeMainDrawType(Status.MA)
            }
        }
        binding?.bollText?.setOnClickListener {
            if (mainIndex != 1) {
                binding?.kLineChartView?.hideSelectData()
                mainIndex = 1
                binding?.bollText?.setTextColor(Color.parseColor("#eeb350"))
                binding?.maText?.setTextColor(Color.WHITE)
                binding?.kLineChartView?.changeMainDrawType(Status.BOLL)
            }
        }
        binding?.mainHide?.setOnClickListener {
            if (mainIndex != -1) {
                binding?.kLineChartView?.hideSelectData()
                mainIndex = -1
                binding?.bollText?.setTextColor(Color.WHITE)
                binding?.maText?.setTextColor(Color.WHITE)
                binding?.kLineChartView?.changeMainDrawType(Status.NONE)
            }
        }
        for ((index, text) in subTexts.withIndex()) {
            text.setOnClickListener {
                if (subIndex != index) {
                    binding?.kLineChartView?.hideSelectData()
                    if (subIndex != -1) {
                        subTexts[subIndex].setTextColor(Color.WHITE)
                    }
                    subIndex = index
                    text.setTextColor(Color.parseColor("#eeb350"))
                    binding?.kLineChartView?.setChildDraw(subIndex)
                }
            }
        }
        binding?.subHide?.setOnClickListener {
            if (subIndex != -1) {
                binding?.kLineChartView?.hideSelectData()
                subTexts[subIndex].setTextColor(Color.WHITE)
                subIndex = -1
                binding?.kLineChartView?.hideChildDraw()
            }
        }
        binding?.fenText?.setOnClickListener {
            binding?.kLineChartView?.hideSelectData()
            binding?.fenText?.setTextColor(Color.parseColor("#eeb350"))
            binding?.kText?.setTextColor(Color.WHITE)
            binding?.kLineChartView?.setMainDrawLine(true)
        }
        binding?.kText?.setOnClickListener {
            binding?.kLineChartView?.hideSelectData()
            binding?.kText?.setTextColor(Color.parseColor("#eeb350"))
            binding?.fenText?.setTextColor(Color.WHITE)
            binding?.kLineChartView?.setMainDrawLine(false)
        }
        binding?.previousStep?.setOnClickListener {
            if (position == 0){
                toast("当前已是第一只股票了")
                return@setOnClickListener
            }

            onStepClicked(position - 1)
        }

        binding?.nextStep?.setOnClickListener {
            if (position == lastPosition - 1){
                toast("当然已是最后一只股票了")
                return@setOnClickListener
            }

            onStepClicked(position + 1)
        }

        binding?.stockReport?.setOnCheckedChangeListener { _, isChecked ->

            if (stockInfo.stockName == "-1" && stockReport.stockName == "-1")
                return@setOnCheckedChangeListener

            if (isChecked)
                binding?.reportPlane?.visibility = View.GONE
            else
                binding?.reportPlane?.visibility = View.VISIBLE
        }
    }

    private fun onStepClicked(nowPosition: Int){
        when(activity?.intent?.getStringExtra("SockItemType")){
            StockItemType.AllStockItem.itemType ->{
                startActivity(Intent(context, KLineActivity::class.java).apply {
                    putExtra("position", nowPosition)
                    putExtra("StockCode", BaseApplication.instance?.stockItemTodayModels?.get(nowPosition)?.stockCode)
                    putExtra("lastPosition", BaseApplication.instance?.stockItemTodayModels?.size)
                    putExtra("SockItemType", StockItemType.AllStockItem.itemType)
                })
            }

            StockItemType.HighQualityItem.itemType ->{
                startActivity(Intent(context, KLineActivity::class.java).apply {
                    putExtra("position", nowPosition)
                    putExtra("StockCode", BaseApplication.instance?.highQualityStockItems?.get(nowPosition)?.stockCode)
                    putExtra("lastPosition", BaseApplication.instance?.highQualityStockItems?.size)
                    putExtra("SockItemType", StockItemType.HighQualityItem.itemType)
                })
            }

            StockItemType.FavoriteItem.itemType ->{
                startActivity(Intent(context, KLineActivity::class.java).apply {
                    putExtra("position", nowPosition)
                    putExtra("StockCode", BaseApplication.instance?.favoriteItems?.get(nowPosition)?.stockCode)
                    putExtra("lastPosition", BaseApplication.instance?.favoriteItems?.size)
                    putExtra("SockItemType", StockItemType.FavoriteItem.itemType)
                })
            }

            StockItemType.KeyStockItem.itemType ->{
                startActivity(Intent(context, KLineActivity::class.java).apply {
                    putExtra("position", nowPosition)
                    putExtra("StockCode", BaseApplication.instance?.keyStockItems?.get(nowPosition)?.stockCode)
                    putExtra("lastPosition", BaseApplication.instance?.keyStockItems?.size)
                    putExtra("SockItemType", StockItemType.KeyStockItem.itemType)
                })
            }
        }

        activity?.finish()
    }

    private fun setColor(textView: TextView, data: Float){
        if (data > 0){
            textView.setTextColor(resources.getColor(R.color.loss_true))
        }else
            textView.setTextColor(resources.getColor(R.color.loss_false))
    }

    @SuppressLint("SetTextI18n")
    private fun setTradeAmount(amount : Float){
        if (amount > 10000 * 10000){
            binding?.tradeAmount?.text = "成交金额: " + decimalFormat.format(amount / 100000000) + "亿"
        }else{
            binding?.tradeAmount?.text = "成交金额: " + decimalFormat.format(amount / 10000) + "万"
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setTextColor(textView: TextView, rate: String){
        if (rate.contains("-")) {
            textView.text = "：${rate}%"
            return
        }
        textView.text = "：${decimalFormat2.format(rate.toFloat())}%"

        if (rate.toFloat() > 0f)
            textView.setTextColor(resources.getColor(R.color.chart_red))
        else
            textView.setTextColor(resources.getColor(R.color.chart_green))
    }
}