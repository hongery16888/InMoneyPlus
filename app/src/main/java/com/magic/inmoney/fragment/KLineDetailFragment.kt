package com.magic.inmoney.fragment

import com.magic.inmoney.R
import com.magic.inmoney.base.BaseActivity
import com.magic.inmoney.base.BaseApplication
import com.magic.inmoney.base.BaseFragment
import com.magic.inmoney.const.StockItemType
import com.magic.inmoney.databinding.FragmentKLineDetailBinding
import com.magic.inmoney.viewmodel.kline.KLineDetailViewModel

class KLineDetailFragment: BaseFragment<FragmentKLineDetailBinding, KLineDetailViewModel>(){

    private var position = 0

    override val layoutId: Int
        get() = R.layout.fragment_k_line_detail

    override fun createFragmentViewModel(): KLineDetailViewModel {
        return KLineDetailViewModel()
    }

    override fun initView() {

        position = activity?.intent?.getIntExtra("position", 0)!!

        when(activity?.intent?.getStringExtra("SockItemType")){
            StockItemType.AllStockItem.itemType ->{
                binding?.stockCode = BaseApplication.instance?.stockItemTodayModels?.get(position)?.market + BaseApplication.instance?.stockItemTodayModels?.get(position)?.stockCode
            }

            StockItemType.HighQualityItem.itemType ->{
                binding?.stockCode = BaseApplication.instance?.highQualityStockItems?.get(position)?.market + BaseApplication.instance?.highQualityStockItems?.get(position)?.stockCode
            }

            StockItemType.FavoriteItem.itemType ->{
                binding?.stockCode = BaseApplication.instance?.favoriteItems?.get(position)?.market + BaseApplication.instance?.favoriteItems?.get(position)?.stockCode
            }
        }
    }

    override fun setListener() {

    }
}