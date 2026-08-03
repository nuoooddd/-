package com.exemptenjoy.common.core.domain.model;

public class LoginBody
{
    private String username;
    private String password;
    private String code;
    private String uuid;
    private String loginType;
    private String phonenumber;
    private String smsCode;
    private String faceImageBase64;

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public String getUuid()
    {
        return uuid;
    }

    public void setUuid(String uuid)
    {
        this.uuid = uuid;
    }

    public String getLoginType()
    {
        return loginType;
    }

    public void setLoginType(String loginType)
    {
        this.loginType = loginType;
    }

    public String getPhonenumber()
    {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber)
    {
        this.phonenumber = phonenumber;
    }

    public String getSmsCode()
    {
        return smsCode;
    }

    public void setSmsCode(String smsCode)
    {
        this.smsCode = smsCode;
    }

    public String getFaceImageBase64()
    {
        return faceImageBase64;
    }

    public void setFaceImageBase64(String faceImageBase64)
    {
        this.faceImageBase64 = faceImageBase64;
    }
}
