package com.magic.inmoney.model

import com.google.gson.annotations.SerializedName

internal class ResBodyBean{
    @SerializedName("allPages")
    var allPages = 0

    @SerializedName("ret_code")
    var retCode = 0

    @SerializedName("currentPage")
    var currentPage = 0

    @SerializedName("allNum")
    var allNum = 0

    @SerializedName("maxResult")
    var maxResult = 0

    @SerializedName("contentlist")
    var contentlist: ArrayList<StockModel>? = null
}