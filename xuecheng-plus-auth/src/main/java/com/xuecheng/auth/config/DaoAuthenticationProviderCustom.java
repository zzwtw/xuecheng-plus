package com.xuecheng.auth.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

/**
 * 如果OverridingClass位于Spring的组件扫描路径下，
 * 并且它被标记为一个Spring组件（例如，通过@Component、@Service、@Repository或@Controller注解），
 * 那么Spring会自动将它作为一个Bean注册到容器中。
 * 在这种情况下，当你从容器中请求OriginalClass类型的Bean时，
 * Spring会提供一个OverridingClass的实例，前提是没有其他同名的OriginalClass Bean定义覆盖了它。
 */
@Component
public class DaoAuthenticationProviderCustom extends DaoAuthenticationProvider {
    @Autowired
    public void setUserDetailsService(UserDetailsService userDetailsService) {
        super.setUserDetailsService(userDetailsService);
    }

    // 屏蔽密码对比，不用@Override也可以重写方法。
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {

    }

}
