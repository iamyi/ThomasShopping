-- MySQL dump 10.13  Distrib 5.7.19, for macos10.12 (x86_64)
--
-- Host: localhost    Database: banksystem
-- ------------------------------------------------------
-- Server version	5.7.19

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `debit_cards`
--

DROP TABLE IF EXISTS `debit_cards`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `debit_cards` (
  `account` varchar(10) DEFAULT NULL,
  `name` varchar(20) DEFAULT NULL,
  `id_card` varchar(20) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `password` varchar(6) DEFAULT NULL,
  `e_bank` tinyint(1) DEFAULT NULL,
  `loss` tinyint(1) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `debit_cards`
--

LOCK TABLES `debit_cards` WRITE;
/*!40000 ALTER TABLE `debit_cards` DISABLE KEYS */;
/*!40000 ALTER TABLE `debit_cards` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `debit_sub_accounts`
--

DROP TABLE IF EXISTS `debit_sub_accounts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `debit_sub_accounts` (
  `account` varchar(10) DEFAULT NULL,
  `sub_account` varchar(5) DEFAULT NULL,
  `money` decimal(22,2) DEFAULT NULL,
  `interest` decimal(22,2) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `debit_sub_accounts`
--

LOCK TABLES `debit_sub_accounts` WRITE;
/*!40000 ALTER TABLE `debit_sub_accounts` DISABLE KEYS */;
/*!40000 ALTER TABLE `debit_sub_accounts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `debit_sub_transcations`
--

DROP TABLE IF EXISTS `debit_sub_transcations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `debit_sub_transcations` (
  `account` varchar(10) DEFAULT NULL,
  `sub_account` varchar(5) DEFAULT NULL,
  `money` decimal(22,2) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `debit_sub_transcations`
--

LOCK TABLES `debit_sub_transcations` WRITE;
/*!40000 ALTER TABLE `debit_sub_transcations` DISABLE KEYS */;
/*!40000 ALTER TABLE `debit_sub_transcations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `interest_rates`
--

DROP TABLE IF EXISTS `interest_rates`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `interest_rates` (
  `rate` decimal(5,4) DEFAULT NULL,
  `type` varchar(1) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `interest_rates`
--

LOCK TABLES `interest_rates` WRITE;
/*!40000 ALTER TABLE `interest_rates` DISABLE KEYS */;
/*!40000 ALTER TABLE `interest_rates` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tmp`
--

DROP TABLE IF EXISTS `tmp`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tmp` (
  `t` varchar(20) NOT NULL,
  UNIQUE KEY `t` (`t`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tmp`
--

LOCK TABLES `tmp` WRITE;
/*!40000 ALTER TABLE `tmp` DISABLE KEYS */;
/*!40000 ALTER TABLE `tmp` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `users` (
  `account` varchar(5) DEFAULT NULL,
  `password` varchar(100) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES ('50001','$2a$10$6pY4v5lar5Yh2yQDxPxyDuBs4yURhzjiDixpLHDJYVcM7KQ1n2gL.','2018-05-29 00:00:00'),('10000','$2a$10$..GbL7bPlcs2nj8g0kjm9e12F/cSwyOgfE.ULZzD1v2zxnBTftHP2','2018-06-08 03:43:22'),('10001','$2a$10$s36wLQx9cjG.8LqjklirK.XiO6cTb9BeRzfpmoyB0/lEjS3NcSJzq','2018-06-08 03:44:55'),('10002','$2a$10$V3xqk7t6gAJXhwvVFErJH.lLNVuSMC3bkWAX3VPp6el79yP3/yehK','2018-06-08 03:45:42'),('30000','$2a$10$IRsUBXfXM1udKCOqAHb4OeCSeNS4NDrQ1y9YgZY5tumAlz5zXNfxy','2018-06-08 03:46:11'),('40000','$2a$10$XtPCn.4PjfPUn9K557QBB.FG1ycoj.7KjgRVXdsK.LVjYDKztmjs6','2018-06-08 03:46:14'),('20000','$2a$10$tnkyiZWeU4OBV9uZuwSY/OxLv2CZWy59./lHISGmeBE6gop7M9tZ.','2018-06-08 03:46:25'),('10003','$2a$10$t.8pCIRX6rsetok13XTqju/LjRP/uvWHhuTBHv7KiYIwu8.sidwki','2018-06-09 10:47:49'),('10004','$2a$10$NWQlkd0kTWypXgUGYa3HkeI/QaQqqzf20g5nIiQeIpuh.I2VI2rla','2018-06-09 01:52:28');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2018-06-09 17:12:23
