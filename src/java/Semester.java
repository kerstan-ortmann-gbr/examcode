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

import javafx.beans.property.*;

public class Semester extends ExamDatabaseRow {

    private StringProperty shortTitle;

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public boolean isSummer() {
        return summer;
    }

    public void setSummer(boolean summer) {
        this.summer = summer;
    }

    private int year;
    private boolean summer; // 0 = summer, 1 = winter

    public Semester(ExamDatabase db, int rowID){
        super(db, "terms", rowID);
    }

    public Semester(ExamDatabase db){
        super(db, "terms");
    }

    public StringProperty shortTitle(){
        if (shortTitle == null){
            shortTitle = new SimpleStringProperty(this, toString());
        }
        return shortTitle;
    }

    public String toString(){
        if (summer){
            return "SoSe " + year;
        } else {
            return "WiSe " + year + "/" + ((year % 100) + 1);
        }
    }
}
