CREATE TABLE IF NOT EXISTS ptl_log(
	id			int(11)		NOT NULL AUTO_INCREMENT,
	username	varchar(32)	NOT NULL,
	day			int(4)		NOT NULL,
	month		int(2)		NOT NULL,
	year		int(11)		NOT NULL,
	time		int(11)		NOT NULL,
	vanish		int(11)		NOT NULL,
	PRIMARY KEY(id),
	UNIQUE(username, day, month, year)
);