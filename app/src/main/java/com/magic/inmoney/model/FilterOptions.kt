package com.magic.inmoney.model

import org.litepal.crud.LitePalSupport

class FilterOptions : LitePalSupport(){

    var name = "filterOptions"
    var stockTypes: ArrayList<String> = ArrayList()
    var startRate = 0
    var endRate = 0
    var sortType = "stockRate"
    var sortDirection = "desc"
    var beforeDay = 0
    var promptType = "music"
    var buyPoint = "any"
    var throughType = "normalThrough"
    var volumeStartRate = 0
    var volumeEndRate = 0
    var turnoverStartRate = 0
    var turnoverEndRate = 8
    var needTrendPrompt = false
}