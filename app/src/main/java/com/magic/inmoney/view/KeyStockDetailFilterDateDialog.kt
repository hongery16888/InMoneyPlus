package com.magic.inmoney.view

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.View.inflate
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.children
import androidx.databinding.DataBindingUtil
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import com.kizitonwose.calendarview.utils.yearMonth
import com.magic.inmoney.R
import com.magic.inmoney.databinding.Example1CalendarDayBinding
import com.magic.inmoney.databinding.KeyStockDetailFilterDateDialogBinding
import com.magic.inmoney.listener.OnDateSelectListener
import com.magic.inmoney.utilities.daysOfWeekFromLocale
import com.magic.inmoney.utilities.setTextColorRes
import kotlinx.coroutines.NonCancellable.children
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import kotlin.collections.ArrayList

open class KeyStockDetailFilterDateDialog : Dialog {

    private val selectedDates = mutableSetOf<LocalDate>()
    private val today = LocalDate.now()
    private val monthTitleFormatter = DateTimeFormatter.ofPattern("MMMM")
    private var binding: KeyStockDetailFilterDateDialogBinding
    private var onDateSetListener: OnDateSelectListener? = null

    constructor(context: Context) : this(context, 0)
    constructor(context: Context, themeResId: Int) : super(context, R.style.dialog) {
        val view = inflate(context, R.layout.key_stock_detail_filter_date_dialog, null)
        setContentView(view)
        window?.setGravity(Gravity.CENTER)
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        binding = DataBindingUtil.bind(view)!!

        binding.resetButton.setOnClickListener {
            selectedDates.clear()
            setCalendar()
        }

        binding.confirmButton.setOnClickListener {
            onDateSetListener?.callback(transforDate())
            transforDate()
            dismiss()
        }

        setCalendar()
    }

    @SuppressLint("SetTextI18n")
    private fun setCalendar() {

        val daysOfWeek = daysOfWeekFromLocale()

        binding.legendLayout.children.forEachIndexed { index, view ->
            (view as TextView).apply {
                text =
                    daysOfWeek[index].getDisplayName(TextStyle.SHORT, Locale.CHINESE).toUpperCase(
                        Locale.CHINESE
                    )
                setTextColorRes(R.color.example_1_white_light)
            }
        }

        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(10)
        val endMonth = currentMonth.plusMonths(10)

        binding.exOneCalendar.setup(startMonth, endMonth, daysOfWeek.first())
        binding.exOneCalendar.scrollToMonth(currentMonth)

        class DayViewContainer(view: View) : ViewContainer(view) {
            // Will be set when this container is bound. See the dayBinder.
            lateinit var day: CalendarDay
            val textView = Example1CalendarDayBinding.bind(view).exOneDayText

            init {
                view.setOnClickListener {
                    if (day.owner == DayOwner.THIS_MONTH) {
                        if (selectedDates.contains(day.date)) {
                            selectedDates.remove(day.date)
                        } else {
                            selectedDates.add(day.date)
                        }
                        binding.exOneCalendar.notifyDayChanged(day)
                    }
                }
            }
        }

        binding.exOneCalendar.dayBinder = object : DayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.day = day
                val textView = container.textView
                textView.text = day.date.dayOfMonth.toString()
                if (day.owner == DayOwner.THIS_MONTH) {
                    when {
                        selectedDates.contains(day.date) -> {
                            textView.setTextColorRes(R.color.example_1_bg)
                            textView.setBackgroundResource(R.drawable.example_1_selected_bg)
                        }
                        today == day.date -> {
                            textView.setTextColorRes(R.color.example_1_white)
                            textView.setBackgroundResource(R.drawable.example_1_today_bg)
                        }
                        else -> {
                            textView.setTextColorRes(R.color.example_1_white)
                            textView.background = null
                        }
                    }
                } else {
                    textView.setTextColorRes(R.color.example_1_white_light)
                    textView.background = null
                }
            }
        }

        binding.exOneCalendar.monthScrollListener = {
            if (binding.exOneCalendar.maxRowCount == 6) {
                binding.exOneYearText.text = it.yearMonth.year.toString()
                binding.exOneMonthText.text = monthToChinese(monthTitleFormatter.format(it.yearMonth))
            } else {
                val firstDate = it.weekDays.first().first().date
                val lastDate = it.weekDays.last().last().date
                if (firstDate.yearMonth == lastDate.yearMonth) {
                    binding.exOneYearText.text = firstDate.yearMonth.year.toString()
                    binding.exOneMonthText.text = monthTitleFormatter.format(firstDate)
                } else {
                    binding.exOneMonthText.text = monthToChinese("${monthTitleFormatter.format(firstDate)} - ${monthTitleFormatter.format(
                        lastDate
                    )}")
                    if (firstDate.year == lastDate.year) {
                        binding.exOneYearText.text = firstDate.yearMonth.year.toString()
                    } else {
                        binding.exOneYearText.text =
                            "${firstDate.yearMonth.year} - ${lastDate.yearMonth.year}"
                    }
                }
            }
        }
    }

    private fun monthToChinese(enMonth: String) : String{

        when (enMonth) {
            "January" -> {
                return "一月"
            }
            "February" -> {
                return "二月"
            }
            "March" -> {
                return "三月"
            }
            "April" -> {
                return "四月"
            }
            "May" -> {
                return "五月"
            }
            "June" -> {
                return "六月"
            }
            "July" -> {
                return "七月"
            }
            "August" -> {
                return "八月"
            }
            "September" -> {
                return "九月"
            }
            "October" -> {
                return "十月"
            }
            "November" -> {
                return "十一月"
            }
            "December" -> {
                return "十二月"
            }
        }

        return enMonth
    }

    fun setSelectDateListener(selectListener: OnDateSelectListener?): KeyStockDetailFilterDateDialog{
        onDateSetListener = selectListener
        return this
    }

    private fun transforDate(): ArrayList<String>{

        var month = ""
        var day = ""

        return ArrayList<String>().apply {
            selectedDates.forEach {
                month = if (it.monthValue > 9) it.monthValue.toString() else "0" + it.monthValue
                day = if (it.dayOfMonth > 9) it.dayOfMonth.toString() else "0" + it.dayOfMonth
                add("${it.year}-$month-$day")
            }
        }


    }
}