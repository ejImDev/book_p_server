package kr.co.book_p.controller;

import jakarta.servlet.http.HttpServletRequest;
import kr.co.book_p.mapper.MailMapper;
import kr.co.book_p.mapper.UserMapper;
import kr.co.book_p.model.CommonResult;
import kr.co.book_p.security.JwtIssuer;
import kr.co.book_p.security.UserPrincipal;
import kr.co.book_p.security.UserPrincipalAuthorcationToken;
import kr.co.book_p.service.MailService;
import kr.co.book_p.service.ResponseService;
import kr.co.book_p.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AuthController extends BaseController{

    private final JwtIssuer jwtIssuer;
    private final ResponseService responseService;
    private final AuthenticationManager authenticationManager;

    /**
     * 로그인
     * @param rq
     * @param userVO
     */
    @PostMapping("/auth/login")
    public CommonResult register(@RequestBody @Validated UserVO userVO) throws Exception {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userVO.getUser_email(), userVO.getUser_pw())
        );
        SecurityContextHolder. getContext().setAuthentication(authentication);

        var principal = (UserPrincipal) authentication.getPrincipal();
        List<String> roles = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).toList();

        String _token = jwtIssuer.issue(principal.getUserIdx(), principal.getUserEmail(), roles);
        Map<String, Object> param0 = new HashMap<String, Object>();
        param0.put("token",_token);

        return responseService.getSuccessResult(param0, "login", "로그인 성공");
    }

}
