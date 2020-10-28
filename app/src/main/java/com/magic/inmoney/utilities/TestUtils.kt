package com.magic.inmoney.utilities

import com.github.fujianlian.klinechart.KLineEntity
import com.magic.inmoney.base.BaseApplication
import com.magic.inmoney.const.QualityType
import com.magic.inmoney.const.ThroughType
import com.magic.inmoney.model.StatisticsModel
import com.magic.inmoney.model.StockItemModel
import com.magic.inmoney.orm.LitePalDBase
import java.text.DecimalFormat
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object TestUtils {

    private var decimalFormat: DecimalFormat = DecimalFormat("#0.0")
    private var decimalFormat3: DecimalFormat = DecimalFormat("#0.00")

    //判断突破5日均线后，回踩后，反抽的成功情况
    fun judgeFiveSuccessRate(
        stockCode: String,
        stockName: String,
        datas: ArrayList<KLineEntity>
    ): StatisticsModel {
        if (datas.size < 12) return StatisticsModel()

        var data = ArrayList<KLineEntity>().apply { addAll(datas) }

        var successHighCount = 0
        var successCloseCount = 0
        var success1PointCount = 0
        var success3PointCount = 0
        var successCloseProfit = 0f
        var failureHighCount = 0
        var failureCloseCount = 0
        var failure1PointCount = 0
        var failure3PointCount = 0
        var failureCloseProfit = 0f
        var stockCostPrice: Float
        var keyCount = 0
        var keyFiveCount = 0
        var nextDayCount = 0
        var thirdDayCount = 0
        var runCount = 0
        var maxProfit = 0f
        var mixProfit = 0f

        var testSuccessHighTradeCount = 0
        var testSuccessLowTradeCount = 0

        var testFailureHighTradeCount = 0
        var testFailureLowTradeCount = 0

        var successDate = ArrayList<String>()
        var failureDate = ArrayList<String>()

        var dustCount = 0
        var dustDate = ArrayList<String>()

        for (i in 10..(data.size - 4)) {
            runCount += 1
            if ((data[i].Close - data[i - 1].Close) / data[i - 1].Close > 0.045f) {
                keyCount += 1

                val status = when (BaseApplication.instance?.filterOptions?.throughType) {
                    ThroughType.NormalThrough.type -> data[i].Open < data[i].mA5Price && data[i].Close > data[i].mA5Price
                    ThroughType.HighThrough.type -> data[i].Open < data[i].mA5Price && data[i].Close > data[i].mA5Price &&
                            data[i].Close - data[i].mA5Price > data[i].mA5Price - data[i].Open
                    ThroughType.ThroughAndTrade.type -> data[i].Open < data[i].mA5Price && data[i].Close > data[i].mA5Price &&
                            data[i].Volume > data[i - 1].Volume * 2
                    ThroughType.HighThroughAndTrade.type -> data[i].Open < data[i].mA5Price && data[i].Close > data[i].mA5Price &&
                            data[i].Close - data[i].mA5Price > data[i].mA5Price - data[i].Open &&
                            data[i].Volume > data[i - 1].Volume * 2
                    else -> data[i].Open < data[i].mA5Price && data[i].Close > data[i].mA5Price
                }

                if (status) {

                    if (abs(data[i].MA5Price - data[i - 10].MA5Price) / data[i - 10].MA5Price > 0.25f) {
                        dustCount += 1
                        dustDate.add(data[i].Date)
                    }

                    keyFiveCount += 1
                    if (data[i + 1].High > data[i + 1].mA5Price && data[i + 1].Low < data[i + 1].mA5Price) {
                        nextDayCount += 1
                        stockCostPrice = data[i + 1].mA5Price

                        if (data[i + 1].Volume > data[i].Volume && data[i + 1].Open > data[i + 1].Close) {
                            dustCount += 1
                            dustDate.add(data[i].Date)
                        }

                        if (data[i + 2].High > stockCostPrice) {
                            successHighCount += 1
                            maxProfit = max(
                                maxProfit,
                                decimalFormat.format((data[i + 2].High - stockCostPrice) / stockCostPrice * 100)
                                    .toFloat()
                            )
                            successDate.add(data[i].Date)
                            if (data[i + 1].Volume / data[i].Open > 1) {
                                testSuccessHighTradeCount += 1
                            } else {
                                testSuccessLowTradeCount += 1
                            }
                        } else {
                            failureHighCount += 1
                            failureDate.add(data[i].Date)

                            if (data[i + 1].Volume / data[i].Volume > 1) {
                                testFailureHighTradeCount += 1
                            } else {
                                testFailureLowTradeCount += 1
                            }
                            mixProfit = min(
                                mixProfit,
                                decimalFormat.format((data[i + 2].Low - stockCostPrice) / stockCostPrice * 100)
                                    .toFloat()
                            )
                        }
                        if (data[i + 2].Close > stockCostPrice) {
                            successCloseCount += 1
                            successCloseProfit += decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100)
                                .toFloat()
                        } else {
                            failureCloseCount += 1
                            failureCloseProfit += decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100)
                                .toFloat()
                        }

                        if (data[i + 2].High > data[i + 1].Close && (data[i + 2].High - stockCostPrice) / stockCostPrice > 0.01f) {
                            success1PointCount += 1
                        }

                        if (data[i + 2].High < data[i + 1].Close && (data[i + 2].Low - stockCostPrice) / stockCostPrice < -0.01f) {
                            failure1PointCount += 1
                        }

                        if (data[i + 2].High > data[i + 1].Close && (data[i + 2].High - stockCostPrice) / stockCostPrice > 0.03f) {
                            success3PointCount += 1
                        }

                        if (data[i + 2].High < data[i + 1].Close && (data[i + 2].Low - stockCostPrice) / stockCostPrice < -0.03f) {
                            failure1PointCount += 1
                        }

                    } else if (data[i + 2].High > data[i + 2].mA5Price && data[i + 2].Low < data[i + 2].mA5Price) {
                        thirdDayCount += 1
                        stockCostPrice = data[i + 1].mA5Price

                        if (data[i + 2].Volume > data[i].Volume && data[i + 2].Open > data[i + 2].Close) {
                            dustCount += 1
                            dustDate.add(data[i].Date)
                        }

                        if (data[i + 3].High > stockCostPrice) {
                            successHighCount += 1
                            maxProfit = max(
                                maxProfit,
                                decimalFormat.format((data[i + 3].High - stockCostPrice) / stockCostPrice * 100)
                                    .toFloat()
                            )
                            successDate.add(data[i].Date)
                        } else {
                            failureHighCount += 1
                            failureDate.add(data[i].Date)
                            mixProfit = min(
                                mixProfit,
                                decimalFormat.format((data[i + 3].Close - stockCostPrice) / stockCostPrice * 100)
                                    .toFloat()
                            )
                        }

                        if (data[i + 3].Close > stockCostPrice) {
                            successCloseCount += 1
                            successCloseProfit += decimalFormat.format((data[i + 3].Close - stockCostPrice) / stockCostPrice * 100)
                                .toFloat()
                        } else {
                            failureCloseCount += 1
                            failureCloseProfit += decimalFormat.format((data[i + 3].Close - stockCostPrice) / stockCostPrice * 100)
                                .toFloat()
                        }
                        if (data[i + 3].High > data[i + 2].Close && (data[i + 3].High - stockCostPrice) / stockCostPrice > 0.01f) {
                            success1PointCount += 1
                        }

                        if (data[i + 3].High < data[i + 2].Close && (data[i + 3].Low - stockCostPrice) / stockCostPrice < -0.01f) {
                            failure1PointCount += 1
                        }

                        if (data[i + 3].High > data[i + 2].Close && (data[i + 3].High - stockCostPrice) / stockCostPrice > 0.03f) {
                            success3PointCount += 1
                        }

                        if (data[i + 3].High < data[i + 2].Close && (data[i + 3].Low - stockCostPrice) / stockCostPrice < -0.03f) {
                            failure3PointCount += 1
                        }
                    }
                }
            }
        }

        println("------------------>总运行次数 $runCount")
        println("------------------>超5%的涨幅次数 $keyCount")
        println("------------------>超5%并穿过5日均线次数 $keyFiveCount")
        println("------------------>第二天达标的次数 $nextDayCount")
        println("------------------>第三天达标的次数 $thirdDayCount")

        println("------------------>第二天有高点的个数为 $successHighCount")
        println("------------------>第二天收盘价盈利个数为 $successCloseCount")
        println("------------------>第二天超过1%的盈利个数为 $success1PointCount")
        println("------------------>第二天超过3%的盈利个数为 $success3PointCount")
        println("------------------>第二天最高收益为$maxProfit%")

        println("------------------>第二天有低点的个数为 $failureHighCount")
        println("------------------>第二天收盘价亏损个数为 $failureCloseCount")
        println("------------------>第二天超过1%的亏损个数为 $failure1PointCount")
        println("------------------>第二天超过3%的亏损个数为 $failure3PointCount")
        println("------------------>第二天最低亏损为$mixProfit%")

        println("------------------>第二天有新高时量比大 $testSuccessHighTradeCount")
        println("------------------>第二天有新高时量比小 $testSuccessLowTradeCount")
        println("------------------>第二天强行亏损量比大 $testFailureHighTradeCount")
        println("------------------>第二天强行亏损量比小 $testFailureLowTradeCount")

        println("------------------>收盘价盈利总百分比例为 $successCloseProfit%")
        println("------------------>收盘价亏损总百分比例为 $failureCloseProfit%")

        println("------------------>垃圾位置点 $dustCount")
        println("------------------>成功时间点 $successDate")
        println("------------------>失败时间点 $failureDate")
        val temp = LinkedHashSet<String>().apply {
            addAll(dustDate)
        }
        dustDate.clear()
        dustDate.addAll(temp)
        println("------------------>垃圾时间点 $dustDate")

        var statisticsModel = StatisticsModel().apply {
            this.stockCode = stockCode
            this.stockName = stockName
            this.successHighCount = successHighCount
            this.successCloseCount = successCloseCount
            this.successCloseProfit = successCloseProfit
            this.success1PointCount = success1PointCount
            this.success3PointCount = success3PointCount
            this.failureHighCount = failureHighCount
            this.failureCloseCount = failureCloseCount
            this.failureCloseProfit = failureCloseProfit
            this.failure1PointCount = failure1PointCount
            this.failure3PointCount = failure3PointCount
            this.keyCount = nextDayCount + thirdDayCount
            this.maxProfit = maxProfit
            this.minProfit = mixProfit
        }

        statisticsModel.save()

        return statisticsModel
    }

    //判断突破中间均线后，回踩后，反抽的成功情况
    fun judgeFiveSuccessRateForMinLine(
        stockCode: String,
        stockName: String,
        datas: ArrayList<KLineEntity>,
        statisticsType: String,
        line: Int = 0,
        needThirdDay: Boolean = true
    ): StatisticsModel {
        if (datas.size < 12) return StatisticsModel()

        var data = ArrayList<KLineEntity>().apply { addAll(datas) }

        var successHighCount = 0
        var successCloseCount = 0
        var success1PointCount = 0
        var success3PointCount = 0
        var successCloseProfit = 0f
        var failureHighCount = 0
        var failureCloseCount = 0
        var failure1PointCount = 0
        var failure3PointCount = 0
        var failureCloseProfit = 0f
        var stockCostPrice: Float
        var keyCount = 0
        var keyFiveCount = 0
        var nextDayCount = 0
        var thirdDayCount = 0
        var runCount = 0
        var maxProfit = 0f
        var mixProfit = 0f

        var testSuccessHighTradeCount = 0
        var testSuccessLowTradeCount = 0

        var testFailureHighTradeCount = 0
        var testFailureLowTradeCount = 0

        var successDate = ArrayList<String>()
        var failureDate = ArrayList<String>()

        var dustCount = 0
        var dustDate = ArrayList<String>()

        for (i in 10..(data.size - 4)) {
            runCount += 1
            if ((data[i].Close - data[i - 1].Close) / data[i - 1].Close > 0.045f) {
                keyCount += 1

                val status = when (BaseApplication.instance?.filterOptions?.throughType) {
                    ThroughType.NormalThrough.type -> data[i].Open < data[i].mA5Price && data[i].Close > data[i].mA5Price
                    ThroughType.HighThrough.type -> data[i].Open < data[i].mA5Price && data[i].Close > data[i].mA5Price &&
                            data[i].Close - data[i].mA5Price > data[i].mA5Price - data[i].Open
                    ThroughType.ThroughAndTrade.type -> data[i].Open < data[i].mA5Price && data[i].Close > data[i].mA5Price &&
                            data[i].Volume > data[i - 1].Volume * 2
                    ThroughType.HighThroughAndTrade.type -> data[i].Open < data[i].mA5Price && data[i].Close > data[i].mA5Price &&
                            data[i].Close - data[i].mA5Price > data[i].mA5Price - data[i].Open &&
                            data[i].Volume > data[i - 1].Volume * 2
                    else -> data[i].Open < data[i].mA5Price && data[i].Close > data[i].mA5Price
                }

                if (status) {

                    if (abs(data[i].MA5Price - data[i - 10].MA5Price) / data[i - 10].MA5Price > 0.25f) {
                        dustCount += 1
                        dustDate.add(data[i].Date)
                    }

                    val sortAverage = ArrayList<Float>().apply {
                        add(data[i + 1].mA5Price)
                        add(data[i + 1].mA8Price)
                        add(data[i + 1].mA13Price)
                    }

                    sortAverage.sort()

                    val linePrice = sortAverage[line]

                    keyFiveCount += 1
                    if (data[i + 1].High > linePrice && data[i + 1].Low < linePrice) {
                        nextDayCount += 1
                        stockCostPrice = linePrice

                        if (data[i + 1].Volume > data[i].Volume && data[i + 1].Open > data[i + 1].Close) {
                            dustCount += 1
                            dustDate.add(data[i].Date)
                        } else {

                            if (data[i + 2].High > stockCostPrice) {
                                successHighCount += 1
                                maxProfit += decimalFormat.format((data[i + 2].High - stockCostPrice) / stockCostPrice * 100)
                                    .toFloat()
                                successDate.add(data[i].Date)
                                if (data[i + 1].Volume / data[i].Volume > 1) {
                                    testSuccessHighTradeCount += 1
                                } else {
                                    testSuccessLowTradeCount += 1
                                }
                            } else {
                                failureHighCount += 1
                                failureDate.add(data[i].Date)

                                if (data[i + 1].Volume / data[i].Volume > 1) {
                                    testFailureHighTradeCount += 1
                                } else {
                                    testFailureLowTradeCount += 1
                                }
                                mixProfit += decimalFormat.format((data[i + 2].Low - stockCostPrice) / stockCostPrice * 100)
                                    .toFloat()
                            }
                            if (data[i + 2].Close > stockCostPrice) {
                                successCloseCount += 1
                                successCloseProfit += decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100)
                                    .toFloat()
                            } else {
                                failureCloseCount += 1
                                failureCloseProfit += decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100)
                                    .toFloat()
                            }

                            if ((data[i + 2].High - stockCostPrice) / stockCostPrice > 0.01f) {
                                success1PointCount += 1
                            }

                            if ((data[i + 2].Low - stockCostPrice) / stockCostPrice < -0.01f) {
                                failure1PointCount += 1
                            }

                            if ((data[i + 2].High - stockCostPrice) / stockCostPrice > 0.03f) {
                                success3PointCount += 1
                            }

                            if ((data[i + 2].Low - stockCostPrice) / stockCostPrice < -0.03f) {
                                failure1PointCount += 1
                            }
                        }
                    } else if (data[i + 2].High > linePrice && data[i + 2].Low < linePrice && needThirdDay) {
                        thirdDayCount += 1
                        stockCostPrice = linePrice

                        if (data[i + 2].Volume > data[i].Volume && data[i + 2].Open > data[i + 2].Close) {
                            dustCount += 1
                            dustDate.add(data[i].Date)
                        }

                        if (data[i + 3].High > stockCostPrice) {
                            successHighCount += 1
                            maxProfit += decimalFormat.format((data[i + 3].High - stockCostPrice) / stockCostPrice * 100)
                                .toFloat()
                            successDate.add(data[i].Date)
                        } else {
                            failureHighCount += 1
                            failureDate.add(data[i].Date)
                            mixProfit += decimalFormat.format((data[i + 3].Close - stockCostPrice) / stockCostPrice * 100)
                                .toFloat()
                        }

                        if (data[i + 3].Close > stockCostPrice) {
                            successCloseCount += 1
                            successCloseProfit += decimalFormat.format((data[i + 3].Close - stockCostPrice) / stockCostPrice * 100)
                                .toFloat()
                        } else {
                            failureCloseCount += 1
                            failureCloseProfit += decimalFormat.format((data[i + 3].Close - stockCostPrice) / stockCostPrice * 100)
                                .toFloat()
                        }
                        if ((data[i + 3].High - stockCostPrice) / stockCostPrice > 0.01f) {
                            success1PointCount += 1
                        }

                        if ((data[i + 3].Low - stockCostPrice) / stockCostPrice < -0.01f) {
                            failure1PointCount += 1
                        }

                        if ((data[i + 3].High - stockCostPrice) / stockCostPrice > 0.03f) {
                            success3PointCount += 1
                        }

                        if ((data[i + 3].Low - stockCostPrice) / stockCostPrice < -0.03f) {
                            failure3PointCount += 1
                        }
                    }
                }
            }
        }

        println("------------------>总运行次数 $runCount")
        println("------------------>超5%的涨幅次数 $keyCount")
        println("------------------>超5%并穿过5日均线次数 $keyFiveCount")
        println("------------------>第二天达标的次数 $nextDayCount")
        println("------------------>第三天达标的次数 $thirdDayCount")

        println("------------------>第二天有高点的个数为 $successHighCount")
        println("------------------>第二天收盘价盈利个数为 $successCloseCount")
        println("------------------>第二天超过1%的盈利个数为 $success1PointCount")
        println("------------------>第二天超过3%的盈利个数为 $success3PointCount")
        println("------------------>第二天最高收益为$maxProfit%")

        println("------------------>第二天有低点的个数为 $failureHighCount")
        println("------------------>第二天收盘价亏损个数为 $failureCloseCount")
        println("------------------>第二天超过1%的亏损个数为 $failure1PointCount")
        println("------------------>第二天超过3%的亏损个数为 $failure3PointCount")
        println("------------------>第二天最低亏损为$mixProfit%")

        println("------------------>第二天有新高时量比大 $testSuccessHighTradeCount")
        println("------------------>第二天有新高时量比小 $testSuccessLowTradeCount")
        println("------------------>第二天强行亏损量比大 $testFailureHighTradeCount")
        println("------------------>第二天强行亏损量比小 $testFailureLowTradeCount")

        println("------------------>收盘价盈利总百分比例为 $successCloseProfit%")
        println("------------------>收盘价亏损总百分比例为 $failureCloseProfit%")

        println("------------------>垃圾位置点 $dustCount")
        println("------------------>成功时间点 $successDate")
        println("------------------>失败时间点 $failureDate")
        val temp = LinkedHashSet<String>().apply {
            addAll(dustDate)
        }
        dustDate.clear()
        dustDate.addAll(temp)
        println("------------------>垃圾时间点 $dustDate")

        var statisticsModel = StatisticsModel().apply {
            this.stockCode = stockCode
            this.stockName = stockName
            this.successHighCount = successHighCount
            this.successCloseCount = successCloseCount
            this.successCloseProfit = successCloseProfit
            this.success1PointCount = success1PointCount
            this.success3PointCount = success3PointCount
            this.failureHighCount = failureHighCount
            this.failureCloseCount = failureCloseCount
            this.failureCloseProfit = failureCloseProfit
            this.failure1PointCount = failure1PointCount
            this.failure3PointCount = failure3PointCount
            this.keyCount = nextDayCount + thirdDayCount
            this.maxProfit = maxProfit
            this.minProfit = mixProfit
            this.statisticsType = statisticsType
        }

        statisticsModel.save()

        return statisticsModel
    }

    //判断一阳穿三线突破中间均线后，回踩后，反抽的成功情况
    fun judgeFiveSuccessRateForMinLine3Three(
        stockCode: String,
        stockName: String,
        datas: ArrayList<KLineEntity>,
        statisticsType: String,
        line: Int = 0,
        needThirdDay: Boolean = true
    ): StatisticsModel {
        if (datas.size < 12) return StatisticsModel()

        val data = ArrayList<KLineEntity>().apply { addAll(datas) }

        var successHighCount = 0
        var successCloseCount = 0
        var success1PointCount = 0
        var success2PointCount = 0
        var success3PointCount = 0
        var success4PointCount = 0
        var success5PointCount = 0
        var successCloseProfit = 0f
        var failureHighCount = 0
        var failureCloseCount = 0
        var failure1PointCount = 0
        var failure3PointCount = 0
        var failureCloseProfit = 0f
        var stockCostPrice: Float
        var keyCount = 0
        var keyFiveCount = 0
        var nextDayCount = 0
        var thirdDayCount = 0
        var runCount = 0
        var maxProfit = 0f
        var mixProfit = 0f

        var testSuccessHighTradeCount = 0
        var testSuccessLowTradeCount = 0

        var testFailureHighTradeCount = 0
        var testFailureLowTradeCount = 0

        var successDate = ArrayList<String>()
        var failureDate = ArrayList<String>()

        var dustCount = 0
        var dustDate = ArrayList<String>()

        var addTrade = false

        val currcapital = LitePalDBase.queryCurrcapital(stockCode)

        for (i in 15..(data.size - 4)) {
            runCount += 1
            if ((data[i].Close - data[i - 1].Close) / data[i - 1].Close > BaseApplication.instance?.filterOptions?.startRate!! / 100f &&
                (data[i].Close - data[i - 1].Close) / data[i - 1].Close < BaseApplication.instance?.filterOptions?.endRate!! / 100f
            ) {

                val status = when (BaseApplication.instance?.filterOptions?.throughType) {
                    ThroughType.NormalThrough.type -> data[i].Open < data[i].mA5Price && data[i].Close > data[i].mA5Price &&
                            data[i].Open < data[i].mA8Price && data[i].Close > data[i].mA8Price &&
                            data[i].Open < data[i].mA13Price && data[i].Close > data[i].mA13Price
                    ThroughType.HighThrough.type -> data[i].Open < data[i].mA5Price && data[i].Close > data[i].mA5Price &&
                            data[i].Open < data[i].mA8Price && data[i].Close > data[i].mA8Price &&
                            data[i].Open < data[i].mA13Price && data[i].Close > data[i].mA13Price &&
                            data[i].Close - (data[i].mA5Price + data[i].mA8Price + data[i].mA13Price) / 3 > (data[i].mA5Price + data[i].mA8Price + data[i].mA13Price) / 3 - data[i].Open
                    ThroughType.ThroughAndTrade.type -> data[i].Open < data[i].mA5Price && data[i].Close > data[i].mA5Price &&
                            data[i].Open < data[i].mA8Price && data[i].Close > data[i].mA8Price &&
                            data[i].Open < data[i].mA13Price && data[i].Close > data[i].mA13Price &&
                            data[i].Volume > data[i - 1].Volume * 2
                    ThroughType.HighThroughAndTrade.type -> data[i].Open < data[i].mA5Price && data[i].Close > data[i].mA5Price &&
                            data[i].Open < data[i].mA8Price && data[i].Close > data[i].mA8Price &&
                            data[i].Open < data[i].mA13Price && data[i].Close > data[i].mA13Price &&
                            data[i].Close - (data[i].mA5Price + data[i].mA8Price + data[i].mA13Price) / 3 > (data[i].mA5Price + data[i].mA8Price + data[i].mA13Price) / 3 - data[i].Open &&
                            data[i].Volume > data[i - 1].Volume * 2
                    else -> data[i].Open < data[i].mA5Price && data[i].Close > data[i].mA5Price &&
                            data[i].Open < data[i].mA8Price && data[i].Close > data[i].mA8Price &&
                            data[i].Open < data[i].mA13Price && data[i].Close > data[i].mA13Price
                }

                //                    if (needThirdDay){
//                        (data[i].volume / 240f).toInt() / ((data[i - 1].volume +  data[i - 2].volume + data[i - 3].volume + data[i - 4].volume + data[i - 5].volume  + data[i - 6].volume  + data[i - 7].volume  + data[i - 8].volume  + data[i - 9].volume  + data[i - 10].volume) / 2400f).toInt()
//                    }else{
//                    }

                val volumeRate = (data[i].volume / 240f).toInt()
                    .toFloat() / ((data[i - 1].volume + data[i - 2].volume + data[i - 3].volume + data[i - 4].volume + data[i - 5].volume) / 1200f).toInt()
                    .toFloat()

                if (needThirdDay && volumeRate < BaseApplication.instance?.filterOptions?.volumeStartRate!! * 0.1)
                    continue

                if (currcapital > 0 && (data[i].volume / currcapital > BaseApplication.instance?.filterOptions?.turnoverStartRate!! || data[i].volume / currcapital < BaseApplication.instance?.filterOptions?.turnoverEndRate!!))
                    continue

                val upShadowPoint = if (data[i].Close >= data[i].Open) {
                    data[i].High - data[i].Close
                } else
                    data[i].High - data[i].Open

                if (status &&
                    (upShadowPoint / data[i - 1].Close) < 0.02
                ) {


                    var decimalFormat3: DecimalFormat = DecimalFormat("#0.000")

                    if (abs(data[i].MA5Price - data[i - 10].MA5Price) / data[i - 10].MA5Price > 0.25f) {
                        dustCount += 1
                        dustDate.add(data[i].Date)
                    }

                    data[i + 1].MA5Price =
                        decimalFormat3.format((data[i - 3].closePrice + data[i - 2].closePrice + data[i - 1].closePrice + data[i].closePrice + data[i + 1].openPrice) / 5f)
                            .toFloat()
                    data[i + 1].MA8Price = decimalFormat3.format(
                        (data[i - 6].closePrice + data[i - 5].closePrice + data[i - 4].closePrice +
                                data[i - 3].closePrice + data[i - 2].closePrice + data[i - 1].closePrice + data[i].closePrice + data[i + 1].openPrice) / 8f
                    ).toFloat()
                    data[i + 1].MA13Price = decimalFormat3.format(
                        (data[i - 11].closePrice + data[i - 10].closePrice + data[i - 9].closePrice + data[i - 8].closePrice + data[i - 7].closePrice +
                                data[i - 6].closePrice + data[i - 5].closePrice + data[i - 4].closePrice +
                                data[i - 3].closePrice + data[i - 2].closePrice + data[i - 1].closePrice + data[i].closePrice + data[i + 1].openPrice) / 13f
                    ).toFloat()

                    val sortAverage = ArrayList<Float>().apply {
                        add(data[i + 1].mA5Price)
                        add(data[i + 1].mA8Price)
                        add(data[i + 1].mA13Price)
                    }

                    sortAverage.sort()

                    val linePrice = sortAverage[line]

//                    addTrade = data[i + 1].High > linePrice && data[i + 1].Low < linePrice &&
//                            data[i + 1].High > sortAverage[1] && data[i + 1].Low < sortAverage[1]

                    if (data[i + 1].High > linePrice && data[i + 1].Low < linePrice) {
                        nextDayCount += 1

                        keyFiveCount += 1

                        stockCostPrice = if (data[i + 1].Open < linePrice)
                            data[i + 1].Open
                        else
                            linePrice

//                        stockCostPrice = if (data[i + 1].Open < linePrice)
//                            data[i + 1].Open
//                        else
//                            linePrice

                        if (data[i + 2].High > stockCostPrice) {
                            successHighCount += 1
                            maxProfit += decimalFormat.format((data[i + 2].High - stockCostPrice) / stockCostPrice * 100)
                                .toFloat()
                            if (data[i + 1].Volume / data[i].Volume > 1) {
                                testSuccessHighTradeCount += 1
                            } else {
                                testSuccessLowTradeCount += 1
                            }
                        } else {
                            failureHighCount += 1

                            if (data[i + 1].Volume / data[i].Volume > 1) {
                                testFailureHighTradeCount += 1
                            } else {
                                testFailureLowTradeCount += 1
                            }
                            mixProfit += decimalFormat.format((data[i + 2].Low - stockCostPrice) / stockCostPrice * 100)
                                .toFloat()
                        }
                        if (data[i + 2].Close >= stockCostPrice) {
                            successCloseCount += 1
                            if (addTrade) {
                                successCloseProfit += decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100 * 2)
                                    .toFloat()
                                successDate.add(
                                    data[i].date + "(" + decimalFormat3.format(
                                        stockCostPrice
                                    ) + "::" + decimalFormat3.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100 * 2) + "::" + decimalFormat3.format(
                                        (data[i + 2].High - stockCostPrice) / stockCostPrice * 100 * 2
                                    ) + ")"
                                )
                            } else {
                                successCloseProfit += decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100)
                                    .toFloat()
                                successDate.add(
                                    data[i].date + "(" + decimalFormat3.format(
                                        stockCostPrice
                                    ) + "::" + decimalFormat3.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100) + "::" + decimalFormat3.format(
                                        (data[i + 2].High - stockCostPrice) / stockCostPrice * 100
                                    ) + ")"
                                )
                            }
                        } else {
                            failureCloseCount += 1
                            if (addTrade) {
                                failureCloseProfit += decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100 * 2)
                                    .toFloat()
                                failureDate.add(
                                    data[i].date + "(" + decimalFormat3.format(
                                        stockCostPrice
                                    ) + "::" + decimalFormat3.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100 * 2) + "::" + decimalFormat3.format(
                                        (data[i + 2].High - stockCostPrice) / stockCostPrice * 100 * 2
                                    ) + ")"
                                )
                            } else {
                                failureCloseProfit += decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100)
                                    .toFloat()
                                failureDate.add(
                                    data[i].date + "(" + decimalFormat3.format(
                                        stockCostPrice
                                    ) + "::" + decimalFormat3.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100) + "::" + decimalFormat3.format(
                                        (data[i + 2].High - stockCostPrice) / stockCostPrice * 100 * 2
                                    ) + ")"
                                )
                            }
                        }

                        if (data[i + 2].High > stockCostPrice) {
                            val highRate =
                                (data[i + 2].High - stockCostPrice) / stockCostPrice * 100
                            if (highRate > 1) success1PointCount += 1
                            if (highRate > 2) success2PointCount += 1
                            if (highRate > 3) success3PointCount += 1
                            if (highRate > 4) success4PointCount += 1
                            if (highRate > 5) success5PointCount += 1
                        }

                        if ((data[i + 2].High - stockCostPrice) / stockCostPrice > 0.01f) {
                            success1PointCount += 1
                        }

                        if ((data[i + 2].Low - stockCostPrice) / stockCostPrice < -0.01f) {
                            failure1PointCount += 1
                        }

                        if ((data[i + 2].High - stockCostPrice) / stockCostPrice > 0.03f) {
                            success3PointCount += 1
                        }

                        if ((data[i + 2].Low - stockCostPrice) / stockCostPrice < -0.03f) {
                            failure1PointCount += 1
                        }
                    }
                }
            }
        }

        println("------------------>总运行次数 $runCount")
        println("------------------>超5%的涨幅次数 $keyCount")
        println("------------------>超5%并穿过5日均线次数 $keyFiveCount")
        println("------------------>第二天达标的次数 $nextDayCount")
        println("------------------>第三天达标的次数 $thirdDayCount")

        println("------------------>第二天有高点的个数为 $successHighCount")
        println("------------------>第二天收盘价盈利个数为 $successCloseCount")
        println("------------------>第二天超过1%的盈利个数为 $success1PointCount")
        println("------------------>第二天超过3%的盈利个数为 $success3PointCount")
        println("------------------>第二天最高收益为$maxProfit%")

        println("------------------>第二天有低点的个数为 $failureHighCount")
        println("------------------>第二天收盘价亏损个数为 $failureCloseCount")
        println("------------------>第二天超过1%的亏损个数为 $failure1PointCount")
        println("------------------>第二天超过3%的亏损个数为 $failure3PointCount")
        println("------------------>第二天最低亏损为$mixProfit%")

        println("------------------>第二天有新高时量比大 $testSuccessHighTradeCount")
        println("------------------>第二天有新高时量比小 $testSuccessLowTradeCount")
        println("------------------>第二天强行亏损量比大 $testFailureHighTradeCount")
        println("------------------>第二天强行亏损量比小 $testFailureLowTradeCount")

        println("------------------>收盘价盈利总百分比例为 $successCloseProfit%")
        println("------------------>收盘价亏损总百分比例为 $failureCloseProfit%")

        println("------------------>垃圾位置点 $dustCount")
        println("------------------>成功时间点 $successDate")
        println("------------------>失败时间点 $failureDate")
        val temp = LinkedHashSet<String>().apply {
            addAll(dustDate)
        }
        dustDate.clear()
        dustDate.addAll(temp)
        println("------------------>垃圾时间点 $dustDate")

        var statisticsModel = StatisticsModel().apply {
            this.stockCode = stockCode
            this.stockName = stockName
            this.successHighCount = successHighCount
            this.successCloseCount = successCloseCount
            this.successCloseProfit = successCloseProfit
            this.success1PointCount = success1PointCount
            this.success2PointCount = success2PointCount
            this.success3PointCount = success3PointCount
            this.success4PointCount = success4PointCount
            this.success5PointCount = success5PointCount
            this.failureHighCount = failureHighCount
            this.failureCloseCount = failureCloseCount
            this.failureCloseProfit = failureCloseProfit
            this.failure1PointCount = failure1PointCount
            this.failure3PointCount = failure3PointCount
            this.keyCount = nextDayCount + thirdDayCount
            this.maxProfit = maxProfit
            this.minProfit = mixProfit
            this.statisticsType = statisticsType
            this.successDate = successDate.toString()
            this.failureDate = failureDate.toString()
        }

        statisticsModel.save()

        return statisticsModel
    }

    //判断突破中间均线后，回踩后，反抽的成功情况
    fun judgeFiveSuccessRateForMinLine3ThreeSort(
        stockCode: String,
        stockName: String,
        datas: ArrayList<KLineEntity>,
        statisticsType: String,
        line: Int = 0,
        needThirdDay: Boolean = true
    ): StatisticsModel {
        if (datas.size < 12) return StatisticsModel()

        var data = ArrayList<KLineEntity>().apply { addAll(datas) }

        var successHighCount = 0
        var successCloseCount = 0
        var success1PointCount = 0
        var success3PointCount = 0
        var successCloseProfit = 0f
        var failureHighCount = 0
        var failureCloseCount = 0
        var failure1PointCount = 0
        var failure3PointCount = 0
        var failureCloseProfit = 0f
        var stockCostPrice: Float
        var keyCount = 0
        var keyFiveCount = 0
        var nextDayCount = 0
        var thirdDayCount = 0
        var runCount = 0
        var maxProfit = 0f
        var mixProfit = 0f

        var testSuccessHighTradeCount = 0
        var testSuccessLowTradeCount = 0

        var testFailureHighTradeCount = 0
        var testFailureLowTradeCount = 0

        var successDate = ArrayList<String>()
        var failureDate = ArrayList<String>()

        var dustCount = 0
        var dustDate = ArrayList<String>()

        for (i in 10..(data.size - 4)) {
            runCount += 1
            if ((data[i].Close - data[i - 1].Close) / data[i - 1].Close > 0.04f) {
                keyCount += 1

                val status = when (BaseApplication.instance?.filterOptions?.throughType) {
                    ThroughType.NormalThrough.type -> data[i].Open < data[i].mA5Price && data[i].Close > data[i].mA5Price &&
                            data[i].Open < data[i].mA8Price && data[i].Close > data[i].mA8Price &&
                            data[i].Open < data[i].mA13Price && data[i].Close > data[i].mA13Price &&
                            data[i].mA5Price >= data[i].mA8Price && data[i].mA8Price >= data[i].mA13Price
                    ThroughType.HighThrough.type -> data[i].Open < data[i].mA5Price && data[i].Close > data[i].mA5Price &&
                            data[i].Open < data[i].mA8Price && data[i].Close > data[i].mA8Price &&
                            data[i].Open < data[i].mA13Price && data[i].Close > data[i].mA13Price &&
                            data[i].mA5Price >= data[i].mA8Price && data[i].mA8Price >= data[i].mA13Price &&
                            data[i].Close - (data[i].mA5Price + data[i].mA8Price + data[i].mA13Price) / 3 > (data[i].mA5Price + data[i].mA8Price + data[i].mA13Price) / 3 - data[i].Open
                    ThroughType.ThroughAndTrade.type -> data[i].Open < data[i].mA5Price && data[i].Close > data[i].mA5Price &&
                            data[i].Open < data[i].mA8Price && data[i].Close > data[i].mA8Price &&
                            data[i].Open < data[i].mA13Price && data[i].Close > data[i].mA13Price &&
                            data[i].mA5Price >= data[i].mA8Price && data[i].mA8Price >= data[i].mA13Price &&
                            data[i].Volume > data[i - 1].Volume * 2
                    ThroughType.HighThroughAndTrade.type -> data[i].Open < data[i].mA5Price && data[i].Close > data[i].mA5Price &&
                            data[i].Open < data[i].mA8Price && data[i].Close > data[i].mA8Price &&
                            data[i].Open < data[i].mA13Price && data[i].Close > data[i].mA13Price &&
                            data[i].mA5Price >= data[i].mA8Price && data[i].mA8Price >= data[i].mA13Price &&
                            data[i].Close - (data[i].mA5Price + data[i].mA8Price + data[i].mA13Price) / 3 > (data[i].mA5Price + data[i].mA8Price + data[i].mA13Price) / 3 - data[i].Open &&
                            data[i].Volume > data[i - 1].Volume * 2
                    else -> data[i].Open < data[i].mA5Price && data[i].Close > data[i].mA5Price &&
                            data[i].Open < data[i].mA8Price && data[i].Close > data[i].mA8Price &&
                            data[i].Open < data[i].mA13Price && data[i].Close > data[i].mA13Price &&
                            data[i].mA5Price >= data[i].mA8Price && data[i].mA8Price >= data[i].mA13Price
                }

                if (status) {

                    if (abs(data[i].MA5Price - data[i - 10].MA5Price) / data[i - 10].MA5Price > 0.25f) {
                        dustCount += 1
                        dustDate.add(data[i].Date)
                    }

                    val sortAverage = ArrayList<Float>().apply {
                        add(data[i + 1].mA5Price)
                        add(data[i + 1].mA8Price)
                        add(data[i + 1].mA13Price)
                    }

                    sortAverage.sort()

                    val linePrice = sortAverage[line]

                    keyFiveCount += 1
                    if (data[i + 1].High > linePrice && data[i + 1].Low < linePrice) {
                        nextDayCount += 1
                        stockCostPrice = linePrice

                        if (data[i + 1].Volume > data[i].Volume && data[i + 1].Open > data[i + 1].Close) {
                            dustCount += 1
                            dustDate.add(data[i].Date)
                        } else {

                            if (data[i + 2].High > stockCostPrice) {
                                successHighCount += 1
                                maxProfit += decimalFormat.format((data[i + 2].High - stockCostPrice) / stockCostPrice * 100)
                                    .toFloat()
                                successDate.add(
                                    data[i].Date + "(" + decimalFormat3.format(
                                        stockCostPrice
                                    ) + " ::: " + decimalFormat3.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100) + ")"
                                )
                                if (data[i + 1].Volume / data[i].Volume > 1) {
                                    testSuccessHighTradeCount += 1
                                } else {
                                    testSuccessLowTradeCount += 1
                                }
                            } else {
                                failureHighCount += 1
                                failureDate.add(
                                    data[i].Date + "(" + decimalFormat3.format(
                                        stockCostPrice
                                    ) + " ::: " + decimalFormat3.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100) + ")"
                                )

                                if (data[i + 1].Volume / data[i].Volume > 1) {
                                    testFailureHighTradeCount += 1
                                } else {
                                    testFailureLowTradeCount += 1
                                }
                                mixProfit += decimalFormat.format((data[i + 2].Low - stockCostPrice) / stockCostPrice * 100)
                                    .toFloat()
                            }
                            if (data[i + 2].Close > stockCostPrice) {
                                successCloseCount += 1
                                successCloseProfit += decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100)
                                    .toFloat()
                            } else {
                                failureCloseCount += 1
                                failureCloseProfit += decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100)
                                    .toFloat()
                            }

                            if ((data[i + 2].High - stockCostPrice) / stockCostPrice > 0.01f) {
                                success1PointCount += 1
                            }

                            if ((data[i + 2].Low - stockCostPrice) / stockCostPrice < -0.01f) {
                                failure1PointCount += 1
                            }

                            if ((data[i + 2].High - stockCostPrice) / stockCostPrice > 0.03f) {
                                success3PointCount += 1
                            }

                            if ((data[i + 2].Low - stockCostPrice) / stockCostPrice < -0.03f) {
                                failure1PointCount += 1
                            }
                        }
                    } else if (data[i + 2].High > linePrice && data[i + 2].Low < linePrice && needThirdDay) {
                        thirdDayCount += 1
                        stockCostPrice = linePrice

                        if (data[i + 2].Volume > data[i].Volume && data[i + 2].Open > data[i + 2].Close) {
                            dustCount += 1
                            dustDate.add(data[i].Date)
                        }

                        if (data[i + 3].High > stockCostPrice) {
                            successHighCount += 1
                            maxProfit += decimalFormat.format((data[i + 3].High - stockCostPrice) / stockCostPrice * 100)
                                .toFloat()
                            successDate.add(
                                data[i].Date + "(" + decimalFormat3.format(
                                    stockCostPrice
                                ) + " ::: " + decimalFormat3.format((data[i + 3].Close - stockCostPrice) / stockCostPrice * 100) + ")"
                            )
                        } else {
                            failureHighCount += 1
                            failureDate.add(
                                data[i].Date + "(" + decimalFormat3.format(
                                    stockCostPrice
                                ) + " ::: " + decimalFormat3.format((data[i + 3].Close - stockCostPrice) / stockCostPrice * 100) + ")"
                            )
                            mixProfit += decimalFormat.format((data[i + 3].Close - stockCostPrice) / stockCostPrice * 100)
                                .toFloat()
                        }

                        if (data[i + 3].Close > stockCostPrice) {
                            successCloseCount += 1
                            successCloseProfit += decimalFormat.format((data[i + 3].Close - stockCostPrice) / stockCostPrice * 100)
                                .toFloat()
                        } else {
                            failureCloseCount += 1
                            failureCloseProfit += decimalFormat.format((data[i + 3].Close - stockCostPrice) / stockCostPrice * 100)
                                .toFloat()
                        }
                        if ((data[i + 3].High - stockCostPrice) / stockCostPrice > 0.01f) {
                            success1PointCount += 1
                        }

                        if ((data[i + 3].Low - stockCostPrice) / stockCostPrice < -0.01f) {
                            failure1PointCount += 1
                        }

                        if ((data[i + 3].High - stockCostPrice) / stockCostPrice > 0.03f) {
                            success3PointCount += 1
                        }

                        if ((data[i + 3].Low - stockCostPrice) / stockCostPrice < -0.03f) {
                            failure3PointCount += 1
                        }
                    }
                }
            }
        }

        println("------------------>总运行次数 $runCount")
        println("------------------>超5%的涨幅次数 $keyCount")
        println("------------------>超5%并穿过5日均线次数 $keyFiveCount")
        println("------------------>第二天达标的次数 $nextDayCount")
        println("------------------>第三天达标的次数 $thirdDayCount")

        println("------------------>第二天有高点的个数为 $successHighCount")
        println("------------------>第二天收盘价盈利个数为 $successCloseCount")
        println("------------------>第二天超过1%的盈利个数为 $success1PointCount")
        println("------------------>第二天超过3%的盈利个数为 $success3PointCount")
        println("------------------>第二天最高收益为$maxProfit%")

        println("------------------>第二天有低点的个数为 $failureHighCount")
        println("------------------>第二天收盘价亏损个数为 $failureCloseCount")
        println("------------------>第二天超过1%的亏损个数为 $failure1PointCount")
        println("------------------>第二天超过3%的亏损个数为 $failure3PointCount")
        println("------------------>第二天最低亏损为$mixProfit%")

        println("------------------>第二天有新高时量比大 $testSuccessHighTradeCount")
        println("------------------>第二天有新高时量比小 $testSuccessLowTradeCount")
        println("------------------>第二天强行亏损量比大 $testFailureHighTradeCount")
        println("------------------>第二天强行亏损量比小 $testFailureLowTradeCount")

        println("------------------>收盘价盈利总百分比例为 $successCloseProfit%")
        println("------------------>收盘价亏损总百分比例为 $failureCloseProfit%")

        println("------------------>垃圾位置点 $dustCount")
        println("------------------>成功时间点 $successDate")
        println("------------------>失败时间点 $failureDate")
        val temp = LinkedHashSet<String>().apply {
            addAll(dustDate)
        }
        dustDate.clear()
        dustDate.addAll(temp)
        println("------------------>垃圾时间点 $dustDate")

        var statisticsModel = StatisticsModel().apply {
            this.stockCode = stockCode
            this.stockName = stockName
            this.successHighCount = successHighCount
            this.successCloseCount = successCloseCount
            this.successCloseProfit = successCloseProfit
            this.success1PointCount = success1PointCount
            this.success3PointCount = success3PointCount
            this.failureHighCount = failureHighCount
            this.failureCloseCount = failureCloseCount
            this.failureCloseProfit = failureCloseProfit
            this.failure1PointCount = failure1PointCount
            this.failure3PointCount = failure3PointCount
            this.keyCount = nextDayCount + thirdDayCount
            this.maxProfit = maxProfit
            this.minProfit = mixProfit
            this.statisticsType = statisticsType
        }

        statisticsModel.save()

        return statisticsModel
    }

    //带量穿三高换手
    fun judgeFiveSuccessRateForDoubleDay(
        stockCode: String,
        stockName: String,
        datas: ArrayList<KLineEntity>,
        statisticsType: String,
        line: Int = 0,
        needThirdDay: Boolean = true
    ): StatisticsModel {
        if (datas.size < 12) return StatisticsModel()

        var data = ArrayList<KLineEntity>().apply { addAll(datas) }

        var successHighCount = 0
        var successCloseCount = 0
        var success1PointCount = 0
        var success3PointCount = 0
        var successCloseProfit = 0f
        var failureHighCount = 0
        var failureCloseCount = 0
        var failure1PointCount = 0
        var failure3PointCount = 0
        var failureCloseProfit = 0f
        var stockCostPrice: Float = 0f
        var keyCount = 0
        var keyFiveCount = 0
        var nextDayCount = 0
        var thirdDayCount = 0
        var runCount = 0
        var maxProfit = 0f
        var mixProfit = 0f

        var testSuccessHighTradeCount = 0
        var testSuccessLowTradeCount = 0

        var testFailureHighTradeCount = 0
        var testFailureLowTradeCount = 0

        var successDate = ArrayList<String>()
        var failureDate = ArrayList<String>()

        var dustCount = 0
        var dustDate = ArrayList<String>()

        for (i in 15..(data.size - 4)) {
            runCount += 1
            if ((data[i].Close - data[i - 1].Close) / data[i - 1].Close > 0.05f) {
                keyCount += 1

                val status = when (BaseApplication.instance?.filterOptions?.throughType) {
                    ThroughType.NormalThrough.type -> data[i].Open < data[i].mA5Price && data[i].Close > data[i].mA5Price &&
                            data[i].Open < data[i].mA8Price && data[i].Close > data[i].mA8Price &&
                            data[i].Open < data[i].mA13Price && data[i].Close > data[i].mA13Price
                    ThroughType.HighThrough.type -> data[i].Open < data[i].mA5Price && data[i].Close > data[i].mA5Price &&
                            data[i].Open < data[i].mA8Price && data[i].Close > data[i].mA8Price &&
                            data[i].Open < data[i].mA13Price && data[i].Close > data[i].mA13Price &&
                            data[i].Close - (data[i].mA5Price + data[i].mA8Price + data[i].mA13Price) / 3 > (data[i].mA5Price + data[i].mA8Price + data[i].mA13Price) / 3 - data[i].Open
                    ThroughType.ThroughAndTrade.type -> data[i].Open < data[i].mA5Price && data[i].Close > data[i].mA5Price &&
                            data[i].Open < data[i].mA8Price && data[i].Close > data[i].mA8Price &&
                            data[i].Open < data[i].mA13Price && data[i].Close > data[i].mA13Price &&
                            data[i].Volume > data[i - 1].Volume * 2
                    ThroughType.HighThroughAndTrade.type -> data[i].Open < data[i].mA5Price && data[i].Close > data[i].mA5Price &&
                            data[i].Open < data[i].mA8Price && data[i].Close > data[i].mA8Price &&
                            data[i].Open < data[i].mA13Price && data[i].Close > data[i].mA13Price &&
                            data[i].Close - (data[i].mA5Price + data[i].mA8Price + data[i].mA13Price) / 3 > (data[i].mA5Price + data[i].mA8Price + data[i].mA13Price) / 3 - data[i].Open &&
                            data[i].Volume > data[i - 1].Volume * 2
                    else -> data[i].Open < data[i].mA5Price && data[i].Close > data[i].mA5Price &&
                            data[i].Open < data[i].mA8Price && data[i].Close > data[i].mA8Price &&
                            data[i].Open < data[i].mA13Price && data[i].Close > data[i].mA13Price
                }

                val volumeRate = (data[i].volume / 240f).toInt()
                    .toFloat() / ((data[i - 1].volume + data[i - 2].volume + data[i - 3].volume + data[i - 4].volume + data[i - 5].volume) / 1200f).toInt()
                    .toFloat()

                if (needThirdDay && volumeRate < BaseApplication.instance?.filterOptions?.volumeStartRate!! * 0.1)
                    continue

                if (status) {

                    val sortAverage = ArrayList<Float>().apply {
                        add(data[i + 1].mA5Price)
                        add(data[i + 1].mA8Price)
                        add(data[i + 1].mA13Price)
                    }

                    sortAverage.sort()

                    val upLinePrice = sortAverage[2]
                    val downLinePrice = sortAverage[0]

                    keyFiveCount += 1

                    if (data[i + 1].High > sortAverage[0] && data[i + 1].Low < sortAverage[0]) {
                        stockCostPrice = (sortAverage[2] + sortAverage[0]) / 2
                    } else if (data[i + 1].High > sortAverage[2] && data[i + 1].Low < sortAverage[2]) {
                        stockCostPrice = sortAverage[2]
                    } else {
                        stockCostPrice = 0f
                    }

                    if (stockCostPrice > 0f && !needThirdDay) {
                        nextDayCount += 1

                        if (data[i + 1].Volume > data[i].Volume && data[i + 1].Open > data[i + 1].Close) {
                            dustCount += 1
                            dustDate.add(data[i].Date)
                        } else {
                            val date = data[i].Date
                            println("------------------>Date : $date")
                            if ((data[i + 2].Close - stockCostPrice) / stockCostPrice > 0.03f) {
                                successCloseCount += 1
                                successCloseProfit += decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100)
                                    .toFloat()
                                if (data[i + 2].High > stockCostPrice)
                                    successHighCount += 1
                            } else {
                                if (data[i + 3].Close < stockCostPrice) {
                                    failureCloseCount += 1
                                    failureCloseProfit += decimalFormat.format((data[i + 3].Close - stockCostPrice) / stockCostPrice * 100)
                                        .toFloat()
                                    if (data[i + 3].High > stockCostPrice)
                                        successHighCount += 1
                                    else
                                        failureHighCount += 1
                                } else {
                                    successCloseCount += 1
                                    successCloseProfit += decimalFormat.format((data[i + 3].Close - stockCostPrice) / stockCostPrice * 100)
                                        .toFloat()
                                    if (data[i + 2].High < stockCostPrice)
                                        failureHighCount += 1
                                }

                            }

//                            if (data[i + 2].High > stockCostPrice) {
//                                if ((data[i + 2].High - stockCostPrice) / stockCostPrice > 0.03f){
//                                    successHighCount += 1
//                                    maxProfit += decimalFormat.format((data[i + 2].High - stockCostPrice) / stockCostPrice * 100)
//                                        .toFloat()
//                                }
//                                successHighCount += 1
//                                maxProfit += decimalFormat.format((data[i + 2].High - stockCostPrice) / stockCostPrice * 100)
//                                    .toFloat()
//                                successDate.add(data[i].Date)
//                                if (data[i + 1].Volume / data[i].Volume > 1) {
//                                    testSuccessHighTradeCount += 1
//                                } else {
//                                    testSuccessLowTradeCount += 1
//                                }
//                            } else {
//                                failureHighCount += 1
//                                failureDate.add(data[i].Date)
//
//                                if (data[i + 1].Volume / data[i].Volume > 1) {
//                                    testFailureHighTradeCount += 1
//                                } else {
//                                    testFailureLowTradeCount += 1
//                                }
//                                mixProfit += decimalFormat.format((data[i + 2].Low - stockCostPrice) / stockCostPrice * 100)
//                                    .toFloat()
//                            }
//                            if (data[i + 2].Close > stockCostPrice) {
//                                successCloseCount += 1
//                                successCloseProfit += decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100)
//                                    .toFloat()
//                            } else {
//                                failureCloseCount += 1
//                                failureCloseProfit += decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100)
//                                    .toFloat()
//                            }
//
//                            if ((data[i + 2].High - stockCostPrice) / stockCostPrice > 0.01f) {
//                                success1PointCount += 1
//                            }
//
//                            if ((data[i + 2].Low - stockCostPrice) / stockCostPrice < -0.01f) {
//                                failure1PointCount += 1
//                            }
//
//                            if ((data[i + 2].High - stockCostPrice) / stockCostPrice > 0.03f) {
//                                success3PointCount += 1
//                            }
//
//                            if ((data[i + 2].Low - stockCostPrice) / stockCostPrice < -0.03f) {
//                                failure1PointCount += 1
//                            }
                        }
                    } else if (data[i + 2].High > upLinePrice && data[i + 2].Low < upLinePrice && needThirdDay) {
                        thirdDayCount += 1
                        stockCostPrice = upLinePrice

                        if (data[i + 2].Volume > data[i].Volume && data[i + 2].Open > data[i + 2].Close) {
                            dustCount += 1
                            dustDate.add(data[i].Date)
                        }

                        if (data[i + 3].High > stockCostPrice) {
                            successHighCount += 1
                            maxProfit += decimalFormat.format((data[i + 3].High - stockCostPrice) / stockCostPrice * 100)
                                .toFloat()
                            successDate.add(data[i].Date)
                        } else {
                            failureHighCount += 1
                            failureDate.add(data[i].Date)
                            mixProfit += decimalFormat.format((data[i + 3].Close - stockCostPrice) / stockCostPrice * 100)
                                .toFloat()
                        }

                        if (data[i + 3].Close > stockCostPrice) {
                            successCloseCount += 1
                            successCloseProfit += decimalFormat.format((data[i + 3].Close - stockCostPrice) / stockCostPrice * 100)
                                .toFloat()
                        } else {
                            failureCloseCount += 1
                            failureCloseProfit += decimalFormat.format((data[i + 3].Close - stockCostPrice) / stockCostPrice * 100)
                                .toFloat()
                        }
                        if ((data[i + 3].High - stockCostPrice) / stockCostPrice > 0.01f) {
                            success1PointCount += 1
                        }

                        if ((data[i + 3].Low - stockCostPrice) / stockCostPrice < -0.01f) {
                            failure1PointCount += 1
                        }

                        if ((data[i + 3].High - stockCostPrice) / stockCostPrice > 0.03f) {
                            success3PointCount += 1
                        }

                        if ((data[i + 3].Low - stockCostPrice) / stockCostPrice < -0.03f) {
                            failure3PointCount += 1
                        }
                    }
                }
            }
        }

        println("------------------>总运行次数 $runCount")
        println("------------------>超5%的涨幅次数 $keyCount")
        println("------------------>超5%并穿过5日均线次数 $keyFiveCount")
        println("------------------>第二天达标的次数 $nextDayCount")
        println("------------------>第三天达标的次数 $thirdDayCount")

        println("------------------>第二天有高点的个数为 $successHighCount")
        println("------------------>第二天收盘价盈利个数为 $successCloseCount")
        println("------------------>第二天超过1%的盈利个数为 $success1PointCount")
        println("------------------>第二天超过3%的盈利个数为 $success3PointCount")
        println("------------------>第二天最高收益为$maxProfit%")

        println("------------------>第二天有低点的个数为 $failureHighCount")
        println("------------------>第二天收盘价亏损个数为 $failureCloseCount")
        println("------------------>第二天超过1%的亏损个数为 $failure1PointCount")
        println("------------------>第二天超过3%的亏损个数为 $failure3PointCount")
        println("------------------>第二天最低亏损为$mixProfit%")

        println("------------------>第二天有新高时量比大 $testSuccessHighTradeCount")
        println("------------------>第二天有新高时量比小 $testSuccessLowTradeCount")
        println("------------------>第二天强行亏损量比大 $testFailureHighTradeCount")
        println("------------------>第二天强行亏损量比小 $testFailureLowTradeCount")

        println("------------------>收盘价盈利总百分比例为 $successCloseProfit%")
        println("------------------>收盘价亏损总百分比例为 $failureCloseProfit%")

        println("------------------>垃圾位置点 $dustCount")
        println("------------------>成功时间点 $successDate")
        println("------------------>失败时间点 $failureDate")
        val temp = LinkedHashSet<String>().apply {
            addAll(dustDate)
        }
        dustDate.clear()
        dustDate.addAll(temp)
        println("------------------>垃圾时间点 $dustDate")

        var statisticsModel = StatisticsModel().apply {
            this.stockCode = stockCode
            this.stockName = stockName
            this.successHighCount = successHighCount
            this.successCloseCount = successCloseCount
            this.successCloseProfit = successCloseProfit
            this.success1PointCount = success1PointCount
            this.success3PointCount = success3PointCount
            this.failureHighCount = failureHighCount
            this.failureCloseCount = failureCloseCount
            this.failureCloseProfit = failureCloseProfit
            this.failure1PointCount = failure1PointCount
            this.failure3PointCount = failure3PointCount
            this.keyCount = nextDayCount + thirdDayCount
            this.maxProfit = maxProfit
            this.minProfit = mixProfit
            this.statisticsType = statisticsType
        }

        statisticsModel.save()

        return statisticsModel
    }

    //顺序中均线二次确认5102030二次确认
    fun judgeFiveSuccessRateForHighQuality(
        stockCode: String,
        stockName: String,
        datas: ArrayList<KLineEntity>,
        statisticsType: String,
        line: Int = 0,
        needThirdDay: Boolean = true
    ): StatisticsModel {
        if (datas.size < 12) return StatisticsModel()

        var data = ArrayList<KLineEntity>().apply { addAll(datas) }

        var successHighCount = 0
        var successCloseCount = 0
        var success1PointCount = 0
        var success2PointCount = 0
        var success3PointCount = 0
        var success4PointCount = 0
        var success5PointCount = 0
        var successCloseProfit = 0f
        var failureHighCount = 0
        var failureCloseCount = 0
        var failure1PointCount = 0
        var failure3PointCount = 0
        var failureCloseProfit = 0f
        var stockCostPrice: Float = 0f
        var keyCount = 0
        var keyFiveCount = 0
        var nextDayCount = 0
        var thirdDayCount = 0
        var runCount = 0
        var maxProfit = 0f
        var mixProfit = 0f

        var testSuccessHighTradeCount = 0
        var testSuccessLowTradeCount = 0

        var testFailureHighTradeCount = 0
        var testFailureLowTradeCount = 0

        var successDate = ArrayList<String>()
        var failureDate = ArrayList<String>()

        var dustCount = 0
        var dustDate = ArrayList<String>()

        var currcapital = LitePalDBase.queryCurrcapital(stockCode)

        for (i in 15..(data.size - 4)) {
            runCount += 1
            if ((data[i].Close - data[i - 1].Close) / data[i - 1].Close > BaseApplication.instance?.filterOptions?.startRate!! / 100f &&
                (data[i].Close - data[i - 1].Close) / data[i - 1].Close < BaseApplication.instance?.filterOptions?.endRate!! / 100f
            ) {

                val tempFiveDayPrice =
                    (data[i].Close + data[i - 1].Close + data[i - 2].Close + data[i - 3].Close) / 4f

                val dataJ1MA10Price =
                    (data[i - 8].Close + data[i - 7].Close + data[i - 6].Close + data[i - 5].Close + data[i - 4].Close + data[i - 3].Close + data[i - 2].Close + data[i - 1].Close + data[i].Close) / 9f

                val volumeRate = (data[i].volume / 240f).toInt()
                    .toFloat() / ((data[i - 1].volume + data[i - 2].volume + data[i - 3].volume + data[i - 4].volume + data[i - 5].volume) / 1200f).toInt()
                    .toFloat()

                if (volumeRate < BaseApplication.instance?.filterOptions?.volumeStartRate!! * 0.1f || volumeRate > BaseApplication.instance?.filterOptions?.volumeEndRate!! * 0.1f)
                    continue

                if (currcapital > 0 && (data[i].volume / currcapital < BaseApplication.instance?.filterOptions?.turnoverStartRate!! || data[i].volume / currcapital > BaseApplication.instance?.filterOptions?.turnoverEndRate!!))
                    continue

                val checkDayLowPrice = if (!needThirdDay)
                    data[i + 1].Low <= tempFiveDayPrice
                else
                    data[i + 1].Low <= dataJ1MA10Price

                if ((data[i - 3].mA5Price < data[i - 3].mA10Price || data[i - 3].mA10Price < data[i - 3].mA20Price || data[i - 3].mA20Price < data[i - 3].mA30Price) &&
                    (data[i - 2].mA5Price > data[i - 2].mA10Price && data[i - 2].mA10Price > data[i - 2].mA20Price && data[i - 2].mA20Price > data[i - 2].mA30Price) &&
                    (data[i - 1].mA5Price > data[i - 1].mA10Price && data[i - 1].mA10Price > data[i - 1].mA20Price && data[i - 1].mA20Price > data[i - 1].mA30Price) &&
                    (data[i].mA5Price > data[i].mA10Price && data[i].mA10Price > data[i].mA20Price && data[i].mA20Price > data[i].mA30Price) &&
                    data[i].Close > data[i].mA5Price && checkDayLowPrice && data[i + 1].Volume < data[i + 1].Volume
                ) {

                    keyCount += 1

                    stockCostPrice =
                        if (!needThirdDay) {
                            if (data[i + 1].Open < tempFiveDayPrice)
                                data[i + 1].Open
                            else
                                tempFiveDayPrice
                        } else {
                            if (data[i + 1].Open < dataJ1MA10Price)
                                data[i + 1].Open
                            else
                                dataJ1MA10Price
                        }


                    keyFiveCount += 1

                    val date = data[i].Date
                    println("------------------>Date : $date")

                    if (data[i + 2].Close > stockCostPrice) {
                        successCloseCount += 1
                        successCloseProfit += decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100)
                            .toFloat()
                        successDate.add(
                            data[i].date + "(" + decimalFormat3.format(stockCostPrice) + "::" + decimalFormat.format(
                                (data[i + 2].Close - stockCostPrice) / stockCostPrice * 100
                            ) + "%::" + decimalFormat.format((data[i + 2].High - stockCostPrice) / stockCostPrice * 100) + "%)常"
                        )
                    } else {
                        failureCloseCount += 1
                        failureCloseProfit += decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100)
                            .toFloat()
                        failureDate.add(
                            data[i].date + "(" + decimalFormat3.format(stockCostPrice) + "::" + decimalFormat.format(
                                (data[i + 2].Close - stockCostPrice) / stockCostPrice * 100
                            ) + "%::" + decimalFormat.format((data[i + 2].High - stockCostPrice) / stockCostPrice * 100) + "%)常"
                        )
                    }

                    if (data[i + 2].High > stockCostPrice) {
                        successHighCount += 1
                        val highRate =
                            decimalFormat.format((data[i + 2].High - stockCostPrice) / stockCostPrice * 100)
                                .toFloat()
                        maxProfit += highRate
                        if (highRate > 1) success1PointCount += 1
                        if (highRate > 2) success2PointCount += 1
                        if (highRate > 3) success3PointCount += 1
                        if (highRate > 4) success4PointCount += 1
                        if (highRate > 5) success5PointCount += 1
                    } else {
                        failureHighCount += 1
                        mixProfit += decimalFormat.format((data[i + 2].High - stockCostPrice) / stockCostPrice * 100)
                            .toFloat()
                    }

                }
            }
        }

        println("------------------>总运行次数 $runCount")
        println("------------------>超5%的涨幅次数 $keyCount")
        println("------------------>超5%并穿过5日均线次数 $keyFiveCount")
        println("------------------>第二天达标的次数 $nextDayCount")
        println("------------------>第三天达标的次数 $thirdDayCount")

        println("------------------>第二天有高点的个数为 $successHighCount")
        println("------------------>第二天收盘价盈利个数为 $successCloseCount")
        println("------------------>第二天超过1%的盈利个数为 $success1PointCount")
        println("------------------>第二天超过3%的盈利个数为 $success3PointCount")
        println("------------------>第二天最高收益为$maxProfit%")

        println("------------------>第二天有低点的个数为 $failureHighCount")
        println("------------------>第二天收盘价亏损个数为 $failureCloseCount")
        println("------------------>第二天超过1%的亏损个数为 $failure1PointCount")
        println("------------------>第二天超过3%的亏损个数为 $failure3PointCount")
        println("------------------>第二天最低亏损为$mixProfit%")

        println("------------------>第二天有新高时量比大 $testSuccessHighTradeCount")
        println("------------------>第二天有新高时量比小 $testSuccessLowTradeCount")
        println("------------------>第二天强行亏损量比大 $testFailureHighTradeCount")
        println("------------------>第二天强行亏损量比小 $testFailureLowTradeCount")

        println("------------------>收盘价盈利总百分比例为 $successCloseProfit%")
        println("------------------>收盘价亏损总百分比例为 $failureCloseProfit%")

        println("------------------>垃圾位置点 $dustCount")
        println("------------------>成功时间点 $successDate")
        println("------------------>失败时间点 $failureDate")
        val temp = LinkedHashSet<String>().apply {
            addAll(dustDate)
        }
        dustDate.clear()
        dustDate.addAll(temp)
        println("------------------>垃圾时间点 $dustDate")

        var statisticsModel = StatisticsModel().apply {
            this.stockCode = stockCode
            this.stockName = stockName
            this.successHighCount = successHighCount
            this.successCloseCount = successCloseCount
            this.successCloseProfit = successCloseProfit
            this.success1PointCount = success1PointCount
            this.success2PointCount = success2PointCount
            this.success3PointCount = success3PointCount
            this.success4PointCount = success4PointCount
            this.success5PointCount = success5PointCount
            this.failureHighCount = failureHighCount
            this.failureCloseCount = failureCloseCount
            this.failureCloseProfit = failureCloseProfit
            this.failure1PointCount = failure1PointCount
            this.failure3PointCount = failure3PointCount
            this.keyCount = keyCount
            this.maxProfit = maxProfit
            this.minProfit = mixProfit
            this.statisticsType = statisticsType
            this.successDate = successDate.toString()
            this.failureDate = failureDate.toString()
        }

        statisticsModel.save()

        return statisticsModel
    }

    //顺序中均线以上
    fun judgeFiveSuccessRateFor5813(
        stockCode: String,
        stockName: String,
        datas: ArrayList<KLineEntity>,
        statisticsType: String,
        line: Int = 0,
        needThirdDay: Boolean = true
    ): StatisticsModel {
        if (datas.size < 12) return StatisticsModel()

        var data = ArrayList<KLineEntity>().apply { addAll(datas) }

        var successHighCount = 0
        var successCloseCount = 0
        var success1PointCount = 0
        var success2PointCount = 0
        var success3PointCount = 0
        var success4PointCount = 0
        var success5PointCount = 0
        var successCloseProfit = 0f
        var failureHighCount = 0
        var failureCloseCount = 0
        var failure1PointCount = 0
        var failure3PointCount = 0
        var failureCloseProfit = 0f
        var stockCostPrice: Float = 0f
        var keyCount = 0
        var keyFiveCount = 0
        var nextDayCount = 0
        var thirdDayCount = 0
        var runCount = 0
        var maxProfit = 0f
        var mixProfit = 0f

        var testSuccessHighTradeCount = 0
        var testSuccessLowTradeCount = 0

        var testFailureHighTradeCount = 0
        var testFailureLowTradeCount = 0

        var successDate = ArrayList<String>()
        var failureDate = ArrayList<String>()

        var dustCount = 0
        var dustDate = ArrayList<String>()

        val currcapital = LitePalDBase.queryCurrcapital(stockCode)

        for (i in 15..(data.size - 4)) {
            runCount += 1
            if ((data[i].Close - data[i - 1].Close) / data[i - 1].Close > BaseApplication.instance?.filterOptions?.startRate!! / 100f &&
                (data[i].Close - data[i - 1].Close) / data[i - 1].Close < BaseApplication.instance?.filterOptions?.endRate!! / 100f
            ) {

                val volumeRate = (data[i].volume / 240f).toInt()
                    .toFloat() / ((data[i - 1].volume + data[i - 2].volume + data[i - 3].volume + data[i - 4].volume + data[i - 5].volume) / 1200f).toInt()
                    .toFloat()

                if (needThirdDay && volumeRate < BaseApplication.instance?.filterOptions?.volumeStartRate!! * 0.1)
                    continue

                if (currcapital > 0 && (data[i].volume / currcapital > BaseApplication.instance?.filterOptions?.turnoverStartRate!! || data[i].volume / currcapital < BaseApplication.instance?.filterOptions?.turnoverEndRate!!))
                    continue

                data[i + 1].MA5Price =
                    decimalFormat3.format((data[i - 3].closePrice + data[i - 2].closePrice + data[i - 1].closePrice + data[i].closePrice + data[i + 1].openPrice) / 5f)
                        .toFloat()
                data[i + 1].MA8Price = decimalFormat3.format(
                    (data[i - 6].closePrice + data[i - 5].closePrice + data[i - 4].closePrice +
                            data[i - 3].closePrice + data[i - 2].closePrice + data[i - 1].closePrice + data[i].closePrice + data[i + 1].openPrice) / 8f
                ).toFloat()
                data[i + 1].MA13Price = decimalFormat3.format(
                    (data[i - 11].closePrice + data[i - 10].closePrice + data[i - 9].closePrice + data[i - 8].closePrice + data[i - 7].closePrice +
                            data[i - 6].closePrice + data[i - 5].closePrice + data[i - 4].closePrice +
                            data[i - 3].closePrice + data[i - 2].closePrice + data[i - 1].closePrice + data[i].closePrice + data[i + 1].openPrice) / 13f
                ).toFloat()

                if ((data[i - 3].mA5Price < data[i - 3].mA8Price || data[i - 3].mA8Price < data[i - 3].mA13Price) &&
                    (data[i - 2].mA5Price > data[i - 2].mA8Price && data[i - 2].mA8Price > data[i - 2].mA13Price) &&
                    (data[i - 1].mA5Price > data[i - 1].mA8Price && data[i - 1].mA8Price > data[i - 1].mA13Price) &&
                    (data[i].mA5Price > data[i].mA8Price && data[i].mA8Price > data[i].mA13Price) &&
                    data[i].Close > data[i].mA5Price &&
                    data[i + 1].High > data[i + 1].mA5Price && data[i + 1].Low < data[i + 1].mA5Price
                ) {

                    keyCount += 1

                    stockCostPrice = if (data[i + 1].Open < data[i + 1].mA5Price)
                        data[i + 1].Open
                    else
                        data[i + 1].mA5Price


                    keyFiveCount += 1

                    val date = data[i].Date
                    println("------------------>Date : $date")

                    if (data[i + 2].Close > stockCostPrice) {
                        successCloseCount += 1
                        successCloseProfit += decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100)
                            .toFloat()
                        successDate.add(
                            data[i].date + "(" + decimalFormat3.format(stockCostPrice) + " ::: " + decimalFormat3.format(
                                (data[i + 2].Close - stockCostPrice) / stockCostPrice * 100
                            ) + ")"
                        )
                    } else {
                        failureCloseCount += 1
                        failureCloseProfit += decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100)
                            .toFloat()
                        failureDate.add(
                            data[i].date + "(" + decimalFormat3.format(stockCostPrice) + " ::: " + decimalFormat3.format(
                                (data[i + 2].Close - stockCostPrice) / stockCostPrice * 100
                            ) + ")"
                        )
                    }

                    if (data[i + 2].High > stockCostPrice) {
                        successHighCount += 1
                        val highRate =
                            decimalFormat.format((data[i + 2].High - stockCostPrice) / stockCostPrice * 100)
                                .toFloat()
                        maxProfit += highRate
                        if (highRate > 1) success1PointCount += 1
                        if (highRate > 2) success2PointCount += 1
                        if (highRate > 3) success3PointCount += 1
                        if (highRate > 4) success4PointCount += 1
                        if (highRate > 5) success5PointCount += 1
                    } else {
                        failureHighCount += 1
                        mixProfit += decimalFormat.format((data[i + 2].High - stockCostPrice) / stockCostPrice * 100)
                            .toFloat()
                    }

                }
            }
        }

        println("------------------>总运行次数 $runCount")
        println("------------------>超5%的涨幅次数 $keyCount")
        println("------------------>超5%并穿过5日均线次数 $keyFiveCount")
        println("------------------>第二天达标的次数 $nextDayCount")
        println("------------------>第三天达标的次数 $thirdDayCount")

        println("------------------>第二天有高点的个数为 $successHighCount")
        println("------------------>第二天收盘价盈利个数为 $successCloseCount")
        println("------------------>第二天超过1%的盈利个数为 $success1PointCount")
        println("------------------>第二天超过3%的盈利个数为 $success3PointCount")
        println("------------------>第二天最高收益为$maxProfit%")

        println("------------------>第二天有低点的个数为 $failureHighCount")
        println("------------------>第二天收盘价亏损个数为 $failureCloseCount")
        println("------------------>第二天超过1%的亏损个数为 $failure1PointCount")
        println("------------------>第二天超过3%的亏损个数为 $failure3PointCount")
        println("------------------>第二天最低亏损为$mixProfit%")

        println("------------------>第二天有新高时量比大 $testSuccessHighTradeCount")
        println("------------------>第二天有新高时量比小 $testSuccessLowTradeCount")
        println("------------------>第二天强行亏损量比大 $testFailureHighTradeCount")
        println("------------------>第二天强行亏损量比小 $testFailureLowTradeCount")

        println("------------------>收盘价盈利总百分比例为 $successCloseProfit%")
        println("------------------>收盘价亏损总百分比例为 $failureCloseProfit%")

        println("------------------>垃圾位置点 $dustCount")
        println("------------------>成功时间点 $successDate")
        println("------------------>失败时间点 $failureDate")
        val temp = LinkedHashSet<String>().apply {
            addAll(dustDate)
        }
        dustDate.clear()
        dustDate.addAll(temp)
        println("------------------>垃圾时间点 $dustDate")

        var statisticsModel = StatisticsModel().apply {
            this.stockCode = stockCode
            this.stockName = stockName
            this.successHighCount = successHighCount
            this.successCloseCount = successCloseCount
            this.successCloseProfit = successCloseProfit
            this.success1PointCount = success1PointCount
            this.success3PointCount = success3PointCount
            this.failureHighCount = failureHighCount
            this.failureCloseCount = failureCloseCount
            this.failureCloseProfit = failureCloseProfit
            this.failure1PointCount = failure1PointCount
            this.failure3PointCount = failure3PointCount
            this.keyCount = keyCount
            this.maxProfit = maxProfit
            this.minProfit = mixProfit
            this.statisticsType = statisticsType
            this.successDate = successDate.toString()
            this.failureDate = failureDate.toString()
        }

        statisticsModel.save()

        return statisticsModel
    }

    //5813一天确认
    fun judgeFiveSuccessRateFor5813New(
        stockCode: String,
        stockName: String,
        datas: ArrayList<KLineEntity>,
        statisticsType: String,
        line: Int = 0,
        needThirdDay: Boolean = true
    ): StatisticsModel {
        if (datas.size < 12) return StatisticsModel()

        var data = ArrayList<KLineEntity>().apply { addAll(datas) }

        var successHighCount = 0
        var successCloseCount = 0
        var success1PointCount = 0
        var success2PointCount = 0
        var success3PointCount = 0
        var success4PointCount = 0
        var success5PointCount = 0
        var successCloseProfit = 0f
        var failureHighCount = 0
        var failureCloseCount = 0
        var failure1PointCount = 0
        var failure3PointCount = 0
        var failureCloseProfit = 0f
        var stockCostPrice: Float = 0f
        var keyCount = 0
        var keyFiveCount = 0
        var nextDayCount = 0
        var thirdDayCount = 0
        var runCount = 0
        var maxProfit = 0f
        var mixProfit = 0f

        var testSuccessHighTradeCount = 0
        var testSuccessLowTradeCount = 0

        var testFailureHighTradeCount = 0
        var testFailureLowTradeCount = 0

        var successDate = ArrayList<String>()
        var failureDate = ArrayList<String>()

        var dustCount = 0
        var dustDate = ArrayList<String>()

        val currcapital = LitePalDBase.queryCurrcapital(stockCode)

        for (i in 15..(data.size - 4)) {
            runCount += 1
            if ((data[i].Close - data[i - 1].Close) / data[i - 1].Close > BaseApplication.instance?.filterOptions?.startRate!! / 100f &&
                (data[i].Close - data[i - 1].Close) / data[i - 1].Close < BaseApplication.instance?.filterOptions?.endRate!! / 100f
            ) {

                val volumeRate = (data[i].volume / 240f).toInt()
                    .toFloat() / ((data[i - 1].volume + data[i - 2].volume + data[i - 3].volume + data[i - 4].volume + data[i - 5].volume) / 1200f).toInt()
                    .toFloat()

                if (needThirdDay && volumeRate < BaseApplication.instance?.filterOptions?.volumeStartRate!! * 0.1)
                    continue

                println("------------------>Volume : " + data[i].volume + "       currcapital : " + currcapital)
                println(
                    "------------------>Volume Rate : " + decimalFormat3.format(data[i].volume / currcapital)
                        .toFloat()
                )

                if (currcapital > 0 && (data[i].volume / currcapital < BaseApplication.instance?.filterOptions?.turnoverStartRate!! || data[i].volume / currcapital > BaseApplication.instance?.filterOptions?.turnoverEndRate!!))
                    continue

                data[i + 1].MA5Price =
                    decimalFormat3.format((data[i - 3].closePrice + data[i - 2].closePrice + data[i - 1].closePrice + data[i].closePrice + data[i + 1].openPrice) / 5f)
                        .toFloat()
                data[i + 1].MA8Price = decimalFormat3.format(
                    (data[i - 6].closePrice + data[i - 5].closePrice + data[i - 4].closePrice +
                            data[i - 3].closePrice + data[i - 2].closePrice + data[i - 1].closePrice + data[i].closePrice + data[i + 1].openPrice) / 8f
                ).toFloat()
                data[i + 1].MA13Price = decimalFormat3.format(
                    (data[i - 11].closePrice + data[i - 10].closePrice + data[i - 9].closePrice + data[i - 8].closePrice + data[i - 7].closePrice +
                            data[i - 6].closePrice + data[i - 5].closePrice + data[i - 4].closePrice +
                            data[i - 3].closePrice + data[i - 2].closePrice + data[i - 1].closePrice + data[i].closePrice + data[i + 1].openPrice) / 13f
                ).toFloat()

                val upNextShadowPoint =
                    (data[i].High - data[i].Close) * 2 < data[i].Open - data[i].Low

                if ((data[i - 1].mA5Price < data[i - 1].mA8Price || data[i - 1].mA8Price < data[i - 1].mA13Price || data[i - 1].mA5Price < data[i - 1].mA13Price) &&
                    (data[i].mA5Price > data[i].mA8Price && data[i].mA8Price > data[i].mA13Price) &&
                    data[i].Close > data[i].mA5Price && upNextShadowPoint &&
                    data[i + 1].High > data[i + 1].mA5Price && data[i + 1].Low < data[i + 1].mA5Price
                ) {

                    keyCount += 1

                    stockCostPrice = if (data[i + 1].Open < data[i + 1].mA5Price)
                        data[i + 1].Open
                    else
                        data[i + 1].mA5Price


                    keyFiveCount += 1

                    val date = data[i].Date
                    println("------------------>Date : $date")

                    if (data[i + 2].Close > stockCostPrice) {
                        successCloseCount += 1
                        successCloseProfit += decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100)
                            .toFloat()
                        successDate.add(
                            data[i].date + "(" + decimalFormat3.format(stockCostPrice) + "::" + decimalFormat3.format(
                                (data[i + 2].Close - stockCostPrice) / stockCostPrice * 100
                            ) + "%::" + decimalFormat3.format((data[i + 2].High - stockCostPrice) / stockCostPrice * 100) + "%)"
                        )
                    } else {
                        failureCloseCount += 1
                        failureCloseProfit += decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100)
                            .toFloat()
                        failureDate.add(
                            data[i].date + "(" + decimalFormat3.format(stockCostPrice) + "::" + decimalFormat3.format(
                                (data[i + 2].Close - stockCostPrice) / stockCostPrice * 100
                            ) + "%::" + decimalFormat3.format((data[i + 2].High - stockCostPrice) / stockCostPrice * 100) + ")"
                        )
                    }

                    if (data[i + 2].High > stockCostPrice) {
                        successHighCount += 1
                        val highRate =
                            decimalFormat.format((data[i + 2].High - stockCostPrice) / stockCostPrice * 100)
                                .toFloat()
                        maxProfit += highRate
                        if (highRate > 1) success1PointCount += 1
                        if (highRate > 2) success2PointCount += 1
                        if (highRate > 3) success3PointCount += 1
                        if (highRate > 4) success4PointCount += 1
                        if (highRate > 5) success5PointCount += 1
                    } else {
                        failureHighCount += 1
                        mixProfit += decimalFormat.format((data[i + 2].High - stockCostPrice) / stockCostPrice * 100)
                            .toFloat()
                    }

                }
            }
        }

        println("------------------>总运行次数 $runCount")
        println("------------------>超5%的涨幅次数 $keyCount")
        println("------------------>超5%并穿过5日均线次数 $keyFiveCount")
        println("------------------>第二天达标的次数 $nextDayCount")
        println("------------------>第三天达标的次数 $thirdDayCount")

        println("------------------>第二天有高点的个数为 $successHighCount")
        println("------------------>第二天收盘价盈利个数为 $successCloseCount")
        println("------------------>第二天超过1%的盈利个数为 $success1PointCount")
        println("------------------>第二天超过3%的盈利个数为 $success3PointCount")
        println("------------------>第二天最高收益为$maxProfit%")

        println("------------------>第二天有低点的个数为 $failureHighCount")
        println("------------------>第二天收盘价亏损个数为 $failureCloseCount")
        println("------------------>第二天超过1%的亏损个数为 $failure1PointCount")
        println("------------------>第二天超过3%的亏损个数为 $failure3PointCount")
        println("------------------>第二天最低亏损为$mixProfit%")

        println("------------------>第二天有新高时量比大 $testSuccessHighTradeCount")
        println("------------------>第二天有新高时量比小 $testSuccessLowTradeCount")
        println("------------------>第二天强行亏损量比大 $testFailureHighTradeCount")
        println("------------------>第二天强行亏损量比小 $testFailureLowTradeCount")

        println("------------------>收盘价盈利总百分比例为 $successCloseProfit%")
        println("------------------>收盘价亏损总百分比例为 $failureCloseProfit%")

        println("------------------>垃圾位置点 $dustCount")
        println("------------------>成功时间点 $successDate")
        println("------------------>失败时间点 $failureDate")
        val temp = LinkedHashSet<String>().apply {
            addAll(dustDate)
        }
        dustDate.clear()
        dustDate.addAll(temp)
        println("------------------>垃圾时间点 $dustDate")

        var statisticsModel = StatisticsModel().apply {
            this.stockCode = stockCode
            this.stockName = stockName
            this.successHighCount = successHighCount
            this.successCloseCount = successCloseCount
            this.successCloseProfit = successCloseProfit
            this.success1PointCount = success1PointCount
            this.success3PointCount = success3PointCount
            this.failureHighCount = failureHighCount
            this.failureCloseCount = failureCloseCount
            this.failureCloseProfit = failureCloseProfit
            this.failure1PointCount = failure1PointCount
            this.failure3PointCount = failure3PointCount
            this.keyCount = keyCount
            this.maxProfit = maxProfit
            this.minProfit = mixProfit
            this.statisticsType = statisticsType
            this.successDate = successDate.toString()
            this.failureDate = failureDate.toString()
        }

        statisticsModel.save()

        return statisticsModel
    }


    //顺序中均线以上3510二次确认
    fun judgeFiveSuccessRateFor3510(
        stockCode: String,
        stockName: String,
        datas: ArrayList<KLineEntity>,
        statisticsType: String,
        line: Int = 0,
        needThirdDay: Boolean = false
    ): StatisticsModel {
        if (datas.size < 12) return StatisticsModel()

        var data = ArrayList<KLineEntity>().apply { addAll(datas) }

        var successHighCount = 0
        var successCloseCount = 0
        var success1PointCount = 0
        var success2PointCount = 0
        var success3PointCount = 0
        var success4PointCount = 0
        var success5PointCount = 0
        var successCloseProfit = 0f
        var failureHighCount = 0
        var failureCloseCount = 0
        var failure1PointCount = 0
        var failure3PointCount = 0
        var failureCloseProfit = 0f
        var stockCostPrice: Float = 0f
        var keyCount = 0
        var keyFiveCount = 0
        var nextDayCount = 0
        var thirdDayCount = 0
        var runCount = 0
        var maxProfit = 0f
        var mixProfit = 0f

        var testSuccessHighTradeCount = 0
        var testSuccessLowTradeCount = 0

        var testFailureHighTradeCount = 0
        var testFailureLowTradeCount = 0

        var successDate = ArrayList<String>()
        var failureDate = ArrayList<String>()

        var dustCount = 0
        var dustDate = ArrayList<String>()

        var addTrade = false

        val currcapital = LitePalDBase.queryCurrcapital(stockCode)

        for (i in 15..(data.size - 4)) {
            runCount += 1
//            data[i - 3].MA3Price = (data[i - 3].Close + data[i - 4].Close + data[i - 5].Close) /3f
            data[i - 2].MA3Price = (data[i - 2].Close + data[i - 3].Close + data[i - 4].Close) / 3f
            data[i - 1].MA3Price = (data[i - 1].Close + data[i - 2].Close + data[i - 3].Close) / 3f
            data[i].MA3Price = (data[i].Close + data[i - 1].Close + data[i - 2].Close) / 3f
            data[i + 1].MA3Price = (data[i].Close + data[i - 1].Close + data[i + 1].Open) / 3f

            if ((data[i].Close - data[i - 1].Close) / data[i - 1].Close > (BaseApplication.instance?.filterOptions?.startRate!! / 100f) &&
                (data[i].Close - data[i - 1].Close) / data[i - 1].Close < (BaseApplication.instance?.filterOptions?.endRate!! / 100f)
            ) {

                val lowShadowPoint = if (data[i].Close >= data[i].Open) {
                    data[i].High - data[i].Close
                } else
                    data[i].High - data[i].Open

                val volumeRate = (data[i].volume / 240f).toInt()
                    .toFloat() / ((data[i - 1].volume + data[i - 2].volume + data[i - 3].volume + data[i - 4].volume + data[i - 5].volume) / 1200f).toInt()
                    .toFloat()

                if (needThirdDay && volumeRate < BaseApplication.instance?.filterOptions?.volumeStartRate!! * 0.1)
                    continue

                if (currcapital > 0 && (data[i].volume / currcapital < BaseApplication.instance?.filterOptions?.turnoverStartRate!! || data[i].volume / currcapital > BaseApplication.instance?.filterOptions?.turnoverEndRate!!))
                    continue

                if ((data[i - 2].mA3Price < data[i - 2].mA5Price || data[i - 2].mA5Price < data[i - 2].mA10Price || data[i - 2].mA3Price < data[i - 2].mA10Price) &&
                    (data[i - 1].mA3Price > data[i - 1].mA5Price && data[i - 1].mA5Price > data[i - 1].mA10Price) &&
                    (data[i].mA3Price > data[i].mA5Price && data[i].mA5Price > data[i].mA10Price) &&
                    data[i].Close > data[i].mA3Price && data[i].Open > data[i].mA3Price &&
                    data[i + 1].High > data[i + 1].mA3Price && data[i + 1].Low < data[i + 1].mA3Price &&
                    (lowShadowPoint / data[i - 1].Close) < 0.02
                ) {

                    keyCount += 1

//                    addTrade = data[i + 1].High > data[i + 1].mA3Price && data[i + 1].Low <= data[i + 1].mA3Price &&
//                            data[i + 1].High > data[i + 1].mA5Price && data[i + 1].Low <= data[i + 1].mA5Price

//                    stockCostPrice = if (data[i + 1].Open < data[i + 1].mA3Price)
//                        data[i + 1].Open
//                    else
//                        data[i + 1].mA3Price

                    stockCostPrice = if (data[i + 1].Open < data[i + 1].mA5Price) {
                        data[i + 1].Open
                    } else if (data[i + 1].Open < data[i + 1].mA3Price && addTrade)
                        (data[i + 1].Open + data[i + 1].mA5Price) / 2f
                    else if (addTrade) {
                        (data[i + 1].mA3Price + data[i + 1].mA5Price) / 2f
                    } else data[i + 1].mA3Price


                    keyFiveCount += 1

                    val date = data[i].Date
                    println("------------------>Date : $date")

                    if (data[i + 2].Close > stockCostPrice) {
                        successCloseCount += 1
                        if (addTrade) {
                            successCloseProfit += decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100 * 2)
                                .toFloat()
                            successDate.add(
                                data[i].date + "(" + decimalFormat3.format(
                                    stockCostPrice
                                ) + "::" + decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100 * 2) + "%::" + decimalFormat.format(
                                    (data[i + 2].High - stockCostPrice) / stockCostPrice * 100 * 2
                                ) + "%)"
                            )
                        } else {
                            successCloseProfit += decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100)
                                .toFloat()
                            successDate.add(
                                data[i].date + "(" + decimalFormat3.format(
                                    stockCostPrice
                                ) + "::" + decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100) + "%::" + decimalFormat.format(
                                    (data[i + 2].High - stockCostPrice) / stockCostPrice * 100
                                ) + "%)"
                            )
                        }
                    } else {
                        failureCloseCount += 1
                        if (addTrade) {
                            failureCloseProfit += decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100 * 2)
                                .toFloat()
                            failureDate.add(
                                data[i].date + "(" + decimalFormat3.format(
                                    stockCostPrice
                                ) + "::" + decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100 * 2) + "%::" + decimalFormat.format(
                                    (data[i + 2].High - stockCostPrice) / stockCostPrice * 100 * 2
                                ) + "%)"
                            )
                        } else {
                            failureCloseProfit += decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100)
                                .toFloat()
                            failureDate.add(
                                data[i].date + "(" + decimalFormat3.format(
                                    stockCostPrice
                                ) + "::" + decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100) + "%::" + decimalFormat.format(
                                    (data[i + 2].High - stockCostPrice) / stockCostPrice * 100
                                ) + "%)"
                            )
                        }
                    }

                    if (data[i + 2].High > stockCostPrice) {
                        successHighCount += 1
                        val highRate =
                            decimalFormat.format((data[i + 2].High - stockCostPrice) / stockCostPrice * 100)
                                .toFloat()
                        maxProfit += highRate
                        if (highRate > 1) success1PointCount += 1
                        if (highRate > 2) success2PointCount += 1
                        if (highRate > 3) success3PointCount += 1
                        if (highRate > 4) success4PointCount += 1
                        if (highRate > 5) success5PointCount += 1
                    } else {
                        failureHighCount += 1
                        mixProfit += decimalFormat.format((data[i + 2].High - stockCostPrice) / stockCostPrice * 100)
                            .toFloat()
                    }

                }
            }
        }

        println("------------------>总运行次数 $runCount")
        println("------------------>超5%的涨幅次数 $keyCount")
        println("------------------>超5%并穿过5日均线次数 $keyFiveCount")
        println("------------------>第二天达标的次数 $nextDayCount")
        println("------------------>第三天达标的次数 $thirdDayCount")

        println("------------------>第二天有高点的个数为 $successHighCount")
        println("------------------>第二天收盘价盈利个数为 $successCloseCount")
        println("------------------>第二天超过1%的盈利个数为 $success1PointCount")
        println("------------------>第二天超过3%的盈利个数为 $success3PointCount")
        println("------------------>第二天最高收益为$maxProfit%")

        println("------------------>第二天有低点的个数为 $failureHighCount")
        println("------------------>第二天收盘价亏损个数为 $failureCloseCount")
        println("------------------>第二天超过1%的亏损个数为 $failure1PointCount")
        println("------------------>第二天超过3%的亏损个数为 $failure3PointCount")
        println("------------------>第二天最低亏损为$mixProfit%")

        println("------------------>第二天有新高时量比大 $testSuccessHighTradeCount")
        println("------------------>第二天有新高时量比小 $testSuccessLowTradeCount")
        println("------------------>第二天强行亏损量比大 $testFailureHighTradeCount")
        println("------------------>第二天强行亏损量比小 $testFailureLowTradeCount")

        println("------------------>收盘价盈利总百分比例为 $successCloseProfit%")
        println("------------------>收盘价亏损总百分比例为 $failureCloseProfit%")

        println("------------------>垃圾位置点 $dustCount")
        println("------------------>成功时间点 $successDate")
        println("------------------>失败时间点 $failureDate")
        val temp = LinkedHashSet<String>().apply {
            addAll(dustDate)
        }
        dustDate.clear()
        dustDate.addAll(temp)
        println("------------------>垃圾时间点 $dustDate")

        var statisticsModel = StatisticsModel().apply {
            this.stockCode = stockCode
            this.stockName = stockName
            this.successHighCount = successHighCount
            this.successCloseCount = successCloseCount
            this.successCloseProfit = successCloseProfit
            this.success1PointCount = success1PointCount
            this.success2PointCount = success2PointCount
            this.success3PointCount = success3PointCount
            this.success4PointCount = success4PointCount
            this.success5PointCount = success5PointCount
            this.failureHighCount = failureHighCount
            this.failureCloseCount = failureCloseCount
            this.failureCloseProfit = failureCloseProfit
            this.failure1PointCount = failure1PointCount
            this.failure3PointCount = failure3PointCount
            this.keyCount = keyCount
            this.maxProfit = maxProfit
            this.minProfit = mixProfit
            this.statisticsType = statisticsType
            this.successDate = successDate.toString()
            this.failureDate = failureDate.toString()
        }

        statisticsModel.save()

        return statisticsModel
    }

    //当天一穿三，收前入
    fun judgeFiveSuccessRateForToday(
        stockCode: String,
        stockName: String,
        datas: ArrayList<KLineEntity>,
        statisticsType: String,
        line: Int = 0,
        needThirdDay: Boolean = true
    ): StatisticsModel {
        if (datas.size < 12) return StatisticsModel()

        var data = ArrayList<KLineEntity>().apply { addAll(datas) }

        var successHighCount = 0
        var successCloseCount = 0
        var success1PointCount = 0
        var success3PointCount = 0
        var successCloseProfit = 0f
        var failureHighCount = 0
        var failureCloseCount = 0
        var failure1PointCount = 0
        var failure3PointCount = 0
        var failureCloseProfit = 0f
        var stockCostPrice: Float = 0f
        var keyCount = 0
        var keyFiveCount = 0
        var nextDayCount = 0
        var thirdDayCount = 0
        var runCount = 0
        var maxProfit = 0f
        var mixProfit = 0f

        var testSuccessHighTradeCount = 0
        var testSuccessLowTradeCount = 0

        var testFailureHighTradeCount = 0
        var testFailureLowTradeCount = 0

        var successDate = ArrayList<String>()
        var failureDate = ArrayList<String>()

        var dustCount = 0
        var dustDate = ArrayList<String>()

        for (i in 10..(data.size - 4)) {
            runCount += 1
            if ((data[i].Close - data[i - 1].Close) / data[i - 1].Close > 0.05f) {

                val status = when (BaseApplication.instance?.filterOptions?.throughType) {
                    ThroughType.NormalThrough.type -> data[i].Open < data[i].mA5Price && data[i].Close > data[i].mA5Price &&
                            data[i].Open < data[i].mA8Price && data[i].Close > data[i].mA8Price &&
                            data[i].Open < data[i].mA13Price && data[i].Close > data[i].mA13Price
                    ThroughType.HighThrough.type -> data[i].Open < data[i].mA5Price && data[i].Close > data[i].mA5Price &&
                            data[i].Open < data[i].mA8Price && data[i].Close > data[i].mA8Price &&
                            data[i].Open < data[i].mA13Price && data[i].Close > data[i].mA13Price &&
                            data[i].Close - (data[i].mA5Price + data[i].mA8Price + data[i].mA13Price) / 3 > (data[i].mA5Price + data[i].mA8Price + data[i].mA13Price) / 3 - data[i].Open
                    ThroughType.ThroughAndTrade.type -> data[i].Open < data[i].mA5Price && data[i].Close > data[i].mA5Price &&
                            data[i].Open < data[i].mA8Price && data[i].Close > data[i].mA8Price &&
                            data[i].Open < data[i].mA13Price && data[i].Close > data[i].mA13Price &&
                            data[i].Volume > data[i - 1].Volume * 2
                    ThroughType.HighThroughAndTrade.type -> data[i].Open < data[i].mA5Price && data[i].Close > data[i].mA5Price &&
                            data[i].Open < data[i].mA8Price && data[i].Close > data[i].mA8Price &&
                            data[i].Open < data[i].mA13Price && data[i].Close > data[i].mA13Price &&
                            data[i].Close - (data[i].mA5Price + data[i].mA8Price + data[i].mA13Price) / 3 > (data[i].mA5Price + data[i].mA8Price + data[i].mA13Price) / 3 - data[i].Open &&
                            data[i].Volume > data[i - 1].Volume * 2
                    else -> data[i].Open < data[i].mA5Price && data[i].Close > data[i].mA5Price &&
                            data[i].Open < data[i].mA8Price && data[i].Close > data[i].mA8Price &&
                            data[i].Open < data[i].mA13Price && data[i].Close > data[i].mA13Price
                }

                if (status && (data[i].Close - data[i - 1].Close) / data[i - 1].Close < 9.9) {

                    keyCount += 1

                    stockCostPrice = data[i].Close

                    if (!needThirdDay) {
                        nextDayCount += 1

                        val date = data[i].Date
                        println("------------------>Date : $date")

                        if (data[i + 1].Close > stockCostPrice) {
                            successCloseCount += 1
                            successCloseProfit += decimalFormat.format((data[i + 1].Close - stockCostPrice) / stockCostPrice * 100)
                                .toFloat()
                        } else {
                            failureCloseCount += 1
                            failureCloseProfit += decimalFormat.format((data[i + 1].Close - stockCostPrice) / stockCostPrice * 100)
                                .toFloat()
                        }

                        if (data[i + 1].High > stockCostPrice) {
                            successHighCount += 1
                            maxProfit =
                                decimalFormat.format((data[i + 1].High - stockCostPrice) / stockCostPrice * 100)
                                    .toFloat()
                        } else {
                            failureHighCount += 1
                            mixProfit =
                                decimalFormat.format((data[i + 1].High - stockCostPrice) / stockCostPrice * 100)
                                    .toFloat()
                        }
                    } else {
                        if (data[i + 1].Close > stockCostPrice) {
                            successCloseCount += 1
                            successCloseProfit += decimalFormat.format((data[i + 1].Close - stockCostPrice) / stockCostPrice * 100)
                                .toFloat()
                        } else if (data[i + 2].Close > stockCostPrice) {
                            successCloseCount += 1
                            successCloseProfit += decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100)
                                .toFloat()
                        } else {
                            failureCloseCount += 1
                            failureCloseProfit += decimalFormat.format((data[i + 1].Close - stockCostPrice) / stockCostPrice * 100)
                                .toFloat()
                        }

                        if (data[i + 1].High > stockCostPrice) {
                            successHighCount += 1
                            maxProfit =
                                decimalFormat.format((data[i + 1].High - stockCostPrice) / stockCostPrice * 100)
                                    .toFloat()
                        } else if (data[i + 2].High > stockCostPrice) {
                            successHighCount += 1
                            maxProfit =
                                decimalFormat.format((data[i + 1].High - stockCostPrice) / stockCostPrice * 100)
                                    .toFloat()
                        } else {
                            failureHighCount += 1
                            mixProfit =
                                decimalFormat.format((data[i + 2].High - stockCostPrice) / stockCostPrice * 100)
                                    .toFloat()
                        }
                    }
                }
            }
        }

        println("------------------>总运行次数 $runCount")
        println("------------------>超5%的涨幅次数 $keyCount")
        println("------------------>超5%并穿过5日均线次数 $keyFiveCount")
        println("------------------>第二天达标的次数 $nextDayCount")
        println("------------------>第三天达标的次数 $thirdDayCount")

        println("------------------>第二天有高点的个数为 $successHighCount")
        println("------------------>第二天收盘价盈利个数为 $successCloseCount")
        println("------------------>第二天超过1%的盈利个数为 $success1PointCount")
        println("------------------>第二天超过3%的盈利个数为 $success3PointCount")
        println("------------------>第二天最高收益为$maxProfit%")

        println("------------------>第二天有低点的个数为 $failureHighCount")
        println("------------------>第二天收盘价亏损个数为 $failureCloseCount")
        println("------------------>第二天超过1%的亏损个数为 $failure1PointCount")
        println("------------------>第二天超过3%的亏损个数为 $failure3PointCount")
        println("------------------>第二天最低亏损为$mixProfit%")

        println("------------------>第二天有新高时量比大 $testSuccessHighTradeCount")
        println("------------------>第二天有新高时量比小 $testSuccessLowTradeCount")
        println("------------------>第二天强行亏损量比大 $testFailureHighTradeCount")
        println("------------------>第二天强行亏损量比小 $testFailureLowTradeCount")

        println("------------------>收盘价盈利总百分比例为 $successCloseProfit%")
        println("------------------>收盘价亏损总百分比例为 $failureCloseProfit%")

        println("------------------>垃圾位置点 $dustCount")
        println("------------------>成功时间点 $successDate")
        println("------------------>失败时间点 $failureDate")
        val temp = LinkedHashSet<String>().apply {
            addAll(dustDate)
        }
        dustDate.clear()
        dustDate.addAll(temp)
        println("------------------>垃圾时间点 $dustDate")

        var statisticsModel = StatisticsModel().apply {
            this.stockCode = stockCode
            this.stockName = stockName
            this.successHighCount = successHighCount
            this.successCloseCount = successCloseCount
            this.successCloseProfit = successCloseProfit
            this.success1PointCount = success1PointCount
            this.success3PointCount = success3PointCount
            this.failureHighCount = failureHighCount
            this.failureCloseCount = failureCloseCount
            this.failureCloseProfit = failureCloseProfit
            this.failure1PointCount = failure1PointCount
            this.failure3PointCount = failure3PointCount
            this.keyCount = nextDayCount + thirdDayCount
            this.maxProfit = maxProfit
            this.minProfit = mixProfit
            this.statisticsType = statisticsType
        }

        statisticsModel.save()

        return statisticsModel
    }

    //剧情反转
    fun judgeFiveSuccessRateForRare(
        stockCode: String,
        stockName: String,
        datas: ArrayList<KLineEntity>,
        statisticsType: String,
        line: Int = 0,
        needThirdDay: Boolean = true
    ): StatisticsModel {
        if (datas.size < 12) return StatisticsModel()

        var data = ArrayList<KLineEntity>().apply { addAll(datas) }

        var successHighCount = 0
        var successCloseCount = 0
        var success1PointCount = 0
        var success2PointCount = 0
        var success3PointCount = 0
        var success4PointCount = 0
        var success5PointCount = 0
        var successCloseProfit = 0f
        var failureHighCount = 0
        var failureCloseCount = 0
        var failure1PointCount = 0
        var failure3PointCount = 0
        var failureCloseProfit = 0f
        var stockCostPrice: Float = 0f
        var keyCount = 0
        var keyFiveCount = 0
        var nextDayCount = 0
        var thirdDayCount = 0
        var runCount = 0
        var maxProfit = 0f
        var mixProfit = 0f

        var testSuccessHighTradeCount = 0
        var testSuccessLowTradeCount = 0

        var testFailureHighTradeCount = 0
        var testFailureLowTradeCount = 0

        var successDate = ArrayList<String>()
        var failureDate = ArrayList<String>()

        var dustCount = 0
        var dustDate = ArrayList<String>()

        var addTrade = false

        val currcapital = LitePalDBase.queryCurrcapital(stockCode)

        for (i in 15..(data.size - 4)) {
            runCount += 1

            data[i - 2].MA3Price = (data[i - 2].Close + data[i - 3].Close + data[i - 4].Close) / 3f
            data[i - 1].MA3Price = (data[i - 1].Close + data[i - 2].Close + data[i - 3].Close) / 3f
            data[i].MA3Price = (data[i].Close + data[i - 1].Close + data[i - 2].Close) / 3f
            data[i + 1].MA3Price = (data[i].Close + data[i - 1].Close + data[i + 1].Open) / 3f

            if ((data[i].Close - data[i - 1].Close) / data[i - 1].Close > BaseApplication.instance?.filterOptions?.startRate!! / 100f &&
                (data[i].Close - data[i - 1].Close) / data[i - 1].Close < BaseApplication.instance?.filterOptions?.endRate!! / 100f
            ) {

                val upShadowPoint = if (data[i].Close >= data[i].Open) {
                    data[i].High - data[i].Close
                } else
                    data[i].High - data[i].Open

                val volumeRate = (data[i].volume / 240f).toInt()
                    .toFloat() / ((data[i - 1].volume + data[i - 2].volume + data[i - 3].volume + data[i - 4].volume + data[i - 5].volume) / 1200f).toInt()
                    .toFloat()

                if (needThirdDay && volumeRate < BaseApplication.instance?.filterOptions?.volumeStartRate!! * 0.1f)
                    continue

                println("------------------>1111-----" + data[i].volume / currcapital)

                if (currcapital > 0 && (data[i].volume / currcapital < BaseApplication.instance?.filterOptions?.turnoverStartRate!! || data[i].volume / currcapital > BaseApplication.instance?.filterOptions?.turnoverEndRate!!))
                    continue

                if ((((data[i - 1].mA3Price < data[i - 1].mA5Price || data[i - 1].mA3Price < data[i - 1].mA10Price || data[i - 1].mA5Price < data[i - 1].mA10Price) &&
                            data[i].mA3Price > data[i].mA5Price && data[i].mA5Price > data[i].mA10Price &&
                            data[i].Close > data[i].mA3Price && data[i].Open > data[i].mA10Price &&
                            data[i + 1].High > data[i + 1].mA3Price && data[i + 1].Low <= data[i + 1].mA3Price
                            && data[i + 1].Open > data[i + 1].mA10Price)) &&
                    (upShadowPoint / data[i - 1].Close) < 0.02
                ) {

                    keyCount += 1

//                    addTrade = data[i + 1].High > data[i + 1].mA3Price && data[i + 1].Low <= data[i + 1].mA3Price &&
//                            data[i + 1].High > data[i + 1].mA5Price && data[i + 1].Low <= data[i + 1].mA5Price

                    stockCostPrice = if (data[i + 1].Open < data[i + 1].mA5Price) {
                        data[i + 1].Open
                    } else if (data[i + 1].Open < data[i + 1].mA3Price && addTrade)
                        (data[i + 1].Open + data[i + 1].mA5Price) / 2f
                    else if (addTrade) {
                        (data[i + 1].mA3Price + data[i + 1].mA5Price) / 2f
                    } else data[i + 1].mA3Price

                    keyFiveCount += 1

                    val date = data[i].Date
                    println("------------------>Date : $date")

                    if ((data[i + 2].Open - stockCostPrice) / stockCostPrice * 100 > 5 && !needThirdDay) {
                        successCloseCount += 1
                        successCloseProfit += decimalFormat.format((data[i + 2].Open - stockCostPrice) / stockCostPrice * 100)
                            .toFloat()
                        successDate.add(
                            data[i].date + "(" + decimalFormat3.format(stockCostPrice) + "::" + decimalFormat.format(
                                (data[i + 2].Open - stockCostPrice) / stockCostPrice * 100
                            ) + "%::" + decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100) + "%)开"
                        )
                    } else if ((data[i + 2].Open - stockCostPrice) / stockCostPrice * 100 < -3) {
                        failureCloseCount += 1
                        failureCloseProfit += decimalFormat.format((data[i + 2].Open - stockCostPrice) / stockCostPrice * 100)
                            .toFloat()
                        failureDate.add(
                            data[i].date + "(" + decimalFormat3.format(stockCostPrice) + "::" + decimalFormat.format(
                                (data[i + 2].Open - stockCostPrice) / stockCostPrice * 100
                            ) + "%::" + decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100) + "%)开"
                        )
                    } else if ((data[i + 2].High - stockCostPrice) / stockCostPrice * 100 > 3 && !needThirdDay) {
                        successCloseCount += 1
                        successCloseProfit += 3
                        successDate.add(
                            data[i].date + "(" + decimalFormat3.format(stockCostPrice) + "::3%::" + decimalFormat.format(
                                (data[i + 2].Close - stockCostPrice) / stockCostPrice * 100
                            ) + "%)中"
                        )
                    } else if ((data[i + 2].Low - stockCostPrice) / stockCostPrice * 100 < -3) {
                        failureCloseCount += 1
                        failureCloseProfit += -3
                        failureDate.add(
                            data[i].date + "(" + decimalFormat3.format(stockCostPrice) + "::-3%::" + decimalFormat.format(
                                (data[i + 2].Close - stockCostPrice) / stockCostPrice * 100
                            ) + "%)中"
                        )
                    } else if (data[i + 2].Close > stockCostPrice) {
                        successCloseCount += 1
                        successCloseProfit += decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100)
                            .toFloat()
                        successDate.add(
                            data[i].date + "(" + decimalFormat3.format(stockCostPrice) + "::" + decimalFormat.format(
                                (data[i + 2].Close - stockCostPrice) / stockCostPrice * 100
                            ) + "%::" + decimalFormat.format((data[i + 2].High - stockCostPrice) / stockCostPrice * 100) + "%)常"
                        )
                    } else {
                        failureCloseCount += 1
                        failureCloseProfit += decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100)
                            .toFloat()
                        failureDate.add(
                            data[i].date + "(" + decimalFormat3.format(stockCostPrice) + "::" + decimalFormat.format(
                                (data[i + 2].Close - stockCostPrice) / stockCostPrice * 100
                            ) + "%::" + decimalFormat.format((data[i + 2].High - stockCostPrice) / stockCostPrice * 100) + "%)常"
                        )
                    }

