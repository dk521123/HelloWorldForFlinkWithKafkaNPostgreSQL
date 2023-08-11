CREATE USER demo_db WITH PASSWORD 'password';
CREATE DATABASE demo_db;
GRANT ALL PRIVILEGES ON DATABASE demo_db TO demo_db;
\c demo_db
create table demo_counter(word varchar, counter int);
insert into demo_counter(word, counter) values
('dummy1', 100),
('dummy2', 2000);
