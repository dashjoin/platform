create table t(id int, fk int, a int, b int, c int);
create table u(id int, c int);
create table prj(id int primary key, name varchar(255));
insert into prj values(1000, 'dev-project');
create table emp(id int primary key, name varchar(255), workson int references prj(id));
insert into emp values(1, 'mike', 1000);
insert into emp values(2, 'joe', 1000);
create table nokey(id int, name varchar(255));
