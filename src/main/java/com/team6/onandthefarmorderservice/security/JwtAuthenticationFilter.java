package com.team6.onandthefarmorderservice.security;

import com.team6.onandthefarmorderservice.security.token.JwtTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private String adminKey;
    private JwtTokenUtil jwtTokenUtil;
    Environment env;

    @Autowired
    public JwtAuthenticationFilter(JwtTokenUtil jwtTokenUtil,
                                   Environment env) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.env = env;
        this.adminKey = env.getProperty("custom-api-key.jwt.admin-key");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String accessToken = request.getHeader("Authorization");

        if(accessToken != null) {
            Long memberId = jwtTokenUtil.getId(accessToken);
            String memberRole = jwtTokenUtil.getRole(accessToken);


            // 식별된 정상 유저인 경우, 요청 context 내에서 참조 가능한 인증 정보(jwtAuthentication) 생성
            UsernamePasswordAuthenticationToken jwtAuthentication = null;
            if (memberId != null) {
                if (memberRole.equals("ROLE_USER")) {
                    jwtAuthentication = new UsernamePasswordAuthenticationToken(memberId + " " + memberRole,
                            memberId + adminKey, AuthorityUtils.createAuthorityList("ROLE_USER"));
                } else if (memberRole.equals("ROLE_SELLER")) {
                    jwtAuthentication = new UsernamePasswordAuthenticationToken(memberId + " " + memberRole,
                            memberId + adminKey, AuthorityUtils.createAuthorityList("ROLE_SELLER"));
                } else {
                    jwtAuthentication = new UsernamePasswordAuthenticationToken(memberId + " " + memberRole,
                            memberId + adminKey, AuthorityUtils.createAuthorityList("ROLE_ADMIN"));
                }
            }

            // jwt 토큰으로 부터 획득한 인증 정보(authentication) 설정
            SecurityContextHolder.getContext().setAuthentication(jwtAuthentication);
        }

        filterChain.doFilter(request, response);
    }
}
