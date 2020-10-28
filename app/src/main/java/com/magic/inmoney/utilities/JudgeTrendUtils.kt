package com.magic.inmoney.utilities

import com.magic.inmoney.base.BaseApplication
import com.magic.inmoney.const.QualityType
import com.magic.inmoney.model.StockDayModel
import com.magic.inmoney.model.StockItemCalculateModel
import com.magic.inmoney.model.StockItemModel
import com.magic.inmoney.model.StockItemRecentModel
import com.magic.inmoney.orm.LitePalDBase
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs

object JudgeTrendUtils {

    fun judgeCannonStock(
        qualityType: QualityType,
        qualityStocks: ArrayList<StockDayModel>,
        stockCode: String = ""
    ): Boolean {
        return when (qualityType) {
            QualityType.ThreeCannon -> judgeThreeCannonStock(qualityStocks, stockCode)
            QualityType.ThreeCannonPlus -> judgeThreeCannonPlusStock(qualityStocks)
            QualityType.FourCannon -> judgeFourCannonStock(qualityStocks)
            QualityType.FiveCannon -> judgeFiveCannonStock(qualityStocks)
            else -> return false
        }
    }

    private fun judgeThreeCannonStock(qualityStocks: ArrayList<StockDayModel>, stockCode: String = ""): Boolean {
        if (qualityStocks.size < 3) return false

        //两阳夹一阴
        if (qualityStocks[0].yinyang!! && !qualityStocks[1].yinyang!! && qualityStocks[2].yinyang!!) {
            //第三天的收盘价高于第一天的收盘价
            if (qualityStocks[0].nowPrice!! > qualityStocks[2].nowPrice!!) {
                if (qualityStocks[0].nowPrice!! > qualityStocks[1].openPrice!!) {
                    //第一天的成交量比第二天高，第三天的成交量比第一天高
                    if (qualityStocks[2].tradeNum!! > qualityStocks[1].tradeNum!! && fictitiousTradingVolume(
                            qualityStocks[0].tradeNum!!
                        ) > qualityStocks[2].tradeNum!!
                    ) {
                        //第二天收盘价不能低过第一天的开盘价
                        if (qualityStocks[1].nowPrice!! > qualityStocks[2].openPrice!!) {
//                        checkLongShadowLine(
//                            true,
//                            qualityStocks[0].nowPrice!!,
//                            qualityStocks[0].maxPrice!!
//                        )
                            if (qualityStocks[3].openPrice!! > qualityStocks[2].nowPrice!! )
                                if (!qualityStocks[3].yinyang!!)
                                    return true
                        }
                        return false
                    } else
                        return false
                } else
                    return false
            } else
                return false
        } else
            return false
    }

    private fun judgeThreeCannonPlusStock(qualityStocks: ArrayList<StockDayModel>): Boolean {
        if (qualityStocks.size < 3) return false

        //两阳夹一阴
        if (qualityStocks[0].yinyang!! && !qualityStocks[1].yinyang!! && qualityStocks[2].yinyang!!) {
            //第三天的收盘价高于第一天的收盘价
            if (qualityStocks[0].nowPrice!! > qualityStocks[2].nowPrice!!) {
                //第一天的成交量比第二天高，第三天的成交量比第一天高
                if (qualityStocks[2].tradeNum!! > qualityStocks[1].tradeNum!! && fictitiousTradingVolume(
                        qualityStocks[0].tradeNum!!
                    ) > qualityStocks[2].tradeNum!!
                ) {
                    //第二天收盘价不能低过第一天的开盘价
                    if (qualityStocks[1].nowPrice!! > qualityStocks[2].openPrice!!) {
                        return if (qualityStocks[1].openPrice!! < qualityStocks[2].nowPrice!!) {
                            if (qualityStocks[1].nowPrice!! < qualityStocks[2].nowPrice!!) {
                                if (qualityStocks[0].openPrice!! > qualityStocks[1].nowPrice!!) {
                                    if (qualityStocks[0].openPrice!! < qualityStocks[1].openPrice!!) {
//                                        checkLongShadowLine(
//                                            true,
//                                            qualityStocks[0].nowPrice!!,
//                                            qualityStocks[0].maxPrice!!
//                                        )
                                        return true
                                    } else false
                                } else false
                            } else false
                        } else false
                    }
                    return false
                } else
                    return false
            } else
                return false
        } else
            return false
    }

    private fun judgeFourCannonStock(qualityStocks: ArrayList<StockDayModel>): Boolean {
        if (qualityStocks.size < 4) return false

        //两阳夹两阴
        if (qualityStocks[0].yinyang!! && !qualityStocks[1].yinyang!! && !qualityStocks[2].yinyang!! && qualityStocks[3].yinyang!!) {
            //第四天的收盘价高于第一天的收盘价
            if (qualityStocks[0].nowPrice!! > qualityStocks[3].openPrice!! &&
                qualityStocks[0].nowPrice!! > qualityStocks[2].openPrice!! &&
                qualityStocks[0].nowPrice!! > qualityStocks[1].openPrice!!
            ) {
                //第一天的成交量比第二天高，第三天的成交量比第一天高
                if (qualityStocks[3].tradeNum!! > qualityStocks[2].tradeNum!! && qualityStocks[3].tradeNum!! > qualityStocks[1].tradeNum!! && fictitiousTradingVolume(
                        qualityStocks[0].tradeNum!!
                    ) > qualityStocks[3].tradeNum!!
                ) {
                    //第二天收盘价不能低过第一天的开盘价
                    if (qualityStocks[1].nowPrice!! > qualityStocks[3].openPrice!! && qualityStocks[2].nowPrice!! > qualityStocks[3].openPrice!!) {
//                        checkLongShadowLine(
//                            true,
//                            qualityStocks[0].nowPrice!!,
//                            qualityStocks[0].maxPrice!!
//                        )
                        return true
                    }
                    return false
                } else
                    return false
            } else
                return false
        } else
            return false
    }

    private fun judgeFiveCannonStock(qualityStocks: ArrayList<StockDayModel>): Boolean {
        if (qualityStocks.size < 5) return false

        //两阳夹三阴
        if (qualityStocks[0].yinyang!! && !qualityStocks[1].yinyang!! && !qualityStocks[2].yinyang!! && !qualityStocks[3].yinyang!! && !qualityStocks[4].yinyang!!) {
            //第三天的收盘价高于第一天的收盘价
            if (qualityStocks[0].nowPrice!! > qualityStocks[4].nowPrice!!) {
                //第一天的成交量比第二天高，第三天的成交量比第一天高
                if (qualityStocks[4].tradeNum!! > qualityStocks[3].tradeNum!! && qualityStocks[4].tradeNum!! > qualityStocks[2].tradeNum!! && qualityStocks[4].tradeNum!! > qualityStocks[1].tradeNum!! && fictitiousTradingVolume(
                        qualityStocks[0].tradeNum!!
                    ) > qualityStocks[4].tradeNum!!
                ) {
                    //第二天收盘价不能低过第一天的开盘价
                    if (qualityStocks[1].nowPrice!! > qualityStocks[4].openPrice!! && qualityStocks[2].nowPrice!! > qualityStocks[4].openPrice!! && qualityStocks[3].nowPrice!! > qualityStocks[4].openPrice!!) {
//                        checkLongShadowLine(
//                            true,
//                            qualityStocks[0].nowPrice!!,
//                            qualityStocks[0].maxPrice!!
//                        )
                        return true
                    }
                    return false
                } else
                    return false
            } else
                return false
        } else
            return false
    }

    fun judgeThreeSoldierStock(
        stockCode: String,
        qualityStocks: ArrayList<StockDayModel>
    ): Boolean {
        if (qualityStocks.size < 3) return false

        if (qualityStocks[0].yinyang!! && qualityStocks[1].yinyang!! && qualityStocks[2].yinyang!!)
            if (qualityStocks[0].nowPrice!! > qualityStocks[1].nowPrice!! &&
                qualityStocks[0].nowPrice!! > qualityStocks[2].openPrice!! &&
                qualityStocks[1].nowPrice!! > qualityStocks[2].nowPrice!! &&
                qualityStocks[0].openPrice!! > qualityStocks[1].openPrice!! &&
                qualityStocks[1].openPrice!! > qualityStocks[2].openPrice!! &&
                qualityStocks[0].stockRate!! < 5 &&
                qualityStocks[1].stockRate!! < 5 &&
                qualityStocks[2].stockRate!! < 5
            )
                if (qualityStocks[0].tradeNum!! > qualityStocks[1].tradeNum!! &&
                    qualityStocks[1].tradeNum!! > qualityStocks[2].tradeNum!!
                )
                    if (qualityStocks[3].openPrice!! > qualityStocks[2].nowPrice!! )
                        if (!qualityStocks[3].yinyang!!)
                    return true
        return false

    }

    fun judgeFriendCounterattackStock(stockCode: String, qualityStocks: ArrayList<StockDayModel>): Boolean {
        if (qualityStocks.size < 2) return false

        if (qualityStocks[0].yinyang!! && !qualityStocks[1].yinyang!!)
            if (qualityStocks[1].stockRate!! < -2)
                if (abs(qualityStocks[0].nowPrice!! - qualityStocks[1].nowPrice!!) / qualityStocks[0].nowPrice!! < 0.005)
                    return true

        return false

//        return if (qualityStocks[0].yinyang!! && !qualityStocks[1].yinyang!!) {
//            if (qualityStocks[1].stockRate!! < -2) {
//                if ((qualityStocks[1].nowPrice!! - qualityStocks[0].openPrice!!) / qualityStocks[1].nowPrice!! > 0.01f) {
//                    (qualityStocks[0].nowPrice!! - qualityStocks[1].nowPrice!!) / qualityStocks[1].nowPrice!! > -0.005f &&
//                            (qualityStocks[0].nowPrice!! - qualityStocks[1].nowPrice!!) / qualityStocks[1].nowPrice!! < 0.015f
//                } else false
//            } else false
//        } else false
    }

    fun judgeDawnFlushStock(stockCode: String, qualityStocks: ArrayList<StockDayModel>): Boolean {
        if (qualityStocks.size < 5) return false

//        checkCrossStar(
//            qualityStocks[0].openPrice!!,
//            qualityStocks[0].nowPrice!!,
//            qualityStocks[0].maxPrice!!,
//            qualityStocks[0].minPrice!!
//        )

        if (qualityStocks[0].yinyang!! && !qualityStocks[1].yinyang!! && !qualityStocks[2].yinyang!!)
            if (qualityStocks[1].stockRate!! < -2)
                if (qualityStocks[0].openPrice!! > qualityStocks[1].nowPrice!!)
                    if (qualityStocks[0].nowPrice!! > qualityStocks[1].openPrice!!)
//                        if (qualityStocks[0].tradeNum!! > qualityStocks[1].tradeNum!!)
                        if (qualityStocks[0].openPrice!! < qualityStocks[1].openPrice!!)
                            return true

        return false

//        return if (qualityStocks[0].yinyang!! && !qualityStocks[1].yinyang!! && !qualityStocks[2].yinyang!! && !qualityStocks[3].yinyang!!) {
//            if (qualityStocks[3].stockRate!! < -0.5f && qualityStocks[2].stockRate!! < -0.5f && qualityStocks[1].stockRate!! < -2 && qualityStocks[0].stockRate!! > 1) {
//                if ((qualityStocks[1].nowPrice!! - qualityStocks[1].openPrice!!) / qualityStocks[1].openPrice!! < -0.01f) {
//                    if ((qualityStocks[0].nowPrice!! - qualityStocks[0].openPrice!!) / qualityStocks[0].openPrice!! > 0.01f) {
//                        if (qualityStocks[0].nowPrice!! < qualityStocks[1].openPrice!!) {
//                            if (qualityStocks[0].nowPrice!! > (qualityStocks[1].nowPrice!! + qualityStocks[1].openPrice!!) / 2) {
//                                qualityStocks[0].openPrice!! < qualityStocks[1].nowPrice!!
//                            } else false
//                        } else false
//                    } else false
//                } else false
//            } else false
//        } else false
    }

