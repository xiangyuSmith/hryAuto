package com.haier.service.impl;

import com.alibaba.fastjson.JSON;
import com.haier.enums.*;
import com.haier.exception.HryException;
import com.haier.mapper.TcustomMapper;
import com.haier.mapper.TcustomdetailMapper;
import com.haier.po.*;
import com.haier.testng.run.Runner;
import com.haier.vo.CustomVO;
import com.haier.service.*;
import com.haier.util.ReflectUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Description:
 * @Author: luqiwei
 * @Date: 2018/6/28 15:36
 */
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Service
@Slf4j
public class TcustomServiceImpl implements TcustomService {

    @Value("${zdy.reportPath}")
    String reportPath;

    @Value("${zdy.resourcePathPattern}")
    String resourcePathPattern;

    @Autowired
    TcustomMapper tcustomMapper;

    @Autowired
    TenvService tenvService;

    @Autowired
    UserService userService;

    @Autowired
    TserviceService tserviceService;

    @Autowired
    TreportService treportService;

    @Autowired
    TservicedetailService tservicedetailService;

    @Autowired
    TcustomdetailService tcustomdetailService;

    @Autowired
    Runner runner;

    @Override
    public Integer insertOne(Tcustom tcustom, List<Tcustomdetail> tcustomdetails) {
        tcustomMapper.insertSelective(tcustom);
        Integer customId = tcustom.getId();//获取插入的自增Id
        for (Tcustomdetail tcustomdetail : tcustomdetails) {
            tcustomdetail.setCustomid(customId);
            if (tcustomdetail.getParentclientid() == null) {
                tcustomdetail.setParentclientid(0);
            }
        }
        tcustomdetailService.insertBatch(tcustomdetails);
        return customId;
    }

    @Override
    public Integer insertOne(CustomVO customVO) {
        Tcustom tcustom = new Tcustom();
        ReflectUtil.cloneChildToParent(tcustom, customVO);
        List<Tcustomdetail> list = customVO.getTcustomdetails();
        return this.insertOne(tcustom, list);
    }

    @Override
    public Integer updateOne(Tcustom tcustom) {
        return tcustomMapper.updateByPrimaryKeySelective(tcustom);
    }

    @Override
    public Integer updateOne(CustomVO customVO) {
        Tcustom tcustom = new Tcustom();
        ReflectUtil.cloneChildToParent(tcustom, customVO);
        /**
         *  更新tcustom信息
         */
        this.updateOne(tcustom);
        /**
         * 更新tcustomdetail信息
         */
        //先删除历史保存的tcustomdetail记录
        Tcustomdetail condition = new Tcustomdetail();
        condition.setCustomid(tcustom.getId());
        tcustomdetailService.physicalDeleteByCondition(condition);

        //插入本次编辑的tcustomdetail记录
        List<Tcustomdetail> tcustomdetails = customVO.getTcustomdetails();
        for (Tcustomdetail tcustomdetail : tcustomdetails) {
            tcustomdetail.setCustomid(tcustom.getId());
            //tcustomdetailService.insertOne(tcustomdetail);//为提升效率 ,改用下面一句代码 ,批量插入
        }
        tcustomdetailService.insertBatch(tcustomdetails);
        return tcustom.getId();
    }

    @Override
    public Integer deleteOne(Integer id) {
        /**
         * 删除tcustom
         */
        Tcustom tcustom = new Tcustom();
        tcustom.setId(id);
        tcustom.setStatus(-1);
        this.updateOne(tcustom);
        /**
         * 删除tcustomdetail
         */
        Tcustomdetail tcustomdetail = new Tcustomdetail();
        tcustomdetail.setCustomid(id);
        //物理删除tcustomdetail表的记录
        tcustomdetailService.physicalDeleteByCondition(tcustomdetail);
        return id;
    }


