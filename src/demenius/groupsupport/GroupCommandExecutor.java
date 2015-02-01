/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package demenius.groupsupport;

import java.util.ArrayList;

/**
 *
 * @author Braydon
 */
public class GroupCommandExecutor
{
    private GroupSupport plugin;
    private GroupManager manager;

    public GroupCommandExecutor(GroupSupport instance, GroupManager manager)
    {
        this.plugin = instance;
        this.manager = manager;
    }

    public void execute(Group sg, String player, String[] args)
    {
        if (!sg.isOwner(player))
        {
            this.plugin.sendPlayerMessage(player, "You Do Not Have Permission To Access This Group");
            return;
        }
        ArrayList<String> cmds = new ArrayList<String>();
        for (int i = 0; i < args.length; i++)
        {
            if (args[i].startsWith("-"))
            {
                cmds.add(args[i]);
            }
        }

        for (String cmd : cmds)
        {
            this.modify(sg, player, cmd, getParams(cmd, args));
        }
    }

    private String[] getParams(String cmd, String[] args)
    {
        ArrayList<String> params = new ArrayList<String>();
        for (int i = 0; i < args.length; i++)
        {
            if (args[i].equals(cmd))
            {
                for (int j = i + 1; j < args.length; j++)
                {
                    if (args[j].startsWith("-"))
                    {
                        break;
                    }
                    params.add(args[j]);
                }
            }
        }

        String[] endParams = new String[params.size()];
        for (int i = 0; i < params.size(); i++)
        {
            endParams[i] = params.get(i);
        }

        return endParams;
    }

    private boolean isValidAttributeItem(String attr, String item)
    {
        boolean validPlayer = (attr.equals("-ap") || attr.equals("-au")) && this.plugin.playerExists(item);
        boolean validGroup = attr.equals("-ag") && this.manager.groupExists(item);

        return validPlayer || validGroup;
    }

    private String getList(String attr)
    {
        if (attr.equals("-ap") || attr.equals("-rp") || attr.equals("-cp"))
        {
            return "players";
        }
        if (attr.equals("-ag") || attr.equals("-rg") || attr.equals("-cg"))
        {
            return "groups";
        }
        if (attr.equals("-au") || attr.equals("-ru") || attr.equals("-cu"))
        {
            return "users";
        } else
        {
            return "";
        }
    }

