package com.exemptenjoy.framework.web.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.exemptenjoy.common.constant.UserConstants;
import com.exemptenjoy.common.core.domain.entity.SysRole;
import com.exemptenjoy.common.core.domain.entity.SysUser;
import com.exemptenjoy.common.core.domain.model.RegisterBody;
import com.exemptenjoy.common.exception.ServiceException;
import com.exemptenjoy.common.utils.AesEncryptUtils;
import com.exemptenjoy.common.utils.IdCardUtils;
import com.exemptenjoy.common.utils.SecurityUtils;
import com.exemptenjoy.common.utils.StringUtils;
import com.exemptenjoy.common.utils.ip.IpUtils;
import com.exemptenjoy.system.domain.EeAuditRecord;
import com.exemptenjoy.system.domain.EeVerifyRecord;
import com.exemptenjoy.system.mapper.EeAuditRecordMapper;
import com.exemptenjoy.system.mapper.EeVerifyRecordMapper;
import com.exemptenjoy.system.mapper.SysRoleMapper;
import com.exemptenjoy.system.mapper.SysUserRoleMapper;
import com.exemptenjoy.system.domain.SysUserRole;
import com.exemptenjoy.system.service.ISysUserService;

@Component
public class SysRegisterService
{
    @Autowired
    private ISysUserService userService;

    @Autowired
    private SysRoleMapper roleMapper;

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @Autowired
    private SmsService smsService;

    @Autowired
    private BaiduFaceService baiduFaceService;

    @Autowired
    private EeVerifyRecordMapper verifyRecordMapper;

    @Autowired
    private EeAuditRecordMapper auditRecordMapper;

