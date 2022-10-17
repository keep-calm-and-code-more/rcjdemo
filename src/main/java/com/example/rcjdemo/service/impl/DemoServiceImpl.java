package com.example.rcjdemo.service.impl;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.example.rcjdemo.dao.UserDao;
import com.example.rcjdemo.service.DemoService;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class DemoServiceImpl implements DemoService {

    final UserDao userDao;

    public DemoServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public String demoOne(Map<String, Object> name) {
//        return JSONUtil.toJsonPrettyStr(userDao.selectAll());
        return JSONUtil.toJsonPrettyStr(userDao.getTableList());
    }
}
