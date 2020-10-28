package com.magic.inmoney.const

import com.magic.inmoney.model.DaysStockInfoModel
import com.magic.inmoney.model.StockItemModel

enum class QualityType(val typeId: Int, val day: Long, val kLineType:String) {
    ThreeCannon(0, 20, "两阳夹一阴"),
    FourCannon(1, 20, "两阳夹两阴"),
    FiveCannon(2, 20, "两阳夹三阴"),
    ThreeSoldier(3, 20, "红三兵"),
    FriendCounterattack(4, 20, "好友反攻"),
    DawnFlush(5, 20, "早晨之星"),
    ThreeCannonPlus(6, 20, "两阳夹一阴升级版"),
    CrossStar(7, 20, "启明之星"),
    UpHammer(8, 20, "锤子线"),
    Engulf(9, 20, "看涨吞没"),
    FlatBottom(10, 20, "平头底部"),
    Pregnant(11, 20, "低位孕线"),
    PregnantPlus(12, 20, "低位孕线升级版"),
    FallEnd(13, 20, "下跌尽头线"),
    DoubleNeedle(14, 20, "双针探底"),
    HighTrade(15, 20, "测试"),
    Debug(16, 60, "测试"),
    Resonate(17, 100, "三线共振")
//    RisingSun(6, 7)
}

enum class StockType(val prefix: String) {
    SHA("600|601|603|605"),
    SHB("900"),
    CYB("300"),
    KCB("688"),
    SZA("000"),
    ZXB("002"),
    SZB("200")
}

enum class LineType() {
    HighShadowLine,
    MidEntity,
    LowShadowLine
}

enum class StockSortType(val info: String) {
    StockCode("stockCode"),
    StockName("stockName"),
    StockRate("stockRate"),
    TradeNum("tradeNum"),
    TurnoverRate("turnoverRate"),
}

enum class StockSortDirection(val info: String) {
    Desc("desc"),
    Asc("asc")
}

enum class KeyStockPrompt(val info: String) {
    Voice("voice"),
    Music("music"),
    None("none")
}

enum class WeekDay {
    Saturday,
    Sunday,
    Workday
}

enum class StockItemType(val itemType: String) {
    AllStockItem("allStock"),
    HighQualityItem("highQuality"),
    FavoriteItem("favorite"),
    KeyStockItem("keyStock")
}

enum class StockBuyStatus(val buyStatus: String) {
    Purchased("已购买"),
    WaitTargetPrice("等待中"),
    ProOrder("挂单中"),
    PromptBuy("接近中"),
    Sold("已卖出"),
    ReachTargetPrice("已达标"),
    BuyPrice("直接买")
}

enum class StatisticsType(val line: Int, val count: Int, val title: String, val type: String) {
    StatisticsUpLine(2,3000, "回调上面均线", "upLine"),
    StatisticsMidLine(1,3000, "回调中间均线", "midLine"),
    StatisticsDownLine(0,3000, "回调下面均线", "downLine"),
    StatisticsThreeUpLine(2,200, "穿三线上均线", "threeUpLine"),
    StatisticsThreeMidLine(1,200, "穿三线中均线", "threeMidLine"),
    StatisticsThreeDownLine(0,200, "穿三线下均线", "threeDownLine"),
    StatisticsThreeSortUpLine(2,3000, "穿顺序三线上均线", "threeSortUpLine"),
    StatisticsThreeSortMidLine(1,3000, "穿顺序三线中均线", "threeSortMidLine"),
    StatisticsThreeSortDownLine(0,3000, "穿顺序三线下均线", "threeSortDownLine"),
    StatisticsDoubleDayUpLine(2, 3000, "带量高换手穿三上均线", "doubleDayUpLine"),
    StatisticsHighQualityUpLine(2, 3000, "5102030多头二次确认", "allHighQualityUpLine"),
    Statistics5813UpLine(2, 400, "5813三天确认", "5813UpLine"),
    Statistics3510UpLine(2, 400, "3510两次确认反转", "3510UpLine"),
    StatisticsRareMidLine(2, 400, "3510一次确认反转", "rareMidLine"),
    StatisticsMACD(2, 10000, "MACD指标确认", "macd")
}

enum class BuyPointPrompt(val point: String, val tag: String) {
    UpLine("up", "上均线"),
    MidLine("mid", "中均线"),
    DownLine("down", "下均线"),
    AnyLine("any", "任意线")
}

enum class ThroughType(val type: String, val tag: String) {
    NormalThrough("normalThrough", "普通穿透"),
    HighThrough("highThrough", "高品质穿透"),
    ThroughAndTrade("throughAndTrade", "带量穿透"),
    HighThroughAndTrade("highThroughAndTrade", "带量高品质穿透")
}