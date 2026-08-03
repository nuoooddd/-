package com.exemptenjoy.common.core.domain.model;

public class RegisterBody
{
    private String username;
    private String password;
    private String nickName;
    private String phonenumber;
    private String userType;
    private String smsCode;
    private String idCard;
    private String realName;
    private String faceImageBase64;
    private String idCardFront;
    private String idCardBack;

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

    public String getNickName()
    {
        return nickName;
    }

    public void setNickName(String nickName)
    {
        this.nickName = nickName;
    }

    public String getPhonenumber()
    {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber)
    {
        this.phonenumber = phonenumber;
    }

    public String getUserType()
    {
        return userType;
    }

    public void setUserType(String userType)
    {
        this.userType = userType;
    }

    public String getSmsCode()
    {
        return smsCode;
    }

    public void setSmsCode(String smsCode)
    {
        this.smsCode = smsCode;
    }

    public String getIdCard()
    {
        return idCard;
    }

    public void setIdCard(String idCard)
    {
        this.idCard = idCard;
    }

    public String getRealName()
    {
        return realName;
    }

    public void setRealName(String realName)
    {
        this.realName = realName;
    }

    public String getFaceImageBase64()
    {
        return faceImageBase64;
    }

    public void setFaceImageBase64(String faceImageBase64)
    {
        this.faceImageBase64 = faceImageBase64;
    }

    public String getIdCardFront()
    {
        return idCardFront;
    }

    public void setIdCardFront(String idCardFront)
    {
        this.idCardFront = idCardFront;
    }

    public String getIdCardBack()
    {
        return idCardBack;
    }

    public void setIdCardBack(String idCardBack)
    {
        this.idCardBack = idCardBack;
    }
}
