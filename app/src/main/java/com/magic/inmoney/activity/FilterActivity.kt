package com.magic.inmoney.activity

import android.annotation.SuppressLint
import android.view.View
import com.github.zagum.switchicon.SwitchIconView
import com.jem.rubberpicker.RubberRangePicker
import com.jem.rubberpicker.RubberSeekBar
import com.magic.inmoney.R
import com.magic.inmoney.base.BaseActivity
import com.magic.inmoney.base.BaseApplication
import com.magic.inmoney.const.StockType
import com.magic.inmoney.databinding.ActivityFilterBinding
import com.magic.inmoney.orm.LitePalDBase
import com.magic.inmoney.viewmodel.filter.FilterViewModel
import java.text.DecimalFormat


class FilterActivity : BaseActivity<ActivityFilterBinding?, FilterViewModel>(), View.OnClickListener {

    private var stockTypes = BaseApplication.instance?.filterOptions?.stockTypes
    private var decimalFormat: DecimalFormat = DecimalFormat("#0.0")

    override val layoutId: Int
        get() = R.layout.activity_filter

    override fun createViewModel(): FilterViewModel {
        return FilterViewModel()
    }

    override fun initView() {

        binding?.viewModel = viewModel

        binding?.switchShA?.setIconEnabled(stockTypes!!.contains(StockType.SHA.prefix))
        binding?.switchShB?.setIconEnabled(stockTypes!!.contains(StockType.SHB.prefix))
        binding?.switchCyb?.setIconEnabled(stockTypes!!.contains(StockType.CYB.prefix))
        binding?.switchKcb?.setIconEnabled(stockTypes!!.contains(StockType.KCB.prefix))
        binding?.switchSzA?.setIconEnabled(stockTypes!!.contains(StockType.SZA.prefix))
        binding?.switchZxb?.setIconEnabled(stockTypes!!.contains(StockType.ZXB.prefix))
        binding?.switchSzB?.setIconEnabled(stockTypes!!.contains(StockType.SZB.prefix))

        binding?.diffRateRange?.setCurrentStartValue(BaseApplication.instance?.filterOptions?.startRate!!)
        binding?.diffRateRange?.setCurrentEndValue(BaseApplication.instance?.filterOptions?.endRate!!)
        binding?.beforeDaysSeekBar?.setCurrentValue(BaseApplication.instance?.filterOptions?.beforeDay!!)
        binding?.volumeRatePicker?.setCurrentStartValue(BaseApplication.instance?.filterOptions?.volumeStartRate!!)
        binding?.volumeRatePicker?.setCurrentEndValue(BaseApplication.instance?.filterOptions?.volumeEndRate!!)
        binding?.turnoverRatePicker?.setCurrentStartValue(BaseApplication.instance?.filterOptions?.turnoverStartRate!!)
        binding?.turnoverRatePicker?.setCurrentEndValue(BaseApplication.instance?.filterOptions?.turnoverEndRate!!)
    }