    fun judgeCrossStarStock(qualityStocks: ArrayList<StockDayModel>, stockCode: String = ""): Boolean {
        if (qualityStocks.size < 3) return false

        return if (checkCrossStar(
                qualityStocks[1].openPrice!!,
                qualityStocks[1].nowPrice!!,
                qualityStocks[1].maxPrice!!,
                qualityStocks[1].minPrice!!
            )
        ) {
            if (qualityStocks[0].yinyang!! && !qualityStocks[2].yinyang!!) {
                if (qualityStocks[0].openPrice!! > qualityStocks[1].openPrice!! && qualityStocks[0].openPrice!! > qualityStocks[1].nowPrice!!) {
                    if (qualityStocks[2].nowPrice!! > qualityStocks[1].openPrice!! && qualityStocks[2].nowPrice!! > qualityStocks[1].nowPrice!!) {
                        if (qualityStocks[0].nowPrice!! > qualityStocks[2].nowPrice!! + (qualityStocks[2].openPrice!! - qualityStocks[2].nowPrice!!) * 2 / 3) {
                            fictitiousTradingVolume(qualityStocks[0].tradeNum!!) > qualityStocks[1].tradeNum!!
                        } else false
                    } else false
                } else false
            } else false
        } else false

    }

    private fun checkCrossStar(
        openPrice: Float,
        nowPrice: Float,
        maxPrice: Float,
        minPrice: Float
    ): Boolean {
        var highLine = 0f
        var lowLine = 0f
        var midCube = 0f

        //阳
        if (openPrice - nowPrice < 0) {
            highLine = maxPrice - nowPrice
            lowLine = openPrice - minPrice
            midCube = nowPrice - openPrice

            if (midCube == 0f) {
                return maxPrice > 0 && minPrice > 0
            }

            return if (highLine / midCube > 2) {
                if (lowLine / midCube > 2) {
                    midCube / openPrice < 1f
                } else false
            } else false

        } else {
            highLine = maxPrice - openPrice
            lowLine = nowPrice - minPrice
            midCube = openPrice - nowPrice

            if (midCube == 0f) {
                return maxPrice > 0 && minPrice > 0
            }

            return if (highLine / midCube > 2) {
                if (lowLine / midCube > 2) {
                    midCube / nowPrice < 1f
                } else false
            } else false
        }
    }

    private fun checkLongShadowLine(
        upLine: Boolean,
        nowPrice: Float,
        maxPrice: Float,
        diffRate: Float = 1.5f
    ): Boolean {
        return if (upLine) {
            (maxPrice - nowPrice) / maxPrice > diffRate
        } else {
            (nowPrice - maxPrice) / maxPrice > diffRate
        }
    }

    fun judgeUpHammerStock(stockCode: String, qualityStocks: ArrayList<StockDayModel>): Boolean {
        if (qualityStocks.size < 2) return false

        return if (qualityStocks[0].yinyang!!) {
            if (qualityStocks[0].tradeNum!! > 10 * 10000 * 100 && qualityStocks[0].turnoverRate!! > 3)
                (qualityStocks[0].openPrice!! - qualityStocks[0].minPrice!!) / (qualityStocks[0].nowPrice!! - qualityStocks[0].openPrice!!) > 2
            else false
        } else {
            if (qualityStocks[0].tradeNum!! > 10 * 10000 * 100 && qualityStocks[0].turnoverRate!! > 3)
                (qualityStocks[0].nowPrice!! - qualityStocks[0].openPrice!!) / (qualityStocks[0].openPrice!! - qualityStocks[0].nowPrice!!) > 2
            else false
        }

        //第二天为阳，第一天为阴
//        return if (!qualityStocks[1].yinyang!!) {
        //第二天的实体部份占比1%大小
//            return if ((qualityStocks[0].nowPrice!! - qualityStocks[0].openPrice!!) / qualityStocks[0].openPrice!! > 0.03f) {
//                if ((qualityStocks[0].openPrice!! - qualityStocks[0].minPrice!!) / (qualityStocks[0].nowPrice!! - qualityStocks[0].openPrice!!) > 2f) {
//                    (qualityStocks[0].maxPrice!! - qualityStocks[0].nowPrice!!) / (qualityStocks[0].nowPrice!! - qualityStocks[0].openPrice!!) < 1f
//                } else false
//            } else false
//        } else false
    }

    fun judgeEngulfStock(stockCode: String, qualityStocks: ArrayList<StockDayModel>): Boolean {
        if (qualityStocks.size < 2) return false

        return if (qualityStocks[0].yinyang!! && !qualityStocks[1].yinyang!!) {
            if (qualityStocks[0].nowPrice!! > qualityStocks[1].openPrice!!) {
                if (qualityStocks[0].openPrice!! < qualityStocks[1].nowPrice!!)
                    fictitiousTradingVolume(qualityStocks[0].tradeNum!!) > qualityStocks[1].tradeNum!! else false
            } else false
        } else false
    }

    fun judgeFlatBottomsStock(stockCode: String, qualityStocks: ArrayList<StockDayModel>): Boolean {
        if (qualityStocks.size < 3) return false

        if (qualityStocks[0].yinyang!! && !qualityStocks[1].yinyang!!)
            if (abs((qualityStocks[0].minPrice!! - qualityStocks[1].minPrice!!) / qualityStocks[0].minPrice!!) < 0.003f)
                if (abs((qualityStocks[0].openPrice!! - qualityStocks[1].nowPrice!!) / qualityStocks[0].openPrice!!) < 0.003f)
                    if (fictitiousTradingVolume(qualityStocks[0].tradeNum!!) > qualityStocks[1].tradeNum!!)
                        return true

        if (qualityStocks[0].yinyang!! && !qualityStocks[1].yinyang!!)
            if (abs((qualityStocks[0].openPrice!! - qualityStocks[1].nowPrice!!) / qualityStocks[0].openPrice!!) < 0.003f)
                if (fictitiousTradingVolume(qualityStocks[0].tradeNum!!) > qualityStocks[1].tradeNum!!)
                    return true

        if (qualityStocks[0].yinyang!! && !qualityStocks[1].yinyang!!)
            if (abs((qualityStocks[0].minPrice!! - qualityStocks[1].minPrice!!) / qualityStocks[0].minPrice!!) < 0.003f)
                if (fictitiousTradingVolume(qualityStocks[0].tradeNum!!) > qualityStocks[1].tradeNum!!)
                    return true

        return false
    }

    fun judgePregnantStock(stockCode: String, qualityStocks: ArrayList<StockDayModel>): Boolean {
        if (qualityStocks.size < 2) return false

        return if (qualityStocks[0].yinyang!! && !qualityStocks[1].yinyang!!) {
            if (qualityStocks[0].openPrice!! > qualityStocks[1].nowPrice!!) {
                if (qualityStocks[0].nowPrice!! < qualityStocks[1].openPrice!!) {
                    if (fictitiousTradingVolume(qualityStocks[0].tradeNum!!) > qualityStocks[1].tradeNum!!) {
                        if (checkLineWingRate(
                                qualityStocks[1].openPrice!!,
                                qualityStocks[1].nowPrice!!,
                                qualityStocks[0].openPrice!!
                            ) > 0.02f
                        ) {
                            checkLineWingRate(
                                qualityStocks[0].nowPrice!!,
                                qualityStocks[0].openPrice!!,
                                qualityStocks[0].openPrice!!
                            ) > 0.01f
                        } else false
                    } else false
                } else false
            } else false
        } else false
    }

    fun judgePregnantPlusStock(
        stockCode: String,
        qualityStocks: ArrayList<StockDayModel>
    ): Boolean {
        if (qualityStocks.size < 3) return false

        return if (qualityStocks[0].yinyang!! && !qualityStocks[1].yinyang!! && !qualityStocks[2].yinyang!!) {
            if (qualityStocks[0].openPrice!! > qualityStocks[1].nowPrice!!) {
                if (qualityStocks[0].nowPrice!! < qualityStocks[1].openPrice!!) {
                    if (fictitiousTradingVolume(qualityStocks[0].tradeNum!!) > qualityStocks[1].tradeNum!!) {
                        qualityStocks[1].stockRate!! < 0 && qualityStocks[2].stockRate!! < 0
                    } else false
                } else false
            } else false
        } else false
    }

    private fun checkLineWingRate(highPrice: Float, lowPrice: Float, openPrice: Float): Float {
        return (highPrice - lowPrice) / openPrice
    }

    fun judgeFallEndStock(stockCode: String, qualityStocks: ArrayList<StockDayModel>): Boolean {
        if (qualityStocks.size < 2) return false

        return if (qualityStocks[0].yinyang!! && !qualityStocks[1].yinyang!!) {
            if (abs((qualityStocks[0].minPrice!! - qualityStocks[1].minPrice!!) / qualityStocks[0].minPrice!!) < 0.003f) {
                if (((qualityStocks[1].nowPrice!! - qualityStocks[1].minPrice!!) / (qualityStocks[0].openPrice!! - qualityStocks[0].minPrice!!)) > 2f) {
                    if ((qualityStocks[0].nowPrice!! - qualityStocks[0].openPrice!!) / qualityStocks[0].openPrice!! < 0.015f) {
                        (qualityStocks[1].nowPrice!! - qualityStocks[1].minPrice!!) / qualityStocks[1].nowPrice!! > 0.02f
                    } else false
                } else false
            } else false
        } else false
    }

    fun judgeDoubleNeedleStock(
        stockCode: String,
        qualityStocks: ArrayList<StockDayModel>
    ): Boolean {
        if (qualityStocks.size < 3) return false

        if (qualityStocks[0].yinyang!!) {
            if (abs(qualityStocks[0].nowPrice!! - qualityStocks[0].openPrice!!) / abs(qualityStocks[0].maxPrice!! - qualityStocks[0].nowPrice!!) > 1f) {
                if (abs(qualityStocks[0].openPrice!! - qualityStocks[0].minPrice!!) / abs(
                        qualityStocks[0].nowPrice!! - qualityStocks[0].openPrice!!
                    ) > 2.5f
                ) {
//                    for (i in qualityStocks.indices) {
//                        if (i != 0) {
//                            if (qualityStocks[0].minPrice!! > qualityStocks[i].nowPrice!!)
//                                return false
//                        }
//                    }
                } else return false
            } else return false
        } else {
            if (abs(qualityStocks[0].nowPrice!! - qualityStocks[0].openPrice!!) / abs(qualityStocks[0].maxPrice!! - qualityStocks[0].openPrice!!) > 1f) {
                if (abs(qualityStocks[0].nowPrice!! - qualityStocks[0].minPrice!!) / abs(
                        qualityStocks[0].nowPrice!! - qualityStocks[0].openPrice!!
                    ) > 2.5f
                ) {
//                    for (i in qualityStocks.indices) {
//                        if (i != 0) {
//                            if (qualityStocks[0].minPrice!! > qualityStocks[i].nowPrice!!)
//                                return false
//                        }
//                    }
                } else return false
            } else return false
        }

        if (qualityStocks[1].yinyang!!) {
//            if (abs(qualityStocks[1].nowPrice!! - qualityStocks[1].openPrice!!) / abs(qualityStocks[1].maxPrice!! - qualityStocks[1].nowPrice!!) > 1f) {
            if (abs(qualityStocks[1].openPrice!! - qualityStocks[1].minPrice!!) / abs(
                    qualityStocks[1].nowPrice!! - qualityStocks[1].openPrice!!
                ) > 2.5f
            ) {
//                    for (i in qualityStocks.indices) {
//                        if (i != 0) {
//                            if (qualityStocks[1].minPrice!! > qualityStocks[i].nowPrice!!)
//                                return false
//                        }
//                    }
            } else return false
//            } else return false
        } else {
//            if (abs(qualityStocks[1].nowPrice!! - qualityStocks[1].openPrice!!) / abs(qualityStocks[1].maxPrice!! - qualityStocks[1].openPrice!!) > 1f) {
            if (abs(qualityStocks[1].nowPrice!! - qualityStocks[1].minPrice!!) / abs(
                    qualityStocks[1].nowPrice!! - qualityStocks[1].openPrice!!
                ) > 2.5f
            ) {
//                    for (i in qualityStocks.indices) {
//                        if (i != 0) {
//                            if (qualityStocks[1].minPrice!! > qualityStocks[i].nowPrice!!)
//                                return false
//                        }
//                    }
            } else return false
//            } else return false
        }

//        return true
        return (abs((qualityStocks[0].minPrice!! - qualityStocks[1].minPrice!!) / qualityStocks[1].nowPrice!!)) < 0.005
    }

