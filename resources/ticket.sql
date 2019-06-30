/*
SQLyog Community v13.1.2 (64 bit)
MySQL - 5.7.24 : Database - ticket
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`ticket` /*!40100 DEFAULT CHARACTER SET latin1 */;

USE `ticket`;

/*Table structure for table `account` */

DROP TABLE IF EXISTS `account`;

CREATE TABLE `account` (
  `id` varchar(100) NOT NULL,
  `name` varchar(50) NOT NULL,
  `full_name` varchar(250) DEFAULT NULL,
  `sequence_nr` bigint(20) NOT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  `active` bit(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Data for the table `account` */

insert  into `account`(`id`,`name`,`full_name`,`sequence_nr`,`created_at`,`updated_at`,`active`) values 
('94d7eaeb-d8d9-4ebd-b6a2-d324b5e64f9a','Testing','Testing',1,'2019-06-18 15:55:08','2019-06-18 15:55:08','');

/*Table structure for table `flag` */

DROP TABLE IF EXISTS `flag`;

CREATE TABLE `flag` (
  `id` varchar(50) NOT NULL,
  `code` varchar(50) NOT NULL,
  `val` bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Data for the table `flag` */

insert  into `flag`(`id`,`code`,`val`) values 
('1','workflow',7);

/*Table structure for table `item` */

DROP TABLE IF EXISTS `item`;

CREATE TABLE `item` (
  `id` varchar(100) NOT NULL,
  `name` varchar(100) NOT NULL,
  `description` varchar(200) DEFAULT NULL,
  `workflow_id` varchar(100) DEFAULT NULL,
  `sequence_nr` bigint(20) NOT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Data for the table `item` */

insert  into `item`(`id`,`name`,`description`,`workflow_id`,`sequence_nr`,`created_at`,`updated_at`) values 
('99a132ae-b32b-4c43-b22f-ff3fd83aa215','Ticketing','test','42c9ae63-6767-4c1d-9b95-7cf4f71a4035',1,'2019-06-18 16:09:00','2019-06-18 16:09:00');

/*Table structure for table `participant` */

DROP TABLE IF EXISTS `participant`;

CREATE TABLE `participant` (
  `id` varchar(100) NOT NULL,
  `account_id` varchar(100) NOT NULL,
  `team_id` varchar(100) NOT NULL,
  `department_id` varchar(100) NOT NULL,
  `property_id` varchar(100) NOT NULL,
  `sequence_nr` varchar(100) NOT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  `active` bit(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Data for the table `participant` */

insert  into `participant`(`id`,`account_id`,`team_id`,`department_id`,`property_id`,`sequence_nr`,`created_at`,`updated_at`,`active`) values 
('b8275acf-2b4a-4910-9254-072030a07f4d','94d7eaeb-d8d9-4ebd-b6a2-d324b5e64f9a','94d7eaeb-d8d9-4ebd-b6a2-d324b5e64f9a','94d7eaeb-d8d9-4ebd-b6a2-d324b5e64f9a','94d7eaeb-d8d9-4ebd-b6a2-d324b5e64f9a','1','2019-06-18 15:55:30','2019-06-18 15:55:30','');

/*Table structure for table `process_instance` */

DROP TABLE IF EXISTS `process_instance`;

CREATE TABLE `process_instance` (
  `id` varchar(100) NOT NULL,
  `folio` varchar(50) NOT NULL,
  `sequence_nr` bigint(20) NOT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Data for the table `process_instance` */

/*Table structure for table `workflow` */

DROP TABLE IF EXISTS `workflow`;

CREATE TABLE `workflow` (
  `id` varchar(100) NOT NULL,
  `name` varchar(100) NOT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  `active` bit(1) NOT NULL,
  `sequence_nr` bigint(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Data for the table `workflow` */

insert  into `workflow`(`id`,`name`,`created_at`,`updated_at`,`active`,`sequence_nr`) values 
('42c9ae63-6767-4c1d-9b95-7cf4f71a4035','Ticket_xxx1','2019-06-18 15:56:55','2019-06-18 15:56:55','',1),
('4cc499e6-5d87-46be-bb17-f743717a768f','Ticket','2019-06-18 16:01:49','2019-06-18 16:01:49','',1),
('4d693b02-2757-4a25-9ba2-e830eb681f43','Ticket','2019-06-18 16:01:48','2019-06-18 16:01:48','',1);

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
