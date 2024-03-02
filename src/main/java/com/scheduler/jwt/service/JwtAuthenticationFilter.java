package com.scheduler.jwt.service;

import com.scheduler.jwt.constants.JwtConstants;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Log4j2
@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtHelper jwtHelper;
    private final UserDetailsService userDetailsService;

    /**
     * -> Get Token from Request
     * -> Validate Token
     * -> Get Username from Token
     * -> Load User associated with this Token
     * -> Set Authentication
     * @param request
     * @param response
     * @param filterChain
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestHeader = request.getHeader(JwtConstants.AUTHORIZATION);
        String username = null;
        String token = null;
        if (StringUtils.isNotBlank(requestHeader) && requestHeader.startsWith(JwtConstants.BEARER)) {
            log.info("Authorization Header:  {}", requestHeader);
            token = requestHeader.substring(7);
            try {
                username = jwtHelper.getUsernameFromToken(token);
            } catch (IllegalArgumentException e) {
                log.error("Illegal Argument while fetching the Username: {},{}", e.getMessage(), e);
            } catch (ExpiredJwtException e) {
                log.error("Given JWT Token is Expired: {},{}", e.getMessage(), e);
            } catch (MalformedJwtException e) {
                log.error("Malformed JWT Token: {},{}", e.getMessage(), e);
            } catch (Exception e) {
                log.error("Exception occurred: {},{}", e.getMessage(), e);
            }
        }
        if (StringUtils.isNotBlank(username) && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            Boolean validateToken = jwtHelper.validateToken(token, userDetails);
            if (validateToken) {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                log.info("Not a Valid JWT Token");
            }
        }
        filterChain.doFilter(request, response);
    }
}