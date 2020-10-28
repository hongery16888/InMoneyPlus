package com.magic.inmoney.view

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.magic.inmoney.R
import com.magic.inmoney.databinding.FilterKlineDialogBinding
import com.magic.inmoney.databinding.NumPickerDialogBinding
import com.magic.inmoney.listener.OnKLineSelectListener
import com.magic.inmoney.model.KeyStockModel
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList

class NumPickerDialog : Dialog {

    private var binding: NumPickerDialogBinding
    private var data: ArrayList<String> = ArrayList()
    private var item: KeyStockModel? = null
    private var decimalFormat: DecimalFormat = DecimalFormat("#0.00")

    constructor(context: Context) : this(context, 0)
    constructor(context: Context, themeResId: Int) : super(context, R.style.CustomDialog) {
        val view = View.inflate(context, R.layout.num_picker_dialog, null)
        setContentView(view)
        window?.setGravity(Gravity.CENTER)
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        binding = DataBindingUtil.bind(view)!!

        setCanceledOnTouchOutside(false)
        data = ArrayList()
        for (i in -20..20) {
            data.add("$i%")
        }
        binding.costPricePicker.setData(data)
        binding.nowPricePicker.setData(data)
        binding.costPricePicker.setSelected("0%")
        binding.nowPricePicker.setSelected("0%")
        binding.costPricePicker.setOnSelectListener {
            binding.costPrice.text = decimalFormat.format(item?.stockCostPrice!! * (1 + it.replace("%","").toInt() * 0.01)).toString()
        }

        binding.nowPricePicker.setOnSelectListener {
            binding.nowPrice.text = decimalFormat.format(item?.yestClosePrice!! * (1 + it.replace("%","").toInt() * 0.01)).toString()
        }

        binding.know.setOnClickListener {
            dismiss()
        }
    }

    @SuppressLint("SetTextI18n")
    fun setStockItem(item: KeyStockModel) {
        this.item = item
        binding.title.text = "计算--${item.stockName}--买卖点"
        binding.costPrice.text = item.stockCostPrice.toString()
        binding.nowPrice.text = item.yestClosePrice.toString()
    }
}