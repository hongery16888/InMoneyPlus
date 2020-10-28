package com.magic.inmoney.activity

import android.annotation.SuppressLint
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.magic.inmoney.R
import com.magic.inmoney.base.BaseActivity
import com.magic.inmoney.base.BaseApplication
import com.magic.inmoney.base.BaseFragmentAdapter
import com.magic.inmoney.const.QualityType
import com.magic.inmoney.const.StockItemType
import com.magic.inmoney.const.StockType
import com.magic.inmoney.databinding.ActivityKLineBinding
import com.magic.inmoney.fragment.KLineDetailFragment
import com.magic.inmoney.fragment.KLineFragment
import com.magic.inmoney.model.StockBaseInfo
import com.magic.inmoney.model.StockReport
import com.magic.inmoney.orm.LitePalDBase
import com.magic.inmoney.viewmodel.EmptyViewModel
import java.util.*

class KLineActivity: BaseActivity<ActivityKLineBinding, EmptyViewModel>() {

    private val list: MutableList<Fragment> = ArrayList()
    private var position = 0

    override val layoutId: Int
        get() = R.layout.activity_k_line

    override fun createViewModel(): EmptyViewModel {
        return EmptyViewModel()
    }

    @SuppressLint("SetTextI18n")
    override fun initView() {

        list.add(KLineFragment())
        list.add(KLineDetailFragment())

        binding?.container?.setScanScroll(false)
        binding?.container?.adapter = BaseFragmentAdapter(supportFragmentManager, list)

        position = intent.getIntExtra("position", 0)

        when(intent.getStringExtra("SockItemType")){
            StockItemType.AllStockItem.itemType ->{
                binding?.stockName = BaseApplication.instance?.stockItemTodayModels?.get(position)?.stockName +
                        "(" + BaseApplication.instance?.stockItemTodayModels?.get(position)?.stockCode + ")"
            }

            StockItemType.HighQualityItem.itemType ->{
                binding?.favoriteBtn?.visibility = View.VISIBLE
                binding?.stockName = BaseApplication.instance?.highQualityStockItems?.get(position)?.stockName +
                        "(" + BaseApplication.instance?.highQualityStockItems?.get(position)?.stockCode + ")"
            }

            StockItemType.FavoriteItem.itemType ->{
                binding?.stockName = BaseApplication.instance?.favoriteItems?.get(position)?.stockName +
                        "(" + BaseApplication.instance?.favoriteItems?.get(position)?.stockCode + ")"
            }

            StockItemType.KeyStockItem.itemType ->{
                binding?.stockName = BaseApplication.instance?.keyStockItems?.get(position)?.stockName +
                        "(" + BaseApplication.instance?.keyStockItems?.get(position)?.stockCode + ")"
            }

        }
    }

    override fun setListener() {
        binding?.kLineSwitch?.setOnCheckedChangeListener { _, isChecked ->
            binding?.container?.currentItem = if (isChecked) 0 else 1
        }

        binding?.favoriteBtn?.setOnClickListener {
            LitePalDBase.addSingleFavoriteStock(BaseApplication.instance?.highQualityStockItems?.get(position)!!, QualityType.Debug)
            toast("已添加到自选测试")
        }

    }

}