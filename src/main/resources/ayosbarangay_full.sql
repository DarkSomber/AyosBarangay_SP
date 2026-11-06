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
-- Table structure for table `admin_notifications`
--

DROP TABLE IF EXISTS `admin_notifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admin_notifications` (
  `notification_id` int NOT NULL AUTO_INCREMENT,
  `admin_id` int NOT NULL,
  `report_id` int DEFAULT NULL,
  `message` varchar(255) NOT NULL,
  `status` enum('UNREAD','READ','IGNORED') NOT NULL DEFAULT 'UNREAD',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`notification_id`),
  KEY `fk_notification_report` (`report_id`),
  KEY `idx_notification_admin` (`admin_id`,`status`,`created_at`),
  CONSTRAINT `fk_notification_admin` FOREIGN KEY (`admin_id`) REFERENCES `users` (`user_id`),
  CONSTRAINT `fk_notification_report` FOREIGN KEY (`report_id`) REFERENCES `reports` (`report_id`)
) ENGINE=InnoDB AUTO_INCREMENT=76 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admin_notifications`
--

LOCK TABLES `admin_notifications` WRITE;
/*!40000 ALTER TABLE `admin_notifications` DISABLE KEYS */;
/*!40000 ALTER TABLE `admin_notifications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `admin_settings`
--

DROP TABLE IF EXISTS `admin_settings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admin_settings` (
  `user_id` int NOT NULL,
  `new_report_alerts` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`user_id`),
  CONSTRAINT `fk_admin_settings_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admin_settings`
--

