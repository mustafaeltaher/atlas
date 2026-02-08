-- Migration Script: Convert old allocation month columns to monthly_allocations table
-- This script should be run BEFORE the new application starts

-- Step 1: Create the monthly_allocations table if it doesn't exist
CREATE TABLE IF NOT EXISTS monthly_allocations (
    id BIGSERIAL PRIMARY KEY,
    allocation_id BIGINT NOT NULL,
    year INT NOT NULL,
    month INT NOT NULL,
    percentage DOUBLE PRECISION NOT NULL,
    CONSTRAINT fk_monthly_allocation FOREIGN KEY (allocation_id) REFERENCES allocations(id) ON DELETE CASCADE,
    CONSTRAINT uk_alloc_year_month UNIQUE (allocation_id, year, month)
);

-- Step 2: Migrate data from old month columns to new table
-- Using current year as the default since the old model didn't track years
-- Only migrate non-null and numeric values (skip 'B' and 'P' values)

DO $$
DECLARE
    current_year INT := EXTRACT(YEAR FROM CURRENT_DATE);
    rec RECORD;
    alloc_value TEXT;
    percentage_value DOUBLE PRECISION;
BEGIN
    FOR rec IN SELECT id, jan_alloc, feb_alloc, mar_alloc, apr_alloc,
                      may_alloc, jun_alloc, jul_alloc, aug_alloc,
                      sep_alloc, oct_alloc, nov_alloc, dec_alloc
               FROM allocations
    LOOP
        -- January
        alloc_value := rec.jan_alloc;
        IF alloc_value IS NOT NULL AND alloc_value ~ '^[0-9]*\.?[0-9]+$' THEN
            percentage_value := alloc_value::DOUBLE PRECISION;
            IF percentage_value > 0 THEN
                INSERT INTO monthly_allocations (allocation_id, year, month, percentage)
                VALUES (rec.id, current_year, 1, percentage_value)
                ON CONFLICT (allocation_id, year, month) DO UPDATE SET percentage = EXCLUDED.percentage;
            END IF;
        END IF;
        
        -- February
        alloc_value := rec.feb_alloc;
        IF alloc_value IS NOT NULL AND alloc_value ~ '^[0-9]*\.?[0-9]+$' THEN
            percentage_value := alloc_value::DOUBLE PRECISION;
            IF percentage_value > 0 THEN
                INSERT INTO monthly_allocations (allocation_id, year, month, percentage)
                VALUES (rec.id, current_year, 2, percentage_value)
                ON CONFLICT (allocation_id, year, month) DO UPDATE SET percentage = EXCLUDED.percentage;
            END IF;
        END IF;
        
        -- March
        alloc_value := rec.mar_alloc;
        IF alloc_value IS NOT NULL AND alloc_value ~ '^[0-9]*\.?[0-9]+$' THEN
            percentage_value := alloc_value::DOUBLE PRECISION;
            IF percentage_value > 0 THEN
                INSERT INTO monthly_allocations (allocation_id, year, month, percentage)
                VALUES (rec.id, current_year, 3, percentage_value)
                ON CONFLICT (allocation_id, year, month) DO UPDATE SET percentage = EXCLUDED.percentage;
            END IF;
        END IF;
        
        -- April
        alloc_value := rec.apr_alloc;
        IF alloc_value IS NOT NULL AND alloc_value ~ '^[0-9]*\.?[0-9]+$' THEN
            percentage_value := alloc_value::DOUBLE PRECISION;
            IF percentage_value > 0 THEN
                INSERT INTO monthly_allocations (allocation_id, year, month, percentage)
                VALUES (rec.id, current_year, 4, percentage_value)
                ON CONFLICT (allocation_id, year, month) DO UPDATE SET percentage = EXCLUDED.percentage;
            END IF;
        END IF;
        
        -- May
        alloc_value := rec.may_alloc;
        IF alloc_value IS NOT NULL AND alloc_value ~ '^[0-9]*\.?[0-9]+$' THEN
            percentage_value := alloc_value::DOUBLE PRECISION;
            IF percentage_value > 0 THEN
                INSERT INTO monthly_allocations (allocation_id, year, month, percentage)
                VALUES (rec.id, current_year, 5, percentage_value)
                ON CONFLICT (allocation_id, year, month) DO UPDATE SET percentage = EXCLUDED.percentage;
            END IF;
        END IF;
        
        -- June
        alloc_value := rec.jun_alloc;
        IF alloc_value IS NOT NULL AND alloc_value ~ '^[0-9]*\.?[0-9]+$' THEN
            percentage_value := alloc_value::DOUBLE PRECISION;
            IF percentage_value > 0 THEN
                INSERT INTO monthly_allocations (allocation_id, year, month, percentage)
                VALUES (rec.id, current_year, 6, percentage_value)
                ON CONFLICT (allocation_id, year, month) DO UPDATE SET percentage = EXCLUDED.percentage;
            END IF;
        END IF;
        
        -- July
        alloc_value := rec.jul_alloc;
        IF alloc_value IS NOT NULL AND alloc_value ~ '^[0-9]*\.?[0-9]+$' THEN
            percentage_value := alloc_value::DOUBLE PRECISION;
            IF percentage_value > 0 THEN
                INSERT INTO monthly_allocations (allocation_id, year, month, percentage)
                VALUES (rec.id, current_year, 7, percentage_value)
                ON CONFLICT (allocation_id, year, month) DO UPDATE SET percentage = EXCLUDED.percentage;
            END IF;
        END IF;
        
        -- August
        alloc_value := rec.aug_alloc;
        IF alloc_value IS NOT NULL AND alloc_value ~ '^[0-9]*\.?[0-9]+$' THEN
            percentage_value := alloc_value::DOUBLE PRECISION;
            IF percentage_value > 0 THEN
                INSERT INTO monthly_allocations (allocation_id, year, month, percentage)
                VALUES (rec.id, current_year, 8, percentage_value)
                ON CONFLICT (allocation_id, year, month) DO UPDATE SET percentage = EXCLUDED.percentage;
            END IF;
        END IF;
        
        -- September
        alloc_value := rec.sep_alloc;
        IF alloc_value IS NOT NULL AND alloc_value ~ '^[0-9]*\.?[0-9]+$' THEN
            percentage_value := alloc_value::DOUBLE PRECISION;
            IF percentage_value > 0 THEN
                INSERT INTO monthly_allocations (allocation_id, year, month, percentage)
                VALUES (rec.id, current_year, 9, percentage_value)
                ON CONFLICT (allocation_id, year, month) DO UPDATE SET percentage = EXCLUDED.percentage;
            END IF;
        END IF;
        
        -- October
        alloc_value := rec.oct_alloc;
        IF alloc_value IS NOT NULL AND alloc_value ~ '^[0-9]*\.?[0-9]+$' THEN
            percentage_value := alloc_value::DOUBLE PRECISION;
            IF percentage_value > 0 THEN
                INSERT INTO monthly_allocations (allocation_id, year, month, percentage)
                VALUES (rec.id, current_year, 10, percentage_value)
                ON CONFLICT (allocation_id, year, month) DO UPDATE SET percentage = EXCLUDED.percentage;
            END IF;
        END IF;
        
        -- November
        alloc_value := rec.nov_alloc;
        IF alloc_value IS NOT NULL AND alloc_value ~ '^[0-9]*\.?[0-9]+$' THEN
            percentage_value := alloc_value::DOUBLE PRECISION;
            IF percentage_value > 0 THEN
                INSERT INTO monthly_allocations (allocation_id, year, month, percentage)
                VALUES (rec.id, current_year, 11, percentage_value)
                ON CONFLICT (allocation_id, year, month) DO UPDATE SET percentage = EXCLUDED.percentage;
            END IF;
        END IF;
        
        -- December
        alloc_value := rec.dec_alloc;
        IF alloc_value IS NOT NULL AND alloc_value ~ '^[0-9]*\.?[0-9]+$' THEN
            percentage_value := alloc_value::DOUBLE PRECISION;
            IF percentage_value > 0 THEN
                INSERT INTO monthly_allocations (allocation_id, year, month, percentage)
                VALUES (rec.id, current_year, 12, percentage_value)
                ON CONFLICT (allocation_id, year, month) DO UPDATE SET percentage = EXCLUDED.percentage;
            END IF;
        END IF;
    END LOOP;
    
    RAISE NOTICE 'Migration complete!';
END $$;

-- Step 3: Show migration results
SELECT 'Total monthly allocations created:' as message, COUNT(*) as count FROM monthly_allocations;
