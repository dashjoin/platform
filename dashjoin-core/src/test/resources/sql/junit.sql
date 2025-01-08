
drop table if exists "T";
drop table if exists "U";
drop table if exists "EMP";
drop table if exists "PRJ";
drop table if exists "NOKEY";
drop table if exists "NODE";

create table "T"("ID" int, "FK" int, "A" int, "B" int, "C" int);
create table "U"("ID" int, "C" int);
create table "PRJ"("ID" int not null primary key, "NAME" varchar(255), "BUDGET" int);
insert into "PRJ" values(1000, 'dev-project', null);
insert into "PRJ" values(1001, 'other', null);
create table "EMP"("ID" int not null primary key, "NAME" varchar(255), "WORKSON" int references "PRJ"("ID"));
insert into "EMP" values(1, 'mike', 1000);
insert into "EMP" values(2, 'joe', 1000);
create table "NOKEY"("ID" int, "NAME" varchar(255));

create table "NODE"("ID" int primary key, "REF" int references "NODE"("ID"));
insert into "NODE" values (1,null);
insert into "NODE" values (2,1);
insert into "NODE" values (3,1);
insert into "NODE" values (4,3);
