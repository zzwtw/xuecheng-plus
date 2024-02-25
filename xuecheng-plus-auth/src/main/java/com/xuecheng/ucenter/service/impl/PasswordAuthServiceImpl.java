package com.xuecheng.ucenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.feignClient.CheckCodeClient;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.AuthService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service("password_authservice")
public class PasswordAuthServiceImpl implements AuthService {
    @Autowired
    private XcUserMapper xcUserMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private CheckCodeClient checkCodeClient;
    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {
        // 开始认证
        LambdaQueryWrapper<XcUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(XcUser::getUsername, authParamsDto.getUsername());
        XcUser xcUser = xcUserMapper.selectOne(queryWrapper);
        if (xcUser == null) {
            return null;
        }
        // 验证码校验
        String checkcode = authParamsDto.getCheckcode();
        String checkcodekey = authParamsDto.getCheckcodekey();
        if (checkcodekey == null){
            throw new RuntimeException("验证码不能为空");
        }
        Boolean verify = checkCodeClient.verify(checkcodekey, checkcode);
        if (verify == null || !verify){
            throw new RuntimeException("验证码错误");
        }
        // 取出数据库存储的正确密码
        String password = xcUser.getPassword();
        // 前端传来的密码
        String passwordForm = authParamsDto.getPassword();
        // 校验密码
        boolean matches = passwordEncoder.matches(passwordForm, password);
        if (!matches){
            throw new RuntimeException("账号密码错误");
        }
        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(xcUser,xcUserExt);
        return xcUserExt;
    }
}
