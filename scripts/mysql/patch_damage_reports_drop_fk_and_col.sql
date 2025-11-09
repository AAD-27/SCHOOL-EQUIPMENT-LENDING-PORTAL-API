-- Patch script to remove legacy FK and equipment_id column from damage_reports
-- Run this against your 'equipment_db' schema (or current DB)

-- 1) Drop foreign key referencing equipment (if present)
SET @fk := (
  SELECT rc.CONSTRAINT_NAME
  FROM INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS rc
  WHERE rc.CONSTRAINT_SCHEMA = DATABASE()
    AND rc.TABLE_NAME = 'damage_reports'
    AND rc.REFERENCED_TABLE_NAME = 'equipment'
  LIMIT 1
);
SET @sql := IF(@fk IS NOT NULL, CONCAT('ALTER TABLE `damage_reports` DROP FOREIGN KEY `', @fk, '`'), 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 2) Drop equipment_id column if present
SET @has_col := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'damage_reports' AND COLUMN_NAME = 'equipment_id'
);
SET @sql2 := IF(@has_col = 1, 'ALTER TABLE `damage_reports` DROP COLUMN `equipment_id`', 'SELECT 1');
PREPARE stmt2 FROM @sql2; EXECUTE stmt2; DEALLOCATE PREPARE stmt2;

-- 3) Ensure snapshot columns exist (no-ops if they already do not exist in older MySQL versions)
-- You may need to run individual ALTERs if your MySQL does not support on ADD COLUMN
ALTER TABLE `damage_reports`
  ADD COLUMN `original_equipment_id` BIGINT NULL,
  ADD COLUMN `equipment_name` VARCHAR(255) NULL,
  ADD COLUMN `equipment_category` VARCHAR(255) NULL,
  ADD COLUMN `equipment_condition_description` VARCHAR(255) NULL,
  ADD COLUMN `equipment_quantity` INT NULL,
  ADD COLUMN `equipment_available_quantity` INT NULL,
  ADD COLUMN `repaired_equipment_id` BIGINT NULL;

-- 4) Add index for lookups
CREATE INDEX `idx_damage_reports_original_equipment_id` ON `damage_reports` (`original_equipment_id`);

