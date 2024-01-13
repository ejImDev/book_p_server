package kr.co.book_p.security;

import kr.co.book_p.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = userService.findByEmail(username).orElseThrow();
        return UserPrincipal.builder()
                .userIdx(user.getIdx_user())
                .userEmail(user.getUser_email())
                .authorities(List.of(new SimpleGrantedAuthority(user.getRole())))
                .password(user.getUser_pw())
                .build();
    }
}
