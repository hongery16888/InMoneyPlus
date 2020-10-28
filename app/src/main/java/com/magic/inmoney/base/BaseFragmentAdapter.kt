package com.magic.inmoney.base

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

/**
 * Created by ThinkPad on 2018/2/26.
 */
class BaseFragmentAdapter(fm: FragmentManager?, private val list: List<Fragment>) : FragmentPagerAdapter(fm!!) {
    override fun getItem(position: Int): Fragment {
        return list[position]
    }

    override fun getCount(): Int {
        return list.size
    }

}