    fun judgeHighTradingStock(stockCode: String, qualityStocks: ArrayList<StockDayModel>): Boolean {
        if (qualityStocks.size < 2) return false
        if (qualityStocks[0].yinyang!! && !qualityStocks[1].yinyang!!)
            if (fictitiousTradingVolume(qualityStocks[0].tradeNum!!.toLong()) / qualityStocks[1].tradeNum!! < 2)
                return false

        if (qualityStocks.size < 5) return false

        if (qualityStocks[0].yinyang!!)
            if (fictitiousTradingVolume(qualityStocks[0].tradeNum!!) > qualityStocks[1].tradeNum!!)
                if (qualityStocks[1].tradeNum!! > qualityStocks[2].tradeNum!!)
                    if (qualityStocks[2].tradeNum!! > qualityStocks[3].tradeNum!!)
                        if (qualityStocks[3].tradeNum!! > qualityStocks[4].tradeNum!!)
                            return true

        if (qualityStocks.size < 5) return false

        if (qualityStocks[0].yinyang!!)
            if (fictitiousTradingVolume(qualityStocks[0].tradeNum!!) > qualityStocks[1].tradeNum!!)
                if (qualityStocks[1].tradeNum!! > qualityStocks[2].tradeNum!!)
                    if (qualityStocks[2].tradeNum!! > qualityStocks[3].tradeNum!!)
                        return true

        return false
    }

    fun judgeResonateStock(stockCode: String, qualityStocks: ArrayList<StockItemCalculateModel>, beforeDay: Boolean) : Boolean{
        if (qualityStocks.size < 15) return false

        if (beforeDay)
            for (i in 1..BaseApplication.instance?.filterOptions?.beforeDay!!){
                qualityStocks.removeAt(0)
            }

//        val kLines = ModelConversionUtils.stockDayModelToKLineEntity(qualityStocks)

        var goldX = 0
        var goldPrice = 0f
        var goldDif = 0f
        var deadX = 0
        var dateTime = "2020-01-01"

        if (qualityStocks[0].macd > 0 && qualityStocks[1].macd < 0) {
            goldDif = qualityStocks[0].dif
            goldPrice = qualityStocks[0].nowPrice!!.toFloat()
            dateTime = qualityStocks[0].dateTime!!
            for (index in 1 until qualityStocks.size - 1) {
                if (qualityStocks[index].macd < 0 && qualityStocks[index + 1].macd > 0 && deadX == 0) {
                    deadX = 1
                }else if (deadX == 1 && goldX == 0) {
                    if (qualityStocks[0].macd > 0 && qualityStocks[0].macd < 0){
                        goldX = 1
                    }
                }else if (goldX == 1){
                    if (qualityStocks[index].macd > qualityStocks[index + 1].macd &&
                            qualityStocks[index + 1].macd < qualityStocks[index + 2].macd){
                        if (goldPrice < qualityStocks[index].nowPrice!!.toFloat() &&
                                goldDif > qualityStocks[index].dif) {
                            println("------------------>Gold X Code" + qualityStocks[index].stockCode + "------Date : $dateTime")
                            return true
                        }
                    }
                }
            }
        }

//        if (qualityStocks[0].macd > 0 && qualityStocks[1].macd < 0)
////            if (qualityStocks[0].dif < 0 && qualityStocks[0].dea < 0)
//                if (qualityStocks[0].j >= qualityStocks[0].k && qualityStocks[0].k >= qualityStocks[0].d)
//                    if (qualityStocks[1].d >= qualityStocks[1].k && qualityStocks[1].k >= qualityStocks[1].j)
//                        if(qualityStocks[0].rsi < 20 || (qualityStocks[0].rsi > 50 && qualityStocks[0].rsi < 80))
//                            if (qualityStocks[0].rsi > qualityStocks[1].rsi)
//                                return true

//        if (qualityStocks[0].macd > qualityStocks[1].macd && qualityStocks[1].macd < qualityStocks[2].macd
//                && qualityStocks[1].rsi < 20){
//            return true
//        }
//
//        var wrStatus = 0
//
//        if (abs(qualityStocks[0].r) > 90)
//            wrStatus += 1
//        if (abs(qualityStocks[1].r) > 90)
//            wrStatus += 1
//        if (abs(qualityStocks[2].r) > 90)
//            wrStatus += 1
//        if (abs(qualityStocks[3].r) > 90)
//            wrStatus += 1
//        if (abs(qualityStocks[4].r) > 90)
//            wrStatus += 1
//        if (abs(qualityStocks[5].r) > 90)
//            wrStatus += 1
//        if (abs(qualityStocks[6].r) > 90)
//            wrStatus += 1
//
//        if (abs(qualityStocks[0].r) > 90 && wrStatus > 3)
//            return true
//
//        if (abs(qualityStocks[0].r) < 80 && abs(qualityStocks[1].r) > 80)
//            return true

//        for (index in qualityStocks.indices){
//            if (qualityStocks[index].dif > qualityStocks[index].dea)
//                if (index < 30)
//                if (index + 1 < qualityStocks.size)
//                    if (qualityStocks[index].macd > 0 && qualityStocks[index + 1].macd < 0) {
//                        if (goldDif == 0f && goldPrice == 0f){
//                            goldDif = qualityStocks[index].dif
//                            goldPrice = qualityStocks[index].nowPrice!!.toFloat()
//                            dateTime = qualityStocks[0].dateTime!!
//                            println("------------------>Gold X Code 1: " + qualityStocks[index].stockCode + "------Date : " + qualityStocks[index].dateTime)
//                        }else if (goldPrice < qualityStocks[index].nowPrice!!.toFloat() &&
//                                goldDif > qualityStocks[index].dif &&
//                            DateUtils.compareDate(qualityStocks[0].dateTime!!, dateTime)){
//                            goldX += 1
//
//                            println("------------------>Gold X Code 2: " + qualityStocks[index].stockCode + "------Date : " + qualityStocks[index].dateTime)
//
//                            goldDif = qualityStocks[index].dif
//                            goldPrice = qualityStocks[index].nowPrice!!.toFloat()
//                        }else{
//                            println("------------------>Gold X Code 3: " + qualityStocks[index].stockCode + "------Date : " + qualityStocks[index].dateTime)
//                            goldDif = qualityStocks[index].dif
//                            goldPrice = qualityStocks[index].nowPrice!!.toFloat()
//                            dateTime = qualityStocks[0].dateTime!!
//                        }
//                    }
//
//
//
////                        if (qualityStocks[index].dif < 0 && qualityStocks[index].dea < 0)
////                            if (qualityStocks[index + 1].dif < qualityStocks[index + 1].dea)
////
////                        if (qualityStocks[index + 1].dif < qualityStocks[index].dea)
////                            if (qualityStocks[index].dif < 0 && qualityStocks[index].dea < 0) {
////                                goldX += 1
////                                println("------------------>Gold X Code : " + qualityStocks[index].stockCode + "------Date : " + qualityStocks[index].dateTime)
////                            }
//
//        }
//
//        if (goldX > 0)
//            return true

        return false
    }

    fun fictitiousTradingVolume(tradeNum: Long): Long {
        if (!DateUtils.isCurrentInTimeScope()) return tradeNum

        var nowTime = 0

        if (DateUtils.isCurrentInTimeScope(9, 30, 11, 30)) {
            nowTime = DateUtils.minuteBetween("09:30:00")
        }

        if (DateUtils.isCurrentInTimeScope(11, 30, 13, 0)) {
            nowTime = 120
        }

        if (DateUtils.isCurrentInTimeScope(13, 0, 15, 0)) {
            nowTime = DateUtils.minuteBetween("13:00:00") + 120
        }

        if (nowTime == 0)
            return tradeNum

        return tradeNum * 240 / nowTime
    }

    fun judgeStockLevel(stockCode: String): String {
        if (LitePalDBase.checkLowPREStock(stockCode) &&
            LitePalDBase.checkHighRevenueStock(stockCode, 20) &&
            LitePalDBase.checkNetAssetsRateStock(stockCode)
        )
            return "5"
        else if (LitePalDBase.checkLowPREStock(stockCode, 50, 20) &&
            LitePalDBase.checkHighRevenueStock(stockCode, 10) &&
            LitePalDBase.checkNetAssetsRateStock(stockCode, 5)
        )
            return "4"
        else if (LitePalDBase.checkLowPREStock(stockCode, 100, 30) &&
            LitePalDBase.checkHighRevenueStock(stockCode, 0) &&
            LitePalDBase.checkNetAssetsRateStock(stockCode, 5)
        )
            return "3"
        else if (LitePalDBase.checkLowPREStock(stockCode, 100, 40) &&
            LitePalDBase.checkHighRevenueStock(stockCode, -30) &&
            LitePalDBase.checkNetAssetsRateStock(stockCode, -5)
        )
            return "2"
        else if (LitePalDBase.checkLowPREStock(stockCode, 100, 50) &&
            LitePalDBase.checkHighRevenueStock(stockCode, -80) &&
            LitePalDBase.checkNetAssetsRateStock(stockCode, -20)
        )
            return "1"
        else if (LitePalDBase.checkHighRevenueStock(stockCode, 0) &&
            LitePalDBase.checkNetAssetsRateStock(stockCode, 0)
        )
            return "3"
        else
            return "0"

    }

    fun judgeDebugById(
        stockCode: String,
        debugID: Int = 0,
        qualityStocks: ArrayList<StockDayModel>
    ): Boolean {
        return when (debugID) {
            0 -> judgeTestStock0(stockCode, qualityStocks)
            1 -> judgeTestStock(stockCode, qualityStocks)
            2 -> judgeTestStock2(stockCode, qualityStocks)
            3 -> judgeTestStock3(stockCode, qualityStocks)
            4 -> judgeTestStock4(stockCode, qualityStocks)
            5 -> judgeTestStock5(stockCode, qualityStocks)
            6 -> judgeTestStock6(stockCode, qualityStocks)
            7 -> judgeTestStock7(stockCode, qualityStocks)
            8 -> judgeTestStock8(stockCode, qualityStocks)
            9 -> judgeTestStock9(stockCode, qualityStocks)
            10 -> judgeTestStock10(stockCode, qualityStocks)
            11 -> judgeTestStock11(stockCode, qualityStocks)
            12 -> judgeTestStock12(stockCode, qualityStocks)
            13 -> judgeTestStock13(stockCode, qualityStocks)
            14 -> judgeTestStock14(stockCode, qualityStocks)
            15 -> judgeTestStock15(stockCode, qualityStocks)
            16 -> judgeTestStock16(stockCode, qualityStocks)
            17 -> judgeTestStock17(stockCode, qualityStocks)
            18 -> judgeTestStock18(stockCode, qualityStocks)
            19 -> judgeTestStock19(stockCode, qualityStocks)
            20 -> judgeTestStock20(stockCode, qualityStocks)
            21 -> judgeTestStock21(stockCode, qualityStocks)
            22 -> judgeTestStock22(stockCode, qualityStocks)
            23 -> judgeTestStock23(stockCode, qualityStocks)
            24 -> judgeTestStock24(stockCode, qualityStocks)
            25 -> judgeTestStock25(stockCode, qualityStocks)
            26 -> judgeTestStock26(stockCode, qualityStocks)
            else -> false
        }
    }

    private fun judgeTestStock0(
        stockCode: String,
        qualityStocks: ArrayList<StockDayModel>
    ): Boolean {
        if (qualityStocks.size < 5) return false

        return qualityStocks[1].yinyang!! &&
                !qualityStocks[3].yinyang!! &&
                qualityStocks[5].yinyang!! &&
                !qualityStocks[6].yinyang!! &&
                qualityStocks[1].tradeNum!! > qualityStocks[2].tradeNum!! &&
                qualityStocks[2].tradeNum!! < qualityStocks[3].tradeNum!! &&
                qualityStocks[3].tradeNum!! > qualityStocks[4].tradeNum!! &&
                qualityStocks[4].tradeNum!! < qualityStocks[5].tradeNum!! &&
                qualityStocks[5].tradeNum!! > qualityStocks[6].tradeNum!!
    }

