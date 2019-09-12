/*
 Navicat Premium Data Transfer

 Source Server         : Achieve
 Source Server Type    : MySQL
 Source Server Version : 80013
 Source Host           : localhost:3306
 Source Schema         : cloud

 Target Server Type    : MySQL
 Target Server Version : 80013
 File Encoding         : 65001

 Date: 07/09/2019 21:12:54
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for dir_inf
-- ----------------------------
DROP TABLE IF EXISTS `dir_inf`;
CREATE TABLE `dir_inf`  (
  `dir_id` int(11) NOT NULL AUTO_INCREMENT,
  `dir_name` varchar(512) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `parent_dir` int(11) NULL DEFAULT NULL,
  `dir_user_id` int(11) NOT NULL,
  `dir_path` varchar(1024) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  PRIMARY KEY (`dir_id`) USING BTREE,
  INDEX `dir_user_id`(`dir_user_id`) USING BTREE,
  CONSTRAINT `dir_inf_ibfk_1` FOREIGN KEY (`dir_user_id`) REFERENCES `user_inf` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 23 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of dir_inf
-- ----------------------------
INSERT INTO `dir_inf` VALUES (23, 'qwertyuiop', NULL, 10, '\\');
INSERT INTO `dir_inf` VALUES (24, 'java', 23, 10, '\\qwertyuiop\\');
INSERT INTO `dir_inf` VALUES (25, '多线程', 24, 10, '\\qwertyuiop\\java\\');

-- ----------------------------
-- Table structure for file_inf
-- ----------------------------
DROP TABLE IF EXISTS `file_inf`;
CREATE TABLE `file_inf`  (
  `file_id` int(11) NOT NULL AUTO_INCREMENT,
  `file_name` varchar(512) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `file_size` int(11) NOT NULL,
  `file_type` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `file_upload_time` datetime(0) NOT NULL,
  `file_status` int(11) NOT NULL,
  `file_dir_id` int(11) NOT NULL,
  `file_upload_user_id` int(11) NOT NULL,
  PRIMARY KEY (`file_id`) USING BTREE,
  INDEX `file_upload_user_id`(`file_upload_user_id`) USING BTREE,
  INDEX `file_dir_id`(`file_dir_id`) USING BTREE,
  CONSTRAINT `file_inf_ibfk_2` FOREIGN KEY (`file_upload_user_id`) REFERENCES `user_inf` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `file_inf_ibfk_3` FOREIGN KEY (`file_dir_id`) REFERENCES `dir_inf` (`dir_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 27 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of file_inf
-- ----------------------------
INSERT INTO `file_inf` VALUES (27, '电脑信息.txt', 1, 'txt', '2019-09-07 08:33:00', 0, 24, 10);
INSERT INTO `file_inf` VALUES (28, 'a.png', 545, 'png', '2019-09-07 08:51:11', 0, 24, 10);
INSERT INTO `file_inf` VALUES (29, 'MyBatis预习.wmv', 40297, 'wmv', '2019-09-07 13:09:28', 0, 23, 10);

-- ----------------------------
-- Table structure for user_inf
-- ----------------------------
DROP TABLE IF EXISTS `user_inf`;
CREATE TABLE `user_inf`  (
  `user_id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `password` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `email` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `register_time` datetime(0) NOT NULL,
  `status` int(11) NOT NULL,
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_inf
-- ----------------------------
INSERT INTO `user_inf` VALUES (10, 'qwertyuiop', 'qwertyuiop', 'qwertyuiop', '2019-09-07 08:31:55', 1);

SET FOREIGN_KEY_CHECKS = 1;
