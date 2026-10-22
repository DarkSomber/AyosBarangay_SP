CREATE DATABASE  IF NOT EXISTS `ayosbarangay` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `ayosbarangay`;
-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: ayosbarangay
-- ------------------------------------------------------
-- Server version	8.0.45

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `resolutions`
--

DROP TABLE IF EXISTS `resolutions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `resolutions` (
  `resolution_id` int NOT NULL AUTO_INCREMENT,
  `report_id` int NOT NULL,
  `admin_id` int NOT NULL,
  `resolution_description` varchar(2000) DEFAULT NULL,
  `resolution_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `unresolvable_reason` varchar(2000) DEFAULT NULL,
  PRIMARY KEY (`resolution_id`),
  KEY `fk_resolutions_admin` (`admin_id`),
  KEY `idx_resolutions_report` (`report_id`),
  CONSTRAINT `fk_resolutions_admin` FOREIGN KEY (`admin_id`) REFERENCES `users` (`user_id`),
  CONSTRAINT `fk_resolutions_report` FOREIGN KEY (`report_id`) REFERENCES `reports` (`report_id`),
  CONSTRAINT `chk_resolutions_outcome` CHECK ((((`resolution_description` is not null) and (`unresolvable_reason` is null)) or ((`resolution_description` is null) and (`unresolvable_reason` is not null))))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `resolutions`
--

LOCK TABLES `resolutions` WRITE;
/*!40000 ALTER TABLE `resolutions` DISABLE KEYS */;
/*!40000 ALTER TABLE `resolutions` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-22 21:46:37
