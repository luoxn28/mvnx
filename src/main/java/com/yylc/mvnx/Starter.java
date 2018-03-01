package com.yylc.mvnx;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import com.yylc.mvnx.component.DependencyTreeParser;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * 启动器
 *
 * @author xiangnan
 * @date 2018/3/1 11:15
 */
@Component
public class Starter {

    private static final String currentPath = System.getProperty("user.dir");

    private static final String cmdMavenHome = "cmd /c cd %MAVEN_HOME%\\bin";
    private static final String cmdMvnTree = "cmd /c mvn dependency:tree";

    @Resource
    private DependencyTreeParser treeParser;

    void run(List<String> argList) {

        String path = checkArgs(argList);
        switch (path) {
            case "-v":
            case "-version": {
                System.out.println("mvnx 1.0-SNAPSHOT");
            }
            case "-h":
            case "-help": {
                help();
            }
            default: {
                run(path);
            }
        }
    }

    private void run(String path) {
        System.out.println("[" + path + "] mvnx is working...");

        // 判断是否配置了MAVEN_HOME
        try {
            checkMavenHome();
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<String> fileList = FileUtil.listFileNames(path);
        if (!fileList.contains("pom.xml")) {
            throw new RuntimeException("not found pom.xml");
        }

        try {
            Process p = Runtime.getRuntime().exec(cmdMvnTree);

            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            List<String> lineList = new ArrayList<>();

            Thread thread = new Thread(() -> {
                String line;
                try {
                    List<String> tmpList = new ArrayList<>();
                    while ((line = reader.readLine()) != null) {

                        if (line.contains("[ERROR]") || line.contains("FAILURE") || line.contains("BUILD FAILURE")) {
                            show(tmpList);
                            show(reader);
                            System.exit(-1);
                        }

                        if (line.startsWith("[INFO]")) {
                            lineList.add(line);
                        }
                        tmpList.add(line);
                    }

                    treeParser.parse(lineList);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            thread.start();

            Thread errorThread = new Thread(() -> {
                String line;
                try {
                    boolean error = false;
                    while ((line = errorReader.readLine()) != null) {
                        error = true;
                        System.out.println(line);
                    }

                    if (error) {
                        System.exit(-1);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            errorThread.start();

            p.waitFor();
            if (p.exitValue() != 0) {
                throw new RuntimeException(cmdMvnTree + " error");
            }

            thread.join();
            errorThread.join();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String checkArgs(List<String> argList) {
        if ((argList == null) || (argList.size() > 1)) {
            throw new RuntimeException("params error, please use [ mvnx -h ] for message");
        }

        return CollectionUtil.isEmpty(argList) ? currentPath : argList.get(0);
    }

    private void help() {
        System.out.println("mvnx");
    }

    private void checkMavenHome() throws Exception {
        Process p = Runtime.getRuntime().exec(cmdMavenHome);
        p.waitFor();
        if (p.exitValue() != 0) {
            throw new RuntimeException("not found MAVEN_HOME");
        }
    }

    private void show(BufferedReader reader) throws IOException {
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }

        System.out.println(builder.toString());
    }

    private void show(List<String> lineList) {
        lineList.forEach(line -> System.out.println(line));
    }

}
