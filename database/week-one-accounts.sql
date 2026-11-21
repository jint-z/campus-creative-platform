USE campus_creative_dev;

CREATE TABLE IF NOT EXISTS accounts (
  id BIGINT NOT NULL AUTO_INCREMENT,
  username VARCHAR(32) CHARACTER SET ascii COLLATE ascii_general_ci NOT NULL,
  password_hash CHAR(60) CHARACTER SET ascii NOT NULL,
  role ENUM('STUDENT', 'ADMIN') NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uq_accounts_username (username)
) ENGINE=InnoDB;
