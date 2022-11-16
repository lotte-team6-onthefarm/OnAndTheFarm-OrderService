package com.team6.onandthefarmorderservice.utils;

import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

@Component
public class DateUtils {

    public String transDate(String pattern){
        TimeZone time;
        time = TimeZone.getTimeZone("Asia/Seoul");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, Locale.KOREA);
        simpleDateFormat.setTimeZone(time);
        String date = simpleDateFormat.format(new Date());
        return date;
    }

    public String nextDate(String date){
        String[] dateFragment = date.split("\\.");
        int year = Integer.valueOf(dateFragment[0]);
        int month = Integer.valueOf(dateFragment[1]);
        int day = Integer.valueOf(dateFragment[2]);

        day++; // 1일 증가

        if(month==1||month==3||month==5||month==7||month==8||month==10||month==12){
            if(day==32){
                day=1;
                month++;
                if(month==13){
                    month=1;
                    year++;
                }
            }
        }
        else if(month==4||month==6||month==9||month==11){
            if(day==31){
                day=1;
                month++;
                if(month==13){
                    month=1;
                    year++;
                }
            }
        }
        else{
            if(day==29){
                day=1;
                month++;
                if(month==13){
                    month=1;
                    year++;
                }
            }
        }
        String strMonth = String.valueOf(month);
        String strDay = String.valueOf(day);

        if(strMonth.length()==1){
            strMonth = "0"+strMonth;
        }
        if(strDay.length()==1){
            strDay = "0"+strDay;
        }
        return String.valueOf(year)+"."+strMonth+"."+strDay;
    }
}