    private void modify(Group sg, String player, String attr, String... args)
    {
        if (attr.equals("-ap") || attr.equals("-ag") || attr.equals("-au"))
        {
            if (args.length == 0)
            {
                this.plugin.sendPlayerMessage(player, attr + " Was An Empty Command");
                return;
            }
            String successMessage = "";
            String failMessage = "";
            int sn = 0;
            int fn = 0;
            for (int i = 0; i < args.length; i++)
            {
                if (!isValidAttributeItem(attr, args[i]))
                {
                    failMessage += args[i] + ", ";
                    fn++;
                    continue;
                }
                if (attr.equals("-au") && sg.isOwner(args[i]))
                {
                    this.plugin.sendPlayerMessage(player, "You Are The Owner, You Can Already Use This Group");
                    continue;
                }
                if (attr.equals("-ag") && sg.getName().equalsIgnoreCase(args[i]))
                {
                    this.plugin.sendPlayerMessage(player, "You Can't Add A Group Into Itself");
                    continue;
                }
                if (sg.add(getList(attr), args[i]))
                {
                    successMessage += args[i] + ", ";
                    sn++;
                } else
                {
                    failMessage += args[i] + ", ";
                    fn++;
                }
            }
            if (!successMessage.equals(""))
            {
                this.plugin.sendPlayerMessage(player, successMessage.substring(0, successMessage.length() - 2)
                        + ((sn == 1) ? " Has Been Added" : " Have Been Added"));
            }
            if (!failMessage.equals(""))
            {
                this.plugin.sendPlayerMessage(player, failMessage.substring(0, failMessage.length() - 2)
                        + ((fn == 1) ? " Was Already In The Group Or Is Invalid" : " Were Already In The Group Or Are Invalid"));
            }

        } else if (attr.equals("-rp") || attr.equals("-rg") || attr.equals("-ru"))
        {
            if (args.length == 0)
            {
                this.plugin.sendPlayerMessage(player, attr + " Was An Empty Command");
            }
            String successMessage = "";
            String failMessage = "";
            int sn = 0;
            int fn = 0;
            for (int i = 0; i < args.length; i++)
            {
                if (sg.remove(getList(attr), args[i]))
                {
                    successMessage += args[i] + ", ";
                    sn++;
                } else
                {
                    failMessage += args[i] + ", ";
                    fn++;
                }
            }
            if (!successMessage.equals(""))
            {
                this.plugin.sendPlayerMessage(player, successMessage.substring(0, successMessage.length() - 2)
                        + ((sn == 1) ? " Has Been Removed" : " Have Been Removed"));
            }
            if (!failMessage.equals(""))
            {
                this.plugin.sendPlayerMessage(player, failMessage.equals("") ? "" : failMessage.substring(0, failMessage.length() - 2)
                        + ((fn == 1) ? " Was Not In The Group" : " Were Not In The Group"));
            }

        } else if (attr.equals("-cp") || attr.equals("-cg") || attr.equals("-cu"))
        {
            sg.clear(getList(attr));
            this.plugin.sendPlayerMessage(player, getList(attr) + " Was Successfully Cleared");
        } else if (attr.equals("-rn"))
        {
            if (!sg.setName(args[0]))
            {
                this.plugin.sendPlayerMessage(player, sg.getName() + " Is Already The Group Name");
                return;
            }
            if (!this.manager.renameGroup(sg.getName(), args[0]))
            {
                this.plugin.sendPlayerMessage(player, "Group Name Is In Use Or Is Invalid");
                return;
            }
            this.plugin.sendPlayerMessage(player, "Group Is Now Named " + args[0]);
        } else if (attr.equals("-o"))
        {
            sg.toggleOwnerOnly();
            this.plugin.sendPlayerMessage(player, "Group Is Set To " + (sg.ownerOnly() ? "Owner Use Only"
                    : sg.isRestricted() ? "Restricted Access" : "Allowed Access"));
        } else if (attr.equals("-r"))
        {
            sg.setRestricted(true);
            this.plugin.sendPlayerMessage(player, "Group Is Set To Restricted Access");
        } else if (attr.equals("-a"))
        {
            sg.setRestricted(false);
            this.plugin.sendPlayerMessage(player, "Group Is Set To Allowed Access");
        } else
        {
            this.plugin.sendPlayerMessage(player, attr + " Is Not A Valid Attribute.");
        }
    }

    public boolean playerInGroup(Group sg, String player)
    {
        if (sg.contains("players", player))
        {
            return true;
        }
        ArrayList<String> groupsSearched = new ArrayList<String>();
        for (String s : sg.getGroups())
        {
            if (this.playerInGroup(s, player, groupsSearched))
            {
                return true;
            }
        }
        return false;
    }

    private boolean playerInGroup(String group, String player, ArrayList<String> groupsSearched)
    {
        if (groupsSearched.contains(group))
        {
            return false;
        }
        if (this.manager.getGroup(group).contains("players", player))
        {
            return true;
        }
        groupsSearched.add(group);
        for (String s : this.manager.getGroup(group).getGroups())
        {
            if (this.playerInGroup(s, player, groupsSearched))
            {
                return true;
            }
        }
        return false;
    }

    public String list(Group sg, String info)
    {
        ArrayList<String> members;
        String names = "";
        if (info.equals("players"))
        {
            members = sg.getPlayers();
        } else if (info.equals("groups"))
        {
            members = sg.getGroups();
        } else if (info.equals("users"))
        {
            if (sg.ownerOnly())
            {
                return sg.getOwner();
            }
            if (sg.getSize("users") == 0)
            {
                return info + ": " + (sg.isRestricted() ? "Noone" : "Everyone");
            }
            if (!sg.isRestricted())
            {
                names += sg.getOwner() + ", ";
            }
            members = sg.getUsers();
        } else
        {
            return "Not A Valid List In Group";
        }
        for (String member : members)
        {
            names += member + ", ";
        }
        return info + ": " + (names.length() > 2 ? names.substring(0, names.length() - 2) : "");
    }

}
