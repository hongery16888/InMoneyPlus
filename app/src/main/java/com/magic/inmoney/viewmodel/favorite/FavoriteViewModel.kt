package com.magic.inmoney.viewmodel.favorite

import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.magic.inmoney.base.BaseViewModel
import com.magic.upcoming.games.utilities.Event

class FavoriteViewModel : BaseViewModel() {

    var initData: ObservableField<Boolean> = ObservableField(true)
    var emptyStatus: ObservableField<Boolean> = ObservableField(false)

    private val _LoadingStatus = MutableLiveData<Event<Boolean>>()
    val loadingStatus: LiveData<Event<Boolean>>
        get() = _LoadingStatus

    private fun loadingStatus() {
        initData.set(false)
        _LoadingStatus.value = Event(true)
    }
}