    private fun judgeTestStock(
        stockCode: String,
        qualityStocks: ArrayList<StockDayModel>
    ): Boolean {
        if (qualityStocks.size < 5) return false

        //判断上影线小于实体的1.5倍，下影线大于实体的3倍，并且倒数第二天最低价为最近N天的新低
        if (qualityStocks[1].yinyang!!) {
            if ((qualityStocks[1].nowPrice!! - qualityStocks[1].openPrice!!) / (qualityStocks[1].maxPrice!! - qualityStocks[1].nowPrice!!) > 1.5f) {
                if ((qualityStocks[1].openPrice!! - qualityStocks[1].minPrice!!) / (qualityStocks[1].nowPrice!! - qualityStocks[1].openPrice!!) > 3f) {
                    for (i in qualityStocks.indices) {
                        if (i != 0) {
                            if (qualityStocks[1].minPrice!! > qualityStocks[i].nowPrice!!)
                                return false
                        }
                    }
                } else return false
            } else return false
        } else {
            if ((qualityStocks[1].openPrice!! - qualityStocks[1].nowPrice!!) / (qualityStocks[1].maxPrice!! - qualityStocks[1].openPrice!!) > 1.5f) {
                if ((qualityStocks[1].nowPrice!! - qualityStocks[1].minPrice!!) / (qualityStocks[1].openPrice!! - qualityStocks[1].nowPrice!!) > 3f) {
                    for (i in qualityStocks.indices) {
                        if (i != 0) {
                            if (qualityStocks[1].minPrice!! > qualityStocks[i].openPrice!!)
                                return false
                        }
                    }
                } else return false
            } else return false
        }

        //判断最近的跌幅超5%
        if ((qualityStocks[1].nowPrice!! - qualityStocks[5].openPrice!!) / qualityStocks[5].openPrice!! < -0.05f) {
            return true
        }

        return false
    }

    private fun judgeTestStock2(
        stockCode: String,
        qualityStocks: ArrayList<StockDayModel> = ArrayList()
    ): Boolean {
        if (stockCode.isEmpty()) return false

        return LitePalDBase.checkLowPREStock(stockCode) &&
                LitePalDBase.checkHighRevenueStock(stockCode)
    }

    private fun judgeTestStock3(
        stockCode: String,
        qualityStocks: ArrayList<StockDayModel>
    ): Boolean {
        if (qualityStocks.size < 5) return false
//        下跌反弹机会
        if (!qualityStocks[0].yinyang!! && qualityStocks[1].yinyang!! && qualityStocks[2].yinyang!! && qualityStocks[3].yinyang!!)
            if (qualityStocks[0].openPrice!! < qualityStocks[1].nowPrice!!)
                if (qualityStocks[0].nowPrice!! > qualityStocks[2].openPrice!!)
                    if (qualityStocks[1].openPrice!! > qualityStocks[2].nowPrice!!)
                        if (qualityStocks[0].nowPrice!! < qualityStocks[1].openPrice!!)
                            if (qualityStocks[0].openPrice!! > qualityStocks[1].openPrice!!)
                                return true

        return false
    }

    private fun judgeTestStock4(
        stockCode: String,
        qualityStocks: ArrayList<StockDayModel>
    ): Boolean {

        return qualityStocks[0].stockRate!! > BaseApplication.instance?.filterOptions?.startRate!! &&
                qualityStocks[0].stockRate!! < BaseApplication.instance?.filterOptions?.endRate!!
    }

    private fun judgeTestStock5(
        stockCode: String,
        qualityStocks: ArrayList<StockDayModel>
    ): Boolean {

        if (!qualityStocks[2].yinyang!! && qualityStocks[3].yinyang!! &&
            qualityStocks[5].yinyang!! &&
            qualityStocks[6].yinyang!! &&
            qualityStocks[7].yinyang!! &&
            qualityStocks[2].tradeNum!! > qualityStocks[3].tradeNum!! &&
            qualityStocks[3].tradeNum!! < qualityStocks[4].tradeNum!! &&
            qualityStocks[4].tradeNum!! < qualityStocks[5].tradeNum!! &&
            qualityStocks[5].tradeNum!! < qualityStocks[6].tradeNum!! &&
            qualityStocks[6].tradeNum!! < qualityStocks[7].tradeNum!! &&
            qualityStocks[7].tradeNum!! > qualityStocks[8].tradeNum!!
        )
            return true

        return false
    }

    private fun judgeTestStock6(
        stockCode: String,
        qualityStocks: ArrayList<StockDayModel>
    ): Boolean {
        if (qualityStocks.size < 6) return false

        val totalTradeNum = qualityStocks[1].tradeNum!! +
                qualityStocks[2].tradeNum!! +
                qualityStocks[3].tradeNum!! +
                qualityStocks[4].tradeNum!! +
                qualityStocks[5].tradeNum!!

        val fiveDayTradeRate = totalTradeNum.toFloat() / 1200

        var nowTime = 240

        if (DateUtils.isCurrentInTimeScope(9, 30, 11, 30)) {
            nowTime = DateUtils.minuteBetween("09:30:00")
        }
        if (DateUtils.isCurrentInTimeScope(11, 30, 13, 0)) {
            nowTime = 120
        }
        if (DateUtils.isCurrentInTimeScope(13, 0, 15, 0)) {
            nowTime = DateUtils.minuteBetween("13:00:00") + 120
        }

        val todayTradeRate = qualityStocks[0].tradeNum!!.toFloat() / nowTime

        return todayTradeRate / fiveDayTradeRate > 1 && qualityStocks[0].stockRate!! > 0
                && qualityStocks[0].stockRate!! < 5
    }

    private fun judgeTestStock7(
        stockCode: String,
        qualityStocks: ArrayList<StockDayModel>
    ): Boolean {
        if (qualityStocks.size < 11) return false

        val totalTradeNum = qualityStocks[1].tradeNum!! +
                qualityStocks[2].tradeNum!! +
                qualityStocks[3].tradeNum!! +
                qualityStocks[4].tradeNum!! +
                qualityStocks[5].tradeNum!! +
                qualityStocks[6].tradeNum!! +
                qualityStocks[7].tradeNum!! +
                qualityStocks[8].tradeNum!! +
                qualityStocks[9].tradeNum!! +
                qualityStocks[10].tradeNum!!

        val fiveDayTradeRate = totalTradeNum.toFloat() / 2400

        var nowTime = 240

        if (DateUtils.isCurrentInTimeScope(9, 30, 11, 30)) {
            nowTime = DateUtils.minuteBetween("09:30:00")
        }
        if (DateUtils.isCurrentInTimeScope(11, 30, 13, 0)) {
            nowTime = 120
        }
        if (DateUtils.isCurrentInTimeScope(13, 0, 15, 0)) {
            nowTime = DateUtils.minuteBetween("13:00:00") + 120
        }

        val todayTradeRate = qualityStocks[0].tradeNum!!.toFloat() / nowTime

        return todayTradeRate / fiveDayTradeRate > 1 && qualityStocks[0].stockRate!! > 0
                && qualityStocks[0].stockRate!! < 5
    }

    private fun judgeTestStock8(
        stockCode: String,
        qualityStocks: ArrayList<StockDayModel>
    ): Boolean {
        if (qualityStocks.size < 11) return false

        return if (qualityStocks[0].stockRate!! > 5) {
            val prices = ArrayList<Float>().apply {
                qualityStocks.forEach {
                    add(it.maxPrice!!)
                }
            }
            prices[0] = qualityStocks[0].nowPrice!!
            Collections.max(prices) == qualityStocks[0].nowPrice!!
        } else
            false
    }

    private fun judgeTestStock9(
        stockCode: String,
        qualityStocks: ArrayList<StockDayModel>
    ): Boolean {
        if (stockCode.isEmpty()) return false

        return LitePalDBase.checkLowPREStock(stockCode) &&
                LitePalDBase.checkHighRevenueStock(stockCode) &&
                LitePalDBase.checkNetAssetsRateStock(stockCode)
    }

    private fun judgeTestStock10(
        stockCode: String,
        qualityStocks: ArrayList<StockDayModel>
    ): Boolean {
        if (qualityStocks.size < 10) return false

        val fiveAveragePrice = (qualityStocks[0].nowPrice!! + qualityStocks[1].nowPrice!! +
                qualityStocks[2].nowPrice!! + qualityStocks[3].nowPrice!! + qualityStocks[4].nowPrice!!) / 5F

        if (qualityStocks[0].yinyang!!) {
            if (qualityStocks[0].stockRate!! > 4.5)
                if (qualityStocks[0].nowPrice!! > fiveAveragePrice && qualityStocks[0].openPrice!! < fiveAveragePrice)
                    if ((qualityStocks[0].nowPrice!! - fiveAveragePrice) > (fiveAveragePrice - qualityStocks[0].openPrice!!))
                        return true
        } else {
            if (qualityStocks[0].stockRate!! > 4.5)
                if (qualityStocks[0].openPrice!! > fiveAveragePrice && qualityStocks[0].nowPrice!! < fiveAveragePrice)
                    if ((qualityStocks[0].openPrice!! - fiveAveragePrice) > (fiveAveragePrice - qualityStocks[0].nowPrice!!))
                        return true
        }

        return false
    }

    private fun judgeTestStock11(
        stockCode: String,
        qualityStocks: ArrayList<StockDayModel>
    ): Boolean {
        if (qualityStocks.size < 10) return false

        var fiveAveragePrice = (qualityStocks[2].nowPrice!! + qualityStocks[3].nowPrice!! +
                qualityStocks[4].nowPrice!! + qualityStocks[5].nowPrice!! + qualityStocks[6].nowPrice!!) / 5F

        val todayAveragePrice = (qualityStocks[1].nowPrice!! + qualityStocks[2].nowPrice!! +
                qualityStocks[3].nowPrice!! + qualityStocks[4].nowPrice!! + qualityStocks[5].nowPrice!!) / 5F

        if (qualityStocks[1].stockRate!! > 5)
            if (qualityStocks[1].nowPrice!! > fiveAveragePrice && qualityStocks[1].openPrice!! < fiveAveragePrice)
                if (abs(qualityStocks[0].nowPrice!! - todayAveragePrice) / qualityStocks[0].openPrice!! < 0.003f)
                    return true


        fiveAveragePrice = (qualityStocks[3].nowPrice!! + qualityStocks[4].nowPrice!! +
                qualityStocks[5].nowPrice!! + qualityStocks[6].nowPrice!! + qualityStocks[7].nowPrice!!) / 5F

        if (qualityStocks[2].stockRate!! > 5)
            if (qualityStocks[2].nowPrice!! > fiveAveragePrice && qualityStocks[2].openPrice!! < fiveAveragePrice)
                if (abs(qualityStocks[0].nowPrice!! - todayAveragePrice) / qualityStocks[0].openPrice!! < 0.003f)
                    return true

        fiveAveragePrice = (qualityStocks[4].nowPrice!! + qualityStocks[5].nowPrice!! +
                qualityStocks[6].nowPrice!! + qualityStocks[7].nowPrice!! + qualityStocks[8].nowPrice!!) / 5F

        if (qualityStocks[3].stockRate!! > 5)
            if (qualityStocks[3].nowPrice!! > fiveAveragePrice && qualityStocks[3].openPrice!! < fiveAveragePrice)
                if (abs(qualityStocks[0].nowPrice!! - todayAveragePrice) / qualityStocks[0].openPrice!! < 0.003f)
                    return true

        fiveAveragePrice = (qualityStocks[5].nowPrice!! + qualityStocks[6].nowPrice!! +
                qualityStocks[7].nowPrice!! + qualityStocks[8].nowPrice!! + qualityStocks[9].nowPrice!!) / 5F

        if (qualityStocks[4].stockRate!! > 5)
            if (qualityStocks[4].nowPrice!! > fiveAveragePrice && qualityStocks[4].openPrice!! < fiveAveragePrice)
                if (abs(qualityStocks[0].nowPrice!! - todayAveragePrice) / qualityStocks[0].openPrice!! < 0.003f)
                    return true

        return false
    }

