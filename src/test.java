import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class test {
    public static void main(String[] args) throws IOException {
        // 指定文件路径和待查找字符串
        String filePath = "resources/local_answer/Q3.txt";
        String searchString = "==";

        // 读取文件内容
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;
        int count = 0;
        while ((line = reader.readLine()) != null) {
            // 在每一行中查找字符串
            int index = line.indexOf(searchString);
            while (index != -1) {
                count++;
                index = line.indexOf(searchString, index + searchString.length());
            }
        }

        // 输出结果
        System.out.println("The string \"" + searchString + "\" appears " + count + " times in the file " + filePath);
    }
}
