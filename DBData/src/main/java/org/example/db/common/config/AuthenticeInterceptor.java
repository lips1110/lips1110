package org.example.db.common.config;

import org.apache.commons.lang3.StringUtils;
import org.example.db.common.utils.CookieUtils;
import org.example.db.common.utils.JsonTool;
import org.example.db.common.utils.ServletUtils;
import org.example.db.modules.dbmain.bean.BaseResult;
import org.example.db.modules.dbmain.util.JwtTokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

/**
 * @author liuxingyu01
 * @date 2020-03-22-11:23
 * @description: 用户会话验证拦截器
 **/
@Component
public class AuthenticeInterceptor implements HandlerInterceptor {
    private final static Logger logger = LoggerFactory.getLogger(AuthenticeInterceptor.class);

    /**
     * 进入controller层之前拦截请求
     * 返回值：表示是否将当前的请求拦截下来  false：拦截请求，请求别终止。true：请求不被拦截，继续执行
     * Object obj:表示被拦的请求的目标对象（controller中方法）
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        // 判断请求类型，如果是OPTIONS，直接返回
        String options = HttpMethod.OPTIONS.toString();
        if (options.equals(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return true;
        }
        // 从请求中获取token，先从Header里取，取不到的话再从cookie里取（适配前后端分离的模式）
        String token = request.getHeader("token");
        if (StringUtils.isBlank(token)) {
            token = CookieUtils.getCookie(request, "token");
        }
        if (StringUtils.isBlank(token)) {
            responseError(request, response);
            return false;
        } else {
            token = JwtTokenUtil.parseJWT(token);
            if (StringUtils.isBlank(token)) { // JWT验证未通过，返回false
                responseError(request, response);
                return false;
            }
        }
        return true;
    }


    /**
     * 处理请求完成后，视图渲染之前的处理操作(Controller方法调用之后)
     * 此处可以通过ModelAndView(模型和视图对象)对模型数据进行处理或对视图进行处理，modelAndView也可能为null
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
    }


    /**
     * 在整个请求结束之后被调用，也就是视图渲染完毕之后的操作（主要是用于进行性能监控、资源清理工作）
     */
    @Override
    public void afterCompletion(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2, Exception arg3) throws Exception {
    }

    /**
     * 无权限时的返回
     *
     * @param request
     * @param response
     * @throws IOException
     */
    private void responseError(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 返回401状态码
        outWrite(response);
    }

    private void outWrite(HttpServletResponse response) throws IOException {
        BaseResult baseResult = new BaseResult(BaseResult.UNAUTHORIZED, "未登录或会话已失效，请重新登录", null);
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.write(JsonTool.toJsonString(baseResult));
        out.close();
    }

}
