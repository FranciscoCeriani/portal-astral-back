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

create table course (
  id                            varchar(255) not null,
  start_time                    timestamp,
  end_time                      timestamp,
  subject_id                    varchar(255),
  constraint pk_course primary key (id)
);

create table course_dictation_hours (
  course_id                     varchar(255) not null,
  dictation_hours_id            varchar(255) not null,
  constraint pk_course_dictation_hours primary key (course_id,dictation_hours_id)
);

create table dictation_hours (
  id                            varchar(255) not null,
  day                           varchar(255),
  start_time                    timestamp,
  end_time                      timestamp,
  constraint pk_dictation_hours primary key (id)
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

create table token (
  id                            varchar(255) not null,
  user_id                       varchar(255),
  valid_until                   timestamp,
  lifespan                      integer not null,
  constraint pk_token primary key (id)
);

create index ix_course_subject_id on course (subject_id);
alter table course add constraint fk_course_subject_id foreign key (subject_id) references subject (id) on delete restrict on update restrict;

create index ix_course_dictation_hours_course on course_dictation_hours (course_id);
alter table course_dictation_hours add constraint fk_course_dictation_hours_course foreign key (course_id) references course (id) on delete restrict on update restrict;

create index ix_course_dictation_hours_dictation_hours on course_dictation_hours (dictation_hours_id);
alter table course_dictation_hours add constraint fk_course_dictation_hours_dictation_hours foreign key (dictation_hours_id) references dictation_hours (id) on delete restrict on update restrict;

create index ix_subject_student_subject on subject_student (subject_id);
alter table subject_student add constraint fk_subject_student_subject foreign key (subject_id) references subject (id) on delete restrict on update restrict;

create index ix_subject_student_student on subject_student (student_id);
alter table subject_student add constraint fk_subject_student_student foreign key (student_id) references student (id) on delete restrict on update restrict;


# --- !Downs

alter table course drop constraint if exists fk_course_subject_id;
drop index if exists ix_course_subject_id;

alter table course_dictation_hours drop constraint if exists fk_course_dictation_hours_course;
drop index if exists ix_course_dictation_hours_course;

alter table course_dictation_hours drop constraint if exists fk_course_dictation_hours_dictation_hours;
drop index if exists ix_course_dictation_hours_dictation_hours;

alter table subject_student drop constraint if exists fk_subject_student_subject;
drop index if exists ix_subject_student_subject;

alter table subject_student drop constraint if exists fk_subject_student_student;
drop index if exists ix_subject_student_student;

drop table if exists admin;

drop table if exists course;

drop table if exists course_dictation_hours;

drop table if exists dictation_hours;

drop table if exists professor;

drop table if exists student;

drop table if exists subject;

drop table if exists subject_student;

drop table if exists token;

