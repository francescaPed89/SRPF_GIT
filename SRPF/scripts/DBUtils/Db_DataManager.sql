DROP sequence GSIF_PAW_SEQ;
DROP sequence MISSION_SEQ;
DROP sequence SAT_BEAM_ASSOCIATION_SEQ;
DROP sequence SATELLITE_SEQ;
DROP sequence SENSOR_MODE_SEQ;
DROP sequence BEAM_SEQ;
DROP sequence SATELLITE_PASS_seq;
DROP TABLE
    MISSION CASCADE constraints;
DROP TABLE
    SENSOR_MODE CASCADE constraints;
DROP TABLE
    SATELLITE CASCADE constraints;
DROP TABLE
    BEAM CASCADE constraints;
DROP TABLE
    SAT_BEAM_ASSOCIATION CASCADE constraints;
DROP TABLE
    GSIF_PAW CASCADE constraints;
DROP TABLE 
    OBDATA_FILES CASCADE constraints;
DROP TABLE
    SATELLITE_PASS CASCADE constraints;	



CREATE TABLE
    MISSION
    (
        ID_MISSION NUMBER(11) NOT NULL,
        MISSION_NAME VARCHAR2(255),
        CONSTRAINT MISSION_PK PRIMARY KEY (ID_MISSION)
    );
CREATE SEQUENCE MISSION_seq;
CREATE TABLE
    SENSOR_MODE
    (
        ID_SENSOR_MODE NUMBER(11) NOT NULL,
        SENSOR_MODE_NAME VARCHAR2(255),
        IS_SPOT_LIGHT NUMBER(1),
        CONSTRAINT SENSOR_MODE_PK PRIMARY KEY (ID_SENSOR_MODE) 
    );
CREATE SEQUENCE SENSOR_MODE_seq;


CREATE TABLE
    SATELLITE
    (
        ID_SATELLITE NUMBER(11) NOT NULL,
        SATELLITE_NAME VARCHAR2(255),
        MISSION NUMBER(11),
        IS_ENABLED NUMBER(1),
        ID_ALLOWED_LOOK_SIDE NUMBER(11),
	TRACK_OFFSET NUMBER DEFAULT 0,
        CONSTRAINT SATELLITE_PK PRIMARY KEY (ID_SATELLITE),
        CONSTRAINT SATELLITE_R01 FOREIGN KEY (MISSION) REFERENCES MISSION (ID_MISSION) ON DELETE CASCADE ENABLE
    );
CREATE SEQUENCE SATELLITE_seq;


CREATE TABLE
    BEAM
    (
        ID_BEAM NUMBER(11) NOT NULL,
        BEAM_NAME VARCHAR2(4000),
        NEAR_OFF_NADIR NUMBER(11,4),
        FAR_OFF_NADIR NUMBER(11,4),
        SENSOR_MODE NUMBER(11),
 	  IS_ENABLED NUMBER(1),
        SW_DIM1 NUMBER(11,4),
        SW_DIM2 NUMBER(11,4),
        DTO_MIN_DURATION NUMBER(11),
        DTO_MAX_DURATION NUMBER(11),
        RES_TIME NUMBER(11),
        CONSTRAINT BEAM_PK PRIMARY KEY (ID_BEAM),
        CONSTRAINT BEAM_R01 FOREIGN KEY (SENSOR_MODE) REFERENCES SENSOR_MODE (ID_SENSOR_MODE) ON DELETE CASCADE ENABLE
    );
CREATE SEQUENCE BEAM_seq;


CREATE TABLE
    SAT_BEAM_ASSOCIATION
    (
        ID_BEAM_ASSOCIATION NUMBER(11) NOT NULL,
        SATELLITE NUMBER(10),
        BEAM NUMBER(11),
        CONSTRAINT ID_BEAM_ASSOCIATION PRIMARY KEY (ID_BEAM_ASSOCIATION),
        CONSTRAINT SAT_BEAM_ASSOCIATION_R01 FOREIGN KEY (SATELLITE) REFERENCES SATELLITE
        (ID_SATELLITE) ON DELETE CASCADE ENABLE,
        CONSTRAINT SAT_BEAM_ASSOCIATION_R02 FOREIGN KEY (BEAM) REFERENCES BEAM (ID_BEAM) ON DELETE CASCADE ENABLE
    );
CREATE SEQUENCE SAT_BEAM_ASSOCIATION_seq;
--table GSIF_PAW
CREATE TABLE
    GSIF_PAW
    (
        ID INTEGER NOT NULL,
        SATELLITE INTEGER NOT NULL,
        ACTIVITY_TYPE VARCHAR2(255),
        ACTIVITY_ID INTEGER NOT NULL,
        ACTIVITY_START_TIME FLOAT(126) NOT NULL,
        ACTIVITY_STOP_TIME FLOAT(126) NOT NULL,
        DEFERRABLE_FLAG NUMBER(1),
        CONSTRAINT "GSIF_PAW_FK1" FOREIGN KEY ("SATELLITE") REFERENCES "SATELLITE" ("ID_SATELLITE") ON DELETE CASCADE ENABLE
    );
ALTER TABLE
    GSIF_PAW ADD ( CONSTRAINT GSIF_PAW_pk PRIMARY KEY (ID));
CREATE SEQUENCE GSIF_PAW_seq;
--ALTER TABLE
--    GSIF_PAW ADD CONSTRAINT GSIF_PAW_ix1 UNIQUE (SATELLITE, ACTIVITY_START_TIME,
--    ACTIVITY_STOP_TIME);

--OBDATA_FILES
CREATE TABLE OBDATA_FILES 
   (	"ID_SATELLITE" NUMBER(11,0), 
	"ODSTP" VARCHAR2(512 BYTE), 
	"ODMTP" VARCHAR2(512 BYTE), 
	"ODNOM" VARCHAR2(512 BYTE), 
	"ODREF" VARCHAR2(512 BYTE), 
	 CONSTRAINT "OBDATA_FILES_FK1" FOREIGN KEY ("ID_SATELLITE")
	 REFERENCES "SATELLITE" ("ID_SATELLITE") ON DELETE CASCADE ENABLE
   ); 

--TABLE SATELLITE_PASS

CREATE TABLE
    SATELLITE_PASS
    (
	ID INTEGER NOT NULL,        
	SATELLITE INTEGER NOT NULL,
        ASID VARCHAR2(255) NULL,
        CONTACT_COUNTER INTEGER NOT NULL,
        VISIBILITY_START_TIME FLOAT(126) NOT NULL,
        VISIBILITY_STOP_TIME FLOAT(126) NOT NULL,
        CONSTRAINT "SATELLITE_PASS_FK1" FOREIGN KEY ("SATELLITE") REFERENCES "SATELLITE" ("ID_SATELLITE") ON DELETE CASCADE ENABLE,
	CONSTRAINT "SATELLITE_PASS_UK1" UNIQUE ("SATELLITE", "ASID", "CONTACT_COUNTER")
    );
ALTER TABLE
    SATELLITE_PASS ADD ( CONSTRAINT SATELLITE_PASS_pk PRIMARY KEY (ID));
CREATE SEQUENCE SATELLITE_PASS_seq;
	