    public String register(RegisterBody registerBody)
    {
        String username = registerBody.getUsername();
        String password = registerBody.getPassword();

        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password))
        {
            throw new ServiceException("用户名和密码不能为空");
        }
        if (username.length() < UserConstants.USERNAME_MIN_LENGTH
                || username.length() > UserConstants.USERNAME_MAX_LENGTH)
        {
            throw new ServiceException("账户长度必须在2到20个字符之间");
        }
        if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH)
        {
            throw new ServiceException("密码长度必须在5到20个字符之间");
        }
        if (StringUtils.isEmpty(registerBody.getPhonenumber()))
        {
            throw new ServiceException("手机号不能为空");
        }
        if (StringUtils.isEmpty(registerBody.getRealName()))
        {
            throw new ServiceException("真实姓名不能为空");
        }
        if (StringUtils.isEmpty(registerBody.getIdCard()))
        {
            throw new ServiceException("身份证号不能为空");
        }
        if (StringUtils.isEmpty(registerBody.getIdCardFront()))
        {
            throw new ServiceException("请上传身份证人像面照片");
        }

        if (!StringUtils.isEmpty(registerBody.getSmsCode()))
        {
            smsService.validateSmsCode(registerBody.getPhonenumber(), registerBody.getSmsCode());
        }

        SysUser sysUser = new SysUser();
        sysUser.setUserName(username);
        if (!userService.checkUserNameUnique(sysUser))
        {
            throw new ServiceException("注册用户'" + username + "'失败，注册账号已存在");
        }

        sysUser.setNickName(StringUtils.isEmpty(registerBody.getNickName()) ? username : registerBody.getNickName());
        sysUser.setPhonenumber(registerBody.getPhonenumber());
        sysUser.setPassword(SecurityUtils.encryptPassword(password));
        sysUser.setStatus("0");
        sysUser.setPhoneVerified("1");

        boolean hasIdCardInfo = !StringUtils.isEmpty(registerBody.getIdCard())
                || !StringUtils.isEmpty(registerBody.getIdCardFront())
                || !StringUtils.isEmpty(registerBody.getIdCardBack());

        if (hasIdCardInfo)
        {
            if (!StringUtils.isEmpty(registerBody.getIdCard()))
            {
                if (!IdCardUtils.isValid(registerBody.getIdCard()))
                {
                    throw new ServiceException("身份证号格式不正确");
                }
                sysUser.setIdCard(AesEncryptUtils.encrypt(registerBody.getIdCard()));
            }
            if (!StringUtils.isEmpty(registerBody.getRealName()))
            {
                sysUser.setRealName(registerBody.getRealName());
            }
            if (!StringUtils.isEmpty(registerBody.getIdCardFront()))
            {
                sysUser.setIdCardFront(registerBody.getIdCardFront());
            }
            if (!StringUtils.isEmpty(registerBody.getIdCardBack()))
            {
                sysUser.setIdCardBack(registerBody.getIdCardBack());
            }
            sysUser.setAuditStatus("0");
            sysUser.setIdCardVerified("0");
        }

        if (!StringUtils.isEmpty(registerBody.getFaceImageBase64()))
        {
            sysUser.setFaceVerified("1");
        }


        boolean regFlag = userService.registerUser(sysUser);
        if (!regFlag)
        {
            throw new ServiceException("注册失败，请联系管理员");
        }

        if (!StringUtils.isEmpty(registerBody.getFaceImageBase64()))
        {
            try
            {
                baiduFaceService.faceRegister(
                        registerBody.getFaceImageBase64(),
                        String.valueOf(sysUser.getUserId()),
                        sysUser.getUserName());
                saveVerifyRecord(sysUser.getUserId(), "face", "1", "人脸库注册成功", null);
            }
            catch (Exception e)
            {
                saveVerifyRecord(sysUser.getUserId(), "face", "0", "人脸库注册失败:" + e.getMessage(), null);
            }
        }

        saveVerifyRecord(sysUser.getUserId(), "sms", "1", "手机验证码验证通过", registerBody.getPhonenumber());

        EeAuditRecord auditRecord = new EeAuditRecord();
        auditRecord.setUserId(sysUser.getUserId());
        auditRecord.setAuditType("idcard");
        auditRecord.setAuditStatus("1");
        if (hasIdCardInfo)
        {
            auditRecord.setIdCardFront(registerBody.getIdCardFront());
            auditRecord.setIdCardBack(registerBody.getIdCardBack());
        }
        auditRecordMapper.insertEeAuditRecord(auditRecord);

        EeAuditRecord updateRecord = new EeAuditRecord();
        updateRecord.setRecordId(auditRecord.getRecordId());
        updateRecord.setAuditStatus("1");
        updateRecord.setAuditByName("system");
        updateRecord.setAuditTime(new java.util.Date());
        auditRecordMapper.updateAuditStatus(updateRecord);

        String roleKey = "2".equals(registerBody.getUserType()) ? "person" : "enterprise";
        SysRole targetRole = roleMapper.checkRoleKeyUnique(roleKey);
        if (targetRole != null)
        {
            List<SysUserRole> list = new ArrayList<>();
            SysUserRole ur = new SysUserRole();
            ur.setUserId(sysUser.getUserId());
            ur.setRoleId(targetRole.getRoleId());
            list.add(ur);
            userRoleMapper.batchUserRole(list);
        }

        com.exemptenjoy.system.domain.EeTargetData target = new com.exemptenjoy.system.domain.EeTargetData();
        target.setUserId(sysUser.getUserId());
        target.setTargetName(StringUtils.isEmpty(registerBody.getNickName()) ? registerBody.getUsername() : registerBody.getNickName());
        target.setTargetType("2".equals(registerBody.getUserType()) ? "2" : "1");
        target.setContactPhone(registerBody.getPhonenumber());
        target.setBankAccount("");
        target.setIdentifier(!StringUtils.isEmpty(registerBody.getIdCard()) ? registerBody.getIdCard() : "-");
        target.setAttributes("{}");
        target.setStatus("0");
        com.exemptenjoy.system.service.IEeTargetDataService targetDataService =
            com.exemptenjoy.common.utils.spring.SpringUtils.getBean(com.exemptenjoy.system.service.IEeTargetDataService.class);
        targetDataService.insertEeTargetData(target);

        return "注册成功";
    }

    private void saveVerifyRecord(Long userId, String verifyType, String verifyResult, String verifyContent, String verifyNo)
    {
        try
        {
            EeVerifyRecord record = new EeVerifyRecord();
            record.setUserId(userId);
            record.setVerifyType(verifyType);
            record.setVerifyResult(verifyResult);
            record.setVerifyContent(verifyContent);
            record.setVerifyNo(verifyNo);
            record.setIpAddress(IpUtils.getIpAddr());
            verifyRecordMapper.insertEeVerifyRecord(record);
        }
        catch (Exception e)
        {
        }
    }
}
