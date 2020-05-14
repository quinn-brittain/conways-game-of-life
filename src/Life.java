import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;

/**
 * @author Quinn Brittain
 *
 * Conway's Game of life
 *
 * In Memory of John Conway (Dec 1937 - Apr 2020)
 * He helped us see the beauty of math in everything.
 */

public class Life extends Application {

    // Dimensions of cell grid
    private static final int DIM = 32;
    private static final int CELLDIM = 24;

    // Create and initialize cell
    private Cell[][] cell = new Cell[DIM][DIM];
    private boolean lastStatus;

    // Create style
    private String liveColor = "green";
    private String deadColor = "black";
    private int maxDeathCount = 7;
    private String[] deathColor = new String[maxDeathCount];
    private String[] growthColor = new String[3];
    private String gridColor = "#303030";
    private String grid = "-fx-border-color: " + gridColor;

    // Global Menu Items
    private RadioMenuItem menuItemMirrorHorizontal = new RadioMenuItem("Horizontal");
    private RadioMenuItem menuItemMirrorVertical = new RadioMenuItem("Vertical");
    private RadioMenuItem menuItemMirrorBoth = new RadioMenuItem("Both");
    private RadioMenuItem menuItemMirrorDiagonal = new RadioMenuItem("Diagonal");
    private RadioMenuItem menuItemDuplicateHorizontal = new RadioMenuItem("Horizontal");
    private RadioMenuItem menuItemDuplicateVertical = new RadioMenuItem("Vertical");
    private RadioMenuItem menuItemDuplicateBoth = new RadioMenuItem("Both");
    private RadioMenuItem menuItemDuplicateDiagonal = new RadioMenuItem("Diagonal");


    private RadioMenuItem menuItemThemeDark = new RadioMenuItem("Dark");
    private RadioMenuItem menuItemThemeLight = new RadioMenuItem("Light");

    private RadioMenuItem menuItemTrailNone = new RadioMenuItem("None");
    private RadioMenuItem menuItemTrailDeath = new RadioMenuItem("Death");
    private RadioMenuItem menuItemTrailFrequency = new RadioMenuItem("Frequency");

    private CheckMenuItem menuItemFilterGrowth = new CheckMenuItem("Growth");

    private CheckMenuItem menuItemGrid = new CheckMenuItem("Grid");
    private CheckMenuItem menuItemGrayscale = new CheckMenuItem("Greyscale");
    private CheckMenuItem menuItemNumbers = new CheckMenuItem("Numbers");

    private MenuItem menuItemPlay = new MenuItem("Play");
    private MenuItem menuItemStop = new MenuItem("Stop");
    private MenuItem menuItemStep = new MenuItem("Step");

    // Global Controls
    private Button btStep = new Button("Step");
    private Button btPlay = new Button("Play");
    private Slider slRate = new Slider();
    private RadioButton rbLife = new RadioButton("Life");
    private RadioButton rbHighLife = new RadioButton("High Life");

    // Timeline
    private boolean playing = false;
    private Timeline animation = new Timeline(new KeyFrame(Duration.millis(3000), e -> calc()));

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Create menu and menu items
        MenuBar menuBar = new MenuBar();
        Menu menuFile = new Menu("File");
        Menu menuEdit = new Menu("Edit");
        Menu menuView = new Menu("View");
        Menu menuRun = new Menu("Run");

        menuBar.getMenus().addAll(menuFile, menuEdit, menuView, menuRun);

        // MenuFile
        MenuItem menuItemNewGame = new MenuItem("New Game");
        MenuItem menuItemSaveAs = new MenuItem("Save As...");
        MenuItem menuItemLoadGame = new MenuItem("Load Game");
        MenuItem menuItemExit = new MenuItem("Quit");

        menuFile.getItems().addAll(menuItemNewGame, new SeparatorMenuItem(), menuItemSaveAs, menuItemLoadGame, new SeparatorMenuItem(), menuItemExit);

