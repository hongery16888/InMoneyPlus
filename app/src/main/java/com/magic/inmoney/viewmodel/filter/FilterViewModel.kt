package com.magic.inmoney.viewmodel.filter

import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.magic.inmoney.base.BaseApplication
import com.magic.inmoney.base.BaseViewModel
import com.magic.inmoney.const.StockSortDirection
import com.magic.inmoney.const.ThroughType
import com.magic.upcoming.games.utilities.Event

class FilterViewModel : BaseViewModel() {

    var initData: ObservableField<Boolean> = ObservableField(true)
    var sortType: ObservableField<String> = ObservableField<String>(BaseApplication.instance?.filterOptions?.sortType!!)
    var sortDirection: ObservableField<String> = ObservableField<String>(BaseApplication.instance?.filterOptions?.sortDirection!!)
    var promptType: ObservableField<String> = ObservableField<String>(BaseApplication.instance?.filterOptions?.promptType!!)
    var buyPoint: ObservableField<String> = ObservableField<String>(if (BaseApplication.instance?.filterOptions?.buyPoint.isNullOrEmpty()) "any" else BaseApplication.instance?.filterOptions?.buyPoint!!)
    var throughType: ObservableField<String> = ObservableField<String>(if (BaseApplication.instance?.filterOptions?.throughType.isNullOrEmpty()) ThroughType.NormalThrough.type else BaseApplication.instance?.filterOptions?.throughType!!)
    var needTrend: ObservableField<Boolean> = ObservableField<Boolean>(BaseApplication.instance?.filterOptions?.needTrendPrompt!!)

    var emptyStatus: ObservableField<Boolean> = ObservableField(false)

    private val _LoadingStatus = MutableLiveData<Event<Boolean>>()
    val loadingStatus: LiveData<Event<Boolean>>
        get() = _LoadingStatus

    private fun loadingStatus() {
        initData.set(false)
        _LoadingStatus.value = Event(true)
    }


}