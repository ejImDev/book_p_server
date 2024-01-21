package kr.co.book_p.mapper;

import kr.co.book_p.vo.UserVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper {
    public UserVO getUserInfoForId(UserVO userVO);

    public UserVO getUserInfoForIdx(UserVO userVO);
    public void saveLoginHistory(UserVO userVO);

    public Integer savUserInfo(UserVO userVo);
    public Integer memberDropOutByIdx(UserVO userVo);

    public Integer modUserInfo(UserVO userVO);

    public Integer updateUserPW(UserVO userInfo);
}
