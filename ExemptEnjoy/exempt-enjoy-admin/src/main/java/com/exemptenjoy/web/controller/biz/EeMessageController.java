package com.exemptenjoy.web.controller.biz;

import java.util.List;
import java.util.Date;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.exemptenjoy.common.core.controller.BaseController;
import com.exemptenjoy.common.core.domain.AjaxResult;
import com.exemptenjoy.common.core.domain.entity.SysUser;
import com.exemptenjoy.system.domain.EeMessage;
import com.exemptenjoy.system.service.IEeMessageService;
import com.exemptenjoy.system.mapper.SysUserMapper;
import com.exemptenjoy.common.core.page.TableDataInfo;

@RestController
@RequestMapping("/biz/message")
public class EeMessageController extends BaseController {

    @Autowired
    private IEeMessageService eeMessageService;

    @Autowired
    private SysUserMapper sysUserMapper;

    @GetMapping("/list")
    public TableDataInfo list(EeMessage eeMessage) {
        startPage();
        eeMessage.setUserId(getUserId());
        List<EeMessage> list = eeMessageService.selectEeMessageList(eeMessage);
        return getDataTable(list);
    }

    @GetMapping("/{messageId}")
    public AjaxResult getInfo(@PathVariable("messageId") Long messageId) {
        return success(eeMessageService.selectEeMessageByMessageId(messageId));
    }

    @PostMapping
    public AjaxResult add(@RequestBody EeMessage eeMessage) {
        return toAjax(eeMessageService.insertEeMessage(eeMessage));
    }

    @PostMapping("/send")
    public AjaxResult send(@RequestBody Map<String, Object> params) {
        Long receiverId = Long.valueOf(params.get("receiverId").toString());
        String title = params.get("title").toString();
        String content = params.get("content").toString();
        EeMessage msg = new EeMessage();
        msg.setUserId(receiverId);
        msg.setSenderId(getUserId());
        msg.setSenderName(getUsername());
        msg.setTitle(title);
        msg.setContent(content);
        msg.setMsgType("mail");
        return toAjax(eeMessageService.insertEeMessage(msg));
    }

    @GetMapping("/users")
    public AjaxResult getUsers() {
        List<SysUser> users = sysUserMapper.selectUserList(new SysUser());
        return success(users);
    }

    @PutMapping
    public AjaxResult edit(@RequestBody EeMessage eeMessage) {
        return toAjax(eeMessageService.updateEeMessage(eeMessage));
    }

    @DeleteMapping("/{messageIds}")
    public AjaxResult remove(@PathVariable Long[] messageIds) {
        return toAjax(eeMessageService.deleteEeMessageByMessageIds(messageIds));
    }

    @PutMapping("/read/{messageId}")
    public AjaxResult markRead(@PathVariable("messageId") Long messageId) {
        EeMessage msg = new EeMessage();
        msg.setMessageId(messageId);
        msg.setIsRead("1");
        msg.setReadTime(new Date());
        return toAjax(eeMessageService.updateEeMessage(msg));
    }

    @PutMapping("/unread/{messageId}")
    public AjaxResult markUnread(@PathVariable("messageId") Long messageId) {
        EeMessage msg = new EeMessage();
        msg.setMessageId(messageId);
        msg.setIsRead("0");
        return toAjax(eeMessageService.updateEeMessage(msg));
    }

    @PutMapping("/readAll")
    public AjaxResult markAllRead() {
        Long userId = getUserId();
        return toAjax(eeMessageService.markAllRead(userId));
    }

    @GetMapping("/unreadCount")
    public AjaxResult unreadCount() {
        Long userId = getUserId();
        return success(eeMessageService.selectUnreadCount(userId));
    }
}