        // MenuFile event handling
        menuItemNewGame.setOnAction(e -> newGame());
        menuItemSaveAs.setOnAction(e -> saveAs(primaryStage));
        menuItemLoadGame.setOnAction(e -> loadGame(primaryStage));
        menuItemExit.setOnAction(e -> System.exit(0));

        menuItemNewGame.setAccelerator(
                KeyCombination.keyCombination("Ctrl+N"));
        menuItemSaveAs.setAccelerator(
                KeyCombination.keyCombination("Ctrl+S"));
        menuItemLoadGame.setAccelerator(
                KeyCombination.keyCombination("Ctrl+L"));
        menuItemExit.setAccelerator(
                KeyCombination.keyCombination("Ctrl+Q"));

        // MenuEdit
        Menu subMenuMirror = new Menu("Mirror");
        ToggleGroup tgMirror = new ToggleGroup();
        RadioMenuItem menuItemMirrorNone = new RadioMenuItem("None");
        menuItemMirrorNone.setToggleGroup(tgMirror);
        menuItemMirrorHorizontal.setToggleGroup(tgMirror);
        menuItemMirrorVertical.setToggleGroup(tgMirror);
        menuItemMirrorBoth.setToggleGroup(tgMirror);
        menuItemMirrorDiagonal.setToggleGroup(tgMirror);
        menuItemMirrorNone.setSelected(true);
        subMenuMirror.getItems().addAll(menuItemMirrorNone, menuItemMirrorHorizontal, menuItemMirrorVertical, menuItemMirrorBoth, menuItemMirrorDiagonal);

        Menu subMenuDuplicate = new Menu("Duplicate");
        ToggleGroup tgDuplicate = new ToggleGroup();
        RadioMenuItem menuItemDuplicateNone = new RadioMenuItem("None");
        menuItemDuplicateNone.setToggleGroup(tgDuplicate);
        menuItemDuplicateHorizontal.setToggleGroup(tgDuplicate);
        menuItemDuplicateVertical.setToggleGroup(tgDuplicate);
        menuItemDuplicateBoth.setToggleGroup(tgDuplicate);
        menuItemDuplicateDiagonal.setToggleGroup(tgDuplicate);
        menuItemDuplicateNone.setSelected(true);
        subMenuDuplicate.getItems().addAll(menuItemDuplicateNone, menuItemDuplicateHorizontal, menuItemDuplicateVertical, menuItemDuplicateBoth, menuItemDuplicateDiagonal);

        MenuItem menuItemClear = new MenuItem("Clear");

        menuEdit.getItems().addAll(subMenuMirror, subMenuDuplicate, new SeparatorMenuItem(), menuItemClear);

        // MenuEdit event handling
        menuItemClear.setOnAction(e -> clear());

        menuItemClear.setAccelerator(
                KeyCombination.keyCombination("Shift+Delete")
        );

        // MenuView
        Menu subMenuTheme = new Menu("Theme");
        ToggleGroup tgTheme = new ToggleGroup();
        menuItemThemeDark.setToggleGroup(tgTheme);
        menuItemThemeLight.setToggleGroup(tgTheme);
        menuItemThemeDark.setSelected(true);
        subMenuTheme.getItems().addAll(menuItemThemeDark, menuItemThemeLight);

        Menu subMenuTrail = new Menu("Trail");
        ToggleGroup tgTrail = new ToggleGroup();
        menuItemTrailNone.setToggleGroup(tgTrail);
        menuItemTrailDeath.setToggleGroup(tgTrail);
        menuItemTrailFrequency.setToggleGroup(tgTrail);
        menuItemTrailNone.setSelected(true);
        subMenuTrail.getItems().addAll(menuItemTrailNone, menuItemTrailDeath, menuItemTrailFrequency);

        Menu subMenuFilter = new Menu("Filters");
        menuItemFilterGrowth.setSelected(false);
        subMenuFilter.getItems().addAll(menuItemFilterGrowth);

