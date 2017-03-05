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
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;


public class ExamWindow {
    // GUI Elements
    private Stage stage = new Stage();
    private GridPane gridPane = new GridPane();

    private Label lectureLabel = new Label("Vorlesung");
    private ComboBox lectureComboBox = new ComboBox();

    private Label semesterLabel = new Label("Semester");
    private ComboBox semesterComboBox = new ComboBox();

    private Label examinerLabel = new Label("Prüfer");
    private ComboBox examinerComboBox = new ComboBox();

    private Label dateLabel = new Label("Datum");
    private DatePicker datePicker = new DatePicker();


    // Data
    ExamDatabase examDatabase = null;
    ObservableList<Lecture> lectures = null;
    ObservableList<Semester> semesters = null;


    ExamWindow(Stage primaryStage, ExamDatabase db){
        stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(primaryStage);
        stage.setTitle("Neue Klausur");
        stage.setResizable(false);
        examDatabase = db;


        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(15, 10, 15, 10));


        // Lecture
        int row = 0;
        GridPane.setHalignment(lectureLabel, HPos.RIGHT);
        gridPane.add(lectureLabel, 0, row);
        GridPane.setHalignment(lectureComboBox, HPos.LEFT);
        gridPane.add(lectureComboBox, 1, row);
        lectures = FXCollections.observableArrayList(examDatabase.getLectures());
        lectureComboBox.setItems(lectures);


        // Semester
        ++row;
        GridPane.setHalignment(semesterLabel, HPos.RIGHT);
        gridPane.add(semesterLabel, 0, row);
        GridPane.setHalignment(semesterComboBox, HPos.LEFT);
        gridPane.add(semesterComboBox, 1, row);
        semesters = FXCollections.observableArrayList(examDatabase.getSemesters());
        semesterComboBox.setItems(semesters);

        // Examiner
        ++row;
        GridPane.setHalignment(examinerLabel, HPos.RIGHT);
        gridPane.add(examinerLabel, 0, row);
        GridPane.setHalignment(examinerComboBox, HPos.LEFT);
        gridPane.add(examinerComboBox, 1, row);

        // Date
        ++row;
        datePicker.setOnAction(new EventHandler() {
            public void handle(Event t) {
                LocalDate date = datePicker.getValue();
                System.err.println("Selected date: " + date);
            }
        });

        GridPane.setHalignment(dateLabel, HPos.RIGHT);
        gridPane.add(dateLabel, 0, row);
        GridPane.setHalignment(datePicker, HPos.LEFT);
        gridPane.add(datePicker, 1, row);


        // Time
        ++row;
        Label timeLabel = new Label("Uhrzeit (hh:mm)");
        TextField timeTextField = new TextField();
        GridPane.setHalignment(timeLabel, HPos.RIGHT);
        gridPane.add(timeLabel, 0, row);
        GridPane.setHalignment(timeTextField, HPos.LEFT);
        gridPane.add(timeTextField, 1, row);


        // Buttons
        ++row;
        Button cancelButton = new Button("Abbrechen");
        Button saveButton = new Button("Hinzufügen");
        GridPane.setHalignment(cancelButton, HPos.RIGHT);
        gridPane.add(cancelButton, 0, row);
        GridPane.setHalignment(saveButton, HPos.RIGHT);
        gridPane.add(saveButton, 1, row);

        cancelButton.setOnAction((event) -> {
            if (stage != null){
                stage.close();
            }
        });


        saveButton.setOnAction((event) -> {
            /*examDatabase.addParticipant(selectedExam, firstNameTextField.getText(), lastNameTextField.getText(), matriculationTextField.getText());
*/
            if (stage != null){
                stage.close();
            }
        });


        Scene scene = new Scene(gridPane, 500,160+100);

        stage.setScene(scene);
        stage.showAndWait();
    }
}
