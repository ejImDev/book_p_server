package kr.co.book_p.controller;

import jakarta.servlet.http.HttpServletRequest;
import kr.co.book_p.mapper.MailMapper;
import kr.co.book_p.mapper.UserMapper;
import kr.co.book_p.model.CommonResult;
import kr.co.book_p.security.JwtIssuer;
import kr.co.book_p.security.UserPrincipal;
import kr.co.book_p.service.MailService;
import kr.co.book_p.service.ResponseService;
import kr.co.book_p.vo.MailVO;
import kr.co.book_p.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
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
     * @return CommonResult
     * @throws Exception
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
     * @return CommonResult
     * @throws Exception
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
     * @return CommonResult
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

        mailSendVO.setReceiver_name("회원가입 안내메일");
        mailService.sendMail(mailSendVO, null, 0);
        return responseService.getSuccessResult("join", "인증 메일을 발송했습니다.");
    }

    /**
     * 회원가입 - 메일 인증번호 체크
     * @param paramVo
     * @return CommonResult
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
     * @return CommonResult
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

    /**
     * 회원 탈퇴
     * @param userPrincipal
     * @param userVo
     * @return CommonResult
     * @throws Exception
     */
    @PostMapping("/member/del")
    public CommonResult member_del(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody UserVO userVo) throws Exception {

        if(!userPrincipal.getUserIdx().equals(userVo.getIdx_user())){
            return responseService.getFailResult("member_del", "로그인 상태를 다시 확인해주세요.");
        }
        UserVO param = userMapper.getUserInfoForIdx(userVo);
        if(param!=null) {
            if(param.getUser_type()==2) {
                return responseService.getFailResult("member_del", "회원 탈퇴가 이미 진행되었습니다.");
            }
            if(param.getUser_type()==9) {
                return responseService.getFailResult("member_del", "관리자 계정은 삭제가 불가능 합니다.");
            }
            if(userVo.getConfirm_code()==null || userVo.getConfirm_code()!=1){
                return responseService.getFailResult("member_del", "탈퇴 약관에 대한 동의를 진행해주세요.");
            }

            UserVO _r = new UserVO();
            _r.setIdx_user(userPrincipal.getUserIdx());
            _r.setUser_type(2);

            Integer rs = userMapper.memberDropOutByIdx(_r);

            if(rs == 1){
                return responseService.getSuccessResult("member_del", "회원 탈퇴를 마쳤습니다.");
            }
        }
        return responseService.getFailResult("member_del", "입력된 정보를 다시 확인해주세요.");
    }

    /**
     * 회원 정보 수정 (비밀번호 수정 포함)
     * @param userVO
     * @return CommonResult
     */
    @PostMapping("/member/modInfo")
    public CommonResult member_modInfo(@RequestBody UserVO userVO) {

        UserVO userInfo = userMapper.getUserInfoForIdx(userVO);

        if(userInfo!=null){
            if(StringUtils.isEmpty(userVO.getUser_name())){
                return responseService.getFailResult("member_mod","이름을 입력한 후 다시 시도해주세요.");
            }
            if(StringUtils.isEmpty(userVO.getUser_phone())){
                return responseService.getFailResult("member_mod","연락처를 입력한 후 다시 시도해주세요.");
            }

            if (StringUtils.isNotEmpty(userVO.getUser_pw())) {
                if (StringUtils.isEmpty(userVO.getUser_pw_origin())) {
                    return responseService.getFailResult("member_mod","기존 비밀번호를 입력해주세요.");
                }

                BCryptPasswordEncoder passEncoder = new BCryptPasswordEncoder();

                if (passEncoder.matches(userVO.getUser_pw_origin(), userInfo.getUser_pw())) {

                    String regexPw = "(?=.*\\d{1,50})(?=.*[~`!@#$%\\^&*()-+=]{1,50})(?=.*[a-zA-Z]{2,50}).{10,20}$";
                    Matcher matcherPw = Pattern.compile(regexPw).matcher(userVO.getUser_pw());

                    if (userVO.getUser_pw().length() > 12 || userVO.getUser_pw().length() < 8) {
                        return responseService.getFailResult("member_mod","비밀번호는 8~12자 사이로 지정해주세요.");
                    }
                    if (!matcherPw.find()) {
                        return responseService.getFailResult("member_mod","영어, 숫자, 특수문자로 조합된 비밀번호만 사용가능합니다.");
                    }
                    String cg_pw = passEncoder.encode(userVO.getUser_pw());
                    userVO.setUser_pw(cg_pw);
                } else {
                    return responseService.getFailResult("member_mod","기존 비밀번호를 다시 확인해 주세요.");
                }
            } else {
                userVO.setUser_pw(userInfo.getUser_pw());
            }
            Integer rs = userMapper.modUserInfo(userVO);

            if(rs==1){
                return responseService.getSuccessResult("member_mod","회원 정보를 수정하였습니다.");
            }else{
                return responseService.getFailResult("member_mod","데이터를 다시 확인해주세요");
            }

        } else {
            return responseService.getFailResult("member_mod","존재하지 않는 회원입니다.");
        }
    }

    /**
     * 새 비밀번호 발송
     * @param userVO
     * @return CommonResult
     * @throws Exception
     */
    @PostMapping("/auth/chg_pw")
    public CommonResult chg_pw(@RequestBody UserVO userVO) throws Exception {

        UserVO userInfo = userMapper.getUserInfoForId(userVO);

        if(userInfo!=null){
            String tempPW = mailService.getRamdomPassword();
            BCryptPasswordEncoder passEncoder = new BCryptPasswordEncoder();
            String cgPw = passEncoder.encode(tempPW);
            userInfo.setUser_pw(cgPw);

            Integer rs = userMapper.updateUserPW(userInfo);

            if(rs==1){
                MailVO mailSendVO = new MailVO();
                mailSendVO.setReceiver(userInfo.getUser_email());
                mailSendVO.setTitle("[BOOK_P] 임시비밀번호 발급");
                mailSendVO.setContent("새로 발급된 임시비밀번호 입니다. 로그인 후 꼭 비밀번호 변경을 해 주세요. : " + tempPW);
                mailService.sendMail(mailSendVO, null, 1); // 1: 임시비밀번호 발급
                return responseService.getSuccessResult("findPw","임시 비밀번호가 발급되었습니다.");
            }else{
                return responseService.getFailResult("findPw","메일 발송을 실패하였습니다.");
            }

        } else {
            return responseService.getFailResult("findPw","존재하지 않는 회원입니다.");
        }
    }

}
