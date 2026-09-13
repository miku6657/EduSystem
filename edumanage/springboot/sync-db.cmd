@echo off
REM ============================================================
REM  Sync database schema + seed data (one command)
REM
REM  Usage: double-click this file, or run sync-db.cmd in this folder.
REM  Chinese notes: see README.md in the repository root.
REM
REM  Why this file contains ASCII only:
REM    cmd.exe parses .bat/.cmd using the console code page (GBK on
REM    Chinese Windows), so a UTF-8 batch file with Chinese text can be
REM    mis-parsed and even fail midway. ASCII keeps it portable.
REM
REM  What it does:
REM    1) schema.sql : rebuilds all tables (each one is preceded by
REM                    DROP TABLE IF EXISTS, so it is re-runnable);
REM    2) data.sql   : inserts seed data with INSERT IGNORE and resets
REM                    the seed account passwords to BCrypt.
REM    Run it once after every git pull to avoid
REM    "table/column does not exist" or "login failed" errors.
REM
REM  Overridable via environment variables:
REM    MYSQL_USER (default root), MYSQL_PWD (default 123456),
REM    MYSQL_HOST (default 127.0.0.1), MYSQL_PORT (default 3306)
REM ============================================================
setlocal
cd /d "%~dp0"

if "%MYSQL_USER%"=="" set MYSQL_USER=root
if "%MYSQL_PWD%"==""  set MYSQL_PWD=123456
if "%MYSQL_HOST%"=="" set MYSQL_HOST=127.0.0.1
if "%MYSQL_PORT%"=="" set MYSQL_PORT=3306

where mysql >nul 2>nul
if errorlevel 1 (
  echo [ERROR] mysql client not found. Add the MySQL bin directory to PATH.
  exit /b 1
)

set MYSQL_ARGS=--default-character-set=utf8mb4 --user=%MYSQL_USER% --host=%MYSQL_HOST% --port=%MYSQL_PORT%

echo [1/2] schema.sql: create / reset tables
mysql %MYSQL_ARGS% < schema.sql
if errorlevel 1 goto fail

echo [2/2] data.sql: insert seed data
mysql %MYSQL_ARGS% eduSYSTEM < data.sql
if errorlevel 1 goto fail

echo.
echo [OK] database eduSYSTEM has been synced.
echo      accounts (password 123456): admin / T001 / 2023005001
exit /b 0

:fail
echo.
echo [FAIL] sync aborted. Please check:
echo   1) MySQL service is running (default port 3306)
echo   2) credentials match application.yml (default root/123456;
echo      override with MYSQL_USER / MYSQL_PWD / MYSQL_HOST / MYSQL_PORT)
echo   3) the account has DDL privileges (root is recommended)
exit /b 1
