package com.magic.inmoney.model

import com.google.gson.annotations.SerializedName
import org.litepal.crud.LitePalSupport

class StockModel: LitePalSupport() {
    /**
     * stockType : B
     * market : sh
     * name : 神奇B股
     * state : 1
     * currcapital : 5475.165000
     * profit_four : 0.144277484
     * code : 900904
     * totalcapital : 53407.162800
     * mgjzc : 4.901040
     * pinyin : sqbg
     * listing_date : 1992-07-22
     * ct : 2016-10-16 15:40:05.647
     */
    @SerializedName("stockType")
    var stockType: String? = null

    @SerializedName("market")
    var market: String? = null

    @SerializedName("name")
    var name: String? = null

    @SerializedName("state")
    var state = 0

    @SerializedName("currcapital")
    var currcapital: String? = null

    @SerializedName("profit_four")
    var profitFour: String? = null

    @SerializedName("code")
    var code: String? = null

    @SerializedName("totalcapital")
    var totalcapital: String? = null

    @SerializedName("mgjzc")
    var mgjzc: String? = null

    @SerializedName("pinyin")
    var pinyin: String? = null

    @SerializedName("listing_date")
    var listingDate: String? = null

    @SerializedName("ct")
    var ct: String? = null

}