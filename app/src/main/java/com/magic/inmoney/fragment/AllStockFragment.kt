package com.magic.inmoney.fragment

import android.annotation.SuppressLint
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.magic.inmoney.R
import com.magic.inmoney.adapter.AllStockListAdapter
import com.magic.inmoney.base.BaseApplication
import com.magic.inmoney.base.BaseFragment
import com.magic.inmoney.databinding.FragmentAllStockBinding
import com.magic.inmoney.orm.LitePalDBase
import com.magic.inmoney.viewmodel.allstock.AllStockViewModel

class AllStockFragment : BaseFragment<FragmentAllStockBinding, AllStockViewModel>() {

    private lateinit var adapter: AllStockListAdapter

    override val layoutId: Int
        get() = R.layout.fragment_all_stock

    override fun createFragmentViewModel(): AllStockViewModel {
        return AllStockViewModel()
    }

    override fun initView() {
        binding?.lifecycleOwner = this
        binding?.viewModel = viewModel

        binding?.allStockRecyclerView?.layoutManager = LinearLayoutManager(requireContext())
        adapter = AllStockListAdapter(requireContext())
        binding?.allStockRecyclerView?.adapter = adapter

        viewModel?.reset()
        viewModel?.isRefresh = false
        viewModel?.stockList()

    }

    override fun setListener() {
        binding?.clean?.setOnClickListener {
            binding?.searchTextView?.setText("")
        }

        binding?.searchTextView?.addTextChangedListener {
            adapter.items.clear()
            if (it.isNullOrEmpty()) {
                binding?.clean?.visibility = View.INVISIBLE
                viewModel?.isRefresh = false
                viewModel?.filterStatus?.set(true)
                LitePalDBase.loadTodayStockItem()
                viewModel?.stockList()
            } else {
                binding?.clean?.visibility = View.VISIBLE
                viewModel?.isRefresh = false
                viewModel?.filterStatus?.set(true)
                LitePalDBase.loadTodayStockItem(it.toString())
                viewModel?.stockList()
            }
        }

        viewModel?.loadingStatus?.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                binding?.refreshLayout?.finishRefresh()
                binding?.refreshLayout?.finishLoadMore()
            }
        })

        viewModel?.updateStatus?.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {

                if (viewModel?.isRefresh!!) {
                    LitePalDBase.updateStockItems(adapter.items)
                    BaseApplication.instance?.stockItemTodayModels?.clear()
                    BaseApplication.instance?.stockItemTodayModels?.addAll(adapter.items)

//                    LitePalDBase.updateFavoriteStockForNowPrice(BaseApplication.instance?.stockItems!!)
                }
            }
        })

        viewModel?.stockList?.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { model ->

                model.let { date ->
                    adapter.items.addAll(date)
                    viewModel?.emptyStatus?.set(adapter.items.size == 0)
                    resetTitle()
                }
            }
        })

        binding?.allStockRecyclerView?.addOnScrollListener(object :
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

        binding?.refreshLayout?.setOnRefreshListener {
            adapter.items.clear()
            viewModel?.reset()
            LitePalDBase.loadStock()
            viewModel?.isRefresh = true
            viewModel?.stockList()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun resetTitle() {
        binding?.title?.text = getString(R.string.all_stock) + "(" + adapter.items.size + ")"
    }

    override fun onResume() {
        super.onResume()
    }
}