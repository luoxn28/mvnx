package com.yylc.mvnx.component;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xiangnan
 * @date 2018/3/1 12:10
 */
@Component
public class DependencyTreeParser {

    // Map<jarName, jarVersionList>
    private Map<String, List<String>> jarInfoMap = new HashMap<>();

    private boolean allJarVersionOK = true;

    public void parse(List<String> lineList) {
        if (CollectionUtil.isEmpty(lineList)) {
            throw new RuntimeException("no valid result");
        }

        allJarVersionOK = true;
        lineList.forEach(line -> {
            /*
             * line示例：  +- org.springframework.boot:spring-boot-starter:jar:1.5.6.RELEASE:compile
             * line示例：  \- org.springframework.boot:spring-boot-starter:jar:1.5.6.RELEASE:compile
             * line格式为：groupId : artifactId : jar : version : compile
             */
            if (line.contains("+- ")) {
                parseJarNameVersion(line.substring(line.indexOf("+- ") + 3).trim());
            } else if (line.contains("\\- ")) {
                parseJarNameVersion(line.substring(line.indexOf("\\- ") + 3).trim());
            }
        });

        jarInfoMap.forEach((key, value) -> {
            if (value.size() > 1) {
                allJarVersionOK = false;
                System.out.println("[ERROR]：" + key + " - " + value);
            }
        });

        if (allJarVersionOK) {
            System.out.println("[mvnx] all jar version is ok");
        } else {
            System.out.println();
            show(lineList);
        }
    }

    private void parseJarNameVersion(String line) {
        /*
         * line示例：  org.springframework.boot:spring-boot-starter:jar:1.5.6.RELEASE:compile
         * line格式为：groupId : artifactId : jar : version : compile
         */
        String[] jarInfo = StrUtil.split(line, ":");
        String jarName = jarInfo[0] + ":" + jarInfo[1];
        String jarVersion = jarInfo[3];

        if (!jarInfoMap.containsKey(jarName)) {
            jarInfoMap.put(jarName, CollectionUtil.newArrayList(jarVersion));
        } else {
            List<String> versionList = jarInfoMap.get(jarName);
            if (!versionList.contains(jarVersion)) {
                versionList.add(jarVersion);
            }
        }
    }

    private void show(List<String> lineList) {
        lineList.forEach(line -> System.out.println(line));
    }

}
