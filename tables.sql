CREATE TABLE IF NOT EXISTS ptl_log(
	id int(11) unsigned NOT NULL AUTO_INCREMENT,
	username varchar(32) NOT NULL,
	date int(11) unsigned NOT NULL,
	time mediumint(11) unsigned NOT NULL,
	vanish int(11) unsigned NOT NULL,
	PRIMARY KEY(id),
	UNIQUE(username, date)
);