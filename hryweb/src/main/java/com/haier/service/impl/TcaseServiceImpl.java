package com.haier.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.arronlong.httpclientutil.exception.HttpProcessException;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.haier.enums.*;
import com.haier.exception.HryException;
import com.haier.mapper.*;
import com.haier.po.*;
import com.haier.service.*;
import com.haier.util.AssertUtil;
import com.haier.util.BeforeUtil;
import com.haier.util.HryHttpClientUtil;
import com.haier.util.ReflectUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description:
 * @Author: luqiwei
 * @Date: 2018/5/24 17:07
 */
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Slf4j
@Service
public class TcaseServiceImpl implements TcaseService {

    @Autowired
    TcaseMapper tcaseMapper;

    @Autowired
    TcaseCustomMapper tcaseCustomMapper;

    @Autowired
    TserviceService tserviceService;

    @Autowired
    TservicedetailService tservicedetailService;

    @Autowired
    TenvService tenvService;

    @Autowired
    TiService tiService;

    @Override
    public Integer insertOne(Tcase tcase) {
        tcaseMapper.insertSelective(tcase);
        return tcase.getId();
    }

    @Override
    public Integer deleteOne(Integer id) {
        Tcase tcase = new Tcase();
        tcase.setId(id);
        tcase.setStatus(StatusEnum._ONE.getId());
        return tcaseMapper.updateByPrimaryKeySelective(tcase);
    }

    @Override
    public Integer deleteByCondition(Tcase tcase) {
        TcaseExample tcaseExample = new TcaseExample();
        TcaseExample.Criteria criteria = tcaseExample.createCriteria();
        criteria.andStatusGreaterThan(0);
        if (tcase != null) {
            if (tcase.getIid() != null) {
                criteria.andIidEqualTo(tcase.getIid());
            }
            if (tcase.getEnvid() != null) {
                criteria.andEnvidEqualTo(tcase.getEnvid());
            }
            if (tcase.getServiceid() != null) {
                criteria.andServiceidEqualTo(tcase.getServiceid());
            }
        } else {
            return null;
        }
        Tcase t = new Tcase();
        t.setStatus(StatusEnum._ONE.getId());
        return tcaseMapper.updateByExampleSelective(t, tcaseExample);
    }

    @Override
    public Integer updateOne(Tcase tcase) {
        return tcaseMapper.updateByPrimaryKeySelective(tcase);
    }

