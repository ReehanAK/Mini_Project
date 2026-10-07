@echo off
title FoodieHub — System Verification Tests
echo ====================================================================
echo Running FoodieHub Verification Test Suite (ACID Transactions & 3NF)
echo ====================================================================

java -cp "bin;lib/*" com.foodsystem.test.SystemVerificationTest
pause
