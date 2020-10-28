package com.magic.inmoney.base

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

abstract class BaseActivity<B : ViewDataBinding?, VM : BaseViewModel> : AppCompatActivity() {
    protected var binding: B? = null
    protected abstract val layoutId: Int
    protected abstract fun createViewModel(): VM
    protected abstract fun initView()
    protected abstract fun setListener()
    protected var viewModel: VM? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<B>(this, layoutId)
        viewModel = if (viewModel == null) createViewModel() else viewModel
        initView()
        setListener()
    }

    fun hideKeyboard(view: View) {
        val inputMethodManager =
                view.context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun showSoftKeyboard(view: View) {
        if (view.requestFocus()) {
            val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    fun toast(msg: String?){
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }
}