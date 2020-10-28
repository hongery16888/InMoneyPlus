package com.magic.inmoney.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment

abstract class BaseFragment<B : ViewDataBinding?, VM : BaseViewModel> : Fragment() {

    protected var binding: B? = null
    protected abstract val layoutId: Int
    protected var viewModel: VM? = null
    protected abstract fun initView()
    protected abstract fun setListener()
    protected abstract fun createFragmentViewModel(): VM

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(layoutId, container, false)
        binding = DataBindingUtil.bind<B>(view)
        viewModel = if (viewModel == null) createFragmentViewModel() else viewModel
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        setListener()
    }

    fun toast(msg: String?){
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
    }
}