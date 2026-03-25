package com.mtp.config.center.model;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.LinkedHashMap;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class R extends LinkedHashMap<String, Object> {

    // 状态码字段名
    public static final String CODE = "code";
    public static final String MSG = "message";
    public static final String DATA = "data";
    public static final String SUCCESS = "success";

    // 默认成功状态码
    private static final int DEFAULT_SUCCESS_CODE = 200;
    private static final String DEFAULT_SUCCESS_MSG = "操作成功";

    // 默认失败状态码
    private static final int DEFAULT_ERROR_CODE = 500;
    private static final String DEFAULT_ERROR_MSG = "操作失败";

    /**
     * 创建成功响应（无数据）
     */
    public static R ok() {
        return ok(DEFAULT_SUCCESS_MSG, null);
    }

    /**
     * 创建成功响应（带消息）
     */
    public static R ok(String msg) {
        return ok(msg, null);
    }

    /**
     * 创建成功响应（带数据）
     */
    public static R ok(Object data) {
        return ok(DEFAULT_SUCCESS_MSG, data);
    }

    /**
     * 创建成功响应（带分页数据）
     */
    public static R ok(Page<?> page) {
        java.util.Map<String, Object> data = new java.util.LinkedHashMap<>();
        data.put("records", page.getRecords());
        data.put("total", page.getTotal());
        data.put("current", page.getCurrent());
        data.put("size", page.getSize());
        return ok(DEFAULT_SUCCESS_MSG, data);
    }

    /**
     * 创建成功响应（带消息和数据）
     */
    public static R ok(String msg, Object data) {
        return new R()
                .setCode(DEFAULT_SUCCESS_CODE)
                .setMsg(msg)
                .setSuccess(true)
                .setData(data);
    }

    /**
     * 创建失败响应
     */
    public static R error() {
        return error(DEFAULT_ERROR_MSG);
    }

    /**
     * 创建失败响应（带消息）
     */
    public static R error(String msg) {
        return error(DEFAULT_ERROR_CODE, msg);
    }

    /**
     * 创建失败响应（带状态码和消息）
     */
    public static R error(int code, String msg) {
        return new R()
                .setCode(code)
                .setMsg(msg)
                .setSuccess(false);
    }

    /**
     * 创建失败响应（带状态码、消息和数据）
     */
    public static R error(int code, String msg, Object data) {
        return new R()
                .setCode(code)
                .setMsg(msg)
                .setSuccess(false)
                .setData(data);
    }

    /**
     * 使用 HttpStatus 创建失败响应
     */
    public static R error(HttpStatus httpStatus) {
        return error(httpStatus.value(), httpStatus.getReasonPhrase());
    }

    /**
     * 使用 HttpStatus 创建失败响应（带消息）
     */
    public static R error(HttpStatus httpStatus, String msg) {
        return error(httpStatus.value(), msg);
    }

    /**
     * 设置状态码
     */
    public R setCode(int code) {
        this.put(CODE, code);
        return this;
    }

    /**
     * 获取状态码
     */
    public Integer getCode() {
        return (Integer) this.get(CODE);
    }

    /**
     * 设置消息
     */
    public R setMsg(String msg) {
        this.put(MSG, msg);
        return this;
    }

    /**
     * 获取消息
     */
    public String getMsg() {
        return (String) this.get(MSG);
    }

    /**
     * 设置数据
     */
    public R setData(Object data) {
        this.put(DATA, data);
        return this;
    }

    /**
     * 获取数据
     */
    public Object getData() {
        return this.get(DATA);
    }

    /**
     * 设置成功状态
     */
    public R setSuccess(boolean success) {
        this.put(SUCCESS, success);
        return this;
    }

    /**
     * 获取成功状态
     */
    public Boolean getSuccess() {
        return (Boolean) this.get(SUCCESS);
    }

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        Boolean success = getSuccess();
        return success != null && success;
    }

    /**
     * 判断是否失败
     */
    public boolean isError() {
        return !isSuccess();
    }

    /**
     * 链式添加自定义字段
     */
    public R put(String key, Object value) {
        super.put(key, value);
        return this;
    }

    /**
     * 构建响应（用于Spring MVC）
     */
    public static R build() {
        return new R();
    }

    // ========== 常用业务状态码 ==========

    /**
     * 参数错误
     */
    public static R paramError() {
        return error(400, "参数错误");
    }

    public static R paramError(String msg) {
        return error(400, msg);
    }

    /**
     * 未授权
     */
    public static R unauthorized() {
        return error(401, "未授权，请登录");
    }

    /**
     * 无权限
     */
    public static R forbidden() {
        return error(403, "无权限访问");
    }

    /**
     * 资源不存在
     */
    public static R notFound() {
        return error(404, "资源不存在");
    }

    /**
     * 业务异常
     */
    public static R businessError(String msg) {
        return error(1001, msg);
    }
}