    private fun judgeTestStock12(
        stockCode: String,
        qualityStocks: ArrayList<StockDayModel>
    ): Boolean {
        if (qualityStocks.size < 15) return false

        val fiveAveragePrice = (qualityStocks[0].nowPrice!! + qualityStocks[1].nowPrice!! +
                qualityStocks[2].nowPrice!! + qualityStocks[3].nowPrice!! + qualityStocks[4].nowPrice!!) / 5F

        val eightAveragePrice = (qualityStocks[0].nowPrice!! + qualityStocks[1].nowPrice!! +
                qualityStocks[2].nowPrice!! + qualityStocks[3].nowPrice!! + qualityStocks[4].nowPrice!! +
                qualityStocks[5].nowPrice!! + qualityStocks[6].nowPrice!! + qualityStocks[7].nowPrice!!) / 8F

        val thirteenAveragePrice = (qualityStocks[0].nowPrice!! + qualityStocks[1].nowPrice!! +
                qualityStocks[2].nowPrice!! + qualityStocks[3].nowPrice!! + qualityStocks[4].nowPrice!! +
                qualityStocks[5].nowPrice!! + qualityStocks[6].nowPrice!! + qualityStocks[7].nowPrice!! +
                qualityStocks[8].nowPrice!! + qualityStocks[9].nowPrice!! + qualityStocks[10].nowPrice!! +
                qualityStocks[11].nowPrice!! + qualityStocks[12].nowPrice!!) / 13F

        if (qualityStocks[0].yinyang!!) {
            if (qualityStocks[0].stockRate!! > 3)
                if (qualityStocks[0].nowPrice!! > fiveAveragePrice && qualityStocks[0].openPrice!! < fiveAveragePrice &&
                    qualityStocks[0].nowPrice!! > eightAveragePrice && qualityStocks[0].openPrice!! < eightAveragePrice &&
                    qualityStocks[0].nowPrice!! > thirteenAveragePrice && qualityStocks[0].openPrice!! < thirteenAveragePrice
                )
                    if (checkVolumeRate(stockCode, qualityStocks))
                        return true
        } else {
            if (qualityStocks[0].stockRate!! > 3)
                if (qualityStocks[0].openPrice!! > fiveAveragePrice && qualityStocks[0].nowPrice!! < fiveAveragePrice &&
                    qualityStocks[0].openPrice!! > eightAveragePrice && qualityStocks[0].nowPrice!! < eightAveragePrice &&
                    qualityStocks[0].openPrice!! > thirteenAveragePrice && qualityStocks[0].nowPrice!! < thirteenAveragePrice
                )
                    if (checkVolumeRate(stockCode, qualityStocks))
                        return true
        }

        return false
    }

    private fun judgeTestStock13(
        stockCode: String,
        qualityStocks: ArrayList<StockDayModel>
    ): Boolean {
        if (qualityStocks.size < 15) return false

//        val fiveAveragePrice = (qualityStocks[0].nowPrice!! + qualityStocks[1].nowPrice!! +
//                qualityStocks[2].nowPrice!! + qualityStocks[3].nowPrice!! + qualityStocks[4].nowPrice!!) / 5F
//
//        val eightAveragePrice = (qualityStocks[0].nowPrice!! + qualityStocks[1].nowPrice!! +
//                qualityStocks[2].nowPrice!! + qualityStocks[3].nowPrice!! + qualityStocks[4].nowPrice!! +
//                qualityStocks[5].nowPrice!! + qualityStocks[6].nowPrice!! + qualityStocks[7].nowPrice!!) / 8F
//
//        val thirteenAveragePrice = (qualityStocks[0].nowPrice!! + qualityStocks[1].nowPrice!! +
//                qualityStocks[2].nowPrice!! + qualityStocks[3].nowPrice!! + qualityStocks[4].nowPrice!! +
//                qualityStocks[5].nowPrice!! + qualityStocks[6].nowPrice!! + qualityStocks[7].nowPrice!! +
//                qualityStocks[8].nowPrice!! + qualityStocks[9].nowPrice!! + qualityStocks[10].nowPrice!! +
//                qualityStocks[11].nowPrice!! + qualityStocks[12].nowPrice!!) / 13F

        val fiveAveragePrice = (qualityStocks[0].nowPrice!! + qualityStocks[1].nowPrice!! +
                qualityStocks[2].nowPrice!!) / 3F

        val eightAveragePrice = (qualityStocks[0].nowPrice!! + qualityStocks[1].nowPrice!! +
                qualityStocks[2].nowPrice!! + qualityStocks[3].nowPrice!! + qualityStocks[4].nowPrice!!) / 5F

        val thirteenAveragePrice = (qualityStocks[0].nowPrice!! + qualityStocks[1].nowPrice!! +
                qualityStocks[2].nowPrice!! + qualityStocks[3].nowPrice!! + qualityStocks[4].nowPrice!! +
                qualityStocks[5].nowPrice!! + qualityStocks[6].nowPrice!! + qualityStocks[7].nowPrice!! +
                qualityStocks[8].nowPrice!! + qualityStocks[9].nowPrice!!) / 10F

         if (stockCode == "603677"){
             println("------------------>ertgujl")
         }

        if (qualityStocks[0].yinyang!!) {
            if (qualityStocks[0].stockRate!! > 3)
                if (qualityStocks[0].nowPrice!! > fiveAveragePrice && qualityStocks[0].openPrice!! < fiveAveragePrice &&
                    qualityStocks[0].nowPrice!! > eightAveragePrice && qualityStocks[0].openPrice!! < eightAveragePrice &&
                    qualityStocks[0].nowPrice!! > thirteenAveragePrice && qualityStocks[0].openPrice!! < thirteenAveragePrice
                )
                    if ((qualityStocks[0].nowPrice!! - (fiveAveragePrice + eightAveragePrice + thirteenAveragePrice) / 3) > ((fiveAveragePrice + eightAveragePrice + thirteenAveragePrice) / 3 - qualityStocks[0].openPrice!!))
                        if (checkVolumeRate(stockCode, qualityStocks))
                            return true
        } else {
            if (qualityStocks[0].stockRate!! > 3)
                if (qualityStocks[0].openPrice!! > fiveAveragePrice && qualityStocks[0].nowPrice!! < fiveAveragePrice &&
                    qualityStocks[0].openPrice!! > eightAveragePrice && qualityStocks[0].nowPrice!! < eightAveragePrice &&
                    qualityStocks[0].openPrice!! > thirteenAveragePrice && qualityStocks[0].nowPrice!! < thirteenAveragePrice
                )
                    if ((qualityStocks[0].openPrice!! - (fiveAveragePrice + eightAveragePrice + thirteenAveragePrice) / 3) > ((fiveAveragePrice + eightAveragePrice + thirteenAveragePrice) / 3 - qualityStocks[0].nowPrice!!))
                        if (checkVolumeRate(stockCode, qualityStocks))
                            return true
        }

        return false
    }

    private fun judgeTestStock14(
        stockCode: String,
        qualityStocks: ArrayList<StockDayModel>
    ): Boolean {
        if (qualityStocks.size < 15) return false

        val fiveAveragePrice = (qualityStocks[0].nowPrice!! + qualityStocks[1].nowPrice!! +
                qualityStocks[2].nowPrice!! + qualityStocks[3].nowPrice!! + qualityStocks[4].nowPrice!!) / 5F

        val eightAveragePrice = (qualityStocks[0].nowPrice!! + qualityStocks[1].nowPrice!! +
                qualityStocks[2].nowPrice!! + qualityStocks[3].nowPrice!! + qualityStocks[4].nowPrice!! +
                qualityStocks[5].nowPrice!! + qualityStocks[6].nowPrice!! + qualityStocks[7].nowPrice!!) / 8F

        val thirteenAveragePrice = (qualityStocks[0].nowPrice!! + qualityStocks[1].nowPrice!! +
                qualityStocks[2].nowPrice!! + qualityStocks[3].nowPrice!! + qualityStocks[4].nowPrice!! +
                qualityStocks[5].nowPrice!! + qualityStocks[6].nowPrice!! + qualityStocks[7].nowPrice!! +
                qualityStocks[8].nowPrice!! + qualityStocks[9].nowPrice!! + qualityStocks[10].nowPrice!! +
                qualityStocks[11].nowPrice!! + qualityStocks[12].nowPrice!!) / 13F

        if (qualityStocks[0].yinyang!!) {
            if (qualityStocks[0].stockRate!! > 3)
                if (qualityStocks[0].nowPrice!! > fiveAveragePrice && qualityStocks[0].openPrice!! < fiveAveragePrice &&
                    qualityStocks[0].nowPrice!! > eightAveragePrice && qualityStocks[0].openPrice!! < eightAveragePrice &&
                    qualityStocks[0].nowPrice!! > thirteenAveragePrice && qualityStocks[0].openPrice!! < thirteenAveragePrice
                )
                    if (qualityStocks[0].tradeNum!! > qualityStocks[1].tradeNum!! * 2)
                        return true
        } else {
            if (qualityStocks[0].stockRate!! > 3)
                if (qualityStocks[0].openPrice!! > fiveAveragePrice && qualityStocks[0].nowPrice!! < fiveAveragePrice &&
                    qualityStocks[0].openPrice!! > eightAveragePrice && qualityStocks[0].nowPrice!! < eightAveragePrice &&
                    qualityStocks[0].openPrice!! > thirteenAveragePrice && qualityStocks[0].nowPrice!! < thirteenAveragePrice
                )
                    if (qualityStocks[0].tradeNum!! > qualityStocks[1].tradeNum!! * 2)
                        return true
        }

        return false
    }

    private fun judgeTestStock15(
        stockCode: String,
        qualityStocks: ArrayList<StockDayModel>
    ): Boolean {
        if (qualityStocks.size < 15) return false

        val fiveAveragePrice = (qualityStocks[0].nowPrice!! + qualityStocks[1].nowPrice!! +
                qualityStocks[2].nowPrice!! + qualityStocks[3].nowPrice!! + qualityStocks[4].nowPrice!!) / 5F

        val eightAveragePrice = (qualityStocks[0].nowPrice!! + qualityStocks[1].nowPrice!! +
                qualityStocks[2].nowPrice!! + qualityStocks[3].nowPrice!! + qualityStocks[4].nowPrice!! +
                qualityStocks[5].nowPrice!! + qualityStocks[6].nowPrice!! + qualityStocks[7].nowPrice!!) / 8F

        val thirteenAveragePrice = (qualityStocks[0].nowPrice!! + qualityStocks[1].nowPrice!! +
                qualityStocks[2].nowPrice!! + qualityStocks[3].nowPrice!! + qualityStocks[4].nowPrice!! +
                qualityStocks[5].nowPrice!! + qualityStocks[6].nowPrice!! + qualityStocks[7].nowPrice!! +
                qualityStocks[8].nowPrice!! + qualityStocks[9].nowPrice!! + qualityStocks[10].nowPrice!! +
                qualityStocks[11].nowPrice!! + qualityStocks[12].nowPrice!!) / 13F

        if (qualityStocks[0].yinyang!!) {
            if (qualityStocks[0].stockRate!! > 3)
                if (qualityStocks[0].nowPrice!! > fiveAveragePrice && qualityStocks[0].openPrice!! < fiveAveragePrice &&
                    qualityStocks[0].nowPrice!! > eightAveragePrice && qualityStocks[0].openPrice!! < eightAveragePrice &&
                    qualityStocks[0].nowPrice!! > thirteenAveragePrice && qualityStocks[0].openPrice!! < thirteenAveragePrice
                )
                    if ((qualityStocks[0].nowPrice!! - (fiveAveragePrice + eightAveragePrice + thirteenAveragePrice) / 3) > ((fiveAveragePrice + eightAveragePrice + thirteenAveragePrice) / 3 - qualityStocks[0].openPrice!!))
                        if (qualityStocks[0].tradeNum!! > qualityStocks[1].tradeNum!! * 2)
                            return true
        } else {
            if (qualityStocks[0].stockRate!! > 3)
                if (qualityStocks[0].openPrice!! > fiveAveragePrice && qualityStocks[0].nowPrice!! < fiveAveragePrice &&
                    qualityStocks[0].openPrice!! > eightAveragePrice && qualityStocks[0].nowPrice!! < eightAveragePrice &&
                    qualityStocks[0].openPrice!! > thirteenAveragePrice && qualityStocks[0].nowPrice!! < thirteenAveragePrice
                )
                    if ((qualityStocks[0].openPrice!! - (fiveAveragePrice + eightAveragePrice + thirteenAveragePrice) / 3) > ((fiveAveragePrice + eightAveragePrice + thirteenAveragePrice) / 3 - qualityStocks[0].nowPrice!!))
                        if (qualityStocks[0].tradeNum!! > qualityStocks[1].tradeNum!! * 2)
                            return true
        }

        return false
    }

