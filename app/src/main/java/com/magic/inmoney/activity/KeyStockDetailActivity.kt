package com.magic.inmoney.activity

import android.annotation.SuppressLint
import android.view.View
import android.widget.AdapterView
import com.jem.rubberpicker.RubberRangePicker
import com.magic.inmoney.R
import com.magic.inmoney.base.BaseActivity
import com.magic.inmoney.const.StockBuyStatus
import com.magic.inmoney.databinding.ActivityKeyStockDetailBinding
import com.magic.inmoney.listener.OnDateSelectListener
import com.magic.inmoney.model.KeyStockModel
import com.magic.inmoney.orm.LitePalDBase
import com.magic.inmoney.utilities.DateUtils
import com.magic.inmoney.view.KeyStockDetailFilterDateDialog
import com.magic.inmoney.viewmodel.key.KeyStockDetailViewModel
import com.rm.rmswitch.RMSwitch.RMSwitchObserver
import java.text.DecimalFormat


class KeyStockDetailActivity: BaseActivity<ActivityKeyStockDetailBinding, KeyStockDetailViewModel>(){

    private var keyStock: KeyStockModel? = null

    private var decimalFormat: DecimalFormat = DecimalFormat("#0.00")
    private var filterDateDialog: KeyStockDetailFilterDateDialog? = null

    override val layoutId: Int
        get() = R.layout.activity_key_stock_detail

    override fun createViewModel(): KeyStockDetailViewModel {
        return KeyStockDetailViewModel()
    }

    override fun initView() {
        keyStock = LitePalDBase.queryKeyStockItem(intent.getStringExtra("stockCode")!!)
        if (keyStock == null){
            toast("查无此股")
            return
        }

        binding?.keyStock = keyStock
        binding?.viewModel = viewModel
        if (keyStock?.buyDateTime == null || keyStock?.buyDateTime!!.isEmpty()){
            binding?.buyDateDefault = DateUtils.today("yyyy-MM-dd")
        }else
            binding?.buyDateDefault = keyStock?.buyDateTime
        binding?.currentProfitRate = decimalFormat.format((keyStock?.nowPrice!! - keyStock?.stockCostPrice!!) / keyStock?.stockCostPrice!! * 100f).toFloat()
        binding?.maxProfitRate = decimalFormat.format((keyStock?.todayMax!! - keyStock?.stockCostPrice!!) / keyStock?.stockCostPrice!! * 100f).toFloat()

        binding?.stockStatusType?.setSelection(
            when(keyStock?.buyStatus){
                StockBuyStatus.Purchased.buyStatus -> 0
                StockBuyStatus.WaitTargetPrice.buyStatus -> 1
                StockBuyStatus.ProOrder.buyStatus -> 2
                StockBuyStatus.PromptBuy.buyStatus -> 3
                StockBuyStatus.Sold.buyStatus -> 4
                else -> 5
            }
        )

        binding?.diffRateRange?.setCurrentStartValue(keyStock?.targetLossRate!!)
        binding?.diffRateRange?.setCurrentEndValue(keyStock?.targetProfitRate!!)
    }

    override fun setListener() {
        binding?.modifyPrice?.setOnClickListener {
            binding?.buyPriceSettingLay?.visibility = View.VISIBLE
        }

        binding?.modifyPriceConfirm?.setOnClickListener {

            if (binding?.modifyPriceEdit?.text.toString().isEmpty()){
                toast("请输入买入单价")
                return@setOnClickListener
            }

            binding?.buyPriceSettingLay?.visibility = View.GONE

            keyStock?.stockCostPrice = decimalFormat.format(binding?.modifyPriceEdit?.text.toString().toFloat()).toFloat()

            binding?.stockCostPrice?.text = keyStock?.stockCostPrice.toString()

        }

        binding?.diffRateRange?.setOnRubberRangePickerChangeListener(object : RubberRangePicker.OnRubberRangePickerChangeListener{
            override fun onProgressChanged(
                rangePicker: RubberRangePicker,
                startValue: Int,
                endValue: Int,
                fromUser: Boolean
            ) {
                println("------------------>Start : $startValue")
                resetRateText(startValue, endValue)
                keyStock?.targetLossRate = startValue
                keyStock?.targetProfitRate = endValue
            }

            override fun onStartTrackingTouch(
                rangePicker: RubberRangePicker,
                isStartThumb: Boolean
            ) {

            }

            override fun onStopTrackingTouch(
                rangePicker: RubberRangePicker,
                isStartThumb: Boolean
            ) {

            }
        })

        binding?.needPrompt?.addSwitchObserver { _, isChecked ->
            keyStock?.needPrompt = isChecked
        }

        binding?.save?.setOnClickListener {
            if (LitePalDBase.updateKeyStock(keyStock!!)){
                toast("更新成功")
            }else{
                toast("更新失败")
            }
        }

        binding?.stockStatusType?.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                keyStock?.buyStatus = resources.getStringArray(R.array.stock_status)[p2]

                if (keyStock?.buyStatus == StockBuyStatus.Purchased.buyStatus){
                    binding?.buyDateLay?.visibility = View.VISIBLE
                    binding?.buyPriceLay?.visibility = View.VISIBLE
                }else{
                    binding?.buyDateLay?.visibility = View.GONE
                    binding?.buyPriceLay?.visibility = View.GONE
                }
            }
        }

        binding?.buyDateSetting?.setOnClickListener {
            if (filterDateDialog == null){
                filterDateDialog = KeyStockDetailFilterDateDialog(this).setSelectDateListener(object :
                    OnDateSelectListener {
                    override fun callback(dates: ArrayList<String>) {
                        if (dates.isEmpty()){
                            toast("无效选择，请重试")
                            return
                        }

                        keyStock?.buyDateTime = dates[0]
                        binding?.buyDate?.text = keyStock?.buyDateTime

                    }
                })
            }

            filterDateDialog?.show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun resetRateText(startValue: Int, endValue: Int) {
        binding?.diffRateStart?.text = "$startValue%"
        binding?.diffRateEnd?.text = "$endValue%"
        when {
            startValue > 0 -> {
                binding?.diffRateStart?.setTextColor(resources.getColor(R.color.loss_true))
            }
            startValue == 0 -> {
                binding?.diffRateStart?.setTextColor(resources.getColor(R.color.loss_equ))
            }
            startValue < 0 -> {
                binding?.diffRateStart?.setTextColor(resources.getColor(R.color.loss_false))
            }
        }

        when {
            endValue > 0 -> {
                binding?.diffRateEnd?.setTextColor(resources.getColor(R.color.loss_true))
            }
            endValue == 0 -> {
                binding?.diffRateEnd?.setTextColor(resources.getColor(R.color.loss_equ))
            }
            endValue < 0 -> {
                binding?.diffRateEnd?.setTextColor(resources.getColor(R.color.loss_false))
            }
        }
    }
}