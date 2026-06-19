-- Run this once on an existing MySQL database before creating duplicate
-- apartment numbers across different buildings or complexes.

ALTER TABLE apartments
    ADD COLUMN IF NOT EXISTS complex_name varchar(255) NULL;

UPDATE apartments
SET complex_name = 'Hưng Thịnh'
WHERE complex_name IS NULL OR complex_name = '';

SET @old_apartment_number_index := (
    SELECT s.index_name
    FROM information_schema.statistics s
    JOIN (
        SELECT table_schema, table_name, index_name, COUNT(*) AS column_count
        FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = 'apartments'
        GROUP BY table_schema, table_name, index_name
    ) indexed_columns
      ON indexed_columns.table_schema = s.table_schema
     AND indexed_columns.table_name = s.table_name
     AND indexed_columns.index_name = s.index_name
    WHERE s.table_schema = DATABASE()
      AND s.table_name = 'apartments'
      AND s.column_name = 'apartment_number'
      AND s.non_unique = 0
      AND s.index_name <> 'PRIMARY'
      AND indexed_columns.column_count = 1
    LIMIT 1
);

SET @drop_old_index_sql := IF(
    @old_apartment_number_index IS NULL,
    'SELECT 1',
    CONCAT('ALTER TABLE apartments DROP INDEX ', @old_apartment_number_index)
);
PREPARE drop_old_index_stmt FROM @drop_old_index_sql;
EXECUTE drop_old_index_stmt;
DEALLOCATE PREPARE drop_old_index_stmt;

ALTER TABLE apartments
    ADD CONSTRAINT uk_apartment_location_number
        UNIQUE (complex_name, block, apartment_number);
