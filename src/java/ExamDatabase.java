package de.klang_technik.examcode;

/*
    Copyright (c) 2016-2017 Henning Kerstan und Roman Ortmann GbR.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

import java.io.File;
import java.sql.*;
import java.util.*;
import java.text.SimpleDateFormat;
import java.sql.Date;

/* List of Todos:
- close connection and file!
- multiton is bad: memory leak!
- constants for SQL
 */

public class ExamDatabase {

    // constants
    public static final String TABLE_EXAMS = "exams";
    private String codeAlphabet = "ABCEHKMPTW0123459";

    // Multiton: only one db controller per file allowed!
    private static final Map<File, ExamDatabase> instances = new HashMap<File, ExamDatabase>();

    private ExamDatabase(){}

    public static ExamDatabase getInstance(File file){
        if (file != null && file.exists()){

            // try to get existing instance
            ExamDatabase instance = instances.get(file);

            if (instance != null){
                System.out.println("ExamDatabase: returning existing instance");
                return instance;
            }

            // try to open
            assert(instance == null);
            try {
                instance = new ExamDatabase();
                Class.forName("org.sqlite.JDBC");
                instance.conn = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
                instance.file=file;
                instance.checkIntegrity();
                instances.put(file,instance);
            } catch ( Exception e ) {
                System.err.println( e.getClass().getName() + ": " + e.getMessage() );
                System.exit(0);
            }

            System.out.println("ExamDatabase: returning new instance");
            return instance;

        } else {
            return null; // todo: throw file does not exist!
        }
    }

    // fields
    private File file = null;
    private Connection conn = null;

    private Boolean checkIntegrity(){
        // NOT YET FINISHED!

        if (conn == null){
            return false;
        }

        // check if all necessary tables exist
        try {
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT tbl_name FROM sqlite_master WHERE type='table'");
            while (rs.next()) {
                System.out.println("id = " + rs.getString("tbl_name"));
            }

            rs.close();
        } catch (Exception e) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

        return true;
    };


    // Semesters
    public ArrayList<Semester> getSemesters(){
        ArrayList<Semester> semesters = new ArrayList<Semester>();

        try {
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT id,year,season FROM terms ORDER BY year DESC, season DESC");

            while (rs.next()) {
                int rowID = rs.getInt("id");
                Semester semester = new Semester(this, rowID);
                semester.setYear(rs.getInt("year"));
                semester.setSummer(rs.getInt("season") == 0);
                semesters.add(semester);
            }
        } catch (Exception e) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

        return semesters;
    }

    public ArrayList<Exam> getExams(Semester semester){
        ArrayList<Exam> exams = new ArrayList<Exam>();

        if(conn!= null){
            try {
                Statement s = conn.createStatement();
                //System.out.println("sem: " + semester.getRowID());
                String queryString = "SELECT id,comment,beginDate,duration,useCodes,anonymize,publicID,keyHMAC,lecture,lectureTitle,lectureShortTitle,term,year,season,examiner,firstName,lastName,title,status FROM viewExams  WHERE term="+semester.getRowID() + " ORDER BY beginDate DESC";

                //System.out.println("string: " + queryString);

                ResultSet rs = s.executeQuery(queryString);

                while (rs.next()) {
                    int rowID = rs.getInt("id");
                    Exam e = new Exam(this, rowID);

                    e.setSemester(semester);

                    int lectureID = rs.getInt("lecture");
                    Lecture l = new Lecture(this, lectureID);
                    l.setTitle(rs.getString("lectureTitle"));
                    l.setShortTitle(rs.getString("lectureShortTitle"));
                    e.setLecture(l);

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    java.util.Date date = dateFormat.parse(rs.getString("beginDate"));
                    e.setDate(date);
                    exams.add(e);
                }

                rs.close();

            } catch (Exception e) {
                System.err.println( e.getClass().getName() + ": " + e.getMessage() );
                System.exit(0);
            }

        }

        return exams;
    }


