package kr.co.book_p.mapper;

import kr.co.book_p.vo.UserVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper {
    public UserVO getUserInfoForId(UserVO userVO);
    public void saveLoginHistory(UserVO userVO);
}
