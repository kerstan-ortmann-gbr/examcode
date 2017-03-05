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

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.HPos;
import javafx.scene.Scene;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;

import java.io.File;

import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.ObservableList;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.*;
import javafx.util.Callback;
import javafx.util.Pair;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


// TODO
// - use ControlFX (controlsfx.org)??


public class Main extends Application {

    Path applicationSupportPath = null;
    Path imagePath = null;

    // Images; TODO: improve image handling
    Image imgExam;
    Image imgAddExam;
    Image imgRemExam;
    Image imgEditExam;

    Image imgParticipants;
    Image imgAddParticipant;
    Image imgImportParticipant;
    Image imgExportParticipant;
    Image imgRemParticipant;
    Image imgEditParticipant;

    Image imgCorrections;
    Image imgExercises;

    Image imgAddExercise;
    Image imgRemExercise;

    Image imgSemester;


    // GUI elements
    private Stage primaryStage;
    private TabPane tabPane;
    private Tab examTab;
    private ToolBar examsToolBar;
    private ToolBar exercisesToolBar;

    private TableView examDataTable = new TableView();
    private TableView registerTable = new TableView();
    private TableView exerciseTable = new TableView();
    private TableView participationTable = new TableView();
    private TableView correctionTable = new TableView();

    TableColumn codeCol = new TableColumn("Code");


    TableColumn participantColumn = new TableColumn("Code");
    TableColumn bonusPointsColumn = new TableColumn("Bonus");
    TableColumn gradeColumn = new TableColumn("Note");

    private Stage addParticipantDialog = null;
    private Stage addExamDialog = null;


    Button remExerciseButton = new Button();

    Tab registerTab = new Tab();
    Tab exerciseTab = new Tab();
    Tab correctionTab = new Tab();


    private HBox statusBar = new HBox();
    private ExamWindow examWindow = null;

    // private TableView

    private TreeView<ExamDatabaseRow> treeView = new TreeView<ExamDatabaseRow>();



    // Data
    private ExamDatabase examDatabase;
    private Exam selectedExam;
    private ObservableList<Participant> participantsWithCode = null;

    private ObservableList<Code> codes = null;
    private List<Participant> selectedParticipants;
    private List<Exercise> selectedExercises;

    public static void main(String[] args) {
        launch(args);
    }

    private List<Pair<String, String>> examProperties;


    @Override
    public void start(Stage primaryStage) {
        setupApplicationPaths();
        loadResources();

        // set up application icon (in title bar)
        primaryStage.getIcons().add(imgExam);

        this.primaryStage = primaryStage;
        primaryStage.setTitle("Hello World!");
        primaryStage.setWidth(1024);
        primaryStage.setHeight(768);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);

        // Menu
        MenuBar menuBar = new MenuBar();
        Menu semesterMenu = new Menu("Semester");
        Menu examMenu = new Menu("Datenbank");

