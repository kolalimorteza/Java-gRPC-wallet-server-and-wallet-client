CREATE DATABASE IF NOT EXISTS wallet;
USE wallet;

DROP TABLE IF EXISTS balance;
CREATE TABLE balance (
id int(11) NOT NULL AUTO_INCREMENT,
user_id int(11) NOT NULL,
amount decimal(15,2) NOT NULL,
currency char(3) NOT NULL,
PRIMARY KEY (id),
UNIQUE KEY idx_user_currency (user_id, currency),
KEY idx_user_id (user_id)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=latin1;
