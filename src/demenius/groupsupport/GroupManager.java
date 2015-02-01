/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package demenius.groupsupport;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Braydon
 */
public class GroupManager
{
    public GroupSupport plugin;
    private HashMap<String, Group> groups = new HashMap<String, Group>();
    private GroupCommandExecutor executor;

    public GroupManager(GroupSupport instance)
    {
        plugin = instance;
        executor = new GroupCommandExecutor(instance, this);
    }

    public void loadGroups(ObjectInputStream oIn) throws IOException, ClassNotFoundException
    {
        Group group;
        try
        {
            while (true)
            {
                group = (Group) oIn.readObject();
                this.groups.put(group.getName(), group);
            }
        } catch (EOFException ex)
        {
        }
    }

    public void saveGroups(ObjectOutputStream oOut) throws IOException
    {
        for (String key : this.groups.keySet())
        {
            oOut.writeObject(this.groups.get(key));
        }
    }

    public Group getGroup(String group)
    {
        return this.groups.get(group.toLowerCase());
    }

    public boolean groupExists(String group)
    {
        return this.groups.containsKey(group.toLowerCase());
    }

    public boolean playerInGroup(String player, String group)
    {
        if (!this.groupExists(group))
        {
            return false;
        }
        return executor.playerInGroup(groups.get(group), player);
    }

    public boolean groupUseAllowed(String player, String group)
    {
        if (!this.groupExists(group))
        {
            return false;
        }
        if (player.equals("CONSOLE"))
        {
            return true;
        }
        return this.groups.get(group.toLowerCase()).isAllowedUse(player);
    }

    public void execute(String cmd, String owner, String[] args)
    {
        if (cmd.equalsIgnoreCase("create"))
        {
            if (!this.createGroup(args[0], owner))
            {
                this.plugin.sendPlayerMessage(owner, args[0] + " Already Exists Or Is An Invalid Name");
                return;
            } else
            {
                this.plugin.sendPlayerMessage(owner, "The Group Has Been Created!");
            }
        }


        if (!this.groupExists(args[0]))
        {
            this.plugin.sendPlayerMessage(owner, args[0] + " Does Not Exist");
            return;
        }

        if (args.length > 1)
        {
            this.executor.execute(this.groups.get(args[0].toLowerCase()), owner, args);
        }
    }

    private boolean createGroup(String name, String owner)
    {
        if (this.groupExists(name) || !this.validGroupName(name))
        {
            return false;
        }
        groups.put(name.toLowerCase(), new Group(name, owner));
        return true;
    }

    private boolean validGroupName(String name)
    {
        if (name.length() > 10 || name.length() == 0)
        {
            return false;
        }
        if (this.plugin.playerExists(name))
        {
            return false;
        }

        if (name.substring(0, 1).matches("[a-zA-Z]"))
        {
            if (name.length() > 1)
            {
                for(int i = 1; i < name.length(); i++)
                {
                    if(!name.substring(i, i+1).matches("[a-zA-Z_0-9]"))
                        return false;
                }
            }
            return true;
        }
        return false;
    }

    public boolean renameGroup(String oldName, String newName)
    {
        if (this.groupExists(newName) || !this.validGroupName(newName))
        {
            return false;
        }
        Group oldGroup = groups.get(oldName);
        Group newGroup = new Group(newName, oldGroup.getOwner(), oldGroup.getPlayers(), oldGroup.getGroups());
        groups.put(newName.toLowerCase(), newGroup);
        groups.remove(oldName.toLowerCase());
        return true;
    }

    public boolean deleteGroup(String group, String player)
    {
        if (!this.groupExists(group))
        {
            return false;
        }
        if (!this.groups.get(group.toLowerCase()).isOwner(player))
        {
            return false;
        }
        this.groups.remove(group.toLowerCase());
        return true;
    }

    public String merge(String owner, String... args)
    {
        if (!this.createGroup(args[0], owner))
        {
            return "Group Name Taken Or Invalid";
        }
        String notFound = "";
        for (int i = 1; i < args.length; i++)
        {
            if (!this.groupExists(args[0]))
            {
                notFound += args[i];
                continue;
            }
            if (!this.groupUseAllowed(owner, args[i]))
            {
                notFound += args[i];
                continue;
            }
            if (this.groups.get(args[i]).getSize("players") > 0)
            {
                execute("-ap", owner, toStringArray(this.groups.get(args[i]).getPlayers()));
            }
            if (this.groups.get(args[i]).getSize("groups") > 0)
            {
                execute("-ag", owner, toStringArray(this.groups.get(args[i]).getGroups()));
            }
        }
        return "Merge Successfull" + (notFound.equals("") ? "" : " But Groups " + notFound + " Did Not Exist Or You Can Not Use Them");
    }

    private String[] toStringArray(ArrayList<String> list)
    {
        String[] endParams = new String[list.size()];
        for (int i = 0; i < list.size(); i++)
        {
            endParams[i] = list.get(i);
        }
        return endParams;
    }

    public String getGroupList(String player)
    {
        String names = "";
        int i = 0;
        for (String name : this.groups.keySet())
        {
            if (this.groupUseAllowed(player, name))
            {
                names += name;
                if (this.groups.keySet().size() > i + 1)
                {
                    names += ", ";
                }
                i++;
            }
        }
        return names;
    }

    public void getInGroupList(String player, String... list)
    {
        if (!groups.containsKey(list[0]))
        {
            this.plugin.sendPlayerMessage(player, "Group " + list[0] + " Does Not Exist");
            return;
        }

        if (!this.groupUseAllowed(player, list[0]))
        {
            this.plugin.sendPlayerMessage(player, "Group " + list[0] + " Is Restricted");
            return;
        }

        this.plugin.sendPlayerMessage(player, "Group Contains:");
        this.plugin.sendPlayerMessage(player, "owner: " + this.groups.get(list[0]).getOwner());
        if (list.length == 1)
        {
            this.plugin.sendPlayerMessage(player, executor.list(groups.get(list[0]), "players"));
            this.plugin.sendPlayerMessage(player, executor.list(groups.get(list[0]), "groups"));
            if (groups.get(list[0]).ownerOnly())
            {
                this.plugin.sendPlayerMessage(player, "allowed users: " + groups.get(list[0]).getOwner());
            } else
            {
                this.plugin.sendPlayerMessage(player, (groups.get(list[0]).isRestricted() ? "restricted " : "allowed ")
                        + executor.list(groups.get(list[0]), "users"));
            }
            return;
        }
        for (int i = 1; i < list.length; i++)
        {
            this.plugin.sendPlayerMessage(player, executor.list(groups.get(list[0]), list[i]));
        }
    }

}
