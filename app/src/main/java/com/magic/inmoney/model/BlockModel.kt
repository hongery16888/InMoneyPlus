package com.magic.inmoney.model

import com.google.gson.annotations.SerializedName
import org.litepal.crud.LitePalSupport

class BlockModel  : LitePalSupport() {
    /**
     * high_px : 787.83
     * member_count : 13
     * open_px : 763.97
     * px_change : 20.03
     * px_change_rate : 2.63
     * rise_count : 9
     * last_px : 782.02
     * preclose_px : 761.99
     * fall_first_grp : 300123.SZ
     * rise_first_grp : 300589.SZ
     * fall_count : 3
     * update_time : 1601017199
     * block_code : BK0729
     * block_name : 船舶制造
     * low_px : 759.94
     */
    @SerializedName("high_px")
    var highPx = 0.0

    @SerializedName("member_count")
    var memberCount = 0

    @SerializedName("open_px")
    var openPx = 0.0

    @SerializedName("px_change")
    var pxChange = 0.0

    @SerializedName("px_change_rate")
    var pxChangeRate = 0.0

    @SerializedName("rise_count")
    var riseCount = 0

    @SerializedName("last_px")
    var lastPx = 0.0

    @SerializedName("preclose_px")
    var preclosePx = 0.0

    @SerializedName("fall_first_grp")
    var fallFirstGrp: String? = null

    @SerializedName("rise_first_grp")
    var riseFirstGrp: String? = null

    @SerializedName("fall_count")
    var fallCount = 0

    @SerializedName("update_time")
    var updateTime = 0

    @SerializedName("block_code")
    var blockCode: String? = null

    @SerializedName("block_name")
    var blockName: String? = null

    @SerializedName("low_px")
    var lowPx = 0.0

}