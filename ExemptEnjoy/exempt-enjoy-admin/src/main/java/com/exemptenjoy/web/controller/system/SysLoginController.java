package com.exemptenjoy.web.controller.system;

import java.util.Date;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.exemptenjoy.common.constant.Constants;
import com.exemptenjoy.common.core.domain.AjaxResult;
import com.exemptenjoy.common.core.domain.entity.SysMenu;
import com.exemptenjoy.common.core.domain.entity.SysUser;
import com.exemptenjoy.common.core.domain.model.LoginBody;
import com.exemptenjoy.common.core.domain.model.LoginUser;
import com.exemptenjoy.common.core.text.Convert;
import com.exemptenjoy.common.utils.DateUtils;
import com.exemptenjoy.common.utils.SecurityUtils;
import com.exemptenjoy.common.utils.StringUtils;
import com.exemptenjoy.framework.web.service.SmsService;
import com.exemptenjoy.framework.web.service.SysLoginService;
import com.exemptenjoy.framework.web.service.SysPermissionService;
import com.exemptenjoy.framework.web.service.TokenService;
import com.exemptenjoy.system.service.ISysConfigService;
import com.exemptenjoy.system.service.ISysMenuService;

@RestController
public class SysLoginController
{
    @Autowired
    private SysLoginService loginService;

    @Autowired
    private ISysMenuService menuService;

    @Autowired
    private SysPermissionService permissionService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private SmsService smsService;

    @PostMapping("/login")
    public AjaxResult login(@RequestBody LoginBody loginBody)
    {
        AjaxResult ajax = AjaxResult.success();
        if ("sms".equals(loginBody.getLoginType()))
        {
            String token = loginService.smsLogin(loginBody.getPhonenumber(), loginBody.getSmsCode());
            ajax.put(Constants.TOKEN, token);
            return ajax;
        }
        if ("face".equals(loginBody.getLoginType()))
        {
            String token = loginService.faceLogin(loginBody.getFaceImageBase64());
            ajax.put(Constants.TOKEN, token);
            return ajax;
        }
        String token = loginService.login(loginBody.getUsername(), loginBody.getPassword(), loginBody.getCode(),
                loginBody.getUuid());
        ajax.put(Constants.TOKEN, token);
        return ajax;
    }

    @PostMapping("/sms/send")
    public AjaxResult sendSms(@RequestBody java.util.Map<String, String> params)
    {
        String phonenumber = params.get("phonenumber");
        smsService.sendSmsCode(phonenumber);
        return AjaxResult.success("验证码发送成功");
    }

    @GetMapping("getInfo")
    public AjaxResult getInfo()
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        SysUser user = loginUser.getUser();
        Set<String> roles = permissionService.getRolePermission(user);
        Set<String> permissions = permissionService.getMenuPermission(user);
        if (loginUser.getPermissions() == null || !loginUser.getPermissions().equals(permissions))
        {
            loginUser.setPermissions(permissions);
            tokenService.refreshToken(loginUser);
        }
        AjaxResult ajax = AjaxResult.success();
        ajax.put("user", user);
        ajax.put("roles", roles);
        ajax.put("permissions", permissions);
        ajax.put("pwdChrtype", getSysAccountChrtype());
        ajax.put("isDefaultModifyPwd", initPasswordIsModify(user.getPwdUpdateDate()));
        ajax.put("isPasswordExpired", passwordIsExpiration(user.getPwdUpdateDate()));
        return ajax;
    }

    @GetMapping("getRouters")
    public AjaxResult getRouters()
    {
        Long userId = SecurityUtils.getUserId();
        List<SysMenu> menus = menuService.selectMenuTreeByUserId(userId);
        return AjaxResult.success(menuService.buildMenus(menus));
    }

    public String getSysAccountChrtype()
    {
        return Convert.toStr(configService.selectConfigByKey("sys.account.chrtype"), "0");
    }

    public boolean initPasswordIsModify(Date pwdUpdateDate)
    {
        Integer initPasswordModify = Convert.toInt(configService.selectConfigByKey("sys.account.initPasswordModify"));
        return initPasswordModify != null && initPasswordModify == 1 && pwdUpdateDate == null;
    }

    public boolean passwordIsExpiration(Date pwdUpdateDate)
    {
        Integer passwordValidateDays = Convert.toInt(configService.selectConfigByKey("sys.account.passwordValidateDays"));
        if (passwordValidateDays != null && passwordValidateDays > 0)
        {
            if (StringUtils.isNull(pwdUpdateDate))
            {
                return true;
            }
            Date nowDate = DateUtils.getNowDate();
            return DateUtils.differentDaysByMillisecond(nowDate, pwdUpdateDate) > passwordValidateDays;
        }
        return false;
    }
}
