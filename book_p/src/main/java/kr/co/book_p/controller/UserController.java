package kr.co.book_p.controller;

import jakarta.servlet.http.HttpServletRequest;
import kr.co.book_p.mapper.MailMapper;
import kr.co.book_p.mapper.UserMapper;
import kr.co.book_p.model.CommonResult;
import kr.co.book_p.security.JwtIssuer;
import kr.co.book_p.service.MailService;
import kr.co.book_p.service.ResponseService;
import kr.co.book_p.vo.MailVO;
import kr.co.book_p.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequiredArgsConstructor
public class UserController extends BaseController{

    private final JwtIssuer jwtIssuer;
    private final ResponseService responseService;

    private final UserMapper userMapper;
    private final MailMapper mailMapper;
    private final MailService mailService;

    /**
     * 로그인
     * @param rq
     * @param userVO
     */
    @PostMapping("/auth/login")
    public CommonResult register(HttpServletRequest rq, @RequestBody UserVO userVO) throws Exception {

        if(StringUtils.isEmpty(userVO.getUser_email())){
            return responseService.getFailResult("login", "아이디를 입력해 주세요.");
        }
        if(StringUtils.isEmpty(userVO.getUser_pw())){
            return responseService.getFailResult("login", "비밀번호를 입력해 주세요.");
        }

        UserVO param = userMapper.getUserInfoForId(userVO);
        if(param==null) {
            return responseService.getFailResult("login", "존재하지 않는 회원입니다.");
        }

        if(param.getUser_type()==2) {
            return responseService.getFailResult("login", "탈퇴 된 계정입니다.");
        }

        BCryptPasswordEncoder pwEncoder = new BCryptPasswordEncoder();
        if(pwEncoder.matches(userVO.getUser_pw(), param.getUser_pw())) {
            if(param.getUser_type()==0) {
                return responseService.getFailResult("login", "회원 인증이 진행되지 않은 계정입니다.");
            }
            UserVO rs = new UserVO();
            rs.setIdx_user(param.getIdx_user());
            rs.setUser_type(param.getUser_type());
            rs.setRemoteIP(getClientIP(rq));
            //userMapper.saveLoginHistory(rs);

            if(param.getUser_type()==1) param.setRole("ROLE_USER");
            if(param.getUser_type()==9) param.setRole("ROLE_ADMIN");
            String _token = jwtIssuer.issue(param.getIdx_user(), param.getUser_email(), List.of(param.getRole()));
            Map<String, Object> param0 = new HashMap<String, Object>();
            param0.put("token",_token);

            return responseService.getSuccessResult(param0, "login", "로그인 성공");
        } else {
            return responseService.getFailResult("login", "비밀번호가 일치하지 않습니다.");
        }
    }

    /**
     * 아이디 중복 확인
     * @param userVO
     */
    @PostMapping("/id_dupl_check")
    public CommonResult id_dupl_check(@RequestBody UserVO userVO) throws Exception {

        String regexId = "^[_a-zA-Z0-9-\\.]+@[\\.a-zA-Z0-9-]+\\.[a-zA-Z]+$";
        Matcher matcherId = Pattern.compile(regexId).matcher(userVO.getUser_email());
        if (!matcherId.find()) {
            return responseService.getFailResult("login", "이메일 형식이 올바르지 않습니다.");
        }

        UserVO param = userMapper.getUserInfoForId(userVO);
        if(param!=null){
            return responseService.getFailResult("login", "이미 사용중인 메일입니다.");
        } else {
            return responseService.getSuccessResult("login", "사용 가능한 이메일 입니다.");
        }
    }

    /**
     * 회원가입 - 인증메일 발송
     * @param userVO
     * @return
     * @throws Exception
     */
    @PostMapping("/mail_confirm")
    public CommonResult mail_confirm(@RequestBody UserVO userVO) throws Exception {

        String regexId = "^[_a-zA-Z0-9-\\.]+@[\\.a-zA-Z0-9-]+\\.[a-zA-Z]+$";
        Matcher matcherId = Pattern.compile(regexId).matcher(userVO.getUser_email());
        if (!matcherId.find()) {
            return responseService.getFailResult("login", "이메일 형식이 올바르지 않습니다.");
        }

        UserVO param = userMapper.getUserInfoForId(userVO);
        if(param!=null){
            return responseService.getFailResult("login", "이미 사용중인 메일입니다.");
        }

        if (userVO != null && userVO.getUser_email() != null) {
            String authKey = mailService.generateAuthNo(6);
            String title = "[BOOK_P] 회원가입 이메일 인증";

            MailVO mailSendVO = new MailVO();
            mailSendVO.setReceiver(userVO.getUser_email());
            mailSendVO.setTitle(title);
            mailSendVO.setContent("인증번호 : " + authKey);
            mailSendVO.setAuthKey(authKey);
            mailSendVO.setMail_type(0); // 인증메일

            //mailMapper.addEmailLog(mailSendVO);

            mailSendVO.setReceiver_name(userVO.getUser_name());
            mailService.sendMail(mailSendVO, null, 0);
            return responseService.getSuccessResult("login", "인증 메일을 발송했습니다.");
        } else {
            return responseService.getFailResult("login", "입력 정보를 다시 확인해 주세요.");
        }
    }

}
