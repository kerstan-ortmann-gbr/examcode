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

public class Lecture extends ExamDatabaseRow {
    private static final String LECTURES_TABLE = "lectures";

    private String title;
    private String shortTitle;

    // constructors
    public Lecture(ExamDatabase db, int rowID){
        super(db, LECTURES_TABLE, rowID);
    }

    public Lecture(ExamDatabase db){
        super(db, LECTURES_TABLE);
    }

    // setter and getter
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getShortTitle() {
        return shortTitle == null ? title : shortTitle;
    }

    public void setShortTitle(String shortTitle) {
        this.shortTitle = shortTitle;
    }


    // other methods
    public String toString(){
        if (shortTitle!=null){
            return title + " (" + shortTitle + ")";
        } else {
            return title;
        }
    }
}
