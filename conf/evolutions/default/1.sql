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

create table career (
  id                            varchar(255) not null,
  career_name                   varchar(255),
  career_subjects               longtext,
  constraint pk_career primary key (id)
);

create table course (
  id                            varchar(255) not null,
  start_date                    varchar(255),
  end_date                      varchar(255),
  subject_id                    varchar(255),
  constraint pk_course primary key (id)
);

create table course_student (
  course_id                     varchar(255) not null,
  student_id                    varchar(255) not null,
  constraint pk_course_student primary key (course_id,student_id)
);

create table dictation_hours (
  id                            varchar(255) not null,
  day                           varchar(255),
  start_time                    datetime(6),
  end_time                      datetime(6),
  constraint pk_dictation_hours primary key (id)
);

create table exam (
  id                            varchar(255) not null,
  course_id                     varchar(255),
  date                          varchar(255),
  constraint pk_exam primary key (id)
);

create table exam_inscription (
  id                            varchar(255) not null,
  student_id                    varchar(255),
  exam_id                       varchar(255),
  result                        integer,
  constraint pk_exam_inscription primary key (id)
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
  address                       varchar(255),
  constraint uq_student_email unique (email),
  constraint pk_student primary key (id)
);

create table subject (
  id                            varchar(255) not null,
  subject_name                  varchar(255),
  career_year                   integer not null,
  required_subjects             longtext,
  constraint pk_subject primary key (id)
);

create table token (
  id                            varchar(255) not null,
  user_id                       varchar(255),
  valid_until                   datetime(6),
  lifespan                      integer not null,
  constraint pk_token primary key (id)
);

create index ix_course_subject_id on course (subject_id);
alter table course add constraint fk_course_subject_id foreign key (subject_id) references subject (id) on delete restrict on update restrict;

create index ix_course_student_course on course_student (course_id);
alter table course_student add constraint fk_course_student_course foreign key (course_id) references course (id) on delete restrict on update restrict;

create index ix_course_student_student on course_student (student_id);
alter table course_student add constraint fk_course_student_student foreign key (student_id) references student (id) on delete restrict on update restrict;

create index ix_exam_course_id on exam (course_id);
alter table exam add constraint fk_exam_course_id foreign key (course_id) references course (id) on delete restrict on update restrict;

create index ix_exam_inscription_student_id on exam_inscription (student_id);
alter table exam_inscription add constraint fk_exam_inscription_student_id foreign key (student_id) references student (id) on delete restrict on update restrict;

create index ix_exam_inscription_exam_id on exam_inscription (exam_id);
alter table exam_inscription add constraint fk_exam_inscription_exam_id foreign key (exam_id) references exam (id) on delete restrict on update restrict;


# --- !Downs

alter table course drop foreign key fk_course_subject_id;
drop index ix_course_subject_id on course;

alter table course_student drop foreign key fk_course_student_course;
drop index ix_course_student_course on course_student;

alter table course_student drop foreign key fk_course_student_student;
drop index ix_course_student_student on course_student;

alter table exam drop foreign key fk_exam_course_id;
drop index ix_exam_course_id on exam;

alter table exam_inscription drop foreign key fk_exam_inscription_student_id;
drop index ix_exam_inscription_student_id on exam_inscription;

alter table exam_inscription drop foreign key fk_exam_inscription_exam_id;
drop index ix_exam_inscription_exam_id on exam_inscription;

drop table if exists admin;

drop table if exists career;

drop table if exists course;

drop table if exists course_student;

drop table if exists dictation_hours;

drop table if exists exam;

drop table if exists exam_inscription;

drop table if exists professor;

drop table if exists student;

drop table if exists subject;

drop table if exists token;