        MenuItem newExamMenuItem = new MenuItem("Neue Klausur");
        newExamMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                System.out.println("open");
                showAddExamDialog();
            }
        });
        examMenu.getItems().add(newExamMenuItem);

        MenuItem addParticipantMenuItem = new MenuItem("Neuer Teilnehmer");
        addParticipantMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                System.out.println("open");
                showAddParticipantDialog();
            }
        });
        examMenu.getItems().add(addParticipantMenuItem);

        //examMenu.getItems().add(menuOpenFileItem);
        //Menu menuEdit = new Menu("Teilnehmer");
        //Menu menuView = new Menu("Ansicht");
        menuBar.getMenus().addAll(examMenu);


        // add menu to top for Mac
        Menu viewMenu = new Menu("Ansicht");
        MenuItem useSystemMenuBarMenuItem = new MenuItem("Mac Menüleiste");
        viewMenu.getItems().add(useSystemMenuBarMenuItem);
        useSystemMenuBarMenuItem.setOnAction((event) -> {
            boolean currentValue = menuBar.useSystemMenuBarProperty().get();
            menuBar.useSystemMenuBarProperty().set(!currentValue);
            //useSystemMenuBarMenuItem.
        });

        final String os = System.getProperty ("os.name");
        if (os != null && os.startsWith ("Mac")){
            menuBar.getMenus().addAll(viewMenu);
            menuBar.useSystemMenuBarProperty().set(true);
        }

        // setup layout
        BorderPane rootBorderPane = new BorderPane();
        VBox top = new VBox();

        tabPane = new TabPane();

        examTab = new Tab();
        examTab.setText("Allgemeine Klausurdaten");
        examTab.setClosable(false);
        Label noExamSelectedLabel = new Label();
        noExamSelectedLabel.setText("Keine Klausur ausgewählt.");
        examTab.setDisable(true);

        examTab.setContent(noExamSelectedLabel);
        //tabPane.getTabs().add(examTab);

        exerciseTab.setText("Aufgaben");
        exerciseTab.setClosable(false);
        exerciseTab.setDisable(true);
        exerciseTab.setGraphic(new ImageView(imgExercises));
        tabPane.getTabs().add(exerciseTab);
        exerciseTab.setOnSelectionChanged(new EventHandler<Event>() {
            @Override
            public void handle(Event t) {
                if (exerciseTab.isSelected()) {
                    System.out.println("ex");
                }
            }
        });

        registerTab.setText("Teilnehmer");
        registerTab.setGraphic(new ImageView(imgParticipants));
        registerTab.setClosable(false);

        final BorderPane registerTabBorderPane = new BorderPane();
        //registerTabBorderPane.setSpacing(5);
        registerTabBorderPane.setPadding(new Insets(5,5, 5, 5));

        // Buttons for Participants
        Button addParticipantButton = new Button();
        addParticipantButton.setGraphic(new ImageView(imgAddParticipant));
        addParticipantButton.setDisable(true);
        addParticipantButton.setTooltip(new Tooltip("Neuen Teilnehmer hinzufügen"));
        addParticipantButton.setOnAction((event) -> {
            showAddParticipantDialog();
        });

        Button importParticipantButton = new Button();
        importParticipantButton.setGraphic(new ImageView(imgImportParticipant));
        importParticipantButton.setDisable(true);
        importParticipantButton.setTooltip(new Tooltip("Einen oder mehrere Teilnehmer importieren"));

        Button editParticipantButton = new Button();
        editParticipantButton.setGraphic(new ImageView(imgEditParticipant));
        editParticipantButton.setDisable(true);
        editParticipantButton.setTooltip(new Tooltip("Ausgewählte(n) Teilnehmer bearbeiten"));

        Button remParticipantButton = new Button();
        remParticipantButton.setGraphic(new ImageView(imgRemParticipant));
        remParticipantButton.setDisable(true);
        remParticipantButton.setTooltip(new Tooltip("Ausgewählte(n) Teilnehmer entfernen"));
        remParticipantButton.setOnAction((event) -> {

            if (selectedParticipants.size() == 1){
                Participant p = selectedParticipants.get(0);

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setHeaderText("'"+ p.toString() + "' entfernen?");
                alert.setContentText("Wollen Sie '"+ p.getFirstName() + " " + p.getLastName() + " (" + p.getMatriculation() + ")' entfernen? Diese Aktion kann nicht rückgängig gemacht werden!");
                Optional<ButtonType> result = alert.showAndWait();

                if (result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                    examDatabase.removeParticipant(p);
                    reloadParticipants();
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                int numParticipants = selectedParticipants.size();
                alert.setHeaderText(numParticipants + " Teilnehmer entfernen?");
                alert.setContentText("Wollen Sie wirklich die markierten " + numParticipants + " Teilnehmer entfernen? Diese Aktion kann nicht rückgängig gemacht werden!");
                Optional<ButtonType> result = alert.showAndWait();

                if (result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                    for (Participant participant:selectedParticipants){
                        examDatabase.removeParticipant(participant);
                    }
                    reloadParticipants();
                }
            }
        });

        ToolBar registerTabToolBar = new ToolBar(
                addParticipantButton,
                new Separator(),
                editParticipantButton,
                remParticipantButton,
                new Separator(),
                importParticipantButton);
        registerTabBorderPane.setTop(registerTabToolBar);
        registerTabBorderPane.setCenter(registerTable);

        registerTab.setContent(registerTabBorderPane);
        registerTab.setDisable(true);
        registerTab.setOnSelectionChanged(new EventHandler<Event>() {
            @Override
            public void handle(Event t) {
                if (registerTab.isSelected()) {
                    reloadParticipants();
                }
            }
        });
        tabPane.getTabs().add(registerTab);
        tabPane.setDisable(true);

        correctionTab.setText("Korrektur");
        correctionTab.setClosable(false);
        correctionTab.setDisable(true);
        tabPane.getTabs().add(correctionTab);
        correctionTab.setOnSelectionChanged(new EventHandler<Event>() {
            @Override
            public void handle(Event t) {
                if (correctionTab.isSelected()) {
                    System.out.println("correction");
                }
            }
        });

        //tabPane.setDisable(true);

        //Image imgAddExam = new Image(imagePath.toUri() + "/040.png");
        //System.out.println(imgAddExam);
        Button addExamButton = new Button();
        addExamButton.setGraphic(new ImageView(imgAddExam));
        addExamButton.setTooltip(new Tooltip("Neue Klausur hinzufügen"));

        Button remExamButton = new Button();
        remExamButton.setGraphic(new ImageView(imgRemExam));
        remExamButton.setDisable(true);
        remExamButton.setTooltip(new Tooltip("Ausgewählte Klausur entfernen"));

        Button editExamButton = new Button();
        editExamButton.setGraphic(new ImageView(imgEditExam));
        editExamButton.setDisable(true);
        editExamButton.setTooltip(new Tooltip("Ausgewählte Klausur bearbeiten"));

        examsToolBar = new ToolBar(
                addExamButton,
                new Separator(),
                editExamButton,
                remExamButton
        );

        top.getChildren().addAll(menuBar);//, examsToolBar);

        rootBorderPane.setTop(top);

        //BorderPane centerPane = new BorderPane();
        //centerPane.setTop(tabPane);

        // exam chooser (tree)
        TreeItem<ExamDatabaseRow> treeRoot = new TreeItem<>();
        treeView.setRoot(treeRoot);
        treeView.setShowRoot(false);
        treeRoot.setExpanded(true);

        TreeTableColumn<File,java.util.Date> examDateCol = new TreeTableColumn<>("Datum");
        TreeTableColumn<File,String> examNameCol = new TreeTableColumn<>("Klausur");
        //treeView.getColumns().addAll(examDateCol,examNameCol);

        treeView.getSelectionModel().selectedItemProperty().addListener( new ChangeListener() {

            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                TreeItem<ExamDatabaseRow> selectedItem = (TreeItem<ExamDatabaseRow>) newValue;

                // deselect participants
                registerTable.getSelectionModel().clearSelection();
                if (selectedParticipants != null){
                    selectedParticipants.clear();
                }

                remParticipantButton.setDisable(true);
                editParticipantButton.setDisable(true);

                registerTable.getItems().clear();


                // deselect exercises
                exerciseTable.getSelectionModel().clearSelection();
                if (selectedExercises!= null){
                    selectedExercises.clear();
                }
                remExerciseButton.setDisable(true);


                if (selectedItem.getValue() instanceof Semester){
                    // expand?
                    /*if(selectedItem.getChildren().size() > 0){
                        selectedItem.setExpanded(!selectedItem.isExpanded());
                    }*/

                    selectedExam = null;
                    selectedParticipants = null;
                    participantsWithCode = null;

                    // disable buttons
                    editExamButton.setDisable(true);
                    remExamButton.setDisable(true);

                    addParticipantButton.setDisable(true);

                    // disable pane
                    tabPane.setDisable(true);


                } else if (selectedItem.getValue() instanceof Exam){
                    // select new exam
                    System.out.println("selected: " + selectedItem.getValue());
                    selectedExam = (Exam) selectedItem.getValue();
                    reloadParticipants();
                    reloadExercises();
                    reloadCodes();



                    primaryStage.setTitle("Klausur \"" + selectedExam.getLecture().getTitle() + " (" + selectedExam.toString() + ")\"");
                    enableTabs();


                    // enable buttons
                    editExamButton.setDisable(false);
                    remExamButton.setDisable(false);

                    addParticipantButton.setDisable(false);


                    // enable panes
                    tabPane.setDisable(false);

                }


            }

        });


       // root.setLeft(treeView);

        SplitPane splitPane = new SplitPane();
        //treeView.setPrefWidth(250);

        BorderPane examTreeBorderPane = new BorderPane();
        //examTreeVBox.setPa
        examTreeBorderPane.setTop(examsToolBar);
        examTreeBorderPane.setCenter(treeView);

        splitPane.getItems().addAll(examTreeBorderPane, tabPane);
        treeView.setMinWidth(200);
        tabPane.setMinWidth(400);

        splitPane.setResizableWithParent(examTreeBorderPane, false);
        splitPane.setDividerPositions(0.01f);

        rootBorderPane.setCenter(splitPane);

        File file = new File(applicationSupportPath.toFile() + "/exams.db");

        /*FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Klausurdatenbank öffnen");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Klausurdatenbanken (*.db)", "*.db");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Klausurdatenbanken (*.db)", "*.db"),
                new FileChooser.ExtensionFilter("Alle Dateien", "*.*"));

        file = fileChooser.showOpenDialog(primaryStage);*/

        // set up registerTable
        registerTable.setEditable(true);
        registerTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        registerTable.setPlaceholder(new Label("Es sind noch keine Teilnehmer angemeldet."));


        registerTable.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observableValue, Object oldValue, Object newValue) {

                selectedParticipants = new ArrayList<Participant>();
                System.out.println("new selection:");

                TableView.TableViewSelectionModel selectionModel = registerTable.getSelectionModel();

                if(selectionModel.getSelectedItems() != null) {
                    for (Object i: selectionModel.getSelectedItems()) {
                        selectedParticipants.add((Participant) i);
                    }
                    for (Participant p:selectedParticipants){
                        System.out.println("- " + p);
                    }


                    // enable buttons
                    editParticipantButton.setDisable(false);
                    remParticipantButton.setDisable(false);
                } else {
                    // disable buttons
                    editParticipantButton.setDisable(true);
                    remParticipantButton.setDisable(true);
                }
            }
        });

        registerTable.setOnKeyReleased((event) -> {
            KeyCombination markAll = new KeyCodeCombination(KeyCode.A, KeyCombination.SHORTCUT_DOWN);
            if (markAll.match(event)){
                selectedParticipants = selectedExam.getParticipants();
            }
        });

        TableColumn lastNameCol = new TableColumn("Nachname");
        lastNameCol.setCellValueFactory(new PropertyValueFactory<Participant, String>("lastName"));
        lastNameCol.setPrefWidth(140);

        TableColumn firstNameCol = new TableColumn("Vorname");
        firstNameCol.setCellValueFactory(new PropertyValueFactory<Participant, String>("firstName"));
        firstNameCol.setPrefWidth(140);

        TableColumn matriculationCol = new TableColumn("Matrikelnr.");
        matriculationCol.setCellValueFactory(new PropertyValueFactory<Participant, String>("matriculation"));

        TableColumn gradeStringColRegisterTable = new TableColumn("Note");
        gradeStringColRegisterTable.setCellValueFactory(new PropertyValueFactory<Participant, String>("gradeString"));
        gradeStringColRegisterTable.setPrefWidth(90);
        gradeStringColRegisterTable.setStyle("-fx-alignment: CENTER; -fx-font-weight:bold;");

        TableColumn sumCol = new TableColumn("Punkte");
        sumCol.setCellValueFactory(new PropertyValueFactory<Participant, String>("pointsString"));
        sumCol.setPrefWidth(90);
        sumCol.setStyle("-fx-alignment: CENTER;");

        TableColumn courseCol = new TableColumn("Studiengang");
        courseCol.setCellValueFactory(new PropertyValueFactory<Participant, String>("courseName"));
        courseCol.setPrefWidth(180);


        codeCol.setCellValueFactory(new PropertyValueFactory<Participant, Code>("codeString"));
        codeCol.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Participant, Code>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Participant,Code> t) {
                        Participant p = (Participant) t.getTableView().getItems().get(t.getTablePosition().getRow());
                        Code c = (Code) t.getNewValue();

                        if (c.getRowID() > 0){
                            if (p.getCode().getRowID() == 0){
                                examDatabase.assignCodeToParticipant(c,p);
                            } else {
                                // issue warning
                                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                                alert.setHeaderText("Zuordnung ändern");
                                alert.setContentText("Dem Teilnehmer '"+ p.getFirstName() + " " + p.getLastName() + " (" + p.getMatriculation() + ")' ist momentan der Code '"+ p.getCodeString() + "' zugewiesen. Möchten Sie diese Zuordnung ersetzen durch den neuen Code '"+c.getCodeString() + "'?");
                                Optional<ButtonType> result = alert.showAndWait();

                                if (result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                                    examDatabase.assignCodeToParticipant(c,p);
                                }
                            }
                        } else {

                            if (p.getCode().getRowID() == 0){
                                examDatabase.clearCodeOfParticipant(p);
                            } else {
                                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                                alert.setHeaderText("Zuordnung aufheben");
                                alert.setContentText("Dem Teilnehmer '" + p.getFirstName() + " " + p.getLastName() + " (" + p.getMatriculation() + ")' ist momentan der Code '" + p.getCodeString() + "' zugewiesen. Möchten Sie diese Zuweisung aufheben?");
                                Optional<ButtonType> result = alert.showAndWait();

                                if (result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                                    examDatabase.clearCodeOfParticipant(p);
                                }
                            }
                        }

                        reloadParticipants();
                        reloadCodes();
                    };
                }
        );
        codeCol.setPrefWidth(80);
        codeCol.setStyle( "-fx-alignment: CENTER; -fx-font-weight: bold");// -fx-font-family: monospace; -fx-font-weight: bold");

        //TableColumn gradeCol = new TableColumn("Note");
        //gradeCol.setCellValueFactory(new PropertyValueFactory<Participant, String>("grade"));

        TableColumn attemptCol = new TableColumn("Versuch");
        attemptCol.setCellValueFactory(new PropertyValueFactory<Participant, Integer>("attempt"));
        attemptCol.setStyle( "-fx-alignment: CENTER;");
        attemptCol.setMaxWidth(80);
        attemptCol.setMinWidth(80);

        TableColumn fileCol = new TableColumn("Datei");
        fileCol.setCellValueFactory(new PropertyValueFactory<Participant, String>("fileName"));
        fileCol.setPrefWidth(120);



        /*TableColumn registeredCol = new TableColumn("Angemeldet");
        registeredCol.setCellValueFactory(new PropertyValueFactory<Participant, Boolean>("registered"));
        registeredCol.setCellFactory(CheckBoxTableCell.forTableColumn(registeredCol));
        registeredCol.setEditable(true);*/


        registerTable.getColumns().addAll(codeCol, lastNameCol, firstNameCol, matriculationCol, gradeStringColRegisterTable, sumCol, courseCol, attemptCol, fileCol);

        // Correction table

        participantColumn.setCellValueFactory(new PropertyValueFactory<Participant, String>("codeString"));
        participantColumn.setPrefWidth(120);
        participantColumn.setStyle("-fx-font-weight: bold;");


        bonusPointsColumn.setCellValueFactory(new PropertyValueFactory<Participant, String>("bonusPointsString"));
        bonusPointsColumn.setPrefWidth(70);
        bonusPointsColumn.setStyle( "-fx-alignment: CENTER;");



        gradeColumn.setCellValueFactory(new PropertyValueFactory<Participant, String>("gradeString"));
        gradeColumn.setPrefWidth(70);
        gradeColumn.setStyle( "-fx-alignment: CENTER;");



        correctionTable.getColumns().addAll(participantColumn,bonusPointsColumn,gradeColumn);
        correctionTable.setPlaceholder(new Label("Es sind noch keine Teilnehmer erfasst.\nWeisen Sie zunächst im 'Teilnehmer'-Tab den Teilnehmern ihren Klausurcode zu."));
        correctionTable.setEditable(true);
        correctionTab.setContent(correctionTable);

        correctionTab.setGraphic(new ImageView(imgCorrections));


        Scene scene = new Scene(rootBorderPane, 400,700);


        // General Exam Data
        examDataTable.setEditable(false);
        examDataTable.setPlaceholder(new Label("Fehler: Keine Klausurdaten gefunden."));
        TableColumn keyCol = new TableColumn("Parameter");
        //keyCol.setCellValueFactory(new MapValueFactory<>());
        //keyCol.setCellValueFactory(new PropertyValueFactory<Exercise, Integer>("number"));
        TableColumn valCol = new TableColumn("Wert");
        examDataTable.getColumns().addAll(keyCol, valCol);

        examTab.setContent(examDataTable);


        // Exercises
        exerciseTable.setEditable(true);
        exerciseTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        exerciseTable.setPlaceholder(new Label("Es sind noch keine Aufgaben vorhanden."));

        TableColumn exerciseNumberCol = new TableColumn("Aufgabe");
        exerciseNumberCol.setCellValueFactory(new PropertyValueFactory<Exercise, Integer>("number"));
        exerciseNumberCol.setPrefWidth(140);

        TableColumn exercisePointsCol = new TableColumn("Maximalpunktzahl");
        exercisePointsCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures, ObservableValue>() {
            @Override
            public ObservableValue call(TableColumn.CellDataFeatures param) {
                Exercise exercise = (Exercise) param.getValue();
                Double points = new Double(exercise.getPoints());
                return new SimpleStringProperty(points.toString());
            }
        });
        exercisePointsCol.setCellFactory(TextFieldTableCell.forTableColumn());
        exercisePointsCol.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Exercise, String>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Exercise, String> event) {
                        Exercise exercise = (Exercise) event.getRowValue();

                        // check if value can be parsed to double
                        double value = 0;
                        try {
                            value = Double.parseDouble( event.getNewValue().replace(",",".") );
                            if (value <= 0){
                                throw new IllegalArgumentException("Punktzahl muss positiv sein.");
                            }

                        } catch (Exception e){

                            // display alert message
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setHeaderText("Ungültiger Wert");
                            alert.setContentText("Bitte geben Sie eine positive Fließkommazahl ohne Dezimaltrennzeichen ein.");
                            alert.show();

                            // refresh cell value; TODO: is there a better way to do this????
                            event.getTableView().getColumns().get(0).setVisible(false);
                            event.getTableView().getColumns().get(0).setVisible(true);

                            return;
                        }

                        //System.out.println("new value as double: " + value);
                        exercise.setPoints(value);

                        examDatabase.updateExercise(exercise);
                        reloadExercises();
                    }
                }
        );
        exercisePointsCol.setPrefWidth(140);

        exerciseTable.getColumns().addAll(exerciseNumberCol, exercisePointsCol);


        exerciseTable.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                TableView.TableViewSelectionModel selectionModel = exerciseTable.getSelectionModel();

                selectedExercises = new ArrayList<Exercise>();

                if(selectionModel.getSelectedItems() != null) {
                    System.out.println("Aufgaben ausgewählt:");
                    for (Object i: selectionModel.getSelectedItems()) {
                        selectedExercises.add((Exercise) i);
                    }
                    for (Exercise e:selectedExercises){
                        System.out.println("- " + e);
                    }


                    // enable buttons
                    remExerciseButton.setDisable(false);
                } else {
                    // disable buttons
                    remExerciseButton.setDisable(true);
                }
            }
        });


        // Buttons for Exercises
        Button addExerciseButton = new Button();
        addExerciseButton.setGraphic(new ImageView(imgAddExercise));
        addExerciseButton.setDisable(false);
        addExerciseButton.setTooltip(new Tooltip("Neue Aufgabe hinzufügen"));
        addExerciseButton.setOnAction((event) -> {
            if (selectedExam != null) {
                examDatabase.addExercise(selectedExam);
                reloadExercises();
            }
        });

        remExerciseButton.setGraphic(new ImageView(imgRemExercise));
        remExerciseButton.setDisable(true);
        remExerciseButton.setTooltip(new Tooltip("Gewählte Aufgabe(n) entfernen"));
        remExerciseButton.setOnAction((event) -> {

            if (selectedExercises.size() == 1){
                Exercise exercise = selectedExercises.get(0);

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setHeaderText("Aufgabe "+ exercise.getNumber() + " entfernen?");
                alert.setContentText("Wollen Sie Aufgabe "+ exercise.getNumber() + " entfernen? Diese Aktion kann nicht rückgängig gemacht werden!");
                Optional<ButtonType> result = alert.showAndWait();

                if (result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                    examDatabase.removeExercise(exercise);
                    selectedExercises.clear();
                    reloadExercises();
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                int numExercises = selectedExercises.size();
                alert.setHeaderText(numExercises + " Aufgaben entfernen?");
                alert.setContentText("Wollen Sie wirklich die markierten " + numExercises + " Aufgaben entfernen? Diese Aktion kann nicht rückgängig gemacht werden!");
                Optional<ButtonType> result = alert.showAndWait();

                if (result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                    for (Exercise exercise:selectedExercises){
                        examDatabase.removeExercise(exercise);
                    }
                    selectedExercises.clear();
                    reloadExercises();
                }
            }
        });

        exercisesToolBar = new ToolBar(
                addExerciseButton,
                new Separator(),
                remExerciseButton
        );

        BorderPane exercisesBorderPane = new BorderPane();
        exercisesBorderPane.setTop(exercisesToolBar);
        exercisesBorderPane.setCenter(exerciseTable);

        exerciseTab.setContent(exercisesBorderPane);

        if(file != null){

            primaryStage.setTitle("Klausurdatenbank: " + file.getName());
            examDatabase = ExamDatabase.getInstance(file);
            System.out.println("Opened database successfully");

            ExamDatabase db2 = ExamDatabase.getInstance(file);

            reloadParticipants();

            ArrayList<Semester> semesters = examDatabase.getSemesters();

            for (Semester s:semesters){
                ArrayList<Exam> exams = examDatabase.getExams(s);

                if (exams.size() > 0){
                    TreeItem<ExamDatabaseRow> semesterNode = new TreeItem<>(s);//(s.toString() + " (" + exams.size() + ")");
                    semesterNode.setGraphic(new ImageView(imgSemester));
                    treeRoot.getChildren().add(semesterNode);

                    for(Exam e:exams){
                        TreeItem<ExamDatabaseRow> examLeaf = new TreeItem<>(e);// = new TreeItem<String>(e.getLecture().getShortTitle());
                        examLeaf.setGraphic(new ImageView(imgExam));
                        semesterNode.getChildren().add(examLeaf);
                        System.out.println("- " + e.toString());
                    }
                }
            }

            //treeView.
            if(treeRoot.getChildren() != null && treeRoot.getChildren().size() > 0){
                if (treeRoot.getChildren().get(0).getChildren() != null){
                    if (treeRoot.getChildren().get(0).getChildren().size() > 0){
                        treeRoot.getChildren().get(0).setExpanded(true);
                    }
                }
            }
        }

        Label testLabel = new Label("ExamCode (Java Version). (c) 2016-2017 Kerstan und Ortmann GbR.");
        statusBar.setPadding(new Insets(5, 5, 5, 5)); // top, left, right, bottom?
        statusBar.getChildren().add(testLabel);

        rootBorderPane.setBottom(statusBar);

        primaryStage.setScene(scene);

        primaryStage.show();
    }

    private void enableTabs(){
        for (Tab t:tabPane.getTabs()){
            t.setDisable(false);
        }
    }


    private void openFileOpenDialog(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Klausurdatenbank öffnen");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Klausurdatenbanken (*.db)", "*.db");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Klausurdatenbanken (*.db)", "*.db"),
                new FileChooser.ExtensionFilter("Alle Dateien", "*.*"));

        File file = fileChooser.showOpenDialog(primaryStage);

        if (file != null){
            examDatabase = ExamDatabase.getInstance(file);
        }
    }

    private void reloadExercises(){
        //exercises = FXCollections.observableArrayList(examDatabase.getExercises(selectedExam));
        selectedExam.reloadExercises();
        ObservableList<Exercise> exercises = selectedExam.getExercises();
        exerciseTable.setItems(exercises);
        exerciseTab.setText("Aufgaben (" + exercises.size() + ")");

        // reconstruct correction table

        correctionTable.getColumns().clear();

        correctionTable.getColumns().add(participantColumn);
        TableColumn exerciseColumn = new TableColumn("Aufgaben");
        exerciseColumn.setMinWidth(90);
        exerciseColumn.setPrefWidth(90);

        for(Exercise e:exercises){
            TableColumn<Participant, String> col = new TableColumn<>(e.getNumber().toString());
            col.setStyle( "-fx-alignment: CENTER;");

            ObservableList<String> pointSelection = FXCollections.observableArrayList();

            pointSelection.add("?");
            pointSelection.add("-.-");

            for (Double i = new Double(0); i < e.getPoints(); i += 0.5){
                pointSelection.add(i.toString());
            }

            col.setCellFactory(ComboBoxTableCell.forTableColumn(pointSelection));
            col.setEditable(true);
            col.setPrefWidth(70);

            col.setUserData(e);


            col.setCellValueFactory(cellData -> {
                Participant p = (Participant) cellData.getValue();
                Correction c = examDatabase.getCorrection(p,e);

                if (c.getRowID() == 0){
                    return new SimpleStringProperty("?");
                } else {
                    Double points = new Double(c.getPoints());

                    if (points.doubleValue() >= 0){
                        return new SimpleStringProperty(points.toString());
                    } else {
                        return new SimpleStringProperty("-.-");
                    }
                }
            });


            col.setOnEditCommit(
                    new EventHandler<TableColumn.CellEditEvent<Participant, String>>() {
                        @Override
                        public void handle(TableColumn.CellEditEvent<Participant, String> t) {
                            Participant p = (Participant) t.getTableView().getItems().get(t.getTablePosition().getRow());

                            // find exercise & correction
                            Exercise e = (Exercise) t.getTableColumn().getUserData();
                            Correction c = (Correction) examDatabase.getCorrection(p,e);

                            System.out.println("Corr: " + c);

                            //t.getTableColumn().
                            String pointString = (String) t.getNewValue();

                            if (pointString.equals("-.-")){
                                System.out.println("nicht bearbeitet");
                                c.setPoints(-1);

                                if (c.getRowID() == 0){
                                    examDatabase.addCorrection(c);
                                } else {
                                    examDatabase.updateCorrection(c);
                                }

                            } else if (pointString.equals("?")){
                                System.out.println("remove");
                                if (c.getRowID() > 0) {
                                    examDatabase.deleteCorrection(c);
                                }
                            } else {
                                System.out.println("set points");
                                Double points = Double.parseDouble(pointString);
                                c.setPoints(points.doubleValue());

                                if (c.getRowID() == 0){
                                    examDatabase.addCorrection(c);
                                } else {
                                    examDatabase.updateCorrection(c);
                                    System.out.println("update");
                                }
                            }
                            reloadParticipants();
                        }
                    }
            );

            exerciseColumn.getColumns().add(col);
        }

        TableColumn sumColumn = new TableColumn("Summe");
        sumColumn.setCellValueFactory(new PropertyValueFactory<Participant, Double>("pointsString"));
        sumColumn.setStyle( "-fx-alignment: CENTER;");
        gradeColumn.setStyle("-fx-alignment:CENTER; -fx-font-weight: bold");
        gradeColumn.setPrefWidth(70);

        correctionTable.getColumns().addAll(exerciseColumn,bonusPointsColumn,sumColumn, gradeColumn);
    }

    private void reloadCodes(){
        codes = FXCollections.observableArrayList(examDatabase.getAvailableCodes(selectedExam));
        Code c = new Code(examDatabase,0);
        c.setCodeString("- entfernen -");
        codes.add(0,c);
        System.out.println("Verfügbare Codes: " + codes.size());
        codeCol.setCellFactory(ComboBoxTableCell.forTableColumn(codes));

        //codeCol.cellFactoryProperty().set
    }

    private void reloadParticipants(){
        selectedParticipants = null;
        participantsWithCode = FXCollections.observableArrayList();

        if (selectedExam != null){
            selectedExam.reloadParticipants();
        }
        ObservableList<Participant> participants = selectedExam != null ? selectedExam.getParticipants() : FXCollections.observableArrayList();

        registerTable.setItems(participants);

        int participating = 0;
        int corrected = 0;

        for (Participant p:participants){
            if(p.getParticipated()){
                participating++;
            }

            if(p.getCode().getRowID() > 0) {
                participantsWithCode.add(p);

                // TODO: implement check for correction complete?

            }
        }

        Collections.sort(participantsWithCode, new Comparator<Participant>() {
            @Override
            public int compare(Participant p1, Participant p2)
            {
                return  p1.getCodeString().compareTo(p2.getCodeString());
            }
        });
        correctionTable.setItems(participantsWithCode);

        if (selectedExam != null){
            registerTab.setText("Teilnehmer (" + participating + "/" + participants.size() + ")");
            correctionTab.setText("Korrektur (" + selectedExam.getFinishedCorrectionCount() + "/" + participating + ")");
        } else {
            registerTab.setText("Teilnehmer");
            correctionTab.setText("Korrektur");
        }
    }


    private void showAddExamDialog(){
        examWindow = new ExamWindow(primaryStage, examDatabase);

    }

    private void showAddParticipantDialog(){

        if(selectedExam == null){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Keine Klausur ausgewählt.");
            Optional<ButtonType> result = alert.showAndWait();
            return;
        }

        addParticipantDialog = new Stage();
        addParticipantDialog.initModality(Modality.WINDOW_MODAL);
        addParticipantDialog.initOwner(primaryStage);
        addParticipantDialog.setTitle("Neuer Teilnehmer");
        addParticipantDialog.setResizable(false);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15, 10, 15, 10));


        // First name
        Label firstNameLabel = new Label("Vorname");
        TextField firstNameTextField = new TextField();
        GridPane.setHalignment(firstNameLabel, HPos.RIGHT);
        grid.add(firstNameLabel, 0, 0);
        GridPane.setHalignment(firstNameTextField, HPos.LEFT);
        grid.add(firstNameTextField, 1, 0);

        // Last name
        Label lastNameLabel = new Label("Nachname");
        TextField lastNameTextField = new TextField();
        GridPane.setHalignment(lastNameLabel, HPos.RIGHT);
        grid.add(lastNameLabel, 0, 1);
        GridPane.setHalignment(lastNameTextField, HPos.LEFT);
        grid.add(lastNameTextField, 1, 1);

        // Matriculation
        Label matriculationLabel = new Label("Matrikelnr.");
        TextField matriculationTextField = new TextField();
        GridPane.setHalignment(matriculationLabel, HPos.RIGHT);
        grid.add(matriculationLabel, 0, 2);
        GridPane.setHalignment(matriculationTextField, HPos.LEFT);
        grid.add(matriculationTextField, 1, 2);

        // Buttons
        Button cancelButton = new Button("Abbrechen");
        Button saveButton = new Button("Hinzufügen");
        GridPane.setHalignment(cancelButton, HPos.RIGHT);
        grid.add(cancelButton, 0, 3);
        GridPane.setHalignment(saveButton, HPos.RIGHT);
        grid.add(saveButton, 1, 3);

        cancelButton.setOnAction((event) -> {
            if (addParticipantDialog != null){
                addParticipantDialog.close();
            }
        });

        saveButton.setOnAction((event) -> {
            examDatabase.addParticipant(selectedExam, firstNameTextField.getText(), lastNameTextField.getText(), matriculationTextField.getText());

            if (addParticipantDialog != null){
                addParticipantDialog.close();
            }

            reloadParticipants();
            reloadCodes();
        });

        Scene scene = new Scene(grid, 280,160);

        addParticipantDialog.setScene(scene);
        addParticipantDialog.showAndWait();
    }

    void setupApplicationPaths() {
        // TODO: make the following code cross platform
        String dir = System.getProperty("user.home") + "/Library/Application Support/de.klang-technik.ExamCode"; // should not be stored in App. Support!

        applicationSupportPath = Paths.get(dir);

        imagePath = Paths.get(applicationSupportPath.toString() + "/img");

        System.out.println(imagePath);

       // return new Files.(dir);
    }


    protected Image getImageResource(String name){
        //URL url = this.getClass().getResource("/img/"+name);
        String fileName = "file:"+imagePath.toString() + "/" + name;

        try {
            return new Image(fileName);
        }
        catch (Exception e){
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            //System.exit(0);
            return null;
        }
    }

    void loadResources(){
        imgExam = getImageResource("exam.png");
        imgAddExam = getImageResource("addExam.png");
        imgRemExam = getImageResource("remExam.png");
        imgEditExam = getImageResource("editExam.png");
        imgParticipants = getImageResource("participants.png");
        imgAddParticipant = getImageResource("addParticipant.png");
        imgImportParticipant = getImageResource("importParticipant.png");
        imgRemParticipant = getImageResource("remParticipant.png");
        imgEditParticipant = getImageResource("editParticipant.png");
        imgCorrections = getImageResource("corrections.png");
        imgExercises = getImageResource("exercises.png");
        imgAddExercise = getImageResource("addExercise.png");
        imgRemExercise = getImageResource("remExercise.png");
        imgExportParticipant = getImageResource("exportParticipant.png");
        imgSemester = getImageResource("semester.png");
    }

    void exportParticipantsList(){

    }
}
