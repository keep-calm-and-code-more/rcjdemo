package com.example.rcjdemo.controller;

import com.example.rcjdemo.service.DemoService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/demo")
public class DemoController {

    final DemoService demoService;

    public DemoController(DemoService demoService){
        this.demoService = demoService;
    }

    @GetMapping("demo1")
    public String demoOne(@RequestParam Map<String,Object> data){
        // ...
        return demoService.demoOne(data);
    }
}