    @Override
    public CustomVO selectOne(Integer id) {
        CustomVO vo = new CustomVO();
        Tcustom tcustom = tcustomMapper.selectByPrimaryKey(id);
        ReflectUtil.cloneParentToChild(tcustom, vo);

        List<Tcustomdetail> tcustomdetails = tcustomdetailService.selectByCondition(id);
        vo.setTcustomdetails(tcustomdetails);

        return vo;
    }

    @Override
    public List<Tcustom> selectByCondition(Tcustom tcustom) {
        TcustomExample tcustomExample = new TcustomExample();
        TcustomExample.Criteria criteria = tcustomExample.createCriteria();
        tcustomExample.setOrderByClause(SortEnum.UPDATETIME.getValue());
        criteria.andStatusGreaterThan(0);
        if (tcustom != null && tcustom.getUserid() != null) {
            criteria.andUseridEqualTo(tcustom.getUserid());
        }
        return tcustomMapper.selectByExample(tcustomExample);
    }


    @Override
    public List<TcustomCustom> selectTcustomCustomByCondition(Tcustom tcustom) {
        List<Tcustom> tcustoms = this.selectByCondition(tcustom);
        List<TcustomCustom> tcustomCustoms = new ArrayList<>();

        List<Tenv> tenvList = tenvService.selectAll();
        Map<Integer, Tenv> tenvMap = new HashMap<>();
        for (Tenv _env : tenvList) {
            tenvMap.put(_env.getId(), _env);
        }

        for (Tcustom t : tcustoms) {
            TcustomCustom tcustomCustom = new TcustomCustom();
            ReflectUtil.cloneParentToChild(t, tcustomCustom);
            tcustomCustom.setEnvkey(tenvMap.get(tcustomCustom.getEnvid()).getEnvkey());

            List<Tcustomdetail> tcustomdetails = tcustomdetailService.selectByCondition(t.getId());
            tcustomCustom.setTcustomdetails(tcustomdetails);

            tcustomCustoms.add(tcustomCustom);
        }

        return tcustomCustoms;
    }