    // Participants
    public Participant addParticipant(Exam exam, String firstName, String lastName, String matriculation){
        Participant p = null;
        if (conn !=null && exam!= null && exam.getRowID()> 0){
            try {
                PreparedStatement s = conn.prepareStatement("INSERT INTO participants(exam,lastName,firstName,matriculation) VALUES (?,?,?,?)");

                s.setInt(1,exam.getRowID());
                s.setString(2, lastName);
                s.setString(3, firstName);
                s.setString(4, matriculation);
                s.executeUpdate();

            } catch (Exception e) {
                System.err.println( e.getClass().getName() + ": " + e.getMessage() );
                System.exit(0);
            }


            // add codes (number of participants + max(.1*numOfParcitipants, 2);
            ArrayList<Code> codes = getCodes(exam);
            int numberOfCodes = codes.size();
            int numberOfParticipants = getParticipants(exam).size();
            int numberOfCodesNeeded = numberOfParticipants +  Math.max((int)Math.ceil(0.1*numberOfParticipants), 2);
            int numberOfCodesToAdd = Math.max(0, numberOfCodesNeeded - numberOfCodes);

            System.out.println("current codes: " + numberOfCodes);
            System.out.println("participants " + numberOfParticipants);
            System.out.println("needed codes: " + numberOfCodesNeeded);
            System.out.println("adding: " + numberOfCodesToAdd);

            for (int i = 0; i<numberOfCodesToAdd; i++){
                codes = getCodes(exam);
                boolean found = false;
                String codeString;

                // generate Code as long as necessary
                do {
                    codeString = randomString(codeAlphabet, 4);
                    found = false;
                    for (Code c:codes){
                        if(c.getCodeString() == codeString){
                            found = true;
                            break;
                        }
                    }
                } while (found);

                addCode(exam, codeString);
                System.out.println("Adding: "+codeString);
            }

        } else {
            // throw an exception?
        }

        return p; // TODO: insert rowID!!
    };

    public Exercise addExercise(Exam exam){
        if (conn !=null && exam!= null && exam.getRowID()> 0){

            // get exercises to find smallest free number; requires that getExercises is sorted!
            int currentNumber = 1;
            ArrayList<Exercise> exercises = getExercises(exam);
            for(Exercise ex:exercises){
                if(ex.getNumber() == currentNumber){
                    currentNumber++;
                }
            }

            try {
                PreparedStatement s = conn.prepareStatement("INSERT INTO exercises(exam,number,maxPoints) VALUES (?,?,?)");

                s.setInt(1,exam.getRowID());
                s.setInt(2, currentNumber);
                s.setDouble(3, 10.0);
                s.executeUpdate();

            } catch (Exception e) {
                System.err.println( e.getClass().getName() + ": " + e.getMessage() );
                System.exit(0);
            }

            // get exercises again to find correct id; TODO: improve performance by only selecting relevant exerise, not all
            exercises = getExercises(exam);
            for (Exercise exercise: exercises){
                if (exercise.getNumber() == currentNumber){
                    return exercise;
                }
            }

        } else {
            // throw an exception?

        }

        return null;
    };


    public void removeExercise(Exercise exercise){
        if (conn !=null && exercise!= null && exercise.getRowID()> 0){
            try {
                PreparedStatement s = conn.prepareStatement("DELETE FROM exercises WHERE id=?");

                s.setInt(1,exercise.getRowID());
                s.executeUpdate();

            } catch (Exception e) {
                System.err.println( e.getClass().getName() + ": " + e.getMessage() );
                System.exit(0);
            }

        } else {
            // throw an exception?

        }
    };

    public void updateExercise(Exercise exercise){
        if (conn !=null && exercise!= null && exercise.getRowID()> 0) {
            try {
                PreparedStatement s = conn.prepareStatement("UPDATE exercises SET number=?,maxPoints=? WHERE id=?");

                s.setInt(1, exercise.getNumber());
                s.setDouble(2, exercise.getPoints());
                s.setInt(3, exercise.getRowID());
                s.executeUpdate();

            } catch (Exception e) {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
                System.exit(0);
            }
        }
    }


    public Code addCode(Exam exam, String codeString){
        Code c = null;
        if (conn !=null && exam!= null && exam.getRowID()> 0){
            try {
                PreparedStatement s = conn.prepareStatement("INSERT INTO codes(exam,codeString) VALUES (?,?)");

                s.setInt(1,exam.getRowID());
                s.setString(2, codeString);
                s.executeUpdate();

            } catch (Exception e) {
                System.err.println( e.getClass().getName() + ": " + e.getMessage() );
                System.exit(0);
            }

        } else {
            // throw an exception?

        }

        // add code!
        return c; // TODO: insert rowID!!
    };

    public void removeParticipant(Participant p){
            if (conn !=null && p!= null && p.getRowID()> 0){
                try {
                    PreparedStatement s = conn.prepareStatement("DELETE FROM participants WHERE id=?");

                    s.setInt(1,p.getRowID());
                    s.executeUpdate();

                } catch (Exception e) {
                    System.err.println( e.getClass().getName() + ": " + e.getMessage() );
                    System.exit(0);
                }

            } else {
                // throw an exception?
            }
    };

