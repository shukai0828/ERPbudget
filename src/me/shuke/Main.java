package me.shuke;

import java.io.*;
import java.util.*;

public class Main {

    private static Map<String, Project> projectMap;
    private static String[] roleList;

    public static void main(String[] args) {

        projectMap = new HashMap<>();
        roleList = new String[0];
        // 构建项目信息列表
        setupProject();
        // 构建详细数据
        buildRoleExpense();
        // 构建角色顺序
        loadRoleList("D:/ProjectGithub/ERPbudget_data/m5_roles_utf8.csv");
        // 打印统计结果，csv格式
        printResult("D:/ProjectGithub/ERPbudget_data/result.csv");
    }

    private static void setupProject()
    {
        try
        {
            BufferedReader in = new BufferedReader(new FileReader("D:/ProjectGithub/ERPbudget_data/m5_projects_utf8.csv"));
            String contentLine;
            // 第一行为配置行
            in.readLine();
            // 后续行为内容行
            while ((contentLine = in.readLine()) != null) {
                String[] contentPart = contentLine.split(",");
                Project project = new Project(contentPart[0], contentPart[4]);
                projectMap.put(contentPart[0], project);
            }
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
        }
    }

    private static void buildRoleExpense()
    {
        try
        {
            BufferedReader in = new BufferedReader(new FileReader("D:/ProjectGithub/ERPbudget_data/m5_utf8.csv"));
            String contentLine;
            // 第一行为配置行
            in.readLine();
            // 后续行为内容行
            while ((contentLine = in.readLine()) != null) {
                String[] contentPart = contentLine.split(",");
                String roleName = contentPart[3];
                for (int i = 0; i < contentPart.length; i++)
                {
                    if(i < 4) continue; // 前4个为基础信息

                    String projectKey = contentPart[i];

                    Project project = projectMap.get(projectKey);
                    if(project == null)
                    {
                        if(projectKey.length() != 0) {
                            System.out.println(contentPart[i] + " not found!");
                        }

                        continue;
                    }

                    project.addRoleDay(roleName);
                    projectMap.replace(projectKey, project);
                }
            }
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
        }
    }

    public static void loadRoleList(String roleConfigFile)
    {
        try
        {
            BufferedReader in = new BufferedReader(new FileReader(roleConfigFile));
            String roleConfigLine;
            roleConfigLine = in.readLine();
            roleList = roleConfigLine.split(",");
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
        }
    }

    public static void printResult(String resultFile)
    {
        try
        {
            File file = new File(resultFile);
            // 创建文件
            file.createNewFile();
            // creates a FileWriter Object
            FileWriter writer = new FileWriter(file);
            // 按项目写入各角色工作量投入
            for (Map.Entry<String, Project> entry : projectMap.entrySet())
            {
                Project project = entry.getValue();
                writer.write(project.getID()+",");
                writer.write(project.getName()+",");

                Map<String, Integer> roleExpense = project.getRoleExpense();
                for(String roleName: roleList)
                {
                    if(roleExpense.get(roleName) == null)
                    {
                        writer.write(",");
                    }
                    else {
                        writer.write(roleExpense.get(roleName).toString() + ",");
                    }
                }
                writer.write("\n");
                writer.flush();
            }
            writer.close();
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
        }
    }
}