    private fun judgeTestStock16(
        stockCode: String,
        qualityStocks: ArrayList<StockDayModel>
    ): Boolean {
        if (qualityStocks.size < 15) return false

        val fiveAveragePrice = (qualityStocks[0].nowPrice!! + qualityStocks[1].nowPrice!! +
                qualityStocks[2].nowPrice!! + qualityStocks[3].nowPrice!! + qualityStocks[4].nowPrice!!) / 5F

        val eightAveragePrice = (qualityStocks[0].nowPrice!! + qualityStocks[1].nowPrice!! +
                qualityStocks[2].nowPrice!! + qualityStocks[3].nowPrice!! + qualityStocks[4].nowPrice!! +
                qualityStocks[5].nowPrice!! + qualityStocks[6].nowPrice!! + qualityStocks[7].nowPrice!!) / 8F

        val thirteenAveragePrice = (qualityStocks[0].nowPrice!! + qualityStocks[1].nowPrice!! +
                qualityStocks[2].nowPrice!! + qualityStocks[3].nowPrice!! + qualityStocks[4].nowPrice!! +
                qualityStocks[5].nowPrice!! + qualityStocks[6].nowPrice!! + qualityStocks[7].nowPrice!! +
                qualityStocks[8].nowPrice!! + qualityStocks[9].nowPrice!! + qualityStocks[10].nowPrice!! +
                qualityStocks[11].nowPrice!! + qualityStocks[12].nowPrice!!) / 13F

        if (qualityStocks[0].yinyang!!) {
            if (qualityStocks[0].stockRate!! > 3)
                if (qualityStocks[0].nowPrice!! > fiveAveragePrice && qualityStocks[0].openPrice!! < fiveAveragePrice &&
                    qualityStocks[0].nowPrice!! > eightAveragePrice && qualityStocks[0].openPrice!! < eightAveragePrice &&
                    qualityStocks[0].nowPrice!! > thirteenAveragePrice && qualityStocks[0].openPrice!! < thirteenAveragePrice
                )
                    if (qualityStocks[0].tradeNum!! > qualityStocks[1].tradeNum!! * 2)
                        if (qualityStocks[0].turnoverRate!! + qualityStocks[1].turnoverRate!! +
                            qualityStocks[2].turnoverRate!! + qualityStocks[3].turnoverRate!! +
                            qualityStocks[4].turnoverRate!! > 5
                        )
                            return true
        } else {
            if (qualityStocks[0].stockRate!! > 3)
                if (qualityStocks[0].openPrice!! > fiveAveragePrice && qualityStocks[0].nowPrice!! < fiveAveragePrice &&
                    qualityStocks[0].openPrice!! > eightAveragePrice && qualityStocks[0].nowPrice!! < eightAveragePrice &&
                    qualityStocks[0].openPrice!! > thirteenAveragePrice && qualityStocks[0].nowPrice!! < thirteenAveragePrice
                )
                    if (qualityStocks[0].tradeNum!! > qualityStocks[1].tradeNum!! * 2)
                        if (qualityStocks[0].turnoverRate!! + qualityStocks[1].turnoverRate!! +
                            qualityStocks[2].turnoverRate!! + qualityStocks[3].turnoverRate!! +
                            qualityStocks[4].turnoverRate!! > 5
                        )
                            return true
        }

        return false
    }

    private fun judgeTestStock17(
        stockCode: String,
        qualityStocks: ArrayList<StockDayModel>
    ): Boolean {
        if (qualityStocks.size < 15) return false

        val fiveAveragePrice = (qualityStocks[0].nowPrice!! + qualityStocks[1].nowPrice!! +
                qualityStocks[2].nowPrice!! + qualityStocks[3].nowPrice!! + qualityStocks[4].nowPrice!!) / 5F

        val eightAveragePrice = (qualityStocks[0].nowPrice!! + qualityStocks[1].nowPrice!! +
                qualityStocks[2].nowPrice!! + qualityStocks[3].nowPrice!! + qualityStocks[4].nowPrice!! +
                qualityStocks[5].nowPrice!! + qualityStocks[6].nowPrice!! + qualityStocks[7].nowPrice!!) / 8F

        val thirteenAveragePrice = (qualityStocks[0].nowPrice!! + qualityStocks[1].nowPrice!! +
                qualityStocks[2].nowPrice!! + qualityStocks[3].nowPrice!! + qualityStocks[4].nowPrice!! +
                qualityStocks[5].nowPrice!! + qualityStocks[6].nowPrice!! + qualityStocks[7].nowPrice!! +
                qualityStocks[8].nowPrice!! + qualityStocks[9].nowPrice!! + qualityStocks[10].nowPrice!! +
                qualityStocks[11].nowPrice!! + qualityStocks[12].nowPrice!!) / 13F

        if (qualityStocks[0].yinyang!!) {
            if (qualityStocks[0].stockRate!! > 3)
                if (qualityStocks[0].nowPrice!! > fiveAveragePrice && qualityStocks[0].openPrice!! < fiveAveragePrice &&
                    qualityStocks[0].nowPrice!! > eightAveragePrice && qualityStocks[0].openPrice!! < eightAveragePrice &&
                    qualityStocks[0].nowPrice!! > thirteenAveragePrice && qualityStocks[0].openPrice!! < thirteenAveragePrice
                )
                    if ((qualityStocks[0].nowPrice!! - (fiveAveragePrice + eightAveragePrice + thirteenAveragePrice) / 3) > ((fiveAveragePrice + eightAveragePrice + thirteenAveragePrice) / 3 - qualityStocks[0].openPrice!!))
                        if (qualityStocks[0].tradeNum!! > qualityStocks[1].tradeNum!! * 2)
                            if (qualityStocks[0].turnoverRate!! + qualityStocks[1].turnoverRate!! +
                                qualityStocks[2].turnoverRate!! + qualityStocks[3].turnoverRate!! +
                                qualityStocks[4].turnoverRate!! > 5
                            )
                                return true
        } else {
            if (qualityStocks[0].stockRate!! > 3)
                if (qualityStocks[0].openPrice!! > fiveAveragePrice && qualityStocks[0].nowPrice!! < fiveAveragePrice &&
                    qualityStocks[0].openPrice!! > eightAveragePrice && qualityStocks[0].nowPrice!! < eightAveragePrice &&
                    qualityStocks[0].openPrice!! > thirteenAveragePrice && qualityStocks[0].nowPrice!! < thirteenAveragePrice
                )
                    if ((qualityStocks[0].openPrice!! - (fiveAveragePrice + eightAveragePrice + thirteenAveragePrice) / 3) > ((fiveAveragePrice + eightAveragePrice + thirteenAveragePrice) / 3 - qualityStocks[0].nowPrice!!))
                        if (qualityStocks[0].tradeNum!! > qualityStocks[1].tradeNum!! * 2)
                            if (qualityStocks[0].turnoverRate!! + qualityStocks[1].turnoverRate!! +
                                qualityStocks[2].turnoverRate!! + qualityStocks[3].turnoverRate!! +
                                qualityStocks[4].turnoverRate!! > 5
                            )
                                return true
        }

        return false
    }

    private fun judgeTestStock18(
        stockCode: String,
        qualityStocks: ArrayList<StockDayModel>
    ): Boolean {
        if (qualityStocks.size < 15) return false

        val fiveAveragePrice = (qualityStocks[0].nowPrice!! + qualityStocks[1].nowPrice!! +
                qualityStocks[2].nowPrice!! + qualityStocks[3].nowPrice!! + qualityStocks[4].nowPrice!!) / 5F

        val eightAveragePrice = (qualityStocks[0].nowPrice!! + qualityStocks[1].nowPrice!! +
                qualityStocks[2].nowPrice!! + qualityStocks[3].nowPrice!! + qualityStocks[4].nowPrice!! +
                qualityStocks[5].nowPrice!! + qualityStocks[6].nowPrice!! + qualityStocks[7].nowPrice!!) / 8F

        val thirteenAveragePrice = (qualityStocks[0].nowPrice!! + qualityStocks[1].nowPrice!! +
                qualityStocks[2].nowPrice!! + qualityStocks[3].nowPrice!! + qualityStocks[4].nowPrice!! +
                qualityStocks[5].nowPrice!! + qualityStocks[6].nowPrice!! + qualityStocks[7].nowPrice!! +
                qualityStocks[8].nowPrice!! + qualityStocks[9].nowPrice!! + qualityStocks[10].nowPrice!! +
                qualityStocks[11].nowPrice!! + qualityStocks[12].nowPrice!!) / 13F

        if (qualityStocks[0].yinyang!!) {
            if (qualityStocks[0].stockRate!! > 3)
                if (qualityStocks[0].nowPrice!! > fiveAveragePrice && qualityStocks[0].openPrice!! < fiveAveragePrice &&
                    qualityStocks[0].nowPrice!! > eightAveragePrice && qualityStocks[0].openPrice!! < eightAveragePrice &&
                    qualityStocks[0].nowPrice!! > thirteenAveragePrice && qualityStocks[0].openPrice!! < thirteenAveragePrice
                )
                    if (qualityStocks[0].tradeNum!! > qualityStocks[1].tradeNum!! * 2)
                        if (qualityStocks[0].turnoverRate!! + qualityStocks[1].turnoverRate!! +
                            qualityStocks[2].turnoverRate!! + qualityStocks[3].turnoverRate!! +
                            qualityStocks[4].turnoverRate!! > 5
                        )
                            if (qualityStocks[0].maxPrice!! - qualityStocks[0].nowPrice!! < qualityStocks[0].openPrice!! - qualityStocks[0].minPrice!!)
                                return true
        } else {
            if (qualityStocks[0].stockRate!! > 3)
                if (qualityStocks[0].openPrice!! > fiveAveragePrice && qualityStocks[0].nowPrice!! < fiveAveragePrice &&
                    qualityStocks[0].openPrice!! > eightAveragePrice && qualityStocks[0].nowPrice!! < eightAveragePrice &&
                    qualityStocks[0].openPrice!! > thirteenAveragePrice && qualityStocks[0].nowPrice!! < thirteenAveragePrice
                )
                    if (qualityStocks[0].tradeNum!! > qualityStocks[1].tradeNum!! * 2)
                        if (qualityStocks[0].turnoverRate!! + qualityStocks[1].turnoverRate!! +
                            qualityStocks[2].turnoverRate!! + qualityStocks[3].turnoverRate!! +
                            qualityStocks[4].turnoverRate!! > 5
                        )
                            if (qualityStocks[0].maxPrice!! - qualityStocks[0].openPrice!! < qualityStocks[0].openPrice!! - qualityStocks[0].nowPrice!!)
                                return true
        }

        return false
    }

    private fun judgeTestStock19(
        stockCode: String,
        qualityStocks: ArrayList<StockDayModel>
    ): Boolean {
        if (qualityStocks.size < 15) return false

        val threeAveragePriceToday = (qualityStocks[0].nowPrice!! + qualityStocks[1].nowPrice!! +
                qualityStocks[2].nowPrice!!) / 3F

        val fiveAveragePriceToday = (qualityStocks[0].nowPrice!! + qualityStocks[1].nowPrice!! +
                qualityStocks[2].nowPrice!! + qualityStocks[3].nowPrice!! + qualityStocks[4].nowPrice!!) / 5F

        val tenAveragePriceToday = (qualityStocks[0].nowPrice!! + qualityStocks[1].nowPrice!! +
                qualityStocks[2].nowPrice!! + qualityStocks[3].nowPrice!! + qualityStocks[4].nowPrice!! +
                qualityStocks[5].nowPrice!! + qualityStocks[6].nowPrice!! + qualityStocks[7].nowPrice!! +
                qualityStocks[8].nowPrice!! + qualityStocks[9].nowPrice!!) / 10F

        val threeAveragePriceYest = (qualityStocks[1].nowPrice!! + qualityStocks[2].nowPrice!! +
                qualityStocks[3].nowPrice!!) / 3F

        val fiveAveragePriceYest = (qualityStocks[1].nowPrice!! + qualityStocks[2].nowPrice!! +
                qualityStocks[3].nowPrice!! + qualityStocks[4].nowPrice!! + qualityStocks[5].nowPrice!!) / 5F

        val tenAveragePriceYest = (qualityStocks[8].nowPrice!! + qualityStocks[1].nowPrice!! +
                qualityStocks[2].nowPrice!! + qualityStocks[3].nowPrice!! + qualityStocks[4].nowPrice!! +
                qualityStocks[5].nowPrice!! + qualityStocks[6].nowPrice!! + qualityStocks[7].nowPrice!! +
                qualityStocks[8].nowPrice!! + qualityStocks[9].nowPrice!!) / 10F

        val threeAveragePriceYestYest = (qualityStocks[4].nowPrice!! + qualityStocks[2].nowPrice!! +
                qualityStocks[3].nowPrice!!) / 3F

        val fiveAveragePriceYestYest = (qualityStocks[6].nowPrice!! + qualityStocks[2].nowPrice!! +
                qualityStocks[3].nowPrice!! + qualityStocks[4].nowPrice!! + qualityStocks[5].nowPrice!!) / 5F

        val tenAveragePriceYestYest = (qualityStocks[10].nowPrice!! + qualityStocks[1].nowPrice!! +
                qualityStocks[2].nowPrice!! + qualityStocks[3].nowPrice!! + qualityStocks[4].nowPrice!! +
                qualityStocks[5].nowPrice!! + qualityStocks[6].nowPrice!! + qualityStocks[7].nowPrice!! +
                qualityStocks[8].nowPrice!! + qualityStocks[9].nowPrice!!) / 10F


        if (threeAveragePriceYestYest < fiveAveragePriceYestYest || threeAveragePriceYestYest < tenAveragePriceYestYest ||
            fiveAveragePriceYestYest < tenAveragePriceYestYest
        )
            if (threeAveragePriceYest > fiveAveragePriceYest && fiveAveragePriceToday > tenAveragePriceYest)
                if (threeAveragePriceToday > fiveAveragePriceToday && fiveAveragePriceToday > tenAveragePriceToday)
                    if (qualityStocks[0].nowPrice!! > threeAveragePriceToday)
                        if (qualityStocks[0].stockRate!! > 2 && qualityStocks[0].stockRate!! < 8)
                            return true

        return false
    }

