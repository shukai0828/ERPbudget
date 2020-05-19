package me.shuke.erpbudget;

import java.io.*;
import java.util.*;

/**
 * 根据石墨文档的工时记录格式和统计格式按项目、角色计算投入人数，支持一位小数，四舍五入。
 *
 * 石墨文档参考：为隐私考虑，这里不记录了。
 *
 * 统计算法：
 *     1. 统计时，不在项目列表中的记录去除，不参与人日数叠加
 *     2. 每个项目/每个角色的平均人头数计算公式为：当前项目和角色人日数 / 全部项目角色平均人日数
 *     3. “全部项目角色平均人日数” 的计算方式为：全部有效人日数 / 全部人数
 *         这个计算有一定的误差，周末加班、休假、不合法的项目ID不统计在"全部有效人日数"中，会导致该值偏低。
 *
 * 预处理过程：
 *     1. 从石墨上导出excel文件，并另存为csv文件
 *     2. 修剪数据，去除干扰
 *
 * 处理过程：
 *     1. 从项目列表文件中加载项目ID和项目名称
 *     2. 从明细文件中循环处理，累加项目/角色的人日数
 *     3. 从角色定义文件中获取角色列表和展示顺序
 *     4. 将统计结果按照格式要求输出，过程中进行实际人数计算
 *
 * @author shukai0828
 * @date  2020/5/19
 */

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

    /**
     * 根据项目集列表构建初始项目结构
     *
     * @param projectsFile 文件地址
     * @param charsetName 文件的编码格式
     */
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

    /**
     * 遍历指定的明细工时统计文件，统计分项目、角色人日数，以及总人数和总人日数
     *
     * @param dataFile 明细文件地址
     * @param charsetName 文件的编码格式
     */
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

    /**
     * 读取角色配置文件，其顺序就是展示顺序
     *
     * @param roleConfigFile 角色配置文件地址
     * @param charsetName 文件编码格式
     */
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

    /**
     * 输出符合基本要求的统计结果
     *
     * @param resultFile 输出文件目标地址
     * @param charsetName 输出时的字符集
     */
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
