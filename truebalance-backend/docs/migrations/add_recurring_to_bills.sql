-- Migration: Add is_recurring column to bills table
-- Date: 2025-01-XX
-- Description: Adds a boolean column to mark bills as recurring (e.g., monthly internet bills)

-- Add the is_recurring column with default value false
ALTER TABLE bills 
ADD COLUMN IF NOT EXISTS is_recurring BOOLEAN NOT NULL DEFAULT false;

-- Add a comment to document the column
COMMENT ON COLUMN bills.is_recurring IS 'Indicates if the bill is recurring (e.g., monthly internet bill, subscription, etc.)';
