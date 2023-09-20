package com.ziumks.hams.controller;

import com.ziumks.hams.service.HamsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.google.gson.Gson;


/**
 * 서치 라이트 연계 API 입니다.
 * 작성자 : 김주현
 * */
@RestController
@Slf4j
@RequestMapping(value = "/hams")
public class HamsController {

    @Autowired
    HamsService hamsService;

    /*
    * Hams Server 연결 요청 컨트롤러 - 20230920_1715_이상민
    */
    @GetMapping("/con")
    public String hamsCon() {
        Gson gson = new Gson();
        ResponseEntity<String> result = hamsService.hamsCon();

        return gson.toJson(result);
    }

    /*
     * Hams Server 장비 제어 컨트롤러 - 20230920_1750_이상민
     */
    @GetMapping("/poc")
    public String hamsPoc(
            @RequestParam String equipment_ID,
            @RequestParam String type
    ) {
        Gson gson = new Gson();
        ResponseEntity<String> result = hamsService.hamsPoc(equipment_ID, type);

        return gson.toJson(result);
    }

}