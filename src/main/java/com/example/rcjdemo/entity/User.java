package com.example.rcjdemo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author lhc
 * @version 1.0
 * @className User
 * @date 2022年10月17日 17:05
 * @description 描述
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name="user")
public class User {
    @Id
    @GeneratedValue(generator="JDBC")
    private Integer userId;

    private String username;

    private String userPassword;
}
