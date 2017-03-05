-- ExamCode - CreateEmptyDatabase.sql
-- Copyright (c) 2013-2017 Henning Kerstan und Roman Ortmann GbR.

-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--    http://www.apache.org/licenses/LICENSE-2.0

-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.



-- Creates an empty ExamCode database (SQLite) with schema version 0.1.0
-- Author: Henning Kerstan (henning.kerstan@klang-technik.de)

PRAGMA foreign_keys=ON; -- allow foreign keys

CREATE TABLE settings (
    id		INTEGER PRIMARY KEY,
    key		TEXT UNIQUE NOT NULL,
    value	TEXT NOT NULL
);

INSERT INTO settings (key,value) VALUES
('schema.name', 'ExamDB'), 
('schema.major', '0'),
('schema.minor', '1'),
('schema.revision', '0'),
('db.initial_schema', '0.1.0'),
('db.created', datetime('now'));

-- In the following table the user data is stored. It is used just to identify
-- users who have made changes in the other tables. (not yet implemented)
CREATE TABLE users (
	id			INTEGER PRIMARY KEY, 
    userName	TEXT UNIQUE NOT NULL,
    lastName	TEXT NOT NULL,
    firstName	TEXT NOT NULL
);


CREATE TABLE lectures (
    id    	     INTEGER PRIMARY KEY,
    title 	     TEXT UNIQUE NOT NULL,
    shortTitle    TEXT UNIQUE
);


CREATE TABLE terms (
       id		INTEGER PRIMARY KEY,
       year		INTEGER NOT NULL,
       season	INTEGER NOT NULL DEFAULT 0, 		-- 0: summer, 1: winter
       
       CONSTRAINT termUnique UNIQUE(year,season)
);


CREATE TABLE examiners (
    id    	    INTEGER PRIMARY KEY,
    lastName	TEXT NOT NULL,
    firstName	TEXT NOT NULL,
    title	    TEXT
);


CREATE TABLE exams (
    id			INTEGER PRIMARY KEY,
    comment		TEXT,
    lecture 	INTEGER NOT NULL REFERENCES lectures 	ON UPDATE CASCADE ON DELETE RESTRICT,
    term 		INTEGER NOT NULL REFERENCES terms 		ON UPDATE CASCADE ON DELETE RESTRICT,
    examiner 	INTEGER NOT NULL REFERENCES examiners 	ON UPDATE CASCADE ON DELETE RESTRICT,
    beginDate	TEXT NOT NULL,
    duration	INTEGER NOT NULL, -- in minutes
	useCodes	INTEGER NOT NULL,
	anonymize	INTEGER NOT NULL DEFAULT 0,
	publicID	TEXT UNIQUE NOT NULL,
	keyHMAC		TEXT NOT NULL,
	status		INTEGER NOT NULL DEFAULT 0,

    CONSTRAINT examUnique UNIQUE(lecture,term,examiner,beginDate)
);


CREATE TABLE exercises (
	id			INTEGER PRIMARY KEY,
    exam		INTEGER REFERENCES exams 		ON UPDATE CASCADE ON DELETE RESTRICT,	
    number		INTEGER, -- number
    maxPoints	NUMERIC, -- should be NULL for exercise groups 
    parent		INTEGER REFERENCES exercises 	ON UPDATE CASCADE ON DELETE CASCADE,
    
    CONSTRAINT exerciseUnique UNIQUE(number, exam, parent)
);


CREATE TABLE courses ( -- courses of study
       id    INTEGER PRIMARY KEY,
       name  TEXT UNIQUE NOT NULL
);


CREATE TABLE files (
	id		INTEGER PRIMARY KEY,
	name	TEXT NOT NULL
);


CREATE TABLE participants (
    id				INTEGER PRIMARY KEY,
    exam			INTEGER NOT NULL REFERENCES exams 		ON UPDATE CASCADE ON DELETE RESTRICT,
	lastName		TEXT,
	firstName		TEXT,
	matriculation	TEXT,
	registered		INTEGER NOT NULL DEFAULT 0,
	participated	INTEGER NOT NULL DEFAULT 0,
	room			INTEGER REFERENCES examRooms 			ON UPDATE CASCADE ON DELETE RESTRICT,
    code			INTEGER UNIQUE REFERENCES codes 		ON UPDATE CASCADE ON DELETE RESTRICT,
	publishResult	INTEGER NOT NULL DEFAULT 0,
    hasBonus		INTEGER NOT NULL DEFAULT 0, 
	comment			TEXT,
    file			INTEGER REFERENCES files				ON UPDATE CASCADE ON DELETE RESTRICT,
	course			INTEGER REFERENCES courses 				ON UPDATE CASCADE ON DELETE RESTRICT,
	attempt			INTEGER,

    CONSTRAINT participantUnique UNIQUE(exam,lastName,firstName,matriculation)			
);