    @Override
    public String run(Integer customId, Integer executeUserId) {

        //VO包含Tcustom 和 Tcustomdetail
        CustomVO customVO = this.selectOne(customId);

        List<Tcustomdetail> tcustomdetails = customVO.getTcustomdetails();

        List<Tcustomdetail> tcustomdetails_service = new ArrayList<>();//定制的服务
        List<Tcustomdetail> tcustomdetails_interface = new ArrayList<>();//定制的接口
        List<Tcustomdetail> tcustomdetails_case = new ArrayList<>();//定制的用例

        List<Integer> service_ids = new ArrayList<>();
        List<String> service_names = new ArrayList<>();

        for (Tcustomdetail tcustomdetail : tcustomdetails) {
            /**
             * 服务-对应测试类
             */
            if (ClientLevelEnum.SERVICE.getLevel().equals(tcustomdetail.getClientlevel())) {
                tcustomdetails_service.add(tcustomdetail);
                service_ids.add(tcustomdetail.getClientid());
                service_names.add(tcustomdetail.getClientname());
            }

            /**
             * 接口-对应测试方法,注意,iUri与测试方法需要转换一下 /aaa/bbb   转成 aaa_bbb
             */
            if (ClientLevelEnum.INTERFACE.getLevel().equals(tcustomdetail.getClientlevel())) {
                tcustomdetails_interface.add(tcustomdetail);
            }

            /**
             * 用例,用例需要传到测试类中,但是测试类现在仅支持传入Map<String,String>,需要将用例ids转成字符串传入并在测试类中解析出来
             */
            if (ClientLevelEnum.CASE.getLevel().equals(tcustomdetail.getClientlevel())) {
                tcustomdetails_case.add(tcustomdetail);
            }
        }

        /**
         * iMap<服务id,List<接口名称>>
         */
        Map<Integer, List<String>> iMap = new HashMap<>();

        /**
         * cMap<服务id,Map<接口名称,List<用例id>>>
         */
        Map<Integer, Map<String, List<Integer>>> cMap = new HashMap<>();

        if (tcustomdetails_interface.size() > 0) {
            for (Tcustomdetail i : tcustomdetails_interface) {
                /**
                 * 将iUri转换成对应的测试方法名称
                 */
                String iUri = i.getClientname();//接口Uri
                String testMethodName;//对应测试类中的测试方法名
                if (iUri.startsWith("/")) {
                    testMethodName = iUri.replaceFirst("/", "").replaceAll("/", "_");
                } else {
                    testMethodName = iUri.replaceAll("/", "_");
                }

                /**
                 * 将测试方法归类到测试类中t.getParentclientid()==Service.ID
                 */
                if (!iMap.containsKey(i.getParentclientid())) {
                    List<String> list = new ArrayList<>();
                    list.add(testMethodName);
                    iMap.put(i.getParentclientid(), list);
                } else {
                    iMap.get(i.getParentclientid()).add(testMethodName);
                }

                /**
                 * 找此接口是否定制过用例
                 */
                Map<String, List<Integer>> i_c_map = new HashMap<>();
                for (Tcustomdetail c : tcustomdetails_case) {
                    if (c.getParentclientid().equals(i.getClientid())) {
                        //此接口定制了用例
                        if (!i_c_map.containsKey(testMethodName)) {
                            List<Integer> listC = new ArrayList<>();
                            listC.add(c.getClientid());
                            i_c_map.put(testMethodName, listC);
                        } else {
                            i_c_map.get(testMethodName).add(c.getClientid());
                        }
                    }
                }
                if (i_c_map.size() > 0) {
                    if (cMap.containsKey(i.getParentclientid())) {
                        //如果serviceId已经存在 ,则将i_c_map合并到原来的i_c_map中,fixed by luqiwei 2018/08/07
                        cMap.get(i.getParentclientid()).putAll(i_c_map);//map.putAll():映射不存在则复制,存在则覆盖
                    } else {
                        cMap.put(i.getParentclientid(), i_c_map);
                    }
                }
            }

        }


        /**
         * 将准备好的测试类,测试方法,测试case传递给testNG
         */
        Map<Tcustomdetail, XmlClass> sMap = new HashMap<>();

        Integer envid = customVO.getEnvid();
        User user = userService.selectOne(executeUserId);

        log.info("即将运行定制:" + customId);
//        Tservicedetail condition = new Tservicedetail();
        for (Tcustomdetail tcustomdetail : tcustomdetails_service) {
            //构建测试类
            String testClassName = PackageEnum.TEST.getPackageName() + "." + tcustomdetail.getClassname();
            log.info("此次定制要测试的类:" + testClassName);
            XmlClass xmlClass = new XmlClass(testClassName);

            Map<String, String> params = new HashMap<>();//构建测试类常规参数
            params.put(ParamKeyEnum.SERVICEID.getKey(), tcustomdetail.getClientid() + "");
            params.put(ParamKeyEnum.ENVID.getKey(), envid + "");
            params.put(ParamKeyEnum.DESIGNER.getKey(), "");//此字段为预留后期使用,先传空值

            //构建此测试类对应的方法选择器(如果有的话)
            if (iMap.size() > 0) {
                List<String> runInterface = iMap.get(tcustomdetail.getClientid());
                if (runInterface != null && runInterface.size() > 0) {
                    List<XmlInclude> xmlIncludes = new ArrayList<>();
                    for (String s : runInterface) {
                        XmlInclude include = new XmlInclude(s);
                        xmlIncludes.add(include);
                    }
                    xmlClass.setIncludedMethods(xmlIncludes);
                }
            }

            //构建此测试类对应的case参数(如果有的话)
            String i_c_jsonStr = "";
            if (cMap.size() > 0) {
                Map<String, List<Integer>> stringListMap = cMap.get(tcustomdetail.getClientid());
                if (stringListMap != null && stringListMap.size() > 0) {
                    i_c_jsonStr = JSON.toJSONString(stringListMap);

                }
            }

            params.put(ParamKeyEnum.I_C.getKey(), i_c_jsonStr);//参数名:i_c
            params.put(ParamKeyEnum.I_C_ZDY.getKey(), "");//自定义Case,定制测试中使用不到,故这里传空值

            log.info("传给测试类的参数:" + params.toString());

            xmlClass.setParameters(params);
            sMap.put(tcustomdetail, xmlClass);//建立映射关系


            /**
             * 以下注释的内容为老的直接从tservicedetail表中获取对应的测试类
             * 原因:
             * 现在需求变化为:一个服务可以对应多个测试类,
             * 测试类信息直接维护到定制详情表中(tcustomdetail.className),前提是tcustomdetail.clientLevel=1,即服务才会也必须要标记测试类信息
             */

            /*condition.setEnvid(envid);
            condition.setServiceid(tcustomdetail.getClientid());
            List<Tservicedetail> tservicedetails_service = tservicedetailService.selectByCondition(condition);
            if (tservicedetails_service != null && tservicedetails_service.size() > 0) {

                //取第1条记录,按正常情况 ,有且仅有一条记录才对,否则 就是脏数据
                Tservicedetail s0 = tservicedetails_service.get(0);

                //测试类必须已经填写
                if (s0.getClazz() != null && !"".equals(s0.getClazz())) {

                    //构建测试类
                    XmlClass xmlClass = new XmlClass(s0.getClazz());

                    Map<String, String> params = new HashMap<>();//构建测试类常规参数
                    params.put(ParamKeyEnum.SERVICEID.getKey(), s0.getServiceid() + "");
                    params.put(ParamKeyEnum.ENVID.getKey(), s0.getEnvid() + "");
                    params.put(ParamKeyEnum.DESIGNER.getKey(), "");//此字段为预留后期使用,先传空值

                    //构建此测试类对应的方法选择器(如果有的话)
                    if (iMap.size() > 0) {
                        List<String> runInterface = iMap.get(tcustomdetail.getClientid());
                        if (runInterface != null && runInterface.size() > 0) {
                            List<XmlInclude> xmlIncludes = new ArrayList<>();
                            for (String s : runInterface) {
                                XmlInclude include = new XmlInclude(s);
                                xmlIncludes.add(include);
                            }
                            xmlClass.setIncludedMethods(xmlIncludes);
                        }
                    }

                    //构建此测试类对应的case参数(如果有的话)
                    String i_c_jsonStr = "";
                    if (cMap.size() > 0) {
                        Map<String, List<Integer>> stringListMap = cMap.get(tcustomdetail.getClientid());
                        if (stringListMap != null && stringListMap.size() > 0) {
                            i_c_jsonStr = JSON.toJSONString(stringListMap);

                        }
                    }
                    params.put(ParamKeyEnum.I_C.getKey(), i_c_jsonStr);//参数名:i_c
                    xmlClass.setParameters(params);
                    sMap.put(tcustomdetail, xmlClass);//建立映射关系

        }
    }*/
        }

        Tenv tenv = tenvService.selectOne(envid);

        String date = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String reportName = "r_u" + user.getId() + "_c" + customId + "_" + date + ".html";//u(user)代表用户,c(custom)代表定制
        //构造入库测试报告记录
        Treport treport = new Treport();
        treport.setCustomid(customVO.getId());
        treport.setCustomname(customVO.getCustomname());
        treport.setEnvid(envid);
        treport.setEnvkey(tenv.getEnvkey());
        treport.setServiceids(JSON.toJSONString(service_ids));//[1,2,3]
        treport.setServicenames(JSON.toJSONString(service_names));//["aaa","bbb","ccc"],取出后,可直接转换成JSONArray
        treport.setUserid(user.getId());
        treport.setUsername(user.getRealname());
        treport.setReportpath(reportPath);
        treport.setReportname(resourcePathPattern + reportName);
        treport.setStatus(StatusEnum.FIVE.getId());//测试报告生成中

        Integer treportId = treportService.insertOne(treport);

        runner.run(null, treportId, reportName, customVO.getCustomname(), sMap);
        return resourcePathPattern + reportName;
    }

}
