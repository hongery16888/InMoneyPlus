package com.magic.inmoney.utilities

import java.lang.StringBuilder

object SqlFormat {

    fun sqlFormatKLineAndDate(kLines: ArrayList<String>, dates: ArrayList<String>): String {

        val stringBuilder = StringBuilder()

        if (kLines.size > 0) {

            stringBuilder.append("(")

            kLines.forEach {
                stringBuilder.append(" kLineType = '$it' ").append("or")
            }

            stringBuilder.delete(stringBuilder.count() - 3, stringBuilder.count()).append(")")
        }

        if (kLines.size > 0 && dates.size > 0)
            stringBuilder.append(" and ")

        if (dates.size > 0){
            stringBuilder.append("(")

            dates.forEach {
                stringBuilder.append(" stockAddTime = '$it' ").append("or")
            }

            stringBuilder.delete(stringBuilder.count() - 3, stringBuilder.count()).append(")")
        }

        return stringBuilder.toString()
    }

}