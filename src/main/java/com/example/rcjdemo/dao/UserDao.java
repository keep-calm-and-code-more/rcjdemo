package com.example.rcjdemo.dao;

import com.example.rcjdemo.common.MyMapper;
import com.example.rcjdemo.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author lhc
 * @version 1.0
 * @className UserDao
 * @date 2022年10月17日 17:05
 * @description 描述
 */
@Component
public interface UserDao extends MyMapper<User> {

    List<User> getTableList();
}
