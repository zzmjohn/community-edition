create table ACT_GE_PROPERTY (
    NAME_ varchar(255),
    VALUE_ varchar(255),
    REV_ integer,
    primary key (NAME_)
) TYPE=InnoDB;

insert into ACT_GE_PROPERTY
values ('schema.version', '5.0.beta1', 1);

insert into ACT_GE_PROPERTY
values ('next.dbid', '1', 1);

create table ACT_GE_BYTEARRAY (
    ID_ varchar(255),
    REV_ integer,
    NAME_ varchar(255),
    DEPLOYMENT_ID_ varchar(255),
    BYTES_ LONGBLOB,
    primary key (ID_)
) TYPE=InnoDB;

create table ACT_RE_DEPLOYMENT (
    ID_ varchar(255),
    NAME_ varchar(255),
    DEPLOY_TIME_ timestamp,
    primary key (ID_)
) TYPE=InnoDB;

create table ACT_RU_EXECUTION (
    ID_ varchar(255),
    REV_ integer,
    PROC_INST_ID_ varchar(255),
    PARENT_ID_ varchar(255),
    PROC_DEF_ID_ varchar(255),
    SUPER_EXEC_ varchar(255),
    ACTIVITY_ID_ varchar(255),
    IS_ACTIVE_ TINYINT,
    IS_CONCURRENT_ TINYINT,
    IS_SCOPE_ TINYINT,
    primary key (ID_)
) TYPE=InnoDB;

create table ACT_RU_JOB (
    ID_ varchar(255) NOT NULL,
   REV_ integer,
    TYPE_ varchar(255) NOT NULL,
    LOCK_EXP_TIME_ timestamp,
    LOCK_OWNER_ varchar(255),
    EXCLUSIVE_ boolean,
    EXECUTION_ID_ varchar(255),
    PROCESS_INSTANCE_ID_ varchar(255),
    RETRIES_ integer,
    EXCEPTION_ varchar(255),
    DUEDATE_ timestamp NULL,
    REPEAT_ varchar(255),
    HANDLER_TYPE_ varchar(255),
    HANDLER_CFG_ varchar(255),
    primary key (ID_)
) TYPE=InnoDB;

create table ACT_ID_GROUP (
    ID_ varchar(255),
    REV_ integer,
    NAME_ varchar(255),
    TYPE_ varchar(255),
    primary key (ID_)
) TYPE=InnoDB;

create table ACT_ID_MEMBERSHIP (
    USER_ID_ varchar(255),
    GROUP_ID_ varchar(255),
    primary key (USER_ID_, GROUP_ID_)
) TYPE=InnoDB;

create table ACT_ID_USER (
    ID_ varchar(255),
    REV_ integer,
    FIRST_ varchar(255),
    LAST_ varchar(255),
    EMAIL_ varchar(255),
    PWD_ varchar(255),
    primary key (ID_)
) TYPE=InnoDB;

create table ACT_RE_PROC_DEF (
    ID_ varchar(255),
    NAME_ varchar(255),
    KEY_ varchar(255),
    VERSION_ integer,
    DEPLOYMENT_ID_ varchar(255),
   RESOURCE_NAME_ varchar(255),
    primary key (ID_)
) TYPE=InnoDB;

create table ACT_RU_TASK (
    ID_ varchar(255),
    REV_ integer,
    EXECUTION_ID_ varchar(255),
    PROC_INST_ID_ varchar(255),
    PROC_DEF_ID_ varchar(255),
    NAME_ varchar(255),
    DESCRIPTION_ varchar(255),
    FORM_ varchar(255),
    ASSIGNEE_ varchar(255),
    PRIORITY_ integer,
    CREATE_TIME_ timestamp,
    START_DEADLINE_ timestamp,
    COMPLETION_DEADLINE_ timestamp,
    SKIPPABLE_ TINYINT,
    primary key (ID_)
) TYPE=InnoDB;

create table ACT_RU_TASKINVOLVEMENT (
    ID_ varchar(255),
    REV_ integer,
    GROUP_ID_ varchar(255),
    TYPE_ varchar(255),
    USER_ID_ varchar(255),
    TASK_ID_ varchar(255),
    primary key (ID_)
) TYPE=InnoDB;