    public ArrayList<Participant> getParticipants(Exam exam){
        ArrayList<Participant> participants = new ArrayList<Participant>();

        if(conn!= null && exam != null){
            try {
                Statement s = conn.createStatement();
                ResultSet rs = s.executeQuery("SELECT id,exam,matriculation,firstName,lastName,registered,participated,fileName,codeString,publishResult,comment,hasBonus,room,roomName,course,courseName,attempt,code FROM viewParticipants WHERE exam=" + exam.getRowID());

                while (rs.next()) {
                    int rowID = rs.getInt("id");
                    Participant p = new Participant(this, rowID, exam);
                    p.setMatriculation(rs.getString("matriculation"));
                    p.setFirstName(rs.getString("firstName"));
                    p.setLastName(rs.getString("lastName"));

                    p.setRegistered(rs.getBoolean("registered"));
                    p.setParticipated(rs.getBoolean("participated"));
                    p.setFileName(rs.getString("fileName"));

                    p.setAttempt(rs.getInt("attempt"));

                    p.setCourseName(rs.getString("courseName"));

                    int bonus = rs.getInt("hasBonus");
                    switch(bonus){
                        case -1:    p.setHasBonus(Optional.of(false));
                                    break;
                        case 0:     p.setHasBonus(Optional.empty());
                                    break;
                        case 1:     p.setHasBonus(Optional.of(true));
                                    break;
                    }

                    // code
                    rowID = rs.getInt("code");
                    Code c = new Code(this, rowID);
                    c.setCodeString(rs.getString("codeString"));
                    p.setCode(c);

                    participants.add(p);
                    //System.out.println("id = " + rs.getString("tbl_name"));
                }

                rs.close();

            } catch (Exception e) {
                System.err.println( e.getClass().getName() + ": " + e.getMessage() );
                System.exit(0);
            }
        }
        return participants;
    }


    // Lectures
    public ArrayList<Lecture> getLectures(){
        ArrayList<Lecture> lectures = new ArrayList<Lecture>();

        if(conn!= null){
            try {
                Statement s = conn.createStatement();
                ResultSet rs = s.executeQuery("SELECT id, title, shortTitle FROM lectures");

                while (rs.next()) {
                    int rowID = rs.getInt("id");
                    Lecture l = new Lecture(this, rowID);

                    l.setTitle(rs.getString("title"));
                    l.setShortTitle(rs.getString("shortTitle"));

                    lectures.add(l);
                }
                rs.close();

            } catch (Exception e) {
                System.err.println( e.getClass().getName() + ": " + e.getMessage() );
                System.exit(0);
            }
        }
        return lectures;
    }


    // Exercises
    public ArrayList<Exercise> getExercises(Exam exam){
        ArrayList<Exercise> exercises = new ArrayList<Exercise>();

        if(conn!= null){
            try {
                Statement s = conn.createStatement();
                ResultSet rs = s.executeQuery("SELECT * FROM exercises WHERE exam="+ exam.getRowID() + " ORDER BY number");

                while (rs.next()) {
                    int rowID = rs.getInt("id");
                    Exercise e = new Exercise(this, rowID);

                    e.setNumber(rs.getInt("number"));
                    e.setPoints(rs.getDouble("maxPoints"));

                    exercises.add(e);
                }

                rs.close();

            } catch (Exception e) {
                System.err.println( e.getClass().getName() + ": " + e.getMessage() );
                System.exit(0);
            }
        }
        return exercises;
    }

    // Available Codes
    public ArrayList<Code> getAvailableCodes(Exam exam){
        ArrayList<Code> codes = new ArrayList<Code>();

        if(conn!= null){
            try {
                Statement s = conn.createStatement();
                ResultSet rs = s.executeQuery("SELECT * FROM viewAvailableCodes WHERE exam="+ exam.getRowID());

                while (rs.next()) {
                    int rowID = rs.getInt("id");
                    Code c = new Code(this, rowID);

                    c.setCodeString(rs.getString("codeString"));
                    //e.setNumber(rs.getInt("number"));
                    //e.setPoints(rs.getDouble("maxPoints"));

                    codes.add(c);
                }

                rs.close();

            } catch (Exception e) {
                System.err.println( e.getClass().getName() + ": " + e.getMessage() );
                System.exit(0);
            }
        }
        return codes;
    }

    public ArrayList<Code> getCodes(Exam exam){
        ArrayList<Code> codes = new ArrayList<Code>();

        if(conn!= null){
            try {
                Statement s = conn.createStatement();
                ResultSet rs = s.executeQuery("SELECT * FROM codes WHERE exam="+ exam.getRowID());

                while (rs.next()) {
                    int rowID = rs.getInt("id");
                    Code c = new Code(this, rowID);

                    c.setCodeString(rs.getString("codeString"));
                    //e.setNumber(rs.getInt("number"));
                    //e.setPoints(rs.getDouble("maxPoints"));

                    codes.add(c);
                }

                rs.close();

            } catch (Exception e) {
                System.err.println( e.getClass().getName() + ": " + e.getMessage() );
                System.exit(0);
            }
        }
        return codes;
    }

