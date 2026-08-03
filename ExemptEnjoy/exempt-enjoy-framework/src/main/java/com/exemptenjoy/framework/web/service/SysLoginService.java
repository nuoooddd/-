package com.exemptenjoy.framework.web.service;

import jakarta.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import com.exemptenjoy.common.constant.CacheConstants;
import com.exemptenjoy.common.constant.Constants;
import com.exemptenjoy.common.constant.UserConstants;
import com.exemptenjoy.common.core.domain.entity.SysUser;
import com.exemptenjoy.common.core.domain.model.LoginUser;
import com.exemptenjoy.common.core.redis.RedisCache;
import com.exemptenjoy.common.exception.ServiceException;
import com.exemptenjoy.common.exception.user.BlackListException;
import com.exemptenjoy.common.exception.user.CaptchaException;
import com.exemptenjoy.common.exception.user.CaptchaExpireException;
import com.exemptenjoy.common.exception.user.UserNotExistsException;
import com.exemptenjoy.common.exception.user.UserPasswordNotMatchException;
import com.exemptenjoy.common.utils.DateUtils;
import com.exemptenjoy.common.utils.MessageUtils;
import com.exemptenjoy.common.utils.StringUtils;
import com.exemptenjoy.common.utils.ip.IpUtils;
import com.exemptenjoy.framework.manager.AsyncManager;
import com.exemptenjoy.framework.manager.factory.AsyncFactory;
import com.exemptenjoy.framework.security.context.AuthenticationContextHolder;
import com.exemptenjoy.system.service.ISysConfigService;
import com.exemptenjoy.system.service.ISysUserService;

@Component
public class SysLoginService
{
    @Autowired
    private TokenService tokenService;

    @Resource
    private AuthenticationManager authenticationManager;

    @Autowired
    private RedisCache redisCache;
    
    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private SmsService smsService;

    @Autowired
    private BaiduFaceService baiduFaceService;

    public String login(String username, String password, String code, String uuid)
    {
        loginPreCheck(username, password);
        Authentication authentication = null;
        try
        {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
            AuthenticationContextHolder.setContext(authenticationToken);
            authentication = authenticationManager.authenticate(authenticationToken);
        }
        catch (Exception e)
        {
            if (e instanceof BadCredentialsException)
            {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.password.not.match")));
                throw new UserPasswordNotMatchException();
            }
            else
            {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, e.getMessage()));
                throw new ServiceException(e.getMessage());
            }
        }
        finally
        {
            AuthenticationContextHolder.clearContext();
        }
        AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_SUCCESS, MessageUtils.message("user.login.success")));
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        recordLoginInfo(loginUser.getUserId());
        return tokenService.createToken(loginUser);
    }

    public String smsLogin(String phonenumber, String smsCode)
    {
        if (StringUtils.isEmpty(phonenumber) || StringUtils.isEmpty(smsCode))
        {
            throw new ServiceException("手机号和验证码不能为空");
        }
        smsService.validateSmsCode(phonenumber, smsCode);
        SysUser user = userService.selectUserByPhonenumber(phonenumber);
        if (user == null)
        {
            throw new ServiceException("该手机号未注册");
        }
        if ("1".equals(user.getStatus()))
        {
            throw new ServiceException("该账号已被停用");
        }
        LoginUser loginUser = new LoginUser(user.getUserId(), user.getDeptId(), user, new HashSet<>());
        AsyncManager.me().execute(AsyncFactory.recordLogininfor(user.getUserName(), Constants.LOGIN_SUCCESS, "手机验证码登录成功"));
        recordLoginInfo(user.getUserId());
        return tokenService.createToken(loginUser);
    }

    public String faceLogin(String faceImageBase64)
    {
        if (StringUtils.isEmpty(faceImageBase64))
        {
            throw new ServiceException("人脸图片不能为空");
        }
        com.alibaba.fastjson2.JSONObject faceResult = baiduFaceService.faceSearch(faceImageBase64);
        if (!faceResult.getBooleanValue("result"))
        {
            if (faceResult.getBooleanValue("needRegister"))
            {
                List<SysUser> verifiedUsers = userService.selectUserList(new SysUser());
                for (SysUser u : verifiedUsers)
                {
                    if ("1".equals(u.getFaceVerified()) && "0".equals(u.getStatus()))
                    {
                        try
                        {
                            baiduFaceService.faceRegister(faceImageBase64, String.valueOf(u.getUserId()), u.getUserName());
                        }
                        catch (Exception ignored) {}
                    }
                }
            }
            throw new ServiceException(faceResult.getString("msg") != null ? faceResult.getString("msg") : "人脸识别未通过，请重试");
        }
        String matchedUserId = faceResult.getString("userId");
        if (StringUtils.isEmpty(matchedUserId))
        {
            throw new ServiceException("未识别到已注册用户，请先完成实名认证");
        }
        SysUser user = userService.selectUserById(Long.parseLong(matchedUserId));
        if (user == null)
        {
            throw new ServiceException("用户不存在");
        }
        if ("1".equals(user.getStatus()))
        {
            throw new ServiceException("该账号已被停用");
        }
        LoginUser loginUser = new LoginUser(user.getUserId(), user.getDeptId(), user, new HashSet<>());
        AsyncManager.me().execute(AsyncFactory.recordLogininfor(user.getUserName(), Constants.LOGIN_SUCCESS, "人脸识别登录成功"));
        recordLoginInfo(user.getUserId());
        return tokenService.createToken(loginUser);
    }

    public void validateCaptcha(String username, String code, String uuid)
    {
        boolean captchaEnabled = configService.selectCaptchaEnabled();
        if (captchaEnabled)
        {
            String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + StringUtils.nvl(uuid, "");
            String captcha = redisCache.getCacheObject(verifyKey);
            if (captcha == null)
            {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.expire")));
                throw new CaptchaExpireException();
            }
            redisCache.deleteObject(verifyKey);
            if (!code.equalsIgnoreCase(captcha))
            {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.error")));
                throw new CaptchaException();
            }
        }
    }

    public void loginPreCheck(String username, String password)
    {
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password))
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("not.null")));
            throw new UserNotExistsException();
        }
        if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH)
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.password.not.match")));
            throw new UserPasswordNotMatchException();
        }
        if (username.length() < UserConstants.USERNAME_MIN_LENGTH
                || username.length() > UserConstants.USERNAME_MAX_LENGTH)
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.password.not.match")));
            throw new UserPasswordNotMatchException();
        }
        String blackStr = configService.selectConfigByKey("sys.login.blackIPList");
        if (IpUtils.isMatchedIp(blackStr, IpUtils.getIpAddr()))
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("login.blocked")));
            throw new BlackListException();
        }
    }

    public void recordLoginInfo(Long userId)
    {
        userService.updateLoginInfo(userId, IpUtils.getIpAddr(), DateUtils.getNowDate());
    }
}
