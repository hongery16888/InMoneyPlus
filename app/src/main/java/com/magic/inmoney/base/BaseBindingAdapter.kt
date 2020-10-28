package com.magic.inmoney.base

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList.OnListChangedCallback
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by user on 2017/9/2.
 */
abstract class BaseBindingAdapter<M, B : ViewDataBinding?>(protected var context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder?>() {
    var items: ObservableArrayList<M>
        protected set
    protected var itemsChangeCallback: ListChangedCallback

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding= DataBindingUtil.inflate<B>(LayoutInflater.from(context), getLayoutResId(viewType), parent, false)
        return BaseBindingViewHolder(binding!!.root)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding= DataBindingUtil.getBinding<B>(holder.itemView)
        onBindItem(binding, items[position], position)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        items.addOnListChangedCallback(itemsChangeCallback)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        items.removeOnListChangedCallback(itemsChangeCallback)
    }

    //region 处理数据集变化
    protected fun onChanged(newItems: ObservableArrayList<M>) {
        resetItems(newItems)
        notifyDataSetChanged()
    }

    protected fun onItemRangeChanged(newItems: ObservableArrayList<M>, positionStart: Int, itemCount: Int) {
        resetItems(newItems)
        notifyItemRangeChanged(positionStart, itemCount)
    }

    protected fun onItemRangeInserted(newItems: ObservableArrayList<M>, positionStart: Int, itemCount: Int) {
        resetItems(newItems)
        notifyItemRangeInserted(positionStart, itemCount)
    }

    protected fun onItemRangeMoved(newItems: ObservableArrayList<M>) {
        resetItems(newItems)
        notifyDataSetChanged()
    }

    protected fun onItemRangeRemoved(newItems: ObservableArrayList<M>, positionStart: Int, itemCount: Int) {
        resetItems(newItems)
        notifyItemRangeRemoved(positionStart, itemCount)
    }

    protected fun resetItems(newItems: ObservableArrayList<M>) {
        items = newItems
    }

    //endregion
    @LayoutRes
    protected abstract fun getLayoutResId(viewType: Int): Int
    protected abstract fun onBindItem(binding: B?, item: M, position: Int)
    inner class ListChangedCallback : OnListChangedCallback<ObservableArrayList<M>>() {
        override fun onChanged(newItems: ObservableArrayList<M>) {
            this@BaseBindingAdapter.onChanged(newItems)
        }

        override fun onItemRangeChanged(newItems: ObservableArrayList<M>, i: Int, i1: Int) {
            this@BaseBindingAdapter.onItemRangeChanged(newItems, i, i1)
        }

        override fun onItemRangeInserted(newItems: ObservableArrayList<M>, i: Int, i1: Int) {
            this@BaseBindingAdapter.onItemRangeInserted(newItems, i, i1)
        }

        override fun onItemRangeMoved(newItems: ObservableArrayList<M>, i: Int, i1: Int, i2: Int) {
            this@BaseBindingAdapter.onItemRangeMoved(newItems)
        }

        override fun onItemRangeRemoved(sender: ObservableArrayList<M>, positionStart: Int, itemCount: Int) {
            this@BaseBindingAdapter.onItemRangeRemoved(sender, positionStart, itemCount)
        }
    }

    init {
        items = ObservableArrayList()
        itemsChangeCallback = ListChangedCallback()
    }
}