package gangdong.diet.domain.member.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("test")
public class TestController {

    @GetMapping("/get")
    public String getTest() {
        return "Test response Get";
    }

    @PostMapping("/post")
    public String postTest() {
        return "Test response Post";
    }
}