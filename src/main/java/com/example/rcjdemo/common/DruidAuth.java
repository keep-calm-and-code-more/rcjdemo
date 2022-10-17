package com.example.rcjdemo.common;

import lombok.Data;

@Data
public class DruidAuth {

    private String username="admin";
    private String password="123456";
    private String allow = "";
    private String deny="";
}
