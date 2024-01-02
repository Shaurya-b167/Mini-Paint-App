package project;

import java.util.List;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.ImageCursor;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * Controller class for the graphical user interface.
 */
public class Controller {

	ImageCursor drawCursor;
	ParallelTransition shapeTransitions;
	TranslateTransition currentTransition;
	FadeTransition animationFader;

	@FXML private Rectangle circleMenuBorder;
	@FXML private Rectangle lineMenuBorder;
	@FXML private Rectangle rectangleMenuBorder;
	@FXML private Rectangle textMenuBorder;
	@FXML private Rectangle selectMenuBorder;
	@FXML private Rectangle animateMenuBorder;

	private List<Rectangle> menuBorders;

	@FXML private Line menuLine;
	@FXML private Rectangle menuRectangle;
	@FXML private Circle menuCircle;
	@FXML private Text menuText;
	@FXML private CheckBox filledChk;
	@FXML private Slider redSlider;
	@FXML private Slider greenSlider;
	@FXML private Slider blueSlider;

	@FXML private Pane pane;

	private Color selectedColor;
	private Node selectedNode;
	private String mode;
	private boolean drawing = false;
	private double initialX, initialY;
	private double dX, dY;

	@FXML
	void initialize() {
		menuBorders = List.of(circleMenuBorder, lineMenuBorder,
				rectangleMenuBorder, textMenuBorder,
				selectMenuBorder, animateMenuBorder);

		filledChk.setFocusTraversable(false);

		Slider[] sliders = {redSlider, greenSlider, blueSlider};
		for (Slider slider : sliders) {
			slider.valueProperty().addListener((obs, oldV, newV) -> colorChange());
		}

		redSlider.adjustValue(123);
		greenSlider.adjustValue(198);
		blueSlider.adjustValue(248);

		Image drawImg = new Image(getClass().getResourceAsStream("draw-cursor.png"));
		drawCursor = new ImageCursor(drawImg, 70, 70);

		shapeTransitions = new ParallelTransition();

		pane.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> createShape(e));
		pane.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> animateShape(e));
	}

	void colorChange() {
		double redVal = redSlider.getValue(), greenVal = greenSlider.getValue(),
				blueVal = blueSlider.getValue();
		selectedColor = Color.rgb((int) redVal, (int) greenVal, (int) blueVal);
		menuCircle.setFill(selectedColor);
		menuRectangle.setFill(selectedColor);
		menuLine.setStroke(selectedColor);
		menuText.setFill(selectedColor);
	}

	@FXML
	void circleMode() {
		mode = "circle";
		pane.setCursor(drawCursor);
		select(circleMenuBorder);
	}

	@FXML
	void lineMode() {
		mode = "line";
		pane.setCursor(drawCursor);
		select(lineMenuBorder);
	}

	@FXML
	void rectangleMode() {
		mode = "rectangle";
		pane.setCursor(drawCursor);
		select(rectangleMenuBorder);
	}

	@FXML
	void textMode() {
		mode = "text";
		pane.setCursor(drawCursor);
		select(textMenuBorder);
	}

	@FXML
	void selectMode() {
		mode = "select";
		pane.setCursor(null);
		select(selectMenuBorder);
	}

	@FXML
	void animateMode() {
		mode = "animate";
		if (selectedNode != null && !menuBorders.contains(selectedNode)) {
			animationFader = new FadeTransition(Duration.millis(500), selectedNode);
			animationFader.setFromValue(1.0);
			animationFader.setToValue(0.5);
			animationFader.setAutoReverse(true);
			animationFader.setCycleCount(Animation.INDEFINITE);
			animationFader.play();

			currentTransition = new TranslateTransition(Duration.millis(1000), selectedNode);
		}
	}

	@FXML
	void globalKeyEvents(KeyEvent e) {
		if (e.getCode() == KeyCode.ESCAPE) {
			deselect();
			selectMode();
		}

		if (selectedNode instanceof Text) {
			Text textNode = (Text) selectedNode;

			if (e.getCode() == KeyCode.BACK_SPACE || e.getCode() == KeyCode.DELETE) {
				String currentText = textNode.getText();
				if (!currentText.isEmpty()) {
					textNode.setText(currentText.substring(0, currentText.length() - 1));
				}
			} else {
				textNode.setText(textNode.getText() + e.getText());
			}
		}

		if (e.getCode() == KeyCode.DELETE) {
			if (selectedNode instanceof Shape) {
				pane.getChildren().remove(selectedNode);
				selectedNode = null;
			}
		} else if (e.getCode() == KeyCode.BACK_SPACE && !(selectedNode instanceof Text)) {
			pane.getChildren().remove(selectedNode);
			selectedNode = null;
		} else if (e.getCode() == KeyCode.SPACE) {
			shapeTransitions.stop();
			for (Animation an : shapeTransitions.getChildren()) {
				TranslateTransition tt = (TranslateTransition) an;
				tt.setFromX(0);
				tt.setFromY(0);
			}
			shapeTransitions.playFromStart();
		}
	}

	void createShape(MouseEvent e) {
		if (selectedNode == null || (mode.equals("select") || mode.equals("animate"))) {
			return;
		}

		double x = e.getX();
		double y = e.getY();

		Shape newShape = null;

		switch (mode) {
			case "line" -> {
				Line line = new Line(x, y, x, y);
				line.setStroke(selectedColor);
				line.setStrokeWidth(6);
				pane.getChildren().add(line);
				newShape = line;
			}
			case "rectangle" -> {
				Rectangle rectangle = new Rectangle(x, y, 0, 0);
				if (filledChk.isSelected()) {
					rectangle.setFill(selectedColor);
				} else {
					rectangle.setFill(Color.TRANSPARENT);
					rectangle.setStroke(selectedColor);
				}
				pane.getChildren().add(rectangle);
				newShape = rectangle;
			}
			case "circle" -> {
				Ellipse ellipse = new Ellipse();
				ellipse.setCenterX(x);
				ellipse.setCenterY(y);
				if (filledChk.isSelected()) {
					ellipse.setFill(selectedColor);
				} else {
					ellipse.setFill(Color.WHITE);
					ellipse.setStroke(selectedColor);
				}
				pane.getChildren().add(ellipse);
				newShape = ellipse;
			}
			case "text" -> {
				Text text = new Text("Text");
				text.setX(x);
				text.setY(y);
				text.setFill(selectedColor);
				text.setStroke(selectedColor);
				pane.getChildren().add(text);
				newShape = text;
			}
		}

		newShape.addEventHandler(MouseEvent.MOUSE_PRESSED, e2 -> {
			if (!drawing) {
				Shape shape = (Shape) e2.getSource();
				select(shape);
				initialX = e2.getSceneX();
				initialY = e2.getSceneY();
			}
		});

		newShape.setOnMouseDragged(e2 -> moveShape(e2));
		drawing = true;
	}

	@FXML
	void resizeShape(MouseEvent e) {
		if (selectedNode == null) {
			return;
		}

		double x = e.getX();
		double y = e.getY();

		if (drawing) {
			Shape lastShape = (Shape) pane.getChildren().get(pane.getChildren().size() - 1);

			if (lastShape instanceof Rectangle) {
				Rectangle rectangle = (Rectangle) lastShape;

				// Check if Shift key is pressed
				if (e.isShiftDown()) {
					double sideLength = Math.min(Math.abs(x - rectangle.getX()), Math.abs(y - rectangle.getY()));
					if (x < rectangle.getX()) {
						rectangle.setX(rectangle.getX() - sideLength);
					}
					if (y < rectangle.getY()) {
						rectangle.setY(rectangle.getY() - sideLength);
					}
					rectangle.setWidth(sideLength);
					rectangle.setHeight(sideLength);
				} else {
					rectangle.setWidth(x - rectangle.getX());
					rectangle.setHeight(y - rectangle.getY());
				}
			} else if (lastShape instanceof Line) {
				Line line = (Line) lastShape;

				if (e.isShiftDown()) {
					// Check if horizontal or vertical
					if (Math.abs(x - line.getStartX()) > Math.abs(y - line.getStartY())) {
						// Lock to horizontal line
						line.setEndX(x);
						line.setEndY(line.getStartY());
					} else {
						// Lock to vertical line
						line.setEndX(line.getStartX());
						line.setEndY(y);
					}
				} else {
					line.setEndX(x);
					line.setEndY(y);
				}
			} else if (lastShape instanceof Ellipse) {
				Ellipse ellipse = (Ellipse) lastShape;

				// Check if Shift key is pressed
				if (e.isShiftDown()) {
					double radius = Math.min(Math.abs(x - ellipse.getCenterX()), Math.abs(y - ellipse.getCenterY()));
					ellipse.setRadiusX(radius);
					ellipse.setRadiusY(radius);
				} else {
					double radiusX = Math.abs(x - ellipse.getCenterX());
					double radiusY = Math.abs(y - ellipse.getCenterY());
					ellipse.setRadiusX(radiusX);
					ellipse.setRadiusY(radiusY);
				}
			} else if (lastShape instanceof Text) {
				double fontSize = Math.max(5, Math.abs(x - ((Text) lastShape).getX()) / 2);
				((Text) lastShape).setFont(new Font(fontSize));
			}
		}
	}

	@FXML
	void doneDrawing(MouseEvent e) {
		if (!(mode.equals("select") || mode.equals("animate"))) {
			drawing = false;
			selectMode();
			select(pane.getChildren().get(pane.getChildren().size() - 1));
		}
	}

	@FXML
	void moveShape(MouseEvent e) {
		if (selectedNode instanceof Shape && !drawing) {
			Object source = e.getSource();
			double x = e.getX();
			double y = e.getY();

			if (source instanceof Rectangle) {
				Rectangle rect = (Rectangle) source;
				rect.setX(x - dX);
				rect.setY(y - dY);
			} else if (source instanceof Line) {
				Line line = (Line) source;
				double startDistance = Math.sqrt(Math.pow(Math.abs(x - line.getStartX()), 2)
						+ Math.pow(Math.abs(y - line.getStartY()), 2)),
						endDistance = Math.sqrt(Math.pow(Math.abs(x - line.getEndX()), 2)
								+ Math.pow(Math.abs(y - line.getEndY()), 2));
				if (endDistance < startDistance) {
					line.setEndX(x);
					line.setEndY(y);
				} else {
					line.setStartX(x);
					line.setStartY(y);
				}
			} else if (source instanceof Ellipse) {
				Ellipse ellipse = (Ellipse) source;
				ellipse.setCenterX(x - dX);
				ellipse.setCenterY(y - dY);
			} else if (source instanceof Text) {
				Text text = (Text) source;
				text.setX(x - dX);
				text.setY(y - dY);
			}
		}
	}

	@FXML
	void animateShape(MouseEvent e) {
		if (mode.equals("animate") && !menuBorders.contains(selectedNode)) {
			if (selectedNode instanceof Rectangle) {
				Rectangle r = (Rectangle) selectedNode;
				currentTransition.setToX(e.getX() - r.getX());
				currentTransition.setToY(e.getY() - r.getY());
			} else if (selectedNode instanceof Line) {
				Line l = (Line) selectedNode;
				currentTransition.setToX(e.getX() - l.getStartX());
				currentTransition.setToY(e.getY() - l.getStartY());
			} else if (selectedNode instanceof Ellipse) {
				Ellipse ep = (Ellipse) selectedNode;
				currentTransition.setToX(e.getX() - ep.getCenterX());
				currentTransition.setToY(e.getY() - ep.getCenterY());
			} else if (selectedNode instanceof Text) {
				Text t = (Text) selectedNode;
				currentTransition.setToX(e.getX() - t.getX());
				currentTransition.setToY(e.getY() - t.getY());
			}
			shapeTransitions.getChildren().add(currentTransition);

			System.out.println("Added animation");

			animationFader.playFrom(Duration.ZERO);
			animationFader.stop();

			selectMode();
		}
	}

	void select(Node n) {
		deselect();
		selectedNode = n;
		addBorder(n);
	}

	void deselect() {
		removeBorder(selectedNode);
		selectedNode = null;
	}

	void addBorder(Node n) {
		try {
			n.getStyleClass().add("selected");
		} catch (Exception e) {
			// Ignore when no node is selected
		}
	}

	void removeBorder(Node n) {
		try {
			n.getStyleClass().remove("selected");
		} catch (NullPointerException e) {
			// Ignore if no node is selected
		}
	}
}
