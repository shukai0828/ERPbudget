package me.shuke;

import java.io.*;
import java.util.*;

public class Main {

    private static Map<String, Project> projectMap;
    private static String[] roleList;
    private static Integer totalPersonDays; // 总人日
    private static Integer totalPersons;

    public static void main(String[] args) {

        projectMap = new LinkedHashMap<>();
        roleList = new String[0];
        totalPersonDays = 0;
        totalPersons = 0;
        // 构建项目信息列表
        setupProject("D:/ProjectGithub/ERPbudget_data/m5_projects.csv", "GBK");
        // 构建详细数据
        buildRoleExpense("D:/ProjectGithub/ERPbudget_data/m5.csv", "GBK");
        // 构建角色顺序
        loadRoleList("D:/ProjectGithub/ERPbudget_data/m5_roles.csv", "GBK");
        // 打印统计结果，csv格式
        printResult("D:/ProjectGithub/ERPbudget_data/result.csv", "GBK");
    }

    private static void setupProject(String projectsFile, String charsetName)
    {
        try
        {
            FileInputStream fileInputStream = new FileInputStream(projectsFile);
            BufferedReader in = new BufferedReader(new InputStreamReader(fileInputStream, charsetName));
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

    private static void buildRoleExpense(String dataFile, String charsetName)
    {
        try
        {
            FileInputStream fileInputStream = new FileInputStream(dataFile);
            BufferedReader in = new BufferedReader(new InputStreamReader(fileInputStream, charsetName));
            String contentLine;
            // 第一行为配置行，计算实际
            in.readLine();
            // 后续行为内容行
            while ((contentLine = in.readLine()) != null) {
                String[] contentPart = contentLine.split(",");
                String roleName = contentPart[3];
                totalPersons ++;
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

                    totalPersonDays++;
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

    public static void loadRoleList(String roleConfigFile, String charsetName)
    {
        try
        {
            FileInputStream fileInputStream = new FileInputStream(roleConfigFile);
            BufferedReader in = new BufferedReader(new InputStreamReader(fileInputStream, charsetName));
            String roleConfigLine;
            roleConfigLine = in.readLine();
            roleList = roleConfigLine.split(",");
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
        }
    }

    public static void printResult(String resultFile, String charsetName)
    {
        try
        {
            // 计算平均天数
            Float averageDays = (float) (totalPersonDays / totalPersons);
            // 按格式写入文件
            FileOutputStream fileOutputStream = new FileOutputStream(resultFile);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fileOutputStream, charsetName));

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
                        Float averagePerson = roleExpense.get(roleName) / averageDays;
                        writer.write(String.format("%.1f", averagePerson) + ",");
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
