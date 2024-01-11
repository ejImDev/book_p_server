package kr.co.book_p.vo;

import lombok.Data;

@Data
public class MailVO {
    private Integer idx_mail;
    private Integer mail_type;
    private String receiver;
    private String receiver_name;
    private String sendTime;
    private String title;
    private String content;
    private String authKey;
}