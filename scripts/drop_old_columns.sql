-- Drop old month columns from allocations table
-- Run this AFTER successful migration to monthly_allocations table

ALTER TABLE allocations
    DROP COLUMN IF EXISTS jan_alloc,
    DROP COLUMN IF EXISTS feb_alloc,
    DROP COLUMN IF EXISTS mar_alloc,
    DROP COLUMN IF EXISTS apr_alloc,
    DROP COLUMN IF EXISTS may_alloc,
    DROP COLUMN IF EXISTS jun_alloc,
    DROP COLUMN IF EXISTS jul_alloc,
    DROP COLUMN IF EXISTS aug_alloc,
    DROP COLUMN IF EXISTS sep_alloc,
    DROP COLUMN IF EXISTS oct_alloc,
    DROP COLUMN IF EXISTS nov_alloc,
    DROP COLUMN IF EXISTS dec_alloc;

-- Verify columns were dropped
SELECT column_name 
FROM information_schema.columns 
WHERE table_name = 'allocations' 
ORDER BY ordinal_position;
