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

public class Exercise extends ExamDatabaseRow{
    private static final String EXERCISES_TABLE = "exercises";

    private Integer number;
    private double points;


    // constructors
    public Exercise(ExamDatabase db, int rowID){
        super(db, EXERCISES_TABLE, rowID);
    }

    public Exercise(ExamDatabase db){
        super(db, EXERCISES_TABLE);
    }



    // getter and setter
    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public double getPoints() {
        return points;
    }

    public void setPoints(double points) {
        this.points = points;
    }


    // other methods
    public String toString (){
        return number + "(" + points + ")";
    }
}