    private fun judgeTestStock20(
        stockCode: String,
        qualityStocks: ArrayList<StockDayModel>
    ): Boolean {
        if (qualityStocks.size < 15) return false

        val fiveAveragePriceToday = (qualityStocks[0].nowPrice!! + qualityStocks[1].nowPrice!! +
                qualityStocks[2].nowPrice!! + qualityStocks[3].nowPrice!! + qualityStocks[4].nowPrice!!) / 5F

        val eightAveragePriceToday = (qualityStocks[0].nowPrice!! + qualityStocks[1].nowPrice!! +
                qualityStocks[2].nowPrice!! + qualityStocks[3].nowPrice!! + qualityStocks[4].nowPrice!! +
                qualityStocks[5].nowPrice!! + qualityStocks[6].nowPrice!! + qualityStocks[7].nowPrice!!) / 8F

        val thirteenAveragePriceToday = (qualityStocks[0].nowPrice!! + qualityStocks[1].nowPrice!! +
                qualityStocks[2].nowPrice!! + qualityStocks[3].nowPrice!! + qualityStocks[4].nowPrice!! +
                qualityStocks[5].nowPrice!! + qualityStocks[6].nowPrice!! + qualityStocks[7].nowPrice!! +
                qualityStocks[8].nowPrice!! + qualityStocks[9].nowPrice!! + qualityStocks[10].nowPrice!! +
                qualityStocks[11].nowPrice!! + qualityStocks[12].nowPrice!!) / 13F

        val fiveAveragePriceYest = (qualityStocks[1].nowPrice!! + qualityStocks[2].nowPrice!! +
                qualityStocks[3].nowPrice!! + qualityStocks[4].nowPrice!! + qualityStocks[5].nowPrice!!) / 5F

        val eightAveragePriceYest = (qualityStocks[8].nowPrice!! + qualityStocks[1].nowPrice!! +
                qualityStocks[2].nowPrice!! + qualityStocks[3].nowPrice!! + qualityStocks[4].nowPrice!! +
                qualityStocks[5].nowPrice!! + qualityStocks[6].nowPrice!! + qualityStocks[7].nowPrice!!) / 8F

        val thirteenAveragePriceYest = (qualityStocks[13].nowPrice!! + qualityStocks[1].nowPrice!! +
                qualityStocks[2].nowPrice!! + qualityStocks[3].nowPrice!! + qualityStocks[4].nowPrice!! +
                qualityStocks[5].nowPrice!! + qualityStocks[6].nowPrice!! + qualityStocks[7].nowPrice!! +
                qualityStocks[8].nowPrice!! + qualityStocks[9].nowPrice!! + qualityStocks[10].nowPrice!! +
                qualityStocks[11].nowPrice!! + qualityStocks[12].nowPrice!!) / 13F

        if (fiveAveragePriceToday > eightAveragePriceToday && eightAveragePriceToday > thirteenAveragePriceToday)
            if (fiveAveragePriceYest < eightAveragePriceYest || fiveAveragePriceYest < thirteenAveragePriceYest ||
                eightAveragePriceYest < thirteenAveragePriceYest
            )
                if (qualityStocks[0].stockRate!! > 2 && qualityStocks[0].stockRate!! < 8)
                    if (qualityStocks[0].nowPrice!! > fiveAveragePriceToday && qualityStocks[0].openPrice!! > thirteenAveragePriceToday)
                        return true

        return false
    }

    private fun judgeTestStock21(
        stockCode: String,
        qualityStocks: ArrayList<StockDayModel>
    ): Boolean {
        if (qualityStocks.size < 15) return false

        val threeAveragePriceToday = (qualityStocks[0].nowPrice!! + qualityStocks[1].nowPrice!! +
                qualityStocks[2].nowPrice!!) / 3F

        val fiveAveragePriceToday = (qualityStocks[0].nowPrice!! + qualityStocks[1].nowPrice!! +
                qualityStocks[2].nowPrice!! + qualityStocks[3].nowPrice!! + qualityStocks[4].nowPrice!!) / 5F

        val tenAveragePriceToday = (qualityStocks[0].nowPrice!! + qualityStocks[1].nowPrice!! +
                qualityStocks[2].nowPrice!! + qualityStocks[3].nowPrice!! + qualityStocks[4].nowPrice!! +
                qualityStocks[5].nowPrice!! + qualityStocks[6].nowPrice!! + qualityStocks[7].nowPrice!! +
                qualityStocks[8].nowPrice!! + qualityStocks[9].nowPrice!!) / 10F

        val threeAveragePriceYest = (qualityStocks[1].nowPrice!! + qualityStocks[2].nowPrice!! +
                qualityStocks[3].nowPrice!!) / 3F

        val fiveAveragePriceYest = (qualityStocks[1].nowPrice!! + qualityStocks[2].nowPrice!! +
                qualityStocks[3].nowPrice!! + qualityStocks[4].nowPrice!! + qualityStocks[5].nowPrice!!) / 5F

        val tenAveragePriceYest = (qualityStocks[1].nowPrice!! +
                qualityStocks[2].nowPrice!! + qualityStocks[3].nowPrice!! + qualityStocks[4].nowPrice!! +
                qualityStocks[5].nowPrice!! + qualityStocks[6].nowPrice!! + qualityStocks[7].nowPrice!! +
                qualityStocks[8].nowPrice!! + qualityStocks[9].nowPrice!! + qualityStocks[10].nowPrice!!) / 10F

        val threeAveragePriceYestYest = (qualityStocks[2].nowPrice!! +
                qualityStocks[3].nowPrice!! + qualityStocks[4].nowPrice!!) / 3F

        val fiveAveragePriceYestYest = (qualityStocks[2].nowPrice!! +
                qualityStocks[3].nowPrice!! + qualityStocks[4].nowPrice!! + qualityStocks[5].nowPrice!! + qualityStocks[6].nowPrice!!) / 5F

        val tenAveragePriceYestYest =
            (qualityStocks[2].nowPrice!! + qualityStocks[3].nowPrice!! + qualityStocks[4].nowPrice!! +
                    qualityStocks[5].nowPrice!! + qualityStocks[6].nowPrice!! + qualityStocks[7].nowPrice!! +
                    qualityStocks[8].nowPrice!! + qualityStocks[9].nowPrice!! + qualityStocks[10].nowPrice!!
                    + qualityStocks[11].nowPrice!!) / 10F

        val upShadowPoint = if (qualityStocks[0].nowPrice!! > qualityStocks[0].openPrice!!) {
            qualityStocks[0].maxPrice!! - qualityStocks[0].nowPrice!!
        } else {
            qualityStocks[0].maxPrice!! - qualityStocks[0].openPrice!!
        }

        if (threeAveragePriceYestYest < fiveAveragePriceYestYest || threeAveragePriceYestYest < tenAveragePriceYestYest ||
            fiveAveragePriceYestYest < tenAveragePriceYestYest
        )
            if (threeAveragePriceYest > fiveAveragePriceYest && fiveAveragePriceYest > tenAveragePriceYest)
                if (threeAveragePriceToday > fiveAveragePriceToday && fiveAveragePriceToday > tenAveragePriceToday)
                    if (qualityStocks[0].nowPrice!! > threeAveragePriceToday)
                        if (qualityStocks[0].stockRate!! in 2.0..5.0)
                            if (upShadowPoint / qualityStocks[0].nowPrice!! < 0.02)
                                return true

        return false
    }

    private fun judgeTestStock22(
        stockCode: String,
        qualityStocks: ArrayList<StockDayModel>
    ): Boolean {
        if (qualityStocks.size < 15) return false

        val threeAveragePriceToday = (qualityStocks[0].nowPrice!! + qualityStocks[1].nowPrice!! +
                qualityStocks[2].nowPrice!!) / 3F

        val fiveAveragePriceToday = (qualityStocks[0].nowPrice!! + qualityStocks[1].nowPrice!! +
                qualityStocks[2].nowPrice!! + qualityStocks[3].nowPrice!! + qualityStocks[4].nowPrice!!) / 5F

        val tenAveragePriceToday = (qualityStocks[0].nowPrice!! + qualityStocks[1].nowPrice!! +
                qualityStocks[2].nowPrice!! + qualityStocks[3].nowPrice!! + qualityStocks[4].nowPrice!! +
                qualityStocks[5].nowPrice!! + qualityStocks[6].nowPrice!! + qualityStocks[7].nowPrice!! +
                qualityStocks[8].nowPrice!! + qualityStocks[9].nowPrice!!) / 10F

        val threeAveragePriceYest = (qualityStocks[1].nowPrice!! + qualityStocks[2].nowPrice!! +
                qualityStocks[3].nowPrice!!) / 3F

        val fiveAveragePriceYest = (qualityStocks[1].nowPrice!! + qualityStocks[2].nowPrice!! +
                qualityStocks[3].nowPrice!! + qualityStocks[4].nowPrice!! + qualityStocks[5].nowPrice!!) / 5F

        val tenAveragePriceYest = (qualityStocks[1].nowPrice!! +
                qualityStocks[2].nowPrice!! + qualityStocks[3].nowPrice!! + qualityStocks[4].nowPrice!! +
                qualityStocks[5].nowPrice!! + qualityStocks[6].nowPrice!! + qualityStocks[7].nowPrice!! +
                qualityStocks[8].nowPrice!! + qualityStocks[9].nowPrice!! + qualityStocks[10].nowPrice!!) / 10F

        val threeAveragePriceYestYest = (qualityStocks[2].nowPrice!! +
                qualityStocks[3].nowPrice!! + qualityStocks[4].nowPrice!!) / 3F

        val fiveAveragePriceYestYest = (qualityStocks[2].nowPrice!! +
                qualityStocks[3].nowPrice!! + qualityStocks[4].nowPrice!! + qualityStocks[5].nowPrice!! + qualityStocks[6].nowPrice!!) / 5F

        val tenAveragePriceYestYest =
            (qualityStocks[2].nowPrice!! + qualityStocks[3].nowPrice!! + qualityStocks[4].nowPrice!! +
                    qualityStocks[5].nowPrice!! + qualityStocks[6].nowPrice!! + qualityStocks[7].nowPrice!! +
                    qualityStocks[8].nowPrice!! + qualityStocks[9].nowPrice!! + qualityStocks[10].nowPrice!!
                    + qualityStocks[11].nowPrice!!) / 10F

        val upShadowPoint = if (qualityStocks[0].nowPrice!! > qualityStocks[0].openPrice!!) {
            qualityStocks[0].maxPrice!! - qualityStocks[0].nowPrice!!
        } else {
            qualityStocks[0].maxPrice!! - qualityStocks[0].openPrice!!
        }

        if (threeAveragePriceYestYest < fiveAveragePriceYestYest || threeAveragePriceYestYest < tenAveragePriceYestYest ||
            fiveAveragePriceYestYest < tenAveragePriceYestYest
        )
            if (threeAveragePriceYest > fiveAveragePriceYest && fiveAveragePriceYest > tenAveragePriceYest)
                if (threeAveragePriceToday > fiveAveragePriceToday && fiveAveragePriceToday > tenAveragePriceToday)
                    if (qualityStocks[0].nowPrice!! > threeAveragePriceToday)
                        if (qualityStocks[0].stockRate!! in 2.0..5.0)
                            if (upShadowPoint / qualityStocks[0].nowPrice!! < 0.02)
                                if (checkVolumeRate(stockCode, qualityStocks))
                                    return true

        return false
    }

