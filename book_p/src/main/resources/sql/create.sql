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
    `create_dt` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '가입일시',
    `update_dt` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일시',
    PRIMARY KEY (`idx_user`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='회원 정보';

DROP TABLE P_USER_HISTORY;
CREATE TABLE `P_USER_HISTORY` (
    `idx_login` int(11) NOT NULL AUTO_INCREMENT COMMENT '인덱스',
    `idx_user` int(11) NOT NULL COMMENT '회원 인덱스',
    `user_type` tinyint(4) NOT NULL COMMENT '회원 권한 (0:임시회원, 1:정회원, 2:탈퇴회원, 99:관리자)',
    `remoteIP` varchar(50) NOT NULL COMMENT '접속 정보',
    `create_dt` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '로그인 일시',
    PRIMARY KEY (`idx_login`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='로그인 정보';

DROP TABLE P_MAIL_HISTORY;
CREATE TABLE `P_MAIL_HISTORY` (
    `idx_mail` int(11) NOT NULL AUTO_INCREMENT COMMENT '인덱스',
    `receiver` varchar(255) NOT NULL COMMENT '수신 이메일',
    `mail_type` tinyint(4) NOT NULL COMMENT '메일 타입 (0:인증메일, 1:임시비밀번호 발급...)',
    `mail_title` varchar(255) NOT NULL COMMENT '메일 내용',
    `mail_content` TEXT NOT NULL COMMENT '메일 내용'
    `idx_user` int(11) COMMENT '회원 인덱스',
    `create_dt` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '메일 발송 일시',
    PRIMARY KEY (`idx_mail`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='메일 정보';

DROP TABLE P_USER_AUTH;
CREATE TABLE `P_USER_AUTH` (
    `idx_auth` int(11) NOT NULL AUTO_INCREMENT COMMENT '인덱스',
    `receiver` varchar(255) NOT NULL COMMENT '수신 이메일',
    `authKey` int(6) NOT NULL COMMENT '인증키',
    `auth_type` tinyint(1) NOT NULL DEFAULT 0 COMMENT '인증 타입 (0:인증키발급, 1:인증완료...)',
    `create_dt` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '메일 발송 일시',
    PRIMARY KEY (`idx_auth`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='회원 인증 정보';