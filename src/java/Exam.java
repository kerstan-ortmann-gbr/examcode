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

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Exam extends ExamDatabaseRow {
    private Lecture lecture;
    private Semester semester;
    private Date date;

    private ObservableList<Participant> participants;

    // crypto
    private String publicID;
    private String keyHMAC;

    public void reloadParticipants(){
        participants = FXCollections.observableArrayList(getDatabase().getParticipants(this));
    }

    public ObservableList<Participant> getParticipants() {
        if (participants == null){
            reloadParticipants();
        }
        return participants;
    }

    private ObservableList<Exercise> exercises;

    public Lecture getLecture() {
        return lecture;
    }

    public void setLecture(Lecture lecture) {
        this.lecture = lecture;
    }

    public Semester getSemester() {
        return semester;
    }

    public void setSemester(Semester semester) {
        this.semester = semester;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }


    public Exam(ExamDatabase db, int rowID){
        super(db, "exams", rowID);
    }

    public Exam(ExamDatabase db){
        super(db, "exams");
    }

    public String toString(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy");
        return dateFormat.format(getDate()) + " - " + lecture.getShortTitle();
    }

    public void reloadExercises(){
        exercises = FXCollections.observableArrayList(this.getDatabase().getExercises(this));
    }

    public ObservableList<Exercise> getExercises() {
        if(exercises == null){
            reloadExercises();
        }

        return exercises;
    }

    public double totalPoints(){
        if(exercises == null){
            reloadExercises();
        }

        double totalPoints = 0;

        for(Exercise exercise:exercises){
            totalPoints+=exercise.getPoints();
        }

        return totalPoints;
    }

   public int getFinishedCorrectionCount(){
       int finished = 0;
       for (Participant p:getParticipants()){
           if(p.correctionCompleted()){
               finished++;
           }
       }

       return finished;
    }
}
