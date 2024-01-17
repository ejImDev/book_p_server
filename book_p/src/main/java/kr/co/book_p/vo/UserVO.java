package kr.co.book_p.vo;

import lombok.Data;

import java.util.Date;

@Data
public class UserVO {

    Integer idx_user;
    String user_email;
    String user_pw;
    Integer user_type; // 0:임시회원 1:정회원 2:탈퇴회원 99:관리자
    String role;

    String user_name;
    String user_phone;

    Integer confirm_code; // 인증번호
    String create_dt; // 가입 일시
    String update_dt; // 업데이트 일시

    String remoteIP;
}
