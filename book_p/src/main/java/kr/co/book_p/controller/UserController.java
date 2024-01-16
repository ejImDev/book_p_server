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
import org.springframework.web.bind.annotation.*;

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
            userMapper.saveLoginHistory(rs);

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
    @PostMapping("/auth/id_dupl_check")
    public CommonResult id_dupl_check(@RequestBody UserVO userVO) throws Exception {

        String regexId = "^[_a-zA-Z0-9-\\.]+@[\\.a-zA-Z0-9-]+\\.[a-zA-Z]+$";
        Matcher matcherId = Pattern.compile(regexId).matcher(userVO.getUser_email());
        if (!matcherId.find()) {
            return responseService.getFailResult("join", "이메일 형식이 올바르지 않습니다.");
        }

        UserVO param = userMapper.getUserInfoForId(userVO);
        if(param!=null){
            return responseService.getFailResult("join", "이미 사용중인 메일입니다.");
        } else {
            return responseService.getSuccessResult("join", "사용 가능한 이메일 입니다.");
        }
    }

    /**
     * 회원가입 - 인증메일 발송
     * @param userVO
     * @return
     * @throws Exception
     */
    @PostMapping("/auth/mail_confirm")
    public CommonResult mail_confirm(@RequestBody UserVO userVO) throws Exception {

        if(StringUtils.isEmpty(userVO.getUser_email())){
            return responseService.getFailResult("join", "아이디를 입력해 주세요.");
        }

        String regexId = "^[_a-zA-Z0-9-\\.]+@[\\.a-zA-Z0-9-]+\\.[a-zA-Z]+$";
        Matcher matcherId = Pattern.compile(regexId).matcher(userVO.getUser_email());
        if (!matcherId.find()) {
            return responseService.getFailResult("join", "이메일 형식이 올바르지 않습니다.");
        }

        UserVO param = userMapper.getUserInfoForId(userVO);
        if(param!=null){
            return responseService.getFailResult("join", "가입 내역이 있는 메일입니다.");
        }

        String authKey = mailService.generateAuthNo(6);
        String title = "[BOOK_P] 회원가입 이메일 인증";

        MailVO mailSendVO = new MailVO();
        mailSendVO.setReceiver(userVO.getUser_email());
        mailSendVO.setTitle(title);
        mailSendVO.setContent("인증번호 : " + authKey);
        mailSendVO.setMail_type(0); // 인증메일
        mailMapper.addEmailLog(mailSendVO);
        mailSendVO.setAuthKey(authKey);
        mailMapper.addAuthLog(mailSendVO);

        //mailSendVO.setReceiver_name(userVO.getUser_name());
        //mailService.sendMail(mailSendVO, null, 0);
        return responseService.getSuccessResult("join", "인증 메일을 발송했습니다.");
    }

    /**
     * 회원가입 - 메일 인증번호 체크
     * @param paramVo
     * @return
     * @throws Exception
     */
    @PostMapping("/auth/email_check")
    public CommonResult email_check(@RequestBody MailVO paramVo) {

        MailVO rs = mailMapper.getMailCheck(paramVo.getReceiver());
        if(rs==null){
            return responseService.getFailResult("join", "인증메일 발송을 먼저 진행해주세요.");
        }
        if(rs.getAuth_type()==1){
            return responseService.getSuccessResult("join", "이미 인증이 완료된 메일입니다. 회원가입을 진행해주세요.");
        }

        if(rs.getAuthKey().equals(paramVo.getAuthKey())){
            Integer _rs = mailMapper.updateAuthState(paramVo);
            if(_rs>0){
                return responseService.getSuccessResult("join", "메일 인증을 성공했습니다.");
            }
        } else {
            return responseService.getFailResult("join", "인증번호를 다시 확인해주세요.");
        }

        return responseService.getFailResult("join", "입력 정보를 다시 확인해주세요.");
    }

    /**
     * 회원 가입
     * @param userVo
     * @return
     * @throws Exception
     */
    @PostMapping("/auth/join_user")
    public CommonResult join_mail(@RequestBody UserVO userVo) {

        if(StringUtils.isEmpty(userVo.getUser_email())){
            return responseService.getFailResult("join", "아이디를 입력해 주세요.");
        }
        if(StringUtils.isEmpty(userVo.getUser_pw())){
            return responseService.getFailResult("join", "비밀번호를 입력해 주세요.");
        }
        if(StringUtils.isEmpty(userVo.getUser_phone())){
            return responseService.getFailResult("join", "전화번호를 입력해 주세요.");
        }
        if(StringUtils.isEmpty(userVo.getUser_name())){
            return responseService.getFailResult("join", "성함을 입력해 주세요.");
        }

        String regexId = "^[_a-zA-Z0-9-\\.]+@[\\.a-zA-Z0-9-]+\\.[a-zA-Z]+$";
        Matcher matcherId = Pattern.compile(regexId).matcher(userVo.getUser_email());
        if (!matcherId.find()) {
            return responseService.getFailResult("join", "이메일 형식이 올바르지 않습니다.");
        }

        UserVO param = userMapper.getUserInfoForId(userVo);
        if(param!=null){
            return responseService.getFailResult("join", "가입 내역이 있는 메일입니다.");
        }

        MailVO rs = mailMapper.getMailCheck(userVo.getUser_email());
        if(rs==null || rs.getAuth_type()==0){
            return responseService.getFailResult("join", "메일 인증을 먼저 진행해주세요.");
        }

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String hashedPassword = passwordEncoder.encode(userVo.getUser_pw());
        userVo.setUser_pw(hashedPassword);
        userVo.setUser_type(1);
        Integer _rs = userMapper.savUserInfo(userVo);

        if(_rs>0){
            return responseService.getSuccessResult("join", "회원 가입에 성공했습니다.");
        } else {
            return responseService.getFailResult("join", "입력 데이터를 다시 확인해주세요.");
        }
    }

}
