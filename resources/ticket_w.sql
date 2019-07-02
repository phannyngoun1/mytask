/*
SQLyog Community v13.1.1 (64 bit)
MySQL - 5.7.24 : Database - ticket_w
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`ticket_w` /*!40100 DEFAULT CHARACTER SET latin1 */;

USE `ticket_w`;

/*Table structure for table `journal` */

DROP TABLE IF EXISTS `journal`;

CREATE TABLE `journal` (
  `ordering` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `persistence_id` varchar(255) NOT NULL,
  `sequence_number` bigint(20) NOT NULL,
  `deleted` tinyint(1) DEFAULT '0',
  `tags` varchar(255) DEFAULT NULL,
  `message` blob NOT NULL,
  PRIMARY KEY (`persistence_id`,`sequence_number`),
  UNIQUE KEY `ordering` (`ordering`)
) ENGINE=InnoDB AUTO_INCREMENT=46 DEFAULT CHARSET=latin1;

/*Table structure for table `snapshot` */

DROP TABLE IF EXISTS `snapshot`;

CREATE TABLE `snapshot` (
  `persistence_id` varchar(255) NOT NULL,
  `sequence_number` bigint(20) NOT NULL,
  `created` bigint(20) NOT NULL,
  `snapshot` blob NOT NULL,
  PRIMARY KEY (`persistence_id`,`sequence_number`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
