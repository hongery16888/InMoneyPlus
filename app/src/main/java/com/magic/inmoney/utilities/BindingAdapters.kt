package com.magic.inmoney.utilities

import android.widget.ImageView
import android.widget.RadioGroup
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.magic.inmoney.R
import com.magic.inmoney.base.BaseApplication
import com.magic.inmoney.const.*


@BindingAdapter("sortDirection")
fun RadioGroup.bindSortDirection(sortDirection: String) {
    val newCheckedId = when (sortDirection) {
        StockSortDirection.Desc.info -> R.id.sort_desc_radio
        else -> R.id.sort_asc_radio
    }

    println("------------------>SortDirection onChange : $sortDirection")

    BaseApplication.instance?.filterOptions?.sortDirection = sortDirection

    if (checkedRadioButtonId != newCheckedId) {
        check(newCheckedId)
    }
}

@InverseBindingAdapter(attribute = "sortDirection")
fun RadioGroup.setSortDirection(): String {
    return when (checkedRadioButtonId) {
        R.id.sort_desc_radio -> StockSortDirection.Desc.info
        else -> StockSortDirection.Asc.info
    }
}

@BindingAdapter("sortDirectionAttrChanged")
fun RadioGroup.setSortDirectionListeners(listener: InverseBindingListener) {
    setOnCheckedChangeListener { _, _ ->
        listener.onChange()
    }
}

@BindingAdapter("sortType")
fun RadioGroup.bindSortType(sortType: String) {
    val newCheckedId = when (sortType) {
        StockSortType.StockCode.info -> R.id.sort_stock_code
        StockSortType.StockName.info -> R.id.sort_stock_name
        StockSortType.StockRate.info -> R.id.sort_stock_rate
        StockSortType.TurnoverRate.info -> R.id.sort_turnover_rate
        else -> R.id.sort_trade_num
    }

    println("------------------>SortType onChange : $sortType")

    BaseApplication.instance?.filterOptions?.sortType = sortType

    if (checkedRadioButtonId != newCheckedId) {
        check(newCheckedId)
    }
}

@InverseBindingAdapter(attribute = "sortType")
fun RadioGroup.setSortType(): String {
    return when (checkedRadioButtonId) {
        R.id.sort_stock_code -> StockSortType.StockCode.info
        R.id.sort_stock_name -> StockSortType.StockName.info
        R.id.sort_stock_rate -> StockSortType.StockRate.info
        R.id.sort_turnover_rate -> StockSortType.TurnoverRate.info
        else -> StockSortType.TradeNum.info
    }
}

@BindingAdapter("sortTypeAttrChanged")
fun RadioGroup.setSortTypeListeners(listener: InverseBindingListener) {
    setOnCheckedChangeListener { _, _ ->
        listener.onChange()
    }
}

@BindingAdapter("promptType")
fun RadioGroup.bindPromptType(promptType: String) {
    val newCheckedId = when (promptType) {
        KeyStockPrompt.Voice.info -> R.id.voice_type
        KeyStockPrompt.Music.info -> R.id.music_type
        else -> R.id.none_type
    }

    println("------------------>PromptType onChange : $promptType")

    BaseApplication.instance?.filterOptions?.promptType = promptType

    if (checkedRadioButtonId != newCheckedId) {
        check(newCheckedId)
    }
}

@InverseBindingAdapter(attribute = "promptType")
fun RadioGroup.setPromptType(): String {
    return when (checkedRadioButtonId) {
        R.id.voice_type -> KeyStockPrompt.Voice.info
        R.id.music_type -> KeyStockPrompt.Music.info
        else -> KeyStockPrompt.None.info
    }
}

@BindingAdapter("promptTypeAttrChanged")
fun RadioGroup.setPromptTypeListeners(listener: InverseBindingListener) {
    setOnCheckedChangeListener { _, _ ->
        listener.onChange()
    }
}

@BindingAdapter("buyPoint")
fun RadioGroup.bindBuyPoint(buyPoint: String) {
    val newCheckedId = when (buyPoint) {
        BuyPointPrompt.UpLine.point -> R.id.up_line_point
        BuyPointPrompt.MidLine.point -> R.id.mid_line_point
        BuyPointPrompt.DownLine.point -> R.id.down_line_point
        else -> R.id.any_line_point
    }

    println("------------------>PromptType onChange : $buyPoint")

    BaseApplication.instance?.filterOptions?.buyPoint = buyPoint

    if (checkedRadioButtonId != newCheckedId) {
        check(newCheckedId)
    }
}

@InverseBindingAdapter(attribute = "buyPoint")
fun RadioGroup.setBuyPoint(): String {
    return when (checkedRadioButtonId) {
        R.id.up_line_point -> BuyPointPrompt.UpLine.point
        R.id.mid_line_point -> BuyPointPrompt.MidLine.point
        R.id.down_line_point -> BuyPointPrompt.DownLine.point
        else -> BuyPointPrompt.AnyLine.point
    }
}

@BindingAdapter("buyPointAttrChanged")
fun RadioGroup.setBuyPointListeners(listener: InverseBindingListener) {
    setOnCheckedChangeListener { _, _ ->
        listener.onChange()
    }
}

@BindingAdapter("throughType")
fun RadioGroup.bindThroughType(throughType: String) {
    val newCheckedId = when (throughType) {
        ThroughType.NormalThrough.type -> R.id.normal_through
        ThroughType.HighThrough.type -> R.id.high_through
        ThroughType.ThroughAndTrade.type -> R.id.through_trade
        ThroughType.HighThroughAndTrade.type -> R.id.high_through_trade
        else -> R.id.normal_through
    }

    println("------------------>PromptType onChange : $throughType")

    BaseApplication.instance?.filterOptions?.throughType = throughType

    if (checkedRadioButtonId != newCheckedId) {
        check(newCheckedId)
    }
}

@InverseBindingAdapter(attribute = "throughType")
fun RadioGroup.setThroughType(): String {
    return when (checkedRadioButtonId) {
        R.id.normal_through -> ThroughType.NormalThrough.type
        R.id.high_through -> ThroughType.HighThrough.type
        R.id.through_trade -> ThroughType.ThroughAndTrade.type
        R.id.high_through_trade -> ThroughType.HighThroughAndTrade.type
        else -> ThroughType.NormalThrough.type
    }
}

@BindingAdapter("throughTypeAttrChanged")
fun RadioGroup.setThroughTypeListeners(listener: InverseBindingListener) {
    setOnCheckedChangeListener { _, _ ->
        listener.onChange()
    }
}


@BindingAdapter("imageUrl")
fun ImageView.bindImage(stockCode: String?) {

    val klineUrl = when(id){
        R.id.min_kline_image -> "http://image.sinajs.cn/newchart/min/n/$stockCode.gif"
        R.id.daily_kline_image -> "http://image.sinajs.cn/newchart/daily/n/$stockCode.gif"
        R.id.weekly_kline_image -> "http://image.sinajs.cn/newchart/weekly/n/$stockCode.gif"
        else -> "http://image.sinajs.cn/newchart/monthly/n/$stockCode.gif"
    }

    klineUrl.let {
        Glide.with(context)
            .asGif()
            .load(it)
            .fitCenter()
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.loading_animation)
                    .error(R.drawable.broken_image)
            )
            .into(this)
    }

    @BindingAdapter("promptButton")
    fun ImageView.bindImage(stockCode: String?) {
            Glide.with(context)
                .asGif()
                .load(R.drawable.ic_prompt_button)
                .fitCenter()
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.loading_animation)
                        .error(R.drawable.broken_image)
                )
                .into(this)
    }
}

