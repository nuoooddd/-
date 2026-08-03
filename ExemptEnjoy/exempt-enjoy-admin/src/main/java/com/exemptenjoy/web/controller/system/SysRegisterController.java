package com.exemptenjoy.web.controller.system;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.fastjson2.JSONObject;
import com.exemptenjoy.common.core.domain.AjaxResult;
import com.exemptenjoy.common.core.domain.model.RegisterBody;
import com.exemptenjoy.framework.web.service.BaiduFaceService;
import com.exemptenjoy.framework.web.service.SysRegisterService;

@RestController
public class SysRegisterController
{
    @Autowired
    private SysRegisterService registerService;

    @Autowired
    private BaiduFaceService baiduFaceService;

    @PostMapping("/register")
    public AjaxResult register(@RequestBody RegisterBody registerBody)
    {
        String msg = registerService.register(registerBody);
        return AjaxResult.success(msg);
    }

    @PostMapping("/face/verify")
    public AjaxResult faceVerify(@RequestBody Map<String, String> params)
    {
        String imageBase64 = params.get("imageBase64");
        String idCardNumber = params.get("idCardNumber");
        String realName = params.get("realName");
        if (imageBase64 == null || imageBase64.isEmpty())
        {
            return AjaxResult.error("人脸图片不能为空");
        }
        try
        {
            JSONObject result = baiduFaceService.faceVerify(imageBase64, idCardNumber, realName);
            AjaxResult ajax = AjaxResult.success();
            ajax.put("verified", result.getBooleanValue("result"));
            ajax.put("score", result.getDoubleValue("score"));
            ajax.put("msg", result.getString("msg"));
            return ajax;
        }
        catch (Exception e)
        {
            return AjaxResult.error("人脸识别失败: " + e.getMessage());
        }
    }
}
