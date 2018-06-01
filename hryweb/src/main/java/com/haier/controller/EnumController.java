package com.haier.controller;

import com.haier.enums.*;
import com.haier.util.EnumUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @Author: luqiwei
 * @Date: 2018/5/25 14:15
 */
@RestController
@RequestMapping("/enum")
public class EnumController {
    @GetMapping("/allEnum")
    public Map<String,List<Map<String, Object>>> getAllEnum(){
        Map<String,List<Map<String,Object>>> map=new LinkedHashMap<>();
        map.put(AssertTypeEnum.class.getSimpleName(),EnumUtil.getEnumList(AssertTypeEnum.values()));
        map.put(ContentTypeEnum.class.getSimpleName(),EnumUtil.getEnumList(ContentTypeEnum.values()));
        map.put(EnvEnum.class.getSimpleName(),EnumUtil.getEnumList(EnvEnum.values()));
        map.put(HttpTypeEnum.class.getSimpleName(),EnumUtil.getEnumList(HttpTypeEnum.values()));
        map.put(RequestMethodTypeEnum.class.getSimpleName(),EnumUtil.getEnumList(RequestMethodTypeEnum.values()));
        map.put(ResponseTypeEnum.class.getSimpleName(),EnumUtil.getEnumList(ResponseTypeEnum.values()));
        map.put(StatusCodeEnum.class.getSimpleName(),EnumUtil.getEnumList(StatusCodeEnum.values()));
        return map;
    }
    @GetMapping("/assertTypeEnum")
    public List<Map<String, Object>> getAssertTypeEnum() {
        return EnumUtil.getEnumList(AssertTypeEnum.values());
    }

    @GetMapping("/contentTypeEnum")
    public List<Map<String, Object>> getContentTypeEnum() {
        return EnumUtil.getEnumList(ContentTypeEnum.values());
    }

    @GetMapping("/envEnum")
    public List<Map<String, Object>> getEnvEnum() {
        return EnumUtil.getEnumList(EnvEnum.values());
    }

    @GetMapping("/httpTypeEnum.do")
    public List<Map<String, Object>> getHttpTypeEnum() {
        return EnumUtil.getEnumList(HttpTypeEnum.values());
    }

    @GetMapping("/requestMethodTypeEnum")
    public List<Map<String, Object>> getRequestMethodTypeEnum() {
        return EnumUtil.getEnumList(RequestMethodTypeEnum.values());
    }

    @GetMapping("/requestParamTypeEnum")
    public List<Map<String, Object>> getRequestParamTypeEnum() {
        return EnumUtil.getEnumList(RequestParamTypeEnum.values());
    }

    @GetMapping("/responseTypeEnum")
    public List<Map<String, Object>> getResponseTypeEnum() {
        return EnumUtil.getEnumList(ResponseTypeEnum.values());
    }

    @GetMapping("/statusCodeEnum")
    public List<Map<String, Object>> getStatusCodeEnum() {
        return EnumUtil.getEnumList(StatusCodeEnum.values());
    }
}