-- The corrections
CREATE TABLE corrections (
    id			INTEGER PRIMARY KEY,
    participant	INTEGER NOT NULL REFERENCES participants 	ON UPDATE CASCADE ON DELETE RESTRICT,
	exercise 	INTEGER NOT NULL REFERENCES exercises 		ON UPDATE CASCADE ON DELETE RESTRICT,
    points 		NUMERIC, 
    comment 	TEXT,
    noSolution	INTEGER, -- if set to 1 this indicates that the participant did not provide any solution to the exercise 
	
    CONSTRAINT correctionUnique UNIQUE(exercise,participant)				
);


-- The codes
CREATE TABLE codes (
    id			INTEGER PRIMARY KEY,
    exam		INTEGER NOT NULL REFERENCES exams ON UPDATE CASCADE ON DELETE RESTRICT,
	codeString  TEXT,
       
    CONSTRAINT codeUnique UNIQUE(exam,codeString)
);


-- grading 
CREATE TABLE gradeThresholdTableTemplates (
       id    	INTEGER PRIMARY KEY,
       name	TEXT UNIQUE
);

CREATE TABLE gradeThresholdTemplates (
       id    		     INTEGER PRIMARY KEY, 
       template		     INTEGER
			     REFERENCES gradeThresholdTableTemplates
			     ON UPDATE CASCADE
			     ON DELETE RESTRICT,
       threshold	     NUMERIC,
       grade		     NUMERIC,

       CONSTRAINT thresholdUniquePerTemplate UNIQUE(template,threshold)
);

CREATE TABLE gradeThresholds (
       id    		     INTEGER PRIMARY KEY, 
       exam		     INTEGER NOT NULL
       			     REFERENCES exams
			     ON UPDATE CASCADE
			     ON DELETE RESTRICT,
       threshold	     NUMERIC,
       grade		     NUMERIC,

       CONSTRAINT thresholdUniquePerExam UNIQUE(exam,threshold)
);


-- rooms
CREATE TABLE examRooms (
       id		     INTEGER PRIMARY KEY,
       exam   	     	     INTEGER NOT NULL
       			     REFERENCES exams
			     ON UPDATE CASCADE
			     ON DELETE RESTRICT,
       name		     TEXT NOT NULL,
       beginDelay	     NUMERIC DEFAULT 0,
       extraTime	     NUMERIC DEFAULT 0,

       CONSTRAINT examRoomUniquePerExam UNIQUE(exam,name)
);


-- network transfer tables (should be created/destroyed dynamically)
CREATE TABLE newParticipants (
       id    INTEGER PRIMARY KEY,

       exam  INTEGER NOT NULL
       	     REFERENCES exams
	     ON UPDATE CASCADE
	     ON DELETE RESTRICT,

       name TEXT
);

-- Views
CREATE VIEW viewExams AS
SELECT exams.id, 
       exams.comment,
       exams.beginDate,
       exams.duration,
       exams.useCodes,
       exams.anonymize,
       exams.publicID,
       exams.keyHMAC,
       lectures.id AS lecture, 
       lectures.title AS lectureTitle,
       lectures.shortTitle AS lectureShortTitle,
       terms.id AS term, 
       terms.year, 
       terms.season, 
       examiners.id AS examiner,
       examiners.firstName,
       examiners.lastName,
       examiners.title,
       exams.status
FROM exams
JOIN lectures ON lectures.id=exams.lecture
JOIN terms ON terms.id=exams.term
JOIN examiners ON examiners.id=exams.examiner;


CREATE VIEW viewParticipants AS
SELECT participants.id,
       participants.exam AS exam,
       matriculation, 
       firstName, 
       lastName, 
       participants.registered,
       participants.participated,
       files.id	AS file,
       files.name AS fileName,
       courses.id AS course,
       courses.name AS courseName,
       codes.id AS code,
       codes.codeString,
       participants.publishResult,
       participants.comment,
       participants.hasBonus,
       participants.room,
       examRooms.name AS roomName,
       participants.attempt
FROM participants
LEFT JOIN examRooms ON participants.room=examRooms.id
LEFT JOIN courses ON participants.course=courses.id
LEFT JOIN codes ON codes.id=participants.code
LEFT JOIN files ON files.id=participants.file;


CREATE view viewAssignedCodes AS
SELECT codes.id,
       codes.exam,
       codes.codeString
FROM codes 
JOIN participants ON participants.code=codes.id;


CREATE view viewAvailableCodes AS
SELECT codes.id,
       codes.exam,
       codes.codeString
FROM codes
LEFT JOIN participants ON participants.code=codes.id
WHERE participants.code ISNULL;

