DROP TABLE test_table;
CREATE TABLE test_table (
    seq        INT NOT NULL AUTO_INCREMENT,
    test_id     VARCHAR(20),
    test_date    VARCHAR(50) DEFAULT (current_date),
    PRIMARY KEY(seq)
) ENGINE=MYISAM CHARSET=utf8;

DROP TABLE P_USER;
CREATE TABLE `P_USER` (
    `idx_user` int(11) NOT NULL AUTO_INCREMENT COMMENT '인덱스',
    `user_email` varchar(255) NOT NULL COMMENT '회원 이메일',
    `user_pw` varchar(100) NOT NULL COMMENT '회원 비밀번호 (한글, 숫자, 영어, 특수기호 포함 8~12글자)',
    `user_name` varchar(100) NOT NULL COMMENT '이름',
    `user_phone` varchar(20) NOT NULL COMMENT '전화번호',
    `user_type` tinyint(4) DEFAULT 0 COMMENT '회원 권한 (0:임시회원, 1:정회원, 2:탈퇴회원, 99:관리자)',
    `confirm_code` varchar(100) DEFAULT NULL COMMENT '회원가입 인증 코드',
    `create_dt` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '가입일시',
    `update_dt` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일시',
    PRIMARY KEY (`idx_user`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='회원 정보';

DROP TABLE P_USER_HISTORY;
CREATE TABLE `P_USER_HISTORY` (
    `idx_login` int(11) NOT NULL AUTO_INCREMENT COMMENT '인덱스',
    `idx_user` int(11) NOT NULL COMMENT '회원 인덱스',
    `user_type` tinyint(4) NOT NULL COMMENT '회원 권한 (0:임시회원, 1:정회원, 2:탈퇴회원, 99:관리자)',
    `remoteIP` datetime NOT NULL COMMENT '접속 정보',
    `create_dt` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '로그인 일시',
    PRIMARY KEY (`idx_login`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='로그인 정보';