    private fun judgeTestStock23(
        stockCode: String,
        qualityStocks: ArrayList<StockDayModel>
    ): Boolean {
        if (qualityStocks.size < 15) return false

        val threeAveragePriceToday = (qualityStocks[0].nowPrice!! + qualityStocks[1].nowPrice!! +
                qualityStocks[2].nowPrice!!) / 3F

        val fiveAveragePriceToday = (qualityStocks[0].nowPrice!! + qualityStocks[1].nowPrice!! +
                qualityStocks[2].nowPrice!! + qualityStocks[3].nowPrice!! + qualityStocks[4].nowPrice!!) / 5F

        val tenAveragePriceToday = (qualityStocks[0].nowPrice!! + qualityStocks[1].nowPrice!! +
                qualityStocks[2].nowPrice!! + qualityStocks[3].nowPrice!! + qualityStocks[4].nowPrice!! +
                qualityStocks[5].nowPrice!! + qualityStocks[6].nowPrice!! + qualityStocks[7].nowPrice!! +
                qualityStocks[8].nowPrice!! + qualityStocks[9].nowPrice!!) / 10F

        val threeAveragePriceYest = (qualityStocks[1].nowPrice!! + qualityStocks[2].nowPrice!! +
                qualityStocks[3].nowPrice!!) / 3F

        val fiveAveragePriceYest = (qualityStocks[1].nowPrice!! + qualityStocks[2].nowPrice!! +
                qualityStocks[3].nowPrice!! + qualityStocks[4].nowPrice!! + qualityStocks[5].nowPrice!!) / 5F

        val tenAveragePriceYest = (qualityStocks[1].nowPrice!! +
                qualityStocks[2].nowPrice!! + qualityStocks[3].nowPrice!! + qualityStocks[4].nowPrice!! +
                qualityStocks[5].nowPrice!! + qualityStocks[6].nowPrice!! + qualityStocks[7].nowPrice!! +
                qualityStocks[8].nowPrice!! + qualityStocks[9].nowPrice!! + qualityStocks[10].nowPrice!!) / 10F

        val upShadowPoint = if (qualityStocks[0].nowPrice!! > qualityStocks[0].openPrice!!) {
            qualityStocks[0].maxPrice!! - qualityStocks[0].nowPrice!!
        } else {
            qualityStocks[0].maxPrice!! - qualityStocks[0].openPrice!!
        }

        if (threeAveragePriceYest < fiveAveragePriceYest || threeAveragePriceYest < tenAveragePriceYest ||
            fiveAveragePriceYest < tenAveragePriceYest
        )
            if (threeAveragePriceToday > fiveAveragePriceToday && fiveAveragePriceToday > tenAveragePriceToday)
                if (qualityStocks[0].nowPrice!! > threeAveragePriceToday)
                    if (qualityStocks[0].stockRate!! in 2.0..5.0)
                        if (upShadowPoint / qualityStocks[0].nowPrice!! < 0.02)
                            if (checkVolumeRate(stockCode, qualityStocks))
                                return true

        return false
    }

    private fun judgeTestStock24(
        stockCode: String,
        qualityStocks: ArrayList<StockDayModel>
    ): Boolean {

        var decimalFormat2: DecimalFormat = DecimalFormat("#0.00")
        var decimalFormat3: DecimalFormat = DecimalFormat("#0.0000")

        val trss = if (qualityStocks[0].nowPrice!! > qualityStocks[0].openPrice!!){
            qualityStocks[0].maxPrice!! - qualityStocks[0].nowPrice!! > qualityStocks[0].openPrice!! - qualityStocks[0].minPrice!!
        }else
            qualityStocks[0].maxPrice!! - qualityStocks[0].openPrice!! > qualityStocks[0].nowPrice!! - qualityStocks[0].minPrice!!


        val status = qualityStocks[0].stockRate!! < 8 &&
                qualityStocks[0].turnoverRate!! > 3 && qualityStocks[0].turnoverRate!! < 7 &&
                trss &&
                (judgeTestStock12(stockCode, qualityStocks) ||
                judgeTestStock22(stockCode, qualityStocks) ||
                judgeTestStock23(stockCode, qualityStocks) ||
                judgeThreeCannonStock(qualityStocks) ||
                judgeThreeSoldierStock(stockCode, qualityStocks) ||
                judgeCrossStarStock(qualityStocks) ||
                judgeUpHammerStock(stockCode, qualityStocks) ||
                judgeEngulfStock(stockCode, qualityStocks) ||
                judgeFlatBottomsStock(stockCode, qualityStocks) ||
                judgePregnantStock(stockCode, qualityStocks) ||
                judgeFallEndStock(stockCode, qualityStocks) ||
                judgeDoubleNeedleStock(stockCode, qualityStocks) ||
                judgeHighTradingStock(stockCode, qualityStocks) ||
                judgeFourCannonStock(qualityStocks) ||
                judgeThreeCannonPlusStock(qualityStocks) ||
                judgeFiveCannonStock(qualityStocks))

        if (status){
            var speed3AveragePrice = decimalFormat2.format((qualityStocks[2].nowPrice!! + qualityStocks[3].nowPrice!! + qualityStocks[4].nowPrice!!) / 3f).toDouble()
            var speed2AveragePrice = decimalFormat2.format((qualityStocks[1].nowPrice!! + qualityStocks[2].nowPrice!! + qualityStocks[3].nowPrice!!) / 3f).toDouble()
            var speed1AveragePrice = decimalFormat2.format((qualityStocks[0].nowPrice!! + qualityStocks[1].nowPrice!! + qualityStocks[2].nowPrice!!) / 3f).toDouble()
            var speed2Rate = decimalFormat3.format((speed2AveragePrice - speed3AveragePrice) / speed3AveragePrice * 100f).toDouble()
            var speed1Rate = decimalFormat3.format((speed1AveragePrice - speed2AveragePrice) / speed2AveragePrice * 100f).toDouble()

            var canSpeed = decimalFormat3.format(speed1Rate / speed2Rate * speed1Rate /100f).toFloat()

            var buyPrice = speed1AveragePrice * (1f + canSpeed)
        }

        return status
    }

    private fun checkVolumeRate(
        stockCode: String,
        qualityStocks: ArrayList<StockDayModel>
    ): Boolean {
        val volumeRate = (qualityStocks[0].tradeNum!! / 240f).toInt()
            .toFloat() / ((qualityStocks[1].tradeNum!! + qualityStocks[2].tradeNum!! + qualityStocks[3].tradeNum!! + qualityStocks[4].tradeNum!! + qualityStocks[5].tradeNum!!) / 1200f).toInt()
            .toFloat()

        if (qualityStocks[1].stockRate!! + qualityStocks[2].stockRate!! + qualityStocks[3].stockRate!! > 5)
            return false

        return volumeRate > BaseApplication.instance?.filterOptions?.volumeStartRate!! * 0.1f && volumeRate < BaseApplication.instance?.filterOptions?.volumeEndRate!! && checkTurnoverRate(
            qualityStocks[0].turnoverRate!!
        )
    }

    private fun checkTurnoverRate(turnoverRate: Float): Boolean {
        return turnoverRate > BaseApplication.instance?.filterOptions?.turnoverStartRate!! / 10f &&
                turnoverRate < BaseApplication.instance?.filterOptions?.turnoverEndRate!! / 10f
    }

    private fun judgeTestStock25(
        stockCode: String,
        qualityStocks: ArrayList<StockDayModel>
    ): Boolean {
        if (qualityStocks.size < 15) return false

        if (qualityStocks[0].nowPrice!! < qualityStocks [1].nowPrice!!)
            if (qualityStocks[1].nowPrice!! < qualityStocks [2].nowPrice!!)
                if (qualityStocks[2].nowPrice!! < qualityStocks [3].nowPrice!!)
                    if (qualityStocks[3].nowPrice!! < qualityStocks [4].nowPrice!!)
                        if (qualityStocks[4].nowPrice!! < qualityStocks [5].nowPrice!!)
                            if (qualityStocks[5].nowPrice!! < qualityStocks [6].nowPrice!!)
                                if (qualityStocks[6].nowPrice!! < qualityStocks [7].nowPrice!!)
                                    if (checkVolumeRate(stockCode, qualityStocks))
                                    return true

        return false
    }

    private fun judgeTestStock26(
        stockCode: String,
        qualityStocks: ArrayList<StockDayModel>
    ): Boolean {
        if (qualityStocks.size < 15) return false

        val threeAverage = (qualityStocks[0].nowPrice!! + qualityStocks[1].nowPrice!! + qualityStocks[2].nowPrice!!) / 3

        val trss1 = if (qualityStocks[1].nowPrice!! > qualityStocks[1].openPrice!!){
            (qualityStocks[1].maxPrice!! - qualityStocks[1].nowPrice!!) * 2 < qualityStocks[1].openPrice!! - qualityStocks[0].minPrice!!
        }else
            (qualityStocks[1].maxPrice!! - qualityStocks[1].openPrice!!) * 2 < qualityStocks[1].nowPrice!! - qualityStocks[0].minPrice!!

        val trss = if (qualityStocks[0].nowPrice!! > qualityStocks[0].openPrice!!){
            qualityStocks[0].maxPrice!! - qualityStocks[0].nowPrice!! > qualityStocks[0].openPrice!! - qualityStocks[0].minPrice!!
        }else
            qualityStocks[0].maxPrice!! - qualityStocks[0].openPrice!! > qualityStocks[0].nowPrice!! - qualityStocks[0].minPrice!!

        val volumeRate = (qualityStocks[0].tradeNum!! / 240f).toInt()
            .toFloat() / ((qualityStocks[1].tradeNum!! + qualityStocks[2].tradeNum!! + qualityStocks[3].tradeNum!! + qualityStocks[4].tradeNum!! + qualityStocks[5].tradeNum!!) / 1200f).toInt()
            .toFloat()

//        if (qualityStocks[0].nowPrice!! < threeAverage)
//            if (qualityStocks[0].tradeNum!! < qualityStocks[1].tradeNum!!)
//                if (qualityStocks[0].turnoverRate!! < 1)
//                    if (trss && trss1)
                        if (check3510OneConfirm(stockCode, qualityStocks.apply { removeAt(0) }))
//                            if (volumeRate > 0f && volumeRate < 1f)
                            return true
        return false
    }

    private fun check3510OneConfirm(
        stockCode: String,
        qualityStocks: ArrayList<StockDayModel>
    ): Boolean {
        if (qualityStocks.size < 15) return false

        val threeAveragePriceToday = (qualityStocks[0].nowPrice!! + qualityStocks[1].nowPrice!! +
                qualityStocks[2].nowPrice!!) / 3F

        val fiveAveragePriceToday = (qualityStocks[0].nowPrice!! + qualityStocks[1].nowPrice!! +
                qualityStocks[2].nowPrice!! + qualityStocks[3].nowPrice!! + qualityStocks[4].nowPrice!!) / 5F

        val tenAveragePriceToday = (qualityStocks[0].nowPrice!! + qualityStocks[1].nowPrice!! +
                qualityStocks[2].nowPrice!! + qualityStocks[3].nowPrice!! + qualityStocks[4].nowPrice!! +
                qualityStocks[5].nowPrice!! + qualityStocks[6].nowPrice!! + qualityStocks[7].nowPrice!! +
                qualityStocks[8].nowPrice!! + qualityStocks[9].nowPrice!!) / 10F

        val threeAveragePriceYest = (qualityStocks[1].nowPrice!! + qualityStocks[2].nowPrice!! +
                qualityStocks[3].nowPrice!!) / 3F

        val fiveAveragePriceYest = (qualityStocks[1].nowPrice!! + qualityStocks[2].nowPrice!! +
                qualityStocks[3].nowPrice!! + qualityStocks[4].nowPrice!! + qualityStocks[5].nowPrice!!) / 5F

        val tenAveragePriceYest = (qualityStocks[1].nowPrice!! +
                qualityStocks[2].nowPrice!! + qualityStocks[3].nowPrice!! + qualityStocks[4].nowPrice!! +
                qualityStocks[5].nowPrice!! + qualityStocks[6].nowPrice!! + qualityStocks[7].nowPrice!! +
                qualityStocks[8].nowPrice!! + qualityStocks[9].nowPrice!! + qualityStocks[10].nowPrice!!) / 10F

        if (threeAveragePriceYest < fiveAveragePriceYest || threeAveragePriceYest < tenAveragePriceYest ||
            fiveAveragePriceYest < tenAveragePriceYest
        )
            if (threeAveragePriceToday > fiveAveragePriceToday && fiveAveragePriceToday > tenAveragePriceToday)
                if (qualityStocks[0].nowPrice!! > threeAveragePriceToday)
                    if (qualityStocks[0].stockRate!! in 2.0..5.0)
                                return true

        return false
    }
}