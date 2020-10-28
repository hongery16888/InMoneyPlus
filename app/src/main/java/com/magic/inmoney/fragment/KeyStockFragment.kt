package com.magic.inmoney.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.magic.inmoney.R
import com.magic.inmoney.activity.SettingActivity
import com.magic.inmoney.adapter.KeyStockListAdapter
import com.magic.inmoney.base.BaseApplication
import com.magic.inmoney.base.BaseFragment
import com.magic.inmoney.const.KeyStockPrompt
import com.magic.inmoney.const.StockBuyStatus
import com.magic.inmoney.databinding.FragmentKeyStockBinding
import com.magic.inmoney.model.KeyStockModel
import com.magic.inmoney.orm.LitePalDBase
import com.magic.inmoney.utilities.DateUtils
import com.magic.inmoney.utilities.OfflineVoiceUtils
import com.magic.inmoney.viewmodel.key.KeyStockViewModel
import libs.mjn.prettydialog.PrettyDialog
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList

class KeyStockFragment : BaseFragment<FragmentKeyStockBinding, KeyStockViewModel>() {

    private lateinit var adapter: KeyStockListAdapter
    private var stockItems = ArrayList<KeyStockModel>()
    private var keyStocks = ArrayList<KeyStockModel>()
    private var soldStocks = ArrayList<KeyStockModel>()
    private var buyStocks = ArrayList<KeyStockModel>()
    private var trendStocks = ArrayList<KeyStockModel>()
    private lateinit var addDialog: PrettyDialog
    private var mediaPlayer: MediaPlayer? = null
    private var decimalFormat3: DecimalFormat = DecimalFormat("#0.000")
    private val audios = arrayListOf(
        R.raw.m0,
        R.raw.m1,
        R.raw.m2,
        R.raw.m3,
        R.raw.m4,
        R.raw.m5,
        R.raw.m6,
        R.raw.m7,
        R.raw.m8,
        R.raw.m9,
        R.raw.m10,
        R.raw.m11,
        R.raw.m12,
        R.raw.m13,
        R.raw.m14,
        R.raw.m15
    )
    private var random: Random = Random()
    private var promptTime = 0L

    override val layoutId: Int
        get() = R.layout.fragment_key_stock

    override fun createFragmentViewModel(): KeyStockViewModel {
        return KeyStockViewModel()
    }

