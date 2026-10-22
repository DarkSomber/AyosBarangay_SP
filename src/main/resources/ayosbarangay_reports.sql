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
-- Table structure for table `reports`
--

DROP TABLE IF EXISTS `reports`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reports` (
  `report_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `description` varchar(2000) NOT NULL,
  `status` enum('PENDING','UNDER_REVIEW','RESOLVED','UNRESOLVABLE') NOT NULL DEFAULT 'PENDING',
  `date_submitted` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `address` varchar(255) NOT NULL,
  `latitude` decimal(9,6) DEFAULT NULL,
  `longitude` decimal(9,6) DEFAULT NULL,
  `reviewed_by` int DEFAULT NULL,
  `review_date` datetime DEFAULT NULL,
  `deleted_at` datetime DEFAULT NULL,
  `deletion_reason` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`report_id`),
  KEY `fk_reports_reviewer` (`reviewed_by`),
  KEY `idx_reports_user` (`user_id`),
  KEY `idx_reports_status` (`status`),
  KEY `idx_reports_submitted` (`date_submitted`),
  KEY `idx_reports_location` (`latitude`,`longitude`),
  KEY `idx_reports_deleted` (`deleted_at`),
  CONSTRAINT `fk_reports_reviewer` FOREIGN KEY (`reviewed_by`) REFERENCES `users` (`user_id`),
  CONSTRAINT `fk_reports_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
  CONSTRAINT `chk_reports_delete` CHECK (((`deleted_at` is null) or (`deletion_reason` is not null))),
  CONSTRAINT `chk_reports_lat` CHECK (((`latitude` is null) or (`latitude` between -(90) and 90))),
  CONSTRAINT `chk_reports_lng` CHECK (((`longitude` is null) or (`longitude` between -(180) and 180)))
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reports`
--

LOCK TABLES `reports` WRITE;
/*!40000 ALTER TABLE `reports` DISABLE KEYS */;
INSERT INTO `reports` VALUES (1,1,'The streetlight near the basketball court has been out for a week.','PENDING','2026-10-08 09:00:00','2026-10-20 02:01:08','Purok 3, Main St.',14.602000,120.988200,NULL,NULL,NULL,NULL),(2,1,'Water pools on the road every time it rains.','UNDER_REVIEW','2026-10-05 14:30:00','2026-10-20 02:01:08','Purok 1, Riverside Rd.',14.602500,120.988200,NULL,NULL,NULL,NULL),(3,1,'Trash has not been picked up since Monday.','UNDER_REVIEW','2026-10-03 08:15:00','2026-10-20 02:01:08','Purok 5, Mabini St.',14.604000,120.992200,NULL,NULL,NULL,NULL),(4,1,'Large pothole near the barangay hall entrance.','RESOLVED','2026-09-28 16:45:00','2026-10-20 02:01:08','Barangay Hall Rd.',14.605500,120.994200,NULL,NULL,NULL,NULL),(5,1,'A group of stray dogs gathers by the school gate.','UNRESOLVABLE','2026-09-25 07:50:00','2026-10-20 02:01:08','Purok 2, School Ave.',14.607000,120.996200,NULL,NULL,NULL,NULL),(6,999999,'Too much pogi guys NOOOOOO','PENDING','2026-10-20 02:48:55','2026-10-20 02:48:55','Tip quezon city',14.625949,121.062235,NULL,NULL,NULL,NULL),(7,999999,'too many manupilators','PENDING','2026-10-20 02:55:29','2026-10-20 02:55:29','tip quezon city',14.625949,121.062235,NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `reports` ENABLE KEYS */;
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
