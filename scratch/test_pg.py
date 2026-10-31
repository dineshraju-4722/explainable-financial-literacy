import psycopg2

passwords = ["postgres", "postgres_secure_pass_2026", "admin", "root", "123456", "password", ""]
for pwd in passwords:
    try:
        conn = psycopg2.connect(
            dbname="postgres",
            user="postgres",
            password=pwd,
            host="localhost",
            port=5432,
            connect_timeout=2
        )
        print(f"SUCCESS: password is '{pwd}'")
        conn.close()
        break
    except Exception as e:
        print(f"Failed with '{pwd}': {e}")
