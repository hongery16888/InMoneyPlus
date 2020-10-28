package com.magic.inmoney.orm

import android.content.Context
import com.magic.inmoney.model.StockBaseInfo
import com.magic.inmoney.model.StockReport
import jxl.Workbook
import org.litepal.extension.saveAll

object StockInfoDB {
    fun updateStockInfo(context: Context): Boolean {
        return try {
            val inputStream = context.assets.open("StockInfo.xls")
            val book = Workbook.getWorkbook(inputStream)
            book.numberOfSheets
            val sheet = book.getSheet(0)
            val rows = sheet.rows
            ArrayList<StockBaseInfo>().apply {
                for (i in 1 until rows) {
                    add(StockBaseInfo().apply {
                        stockCode = sheet.getCell(0, i).contents
                        stockName = sheet.getCell(1, i).contents
                        per = sheet.getCell(2, i).contents
                        industry = sheet.getCell(3, i).contents
                        pbr = sheet.getCell(4, i).contents
                        capitalization = sheet.getCell(5, i).contents
                        totalMarketValue = sheet.getCell(6, i).contents
                    })
                }
            }.saveAll()
            book.close()
            true
        } catch (e: Exception) {
            println("----------->Read Excel Exception : " + e.message)
            false
        }
    }

    fun updateStockReport(context: Context): Boolean {
        return try {
            val inputStream = context.assets.open("StockReport.xls")
            val book = Workbook.getWorkbook(inputStream)
            book.numberOfSheets
            val sheet = book.getSheet(0)
            val rows = sheet.rows
            ArrayList<StockReport>().apply {
                for (i in 1 until rows) {
                    add(StockReport().apply {
                        stockCode = checkStockCode(sheet.getCell(0, i).contents)
                        stockName = sheet.getCell(1, i).contents
                        earningPerShare = sheet.getCell(2, i).contents
                        revenue = sheet.getCell(3, i).contents
                        onYearGrowthRevenue = sheet.getCell(4, i).contents
                        onMonthGrowthRevenue = sheet.getCell(5, i).contents
                        netProfit = sheet.getCell(6, i).contents
                        onYearGrowthNetProfit = sheet.getCell(7, i).contents
                        onMonthGrowthNetProfit = sheet.getCell(8, i).contents
                        netAssetValuePreShare = sheet.getCell(9, i).contents
                        onNetAssetsRate = sheet.getCell(10, i).contents
                        cashFlowPerShare = sheet.getCell(11, i).contents
                        grossProfitMarginRate = sheet.getCell(12, i).contents
                        profitDistribution = sheet.getCell(13, i).contents
                        industry = sheet.getCell(14, i).contents
                    })
                }
            }.saveAll()
            book.close()
            true
        } catch (e: Exception) {
            println("----------->Read Excel Exception : " + e.message)
            false
        }
    }

    private fun checkStockCode(stockCode: String) : String{
        return when(stockCode.length){
            6 -> stockCode
            5 -> "0$stockCode"
            4 -> "00$stockCode"
            3 -> "000$stockCode"
            2 -> "0000$stockCode"
            else -> "00000$stockCode"
        }
    }
}