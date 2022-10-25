package com.example.rcjdemo.controller;

import com.example.rcjdemo.service.DemoService;
import com.rcjava.client.TranPostClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/demo")
public class DemoController {

    final DemoService demoService;
    final TranPostClient tranPostClient;
    public DemoController(DemoService demoService, TranPostClient tranPostClient){
        this.demoService = demoService;
        this.tranPostClient = tranPostClient;
    }

    @GetMapping("demo1")
    public String demoOne(@RequestParam Map<String,Object> data){
        // ...
        tranPostClient.postSignedTran("somethingtx");
        return demoService.demoOne(data);
    }
}
