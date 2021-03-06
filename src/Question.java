import java.util.ArrayList;
import java.util.Arrays;

public class Question {
    private String ques;
    private ArrayList<String> selection = new ArrayList<>();
    private boolean[] key;
    private boolean[] answer;

    Question(String str) {
        String[] list = str.split("#");
        ques = list[0]; // 问题文本
        key = new boolean[list.length-1];
        answer = new boolean[list.length-1];

        // 添加选项
        for (int i = 1; i < list.length; i++) {
            if (list[i].charAt(0) == '^') {
                key[i-1] = true;
                selection.add(list[i].substring(1));
            }
            else selection.add(list[i]);
        }
    }

    String getQues() {
        return ques;
    }

    ArrayList<String> getSelection() {
        return selection;
    }

    boolean[] getKey() {
        return key;
    }

    boolean[] getAnswer() {
        return answer;
    }

    boolean isMultiple() {
        int count = 0;
        for (boolean bl : key) {
            if (bl)
                count++;
        }
        if (count > 1)
            return true;
        else
            return false;
    }

    boolean isCorrect() {
        return Arrays.equals(key, answer);
    }

    boolean isEmpty() {
        return Arrays.equals(answer, new boolean[selection.size()]);
    }
}