create table ACT_RU_VARIABLE (
    ID_ varchar(255) not null,
    REV_ integer,
    TYPE_ varchar(255) not null,
    NAME_ varchar(255) not null,
    EXECUTION_ID_ varchar(255),
   PROC_INST_ID_ varchar(255),
    TASK_ID_ varchar(255),
    BYTEARRAY_ID_ varchar(255),
    DATE_ timestamp,
    DOUBLE_ double,
    LONG_ bigint,
    TEXT_ varchar(255),
    primary key (ID_)
) TYPE=InnoDB;

create table ACT_HI_PROC_INST (
    ID_ varchar(255) not null,
    PROC_INST_ID_ varchar(255) not null,
    PROC_DEF_ID_ varchar(255) not null,
    START_TIME_ datetime not null,
    END_TIME_ datetime,
    DURATION_ bigint,
    -- TODO: check endStateName length
    END_ACT_ID_ varchar(255),
    primary key (ID_),
    unique (PROC_INST_ID_)
) TYPE=InnoDB;

create table ACT_HI_ACT_INST (
    ID_ varchar(255) not null,
    ACT_ID_ varchar(255) not null,
    ACT_NAME_ varchar(255),
    ACT_TYPE_ varchar(255) not null,
    PROC_INST_ID_ varchar(255) not null,
    PROC_DEF_ID_ varchar(255) not null,
    START_TIME_ datetime not null,
    END_TIME_ datetime,
    DURATION_ bigint,
    primary key (ID_),
    unique (ACT_ID_, PROC_INST_ID_)
) TYPE=InnoDB;

alter table ACT_GE_BYTEARRAY
    add constraint FK_ACT_BYTEARR_DEPL 
    foreign key (DEPLOYMENT_ID_) 
    references ACT_RE_DEPLOYMENT (ID_);

alter table ACT_RU_EXECUTION
    add constraint FK_ACT_EXE_PROCINST 
    foreign key (PROC_INST_ID_) 
    references ACT_RU_EXECUTION (ID_) on delete cascade on update cascade;

alter table ACT_RU_EXECUTION
    add constraint FK_ACT_EXE_PARENT 
    foreign key (PARENT_ID_) 
    references ACT_RU_EXECUTION (ID_);
    
alter table ACT_RU_EXECUTION
    add constraint FK_ACT_EXE_SUPER 
    foreign key (SUPER_EXEC_) 
    references ACT_RU_EXECUTION (ID_);
    
alter table ACT_ID_MEMBERSHIP 
    add constraint FK_ACT_MEMB_GROUP 
    foreign key (GROUP_ID_) 
    references ACT_ID_GROUP (ID_);

alter table ACT_ID_MEMBERSHIP 
    add constraint FK_ACT_MEMB_USER 
    foreign key (USER_ID_) 
    references ACT_ID_USER (ID_);

alter table ACT_RU_TASKINVOLVEMENT
    add constraint FK_ACT_TSKASS_TASK 
    foreign key (TASK_ID_) 
    references ACT_RU_TASK (ID_);
    
alter table ACT_RU_TASK
    add constraint FK_ACT_TASK_EXEC
    foreign key (EXECUTION_ID_)
    references ACT_RU_EXECUTION (ID_);
    
alter table ACT_RU_TASK
    add constraint FK_ACT_TASK_PROCINST
    foreign key (PROC_INST_ID_)
    references ACT_RU_EXECUTION (ID_);
    
alter table ACT_RU_TASK
  add constraint FK_ACT_TASK_PROCDEF
  foreign key (PROC_DEF_ID_)
  references ACT_RE_PROC_DEF (ID_);
  
alter table ACT_RU_VARIABLE 
    add constraint FK_ACT_VAR_TASK 
    foreign key (TASK_ID_) 
    references ACT_RU_TASK (ID_);

alter table ACT_RU_VARIABLE 
    add constraint FK_ACT_VAR_EXE 
    foreign key (EXECUTION_ID_) 
    references ACT_RU_EXECUTION (ID_);

alter table ACT_RU_VARIABLE
    add constraint FK_ACT_VAR_PROCINST
    foreign key (PROC_INST_ID_)
    references ACT_RU_EXECUTION(ID_);

alter table ACT_RU_VARIABLE 
    add constraint FK_ACT_VAR_BYTEARRAY 
    foreign key (BYTEARRAY_ID_) 
    references ACT_GE_BYTEARRAY (ID_);