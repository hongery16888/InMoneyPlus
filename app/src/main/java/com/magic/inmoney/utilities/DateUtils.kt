package com.magic.inmoney.utilities

import android.annotation.SuppressLint
import android.net.ParseException
import android.text.format.Time
import com.magic.inmoney.const.WeekDay
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


object DateUtils {
    // currentTime要转换的long类型的时间 // formatType要转换的string类型的时间格式
    @Throws(ParseException::class, java.text.ParseException::class)
    fun longToString(currentTime: Long, formatType: String?): String? {
        val date = longToDate(
            currentTime,
            formatType
        )
        // long类型转成Date类型
        return dateToString(date, formatType)
    }

    @Throws(ParseException::class, java.text.ParseException::class)
    fun longToDate(currentTime: Long, formatType: String?): Date? {
        val dateOld = Date(currentTime)
        // 根据long类型的毫秒数生命一个date类型的时间
        val sDateTime = dateToString(
            dateOld,
            formatType
        ) // 把date类型的时间转换为string
        return stringToDate(
            sDateTime,
            formatType
        )
    }

    @Throws(ParseException::class, java.text.ParseException::class)
    fun stringToDate(strTime: String?, formatType: String? = "yyyy-MM-dd"): Date? {
        val formatter = SimpleDateFormat(formatType)
        var date: Date? = null
        date = formatter.parse(strTime)
        return date
    }

    @SuppressLint("SimpleDateFormat")
    fun dateToString(data: Date?, formatType: String?): String? {
        return SimpleDateFormat(formatType).format(data)
    }

    @Throws(java.text.ParseException::class)
    fun today(formatType: String?="yyyyMMdd"): String? {
        return longToString(
            System.currentTimeMillis(),
            formatType
        )
    }

    @Throws(java.text.ParseException::class)
    fun isToday(dateStr: String, formatType: String?="yyyy-MM-dd"): Boolean {
        return longToString(
            System.currentTimeMillis(),
            formatType
        ) == dateStr
    }

    @Throws(java.text.ParseException::class)
    fun beforeFiveDay(day: Long = 5): String? {
        return longToString(
            System.currentTimeMillis() - day * 24 * 60 * 60 * 1000,
            "yyyyMMdd"
        )
    }


    fun isCurrentInTimeScope(
        beginHour: Int = 9,
        beginMin: Int = 0,
        endHour: Int = 17,
        endMin: Int = 0
    ): Boolean {
        var result = false
        val aDayInMillis = 1000 * 60 * 60 * 24.toLong()
        val currentTimeMillis = System.currentTimeMillis()
        val now = Time()
        now.set(currentTimeMillis)
        val startTime = Time()
        startTime.set(currentTimeMillis)
        startTime.hour = beginHour
        startTime.minute = beginMin
        val endTime = Time()
        endTime.set(currentTimeMillis)
        endTime.hour = endHour
        endTime.minute = endMin
        if (!startTime.before(endTime)) {
            // 跨天的特殊情况（比如22:00-8:00）
            startTime.set(startTime.toMillis(true) - aDayInMillis)
            result = !now.before(startTime) && !now.after(endTime) // startTime <= now <= endTime
            val startTimeInThisDay = Time()
            startTimeInThisDay.set(startTime.toMillis(true) + aDayInMillis)
            if (!now.before(startTimeInThisDay)) {
                result = true
            }
        } else {
            // 普通情况(比如 8:00 - 14:00)
            result = !now.before(startTime) && !now.after(endTime) // startTime <= now <= endTime
        }
        return result
    }

    @SuppressLint("SimpleDateFormat")
    fun checkWorkday(dateStr: String): WeekDay{
        val format1: DateFormat = SimpleDateFormat("yyyy-MM-dd")
        val calendar = Calendar.getInstance()
        calendar.time = format1.parse(dateStr)!!
        return when {
            calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY -> WeekDay.Sunday
            calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY -> WeekDay.Saturday
            else -> WeekDay.Workday
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun isWorkday(): Boolean{
        val calendar = Calendar.getInstance()
        calendar.time = Date(System.currentTimeMillis())
        return when {
            calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY -> false
            calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY -> false
            else -> true
        }
    }

    fun minuteBetween(defaultTime: String = "09:30:00"): Int{
        val startCalendar = Calendar.getInstance()
        startCalendar.time = Date(System.currentTimeMillis())
        val year = startCalendar.get(Calendar.YEAR)
        val month = if ((startCalendar.get(Calendar.MONTH) + 1) > 9)  (startCalendar.get(Calendar.MONTH) + 1).toString()
                                        else "0" + (startCalendar.get(Calendar.MONTH) + 1)
        val day = if (startCalendar.get(Calendar.DAY_OF_MONTH)  > 9)  startCalendar.get(Calendar.DAY_OF_MONTH).toString()
        else "0" + startCalendar.get(Calendar.DAY_OF_MONTH)

        val defaultDate = stringToDate("$year$month$day $defaultTime", "yyyyMMdd HH:mm:ss")
        return ((System.currentTimeMillis() - defaultDate!!.time) / (1000 * 60)).toInt()
    }

    fun compareDate(dateTime1: String, dateTime2: String, bigStatus: Boolean = true): Boolean{

        return if (bigStatus)
            stringToDate(dateTime1)!!.time > stringToDate(dateTime2)!!.time
        else
            stringToDate(dateTime1)!!.time < stringToDate(dateTime2)!!.time

    }
}