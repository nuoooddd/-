package com.exemptenjoy.common.utils;

import java.util.regex.Pattern;

public class IdCardUtils
{
    private static final int[] WEIGHT = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
    private static final char[] CHECK_CODE = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("^\\d{17}[\\dXx]$");

    public static boolean isValid(String idCard)
    {
        if (StringUtils.isEmpty(idCard) || !ID_CARD_PATTERN.matcher(idCard).matches())
        {
            return false;
        }
        String provinceCode = idCard.substring(0, 2);
        int province = Integer.parseInt(provinceCode);
        if (province < 11 || province > 91)
        {
            return false;
        }
        String dateStr = idCard.substring(6, 14);
        try
        {
            int year = Integer.parseInt(dateStr.substring(0, 4));
            int month = Integer.parseInt(dateStr.substring(4, 6));
            int day = Integer.parseInt(dateStr.substring(6, 8));
            if (year < 1900 || year > 2100 || month < 1 || month > 12 || day < 1 || day > 31)
            {
                return false;
            }
        }
        catch (NumberFormatException e)
        {
            return false;
        }
        int sum = 0;
        for (int i = 0; i < 17; i++)
        {
            sum += (idCard.charAt(i) - '0') * WEIGHT[i];
        }
        char expectedCheck = CHECK_CODE[sum % 11];
        char actualCheck = Character.toUpperCase(idCard.charAt(17));
        return expectedCheck == actualCheck;
    }
}