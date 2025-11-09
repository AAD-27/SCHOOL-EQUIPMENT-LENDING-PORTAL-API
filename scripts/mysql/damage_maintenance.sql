-- MySQL DDL for Damage / Maintenance feature (delete-on-damage, recreate-on-repair)
-- Adjust database name as needed; assumes existing `equipment` table

-- Optional: you can keep this column if you also want soft-hiding without delete
-- ALTER TABLE `equipment`
--   ADD COLUMN IF NOT EXISTS `under_maintenance` TINYINT(1) NOT NULL DEFAULT 0;

-- Create damage_reports table with snapshot columns (no FK to equipment)
CREATE TABLE IF NOT EXISTS `damage_reports` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `description` VARCHAR(1000) NOT NULL,
  `status` VARCHAR(64) NOT NULL,
  `created_at` DATETIME(6) NOT NULL,
  `updated_at` DATETIME(6) NOT NULL,
  -- snapshot of original equipment
  `original_equipment_id` BIGINT NULL,
  `equipment_name` VARCHAR(255) NULL,
  `equipment_category` VARCHAR(255) NULL,
  `equipment_condition_description` VARCHAR(255) NULL,
  `equipment_quantity` INT NULL,
  `equipment_available_quantity` INT NULL,
  -- id of equipment row after repair recreation
  `repaired_equipment_id` BIGINT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX IF NOT EXISTS `idx_damage_reports_original_equipment_id` ON `damage_reports` (`original_equipment_id`);