    @Override
    public Tcase selectOne(Integer id) {
        return tcaseMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<Tcase> selectByCondition(Tcase tcase) {
        ReflectUtil.setFieldAddPercentAndCleanZero(tcase, false);
        TcaseExample tcaseExample = new TcaseExample();
        TcaseExample.Criteria criteria = tcaseExample.createCriteria();
        TcaseExample.Criteria criteria2 = null;
        criteria.andStatusGreaterThan(0);
        if (tcase != null) {
            if (tcase.getEnvid() != null) {
                criteria.andEnvidEqualTo(tcase.getEnvid());
                /**
                 * 不仅要获取传入的EnvID的用例,还需要获取EnvID=0的用例,EnvId=0表明此用例可运行于任意环境
                 */
                criteria2 = tcaseExample.createCriteria();
                criteria2.andStatusGreaterThan(0);
                criteria2.andEnvidEqualTo(0);
                if (tcase.getServiceid() != null) {
                    criteria.andServiceidEqualTo(tcase.getServiceid());
                }
                if (tcase.getIid() != null) {
                    criteria.andIidEqualTo(tcase.getIid());
                }
                if (tcase.getRequestparam() != null) {
                    criteria.andRequestparamLike(tcase.getRequestparam());
                }
                if (tcase.getId() != null) {
                    criteria.andIdEqualTo(tcase.getId());
                }
                if (tcase.getCasename() != null) {
                    criteria.andCasenameLike(tcase.getCasename());
                }
                if (tcase.getTestclass() != null) {
                    criteria.andTestclassEqualTo(tcase.getTestclass().replaceAll("%", ""));
                }
                if (tcase.getAuthor() != null) {
                    criteria.andAuthorLike(tcase.getAuthor());
                }
                if (tcase.getExpected() != null) {
                    criteria.andExpectedLike(tcase.getExpected());
                }
                if (tcase.getRemark() != null) {
                    criteria.andRemarkLike(tcase.getRemark());
                }
                if (tcase.getAsserttype() != null) {
                    criteria.andAsserttypeEqualTo(tcase.getAsserttype());
                }
            }
            if (tcase.getServiceid() != null) {
                criteria.andServiceidEqualTo(tcase.getServiceid());
            }
            if (tcase.getIid() != null) {
                criteria.andIidEqualTo(tcase.getIid());
            }
            if (tcase.getRequestparam() != null) {
                criteria.andRequestparamLike(tcase.getRequestparam());
            }
            if (tcase.getId() != null) {
                criteria.andIdEqualTo(tcase.getId());
            }
            if (tcase.getCasename() != null) {
                criteria.andCasenameLike(tcase.getCasename());
            }
            if (tcase.getTestclass() != null) {
                criteria.andTestclassEqualTo(tcase.getTestclass().replaceAll("%", ""));
            }
            if (tcase.getAuthor() != null) {
                criteria.andAuthorLike(tcase.getAuthor());
            }
            if (tcase.getExpected() != null) {
                criteria.andExpectedLike(tcase.getExpected());
            }
            if (tcase.getRemark() != null) {
                criteria.andRemarkLike(tcase.getRemark());
            }
            if (tcase.getAsserttype() != null) {
                criteria.andAsserttypeEqualTo(tcase.getAsserttype());
            }
        }
        return tcaseMapper.selectByExample(tcaseExample);
    }

    @Override
    public PageInfo<TcaseCustom> selectByContion(TcaseCustom tcaseCustom, Integer pageNum, Integer pageSize) {
        ReflectUtil.setFieldAddPercentAndCleanZero(tcaseCustom, true);
        if (tcaseCustom != null) {
            if (tcaseCustom.getTestclass() != null) {
                tcaseCustom.setTestclass(tcaseCustom.getTestclass().replaceAll("%", ""));//testclass只支持equal查询
            }
        }
        PageHelper.startPage(pageNum, pageSize, SortEnum.UPDATETIME.getValue() + "," + SortEnum.ID.getValue());
        List<TcaseCustom> tcaseCustomList = tcaseCustomMapper.selectByCondition(tcaseCustom);
        PageInfo<TcaseCustom> pageInfo = new PageInfo<>(tcaseCustomList);
        return pageInfo;
    }


    @Override
    public RunOneResult runOne(Tcase tcase) {
        //准备数据

        Tenv tenv = tenvService.selectOne(tcase.getEnvid());
        Ti ti = tiService.selectOne(tcase.getIid());
        Integer serviceId = ti.getServiceid();
        Tservice tservice = tserviceService.selectOne(serviceId);

        Tservicedetail tservicedetail;
        Tservicedetail condition = new Tservicedetail();
        condition.setEnvid(tcase.getEnvid());
        condition.setServiceid(serviceId);
        List<Tservicedetail> tservicedetails = tservicedetailService.selectByCondition(condition);
        if (tservicedetails.size() == 0) {
            throw new HryException(StatusCodeEnum.NOT_FOUND, "服务=" + tservice.getServicekey() + "(" + tservice.getServicename() + ")" + ",环境=" + tenv.getEnvkey() + "(" + tenv.getRemark() + ")");
        } else {
            tservicedetail = tservicedetails.get(0);
        }

        String url = HttpTypeEnum.getValue(tservice.getHttptype()) + "://" + tservicedetail.getHostinfo() + ti.getIuri();
        String actualParam = BeforeUtil.replace(tcase.getRequestparam(), tservicedetail.getDbinfo(), null);

        //发送http请求
        String actual = HryHttpClientUtil.send(url, ti.getIrequestmethod(), ti.getIcontenttype(), ti.getIparamtype(), actualParam);

        //断言结果
        Boolean result = AssertUtil.supperAssert(tcase.getAsserttype(), tcase.getExpected(), actual, ti.getIresponsetype());

        RunOneResult runOneResult = new RunOneResult();
        runOneResult.setAssertType(AssertTypeEnum.getValue(tcase.getAsserttype()));
        runOneResult.setContentType(ContentTypeEnum.getValue(ti.getIcontenttype()));
        runOneResult.setExpected(tcase.getExpected());
        runOneResult.setIUri(ti.getIuri());
        runOneResult.setParam(tcase.getRequestparam());
        runOneResult.setPrarmType(RequestParamTypeEnum.getValue(ti.getIparamtype()));
        runOneResult.setRequestMethod(RequestMethodTypeEnum.getValue(ti.getIrequestmethod()));
        runOneResult.setResponseType(ResponseTypeEnum.getValue(ti.getIresponsetype()));
        runOneResult.setServiceKey(tservice.getServicekey());


        List<RunOneResultSub> list = new ArrayList<>();
        RunOneResultSub runOneResultSub = new RunOneResultSub();
        runOneResultSub.setActualParam(actualParam);
        runOneResultSub.setActual(actual);
        runOneResultSub.setEnv(EnvEnum.getValue(tservicedetail.getEnvid()));
        runOneResultSub.setHostInfo(tservicedetail.getHostinfo());
        if (result) {
            runOneResultSub.setResult(AssertResultEnum.PASS);
        } else {
            runOneResultSub.setResult(AssertResultEnum.FAIL);
        }
        list.add(runOneResultSub);
        runOneResult.setRunOneResultSubList(list);

        return runOneResult;
    }

    @Override
    public RunOneResult runOne(Integer id) {
        Tcase tcase = tcaseMapper.selectByPrimaryKey(id);
        return this.runOne(tcase);
    }

    @Override
    public void runOne(Tcase tcase, String testClassName) {
        //tcase中有iId,

        //确定测试类
        //确定方法选择器,
        //筛选用例
        //如果不传测试类,则根据使用默认的测试类
        if (StringUtils.isBlank(testClassName)) {

        }
    }

    @Override
    public void runOne(Integer caseId, String testClassName) {
        Tcase tcase = this.selectOne(caseId);
        Ti ti = tiService.selectOne(tcase.getIid());
        Tservice tservice = tserviceService.selectOne(ti.getServiceid());
        //如果未指定测试类,默认使用默认测试类来测试
        if (StringUtils.isBlank(testClassName)) {
            testClassName = tservice.getClassname();
        }
        String methodName = ti.getIuri().replaceAll("/", "-");
        if (methodName.startsWith("_")) {
            methodName = methodName.substring(1);
        }
        XmlInclude include = new XmlInclude(methodName);
        XmlClass xmlClass = new XmlClass(PackageEnum.TEST.getPackageName() + "." + testClassName);
        // xmlClass.setIncludedMethods();
    }


}
