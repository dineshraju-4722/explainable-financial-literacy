@echo off
for %%p in (harsh Harsh 1234 pass postgres1 12345678 admin123 root123 postgresql) do (
    set PGPASSWORD=%%p
    echo Testing password %%p ...
    "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U postgres -h localhost -l > nul 2>&1
    if not errorlevel 1 (
        echo SUCCESS: Password is %%p
        goto :end
    )
)
echo All tested passwords failed.
:end
