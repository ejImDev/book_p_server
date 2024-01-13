package kr.co.book_p.service;

import kr.co.book_p.vo.UserVO;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    public Optional<UserVO> findByEmail(String email) {
        UserVO user = new UserVO();
        user.setIdx_user(1);
        user.setUser_email("test@test.com");
        user.setUser_pw("$2a$12$W28VL7Meh9Zlu9a1Ja0qzOKXqVxStXMQmxKbMZcaVHKRiFfPMMvVu");
        user.setUser_type(9);
        if(user.getUser_type()==1) user.setRole("ROLE_USER");
        if(user.getUser_type()==9) user.setRole("ROLE_ADMIN");
        return Optional.of(user);
    }
}
