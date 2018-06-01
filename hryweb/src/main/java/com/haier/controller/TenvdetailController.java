package com.haier.controller;

import com.haier.po.Tenvdetail;
import com.haier.po.TenvdetailCustom;
import com.haier.response.Result;
import com.haier.service.TenvdetailService;
import com.haier.util.ResultUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description: 服务-环境映射关系
 * @Author: luqiwei
 * @Date: 2018/5/30 19:45
 */
@Slf4j
@RestController
@RequestMapping("/tenvdetail")
public class TenvdetailController {

    @Autowired
    TenvdetailService tenvdetailService;

    //增
    @PostMapping("/insertOne.do")
    public Result insertOne(Tenvdetail tenvdetail){
        return ResultUtil.success(tenvdetailService.insertOne(tenvdetail));
    }

    //删
    @PostMapping("/deleteOne.do")
    public Result deleteOne(Tenvdetail tenvdetail){
        return ResultUtil.success(tenvdetailService.deleteOne(tenvdetail));
    }

    //查
    @PostMapping("/selectByCondition.do")
    public Result selectByCondition(TenvdetailCustom tenvdetailCustom,Integer pageNum,Integer pageSize){
        return ResultUtil.success(tenvdetailService.selectByCondition(tenvdetailCustom,pageNum,pageSize));
    }
}