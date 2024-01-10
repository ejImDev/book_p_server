CREATE TABLE test_table (
    seq        INT NOT NULL AUTO_INCREMENT,
    test_id     VARCHAR(20),
    test_date    VARCHAR(50) DEFAULT (current_date),
    PRIMARY KEY(seq)
) ENGINE=MYISAM CHARSET=utf8;