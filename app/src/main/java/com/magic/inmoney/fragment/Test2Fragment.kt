package com.magic.inmoney.fragment

import com.magic.inmoney.R
import com.magic.inmoney.base.BaseFragment
import com.magic.inmoney.databinding.FragmentTest2Binding
import com.magic.inmoney.viewmodel.EmptyViewModel

class Test2Fragment: BaseFragment<FragmentTest2Binding, EmptyViewModel>() {

    override val layoutId: Int
        get() = R.layout.fragment_test2

    override fun createFragmentViewModel(): EmptyViewModel {
        return EmptyViewModel()
    }

    override fun initView() {
    }

    override fun setListener() {

    }

    override fun onResume() {
        super.onResume()
    }
}