    override fun setListener() {

        binding?.switchShA?.setOnClickListener(this)
        binding?.switchShB?.setOnClickListener(this)
        binding?.switchCyb?.setOnClickListener(this)
        binding?.switchKcb?.setOnClickListener(this)
        binding?.switchSzA?.setOnClickListener(this)
        binding?.switchZxb?.setOnClickListener(this)
        binding?.switchSzB?.setOnClickListener(this)

        binding?.upNavigationImageButton?.setOnClickListener {
            finish()
        }

        binding?.beforeDaysSeekBar?.setOnRubberSeekBarChangeListener(object : RubberSeekBar.OnRubberSeekBarChangeListener{
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(seekBar: RubberSeekBar, value: Int, fromUser: Boolean) {
                BaseApplication.instance?.filterOptions?.beforeDay = value
                binding?.beforeDays?.text = "往前筛选天数($value)"
            }

            override fun onStartTrackingTouch(seekBar: RubberSeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: RubberSeekBar) {

            }
        })

        binding?.volumeRatePicker?.setOnRubberRangePickerChangeListener(object : RubberRangePicker.OnRubberRangePickerChangeListener{
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(
                rangePicker: RubberRangePicker,
                startValue: Int,
                endValue: Int,
                fromUser: Boolean
            ) {
                BaseApplication.instance?.filterOptions?.volumeStartRate = startValue
                BaseApplication.instance?.filterOptions?.volumeEndRate = endValue
                binding?.returnHighPointTitle?.text = "五日量比( ${decimalFormat.format(startValue * 0.1f)} ~ ${decimalFormat.format(endValue * 0.1f)} )"
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

        binding?.turnoverRatePicker?.setOnRubberRangePickerChangeListener(object : RubberRangePicker.OnRubberRangePickerChangeListener{
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(
                rangePicker: RubberRangePicker,
                startValue: Int,
                endValue: Int,
                fromUser: Boolean
            ) {
                binding?.turnoverRateTitle?.text = "换手率幅度( ${decimalFormat.format(startValue * 0.1f)}% ~ ${decimalFormat.format(endValue * 0.1f)}% )"
                BaseApplication.instance?.filterOptions?.turnoverStartRate = startValue
                BaseApplication.instance?.filterOptions?.turnoverEndRate = endValue
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

        binding?.diffRateRange?.setOnRubberRangePickerChangeListener(object : RubberRangePicker.OnRubberRangePickerChangeListener{
            override fun onProgressChanged(
                rangePicker: RubberRangePicker,
                startValue: Int,
                endValue: Int,
                fromUser: Boolean
            ) {
                resetRateText(startValue, endValue)
                BaseApplication.instance?.filterOptions?.startRate = startValue
                BaseApplication.instance?.filterOptions?.endRate = endValue
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
            BaseApplication.instance?.filterOptions?.needTrendPrompt = isChecked
        }
    }

    override fun onClick(switch: View?) {
        (switch as SwitchIconView).switchState()
        when(switch.id){
            R.id.switch_sh_a -> {
                if (switch.isIconEnabled){
                    BaseApplication.instance?.filterOptions?.stockTypes?.add(StockType.SHA.prefix)
                }else{
                    BaseApplication.instance?.filterOptions?.stockTypes?.remove(StockType.SHA.prefix)
                }
            }
            R.id.switch_sh_b -> {
                if (switch.isIconEnabled){
                    BaseApplication.instance?.filterOptions?.stockTypes?.add(StockType.SHB.prefix)
                }else{
                    BaseApplication.instance?.filterOptions?.stockTypes?.remove(StockType.SHB.prefix)
                }
            }
            R.id.switch_cyb -> {
                if (switch.isIconEnabled){
                    BaseApplication.instance?.filterOptions?.stockTypes?.add(StockType.CYB.prefix)
                }else{
                    BaseApplication.instance?.filterOptions?.stockTypes?.remove(StockType.CYB.prefix)
                }
            }
            R.id.switch_kcb -> {
                if (switch.isIconEnabled){
                    BaseApplication.instance?.filterOptions?.stockTypes?.add(StockType.KCB.prefix)
                }else{
                    BaseApplication.instance?.filterOptions?.stockTypes?.remove(StockType.KCB.prefix)
                }
            }
            R.id.switch_sz_a -> {
                if (switch.isIconEnabled){
                    BaseApplication.instance?.filterOptions?.stockTypes?.add(StockType.SZA.prefix)
                }else{
                    BaseApplication.instance?.filterOptions?.stockTypes?.remove(StockType.SZA.prefix)
                }
            }
            R.id.switch_zxb -> {
                if (switch.isIconEnabled){
                    BaseApplication.instance?.filterOptions?.stockTypes?.add(StockType.ZXB.prefix)
                }else{
                    BaseApplication.instance?.filterOptions?.stockTypes?.remove(StockType.ZXB.prefix)
                }
            }
            R.id.switch_sz_b -> {
                if (switch.isIconEnabled){
                    BaseApplication.instance?.filterOptions?.stockTypes?.add(StockType.SZB.prefix)
                }else{
                    BaseApplication.instance?.filterOptions?.stockTypes?.remove(StockType.SZB.prefix)
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun resetRateText(startValue: Int, endValue: Int){
        binding?.diffRateStart?.text = "$startValue%"
        binding?.diffRateEnd?.text = "$endValue%"
        when{
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

        when{
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

    override fun onDestroy() {
        LitePalDBase.updateFilterOption()
        super.onDestroy()
    }
}