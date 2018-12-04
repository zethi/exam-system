import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

public class Exam {
    private int currentSecond;

    private String title;
    private ArrayList<Question> questions = new ArrayList<>();
    private boolean isFinished;
    private QuesPane quesPane;
    private HBox hBox;
    private BorderPane rightBox;
    private Scene scene;
    private qtButton[] btList;
    private Button btFinish;

    private ResultPane resultPane;
    private int correctCount;

    private TimerTask timeDisplayTask;


    public void start(File file) {
        int totalSecond = 0;

        // 读取试题
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));

            String[] info = reader.readLine().split("#");
            title = info[0];
            totalSecond = 60 * Integer.parseInt(info[1]);

            String quesStr;
            while ((quesStr = reader.readLine()) != null)
                questions.add(new Question(quesStr));
            Collections.shuffle(questions);
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }


        // 倒计时
        Label lbTimer = new Label();
        lbTimer.setFont(Font.font("Arial", FontWeight.BOLD, 30));

        currentSecond = totalSecond;

        timeDisplayTask = new TimerTask() {
            @Override
            public void run() {
                currentSecond--;
                Platform.runLater(() -> {
                    if (currentSecond == 0)
                        finish();
                    lbTimer.setText(String.format("%02d:", currentSecond / 60) + String.format("%02d", currentSecond % 60));
                });
            }
        };
        Timer displayTimer = new Timer();
        displayTimer.schedule(timeDisplayTask, 0, 1000);



        // 显示考试基本信息
        Label lbTitle = new Label(title);
        lbTitle.setFont(Font.font("微软雅黑", FontWeight.BOLD, 30));

        // 创建题目选择按钮
        FlowPane buttonPane = new FlowPane();
        buttonPane.setMinWidth(5 * qtButton.size);
        buttonPane.setMaxWidth(5 * qtButton.size);

        btList = new qtButton[questions.size()];
        for (int i = 0; i < questions.size(); i++) {
            qtButton button = new qtButton(i);
            btList[i] = button;
            buttonPane.getChildren().add(button);
        }

        // 提交按钮
        btFinish = new Button("提交");
        btFinish.setMinSize(100, 60);

        // 提交前弹出确认窗口
        btFinish.setOnAction(event -> {
            Stage finStage = new Stage();

            Label lbFinish = new Label("确定要交卷吗？");
            lbFinish.setFont(Font.font(16));
            if (!isComplete())
                lbFinish.setText("还有题目未完成，确定要交卷吗？");

            Button btOk = new Button("确定");
            Button btCancel = new Button("取消");
            for (Button button: new Button[]{btOk, btCancel}) {
                button.setMinSize(60, 30);
            }
            btOk.setOnAction(event1 -> {
                finish();
                finStage.close();
            });
            btCancel.setOnAction(event1 -> {
                finStage.close();
            });

            HBox finBtBox = new HBox(10);
            finBtBox.setAlignment(Pos.CENTER);
            finBtBox.getChildren().addAll(btOk, btCancel);
            VBox finBox = new VBox(10);
            finBox.setPadding(new Insets(20,20,20,20));
            finBox.setAlignment(Pos.CENTER);
            finBox.getChildren().addAll(lbFinish, finBtBox);
            Scene finScene = new Scene(finBox);
            finStage.setScene(finScene);
            finStage.show();
        });

        quesPane = new QuesPane(questions.get(0));

        // 布局排版
        VBox leftBox = new VBox(20);
        leftBox.setAlignment(Pos.TOP_CENTER);
        leftBox.getChildren().addAll(lbTitle, buttonPane, lbTimer);

        rightBox = new BorderPane();
        rightBox.setCenter(quesPane);
        rightBox.setBottom(btFinish);

        hBox = new HBox();
        hBox.getChildren().addAll(leftBox, rightBox);

        scene = new Scene(hBox);
        scene.getStylesheets().add(getClass().getResource("skin.css").toExternalForm());   // 设定 css

        rightBox.minWidthProperty().bind(scene.widthProperty().subtract(leftBox.widthProperty()));

        Stage stage = new Stage();

        stage.setScene(scene);
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        stage.show();
    }


    // 结束考试
    private void finish() {
        isFinished = true;
        correctCount = 0;

        timeDisplayTask.cancel();

        for (Question question: questions) {
            if (question.isCorrect())
                correctCount++;
        }
        resultPane = new ResultPane();
        rightBox.setCenter(resultPane);

        //
        for (qtButton bt: btList) {
            bt.refresh();
        }

        btFinish.setText("查看成绩");
        btFinish.setOnAction(event -> rightBox.setCenter(resultPane));
    }


    private boolean isComplete() {
        for (Question question: questions) {
            if (question.isEmpty())
                return false;
        }
        return true;
    }


    // 成绩面板
    class ResultPane extends VBox {
        ResultPane() {
            setAlignment(Pos.CENTER);
            Label lbEnd = new Label("考试结束");
            Label lbCorrect = new Label("" + correctCount);
            getChildren().addAll(lbEnd, lbCorrect);
        }
    }


    // 试题面板
    class QuesPane extends VBox {
        QuesPane(Question question) {
            boolean[] key = question.getKey();
            boolean[] answer = question.getAnswer();

            setPadding(new Insets(20,20,20,20));
            setMinSize(400, 400);

            // 显示题目文本
            Label lbQuestion = new Label(question.getQues());
            lbQuestion.setFont(Font.font(20));
            getChildren().add(lbQuestion);

            // 创建选项
            ArrayList<String> selectionList = question.getSelection();

            if (isFinished) {
                for (int i = 0; i < selectionList.size(); i++) {
                    Label lb;
                    if (answer[i] && key[i]) {
                        lb  = new Label("✔ " + selectionList.get(i));
                        lb.setTextFill(Color.GREEN);
                    }
                    else if (answer[i] && !key[i]) {
                        lb  = new Label("✖ " + selectionList.get(i));
                        lb.setTextFill(Color.RED);
                    }
                    else if (!answer[i] && key[i]) {
                        lb  = new Label("✔ " + selectionList.get(i));
                        lb.setTextFill(Color.BLUE);
                    }
                    else
                        lb = new Label(selectionList.get(i));
                    lb.setFont(Font.font(20));
                    lb.minHeight(40);

                    getChildren().add(lb);
                }
            }
            else {
                if (question.isMultiple()) {
                    // 是多选题
                    for (int i = 0; i < selectionList.size(); i++) {
                        final int pos = i;
                        CheckBox cb = new CheckBox(selectionList.get(pos));
                        cb.setFont(Font.font(20));
                        cb.setMinHeight(40);
                        cb.setSelected(question.getAnswer()[pos]);    // 恢复选择状态
                        cb.selectedProperty().addListener(new ChangeListener<Boolean>() {
                            @Override
                            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                                question.getAnswer()[pos] = newValue;
                                btList[questions.indexOf(question)].refresh();
                            }
                        });
                        getChildren().add(cb);
                    }
                }
                else {
                    // 是单选题
                    ToggleGroup tg = new ToggleGroup();
                    for (int i = 0; i < selectionList.size(); i++) {
                        final int pos = i;
                        RadioButton rb = new RadioButton(selectionList.get(i));
                        rb.setToggleGroup(tg);
                        rb.setFont(Font.font(20));
                        rb.setMinHeight(40);
                        rb.setSelected(question.getAnswer()[i]);
                        rb.selectedProperty().addListener(new ChangeListener<Boolean>() {
                            @Override
                            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                                for (int j = 0; j < question.getAnswer().length; j++)
                                    question.getAnswer()[j] = false;    // 把其他选项设为 false
                                question.getAnswer()[pos] = newValue;
                                btList[questions.indexOf(question)].refresh();
                            }
                        });
                        getChildren().add(rb);
                    }
                }
            }
        }
    }


    // 题目选择按钮
    class qtButton extends Button {
        final static int size = 50;
        Question question;

        qtButton(int num) {
            setText(""+(num+1));
            setMinSize(size,size);
            question = questions.get(num);
            setOnAction(event -> {
                quesPane = new QuesPane(question);
                rightBox.setCenter(quesPane);
            });
            setFont(Font.font(16));
            setTextFill(Color.WHITE);
            setId("qtBt");
        }

        public void refresh() {
            if (isFinished && question.isCorrect())
                setId("qtBt-correct");
            else if (isFinished && !question.isCorrect())
                setId("qtBt-wrong");
            else if (!isFinished && !question.isEmpty())
                setId("qtBt-answered");
            else
                setId("qtBt");
        }
    }
}