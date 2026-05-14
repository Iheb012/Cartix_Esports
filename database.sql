SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";

CREATE DATABASE IF NOT EXISTS `mydatabase`;
USE `mydatabase`;

CREATE TABLE IF NOT EXISTS `administration` (
  `niveauAcces` int(11) NOT NULL,
  `token` varchar(255) NOT NULL,
  `sessionActive` int(1) NOT NULL,
  `tentativesEchouees` int(11) NOT NULL,
  `dateExpiration` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `bracket` (
  `Id` int(11) NOT NULL,
  `tournoiId` int(11) NOT NULL,
  `type` varchar(255) NOT NULL,
  `nbRounds` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `classementelo` (
  `joueurId` int(11) NOT NULL,
  `Score` int(11) NOT NULL,
  `Rang` int(11) NOT NULL,
  `derniereMAJ` date NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `commande` (
  `Id` int(11) NOT NULL AUTO_INCREMENT,
  `iduser` int(11) NOT NULL,
  `dateCommande` date NOT NULL,
  `montantTotal` float NOT NULL,
  `Statut` varchar(255) NOT NULL,
  `AdresseLivraison` varchar(255) NOT NULL,
  PRIMARY KEY (`Id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `commentaire` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `auteurId` int(11) NOT NULL,
  `contenue` varchar(2500) NOT NULL,
  `dateCreation` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `equipe` (
  `Id` int(11) NOT NULL,
  `nom` varchar(255) NOT NULL,
  `capitaineId` int(11) NOT NULL,
  `nbMembres` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `evenement` (
  `Id` int(11) NOT NULL,
  `nom` varchar(255) NOT NULL,
  `Type` int(11) NOT NULL,
  `dateDebut` date NOT NULL,
  `dateFin` date NOT NULL,
  `lieu` varchar(255) NOT NULL,
  `BudgetTital` float NOT NULL,
  `SponsorId` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `inscription` (
  `Id` int(11) NOT NULL AUTO_INCREMENT,
  `equipeId` int(11) NOT NULL,
  `tournoiId` int(11) NOT NULL,
  `DateInscri` int(11) NOT NULL,
  PRIMARY KEY (`Id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `joueur` (
  `pseudo` varchar(255) NOT NULL,
  `niveauELO` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `match` (
  `Id` int(11) NOT NULL,
  `equipe1Id` int(11) NOT NULL,
  `equipe2Id` int(11) NOT NULL,
  `dateMatch` date NOT NULL,
  `Statut` varchar(255) NOT NULL,
  `tournoiId` int(11) NOT NULL,
  `Recompense` varchar(255) NOT NULL,
  `DateObtentionRecompense` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `notification` (
  `Id` int(11) NOT NULL,
  `UserId` int(11) NOT NULL,
  `message` varchar(255) NOT NULL,
  `lue` tinyint(1) NOT NULL,
  `date` date NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `organisateur` (
  `organisation` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `panier` (
  `Id` int(11) NOT NULL,
  `iduser` int(11) NOT NULL,
  `dateCreaction` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `post` (
  `Id` int(11) NOT NULL,
  `auteurId` int(11) NOT NULL,
  `Titre` varchar(255) NOT NULL,
  `Contenue` varchar(255) NOT NULL,
  `Type` varchar(255) NOT NULL,
  `DatePub` date NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `pro` (
  `Id` int(11) NOT NULL AUTO_INCREMENT,
  `nom` varchar(255) NOT NULL,
  `description` varchar(255) NOT NULL,
  `prix` float NOT NULL,
  `stock` int(11) NOT NULL,
  `categorie` varchar(255) NOT NULL,
  `imageUrl` varchar(500) DEFAULT '',
  `note` decimal(2,1) DEFAULT 0.0,
  `nbAvis` int(11) DEFAULT 0,
  PRIMARY KEY (`Id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `user` (
  `Id` int(11) NOT NULL AUTO_INCREMENT,
  `nom` varchar(255) NOT NULL,
  `prenom` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `mdp` varchar(255) NOT NULL,
  `role` varchar(50) NOT NULL,
  PRIMARY KEY (`Id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO `user` (nom, prenom, email, mdp, role) VALUES ('Joueur', 'Test', 'joueur@esport.com', '', 'member');
INSERT INTO `pro` (nom, description, prix, stock, categorie, imageUrl, note, nbAvis) VALUES
('Razer DeathAdder V2', 'Souris gaming haute performance', 250, 50, 'Souris', 'razer_deathadder_v2.png', 4.8, 128),
('Corsair K70 RGB', 'Clavier mécanique gaming', 700, 30, 'Clavier', 'corsair_k70_rgb.png', 4.5, 95),
('Logitech G Pro X', 'Casque audio gaming', 650, 25, 'Audio', 'logitech_g_pro_x.png', 4.2, 67),
('SteelSeries QcK', 'Tapis de souris premium', 120, 100, 'Accessoires', 'steelseries_qck.png', 4.6, 210),
('NVIDIA RTX 4070', 'Carte graphique haut de gamme', 3000, 10, 'Composants', 'nvidia_rtx_4070.png', 4.9, 45),
('AMD Ryzen 7 7800X3D', 'Processeur gaming', 2100, 15, 'Composants', 'amd_ryzen_7_7800x3d.png', 4.7, 83),
('ASUS ROG Strix G16', 'PC portable gaming i9-13980HX RTX 4070', 8500, 8, 'PC', 'asus_rog_strix_g16.png', 4.6, 312),
('HyperX Cloud Alpha', 'Casque audio surround 7.1', 450, 45, 'Audio', 'hyperx_cloud_alpha.png', 4.4, 523),
('Logitech G502 Hero', 'Souris gaming 25000 DPI', 280, 60, 'Souris', 'logitech_g502_hero.png', 4.7, 890),
('MSI MAG274QRF-QD', 'Ecran 27\"\" 165Hz IPS 2K', 1900, 12, 'Ecran', 'msi_mag274qrf_qd.png', 4.5, 234),
('Samsung 990 Pro 2TB', 'SSD NVMe M.2 7450MB/s', 950, 35, 'Composants', 'samsung_990_pro.png', 4.8, 167),
('Corsair Vengeance 32GB', 'RAM DDR5 5600MHz CL36', 650, 40, 'Composants', 'corsair_vengeance_32gb.png', 4.3, 445),
('Razer Kraken Kitty', 'Casque gaming RGB oreilles de chat', 700, 20, 'Audio', 'razer_kraken_kitty.png', 4.1, 378),
('SteelSeries Apex Pro', 'Clavier mecanique switches ajustables', 1050, 18, 'Clavier', 'steelseries_apex_pro.png', 4.6, 612),
('Logitech C920 Pro', 'Webcam Full HD 1080p', 450, 55, 'Accessoires', 'logitech_c920_pro.png', 4.2, 1200),
('NZXT H7 Flow', 'Boitier PC moyen tour blanc', 750, 22, 'Composants', 'nzxt_h7_flow.png', 4.7, 189),
('Corsair RM850x', 'Alimentation 850W 80+ Gold', 700, 28, 'Composants', 'corsair_rm850x.png', 4.8, 256),
('Razer Viper Ultimate', 'Souris sans fil 20000 DPI', 550, 33, 'Souris', 'razer_viper_ultimate.png', 4.5, 734),
('LG 27GP950-B', 'Ecran 27\"\" 4K 160Hz HDMI 2.1', 2800, 7, 'Ecran', 'lg_27gp950.png', 4.6, 145),
('AMD Ryzen 9 7950X', 'Processeur 16 coeurs 5.7GHz', 3000, 9, 'Composants', 'amd_ryzen_9_7950x.png', 4.7, 312),
('Intel Core i9-14900K', 'Processeur 24 coeurs 6.0GHz', 3200, 6, 'Composants', 'intel_core_i9_14900k.png', 4.5, 287),
('Deepcool AK620', 'Ventilateur CPU double tour', 350, 42, 'Composants', 'deepcool_ak620.png', 4.6, 198),
('Thrustmaster T300', 'Volant gaming force feedback', 2400, 5, 'Accessoires', 'thrustmaster_t300.png', 4.4, 89),
('Razer BlackWidow V4', 'Clavier mecanique 75%', 850, 25, 'Clavier', 'razer_blackwidow_v4.png', 4.3, 456),
('WD Black SN850X 1TB', 'SSD NVMe M.2 7300MB/s', 550, 48, 'Composants', 'wd_black_sn850x.png', 4.7, 321),
('Corsair K100 RGB', 'Clavier mecanique optique', 1250, 15, 'Clavier', 'corsair_k100_rgb.png', 4.4, 234);

COMMIT;

-- Composants PC pour le configurateur
INSERT INTO `pro` (nom, description, prix, stock, categorie, imageUrl, note, nbAvis) VALUES
('Intel Core i5-13600KF', 'cpu', 299.00, 15, 'Composant', '', 4.5, 120),
('Intel Core i7-13700K', 'cpu', 449.00, 10, 'Composant', '', 4.7, 85),
('AMD Ryzen 5 7600X', 'cpu', 279.00, 20, 'Composant', '', 4.4, 95),
('AMD Ryzen 7 7800X3D', 'cpu', 499.00, 8, 'Composant', '', 4.8, 150),
('NVIDIA RTX 4060', 'gpu', 329.00, 12, 'Composant', '', 4.3, 200),
('NVIDIA RTX 4070', 'gpu', 549.00, 8, 'Composant', '', 4.6, 140),
('AMD Radeon RX 7800 XT', 'gpu', 499.00, 10, 'Composant', '', 4.5, 90),
('NVIDIA RTX 4080', 'gpu', 899.00, 5, 'Composant', '', 4.8, 110),
('Corsair Vengeance 32GB DDR5', 'ram', 119.00, 25, 'Composant', '', 4.4, 180),
('G.Skill Trident Z5 64GB DDR5', 'ram', 219.00, 15, 'Composant', '', 4.6, 70),
('Crucial 16GB DDR5', 'ram', 69.00, 30, 'Composant', '', 4.2, 95),
('Samsung 980 Pro 1TB NVMe', 'ssd', 79.00, 20, 'Composant', '', 4.5, 210),
('WD Black SN850X 2TB NVMe', 'ssd', 149.00, 12, 'Composant', '', 4.7, 130),
('Crucial P3 Plus 500GB NVMe', 'ssd', 49.00, 25, 'Composant', '', 4.3, 85),
('MSI MAG B760 Tomahawk', 'motherboard', 124.00, 15, 'Composant', '', 4.4, 100),
('ASUS ROG Strix Z790-E', 'motherboard', 249.00, 8, 'Composant', '', 4.6, 75),
('Gigabyte B650 AORUS Elite', 'motherboard', 139.00, 12, 'Composant', '', 4.3, 65);

-- Migration for existing databases: add note/nbAvis columns if missing
SET @db = (SELECT DATABASE());
SET @exist_note = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='pro' AND COLUMN_NAME='note');
SET @sql_note = IF(@exist_note = 0, 'ALTER TABLE pro ADD COLUMN note decimal(2,1) DEFAULT 0.0', 'SELECT 1');
PREPARE stmt_note FROM @sql_note;
EXECUTE stmt_note;
DEALLOCATE PREPARE stmt_note;

SET @exist_nbavis = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='pro' AND COLUMN_NAME='nbAvis');
SET @sql_nbavis = IF(@exist_nbavis = 0, 'ALTER TABLE pro ADD COLUMN nbAvis int(11) DEFAULT 0', 'SELECT 1');
PREPARE stmt_nbavis FROM @sql_nbavis;
EXECUTE stmt_nbavis;
DEALLOCATE PREPARE stmt_nbavis;
