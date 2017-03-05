-- ExamCode - GenerateTestData.sql
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



-- This generates test data in an already existing ExamCode database (schema 0.1.0)
-- All the data is fictional. Any coincidences with real persons and/or real
-- data is random.

-- swith display mode 
.mode columns
.nullvalue ?
.header ON

-- Add users
INSERT INTO users (userName, lastName, firstName) VALUES
('mf', 'Fischer', 'Martin'), ('ak', 'Klein', 'Alexandra');

-- Terms
INSERT INTO terms (year, season) VALUES 
(2016,0), (2016,1),(2017,0);


-- Lectures
INSERT INTO lectures (title,shortTitle) VALUES
('Datenbanken', 'DB'),
('Objective-C', 'Obj-C'),
('Cocoa',NULL),
('Mathematik',NULL),
('Java I', NULL);


-- Examiners
INSERT INTO examiners (lastName, firstName, title) VALUES
('Weber', 'Rike', 'M. Sc.'),
('Vogel', 'Oliver', 'Prof. Dr. Dr. h.c.'),
('Fischer', 'Martin', 'Dr.'),
('Klein', 'Alexandra', 'Prof. Dr.');


-- Exams 
INSERT INTO exams (lecture, term, examiner, beginDate, duration,useCodes, publicID, keyHMAC, anonymize) VALUES
(1, 1, 3, '2016-02-24 10:00:00', 120, 1, 'EXAM1EXAM1', 'This is the first private key.',1),
(1, 2, 3, '2017-02-24 10:00:00', 120, 0 ,'EXAM2EXAM2', 'This is another private key.',0),
(2, 2, 2, '2017-02-17 13:15:00',  90, 1, 'EXAM3EXAM3', 'This is a third private key.',0),
(3, 2, 1, '2017-03-05 11:10:00', 240, 0, 'EXAM4EXAM4', 'Here is another private key.',0);

INSERT INTO exams (comment, lecture, term, examiner, beginDate, duration, useCodes,publicID, keyHMAC) VALUES
('Second Exam', 5, 3, 3, '2016-02-25 14:00:00',  60, 1, 'EXA5', 'PRV1');


-- exam rooms
INSERT INTO examRooms (exam,name) VALUES 
(1, 'Audimax'), (1, 'Raum B'),
(2, 'Raum 1'),
(3, 'Raum'),
(4, 'Raum'),
(5, 'Raum'); 


-- Codes
CREATE TABLE codeImport (codeString TEXT UNIQUE);
.import codes.csv codeImport
INSERT INTO codes (codeString, exam) 
SELECT codeString, '1' FROM codeImport;

INSERT INTO codes (codeString,exam) 
SELECT codeString, '5' FROM codes;

DROP TABLE codeImport;

-- Exercises
INSERT INTO exercises (exam, number, maxPoints, parent) VALUES
(1, 1, 40, NULL),  
(1, 2,  10, NULL),
(1, 3,   7, NULL),
(1, 4,  13, NULL), 

(2, 1,  10, NULL),
(2, 2,  10, NULL),
(2, 3,   7, NULL),
(2, 4,   8, NULL),

(5, 1, 10, NULL),
(5, 2, 10, NULL),
(5, 3, 10, NULL),
(5, 4, 10, NULL),
(5, 5, 10, NULL);


-- courses (new!)
INSERT INTO courses(name) VALUES ('Bachelor Informatik');
INSERT INTO courses(name) VALUES ('Master Informatik');
INSERT INTO courses(name) VALUES ('Bachelor Mathematik');


-- Participants
CREATE TABLE studentImport (lastName TEXT, firstName TEXT, matriculation TEXT, code INTEGER);
.separator ";"
.import students_code.csv studentImport

UPDATE studentImport SET code=NULL where code=0;


-- all participate at first exam
INSERT INTO participants (exam, course,lastName, firstName, matriculation,code) SELECT 1,2,lastName,firstName,matriculation,code FROM studentImport;

-- some participate at last exam
INSERT INTO participants (exam, course, lastName, firstName, matriculation,code) SELECT 5,2,lastName,firstName,matriculation,code+50 FROM studentImport WHERE rowid>45;

UPDATE participants SET participated=1 WHERE NOT code IS NULL;
DROP TABLE studentImport;


-- Corrections 
-- INSERT INTO corrections (exercise,participant,points) VALUES
-- (1, 1, 7.5),
-- (2, 1, 3.75),
-- (3, 1, 10.5),
-- (4, 1, 2.5),
-- (5, 1, 1),
-- (6, 1, 1),
-- (1, 2, 2),
-- (2, 2, 1),
-- (3, 2, 1.5),
-- (4, 2, 3.5),
-- (5, 2, 2),
--(6, 2, 2);
