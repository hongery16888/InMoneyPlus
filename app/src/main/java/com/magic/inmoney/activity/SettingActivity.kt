package com.magic.inmoney.activity

import android.content.Intent
import androidx.lifecycle.Observer
import com.magic.inmoney.R
import com.magic.inmoney.base.BaseActivity
import com.magic.inmoney.base.BaseApplication
import com.magic.inmoney.databinding.ActivitySettingBinding
import com.magic.inmoney.model.StockItemRecentModel
import com.magic.inmoney.model.StockModel
import com.magic.inmoney.viewmodel.update.UpdateViewModel
import com.tencent.mmkv.MMKV
import libs.mjn.prettydialog.PrettyDialog
import org.litepal.LitePal

class SettingActivity : BaseActivity<ActivitySettingBinding, UpdateViewModel>(){

    private var promptDialog: PrettyDialog? = null
    private var sharedPreferences: MMKV? = null
    private var selectType = "recent"

    override val layoutId: Int
        get() = R.layout.activity_setting

    override fun createViewModel(): UpdateViewModel {
        return UpdateViewModel()
    }

    override fun initView() {
        binding?.lifecycleOwner = this
        binding?.viewModel = viewModel

        sharedPreferences = MMKV.defaultMMKV()
    }

    override fun setListener() {
        viewModel?.shProgress?.observe(this, Observer {
            it.getContentIfNotHandled()?.let { progress ->
                binding?.shUpdate?.setProgress(progress)
                if (progress == 100f){
                    binding?.shUpdate?.revertAnimation()
                }
            }
        })

        viewModel?.szProgress?.observe(this, Observer {
            it.getContentIfNotHandled()?.let { progress ->
                binding?.szUpdate?.setProgress(progress)

                if (progress == 100f){
                    binding?.szUpdate?.revertAnimation()
                }
            }
        })

        viewModel?.infoProgress?.observe(this, Observer {
            it.getContentIfNotHandled()?.let { progress ->
                if (!progress)
                    binding?.infoUpdate?.revertAnimation()
            }
        })

        viewModel?.reportProgress?.observe(this, Observer {
            it.getContentIfNotHandled()?.let { progress ->
                if (!progress)
                    binding?.reportUpdate?.revertAnimation()
            }
        })

        binding?.shUpdate?.setOnClickListener {

            if (viewModel?.initData?.get()!!){
                toast("数据正在更新，请稍候")
                return@setOnClickListener
            }

            LitePal.deleteAll(StockModel::class.java, "market = ?", "sh")
            viewModel?.maxPage = 100
            viewModel?.updateSHList()
            binding?.shUpdate?.startAnimation()
        }

        binding?.szUpdate?.setOnClickListener {
            if (viewModel?.initData?.get()!!){
                toast("数据正在更新，请稍候")
                return@setOnClickListener
            }
            LitePal.deleteAll(StockModel::class.java, "market = ?", "sz")
            viewModel?.maxPage = 100
            viewModel?.updateSZList()
            binding?.szUpdate?.startAnimation()
        }

        binding?.infoUpdate?.setOnClickListener {
            toast("数据正在更新，请稍候")
            binding?.infoUpdate?.startAnimation()
            viewModel?.updateStockInfo(this)
        }

        binding?.reportUpdate?.setOnClickListener {
            toast("数据正在更新，请稍候")
            binding?.reportUpdate?.startAnimation()
            viewModel?.updateStockReport(this)
        }

        binding?.statisticsUpdate?.setOnClickListener {
            startActivity(Intent(this, StatisticsActivity::class.java))
        }

        viewModel?.historyProgress?.observe(this, Observer {
            it.getContentIfNotHandled()?.let { progress ->
                binding?.updateHistory?.setProgress(progress / BaseApplication.instance?.stocks?.size!! * 100)
                sharedPreferences?.encode("HistoryCount", progress.toInt())
                if (progress / BaseApplication.instance?.stocks?.size!! * 100 == 100f){
                    binding?.updateHistory?.revertAnimation()
                }
            }
        })

        binding?.updateHistory?.setOnClickListener {

            val count = sharedPreferences?.decodeInt("HistoryCount", 0)

            showUpdateHistory(count!!)
        }

        viewModel?.calculateProgress?.observe(this, Observer {
            it.getContentIfNotHandled()?.let { progress ->
                binding?.calculateStock?.setProgress(progress / BaseApplication.instance?.stocks?.size!! * 100)
                if (selectType == "recent"){
                    sharedPreferences?.encode("RecentCount", progress.toInt())
                }else if (selectType == "calculateRecent"){

                }else {
                    sharedPreferences?.encode("CalculateCount", progress.toInt())
                }
                if ((progress + 1) / BaseApplication.instance?.stocks?.size!! * 100 == 100f){
                    binding?.calculateStock?.revertAnimation()
                }
            }
        })

        binding?.calculateStock?.setOnClickListener {
            val count = sharedPreferences?.decodeInt("CalculateCount", 0)
            showCalculateStock(count!!)
        }
    }

