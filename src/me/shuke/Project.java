package me.shuke;

import java.util.HashMap;
import java.util.Map;

public class Project {
    private final String ID;
    private final String name;

    private final Map<String, Integer> roleExpense;

    public Project(String id, String name)
    {
        ID = id;
        this.name = name;

        roleExpense = new HashMap<>();
    }

    public void addRoleDay(String roleName)
    {
        Integer expense = roleExpense.get(roleName);
        if(expense == null)
        {
            roleExpense.put(roleName, 1);
        }
        else
        {
            roleExpense.replace(roleName, ++expense);
        }
    }

    public String getID()
    {
        return ID;
    }

    public String getName()
    {
        return name;
    }

    public Map<String, Integer> getRoleExpense()
    {
        return roleExpense;
    }
}
