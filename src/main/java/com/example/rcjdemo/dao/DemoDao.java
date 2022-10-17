package com.example.rcjdemo.dao;

import org.springframework.stereotype.Component;

@Component
public interface DemoDao extends Mapper<User> {

    List<xxx> findUserById(Long id);
}
