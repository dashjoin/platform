
drop table if exists T;
drop table if exists U;
drop table if exists EMP;
drop table if exists PRJ;
drop table if exists NOKEY;

create table T(ID int, FK int, A int, B int, C int);
create table U(ID int, C int);
create table PRJ(ID int primary key, NAME varchar(255));
insert into PRJ values(1000, 'dev-project');
create table EMP(ID int primary key, NAME varchar(255), WORKSON int references PRJ(ID));
insert into EMP values(1, 'mike', 1000);
insert into EMP values(2, 'joe', 1000);
create table NOKEY(ID int, NAME varchar(255));
