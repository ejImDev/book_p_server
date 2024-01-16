package kr.co.book_p.mapper;

import kr.co.book_p.vo.MailVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MailMapper {

    public int addEmailLog(MailVO mailSendVO);
    public int addAuthLog(MailVO mailSendVO);

    public MailVO getMailCheck(String receiver);

    public Integer updateAuthState(MailVO paramVo);
}
