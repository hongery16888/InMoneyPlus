package com.magic.inmoney.utilities;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonUtils {

    public static boolean isNumeric(String str){
        Pattern pattern = Pattern.compile("^-?([1-9]\\d*\\.\\d*|0\\.\\d*[1-9]\\d*|0?\\.0+|0)$");
        Matcher isNum = pattern.matcher(str);
        if( !isNum.matches() ){
            return false;
        }
        return true;
    }
}