        menuItemGrid.setSelected(true);
        menuItemGrayscale.setSelected(false);
        menuItemNumbers.setSelected(false);

        menuView.getItems().addAll(subMenuTheme, subMenuTrail, subMenuFilter, new SeparatorMenuItem(), menuItemGrid, menuItemGrayscale, menuItemNumbers);

        // MenuView event handling
        menuItemThemeDark.setOnAction(e -> setTheme());
        menuItemThemeLight.setOnAction(e -> setTheme());
        menuItemTrailNone.setOnAction(e -> setTrail());
        menuItemTrailDeath.setOnAction(e -> setTrail());
        menuItemTrailFrequency.setOnAction(e -> setTrail());
        menuItemFilterGrowth.setOnAction(e -> setGrowth());
        menuItemGrid.setOnAction(e -> toggleGrid());
        menuItemGrayscale.setOnAction(e -> toggleGreyscale());
        menuItemNumbers.setOnAction(e -> toggleNumbers());

        menuItemGrid.setAccelerator(
                KeyCombination.keyCombination("Ctrl+G")
        );
        menuItemGrayscale.setAccelerator(
                KeyCombination.keyCombination("Ctrl+B")
        );
        menuItemNumbers.setAccelerator(
                KeyCombination.keyCombination("Ctrl+C")
        );

        // MenuRun
        menuItemStop.setDisable(true);

        menuRun.getItems().addAll(menuItemPlay, menuItemStop, new SeparatorMenuItem(), menuItemStep);

        // MenuRun event handling
        menuItemPlay.setOnAction(e -> play());
        menuItemStop.setOnAction(e -> stopLife());
        menuItemStep.setOnAction(e -> calc());

        menuItemPlay.setAccelerator(
                KeyCombination.keyCombination("Shift+F10")
        );
        menuItemStop.setAccelerator(
                KeyCombination.keyCombination("Shift+F2")
        );
        menuItemStep.setAccelerator(
                KeyCombination.keyCombination("Shift+Space")
        );

        // Pane to hold cells
        GridPane cellPane = new GridPane();
        for (int i = 0; i < DIM; i++)
            for (int j = 0; j < DIM; j++) {
                cellPane.add(cell[i][j] = new Cell(), j, i);
                cell[i][j].pos = new int[] {i, j};
            }

        for (int i = 0; i < maxDeathCount; i++) {
            deathColor[i] = deadColor;
        }

        growthColor[0] = "#335500";
        growthColor[1] = null;
        growthColor[2] = "#003322";

        // Control bar and controls
        HBox controlBar = new HBox();
        btPlay.setDefaultButton(true);
        Text txtRate = new Text("Rate:");
        Button btClear = new Button("Clear");

        VBox vBoxVersion = new VBox(5);
        vBoxVersion.setPadding(new Insets(5, 5, 5, 5));
        vBoxVersion.getChildren().addAll(rbLife, rbHighLife);
        vBoxVersion.setPadding(new Insets(5, 5, 5, 5));
        ToggleGroup tgVersion = new ToggleGroup();
        rbLife.setToggleGroup(tgVersion);
        rbHighLife.setToggleGroup(tgVersion);
        rbLife.setSelected(true);

        controlBar.getChildren().addAll(btStep, btPlay, txtRate, slRate, btClear, vBoxVersion);
        controlBar.setAlignment(Pos.CENTER);
        controlBar.setSpacing(20);

        // Control bar event handling
        btStep.setOnAction(e -> calc());
        btPlay.setOnAction(e -> play());
        btClear.setOnAction(e -> clear());
        rbLife.setOnAction(event -> setVersion());
        rbHighLife.setOnAction(event -> setVersion());

        // Order UI elements
        BorderPane borderPane = new BorderPane();
        borderPane.setTop(menuBar);
        borderPane.setCenter(cellPane);
        borderPane.setBottom(controlBar);

