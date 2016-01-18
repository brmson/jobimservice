CREATE DATABASE IF NOT EXISTS DT_wikipedia_stanford DEFAULT CHARSET utf8 COLLATE utf8_bin;
USE DT_wikipedia_stanford;

CREATE TABLE word_count (   word varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '',  count int(11) NOT NULL DEFAULT '0',  KEY w (word)) ENGINE=MyISAM DEFAULT CHARSET=utf8;
CREATE TABLE feature_count (  feature varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '',  count int(11) NOT NULL DEFAULT '0',  KEY f (feature)) ENGINE=MyISAM DEFAULT CHARSET=utf8;
CREATE TABLE LMI_1000 (  word varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '',  feature varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '',  sig double NOT NULL DEFAULT '0',  count int(8) unsigned NOT NULL DEFAULT '0',  KEY wf (word, feature),  KEY f (feature)) ENGINE=MyISAM DEFAULT CHARSET=utf8;
CREATE TABLE LMI_1000_l200 (  word1 varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '',  word2 varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '',  count int(8) unsigned NOT NULL DEFAULT '0',  KEY w1 (word1),  KEY w2 (word2)) ENGINE=MyISAM DEFAULT CHARSET=utf8;
CREATE TABLE LMI_1000_feature (  feature varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '',  word varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '',  sig double NOT NULL DEFAULT '0',  count int(8) unsigned NOT NULL DEFAULT '0',  KEY fw (feature, word),  KEY w (word)) ENGINE=MyISAM DEFAULT CHARSET=utf8;
CREATE TABLE LMI_1000_feature_l200 (  feature1 varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '',  feature2 varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '',  count int(8) unsigned NOT NULL DEFAULT '0',  KEY f1 (feature1),  KEY f2 (feature2)) ENGINE=MyISAM DEFAULT CHARSET=utf8;
CREATE TABLE LMI_1000_l200_sense_cluster (  word varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '',  cid int(3) NOT NULL DEFAULT '0',  cluster text,  isas text,  KEY w (word)) ENGINE=MyISAM DEFAULT CHARSET=utf8;
LOAD DATA LOCAL INFILE 'wikipedia_stanford/wikipedia_stanford_word_count' INTO TABLE word_count;
LOAD DATA LOCAL INFILE 'wikipedia_stanford/wikipedia_stanford_feature_count' INTO TABLE feature_count;
LOAD DATA LOCAL INFILE 'wikipedia_stanford/wikipedia_stanford_LMI_s0.0_w2_f2_wf0_wpfmax1000_wpfmin2_p1000' INTO TABLE LMI_1000;
LOAD DATA LOCAL INFILE 'wikipedia_stanford/wikipedia_stanford_LMI_s0.0_w2_f2_wf0_wpfmax1000_wpfmin2_p1000_simsortlimit200' INTO TABLE LMI_1000_l200;
LOAD DATA LOCAL INFILE 'wikipedia_stanford/wikipedia_stanford_BIM_LMI_s0.0_w2_f2_wf0_wpfmax1000_wpfmin2_p1000' INTO TABLE LMI_1000_feature;
LOAD DATA LOCAL INFILE 'wikipedia_stanford/wikipedia_stanford_BIM_LMI_s0.0_w2_f2_wf0_wpfmax1000_wpfmin2_p1000_simsortlimit200' INTO TABLE LMI_1000_feature_l200;
LOAD DATA LOCAL INFILE 'wikipedia_stanford/wikipedia_stanford_sense_cluster' INTO TABLE LMI_1000_l200_sense_cluster;
