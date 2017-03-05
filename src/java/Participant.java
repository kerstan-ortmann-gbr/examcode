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

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;

import java.util.ArrayList;
import java.util.Optional;

public class Participant extends ExamDatabaseRow {

    private static final String PARTICIPANTS_TABLE = "participants";

    // private members
    private String firstName;
    private String lastName;
    private String matriculation;
    private String courseName;
    private Boolean registered;
    private Boolean participated;
    private Integer attempt;
    private String fileName;
    private String comment;


    private Exam exam;

    // corrections
    private ObservableList<Correction> corrections;

    public void reloadCorrections(){
        ArrayList<Correction> corrections = new ArrayList<>();

        for(Exercise e:exam.getExercises()){
            Correction c = this.getDatabase().getCorrection(this,e);

            if (c.getRowID() == 0){
                corrections.add(new Correction(this.getDatabase(), e, this));
            } else {
                corrections.add(c);
            }
        }

        this.corrections = FXCollections.observableArrayList(corrections);
    }

    public ObservableList<Correction> getCorrections() {
        if(corrections == null){
            reloadCorrections();
        }

        return corrections;
    }

    public double getPoints(){
        if (corrections == null){
            reloadCorrections();
        }

        double points = 0;
        for (Correction c:corrections){
            if (c.getPoints() > 0){
                points += c.getPoints();
            }
        }

        double ppg = exam.totalPoints() / 20; // points per grade
        if (getHasBonus().isPresent() && getHasBonus().get() == true){
            points += ppg;
        }

        return points;
    }

    public String getBonusPointsString(){
        Double ppg = exam.totalPoints() / 20; // points per grade
        if (getHasBonus().isPresent()){
            if(getHasBonus().get()){
                return ppg.toString();
            } else {
                return "0.0";
            }
        } else {
            return "?";
        }
    }

    public String getPointsString(){
        Double points = getPoints();

        if (participated){
            return correctionCompleted() ? points.toString() : "≥ " + points.toString();
        } else {
            return "-.-";
        }
    }


    // constructors
    public Participant(ExamDatabase db, int rowID, Exam exam){
        super(db, "participants", rowID);
        this.exam = exam;
    }

    public Participant(ExamDatabase db){
        super(db, "participants");
    }

    // getters & setters
    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public Boolean getRegistered() {
        return registered;
    }

    public void setRegistered(Boolean registered) {
        this.registered = registered;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Boolean getParticipated() {
        return participated;
    }

    public void setParticipated(Boolean participated) {
        this.participated = participated;
    }

    public static String getParticipantsTable() {
        return PARTICIPANTS_TABLE;
    }

    public Integer getAttempt() {
        return attempt;
    }

    public void setAttempt(Integer attempt) {
        this.attempt = attempt;
    }

    public Optional<Boolean> publishResult;

    public Optional<Boolean> getHasBonus() {
        return hasBonus;
    }

    public void setHasBonus(Optional<Boolean> hasBonus) {
        this.hasBonus = hasBonus;
    }

    private Optional<Boolean> hasBonus;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Code getCode() {
        return code;
    }

    public void setCode(Code code) {
        this.code = code;
    }

    private Code code;

    public String getCodeString() {
        return code.getCodeString();
    }

    public void setFirstName(String fName){
        firstName = fName;
    }

    public String getFirstName(){
        return firstName;
    }

    public void setLastName(String lName){
        lastName = lName;
    }

    public String getLastName(){
        return lastName;
    }

    public void setMatriculation(String matric){
        matriculation = matric;
    }

    public String getMatriculation(){
        return matriculation;
    }

    public boolean hasPassed() {
        return true;
    }

    // other methods
    public String toString(){
        return this.firstName + " " + this.lastName + " (" + this.matriculation + ")";//+ ", " + this.getCodeString() + ", " + this.fileName + ")";
    }

    public boolean correctionCompleted(){
        // returns true iff all corrections have rowID > 0

        if(corrections == null){
            reloadCorrections();
        }

        if(!hasBonus.isPresent()){
            return false;
        }

        for (Correction c:corrections){
            if (c.getRowID() == 0){
                return false;
            }
        }

        return true;
    }

    public double getGrade(){
        double maxPoints = exam.totalPoints();
        double ppg = maxPoints / 20;
        double p = getPoints();

        if (p >= maxPoints-ppg){
            return 1.0;
        }

        if (p >= maxPoints-2*ppg){
            return 1.3;
        }

        if (p >= maxPoints-3*ppg){
            return 1.7;
        }

        if (p >= maxPoints-4*ppg){
            return 2.0;
        }

        if (p >= maxPoints-5*ppg){
            return 2.3;
        }

        if (p >= maxPoints-6*ppg){
            return 2.7;
        }

        if (p >= maxPoints-7*ppg){
            return 3.0;
        }

        if (p >= maxPoints-8*ppg){
            return 3.3;
        }

        if (p >= maxPoints-9*ppg){
            return 3.7;
        }

        if (p >= maxPoints-10*ppg){
            return 4.0;
        }

        return 5.0;
    }

    public String getGradeString(){
        if (participated){
            Double grade = getGrade();
            return correctionCompleted() ? grade.toString() : "≥ " + grade;
        } else {
            return "NE";
        }
    }
}