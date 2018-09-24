# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table admin (
  id                            varchar(255) not null,
  name                          varchar(255),
  last_name                     varchar(255),
  file                          varchar(255),
  email                         varchar(255),
  password                      varchar(255),
  constraint uq_admin_email unique (email),
  constraint pk_admin primary key (id)
);

create table professor (
  id                            varchar(255) not null,
  name                          varchar(255),
  last_name                     varchar(255),
  file                          varchar(255),
  email                         varchar(255),
  password                      varchar(255),
  constraint uq_professor_email unique (email),
  constraint pk_professor primary key (id)
);

create table student (
  id                            varchar(255) not null,
  name                          varchar(255),
  last_name                     varchar(255),
  file                          varchar(255),
  email                         varchar(255),
  password                      varchar(255),
  birthday                      varchar(255),
  identification_type           varchar(255),
  identification                varchar(255),
  constraint uq_student_email unique (email),
  constraint pk_student primary key (id)
);

create table subject (
  id                            varchar(255) not null,
  subject_name                  varchar(255),
  career_year                   integer not null,
  required_subjects             clob,
  constraint pk_subject primary key (id)
);

create table subject_student (
  subject_id                    varchar(255) not null,
  student_id                    varchar(255) not null,
  constraint pk_subject_student primary key (subject_id,student_id)
);

create index ix_subject_student_subject on subject_student (subject_id);
alter table subject_student add constraint fk_subject_student_subject foreign key (subject_id) references subject (id) on delete restrict on update restrict;

create index ix_subject_student_student on subject_student (student_id);
alter table subject_student add constraint fk_subject_student_student foreign key (student_id) references student (id) on delete restrict on update restrict;


# --- !Downs

alter table subject_student drop constraint if exists fk_subject_student_subject;
drop index if exists ix_subject_student_subject;

alter table subject_student drop constraint if exists fk_subject_student_student;
drop index if exists ix_subject_student_student;

drop table if exists admin;

drop table if exists professor;

drop table if exists student;

drop table if exists subject;

drop table if exists subject_student;

