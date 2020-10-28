package com.magic.inmoney.fragment

import android.annotation.SuppressLint
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.magic.inmoney.R
import com.magic.inmoney.adapter.AllStockListAdapter
import com.magic.inmoney.base.BaseApplication
import com.magic.inmoney.base.BaseFragment
import com.magic.inmoney.databinding.FragmentAllStockBinding
import com.magic.inmoney.databinding.FragmentFilterStockBinding
import com.magic.inmoney.model.StockItemModel
import com.magic.inmoney.model.StockItemTodayModel
import com.magic.inmoney.orm.LitePalDBase
import com.magic.inmoney.orm.UpdateBaseDB
import com.magic.inmoney.utilities.DateUtils
import com.magic.inmoney.viewmodel.allstock.AllStockViewModel
import com.magic.inmoney.viewmodel.filter.FilterStockViewModel
import libs.mjn.prettydialog.PrettyDialog

class FilterStockFragment: BaseFragment<FragmentFilterStockBinding, FilterStockViewModel>() {

    private lateinit var adapter: AllStockListAdapter
    private var stockItems = ArrayList<StockItemTodayModel>()

    override val layoutId: Int
        get() = R.layout.fragment_filter_stock

    override fun createFragmentViewModel(): FilterStockViewModel {
        return FilterStockViewModel()
    }

    override fun initView() {
        binding?.lifecycleOwner = this
        binding?.viewModel = viewModel

        binding?.filterStockRecyclerView?.layoutManager = LinearLayoutManager(requireContext())
        adapter = AllStockListAdapter(requireContext())
        binding?.filterStockRecyclerView?.adapter = adapter

        resetData()
    }

    override fun setListener() {
        viewModel?.loadingStatus?.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                binding?.refreshLayout?.finishRefresh()
                binding?.refreshLayout?.finishLoadMore()
            }
        })

        binding?.refreshLayout?.setOnRefreshListener {
            stockItems.clear()
            stockItems.addAll(LitePalDBase.queryFilterStockItem())
            resetData()
        }

        binding?.filterStockRecyclerView?.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                val manager = recyclerView.layoutManager as LinearLayoutManager?
                val firstVisibleItemPosition = manager!!.findFirstVisibleItemPosition()
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (firstVisibleItemPosition == 0) {
                        binding?.goTopFab?.visibility = View.GONE
                    } else {
                        //显示回到顶部按钮
                        binding?.goTopFab?.visibility = View.VISIBLE
                        binding?.goTopFab?.setOnClickListener {
                            recyclerView.scrollToPosition(0)
                            binding?.goTopFab?.visibility = View.GONE
                        }
                    }
                }
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun resetData(){

        stockItems.addAll(LitePalDBase.queryFilterStockItem())

        adapter.items.clear()
        adapter.items.addAll(stockItems)
        viewModel?.initData?.set(false)
        viewModel?.emptyStatus?.set(stockItems.size == 0)

        binding?.title?.text = getString(R.string.filter_stock) + "(" + stockItems.size + ")"

        binding?.refreshLayout?.finishRefresh()
    }

    override fun onResume() {
        super.onResume()
    }
}