package de.klang_technik.examcode;

/*
    ExamCode - Correction.java
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

public class Correction extends ExamDatabaseRow {
    private static final String CORRECTIONS_TABLE="corrections";
    private Exercise exercise;
    private Participant participant;
    private double points; // negative: did not try to solve the exercise
    private String comment;

    // constructors
    public Correction(ExamDatabase db, Exercise exercise, Participant participant){
        super(db, CORRECTIONS_TABLE);
        this.exercise = exercise;
        this.participant = participant;
    }

    public Correction(ExamDatabase db, int rowID, Exercise exercise, Participant participant){
        super(db, CORRECTIONS_TABLE, rowID);
        this.exercise = exercise;
        this.participant = participant;
    }

    // getter and setter
    public Exercise getExercise() {
        return exercise;
    }

    public Participant getParticipant() {
        return participant;
    }

    public double getPoints() {
        return points;
    }

    public void setPoints(double points) {
        this.points = points;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }


    public String toString(){
        return this.getRowID() + ": A" + this.exercise.getNumber() + ", " + this.points + "/" +  this.exercise.getPoints();
    }
}