LOCK TABLES `admin_settings` WRITE;
/*!40000 ALTER TABLE `admin_settings` DISABLE KEYS */;
/*!40000 ALTER TABLE `admin_settings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai_summaries`
--

DROP TABLE IF EXISTS `ai_summaries`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_summaries` (
  `summary_id` int NOT NULL AUTO_INCREMENT,
  `generated_by` int NOT NULL,
  `summary_content` text NOT NULL,
  `report_count` int NOT NULL,
  `summary_period_start` date NOT NULL,
  `summary_period_end` date NOT NULL,
  `generated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`summary_id`),
  KEY `fk_summaries_admin` (`generated_by`),
  CONSTRAINT `fk_summaries_admin` FOREIGN KEY (`generated_by`) REFERENCES `users` (`user_id`),
  CONSTRAINT `chk_summaries_count` CHECK ((`report_count` >= 0)),
  CONSTRAINT `chk_summaries_dates` CHECK ((`summary_period_end` >= `summary_period_start`))
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_summaries`
--

LOCK TABLES `ai_summaries` WRITE;
/*!40000 ALTER TABLE `ai_summaries` DISABLE KEYS */;
INSERT INTO `ai_summaries` VALUES (3,1000000,'**Overview**  \nSeven reports were filed between 30 Oct 2026 and 2 Nov 2026. They cover safety hazards, road conditions, noise, and lighting. All are currently unverified (status: UNDER_REVIEW or PENDING) and should be investigated before action.\n\n**Recurring issues and areas**  \n- **Rizal Street (near the Elementary School):** exposed live wires (#8) and stray dogs (#12).  \n- **Barangay Hall Road:** streetlight failure (#16 & #15).  \n- **Mabini Street:** large pothole (#9) and clogged canal causing flooding (#10).  \n- **Purok 4, Sampaguita Lane:** nighttime karaoke noise (#14).  \n\n**Most urgent items (cite report numbers)**  \n1. **#8 – Exposed live wires on Rizal St.** – Electrical hazard to children walking under the pole.  \n2. **#10 – Flooding canal on Mabini St.** – Prevents safe passage during rains.  \n3. **#16 (and #15) – Out streetlight at Barangay Hall Road** – Security risk in dark streets.\n\n**Suggested priorities**  \n1. **Electrical Safety:** Inspect and repair the live wires immediately; consider temporary fencing or signage.  \n2. **Flood Prevention:** Clear the clogged canal; install proper drainage if needed.  \n3. **Lighting:** Replace or repair the streetlight at Barangay Hall Road; verify all nearby fixtures.  \n4. **Road Repairs:** Fill the large pothole on Mabini Street after the main hazards are addressed.  \n5. **Public Order:** Survey the karaoke noise issue and coordinate with local authorities to enforce quiet hours.  \n6. **Animal Control:** Address stray dogs near the elementary school through community outreach or temporary relocation.  \n\nThese actions, in the stated order, address the most immediate risks while laying groundwork for longer‑term improvements.',7,'2026-10-30','2026-11-06','2026-11-06 12:15:53'),(4,1000000,'**Overview**  \nThe latest seven reports cover infrastructure hazards, public safety concerns, and nuisance complaints affecting the barangay. Most issues are pending, with a few currently under review. The reports highlight problems on Rizal Street, Mabini Street, Purok 3 and 4, and the Barangay Hall Road.\n\n**Recurring Issues and Areas**  \n- **Rizal Street (near Elementary School)** – live electric wires, stray dogs.  \n- **Barangay Hall Road** – streetlight outages (reported twice).  \n- **Mabini Street** – pothole and clogged canal causing flooding.  \n- **Purok 4** – noise nuisance from loud karaoke.  \n\nThese locations are repeatedly cited, indicating a concentration of concerns in the central and northern parts of the barangay.\n\n**Most Urgent Items**  \n- **#8 (2026‑11‑02)** – exposed live wires sparking near the elementary school gate; risk to children walking underneath.  \n- **#9 (2026‑11‑01)** – large pothole on Mabini Street; two motorcycles already slipped.  \n- **#10 (2026‑10‑30)** – clogged canal on Purok 3; knee‑deep flooding during rain, blocking passage.  \n- **#16 & #15 (2026‑11‑01 & 2026‑10‑30)** – streetlight out in front of Barangay Hall for three days, creating a dark, potentially unsafe night environment.  \n\nThese items involve immediate safety hazards and impede daily activities.\n\n**Suggested Priorities**  \n1. **Electrical Safety** – Inspect and repair the exposed live wires (#8).  \n2. **Road Safety** – Fill the pothole on Mabini Street (#9) and clear the canal at Purok 3 (#10).  \n3. **Lighting** – Restore the streetlight on Barangay Hall Road (#15/#16).  \n4. **Public Health & Order** – Manage stray dogs near the school (#12) and address the noise nuisance on Purok 4 (#14).  \n\nAddressing the first three items will reduce immediate risk and improve safety for residents, especially children. The remaining issues should be scheduled for follow‑up once critical hazards are resolved.',7,'2026-10-30','2026-11-06','2026-11-06 13:53:55');
/*!40000 ALTER TABLE `ai_summaries` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `barangay_info`
--

DROP TABLE IF EXISTS `barangay_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `barangay_info` (
  `id` tinyint NOT NULL,
  `name` varchar(100) DEFAULT NULL,
  `location` varchar(255) DEFAULT NULL,
  `barangay_phone` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `barangay_info`
--

LOCK TABLES `barangay_info` WRITE;
/*!40000 ALTER TABLE `barangay_info` DISABLE KEYS */;
/*!40000 ALTER TABLE `barangay_info` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `report_activities`
--

DROP TABLE IF EXISTS `report_activities`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `report_activities` (
  `activity_id` int NOT NULL AUTO_INCREMENT,
  `report_id` int NOT NULL,
  `action` enum('SUBMITTED','REVIEWED','STATUS_CHANGED','RESOLVED','UNRESOLVABLE','CANCELLED') NOT NULL,
  `old_status` enum('PENDING','UNDER_REVIEW','RESOLVED','UNRESOLVABLE') DEFAULT NULL,
  `new_status` enum('PENDING','UNDER_REVIEW','RESOLVED','UNRESOLVABLE') DEFAULT NULL,
  `performed_by` int DEFAULT NULL,
  `remarks` varchar(2000) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`activity_id`),
  KEY `fk_activities_user` (`performed_by`),
  KEY `idx_activities_report` (`report_id`,`created_at`),
  CONSTRAINT `fk_activities_report` FOREIGN KEY (`report_id`) REFERENCES `reports` (`report_id`),
  CONSTRAINT `fk_activities_user` FOREIGN KEY (`performed_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=93 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `report_activities`
--

LOCK TABLES `report_activities` WRITE;
/*!40000 ALTER TABLE `report_activities` DISABLE KEYS */;
INSERT INTO `report_activities` VALUES (1,1,'SUBMITTED',NULL,'PENDING',1,NULL,'2026-10-08 09:00:00'),(2,2,'SUBMITTED',NULL,'PENDING',1,NULL,'2026-10-05 14:30:00'),(3,3,'SUBMITTED',NULL,'PENDING',1,NULL,'2026-10-03 08:15:00'),(4,4,'SUBMITTED',NULL,'PENDING',1,NULL,'2026-09-28 16:45:00'),(5,5,'SUBMITTED',NULL,'PENDING',1,NULL,'2026-09-25 07:50:00'),(6,8,'SUBMITTED',NULL,'PENDING',999999,NULL,'2026-11-02 01:08:40'),(7,9,'SUBMITTED',NULL,'PENDING',999999,NULL,'2026-11-01 03:08:40'),(8,10,'SUBMITTED',NULL,'PENDING',1,NULL,'2026-10-30 03:08:40'),(9,11,'SUBMITTED',NULL,'PENDING',999999,NULL,'2026-10-29 03:08:40'),(10,12,'SUBMITTED',NULL,'PENDING',1,NULL,'2026-10-31 03:08:40'),(11,13,'SUBMITTED',NULL,'PENDING',999999,NULL,'2026-10-28 03:08:40'),(12,14,'SUBMITTED',NULL,'PENDING',1,NULL,'2026-11-01 03:08:40'),(13,15,'SUBMITTED',NULL,'PENDING',999999,NULL,'2026-10-30 03:08:40'),(14,16,'SUBMITTED',NULL,'PENDING',1,NULL,'2026-11-01 03:08:40'),(15,19,'SUBMITTED',NULL,'PENDING',999999,NULL,'2026-10-27 03:08:40'),(16,20,'SUBMITTED',NULL,'PENDING',1,NULL,'2026-10-28 03:08:40'),(32,19,'RESOLVED','PENDING','RESOLVED',2,'Fixed on site','2026-10-31 03:08:40'),(33,20,'UNRESOLVABLE','PENDING','UNRESOLVABLE',2,'Not a barangay matter','2026-10-30 03:08:40'),(34,8,'REVIEWED','PENDING','UNDER_REVIEW',1000000,'meow','2026-11-03 23:13:30'),(35,2,'REVIEWED','PENDING','UNDER_REVIEW',2,'Assigned to maintenance','2026-10-06 09:00:00'),(36,3,'REVIEWED','PENDING','UNDER_REVIEW',2,'Waiting for garbage collection','2026-10-04 09:00:00'),(37,4,'RESOLVED','PENDING','RESOLVED',2,'Fixed on site','2026-09-30 10:00:00'),(38,5,'UNRESOLVABLE','PENDING','UNRESOLVABLE',2,'Not a barangay matter','2026-09-26 10:00:00'),(39,11,'REVIEWED','PENDING','UNDER_REVIEW',2,'Assigned to sanitation team','2026-11-01 03:08:40'),(47,1,'SUBMITTED',NULL,'PENDING',1,NULL,'2026-10-08 09:00:00'),(48,2,'SUBMITTED',NULL,'PENDING',1,NULL,'2026-10-05 14:30:00'),(49,3,'SUBMITTED',NULL,'PENDING',1,NULL,'2026-10-03 08:15:00'),(50,4,'SUBMITTED',NULL,'PENDING',1,NULL,'2026-09-28 16:45:00'),(51,5,'SUBMITTED',NULL,'PENDING',1,NULL,'2026-09-25 07:50:00'),(52,8,'SUBMITTED',NULL,'PENDING',999999,NULL,'2026-11-02 01:08:40'),(53,9,'SUBMITTED',NULL,'PENDING',999999,NULL,'2026-11-01 03:08:40'),(54,10,'SUBMITTED',NULL,'PENDING',1,NULL,'2026-10-30 03:08:40'),(55,11,'SUBMITTED',NULL,'PENDING',999999,NULL,'2026-10-29 03:08:40'),(56,12,'SUBMITTED',NULL,'PENDING',1,NULL,'2026-10-31 03:08:40'),(57,13,'SUBMITTED',NULL,'PENDING',999999,NULL,'2026-10-28 03:08:40'),(58,14,'SUBMITTED',NULL,'PENDING',1,NULL,'2026-11-01 03:08:40'),(59,15,'SUBMITTED',NULL,'PENDING',999999,NULL,'2026-10-30 03:08:40'),(60,16,'SUBMITTED',NULL,'PENDING',1,NULL,'2026-11-01 03:08:40'),(61,19,'SUBMITTED',NULL,'PENDING',999999,NULL,'2026-10-27 03:08:40'),(62,20,'SUBMITTED',NULL,'PENDING',1,NULL,'2026-10-28 03:08:40'),(78,19,'RESOLVED','PENDING','RESOLVED',2,'Fixed on site','2026-10-31 03:08:40'),(79,20,'UNRESOLVABLE','PENDING','UNRESOLVABLE',2,'Not a barangay matter','2026-10-30 03:08:40'),(80,8,'REVIEWED','PENDING','UNDER_REVIEW',1000000,'meow','2026-11-03 23:13:30'),(81,2,'REVIEWED','PENDING','UNDER_REVIEW',2,'Assigned to maintenance','2026-10-06 09:00:00'),(82,3,'REVIEWED','PENDING','UNDER_REVIEW',2,'Waiting for garbage collection','2026-10-04 09:00:00'),(83,4,'RESOLVED','PENDING','RESOLVED',2,'Fixed on site','2026-09-30 10:00:00'),(84,5,'UNRESOLVABLE','PENDING','UNRESOLVABLE',2,'Not a barangay matter','2026-09-26 10:00:00'),(85,11,'REVIEWED','PENDING','UNDER_REVIEW',2,'Assigned to sanitation team','2026-11-01 03:08:40'),(86,2,'REVIEWED','PENDING','UNDER_REVIEW',2,'Assigned to maintenance','2026-10-06 09:00:00'),(87,3,'REVIEWED','PENDING','UNDER_REVIEW',2,'Waiting for garbage collection','2026-10-04 09:00:00'),(88,4,'RESOLVED','PENDING','RESOLVED',2,'Fixed on site','2026-09-30 10:00:00'),(89,5,'UNRESOLVABLE','PENDING','UNRESOLVABLE',2,'Not a barangay matter','2026-09-26 10:00:00'),(90,11,'REVIEWED','PENDING','UNDER_REVIEW',2,'Assigned to sanitation team','2026-11-01 03:08:40');
/*!40000 ALTER TABLE `report_activities` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `report_history`
--

DROP TABLE IF EXISTS `report_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `report_history` (
  `history_id` int NOT NULL AUTO_INCREMENT,
  `report_id` int NOT NULL,
  `changed_by` int NOT NULL,
  `old_status` enum('PENDING','UNDER_REVIEW','RESOLVED','UNRESOLVABLE') DEFAULT NULL,
  `new_status` enum('PENDING','UNDER_REVIEW','RESOLVED','UNRESOLVABLE') NOT NULL,
  `change_reason` varchar(500) DEFAULT NULL,
  `changed_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`history_id`),
  KEY `fk_history_user` (`changed_by`),
  KEY `idx_history_report` (`report_id`,`changed_at`),
  CONSTRAINT `fk_history_report` FOREIGN KEY (`report_id`) REFERENCES `reports` (`report_id`),
  CONSTRAINT `fk_history_user` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `report_history`
--

LOCK TABLES `report_history` WRITE;
/*!40000 ALTER TABLE `report_history` DISABLE KEYS */;
INSERT INTO `report_history` VALUES (1,19,2,'PENDING','RESOLVED','Fixed on site','2026-10-31 03:08:40'),(2,20,2,'PENDING','UNRESOLVABLE','Not a barangay matter','2026-10-30 03:08:40'),(3,8,1000000,'PENDING','UNDER_REVIEW','meow','2026-11-03 23:13:30'),(4,2,2,'PENDING','UNDER_REVIEW','Assigned to maintenance','2026-10-06 09:00:00'),(5,3,2,'PENDING','UNDER_REVIEW','Waiting for garbage collection','2026-10-04 09:00:00'),(6,4,2,'PENDING','RESOLVED','Fixed on site','2026-09-30 10:00:00'),(7,5,2,'PENDING','UNRESOLVABLE','Not a barangay matter','2026-09-26 10:00:00'),(8,11,2,'PENDING','UNDER_REVIEW','Assigned to sanitation team','2026-11-01 03:08:40'),(9,2,2,'PENDING','UNDER_REVIEW','Assigned to maintenance','2026-10-06 09:00:00'),(10,3,2,'PENDING','UNDER_REVIEW','Waiting for garbage collection','2026-10-04 09:00:00'),(11,4,2,'PENDING','RESOLVED','Fixed on site','2026-09-30 10:00:00'),(12,5,2,'PENDING','UNRESOLVABLE','Not a barangay matter','2026-09-26 10:00:00'),(13,11,2,'PENDING','UNDER_REVIEW','Assigned to sanitation team','2026-11-01 03:08:40');
/*!40000 ALTER TABLE `report_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `report_images`
--

DROP TABLE IF EXISTS `report_images`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `report_images` (
  `image_id` int NOT NULL AUTO_INCREMENT,
  `report_id` int NOT NULL,
  `image_path` varchar(500) NOT NULL,
  `image_type` enum('ORIGINAL','RESOLUTION') NOT NULL,
  `uploaded_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`image_id`),
  KEY `idx_images_report` (`report_id`),
  CONSTRAINT `fk_images_report` FOREIGN KEY (`report_id`) REFERENCES `reports` (`report_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `report_images`
--

LOCK TABLES `report_images` WRITE;
/*!40000 ALTER TABLE `report_images` DISABLE KEYS */;
/*!40000 ALTER TABLE `report_images` ENABLE KEYS */;
UNLOCK TABLES;

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
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reports`
--

LOCK TABLES `reports` WRITE;
/*!40000 ALTER TABLE `reports` DISABLE KEYS */;
INSERT INTO `reports` VALUES (1,1,'The streetlight near the basketball court has been out for a week.','PENDING','2026-10-08 09:00:00','2026-10-20 02:01:08','Purok 3, Main St.',14.602000,120.988200,NULL,NULL,NULL,NULL),(2,1,'Water pools on the road every time it rains.','UNDER_REVIEW','2026-10-05 14:30:00','2026-11-06 00:22:26','Purok 1, Riverside Rd.',14.602500,120.988200,2,'2026-10-06 09:00:00',NULL,NULL),(3,1,'Trash has not been picked up since Monday.','UNDER_REVIEW','2026-10-03 08:15:00','2026-11-06 00:22:26','Purok 5, Mabini St.',14.604000,120.992200,2,'2026-10-04 09:00:00',NULL,NULL),(4,1,'Large pothole near the barangay hall entrance.','RESOLVED','2026-09-28 16:45:00','2026-11-06 00:22:26','Barangay Hall Rd.',14.605500,120.994200,2,'2026-09-30 10:00:00',NULL,NULL),(5,1,'A group of stray dogs gathers by the school gate.','UNRESOLVABLE','2026-09-25 07:50:00','2026-11-06 00:22:26','Purok 2, School Ave.',14.607000,120.996200,2,'2026-09-26 10:00:00',NULL,NULL),(8,999999,'Exposed live wires are sparking on the electric pole beside the elementary school gate after the rain. Children walk under it every morning.','UNDER_REVIEW','2026-11-02 01:08:40','2026-11-03 23:13:30','Rizal St. near Barangay Elementary School (sample)',14.599500,120.984200,1000000,'2026-11-03 23:13:30',NULL,NULL),(9,999999,'Large pothole in the middle of Mabini Street. Two motorcycles already slipped here this week.','PENDING','2026-11-01 03:08:40','2026-11-02 03:08:40','Mabini St. corner Luna St. (sample)',14.599800,120.985000,NULL,NULL,NULL,NULL),(10,1,'The canal is clogged and the street floods knee-deep whenever it rains. Residents cannot pass until the water goes down.','PENDING','2026-10-30 03:08:40','2026-11-06 00:22:27','Purok 3, Mabini St. (sample)',14.600300,120.986000,NULL,NULL,NULL,NULL),(11,999999,'Garbage has not been collected for over a week. Trash bags are piling up and the smell is spreading to nearby houses.','UNDER_REVIEW','2026-10-29 03:08:40','2026-11-06 00:22:27','Purok 2 Market Road (sample)',14.599600,120.984400,2,'2026-11-01 03:08:40',NULL,NULL),(12,1,'Three stray dogs are barking and chasing people near the elementary school in the afternoon. A child was nearly bitten.','PENDING','2026-10-31 03:08:40','2026-11-06 00:22:27','Rizal St. near Barangay Elementary School (sample)',14.599400,120.984100,NULL,NULL,NULL,NULL),(13,999999,'Stagnant water in an empty lot is full of mosquito larvae. Several neighbors already have dengue symptoms.','PENDING','2026-10-28 03:08:40','2026-11-06 00:22:27','Purok 5 vacant lot (sample)',14.601000,120.987000,NULL,NULL,NULL,NULL),(14,1,'Loud karaoke playing every night past midnight, even on weekdays. Neighbors cannot sleep.','PENDING','2026-11-01 03:08:40','2026-11-06 00:22:27','Purok 4 Sampaguita Lane (sample)',14.601500,120.987500,NULL,NULL,NULL,NULL),(15,999999,'The streetlight in front of the barangay hall has been out for three days and the street is very dark at night.','PENDING','2026-10-30 03:08:40','2026-11-06 00:22:27','Barangay Hall Road (sample)',14.602200,120.988000,NULL,NULL,NULL,NULL),(16,1,'The streetlight in front of the barangay hall has been out for three days and the street is very dark at night!','PENDING','2026-11-01 03:08:40','2026-11-06 00:22:27','Barangay Hall Road (sample)',14.602300,120.988100,NULL,NULL,NULL,NULL),(19,999999,'Broken drainage cover on Luna Street is a hazard for pedestrians and bicycles.','RESOLVED','2026-10-27 03:08:40','2026-11-06 00:22:27','Luna St. (sample)',14.598800,120.983500,2,'2026-10-31 03:08:40',NULL,NULL),(20,1,'My neighbor built a fence that blocks my driveway. Please make him remove it.','UNRESOLVABLE','2026-10-28 03:08:40','2026-11-06 00:22:27','Purok 6 (sample)',14.603000,120.989000,2,'2026-10-30 03:08:40',NULL,NULL);
/*!40000 ALTER TABLE `reports` ENABLE KEYS */;
UNLOCK TABLES;

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
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `resolutions`
--

LOCK TABLES `resolutions` WRITE;
/*!40000 ALTER TABLE `resolutions` DISABLE KEYS */;
INSERT INTO `resolutions` VALUES (1,19,2,'Drainage cover replaced by the barangay maintenance crew.','2026-10-31 03:08:40',NULL),(2,20,2,NULL,'2026-10-30 03:08:40','Private property dispute between neighbors; refer to the Lupon Tagapamayapa.'),(3,4,2,'Pothole filled by the barangay maintenance crew.','2026-09-30 10:00:00',NULL),(4,5,2,NULL,'2026-09-26 10:00:00','Stray animals are outside barangay jurisdiction; refer to the city veterinary office.'),(5,4,2,'Pothole filled by the barangay maintenance crew.','2026-09-30 10:00:00',NULL),(6,5,2,NULL,'2026-09-26 10:00:00','Stray animals are outside barangay jurisdiction; refer to the city veterinary office.');
/*!40000 ALTER TABLE `resolutions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_notifications`
--

DROP TABLE IF EXISTS `user_notifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_notifications` (
  `notification_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `report_id` int NOT NULL,
  `message` varchar(255) NOT NULL,
  `status` enum('UNREAD','READ','IGNORED') NOT NULL DEFAULT 'UNREAD',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`notification_id`),
  KEY `fk_user_notif_report` (`report_id`),
  KEY `idx_user_notif` (`user_id`,`status`,`created_at`),
  CONSTRAINT `fk_user_notif_report` FOREIGN KEY (`report_id`) REFERENCES `reports` (`report_id`),
  CONSTRAINT `fk_user_notif_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_notifications`
