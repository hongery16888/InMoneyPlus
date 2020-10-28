package com.magic.inmoney.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class BlockRankModel {

    @SerializedName("message")
    private String message;
    @SerializedName("data")
    private DataBean data;
    @SerializedName("code")
    private int code;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public static class DataBean {
        /**
         * limit : 2
         * candle : [{"high_px":787.83,"member_count":13,"open_px":763.97,"px_change":20.03,"px_change_rate":2.63,"rise_count":9,"last_px":782.02,"preclose_px":761.99,"fall_first_grp":"300123.SZ","rise_first_grp":"300589.SZ","fall_count":3,"update_time":1601017199,"block_code":"BK0729","block_name":"船舶制造","low_px":759.94},{"high_px":152995.44,"member_count":46,"open_px":149741.9,"px_change":2836.24,"px_change_rate":1.9,"rise_count":41,"last_px":151780.37,"preclose_px":148944.13,"fall_first_grp":"000728.SZ","rise_first_grp":"600958.SS","fall_count":2,"update_time":1601017199,"block_code":"BK0473","block_name":"券商信托","low_px":148760.62}]
         * count : 61
         * page : 1
         * type : 2
         */

        @SerializedName("limit")
        private int limit;
        @SerializedName("count")
        private int count;
        @SerializedName("page")
        private int page;
        @SerializedName("type")
        private int type;
        @SerializedName("candle")
        private ArrayList<BlockModel> candle;

        public int getLimit() {
            return limit;
        }

        public void setLimit(int limit) {
            this.limit = limit;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public ArrayList<BlockModel> getCandle() {
            return candle;
        }

        public void setCandle(ArrayList<BlockModel> candle) {
            this.candle = candle;
        }

    }
}