//                    if (data[i + 2].Close > stockCostPrice){
//                        if (addTrade){
//                            successCloseProfit += decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100 * 2)
//                                .toFloat()
//                            successDate.add(data[i].date + "(" + decimalFormat3.format(stockCostPrice) + "::" + decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100 * 2) + "%::" + decimalFormat.format((data[i + 2].High - stockCostPrice) / stockCostPrice * 100 * 2) + "%)")
//                        }else {
//                            successCloseProfit += decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100)
//                                .toFloat()
//                            successDate.add(data[i].date + "(" + decimalFormat3.format(stockCostPrice) + "::" + decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100) + "%::" + decimalFormat.format((data[i + 2].High - stockCostPrice) / stockCostPrice * 100) + "%)")
//                        }
//                    }else{
//                        failureCloseCount += 1
//                        if (addTrade){
//                            failureCloseProfit += decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100 * 2)
//                                .toFloat()
//                            failureDate.add(data[i].date + "(" + decimalFormat3.format(stockCostPrice) + "::" + decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100 * 2) + "%::" + decimalFormat.format((data[i + 2].High - stockCostPrice) / stockCostPrice * 100 * 2) + "%)")
//                        }else {
//                            failureCloseProfit += decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100)
//                                .toFloat()
//                            failureDate.add(data[i].date + "(" + decimalFormat3.format(stockCostPrice) + "::" + decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100) + "%::" + decimalFormat.format((data[i + 2].High - stockCostPrice) / stockCostPrice * 100) + "%)")
//                        }
//                    }

                    if (data[i + 2].High > stockCostPrice) {
                        successHighCount += 1
                        val highRate =
                            decimalFormat.format((data[i + 2].High - stockCostPrice) / stockCostPrice * 100)
                                .toFloat()
                        maxProfit += highRate
                        if (highRate > 1) success1PointCount += 1
                        if (highRate > 2) success2PointCount += 1
                        if (highRate > 3) success3PointCount += 1
                        if (highRate > 4) success4PointCount += 1
                        if (highRate > 5) success5PointCount += 1
                    } else {
                        failureHighCount += 1
                        mixProfit += decimalFormat.format((data[i + 2].High - stockCostPrice) / stockCostPrice * 100)
                            .toFloat()
                    }

                }
            }
        }

        println("------------------>总运行次数 $runCount")
        println("------------------>超5%的涨幅次数 $keyCount")
        println("------------------>超5%并穿过5日均线次数 $keyFiveCount")
        println("------------------>第二天达标的次数 $nextDayCount")
        println("------------------>第三天达标的次数 $thirdDayCount")

        println("------------------>第二天有高点的个数为 $successHighCount")
        println("------------------>第二天收盘价盈利个数为 $successCloseCount")
        println("------------------>第二天超过1%的盈利个数为 $success1PointCount")
        println("------------------>第二天超过3%的盈利个数为 $success3PointCount")
        println("------------------>第二天最高收益为$maxProfit%")

        println("------------------>第二天有低点的个数为 $failureHighCount")
        println("------------------>第二天收盘价亏损个数为 $failureCloseCount")
        println("------------------>第二天超过1%的亏损个数为 $failure1PointCount")
        println("------------------>第二天超过3%的亏损个数为 $failure3PointCount")
        println("------------------>第二天最低亏损为$mixProfit%")

        println("------------------>第二天有新高时量比大 $testSuccessHighTradeCount")
        println("------------------>第二天有新高时量比小 $testSuccessLowTradeCount")
        println("------------------>第二天强行亏损量比大 $testFailureHighTradeCount")
        println("------------------>第二天强行亏损量比小 $testFailureLowTradeCount")

        println("------------------>收盘价盈利总百分比例为 $successCloseProfit%")
        println("------------------>收盘价亏损总百分比例为 $failureCloseProfit%")

        println("------------------>垃圾位置点 $dustCount")
        println("------------------>成功时间点 $successDate")
        println("------------------>失败时间点 $failureDate")
        val temp = LinkedHashSet<String>().apply {
            addAll(dustDate)
        }
        dustDate.clear()
        dustDate.addAll(temp)
        println("------------------>垃圾时间点 $dustDate")

        var statisticsModel = StatisticsModel().apply {
            this.stockCode = stockCode
            this.stockName = stockName
            this.successHighCount = successHighCount
            this.successCloseCount = successCloseCount
            this.successCloseProfit = successCloseProfit
            this.success1PointCount = success1PointCount
            this.success2PointCount = success2PointCount
            this.success3PointCount = success3PointCount
            this.success4PointCount = success4PointCount
            this.success5PointCount = success5PointCount
            this.failureHighCount = failureHighCount
            this.failureCloseCount = failureCloseCount
            this.failureCloseProfit = failureCloseProfit
            this.failure1PointCount = failure1PointCount
            this.failure3PointCount = failure3PointCount
            this.keyCount = keyCount
            this.maxProfit = maxProfit
            this.minProfit = mixProfit
            this.statisticsType = statisticsType
            this.successDate = successDate.toString()
            this.failureDate = failureDate.toString()
        }

        statisticsModel.save()

        return statisticsModel
    }

    //剧情反转
    fun judgeFiveSuccessRateForRareModify(
        stockCode: String,
        stockName: String,
        datas: ArrayList<KLineEntity>,
        statisticsType: String,
        line: Int = 0,
        needThirdDay: Boolean = true
    ): StatisticsModel {
        if (datas.size < 12) return StatisticsModel()

        var data = ArrayList<KLineEntity>().apply { addAll(datas) }

        var successHighCount = 0
        var successCloseCount = 0
        var success1PointCount = 0
        var success2PointCount = 0
        var success3PointCount = 0
        var success4PointCount = 0
        var success5PointCount = 0
        var successCloseProfit = 0f
        var failureHighCount = 0
        var failureCloseCount = 0
        var failure1PointCount = 0
        var failure3PointCount = 0
        var failureCloseProfit = 0f
        var stockCostPrice: Float = 0f
        var keyCount = 0
        var keyFiveCount = 0
        var nextDayCount = 0
        var thirdDayCount = 0
        var runCount = 0
        var maxProfit = 0f
        var mixProfit = 0f

        var testSuccessHighTradeCount = 0
        var testSuccessLowTradeCount = 0

        var testFailureHighTradeCount = 0
        var testFailureLowTradeCount = 0

        var successDate = ArrayList<String>()
        var failureDate = ArrayList<String>()

        var dustCount = 0
        var dustDate = ArrayList<String>()

        var addTrade = false

        val currcapital = LitePalDBase.queryCurrcapital(stockCode)

        for (i in 15..(data.size - 4)) {
            runCount += 1

            data[i - 2].MA3Price = (data[i - 2].Close + data[i - 3].Close + data[i - 4].Close) / 3f
            data[i - 1].MA3Price = (data[i - 1].Close + data[i - 2].Close + data[i - 3].Close) / 3f
            data[i].MA3Price = (data[i].Close + data[i - 1].Close + data[i - 2].Close) / 3f
            data[i + 1].MA3Price = (data[i].Close + data[i - 1].Close + data[i + 1].Open) / 3f

            if ((data[i].Close - data[i - 1].Close) / data[i - 1].Close > BaseApplication.instance?.filterOptions?.startRate!! / 100f &&
                (data[i].Close - data[i - 1].Close) / data[i - 1].Close < BaseApplication.instance?.filterOptions?.endRate!! / 100f
            ) {

                val downShadowPoint = if (data[i].Close >= data[i].Open) {
                    data[i].High - data[i].Close
                } else
                    data[i].High - data[i].Open

                val upNextShadowPoint = if (data[i + 1].Close >= data[i + 1].Open) {
                    data[i + 1].High - data[i + 1].Close < data[i + 1].Open - data[i + 1].Low
                } else
                    data[i + 1].High - data[i + 1].Open < data[i + 1].Close - data[i + 1].Low

                val volumeRate = (data[i].volume / 240f).toInt()
                    .toFloat() / ((data[i - 1].volume + data[i - 2].volume + data[i - 3].volume + data[i - 4].volume + data[i - 5].volume) / 1200f).toInt()
                    .toFloat()

                if (needThirdDay && volumeRate < BaseApplication.instance?.filterOptions?.volumeStartRate!! * 0.1f)
                    continue

                println("------------------>1111-----" + data[i].volume / currcapital)

                if (currcapital > 0 && (data[i].volume / currcapital < BaseApplication.instance?.filterOptions?.turnoverStartRate!! || data[i].volume / currcapital > BaseApplication.instance?.filterOptions?.turnoverEndRate!!))
                    continue

                if ((((data[i - 1].mA3Price < data[i - 1].mA5Price || data[i - 1].mA3Price < data[i - 1].mA10Price || data[i - 1].mA5Price < data[i - 1].mA10Price) &&
                            data[i].mA3Price > data[i].mA5Price && data[i].mA5Price > data[i].mA10Price &&
                            data[i].Close > data[i].mA3Price && data[i].Open > data[i].mA10Price &&
                            data[i + 1].Close > data[i + 1].Open &&
                            abs(data[i + 1].closePrice - data[i + 1].Open) / data[i].closePrice < 0.015)) && upNextShadowPoint
                ) {

                    keyCount += 1

//                    addTrade = data[i + 1].High > data[i + 1].mA3Price && data[i + 1].Low <= data[i + 1].mA3Price &&
//                            data[i + 1].High > data[i + 1].mA5Price && data[i + 1].Low <= data[i + 1].mA5Price

                    stockCostPrice = data[i + 1].closePrice

                    keyFiveCount += 1

                    val date = data[i].Date
                    println("------------------>Date : $date")

//                    if ((data[i + 2].Open - stockCostPrice) / stockCostPrice * 100 > 2 && !needThirdDay){
//                        successCloseCount += 1
//                        successCloseProfit += decimalFormat.format((data[i + 2].Open - stockCostPrice) / stockCostPrice * 100).toFloat()
//                        successDate.add(data[i].date + "(" + decimalFormat3.format(stockCostPrice) + "::" + decimalFormat.format((data[i + 2].Open - stockCostPrice) / stockCostPrice * 100) + "%::" + decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100) +"%)开")
//                    }else if ((data[i + 2].Open - stockCostPrice) / stockCostPrice * 100 < -2){
//                        failureCloseCount += 1
//                        failureCloseProfit += decimalFormat.format((data[i + 2].Open - stockCostPrice) / stockCostPrice * 100).toFloat()
//                        failureDate.add(data[i].date + "(" + decimalFormat3.format(stockCostPrice) + "::" + decimalFormat.format((data[i + 2].Open - stockCostPrice) / stockCostPrice * 100) + "%::" + decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100) + "%)开")
//                    }else if ((data[i + 2].High - stockCostPrice) / stockCostPrice * 100 > 2 && !needThirdDay){
//                        successCloseCount += 1
//                        successCloseProfit += 2
//                        successDate.add(data[i].date + "(" + decimalFormat3.format(stockCostPrice) + "::2%::" + decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100) + "%)中")
//                    }else if ((data[i + 2].Low - stockCostPrice) / stockCostPrice * 100 < -2){
//                        failureCloseCount += 1
//                        failureCloseProfit += -2
//                        failureDate.add(data[i].date + "(" + decimalFormat3.format(stockCostPrice) + "::-2%::" + decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100) + "%)中")
//                    }else
                    if (data[i + 2].Close > stockCostPrice) {
                        successCloseCount += 1
                        successCloseProfit += decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100)
                            .toFloat()
                        successDate.add(
                            data[i].date + "(" + decimalFormat3.format(stockCostPrice) + "::" + decimalFormat.format(
                                (data[i + 2].Close - stockCostPrice) / stockCostPrice * 100
                            ) + "%::" + decimalFormat.format((data[i + 2].High - stockCostPrice) / stockCostPrice * 100) + "%)常"
                        )
                    } else {
                        failureCloseCount += 1
                        failureCloseProfit += decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100)
                            .toFloat()
                        failureDate.add(
                            data[i].date + "(" + decimalFormat3.format(stockCostPrice) + "::" + decimalFormat.format(
                                (data[i + 2].Close - stockCostPrice) / stockCostPrice * 100
                            ) + "%::" + decimalFormat.format((data[i + 2].High - stockCostPrice) / stockCostPrice * 100) + "%)常"
                        )
                    }

//                    if (data[i + 2].Close > stockCostPrice){
//                        if (addTrade){
//                            successCloseProfit += decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100 * 2)
//                                .toFloat()
//                            successDate.add(data[i].date + "(" + decimalFormat3.format(stockCostPrice) + "::" + decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100 * 2) + "%::" + decimalFormat.format((data[i + 2].High - stockCostPrice) / stockCostPrice * 100 * 2) + "%)")
//                        }else {
//                            successCloseProfit += decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100)
//                                .toFloat()
//                            successDate.add(data[i].date + "(" + decimalFormat3.format(stockCostPrice) + "::" + decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100) + "%::" + decimalFormat.format((data[i + 2].High - stockCostPrice) / stockCostPrice * 100) + "%)")
//                        }
//                    }else{
//                        failureCloseCount += 1
//                        if (addTrade){
//                            failureCloseProfit += decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100 * 2)
//                                .toFloat()
//                            failureDate.add(data[i].date + "(" + decimalFormat3.format(stockCostPrice) + "::" + decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100 * 2) + "%::" + decimalFormat.format((data[i + 2].High - stockCostPrice) / stockCostPrice * 100 * 2) + "%)")
//                        }else {
//                            failureCloseProfit += decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100)
//                                .toFloat()
//                            failureDate.add(data[i].date + "(" + decimalFormat3.format(stockCostPrice) + "::" + decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100) + "%::" + decimalFormat.format((data[i + 2].High - stockCostPrice) / stockCostPrice * 100) + "%)")
//                        }
//                    }

                    if (data[i + 2].High > stockCostPrice) {
                        successHighCount += 1
                        val highRate =
                            decimalFormat.format((data[i + 2].High - stockCostPrice) / stockCostPrice * 100)
                                .toFloat()
                        maxProfit += highRate
                        if (highRate > 1) success1PointCount += 1
                        if (highRate > 2) success2PointCount += 1
                        if (highRate > 3) success3PointCount += 1
                        if (highRate > 4) success4PointCount += 1
                        if (highRate > 5) success5PointCount += 1
                    } else {
                        failureHighCount += 1
                        mixProfit += decimalFormat.format((data[i + 2].High - stockCostPrice) / stockCostPrice * 100)
                            .toFloat()
                    }

                }
            }
        }

        println("------------------>总运行次数 $runCount")
        println("------------------>超5%的涨幅次数 $keyCount")
        println("------------------>超5%并穿过5日均线次数 $keyFiveCount")
        println("------------------>第二天达标的次数 $nextDayCount")
        println("------------------>第三天达标的次数 $thirdDayCount")

        println("------------------>第二天有高点的个数为 $successHighCount")
        println("------------------>第二天收盘价盈利个数为 $successCloseCount")
        println("------------------>第二天超过1%的盈利个数为 $success1PointCount")
        println("------------------>第二天超过3%的盈利个数为 $success3PointCount")
        println("------------------>第二天最高收益为$maxProfit%")

        println("------------------>第二天有低点的个数为 $failureHighCount")
        println("------------------>第二天收盘价亏损个数为 $failureCloseCount")
        println("------------------>第二天超过1%的亏损个数为 $failure1PointCount")
        println("------------------>第二天超过3%的亏损个数为 $failure3PointCount")
        println("------------------>第二天最低亏损为$mixProfit%")

        println("------------------>第二天有新高时量比大 $testSuccessHighTradeCount")
        println("------------------>第二天有新高时量比小 $testSuccessLowTradeCount")
        println("------------------>第二天强行亏损量比大 $testFailureHighTradeCount")
        println("------------------>第二天强行亏损量比小 $testFailureLowTradeCount")

        println("------------------>收盘价盈利总百分比例为 $successCloseProfit%")
        println("------------------>收盘价亏损总百分比例为 $failureCloseProfit%")

        println("------------------>垃圾位置点 $dustCount")
        println("------------------>成功时间点 $successDate")
        println("------------------>失败时间点 $failureDate")
        val temp = LinkedHashSet<String>().apply {
            addAll(dustDate)
        }
        dustDate.clear()
        dustDate.addAll(temp)
        println("------------------>垃圾时间点 $dustDate")

        var statisticsModel = StatisticsModel().apply {
            this.stockCode = stockCode
            this.stockName = stockName
            this.successHighCount = successHighCount
            this.successCloseCount = successCloseCount
            this.successCloseProfit = successCloseProfit
            this.success1PointCount = success1PointCount
            this.success2PointCount = success2PointCount
            this.success3PointCount = success3PointCount
            this.success4PointCount = success4PointCount
            this.success5PointCount = success5PointCount
            this.failureHighCount = failureHighCount
            this.failureCloseCount = failureCloseCount
            this.failureCloseProfit = failureCloseProfit
            this.failure1PointCount = failure1PointCount
            this.failure3PointCount = failure3PointCount
            this.keyCount = keyCount
            this.maxProfit = maxProfit
            this.minProfit = mixProfit
            this.statisticsType = statisticsType
            this.successDate = successDate.toString()
            this.failureDate = failureDate.toString()
        }

        statisticsModel.save()

        return statisticsModel
    }

    //剧情反转
    //剧情反转
    fun judgeFiveSuccessRateForLow(
        stockCode: String,
        stockName: String,
        datas: ArrayList<KLineEntity>,
        statisticsType: String,
        line: Int = 0,
        needThirdDay: Boolean = true
    ): StatisticsModel {
        if (datas.size < 12) return StatisticsModel()

        var data = ArrayList<KLineEntity>().apply { addAll(datas) }

        var successHighCount = 0
        var successCloseCount = 0
        var success1PointCount = 0
        var success2PointCount = 0
        var success3PointCount = 0
        var success4PointCount = 0
        var success5PointCount = 0
        var successCloseProfit = 0f
        var failureHighCount = 0
        var failureCloseCount = 0
        var failure1PointCount = 0
        var failure3PointCount = 0
        var failureCloseProfit = 0f
        var stockCostPrice: Float = 0f
        var keyCount = 0
        var keyFiveCount = 0
        var nextDayCount = 0
        var thirdDayCount = 0
        var runCount = 0
        var maxProfit = 0f
        var mixProfit = 0f

        var testSuccessHighTradeCount = 0
        var testSuccessLowTradeCount = 0

        var testFailureHighTradeCount = 0
        var testFailureLowTradeCount = 0

        var successDate = ArrayList<String>()
        var failureDate = ArrayList<String>()

        var dustCount = 0
        var dustDate = ArrayList<String>()

        var addTrade = false

        val currcapital = LitePalDBase.queryCurrcapital(stockCode)

        if (stockCode.startsWith("600") ||
            stockCode.startsWith("601") ||
            stockCode.startsWith("603") ||
            stockCode.startsWith("605") ||
            stockCode.startsWith("000") ||
            stockCode.startsWith("002")
        )

            for (i in 15..(data.size - 4)) {
                runCount += 1

                data[i - 2].MA3Price =
                    (data[i - 2].Close + data[i - 3].Close + data[i - 4].Close) / 3f
                data[i - 1].MA3Price =
                    (data[i - 1].Close + data[i - 2].Close + data[i - 3].Close) / 3f
                data[i].MA3Price = (data[i].Close + data[i - 1].Close + data[i - 2].Close) / 3f
                data[i + 1].MA3Price = (data[i + 1].Close + data[i].Close + data[i - 1].Close) / 3f
                val dataJ1MA3Price = (data[i - 1].Close + data[i].Close) / 2f
                val dataJ1MA5Price =
                    (data[i - 3].Close + data[i - 2].Close + data[i - 1].Close + data[i].Close) / 4f
                val dataJ1MA10Price =
                    (data[i - 8].Close + data[i - 7].Close + data[i - 6].Close + data[i - 5].Close + data[i - 4].Close + data[i - 3].Close + data[i - 2].Close + data[i - 1].Close + data[i].Close) / 9f

                if ((data[i].Close - data[i - 1].Close) / data[i - 1].Close > BaseApplication.instance?.filterOptions?.startRate!! / 100f &&
                    (data[i].Close - data[i - 1].Close) / data[i - 1].Close < BaseApplication.instance?.filterOptions?.endRate!! / 100f
                ) {

                    val upShadowPoint = if (data[i].Close >= data[i].Open) {
                        (data[i].High - data[i].Close) * 2 < data[i].Open - data[i].Low
                    } else
                        (data[i].High - data[i].Open) * 2 < data[i].Close - data[i].Low

                    val upNextShadowPoint = if (data[i + 1].Close >= data[i + 1].Open) {
                        data[i + 1].High - data[i + 1].Close < data[i + 1].Open - data[i + 1].Low
                    } else
                        data[i + 1].High - data[i + 1].Open < data[i + 1].Close - data[i + 1].Low

                    val volumeRate = (data[i].volume / 240f).toInt()
                        .toFloat() / ((data[i - 1].volume + data[i - 2].volume + data[i - 3].volume + data[i - 4].volume + data[i - 5].volume) / 1200f).toInt()
                        .toFloat()

                    if (volumeRate < BaseApplication.instance?.filterOptions?.volumeStartRate!! * 0.1f || volumeRate > BaseApplication.instance?.filterOptions?.volumeEndRate!! * 0.1f)
                        continue

                    println("------------------>Volume : " + data[i].volume + "       currcapital : " + currcapital)
                    println(
                        "------------------>Volume Rate : " + decimalFormat3.format(data[i].volume / currcapital)
                            .toFloat()
                    )
                    println("------------------>Volume StartRate : " + BaseApplication.instance?.filterOptions?.turnoverStartRate!!)
                    println("------------------>Volume EndRate : " + BaseApplication.instance?.filterOptions?.turnoverEndRate!!)

                    if (currcapital > 0 && (decimalFormat3.format(data[i].volume / currcapital)
                            .toFloat() < BaseApplication.instance?.filterOptions?.turnoverStartRate!! * 0.1f || decimalFormat3.format(
                            data[i].volume / currcapital
                        )
                            .toFloat() > BaseApplication.instance?.filterOptions?.turnoverEndRate!! * 1.0f)
                    )
                        continue

                    val checkDayLowPrice = if (!needThirdDay)
                        data[i + 1].Low <= data[i + 1].mA3Price
                    else
                        data[i + 1].Low <= data[i + 1].mA5Price

                    if ((((data[i - 1].MA3Price < data[i - 1].mA5Price || data[i - 1].MA3Price < data[i - 1].mA10Price || data[i - 1].mA5Price < data[i - 1].mA10Price) &&
                                data[i].MA3Price > data[i].mA5Price && data[i].MA3Price > data[i].mA10Price &&
                                data[i].Close > data[i].MA3Price && data[i].Open > data[i].mA10Price &&
                                checkDayLowPrice)) &&
                        upShadowPoint
                        && upNextShadowPoint
                        && data[i + 1].Volume < data[i].Volume
                    ) {

                        keyCount += 1

//                    addTrade = data[i + 1].High > data[i + 1].mA3Price && data[i + 1].Low <= data[i + 1].mA3Price &&
//                            data[i + 1].High > data[i + 1].mA5Price && data[i + 1].Low <= data[i + 1].mA5Price

                        stockCostPrice =
                            if (!needThirdDay) {
                                if (data[i + 1].Open < data[i + 1].mA3Price)
                                    data[i + 1].Open
                                else
                                    data[i + 1].mA3Price
                            } else {
                                if (data[i + 1].Open < data[i + 1].mA5Price)
                                    data[i + 1].Open
                                else
                                    data[i + 1].mA5Price
                            }

                        println("------------------>Stock Cost Price : $stockCostPrice")

                        keyFiveCount += 1

                        val date = data[i].Date
                        println("------------------>Date : $date")

//                    if ((data[i + 2].Open - stockCostPrice) / stockCostPrice * 100 > 5 && !needThirdDay){
//                        successCloseCount += 1
//                        successCloseProfit += decimalFormat.format((data[i + 2].Open - stockCostPrice) / stockCostPrice * 100).toFloat()
//                        successDate.add(data[i].date + "(" + decimalFormat3.format(stockCostPrice) + "::" + decimalFormat.format((data[i + 2].Open - stockCostPrice) / stockCostPrice * 100) + "%::" + decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100) +"%)开")
//                    }else if ((data[i + 2].Open - stockCostPrice) / stockCostPrice * 100 < -3){
//                        failureCloseCount += 1
//                        failureCloseProfit += decimalFormat.format((data[i + 2].Open - stockCostPrice) / stockCostPrice * 100).toFloat()
//                        failureDate.add(data[i].date + "(" + decimalFormat3.format(stockCostPrice) + "::" + decimalFormat.format((data[i + 2].Open - stockCostPrice) / stockCostPrice * 100) + "%::" + decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100) + "%)开")
//                    }else if ((data[i + 2].High - stockCostPrice) / stockCostPrice * 100 > 3 && !needThirdDay){
//                        successCloseCount += 1
//                        successCloseProfit += 3
//                        successDate.add(data[i].date + "(" + decimalFormat3.format(stockCostPrice) + "::3%::" + decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100) + "%)中")
//                    }else if ((data[i + 2].Low - stockCostPrice) / stockCostPrice * 100 < -3){
//                        failureCloseCount += 1
//                        failureCloseProfit += -3
//                        failureDate.add(data[i].date + "(" + decimalFormat3.format(stockCostPrice) + "::-3%::" + decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100) + "%)中")
//                    }else
                        if (data[i + 2].Close > stockCostPrice) {
                            successCloseCount += 1
                            successCloseProfit += decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100)
                                .toFloat()
                            successDate.add(
                                data[i].date + "(" + decimalFormat3.format(
                                    stockCostPrice
                                ) + "::" + decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100) + "%::" + decimalFormat.format(
                                    (data[i + 2].High - stockCostPrice) / stockCostPrice * 100
                                ) + "%)常"
                            )
                        } else {
                            failureCloseCount += 1
                            failureCloseProfit += decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100)
                                .toFloat()
                            failureDate.add(
                                data[i].date + "(" + decimalFormat3.format(
                                    stockCostPrice
                                ) + "::" + decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100) + "%::" + decimalFormat.format(
                                    (data[i + 2].High - stockCostPrice) / stockCostPrice * 100
                                ) + "%)常"
                            )
                        }

                        if (data[i + 2].High > stockCostPrice) {
                            successHighCount += 1
                            val highRate =
                                decimalFormat.format((data[i + 2].High - stockCostPrice) / stockCostPrice * 100)
                                    .toFloat()
                            maxProfit += highRate
                            if (highRate > 1) success1PointCount += 1
                            if (highRate > 2) success2PointCount += 1
                            if (highRate > 3) success3PointCount += 1
                            if (highRate > 4) success4PointCount += 1
                            if (highRate > 5) success5PointCount += 1
                        } else {
                            failureHighCount += 1
                            mixProfit += decimalFormat.format((data[i + 2].High - stockCostPrice) / stockCostPrice * 100)
                                .toFloat()
                        }

                    }
                }
            }

        println("------------------>总运行次数 $runCount")
        println("------------------>超5%的涨幅次数 $keyCount")
        println("------------------>超5%并穿过5日均线次数 $keyFiveCount")
        println("------------------>第二天达标的次数 $nextDayCount")
        println("------------------>第三天达标的次数 $thirdDayCount")

        println("------------------>第二天有高点的个数为 $successHighCount")
        println("------------------>第二天收盘价盈利个数为 $successCloseCount")
        println("------------------>第二天超过1%的盈利个数为 $success1PointCount")
        println("------------------>第二天超过3%的盈利个数为 $success3PointCount")
        println("------------------>第二天最高收益为$maxProfit%")

        println("------------------>第二天有低点的个数为 $failureHighCount")
        println("------------------>第二天收盘价亏损个数为 $failureCloseCount")
        println("------------------>第二天超过1%的亏损个数为 $failure1PointCount")
        println("------------------>第二天超过3%的亏损个数为 $failure3PointCount")
        println("------------------>第二天最低亏损为$mixProfit%")

        println("------------------>第二天有新高时量比大 $testSuccessHighTradeCount")
        println("------------------>第二天有新高时量比小 $testSuccessLowTradeCount")
        println("------------------>第二天强行亏损量比大 $testFailureHighTradeCount")
        println("------------------>第二天强行亏损量比小 $testFailureLowTradeCount")

        println("------------------>收盘价盈利总百分比例为 $successCloseProfit%")
        println("------------------>收盘价亏损总百分比例为 $failureCloseProfit%")

        println("------------------>垃圾位置点 $dustCount")
        println("------------------>成功时间点 $successDate")
        println("------------------>失败时间点 $failureDate")
        val temp = LinkedHashSet<String>().apply {
            addAll(dustDate)
        }
        dustDate.clear()
        dustDate.addAll(temp)
        println("------------------>垃圾时间点 $dustDate")

        var statisticsModel = StatisticsModel().apply {
            this.stockCode = stockCode
            this.stockName = stockName
            this.successHighCount = successHighCount
            this.successCloseCount = successCloseCount
            this.successCloseProfit = successCloseProfit
            this.success1PointCount = success1PointCount
            this.success2PointCount = success2PointCount
            this.success3PointCount = success3PointCount
            this.success4PointCount = success4PointCount
            this.success5PointCount = success5PointCount
            this.failureHighCount = failureHighCount
            this.failureCloseCount = failureCloseCount
            this.failureCloseProfit = failureCloseProfit
            this.failure1PointCount = failure1PointCount
            this.failure3PointCount = failure3PointCount
            this.keyCount = keyCount
            this.maxProfit = maxProfit
            this.minProfit = mixProfit
            this.statisticsType = statisticsType
            this.successDate = successDate.toString()
            this.failureDate = failureDate.toString()
        }

        statisticsModel.save()

        return statisticsModel
    }

    //剧情反转
    //剧情反转
    fun judgeFiveSuccessRateForLowPrice(
        stockCode: String,
        stockName: String,
        datas: ArrayList<KLineEntity>,
        statisticsType: String,
        line: Int = 0,
        needThirdDay: Boolean = true
    ): StatisticsModel {
        if (datas.size < 12) return StatisticsModel()

        var data = ArrayList<KLineEntity>().apply { addAll(datas) }

        var successHighCount = 0
        var successCloseCount = 0
        var success1PointCount = 0
        var success2PointCount = 0
        var success3PointCount = 0
        var success4PointCount = 0
        var success5PointCount = 0
        var successCloseProfit = 0f
        var failureHighCount = 0
        var failureCloseCount = 0
        var failure1PointCount = 0
        var failure3PointCount = 0
        var failureCloseProfit = 0f
        var stockCostPrice: Float = 0f
        var keyCount = 0
        var keyFiveCount = 0
        var nextDayCount = 0
        var thirdDayCount = 0
        var runCount = 0
        var maxProfit = 0f
        var mixProfit = 0f

        var testSuccessHighTradeCount = 0
        var testSuccessLowTradeCount = 0

        var testFailureHighTradeCount = 0
        var testFailureLowTradeCount = 0

        var successDate = ArrayList<String>()
        var failureDate = ArrayList<String>()

        var dustCount = 0
        var dustDate = ArrayList<String>()

        var addTrade = false

        val currcapital = LitePalDBase.queryCurrcapital(stockCode)

//        if (stockCode.startsWith("600") ||
//            stockCode.startsWith("601") ||
//            stockCode.startsWith("603") ||
//            stockCode.startsWith("605") ||
//            stockCode.startsWith("000") ||
//            stockCode.startsWith("002"))

        for (i in 15..(data.size - 10)) {
            runCount += 1

            val successStatus = if (!needThirdDay)
                JudgeTrendUtils.judgeCannonStock(QualityType.ThreeCannon,
                    ModelConversionUtils.kLineEntityToStockDayModel(currcapital,
                        ArrayList<KLineEntity>().apply { addAll(data.subList(i - 10, i)) }
                    ), stockCode)
            else
                JudgeTrendUtils.judgeThreeSoldierStock(
                    stockCode,
                    ModelConversionUtils.kLineEntityToStockDayModel(currcapital,
                        ArrayList<KLineEntity>().apply { addAll(data.subList(i - 10, i)) }
                    ))

//                JudgeTrendUtils.judgeCrossStarStock(
//                    ModelConversionUtils.KLineEntityToStockDayModel(currcapital,
//                        ArrayList<KLineEntity>().apply { addAll(data.subList(i - 10, i)) }
//                    ), stockCode)

            if (successStatus) {

                data[i - 3].MA3Price =
                    (data[i - 3].Close + data[i - 4].Close + data[i - 5].Close) / 3f
                data[i - 1].MA3Price =
                    (data[i - 1].Close + data[i - 2].Close + data[i - 3].Close) / 3f
                data[i].MA3Price =
                    (data[i].Close + data[i - 1].Close + data[i - 2].Close) / 3f

                if ((data[i - 3].mA3Price < data[i - 3].mA5Price || data[i - 3].mA3Price < data[i - 3].mA10Price || data[i - 3].mA5Price < data[i - 3].mA10Price) &&
                    data[i - 1].mA3Price > data[i - 1].mA5Price && data[i - 1].mA5Price > data[i - 1].mA10Price
                ) {

                    if (data[i].closePrice < data[i].mA3Price) {

                        keyCount += 1

                        stockCostPrice = data[i].closePrice

                        if (data[i + 1].Close > stockCostPrice) {
                            successCloseCount += 1
                            successCloseProfit += decimalFormat.format((data[i + 1].Close - stockCostPrice) / stockCostPrice * 100)
                                .toFloat()
                            successDate.add(
                                data[i].date + "(" + decimalFormat3.format(
                                    stockCostPrice
                                ) + "::" + decimalFormat.format((data[i + 1].Close - stockCostPrice) / stockCostPrice * 100) + "%::" + decimalFormat.format(
                                    (data[i + 1].High - stockCostPrice) / stockCostPrice * 100
                                ) + "%)常"
                            )
                        } else {
                            failureCloseCount += 1
                            failureCloseProfit += decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100)
                                .toFloat()
                            failureDate.add(
                                data[i].date + "(" + decimalFormat3.format(
                                    stockCostPrice
                                ) + "::" + decimalFormat.format((data[i + 2].Close - stockCostPrice) / stockCostPrice * 100) + "%::" + decimalFormat.format(
                                    (data[i + 1].High - stockCostPrice) / stockCostPrice * 100
                                ) + "%)常"
                            )
                        }

                        if (data[i + 1].High > stockCostPrice) {
                            successHighCount += 1
                            val highRate =
                                decimalFormat.format((data[i + 1].High - stockCostPrice) / stockCostPrice * 100)
                                    .toFloat()
                            maxProfit += highRate
                            if (highRate > 1) success1PointCount += 1
                            if (highRate > 2) success2PointCount += 1
                            if (highRate > 3) success3PointCount += 1
                            if (highRate > 4) success4PointCount += 1
                            if (highRate > 5) success5PointCount += 1
                        } else {
                            failureHighCount += 1
                            mixProfit += decimalFormat.format((data[i + 2].High - stockCostPrice) / stockCostPrice * 100)
                                .toFloat()
                        }

                    }
                }
            }

        }

        println("------------------>总运行次数 $runCount")
        println("------------------>超5%的涨幅次数 $keyCount")
        println("------------------>超5%并穿过5日均线次数 $keyFiveCount")
        println("------------------>第二天达标的次数 $nextDayCount")
        println("------------------>第三天达标的次数 $thirdDayCount")

        println("------------------>第二天有高点的个数为 $successHighCount")
        println("------------------>第二天收盘价盈利个数为 $successCloseCount")
        println("------------------>第二天超过1%的盈利个数为 $success1PointCount")
        println("------------------>第二天超过3%的盈利个数为 $success3PointCount")
        println("------------------>第二天最高收益为$maxProfit%")

        println("------------------>第二天有低点的个数为 $failureHighCount")
        println("------------------>第二天收盘价亏损个数为 $failureCloseCount")
        println("------------------>第二天超过1%的亏损个数为 $failure1PointCount")
        println("------------------>第二天超过3%的亏损个数为 $failure3PointCount")
        println("------------------>第二天最低亏损为$mixProfit%")

        println("------------------>第二天有新高时量比大 $testSuccessHighTradeCount")
        println("------------------>第二天有新高时量比小 $testSuccessLowTradeCount")
        println("------------------>第二天强行亏损量比大 $testFailureHighTradeCount")
        println("------------------>第二天强行亏损量比小 $testFailureLowTradeCount")

        println("------------------>收盘价盈利总百分比例为 $successCloseProfit%")
        println("------------------>收盘价亏损总百分比例为 $failureCloseProfit%")

        println("------------------>垃圾位置点 $dustCount")
        println("------------------>成功时间点 $successDate")
        println("------------------>失败时间点 $failureDate")
        val temp = LinkedHashSet<String>().apply {
            addAll(dustDate)
        }
        dustDate.clear()
        dustDate.addAll(temp)
        println("------------------>垃圾时间点 $dustDate")

        var statisticsModel = StatisticsModel().apply {
            this.stockCode = stockCode
            this.stockName = stockName
            this.successHighCount = successHighCount
            this.successCloseCount = successCloseCount
            this.successCloseProfit = successCloseProfit
            this.success1PointCount = success1PointCount
            this.success2PointCount = success2PointCount
            this.success3PointCount = success3PointCount
            this.success4PointCount = success4PointCount
            this.success5PointCount = success5PointCount
            this.failureHighCount = failureHighCount
            this.failureCloseCount = failureCloseCount
            this.failureCloseProfit = failureCloseProfit
            this.failure1PointCount = failure1PointCount
            this.failure3PointCount = failure3PointCount
            this.keyCount = keyCount
            this.maxProfit = maxProfit
            this.minProfit = mixProfit
            this.statisticsType = statisticsType
            this.successDate = successDate.toString()
            this.failureDate = failureDate.toString()
        }

        statisticsModel.save()

        return statisticsModel
    }

    fun judgeFiveSuccessRateForMACD(
        stockCode: String,
        stockName: String,
        datas: ArrayList<StockItemModel>,
        statisticsType: String,
        line: Int = 0,
        needThirdDay: Boolean = true
    ): StatisticsModel {
        if (datas.size < 12) return StatisticsModel()

        var data = ArrayList<StockItemModel>().apply { addAll(datas) }

        var successHighCount = 0
        var successCloseCount = 0
        var success1PointCount = 0
        var success2PointCount = 0
        var success3PointCount = 0
        var success4PointCount = 0
        var success5PointCount = 0
        var successCloseProfit = 0f
        var failureHighCount = 0
        var failureCloseCount = 0
        var failure1PointCount = 0
        var failure3PointCount = 0
        var failureCloseProfit = 0f
        var stockCostPrice: Float = 0f
        var keyCount = 0
        var keyFiveCount = 0
        var nextDayCount = 0
        var thirdDayCount = 0
        var runCount = 0
        var maxProfit = 0f
        var mixProfit = 0f

        var testSuccessHighTradeCount = 0
        var testSuccessLowTradeCount = 0

        var testFailureHighTradeCount = 0
        var testFailureLowTradeCount = 0

        var successDate = ArrayList<String>()
        var failureDate = ArrayList<String>()

        var dustCount = 0
        var dustDate = ArrayList<String>()

        var addTrade = false

//        if (stockCode.startsWith("600") ||
//            stockCode.startsWith("601") ||
//            stockCode.startsWith("603") ||
//            stockCode.startsWith("605") ||
//            stockCode.startsWith("000") ||
//            stockCode.startsWith("002")
//        )

            for (i in 10..(data.size - 10)) {
                runCount += 1

                var wrStatus = 0

                if (abs(data[i].r) > 90)
                    wrStatus += 1
                if (abs(data[i - 1].r) > 90)
                    wrStatus += 1
                if (abs(data[i - 2].r) > 90)
                    wrStatus += 1
                if (abs(data[i - 3].r) > 90)
                    wrStatus += 1
                if (abs(data[i - 4].r) > 90)
                    wrStatus += 1
                if (abs(data[i - 5].r) > 90)
                    wrStatus += 1
                if (abs(data[i - 6].r) > 90)
                    wrStatus += 1

                var jStatus = 0

                if (data[i].j > 100)
                    jStatus += 1
                if (data[i - 1].j > 100)
                    jStatus += 1
                if (data[i - 2].j > 100)
                    jStatus += 1
                if (data[i - 3].j > 100)
                    jStatus += 1

//                val successStatus =
//                    data[i].macd > -0.05f && data[i].macd < 0 &&
//                            data[i - 1].macd < data[i].macd &&
//                            abs(data[i - 1].dea - data[i - 1].dif) > abs(data[i].dea - data[i].dif) &&
//                            data[i].dif > data[i - 1].dif && data[i].dea <= data[i - 1].dea &&
//                            data[i].dif > -0.1 && data[i].dea > -0.1 &&
////                            data[i - 1].dif < -0.1 && data[i - 1].macd < -0.1f &&
//                            data[i].j > data[i].k && data[i].k > data[i].d &&
//                            data[i].j > data[i - 1].j && data[i].k > data[i - 1].k && data[i].d > data[i - 1].d &&
//                            data[i].j > 40 && data[i].k > 40 && data[i].d > 40 && data[i].j < 90 &&
//                            (data[i].rsi < 20 || (data[i].rsi > 50 && data[i].rsi < 80)) &&
//                            data[i].r > 30 && data[i - 1].r > data[i].r
//                            ((data[i - 1].r < 80 && data[i].r > 80) || wrStatus >= 3 || (data[i - 1].r < 50 && data[i].r > 50))

                val successStatus = if (BaseApplication.instance?.filterOptions?.throughType == ThroughType.HighThroughAndTrade.type){
//                    data[i].macd >= 0 && data[i - 1].macd < 0 &&
//                            abs(data[i].dif - data[i].dea) < 0.03 &&
//                            abs(data[i - 1].dif - data[i - 1].dea) > abs(data[i].dif - data[i].dea) &&
//                            data[i].dif > -0.1 && data[i].dea > -0.1 &&
//                        data[i - 1].dif < -0.1 && data[i - 1].macd < -0.1f &&
                    data[i].dif >= data[i].dea && data[i - 1].dif <= data[i - 1].dea &&
//                    ((data[i].dif >= data[i].dea && data[i - 1].dif <= data[i - 1].dea) ||
//                            (data[i].macd > data[i - 1].macd && data[i - 1].macd < data[i - 2].macd)) &&
                            data[i].j >= data[i].k && data[i].k >= data[i].d &&
                            (data[i - 1].d >= data[i - 1].k && data[i - 1].k >= data[i - 1].j) &&
//                            ((data[i - 1].j <= data[i - 1].k && data[i - 1].k >= data[i - 1].d) ||
//                                    ((data[i].j >= data[i - 1].j && data[i].k >= data[i - 1].k && data[i].d >= data[i - 1].d))) &&
//                            data[i].j > 50 && data[i].k > 50 && data[i].d > 50 &&
                            (jStatus < 3) &&
                            (data[i].d > 20 && data[i].d < 80) &&
//                            ((data[i].j >= data[i - 1].j && data[i].k >= data[i - 1].k && data[i].d >= data[i - 1].d) ||
//                                    ((data[i].j >= data[i - 1].j && data[i].d <= data[i - 1].d) &&
//                                            abs(data[i].j - data[i].d) <= abs(data[i - 1].j - data[i - 1].d))) &&
                            (data[i].rsi < 20 || (data[i].rsi > 50 && data[i].rsi < 80)) &&
                            data[i].rsi > data[i - 1].rsi
//                            &&
//                            abs(data[i].r) > 20 && !(abs(data[i - 1].r) < 20 && abs(data[i].r) > 20 )
//                            &&
//                            ((abs(data[i - 1].r) < 80 && abs(data[i].r) > 80) || wrStatus >= 3 || (abs(data[i - 1].r) < 50 && abs(data[i].r) > 50) || abs(data[i].r) > 20)
                }else if (BaseApplication.instance?.filterOptions?.throughType == ThroughType.HighThrough.type){
                    wrStatus >= 3 && abs(data[i].r) > 90
                }else{
                    data[i].macd > -0.05f && data[i].macd < 0.05 &&
                            abs(data[i].dif - data[i].dea) < 0.03 &&
                            abs(data[i - 1].dif - data[i - 1].dea) > abs(data[i].dif - data[i].dea) &&
                            data[i].dif > -0.1 && data[i].dea > -0.1 &&
//                        data[i - 1].dif < -0.1 && data[i - 1].macd < -0.1f &&
                            data[i].j >= data[i].k && data[i].k >= data[i].d &&
                            data[i].j > 40 && data[i].k > 40 && data[i].d > 40 && data[i].j < 90 &&
                            ((data[i].j >= data[i - 1].j && data[i].k >= data[i - 1].k && data[i].d >= data[i - 1].d) ||
                                    ((data[i].j >= data[i - 1].j && data[i].d <= data[i - 1].d) &&
                                            abs(data[i].j - data[i].d) <= abs(data[i - 1].j - data[i - 1].d))) &&
                            (data[i].rsi < 20 || (data[i].rsi > 40 && data[i].rsi < 80)) &&
                            ((abs(data[i - 1].r) < 80 && abs(data[i].r) > 80) || wrStatus >= 3 || (abs(data[i - 1].r) < 50 && abs(data[i].r) > 50) || abs(data[i].r) > 30)
                }

                if (successStatus) {

                    val tempM3Price =
                        (data[i].nowPrice!!.toFloat() + data[i - 1].nowPrice!!.toFloat()) / 2f

                    if ((data[i + 1].todayMax!!.toFloat() > tempM3Price && data[i + 1].todayMin!!.toFloat() < tempM3Price)) {

                        keyCount += 1

                        stockCostPrice = if (BaseApplication.instance?.filterOptions?.throughType == ThroughType.HighThrough.type){
                            data[i].nowPrice!!.toFloat()
                        }else if (data[i + 1].openPrice!!.toFloat() < tempM3Price) {
                            data[i + 1].openPrice!!.toFloat()
                        } else {
                            tempM3Price
                        }

                        if ((data[i + 2].todayMax!!.toFloat() - stockCostPrice) / stockCostPrice * 100 < -5 && !needThirdDay){
                            failureCloseCount += 1
                            failureCloseProfit += -5
                            failureDate.add(
                                data[i].dateTime + "(" + decimalFormat3.format(
                                    stockCostPrice
                                ) + "::1::-5%::" + decimalFormat.format(
                                    (data[i + 2].todayMax!!.toFloat() - stockCostPrice) / stockCostPrice * 100
                                ) + "%)常"
                            )
                        }else if ((data[i + 2].todayMin!!.toFloat() - stockCostPrice) / stockCostPrice * 100 > 10 && !needThirdDay){
                            successCloseCount += 1
                            successCloseProfit += 10
                            successDate.add(
                                data[i].dateTime + "(" + decimalFormat3.format(
                                    stockCostPrice
                                ) + "::1::10%::" + decimalFormat.format(
                                    (data[i + 2].todayMax!!.toFloat() - stockCostPrice) / stockCostPrice * 100
                                ) + "%)常"
                            )
                        }else if ((data[i + 3].todayMin!!.toFloat() - stockCostPrice) / stockCostPrice * 100 < -5 && !needThirdDay){
                            failureCloseCount += 1
                            failureCloseProfit += -5
                            failureDate.add(
                                data[i].dateTime + "(" + decimalFormat3.format(
                                    stockCostPrice
                                ) + "::2::-5%::" + decimalFormat.format(
                                    (data[i + 3].todayMax!!.toFloat() - stockCostPrice) / stockCostPrice * 100
                                ) + "%)常"
                            )
                        }else if ((data[i + 3].todayMax!!.toFloat() - stockCostPrice) / stockCostPrice * 100 > 10 && !needThirdDay){
                            successCloseCount += 1
                            successCloseProfit += 10
                            successDate.add(
                                data[i].dateTime + "(" + decimalFormat3.format(
                                    stockCostPrice
                                ) + "::2::10%::" + decimalFormat.format(
                                    (data[i + 3].todayMax!!.toFloat() - stockCostPrice) / stockCostPrice * 100
                                ) + "%)常"
                            )
                        }else if ((data[i + 4].todayMin!!.toFloat() - stockCostPrice) / stockCostPrice * 100 < -5 && !needThirdDay){
                            failureCloseCount += 1
                            failureCloseProfit += -5
                            failureDate.add(
                                data[i].dateTime + "(" + decimalFormat3.format(
                                    stockCostPrice
                                ) + "::3::-5%::" + decimalFormat.format(
                                    (data[i + 4].todayMax!!.toFloat() - stockCostPrice) / stockCostPrice * 100
                                ) + "%)常"
                            )
                        }else if ((data[i + 4].todayMax!!.toFloat() - stockCostPrice) / stockCostPrice * 100 > 10 && !needThirdDay){
                            successCloseCount += 1
                            successCloseProfit += 10
                            successDate.add(
                                data[i].dateTime + "(" + decimalFormat3.format(
                                    stockCostPrice
                                ) + "::3::10%::" + decimalFormat.format(
                                    (data[i + 4].todayMax!!.toFloat() - stockCostPrice) / stockCostPrice * 100
                                ) + "%)常"
                            )
                        }else if ((data[i + 5].todayMin!!.toFloat() - stockCostPrice) / stockCostPrice * 100 < -5 && !needThirdDay){
                            failureCloseCount += 1
                            failureCloseProfit += -5
                            failureDate.add(
                                data[i].dateTime + "(" + decimalFormat3.format(
                                    stockCostPrice
                                ) + "::4::-5%::" + decimalFormat.format(
                                    (data[i + 5].todayMax!!.toFloat() - stockCostPrice) / stockCostPrice * 100
                                ) + "%)常"
                            )
                        }else if ((data[i + 5].todayMax!!.toFloat() - stockCostPrice) / stockCostPrice * 100 > 10 && !needThirdDay){
                            successCloseCount += 1
                            successCloseProfit += 10
                            successDate.add(
                                data[i].dateTime + "(" + decimalFormat3.format(
                                    stockCostPrice
                                ) + "::4::10%::" + decimalFormat.format(
                                    (data[i + 5].todayMax!!.toFloat() - stockCostPrice) / stockCostPrice * 100
                                ) + "%)常"
                            )
                        }else{
                            if (data[i + 3].nowPrice!!.toFloat() > stockCostPrice){
                                successCloseCount += 1
                                successCloseProfit += decimalFormat.format((data[i + 3].nowPrice!!.toFloat() - stockCostPrice) / stockCostPrice * 100).toFloat()
                                successDate.add(
                                    data[i].dateTime + "(" + decimalFormat3.format(
                                        stockCostPrice
                                    ) + "::3::" +decimalFormat.format((data[i + 3].nowPrice!!.toFloat() - stockCostPrice) / stockCostPrice * 100).toFloat() + "%::" + decimalFormat.format(
                                        (data[i + 3].todayMax!!.toFloat() - stockCostPrice) / stockCostPrice * 100
                                    ) + "%)$wrStatus"
                                )
                            }else{
                                failureCloseCount += 1
                                failureCloseProfit += decimalFormat.format((data[i + 3].nowPrice!!.toFloat() - stockCostPrice) / stockCostPrice * 100).toFloat()
                                failureDate.add(
                                    data[i].dateTime + "(" + decimalFormat3.format(
                                        stockCostPrice
                                    ) + "::3::" + decimalFormat.format((data[i + 3].nowPrice!!.toFloat() - stockCostPrice) / stockCostPrice * 100).toFloat() + "%::" + decimalFormat.format(
                                        (data[i + 3].todayMax!!.toFloat() - stockCostPrice) / stockCostPrice * 100
                                    ) + "%)$wrStatus"
                                )
                            }
                        }

//                        if (data[i + 2].nowPrice!!.toFloat() > stockCostPrice) {
//                            successCloseCount += 1
//                            successCloseProfit += decimalFormat.format((data[i + 2].nowPrice!!.toFloat() - stockCostPrice) / stockCostPrice * 100)
//                                .toFloat()
//                            successDate.add(
//                                data[i].dateTime + "(" + decimalFormat3.format(
//                                    stockCostPrice
//                                ) + "::" + decimalFormat.format((data[i + 2].nowPrice!!.toFloat() - stockCostPrice) / stockCostPrice * 100) + "%::" + decimalFormat.format(
//                                    (data[i + 2].todayMax!!.toFloat() - stockCostPrice) / stockCostPrice * 100
//                                ) + "%)常"
//                            )
//                        } else {
//                            failureCloseCount += 1
//                            failureCloseProfit += decimalFormat.format((data[i + 2].nowPrice!!.toFloat() - stockCostPrice) / stockCostPrice * 100)
//                                .toFloat()
//                            failureDate.add(
//                                data[i].dateTime + "(" + decimalFormat3.format(
//                                    stockCostPrice
//                                ) + "::" + decimalFormat.format((data[i + 2].nowPrice!!.toFloat() - stockCostPrice) / stockCostPrice * 100) + "%::" + decimalFormat.format(
//                                    (data[i + 2].todayMax!!.toFloat() - stockCostPrice) / stockCostPrice * 100
//                                ) + "%)常"
//                            )
//                        }

                        if (data[i + 2].todayMax!!.toFloat() > stockCostPrice) {
                            successHighCount += 1
                            val highRate =
                                decimalFormat.format((data[i + 2].todayMax!!.toFloat() - stockCostPrice) / stockCostPrice * 100)
                                    .toFloat()
                            maxProfit += highRate
                            if (highRate > 1) success1PointCount += 1
                            if (highRate > 2) success2PointCount += 1
                            if (highRate > 3) success3PointCount += 1
                            if (highRate > 4) success4PointCount += 1
                            if (highRate > 5) success5PointCount += 1
                        } else {
                            failureHighCount += 1
                            mixProfit += decimalFormat.format((data[i + 2].todayMax!!.toFloat() - stockCostPrice) / stockCostPrice * 100)
                                .toFloat()
                        }

                    }
                }

            }

        println("------------------>总运行次数 $runCount")
        println("------------------>超5%的涨幅次数 $keyCount")
        println("------------------>超5%并穿过5日均线次数 $keyFiveCount")
        println("------------------>第二天达标的次数 $nextDayCount")
        println("------------------>第三天达标的次数 $thirdDayCount")

        println("------------------>第二天有高点的个数为 $successHighCount")
        println("------------------>第二天收盘价盈利个数为 $successCloseCount")
        println("------------------>第二天超过1%的盈利个数为 $success1PointCount")
        println("------------------>第二天超过3%的盈利个数为 $success3PointCount")
        println("------------------>第二天最高收益为$maxProfit%")

        println("------------------>第二天有低点的个数为 $failureHighCount")
        println("------------------>第二天收盘价亏损个数为 $failureCloseCount")
        println("------------------>第二天超过1%的亏损个数为 $failure1PointCount")
        println("------------------>第二天超过3%的亏损个数为 $failure3PointCount")
        println("------------------>第二天最低亏损为$mixProfit%")

        println("------------------>第二天有新高时量比大 $testSuccessHighTradeCount")
        println("------------------>第二天有新高时量比小 $testSuccessLowTradeCount")
        println("------------------>第二天强行亏损量比大 $testFailureHighTradeCount")
        println("------------------>第二天强行亏损量比小 $testFailureLowTradeCount")

        println("------------------>收盘价盈利总百分比例为 $successCloseProfit%")
        println("------------------>收盘价亏损总百分比例为 $failureCloseProfit%")

        println("------------------>垃圾位置点 $dustCount")
        println("------------------>成功时间点 $successDate")
        println("------------------>失败时间点 $failureDate")
        val temp = LinkedHashSet<String>().apply {
            addAll(dustDate)
        }
        dustDate.clear()
        dustDate.addAll(temp)
        println("------------------>垃圾时间点 $dustDate")

        var statisticsModel = StatisticsModel().apply {
            this.stockCode = stockCode
            this.stockName = stockName
            this.successHighCount = successHighCount
            this.successCloseCount = successCloseCount
            this.successCloseProfit = successCloseProfit
            this.success1PointCount = success1PointCount
            this.success2PointCount = success2PointCount
            this.success3PointCount = success3PointCount
            this.success4PointCount = success4PointCount
            this.success5PointCount = success5PointCount
            this.failureHighCount = failureHighCount
            this.failureCloseCount = failureCloseCount
            this.failureCloseProfit = failureCloseProfit
            this.failure1PointCount = failure1PointCount
            this.failure3PointCount = failure3PointCount
            this.keyCount = keyCount
            this.maxProfit = maxProfit
            this.minProfit = mixProfit
            this.statisticsType = statisticsType
            this.successDate = successDate.toString()
            this.failureDate = failureDate.toString()
        }

        statisticsModel.save()

        return statisticsModel
    }
}