package com.magic.inmoney.base

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import com.magic.inmoney.base.BaseApplication

/**
 * Created by yuzhe on 2017/4/8.
 */
abstract class BaseViewModel(application: Application  = BaseApplication.instance!!) : AndroidViewModel(application) {

//    abstract fun <T> getData(onNetworkListener: OnNetworkListener<T>? = null) : T

    fun toast(mes: String?){
        Toast.makeText(BaseApplication.instance, mes, Toast.LENGTH_LONG).show()
    }
}