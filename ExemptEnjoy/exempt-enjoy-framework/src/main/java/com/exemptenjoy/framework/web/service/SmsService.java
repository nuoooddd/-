package com.exemptenjoy.framework.web.service;

import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.exemptenjoy.common.constant.CacheConstants;
import com.exemptenjoy.common.core.redis.RedisCache;
import com.exemptenjoy.common.exception.ServiceException;
import com.exemptenjoy.common.utils.StringUtils;

@Component
public class SmsService
{
    @Autowired
    private RedisCache redisCache;

    private static final int SMS_CODE_LENGTH = 6;
    private static final int SMS_CODE_EXPIRE_MINUTES = 5;
    private static final int SMS_INTERVAL_SECONDS = 60;
    private static final int SMS_DAILY_LIMIT = 10;

    public String sendSmsCode(String phonenumber)
    {
        if (StringUtils.isEmpty(phonenumber) || !phonenumber.matches("^1[3-9]\\d{9}$"))
        {
            throw new ServiceException("手机号格式不正确");
        }
        String limitKey = CacheConstants.SMS_LIMIT_KEY + phonenumber;
        Integer count = redisCache.getCacheObject(limitKey);
        if (count != null && count >= SMS_DAILY_LIMIT)
        {
            throw new ServiceException("今日发送次数已达上限");
        }
        String intervalKey = CacheConstants.SMS_LIMIT_KEY + "interval:" + phonenumber;
        if (redisCache.getCacheObject(intervalKey) != null)
        {
            throw new ServiceException("发送过于频繁，请稍后再试");
        }
        String code = generateCode();
        String codeKey = CacheConstants.SMS_CODE_KEY + phonenumber;
        redisCache.setCacheObject(codeKey, code, SMS_CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        if (count == null)
        {
            redisCache.setCacheObject(limitKey, 1, 1, TimeUnit.DAYS);
        }
        else
        {
            redisCache.setCacheObject(limitKey, count + 1, 1, TimeUnit.DAYS);
        }
        redisCache.setCacheObject(intervalKey, "1", SMS_INTERVAL_SECONDS, TimeUnit.SECONDS);
        System.out.println("【短信验证码】手机号:" + phonenumber + ", 验证码:" + code);
        return code;
    }

    public boolean validateSmsCode(String phonenumber, String smsCode)
    {
        if (StringUtils.isEmpty(phonenumber) || StringUtils.isEmpty(smsCode))
        {
            return false;
        }
        String codeKey = CacheConstants.SMS_CODE_KEY + phonenumber;
        String cachedCode = redisCache.getCacheObject(codeKey);
        if (cachedCode == null)
        {
            throw new ServiceException("验证码已过期，请重新获取");
        }
        if (!cachedCode.equals(smsCode))
        {
            throw new ServiceException("验证码错误");
        }
        redisCache.deleteObject(codeKey);
        return true;
    }

    private String generateCode()
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < SMS_CODE_LENGTH; i++)
        {
            sb.append((int) (Math.random() * 10));
        }
        return sb.toString();
    }
}