    override fun initView() {
        binding?.lifecycleOwner = this
        binding?.viewModel = viewModel

        binding?.keyStockRecyclerView?.layoutManager = LinearLayoutManager(requireContext())
        adapter = KeyStockListAdapter(requireContext())
        binding?.keyStockRecyclerView?.adapter = adapter

        viewModel?.startMonitor()

        Glide.with(requireContext())
            .asGif()
            .load(R.drawable.ic_prompt_button)
            .fitCenter()
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.loading_animation)
                    .error(R.drawable.broken_image)
            )
            .into(binding?.promptBtn!!)

        Glide.with(requireContext())
            .asGif()
            .load(R.drawable.ic_sold)
            .fitCenter()
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.loading_animation)
                    .error(R.drawable.broken_image)
            )
            .into(binding?.soldBtn!!)

        Glide.with(requireContext())
            .asGif()
            .load(R.drawable.ic_buy)
            .fitCenter()
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.loading_animation)
                    .error(R.drawable.broken_image)
            )
            .into(binding?.buyBtn!!)

        Glide.with(requireContext())
            .asGif()
            .load(R.drawable.ic_trend_prompt)
            .fitCenter()
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.loading_animation)
                    .error(R.drawable.broken_image)
            )
            .into(binding?.trendPromptBtn!!)
    }

    override fun setListener() {
        viewModel?.loadingStatus?.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                binding?.refreshLayout?.finishRefresh()
                binding?.refreshLayout?.finishLoadMore()
            }
        })

        viewModel?.keyStockData?.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { model ->
                if (model.size > 0)
                    model.let { data ->
                        stockItems.clear()
                        var temp = ArrayList<KeyStockModel>().apply { addAll(data) }
                        data.let {
                            data.forEach { item ->
                                if (item.buyStatus == StockBuyStatus.Purchased.buyStatus) {
                                    stockItems.add(item)
                                    temp.remove(item)
                                }
                            }
                        }

                        data.let {
                            data.forEach { item ->
                                if (item.buyStatus == StockBuyStatus.BuyPrice.buyStatus) {
                                    stockItems.add(item)
                                    temp.remove(item)
                                }
                            }
                        }

                        data.let {
                            data.forEach { item ->
                                if (item.buyStatus == StockBuyStatus.ProOrder.buyStatus) {
                                    stockItems.add(item)
                                    temp.remove(item)
                                }
                            }
                        }

                        data.let {
                            data.forEach { item ->
                                if (item.buyStatus == StockBuyStatus.PromptBuy.buyStatus) {
                                    stockItems.add(item)
                                    temp.remove(item)
                                }
                            }
                        }

                        stockItems.addAll(temp)
                    } else stockItems.clear()

                resetData()
                LitePalDBase.updateKeyStock(model)
                checkPromptStock()
                viewModel?.isRefresh = false
            }
        })

        binding?.setting?.setOnClickListener {
            startActivity(Intent(requireContext(), SettingActivity::class.java))
        }

        binding?.refreshLayout?.setOnRefreshListener {
            viewModel?.isRefresh = true
        }

        binding?.promptBtn?.setOnClickListener {
            addDialog = PrettyDialog(requireContext())
                .setIcon(R.drawable.ic_prompt)
                .setTitle("可挂单股票")
                .setMessage("挂单均可按推荐价买入\n星级越高优先级越高，10星最高\n蓝色优先于红色")

            keyStocks.forEach {
                var level = 0

                when {
                    it.blockRankRate!! > 2 -> level = it.level.toInt() + 5
                    it.blockRankRate!! > 1 -> level = it.level.toInt() + 3
                    it.blockRankRate!! > 0 -> level = it.level.toInt() + 1
                    it.blockRankRate!! < -2 -> level = it.level.toInt() - 5
                    it.blockRankRate!! < -1 -> level = it.level.toInt() - 3
                    it.blockRankRate!! < 0 -> level = it.level.toInt() - 1
                }
                if (it.buyStatus == StockBuyStatus.PromptBuy.buyStatus) {
                    addDialog.addButton(
                        "${level}星--${it.stockCode}--${it.stockName}--现价 : ${it.nowPrice}\n" +
                                "激进挂单价：${it.buyTargetPrice}\n" +
                                "保守挂单价：${it.secondPromptBuyPrice}\n" +
                                "天真挂单价：${it.naivePromptBuyPrice}",
                        R.color.red,
                        R.color.prompt_buy_status
                    ) {}
                } else {
                    addDialog.addButton(
                        "${level}星--${it.stockCode}--${it.stockName}--现价 : ${it.nowPrice}\n" +
                                "激进挂单价：${it.buyTargetPrice}\n" +
                                "保守挂单价：${it.secondPromptBuyPrice}\n" +
                                "天真挂单价：${it.naivePromptBuyPrice}",
                        R.color.red,
                        R.color.pro_order_status
                    ) {}
                }
            }

            addDialog.show()
        }

        binding?.soldBtn?.setOnClickListener {
            addDialog = PrettyDialog(requireContext())
                .setIcon(R.drawable.ic_sold)
                .setTitle("可挂单卖出股票")
                .setMessage("挂单均可按推荐价卖出")

            soldStocks.forEach {

                var level = 0

                when {
                    it.blockRankRate!! > 2 -> level = it.level.toInt() + 5
                    it.blockRankRate!! > 1 -> level = it.level.toInt() + 3
                    it.blockRankRate!! > 0 -> level = it.level.toInt() + 1
                    it.blockRankRate!! < -2 -> level = it.level.toInt() - 5
                    it.blockRankRate!! < -1 -> level = it.level.toInt() - 3
                    it.blockRankRate!! < 0 -> level = it.level.toInt() - 1
                }

                addDialog.addButton(
                    "${level}星--${it.stockCode}--${it.stockName}\n盈利1%挂单价：${decimalFormat3.format(it.stockCostPrice!! * 1.01f)}\n盈利3%挂单价：${decimalFormat3.format(
                        it.stockCostPrice!! * 1.03f
                    )}",
                    R.color.white,
                    R.color.red
                ) {}
            }

            addDialog.show()
        }

        binding?.buyBtn?.setOnClickListener {
            addDialog = PrettyDialog(requireContext())
                .setIcon(R.drawable.ic_buy)
                .setTitle("可现价购买")
                .setMessage("挂单均可按现价购买")

            buyStocks.forEach {

                var level = 0

                when {
                    it.blockRankRate!! > 2 -> level = it.level.toInt() + 5
                    it.blockRankRate!! > 1 -> level = it.level.toInt() + 3
                    it.blockRankRate!! > 0 -> level = it.level.toInt() + 1
                    it.blockRankRate!! < -2 -> level = it.level.toInt() - 5
                    it.blockRankRate!! < -1 -> level = it.level.toInt() - 3
                    it.blockRankRate!! < 0 -> level = it.level.toInt() - 1
                }

                addDialog.addButton(
                    "${level}星--${it.stockCode}--${it.stockName}\n现在单价：${it.nowPrice!!}",
                    R.color.white,
                    R.color.red
                ) {}
            }

            addDialog.show()
        }

        binding?.trendPromptBtn?.setOnClickListener {
            addDialog = PrettyDialog(requireContext())
                .setIcon(R.drawable.ic_trend_prompt)
                .setTitle("股票趋势价")
                .setMessage("建议趋势价，风险非常大")

            trendStocks.forEach {
                var level = 0

                when {
                    it.blockRankRate!! > 2 -> level = it.level.toInt() + 5
                    it.blockRankRate!! > 1 -> level = it.level.toInt() + 3
                    it.blockRankRate!! > 0 -> level = it.level.toInt() + 1
                    it.blockRankRate!! < -2 -> level = it.level.toInt() - 5
                    it.blockRankRate!! < -1 -> level = it.level.toInt() - 3
                    it.blockRankRate!! < 0 -> level = it.level.toInt() - 1
                }
                addDialog.addButton(
                    "${level}星--${it.stockCode}--${it.stockName}\n" +
                            "现价 : ${it.nowPrice}\n" +
                            "趋势单价：${it.trendPrice}\n" +
                            "相差趋势价：${decimalFormat3.format((it.nowPrice!! - it.trendPrice) / it.nowPrice!! * 100)}%",
                    R.color.white,
                    R.color.purple
                ) {}
            }

            addDialog.show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun resetData() {
        adapter.items.clear()
        adapter.items.addAll(stockItems)

        viewModel?.initData?.set(false)
        viewModel?.emptyStatus?.set(stockItems.size == 0)

        binding?.title?.text = getString(R.string.key_stock) + "(" + stockItems.size + ")"

        if (stockItems.size == 0) {
            toast("暂无重点打击对象")
        }

        viewModel?.loadingStatus()
    }

    private fun checkPromptStock() {
        if (stockItems.isNotEmpty()) {
            keyStocks.clear()
            soldStocks.clear()
            buyStocks.clear()
            trendStocks.clear()
            stockItems.forEach {
                if ((it.buyStatus == StockBuyStatus.ProOrder.buyStatus ||
                            it.buyStatus == StockBuyStatus.PromptBuy.buyStatus) && it.nowPrice!! > 4
                ) {
                    keyStocks.add(it)
                }

                if (it.buyStatus == StockBuyStatus.Purchased.buyStatus && it.buyDateTime != null &&
                    !DateUtils.isToday(it.buyDateTime!!) && DateUtils.isWorkday() && DateUtils.isCurrentInTimeScope()
                )
                    soldStocks.add(it)

                if (it.buyStatus == StockBuyStatus.BuyPrice.buyStatus)
                    buyStocks.add(it)

                if (it.nowPrice!! > it.trendPrice && (it.nowPrice!! - it.trendPrice) / it.nowPrice!! < 0.015 &&
                    BaseApplication.instance?.filterOptions?.needTrendPrompt!!
                ) {
                    trendStocks.add(it)
                }
            }

            if (soldStocks.size == 0) {
                binding?.soldBtn?.visibility = View.GONE
            } else {
                binding?.soldBtn?.visibility = View.VISIBLE

                if (BaseApplication.instance?.filterOptions?.promptType != KeyStockPrompt.None.info)
                    for (it in soldStocks) {
                        val profitRate =
                            (it.nowPrice!! - it.stockCostPrice!!) / it.stockCostPrice!! * 100
                        if (System.currentTimeMillis() - it.promptTime!! > 1000 * 60 * 3 && profitRate > it.targetProfitRate!! &&
                            !DateUtils.isToday(it.buyDateTime!!) && DateUtils.isWorkday() && DateUtils.isCurrentInTimeScope() &&
                            it.needPrompt
                        ) {
                            OfflineVoiceUtils.soldSpeak(requireContext(), it, profitRate.toInt())
                            break
                        }
                    }
            }

            if (keyStocks.size == 0) {
                binding?.promptBtn?.visibility = View.GONE
            } else {
                binding?.promptBtn?.visibility = View.VISIBLE

                if (BaseApplication.instance?.filterOptions?.promptType != KeyStockPrompt.None.info) {
                    if (System.currentTimeMillis() - promptTime > 1000 * 60 * 2) {
                        promptTime = System.currentTimeMillis()
                        mediaPlayer =
                            MediaPlayer.create(
                                requireContext(),
                                audios[random.nextInt(audios.size)]
                            )
                        mediaPlayer?.start()
                    }
                }
            }

            if (buyStocks.size == 0) {
                binding?.buyBtn?.visibility = View.GONE
            } else {
                binding?.buyBtn?.visibility = View.VISIBLE

                for (it in buyStocks) {
                    if (BaseApplication.instance?.filterOptions?.promptType != KeyStockPrompt.None.info && it.needPrompt) {
                        if (System.currentTimeMillis() - promptTime > 1000 * 60) {
                            promptTime = System.currentTimeMillis()
                            mediaPlayer =
                                MediaPlayer.create(requireContext(), R.raw.dog)
                            mediaPlayer?.start()
                        }
                    }
                }
            }

            if (trendStocks.size == 0) {
                binding?.trendPromptBtn?.visibility = View.GONE
            } else {
                binding?.trendPromptBtn?.visibility = View.VISIBLE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel?.showStatus = true
    }

    override fun onPause() {
        super.onPause()
        viewModel?.showStatus = false
    }
}