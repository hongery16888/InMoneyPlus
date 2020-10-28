package com.magic.inmoney.adapter

import android.content.Context
import com.magic.inmoney.R
import com.magic.inmoney.base.BaseBindingAdapter
import com.magic.inmoney.databinding.ImportImageStockItemBinding
import com.magic.inmoney.databinding.StatisticsTotalStockItemBinding
import com.magic.inmoney.model.KeyStockModel
import com.magic.inmoney.model.StatisticsTotalModel
import com.magic.inmoney.orm.LitePalDBase
import libs.mjn.prettydialog.PrettyDialog
import java.text.DecimalFormat
import kotlin.math.abs

class ImportImageListAdapter(private val mContext: Context) : BaseBindingAdapter<KeyStockModel, ImportImageStockItemBinding>(mContext) {

    override fun getLayoutResId(viewType: Int): Int {
        return R.layout.import_image_stock_item
    }

    override fun onBindItem(binding: ImportImageStockItemBinding?, item: KeyStockModel, position: Int) {
        binding?.item = item
    }
}