    public boolean assignCodeToParticipant(Code c, Participant p){
        if (conn !=null && p!= null && c != null && p.getRowID() > 0 && c.getRowID() > 0){
            try {
                PreparedStatement s = conn.prepareStatement("UPDATE participants SET participated=1,code=? WHERE id=?");

                s.setInt(1,c.getRowID());
                s.setInt(2, p.getRowID());
                s.executeUpdate();

                return true;

            } catch (Exception e) {
                System.err.println( e.getClass().getName() + ": " + e.getMessage() );
                System.exit(0);
                return false;
            }

        } else {
            // throw an exception?
            return false;
        }
    }

    public boolean clearCodeOfParticipant(Participant p){
        if (conn !=null && p!= null && p.getRowID() > 0){
            try {
                PreparedStatement s = conn.prepareStatement("UPDATE participants SET participated=0,code=NULL WHERE id=?");
                s.setInt(1,p.getRowID());
                s.executeUpdate();

                return true;

            } catch (Exception e) {
                System.err.println( e.getClass().getName() + ": " + e.getMessage() );
                System.exit(0);
                return false;
            }

        } else {
            // throw an exception?
            return false;
        }
    }

    private String randomString(String alphabet, int length){
        String currentString ="";
        Random random = new Random();

        if (alphabet == null){
            return "";
        }

        int alphabetSize = alphabet.length();

        if (alphabetSize <= 1 || length <=1){
            return "";
        }

        for (int c=0; c< length; c++){
            currentString = currentString + alphabet.charAt(random.nextInt(alphabetSize));
        }

        return currentString;
    }




    public Correction getCorrection(Participant participant, Exercise exercise){
        if(conn!= null & exercise != null & participant != null){
            try {
                Correction c;
                Statement s = conn.createStatement();
                ResultSet rs = s.executeQuery("SELECT DISTINCT * FROM corrections WHERE participant=" + participant.getRowID() + " AND exercise=" + exercise.getRowID());

                if (rs.next()) {
                    int rowID = rs.getInt("id");

                    c = new Correction(this, rowID, exercise, participant);
                    c.setPoints(rs.getDouble("points"));
                } else {
                    //System.out.println("not found");
                    c = new Correction(this, 0, exercise, participant);

                }

                rs.close();
                return c;
            } catch (Exception e) {
                System.err.println( e.getClass().getName() + ": " + e.getMessage() );
                System.exit(0);
            }
        }
        return null;
    }

    public void deleteCorrection(Correction correction){
        if(conn!= null & correction != null & correction.getRowID() > 0){
            try {
                Correction c;
                Statement s = conn.createStatement();
                s.executeUpdate("DELETE FROM corrections WHERE id=" + correction.getRowID());
            } catch (Exception e) {
                System.err.println( e.getClass().getName() + ": " + e.getMessage() );
                System.exit(0);
            }
        }
    }

    public void addCorrection(Correction correction){
        if (conn !=null && correction!= null && correction.getRowID()== 0 && correction.getParticipant() != null && correction.getParticipant().getRowID() > 0){
            try {
                PreparedStatement s = conn.prepareStatement("INSERT INTO corrections(participant,exercise,points,comment) VALUES (?,?,?,?)");
                s.setInt(1,correction.getParticipant().getRowID());
                s.setInt(2,correction.getExercise().getRowID());
                s.setDouble(3,correction.getPoints());
                s.setString(4,correction.getComment());
                s.executeUpdate();
            } catch (Exception e) {
                System.err.println( e.getClass().getName() + ": " + e.getMessage() );
                System.exit(0);
            }

        } else {
            // throw an exception?

        }
    }

    public void updateCorrection(Correction correction){
        if (conn !=null && correction!= null && correction.getRowID()> 0){
            try {
                PreparedStatement s = conn.prepareStatement("UPDATE corrections SET points=?,comment=? WHERE id=?");
                s.setDouble(1,correction.getPoints());
                s.setString(2,correction.getComment());
                s.setInt(3,correction.getRowID());
                s.executeUpdate();
            } catch (Exception e) {
                System.err.println( e.getClass().getName() + ": " + e.getMessage() );
                System.exit(0);
            }

        } else {
            // throw an exception?

        }
    }
}
