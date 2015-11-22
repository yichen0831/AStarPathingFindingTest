package astarpathfinding;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class AstarPathFinding extends Application {

    public enum Action {
        NONE,
        SET_SOURCE,
        SET_TARGET,
        SET_WALL,
        SET_NORMAL,
        STEP,
        RUN,
        CLEAR
    }

    private final int EMPTY = 0;
    private final int SOURCE = 1;
    private final int TARGET = 2;
    private final int WALL = 3;
    private final int PATH = 4;
    private final int OPEN = 5;
    private final int CLOSED = 6;

    private final double WINDOW_WIDTH = 620;
    private final double WINDOW_HEIGHT = 680;
    private final double CANVAS_WIDTH = 600;
    private final double CANVAS_HEIGHT = 600;

    private final int WIDTH = 20;
    private final int HEIGHT = 20;

    private final double CELL_WIDTH = CANVAS_WIDTH / WIDTH;
    private final double CELL_HEIGHT = CANVAS_HEIGHT / HEIGHT;

    private int[][] cells; // 0: empty, 1: source, 2: target, 3: wall, 4: path, 5: open, 6: closed
    private Node[][] nodes;

    private Action action = Action.NONE;

    private List<Node> openNodes;
    private List<Node> closedNodes;

    private Node sourceNode;
    private Node targetNode;

    private Label statusLabel;
    private Canvas canvas;

    private boolean showGCost;
    private boolean showHCost;
    private boolean showFCost;

    public AstarPathFinding() {
        cells = new int[HEIGHT][WIDTH];
        nodes = new Node[HEIGHT][WIDTH];

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                cells[y][x] = 0;
            }
        }

        openNodes = new ArrayList<>();
        closedNodes = new ArrayList<>();
    }

    @Override
    public void start(Stage primaryStage) {

        Button setSourceButton = new Button("Set source");
        setSourceButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                action = Action.SET_SOURCE;
                statusLabel.setText("Click to set source");
            }
        });

        Button setTargetButton = new Button("Set target");
        setTargetButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                action = Action.SET_TARGET;
                statusLabel.setText("Click to set target");
            }
        });

        Button setWallButton = new Button("Set wall");
        setWallButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                action = Action.SET_WALL;
                statusLabel.setText("Click/drag to set wall");
            }
        });

        Button setNormalButton = new Button("Set empty");
        setNormalButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                action = Action.SET_NORMAL;
                statusLabel.setText("Click/drag to set normal");
            }
        });

        Button clearResultButton = new Button("Clear result");
        clearResultButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                clearBoard();
                update();

                action = Action.CLEAR;
            }
        });

        Button runButton = new Button("Run");
        runButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (sourceNode == null) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Source node is not set", ButtonType.OK);
                    alert.setHeaderText("");
                    alert.showAndWait();
                    return;
                } else if (targetNode == null) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Target node is not set", ButtonType.OK);
                    alert.setHeaderText("");
                    alert.showAndWait();
                    return;
                }
                action = Action.RUN;
                clearBoard();
                if (!findPath()) {
                    Alert alert = new Alert(Alert.AlertType.WARNING, "Cannot find a path", ButtonType.OK);
                    alert.setHeaderText("");
                    alert.showAndWait();
                }
                update();
            }
        });

        HBox hBox1 = new HBox(6);
        hBox1.getChildren().add(setSourceButton);
        hBox1.getChildren().add(setTargetButton);
        hBox1.getChildren().add(setWallButton);
        hBox1.getChildren().add(setNormalButton);
        hBox1.getChildren().add(clearResultButton);
        hBox1.getChildren().add(runButton);

        CheckBox showGCostCheckBox = new CheckBox("Show G Cost");
        showGCostCheckBox.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                showGCost = showGCostCheckBox.isSelected();
                update();
            }

        });

        CheckBox showHCostCheckBox = new CheckBox("Show H Cost");
        showHCostCheckBox.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                showHCost = showHCostCheckBox.isSelected();
                update();
            }

        });

        CheckBox showFCostCheckBox = new CheckBox("Show F Cost");
        showFCostCheckBox.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                showFCost = showFCostCheckBox.isSelected();
                update();
            }
        });

        HBox hBox2 = new HBox(6);
        hBox2.getChildren().add(showGCostCheckBox);
        hBox2.getChildren().add(showHCostCheckBox);
        hBox2.getChildren().add(showFCostCheckBox);

        canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                int x = (int) (event.getX() / CELL_WIDTH);
                int y = (int) (event.getY() / CELL_HEIGHT);
                if (x < 0 || x >= WIDTH || y < 0 || y >= HEIGHT) {
                    return;
                }

                switch (action) {
                    case SET_SOURCE:
                        if (sourceNode != null) {
                            cells[sourceNode.y][sourceNode.x] = EMPTY;
                        }
                        sourceNode = new Node(x, y);
                        cells[y][x] = SOURCE;
                        statusLabel.setText("Source is set");
                        action = Action.NONE;
                        break;
                    case SET_TARGET:
                        if (targetNode != null) {
                            cells[targetNode.y][targetNode.x] = EMPTY;
                        }
                        targetNode = new Node(x, y);
                        cells[y][x] = TARGET;
                        statusLabel.setText("Target is set");
                        action = Action.NONE;
                        break;
                    case SET_WALL:
                        if (sourceNode != null && sourceNode.isXY(x, y)) {
                            sourceNode = null;
                        } else if (targetNode != null && targetNode.isXY(x, y)) {
                            targetNode = null;
                        }
                        cells[y][x] = WALL;
                        statusLabel.setText("Wall is set");
                        action = Action.NONE;
                        break;
                    case SET_NORMAL:
                        if (sourceNode != null && sourceNode.isXY(x, y)) {
                            sourceNode = null;
                        } else if (targetNode != null && targetNode.isXY(x, y)) {
                            targetNode = null;
                        }
                        cells[y][x] = EMPTY;
                        statusLabel.setText("Empty is set");
                        action = Action.NONE;
                        break;
                    case NONE:
                    default:
                        break;
                }
                update();
            }
        });

        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                int x = (int) (event.getX() / CELL_WIDTH);
                int y = (int) (event.getY() / CELL_HEIGHT);
                if (x < 0 || x >= WIDTH || y < 0 || y >= HEIGHT) {
                    return;
                }

                switch (action) {
                    case SET_WALL:
                        if (sourceNode != null && sourceNode.isXY(x, y)) {
                            sourceNode = null;
                        } else if (targetNode != null && targetNode.isXY(x, y)) {
                            targetNode = null;
                        }
                        cells[y][x] = WALL;
                        break;
                    case SET_NORMAL:
                        if (sourceNode != null && sourceNode.isXY(x, y)) {
                            sourceNode = null;
                        } else if (targetNode != null && targetNode.isXY(x, y)) {
                            targetNode = null;
                        }
                        cells[y][x] = EMPTY;
                        break;
                    default:
                        break;
                }
                update();
            }
        });

        statusLabel = new Label("");

        GridPane root = new GridPane();
        root.setPadding(new Insets(10));
        root.setHgap(6);
        root.setVgap(6);
        root.add(hBox1, 0, 0);
        root.add(hBox2, 0, 1);
        root.add(statusLabel, 2, 0);
        root.add(canvas, 0, 2, 2, 1);

        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);

        primaryStage.setTitle("A* Path Finding");
        primaryStage.setScene(scene);
        primaryStage.show();

        update();
    }

    private void update() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        drawBoard(gc);
    }

    private void drawBoard(GraphicsContext gc) {
        gc.setFill(Color.WHITESMOKE);
        gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

        gc.setStroke(Color.BLACK);
        for (int i = 0; i <= WIDTH; i++) {
            gc.strokeLine(i * CELL_WIDTH, 0, i * CELL_WIDTH, CELL_HEIGHT * HEIGHT);
        }

        for (int i = 0; i <= HEIGHT; i++) {
            gc.strokeLine(0, i * CELL_HEIGHT, CELL_WIDTH * WIDTH, i * CELL_HEIGHT);
        }

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                switch (cells[y][x]) {
                    case SOURCE:
                        gc.setFill(Color.BLUE);
                        break;
                    case TARGET:
                        gc.setFill(Color.RED);
                        break;
                    case WALL:
                        gc.setFill(Color.DIMGRAY);
                        break;
                    case PATH:
                        gc.setFill(Color.KHAKI);
                        break;
                    case OPEN:
                        gc.setFill(Color.ALICEBLUE);
                        break;
                    case CLOSED:
                        gc.setFill(Color.DARKKHAKI);
                        break;
                    case EMPTY:
                    default:
                        gc.setFill(Color.TRANSPARENT);
                        break;
                }
                gc.fillRect(x * CELL_WIDTH + 1, y * CELL_HEIGHT + 1, CELL_WIDTH - 2, CELL_HEIGHT - 2);

                Node node = nodes[y][x];
                if (node != null) {
                    if (showGCost) {
                        gc.strokeText(String.format("%d", node.gCost), (node.x + 0.0) * CELL_WIDTH + 2, (node.y + 0.4) * CELL_HEIGHT);
                    }
                    if (showHCost) {
                        gc.strokeText(String.format("%d", node.hCost), (node.x + 0.2) * CELL_WIDTH + 2, (node.y + 0.7) * CELL_HEIGHT);
                    }
                    if (showFCost) {
                        gc.strokeText(String.format("%d", node.fCost), (node.x + 0.0) * CELL_WIDTH + 2, (node.y + 1) * CELL_HEIGHT);
                    }
                }
            }
        }
    }

    private void clearBoard() {
        for (int i = 0; i < HEIGHT; i++) {
            for (int j = 0; j < WIDTH; j++) {
                if (cells[i][j] == SOURCE || cells[i][j] == TARGET || cells[i][j] == WALL) {
                    continue;
                }
                cells[i][j] = 0;
            }
        }
        update();
    }

    private boolean findPath() {
        for (int _y = 0; _y < HEIGHT; _y++) {
            for (int _x = 0; _x < WIDTH; _x++) {
                nodes[_y][_x] = null;
            }
        }

        nodes[sourceNode.y][sourceNode.x] = sourceNode;

        openNodes.clear();
        closedNodes.clear();

        openNodes.add(sourceNode);

        while (!openNodes.isEmpty()) {
            openNodes.sort((node1, node2) -> {
                return node1.fCost - node2.fCost;
            });

            Node node = openNodes.get(0);
            node.closed = true;
            closedNodes.add(node);
            openNodes.remove(node);

            if (checkSurroundingOpenNodes(node)) {
                // reach target
                Node pathNode = targetNode.prev;
                while (pathNode != sourceNode) {
                    cells[pathNode.y][pathNode.x] = PATH;
                    pathNode = pathNode.prev;
                }
                return true;
            }
        }
        return false;
    }

    private boolean checkSurroundingOpenNodes(Node node) {

        int checkX;
        int checkY;
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                checkX = node.x + j;
                checkY = node.y + i;

                if (checkX < 0 || checkX >= WIDTH || checkY < 0 || checkY >= HEIGHT || node.isXY(checkX, checkY)) {
                    continue;
                }

                if (cells[checkY][checkX] == WALL) {
                    continue;
                }

                if (cells[checkY][checkX] == TARGET) {
                    nodes[checkY][checkX] = targetNode;
                    targetNode.prev = node;
                    node.next = targetNode;
                    return true;
                }

                if (nodes[checkY][checkX] == null) {
                    // add to open list
                    Node sNode = new Node(checkX, checkY);
                    nodes[checkY][checkX] = sNode;
                    sNode.prev = node;
                    node.next = sNode;

                    sNode.gCost = node.gCost + (i == 0 || j == 0 ? 10 : 14);
                    int xDist = Math.abs(checkX - targetNode.x);
                    int yDist = Math.abs(checkY - targetNode.y);
                    sNode.hCost = (Math.max(xDist, yDist) - Math.min(xDist, yDist)) * 10 + Math.min(xDist, yDist) * 14;
                    sNode.fCost = sNode.gCost + sNode.hCost;
                    openNodes.add(sNode);
                    cells[sNode.y][sNode.x] = OPEN;
                } else {
                    Node sNode = nodes[checkY][checkX];
                    if (sNode.closed) {
                        continue;
                    }
                    // update fCost
                    int gCost = node.gCost + (i == 0 || j == 0 ? 10 : 14);
                    if (sNode.gCost > gCost) {
                        sNode.gCost = gCost;
                        sNode.fCost = sNode.gCost + sNode.hCost;

                        sNode.prev = node;
                        node.next = sNode;
                    }
                }
            }
        }
        return false;
    }

    public static void main(String[] args) {
        launch(args);
    }

}