        // Create a scene and place it in the stage
        Scene scene = new Scene(borderPane);
        primaryStage.setTitle("Game of Life");
        primaryStage.getIcons().add(new Image(Life.class.getResourceAsStream("resources/Life.png")));
        primaryStage.setScene(scene);
        primaryStage.show();

        // Scene Event Handling
        scene.addEventFilter(MouseEvent.DRAG_DETECTED, e -> scene.startFullDrag());

        // Animation
        slRate.setValue(50);
        animation.setCycleCount(Timeline.INDEFINITE);
        animation.rateProperty().bind(slRate.valueProperty());
    }

    private void newGame() {
        stopLife();
        for (int i = 0; i < DIM; i++)
            for (int j = 0; j < DIM; j++) {
                cell[i][j].setCell(false);
                cell[i][j].deathCount = 0;
                cell[i][j].setLiveCount(0);
            }
        calcGrowth();
        rbLife.setSelected(true);
        refreshCells();
    }

    private void saveAs(Stage primaryStage) {
        stopLife();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File("."));
        fileChooser.setTitle("Enter file name");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Game Save files", "*.sav"),
                new FileChooser.ExtensionFilter("All File Types", "*"));
        File selectedFile = fileChooser.showSaveDialog(primaryStage);
        if (selectedFile != null) {
            String filePath = selectedFile.getAbsolutePath();
            if(!filePath.endsWith(".sav")) {
                selectedFile = new File(filePath.concat(".sav"));
            }
            try ( // Create an output stream for file object.sav
                  ObjectOutputStream output =
                          new ObjectOutputStream(new FileOutputStream(selectedFile))) {
                boolean[][] cellStates = new boolean[DIM][DIM];
                for (int i = 0; i < DIM; i++)
                    for (int j = 0; j < DIM; j++)
                        cellStates[i][j] = cell[i][j].getCell();
                output.writeObject(cellStates);
                output.writeObject(rbLife.isSelected());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void loadGame(Stage primaryStage) {
        stopLife();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File("."));
        fileChooser.setTitle("Enter file name");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Game Save files", "*.sav"),
                new FileChooser.ExtensionFilter("All File Types", "*"));
        File selectedFile = fileChooser.showOpenDialog(primaryStage);
        if (selectedFile != null)
            try {
                try ( // Create an input stream for file object.sav
                      ObjectInputStream input =
                              new ObjectInputStream(new FileInputStream(selectedFile))) {
                        boolean[][] cellStates = (boolean[][]) (input.readObject());
                        for (int i = 0; i < DIM; i++)
                            for (int j = 0; j < DIM; j++)
                                cell[i][j].setCell(cellStates[i][j]);
                        if ((boolean) input.readObject())
                            rbLife.setSelected(true);
                        else
                            rbHighLife.setSelected(true);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        calcNumbers();
        if (menuItemFilterGrowth.isSelected())
            calcGrowth();
        refreshCells();
    }

    private void setVersion() {
        if (menuItemFilterGrowth.isSelected())
            calcGrowth();
    }

    private void refreshCells() {
        for (int i = 0; i < DIM; i++)
            for (int j = 0; j < DIM; j++) {
                cell[i][j].setCell(cell[i][j].getCell());
            }
    }

    private void setTheme() {
        if (menuItemThemeDark.isSelected()) {
            deadColor = "black";
            liveColor = "green";
            gridColor = "#303030";
        } else if (menuItemThemeLight.isSelected()) {
            deadColor = "white";
            liveColor = "green";
            gridColor = "#a0a0a0";
        }
        deathColor[0] = deadColor;
        for (int i = 1; i < maxDeathCount; i++) {
            deathColor[i] = deadColor;
        }
        if (menuItemGrayscale.isSelected())
            toggleGreyscale();
        if (menuItemFilterGrowth.isSelected())
            setGrowth();
        if (menuItemTrailDeath.isSelected() || menuItemTrailFrequency.isSelected())
            setTrail();
        if (menuItemGrid.isSelected()) {
            grid = "-fx-border-color: " + gridColor;
        }
        refreshCells();
    }

    private void setTrail() {
        if (menuItemTrailNone.isSelected()) {
            for (int i = 1; i < maxDeathCount; i++) {
                deathColor[i] = deadColor;
            }
        } else if (menuItemTrailDeath.isSelected()) {
            if (menuItemGrayscale.isSelected()) {
                for (int i = 1; i < maxDeathCount; i++) {
                    if (menuItemThemeLight.isSelected())
                        deathColor[i] = "#808080";
                    else
                        deathColor[i] = "#404040";
                }
            } else {
                if (menuItemThemeDark.isSelected())
                    for (int i = 1; i < maxDeathCount; i++) {
                        deathColor[i] = "#304050";
                    }
                else
                    for (int i = 1; i < maxDeathCount; i++) {
                        deathColor[i] = "#90a0f0";
                    }
            }
        } else if (menuItemTrailFrequency.isSelected()) {
            if (menuItemGrayscale.isSelected()) {
                if (menuItemThemeLight.isSelected()) {
                    deathColor[6] = "#505050";
                    deathColor[5] = "#707070";
                    deathColor[4] = "#909090";
                    deathColor[3] = "#b0b0b0";
                    deathColor[2] = "#d0d0d0";
                    deathColor[1] = "#e0e0e0";
                } else {
                    deathColor[1] = "#505050";
                    deathColor[2] = "#707070";
                    deathColor[3] = "#909090";
                    deathColor[4] = "#b0b0b0";
                    deathColor[5] = "#d0d0d0";
                    deathColor[6] = "#e0e0e0";
                }
            } else {
                if (menuItemThemeDark.isSelected()) {
                    deathColor[1] = "#300000";
                    deathColor[2] = "#500000";
                    deathColor[3] = "#700000";
                    deathColor[4] = "#900000";
                    deathColor[5] = "#b00000";
                    deathColor[6] = "#d00000";
                } else {
                    deathColor[1] = "#f0d0d0";
                    deathColor[2] = "#f0b0b0";
                    deathColor[3] = "#f09090";
                    deathColor[4] = "#f07070";
                    deathColor[5] = "#f05050";
                    deathColor[6] = "#f03030";
                }
            }
        }
        refreshCells();
    }

    private void toggleGrid() {
        if (menuItemGrid.isSelected()) {
            grid = "-fx-border-color: " + gridColor;
        } else {
            grid = "";
        }
        refreshCells();
    }

    private void toggleGreyscale() {
        if (menuItemGrayscale.isSelected()) {
            if (menuItemThemeDark.isSelected()) {
                liveColor = "#606060";
            } else if (menuItemThemeLight.isSelected()) {
                liveColor = "black";
            } else {
                liveColor = "grey";
            }
        } else {
            setTheme();
        }
        if (menuItemFilterGrowth.isSelected())
            setGrowth();
        if (menuItemTrailDeath.isSelected() || menuItemTrailFrequency.isSelected())
            setTrail();
        refreshCells();
    }

    private void toggleNumbers() {
        if (!menuItemNumbers.isSelected()) {
            for (int i = 0; i < DIM; i++)
                for (int j = 0; j < DIM; j++) {
                    cell[i][j].vCent.getChildren().clear();
                }
        } else {
            calcNumbers();
            refreshCells();
        }
    }

    private void calcNumbers() {
        for (int i = 0; i < DIM; i++)
            for (int j = 0; j < DIM; j++) {
                cell[i][j].setLiveCount(countLive(i, j));
                if (menuItemNumbers.isSelected())
                    cell[i][j].setText();
            }
    }

    private void setGrowth() {
        if (menuItemFilterGrowth.isSelected()) {
            if (menuItemThemeLight.isSelected()) {
                if (menuItemGrayscale.isSelected()) {
                    growthColor[0] = "#303030";
                    growthColor[2] = "#606060";
                } else {
                    growthColor[0] = "#507020";
                    growthColor[2] = "#407060";
                }
            } else {
                if (menuItemGrayscale.isSelected()) {
                    growthColor[0] = "#404040";
                    growthColor[2] = "#202020";
                } else {
                    growthColor[0] = "#305000";
                    growthColor[2] = "#003020";
                }
            }
            calcGrowth();
        }
        refreshCells();
    }

    private void calcGrowth() {
        for (int i = 0; i < DIM; i++)
            for (int j = 0; j < DIM; j++) {
                int count = cell[i][j].liveCount;
                if (!cell[i][j].status) {
                    if (rbLife.isSelected() && count == 3)
                        cell[i][j].growthValue = 2;
                    else if (rbHighLife.isSelected() && (count == 3 || count == 6))
                        cell[i][j].growthValue = 2;
                    else
                        cell[i][j].growthValue = 1;
                } else if (cell[i][j].status && count != 2 && count != 3) {
                    cell[i][j].growthValue = 0;
                } else {
                    cell[i][j].growthValue = 1;
                }
            }
        refreshCells();
    }

    private void duplicate(int row, int col, boolean state) {
        if (menuItemDuplicateHorizontal.isSelected())
            duplicateHorizontal(row, col, state);
        else if (menuItemDuplicateVertical.isSelected())
            duplicateVertical(row, col, state);
        else if (menuItemDuplicateBoth.isSelected())
            duplicateBoth(row, col, state);
        else if (menuItemDuplicateDiagonal.isSelected())
            duplicateDiagonal(row, col, state);
    }

    private void duplicateHorizontal(int row, int col, boolean state) {
        cell[row][((DIM / 2) + col + DIM) % DIM].setCell(state);
        calc3x3Area(row, ((DIM / 2) + col + DIM) % DIM);
    }

    private void duplicateVertical(int row, int col, boolean state) {
        cell[((DIM / 2) + row + DIM) % DIM][col].setCell(state);
        calc3x3Area(((DIM / 2) + row + DIM) % DIM, col);
    }

    private void duplicateDiagonal(int row, int col, boolean state) {
        cell[((DIM / 2) + row + DIM) % DIM][((DIM / 2) + col + DIM) % DIM].setCell(state);
        calc3x3Area(((DIM / 2) + row + DIM) % DIM, ((DIM / 2) + col + DIM) % DIM);
    }

    private void duplicateBoth(int row, int col, boolean state) {
        cell[row][((DIM / 2) + col + DIM) % DIM].setCell(state);
        calc3x3Area(row, ((DIM / 2) + col + DIM) % DIM);
        cell[((DIM / 2) + row + DIM) % DIM][col].setCell(state);
        calc3x3Area(((DIM / 2) + row + DIM) % DIM, col);
        cell[((DIM / 2) + row + DIM) % DIM][((DIM / 2) + col + DIM) % DIM].setCell(state);
        calc3x3Area(((DIM / 2) + row + DIM) % DIM, ((DIM / 2) + col + DIM) % DIM);
    }


    private void mirror(int row, int col, boolean state) {
        if (menuItemMirrorHorizontal.isSelected())
            mirrorHorizontal(row, col, state);
        else if (menuItemMirrorVertical.isSelected())
            mirrorVertical(row, col, state);
        else if (menuItemMirrorBoth.isSelected())
            mirrorBoth(row, col, state);
        else if (menuItemMirrorDiagonal.isSelected())
            mirrorDiagonal(row, col, state);
    }

    private void mirrorHorizontal(int row, int col, boolean state) {
        cell[row][((col * -1) + DIM) - 1].setCell(state);
        calc3x3Area(row, ((col * -1) + DIM) - 1);
    }

    private void mirrorVertical(int row, int col, boolean state) {
        cell[((row * -1) + DIM) - 1][col].setCell(state);
        calc3x3Area(((row * -1) + DIM) - 1, col);
    }

    private void mirrorDiagonal(int row, int col, boolean state) {
        cell[((row * -1) + DIM) - 1][((col * -1) + DIM) - 1].setCell(state);
        calc3x3Area(((row * -1) + DIM) - 1, ((col * -1) + DIM) - 1);
    }

    private void mirrorBoth(int row, int col, boolean state) {
        cell[row][((col * -1) + DIM) - 1].setCell(state);
        calc3x3Area(row, ((col * -1) + DIM) - 1);
        cell[((row * -1) + DIM) - 1][col].setCell(state);
        calc3x3Area(((row * -1) + DIM) - 1, col);
        cell[((row * -1) + DIM) - 1][((col * -1) + DIM) - 1].setCell(state);
        calc3x3Area(((row * -1) + DIM) - 1, ((col * -1) + DIM) - 1);
    }

    private void play() {
        if (playing) {
            animation.pause();
            btPlay.setText("Play");
            btStep.setDisable(false);
            menuItemPlay.setDisable(false);
            menuItemStop.setDisable(true);
            menuItemStep.setDisable(false);
        } else {
            animation.play();
            btPlay.setText("Stop");
            btStep.setDisable(true);
            menuItemPlay.setDisable(true);
            menuItemStop.setDisable(false);
            menuItemStep.setDisable(true);
        }
        playing = !playing;
    }

    private void stopLife() {
        animation.pause();
        btPlay.setText("Play");
        playing = false;
        btStep.setDisable(false);
        menuItemPlay.setDisable(false);
        menuItemStop.setDisable(true);
        menuItemStep.setDisable(false);
    }

    private void clear() {
        stopLife();
        for (int i = 0; i < DIM; i++)
            for (int j = 0; j < DIM; j++) {
                cell[i][j].setCell(false);
                cell[i][j].deathCount = 0;
                cell[i][j].setLiveCount(0);
            }
        calcGrowth();
        refreshCells();
    }

    private void calc() {
        if (rbLife.isSelected()) {
            calcLife();
        } else if (rbHighLife.isSelected()) {
            calcHighLife();
        }
        calcNumbers();
        if (menuItemFilterGrowth.isSelected())
            calcGrowth();
    }

    private void calcLife() {
        boolean[][] cellStatesNew = new boolean[DIM][DIM];
        for (int i = 0; i < DIM; i++)
            for (int j = 0; j < DIM; j++) {
                int liveCount = countLive(i, j);
                if (liveCount == 3) {
                    cellStatesNew[i][j] = true;
                } else if (liveCount == 2) {
                    cellStatesNew[i][j] = cell[i][j].getCell();
                } else {
                    if (cell[i][j].getCell() && cell[i][j].deathCount < (maxDeathCount - 1)) {
                        cell[i][j].deathCount++;
                    }
                    cellStatesNew[i][j] = false;
                }
            }
        for (int i = 0; i < DIM; i++)
            for (int j = 0; j < DIM; j++) {
                cell[i][j].setCell(cellStatesNew[i][j]);
            }
    }

    private void calcHighLife() {
        boolean[][] cellStatesNew = new boolean[DIM][DIM];
        for (int i = 0; i < DIM; i++)
            for (int j = 0; j < DIM; j++) {
                int liveCount = countLive(i, j);
                if (3 == liveCount || liveCount == 6) {
                    cellStatesNew[i][j] = true;
                } else if (liveCount == 2) {
                    cellStatesNew[i][j] = cell[i][j].getCell();
                } else {
                    if (cell[i][j].getCell() && cell[i][j].deathCount < (maxDeathCount - 1)) {
                        cell[i][j].deathCount++;
                    }
                    cellStatesNew[i][j] = false;
                }
            }
        for (int i = 0; i < DIM; i++)
            for (int j = 0; j < DIM; j++) {
                cell[i][j].setCell(cellStatesNew[i][j]);
            }
    }

    private int countLive(int row, int col) {
        int liveCount = 0;
        for (int i = -1; i <= 1; i++)
            for (int j = -1; j <= 1; j++)
                if (cell[(row + i + DIM) % DIM][(col + j + DIM) % DIM].getCell())
                    liveCount++;
        if (cell[row][col].getCell())
            liveCount--;
        return liveCount;
    }

    private void calc3x3Area(int row, int col) {
        for (int i = -1; i <= 1; i++)
            for (int j = -1; j <= 1; j++)
                cell[(row + i + DIM) % DIM][(col + j + DIM) % DIM].setLiveCount(countLive((row + i + DIM) % DIM, (col + j + DIM) % DIM));
    }

    // An inner class for a cell
    public class Cell extends Pane {
        // Token used for this cell
        private boolean status = false;
        private int deathCount = 0;
        private int liveCount = 0;
        private int growthValue = 1;
        private int[] pos;
        private Text txtCount = new Text(String.valueOf(liveCount));
        private VBox vCent = new VBox();

        Cell() {
            setStyle("-fx-background-color: " + deadColor + "; " + grid);
            txtCount.setFont(Font.font(CELLDIM * 0.666));
            vCent.setPadding(new Insets(CELLDIM / 18.0, 0, 0, CELLDIM / 3.0));
            this.getChildren().addAll(vCent);
            this.setPrefSize(CELLDIM, CELLDIM);
            this.setOnMousePressed(e -> handleMouseClick());
            this.setOnMouseDragEntered(e -> handleMouseDrag());
        }

        /**
         * Return status
         */
        private boolean getCell() {
            return status;
        }

        /**
         * Set Cell status
         */
        private void setCell(boolean status) {
            this.status = status;
            if (menuItemFilterGrowth.isSelected() && growthValue != 1)
                setStyle("-fx-background-color: " + growthColor[growthValue] + "; " + grid);
            else {
                if (status) {
                    setStyle("-fx-background-color: " + liveColor + "; " + grid);
                } else {
                    if (deathCount >= 0) {
                        setStyle("-fx-background-color: " + deathColor[deathCount] + "; " + grid);
                    }
                }
            }
            if (menuItemNumbers.isSelected())
                setText();
        }

        private void setLiveCount(int count) {
            liveCount = count;
            if (menuItemNumbers.isSelected())
                txtCount.setText(String.valueOf(count));
        }

        private void setText() {
            this.vCent.getChildren().clear();
            this.vCent.getChildren().addAll(this.txtCount);
            if (menuItemThemeDark.isSelected() && !status)
                if (deathCount >= 4 && menuItemTrailFrequency.isSelected())
                    txtCount.setFill(Color.BLACK);
                else
                    txtCount.setFill(Color.WHITE);
            else
                if (menuItemGrayscale.isSelected() && status)
                    txtCount.setFill(Color.WHITE);
                else
                    txtCount.setFill(Color.BLACK);
        }

        /**
         * Toggle Cells status
         */
        private void toggleCell() {
            if (menuItemFilterGrowth.isSelected() && growthValue != 1)
                setStyle("-fx-background-color: " + growthColor[growthValue] + "; " + grid);
            else {
                if (status) {
                    if (deathCount >= 0) {
                        setStyle("-fx-background-color: " + deathColor[deathCount] + "; " + grid);
                    }
                } else {
                    setStyle("-fx-background-color: " + liveColor + "; " + grid);
                }
            }
            status = !status;
            if (menuItemNumbers.isSelected()) {
                setText();
            }
        }

        /**
         * Handle a mouse click event
         */
        private void handleMouseClick() {
            toggleCell();
            calc3x3Area(pos[0], pos[1]);
            if (menuItemFilterGrowth.isSelected())
                calcGrowth();
            mirror(pos[0], pos[1], status);
            duplicate(pos[0], pos[1], status);
            lastStatus = getCell();
        }

        /**
         * Handle a mouse click event
         */
        private void handleMouseDrag() {
            setCell(lastStatus);
            calc3x3Area(pos[0], pos[1]);
            if (menuItemFilterGrowth.isSelected())
                calcGrowth();
            mirror(pos[0], pos[1], status);
            duplicate(pos[0], pos[1], status);
        }
    }
}
