package de.klang_technik.examcode;
/*
    ExamCode - Code.java
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

public class Code extends ExamDatabaseRow {

    public String getCodeString() {
        return codeString;
    }

    public void setCodeString(String codeString) {
        this.codeString = codeString;
    }

    private String codeString;

    Code(ExamDatabase db, int rowID){
        super(db,"codes",rowID);
    }

    Code(ExamDatabase db){
        super(db,"codes");
    }

    public String toString(){
        return codeString;
    }

}