--

LOCK TABLES `user_notifications` WRITE;
/*!40000 ALTER TABLE `user_notifications` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_notifications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `full_name` varchar(100) NOT NULL,
  `email` varchar(254) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `role` enum('USER','ADMIN') NOT NULL DEFAULT 'USER',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uq_users_email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=1000003 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'Sample Resident','sample.resident@example.test','$2a$10$hcccr8cVnhT0wDCv7nXMT.hqx5ieK.CLQXZm8FoakI6Mk53XIbuQi','USER','2026-10-20 02:01:08','2026-10-20 09:07:42'),(2,'Sample Admin','sample.admin@example.test','$2a$10$hcccr8cVnhT0wDCv7nXMT.hqx5ieK.CLQXZm8FoakI6Mk53XIbuQi','ADMIN','2026-10-20 02:01:08','2026-10-20 09:07:42'),(999999,'Demo Resident','demo.resident@example.test','$2a$10$hcccr8cVnhT0wDCv7nXMT.hqx5ieK.CLQXZm8FoakI6Mk53XIbuQi','USER','2026-10-20 02:01:08','2026-10-20 09:07:42'),(1000000,'Demo Admin','demo.admin@example.test','$2a$10$hcccr8cVnhT0wDCv7nXMT.hqx5ieK.CLQXZm8FoakI6Mk53XIbuQi','ADMIN','2026-10-20 08:32:16','2026-10-20 09:07:42');
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

-- Dump completed on 2026-11-06 15:05:30
