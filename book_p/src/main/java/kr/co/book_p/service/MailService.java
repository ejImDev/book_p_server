package kr.co.book_p.service;

import kr.co.book_p.util.MailSender;
import kr.co.book_p.vo.MailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class MailService {
    private final MailSender mailSender;

    private static final char[] SPECIAL_CHARACTERS = {'!', '@', '#', '$', '%', '^', '&', '*', '(', ')'};
    private static final char[] NUMERIC_CHARACTERS = {'1', '2', '3', '4', '5', '6', '7', '8', '9', '0'};
    private static final char[] ALL_CHARACTERS = {'1', '2', '3', '4', '5', '6', '7', '8', '9', '0',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            '!', '@', '#', '$', '%', '^', '&', '*', '(', ')'};

    //난수 발생(회원가입 인증번호)
    public String generateAuthNo(int num) {
        Random rand = new Random();
        String authKey = "";

        for (int i = 0; i < num; i++) {
            String random = Integer.toString(rand.nextInt(10));
            authKey += random;
        }
        return authKey;
    }

    //난수+문자열 발생(임시비밀번호)
    public String getRamdomPassword() {
        return getRandPw(1, SPECIAL_CHARACTERS) + getRandPw(8, ALL_CHARACTERS) + getRandPw(1, NUMERIC_CHARACTERS);
    }

    private String getRandPw(int size, char[] pwCollection) {
        StringBuilder ranPw = new StringBuilder(size);
        SecureRandom secureRandom = new SecureRandom();
        for (int i = 0; i < size; i++) {
            int selectRandomPw = secureRandom.nextInt(pwCollection.length);
            ranPw.append(pwCollection[selectRandomPw]);
        }
        return ranPw.toString();
    }

    @Async
    public void sendMail(MailVO mailSendVO, Object o, int _mFTyp) throws Exception {

        /* 메일폼 디자인 완성되면 활성화. 현재는 일단 내용만 보냄
        String _data = getMailForm(_mFTyp);
        */

        // 회원가입 인증 메일
        if(_mFTyp==0){

            // 메일폼 디자인 완성되면 활성화. 현재는 일단 내용만 보냄
            // _data = _ebody.replace("${AuthKey}", mailSendVO.getAuthKey());

            try {
                mailSender.sender(mailSendVO.getReceiver(), mailSendVO.getTitle(), mailSendVO.getAuthKey());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 임시비밀번호 메일
        if(_mFTyp==1){
            try {
                mailSender.sender(mailSendVO.getReceiver(), mailSendVO.getTitle(), mailSendVO.getContent());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //메일폼 만들어야 함
    public String getMailForm(Integer _stat) throws Exception {
        // 인증메일
        // 임시 비밀번호
        return "";
    }

}