    private fun showUpdateHistory(count: Int){
        promptDialog = PrettyDialog(this)
            .setIcon(R.drawable.ic_favorite_dialog)
            .setTitle("重置历史数据 ( $count )")
            .setMessage("重置前会清空之前历史数据，是否继续重置？")
            .addButton("继续当前更新", R.color.confirm_text, R.color.red) {
                if (viewModel?.initData?.get()!!){
                    toast("数据正在更新，请稍候")
                }else{
                    viewModel?.maxPage = 100
                    selectType = ""
                    viewModel?.updateStockHistory(count)
                    binding?.updateHistory?.startAnimation()
                    promptDialog?.dismiss()
                }
            }
            .addButton("重置并更新", R.color.confirm_text, R.color.red) {
                if (viewModel?.initData?.get()!!){
                    toast("数据正在更新，请稍候")
                }else{
                    viewModel?.maxPage = 100
                    selectType = ""
                    viewModel?.updateStockHistory()
                    binding?.updateHistory?.startAnimation()
                    promptDialog?.dismiss()
                }
            }

        promptDialog?.show()
    }

    private fun showCalculateStock(count: Int){
        promptDialog = PrettyDialog(this)
            .setIcon(R.drawable.ic_favorite_dialog)
            .setTitle("重新计算数据")
            .setMessage("计算前会清空之前历史数据，是否继续计算？")
            .addButton("继续当前计算 ( $count )", R.color.confirm_text, R.color.red) {
                if (viewModel?.initData?.get()!!){
                    toast("数据正在计算，请稍候")
                }else{
                    viewModel?.maxPage = 100
                    selectType = ""
                    viewModel?.initStockItemCalculate(count)
                    binding?.calculateStock?.startAnimation()
                    promptDialog?.dismiss()
                }
            }
            .addButton("清空并计算", R.color.confirm_text, R.color.red) {
                if (viewModel?.initData?.get()!!){
                    toast("数据正在计算，请稍候")
                }else{
                    viewModel?.maxPage = 100
                    viewModel?.initStockItemCalculate()
                    binding?.calculateStock?.startAnimation()
                    promptDialog?.dismiss()
                }
            }
            .addButton("复制最近数据(${sharedPreferences?.decodeInt("RecentCount", 0)})", R.color.confirm_text, R.color.red) {
                if (viewModel?.initData?.get()!!){
                    toast("数据正在复制中，请稍候")
                }else{
                    viewModel?.maxPage = 100
                    selectType = "recent"
                    viewModel?.copyRecentData(sharedPreferences?.decodeInt("RecentCount", 0)!!)
                    binding?.calculateStock?.startAnimation()
                    promptDialog?.dismiss()
                }
            }
            .addButton("计算最近数据", R.color.confirm_text, R.color.red) {
                if (viewModel?.initData?.get()!!){
                    toast("数据正在计算中，请稍候")
                }else{
                    viewModel?.maxPage = 100
                    selectType = "calculateRecent"
                    viewModel?.calculateRecentStockItem()
                    binding?.calculateStock?.startAnimation()
                    promptDialog?.dismiss()
                }
            }


        promptDialog?.show()
    }

    override fun onResume() {
        super.onResume()
    }
}