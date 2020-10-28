package com.magic.inmoney.model;

import java.util.ArrayList;
import java.util.List;

public class DaysStockInfoModel {

    /**
     * status : 0
     * hq : [["2020-08-21","40.15","40.19","-0.35","-0.86%","39.68","40.74","18256","7342.33","4.48%"],["2020-08-20","40.01","40.54","0.34","0.85%","39.22","40.59","22123","8872.19","5.43%"],["2020-08-19","42.00","40.20","-1.75","-4.17%","40.16","42.18","30159","12314.60","7.40%"],["2020-08-18","42.91","41.95","-0.86","-2.01%","41.50","42.91","27709","11640.41","6.80%"]]
     * code : cn_603722
     * stat : ["累计:","2020-08-18至2020-08-21","-2.62","-6.12%",39.22,42.91,98247,40169.53,"24.11%"]
     */

    private int status;
    private String code;
    private ArrayList<ArrayList<String>> hq;
    private ArrayList<String> stat;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public ArrayList<ArrayList<String>> getHq() {
        return hq;
    }

    public void setHq(ArrayList<ArrayList<String>> hq) {
        this.hq = hq;
    }

    public ArrayList<String> getStat() {
        return stat;
    }

    public void setStat(ArrayList<String> stat) {
        this.stat = stat;
    }
}
