package kr.co.book_p.controller;

import kr.co.book_p.mapper.TestMapper;
import kr.co.book_p.model.CommonResult;
import kr.co.book_p.security.UserPrincipal;
import kr.co.book_p.service.ResponseService;
import kr.co.book_p.vo.TestVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final ResponseService responseService;
    private final TestMapper testMapper;

    @GetMapping("/")
    public String index() {
        return "Hello";
    }

    @GetMapping("/secureTest")
    public String secureTest(@AuthenticationPrincipal UserPrincipal userPrincipal)
    {
        return "logged idx : "+userPrincipal.getUserIdx() + ", userID + "+userPrincipal.getUserEmail();
    }

    @PostMapping("/test")
    public CommonResult viewTest () {
        List<TestVO> list = testMapper.getTestList();
        System.out.println("list = " + list);
        return responseService.getSuccessResult(list, "test_list", "테스트 목록을 가져왔습니다.");
    }
}
