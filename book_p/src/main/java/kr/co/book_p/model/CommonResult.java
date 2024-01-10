package kr.co.book_p.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommonResult {
    private String success;
    private String code;
    private String msg;
}