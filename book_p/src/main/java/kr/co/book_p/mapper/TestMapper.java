package kr.co.book_p.mapper;

import kr.co.book_p.vo.TestVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TestMapper {

    public List<TestVO> getTestList();
}
