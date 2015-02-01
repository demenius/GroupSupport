/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package demenius.groupsupport;

import java.io.Serializable;
import java.util.ArrayList;

public class Group implements Serializable
{
    private static final long serialVersionUID = -4241828177523462327L;
    private String name;
    private String owner;
    private boolean onlyOwner;
    private boolean restricted;
    private ArrayList<String> players = new ArrayList<String>();
    private ArrayList<String> groups = new ArrayList<String>();
    private ArrayList<String> users = new ArrayList<String>();

    public Group(String name, String owner)
    {
        this.name = name;
        this.owner = owner;
        this.onlyOwner = false;
        this.restricted = false;
    }

    public Group(String name, String owner, ArrayList<String> p, ArrayList<String> g)
    {
        this(name, owner);

        this.players.addAll(p);
        this.groups.addAll(g);
    }

    public String getName()
    {
        return this.name;
    }

    public boolean setName(String newName)
    {
        if (this.name.equalsIgnoreCase(newName))
        {
            return false;
        }
        this.name = newName;
        return true;
    }

    public String getOwner()
    {
        return this.owner;
    }

    public boolean isOwner(String player)
    {
        return player.equalsIgnoreCase(owner);
    }

    public boolean ownerOnly()
    {
        return this.onlyOwner;
    }

    public void toggleOwnerOnly()
    {
        this.onlyOwner = !this.onlyOwner;
    }

    public boolean isRestricted()
    {
        return this.restricted;
    }

    public void setRestricted(boolean flag)
    {
        this.onlyOwner = false;
        this.restricted = flag;
    }
    
    public boolean isAllowedUse(String player)
    {
        if(this.onlyOwner) return this.isOwner(player);
        return this.isOwner(player) || (!this.restricted && this.users.contains(player))
                || (!this.onlyOwner && this.users.isEmpty());
    }

    public boolean add(String list, String item)
    {
        if (list.equals("players"))
        {
            if (this.players.contains(item.toLowerCase()))
            {
                return false;
            }
            this.players.add(item.toLowerCase());
        } else if (list.equals("groups"))
        {
            if (this.groups.contains(item.toLowerCase()))
            {
                return false;
            }
            this.groups.add(item.toLowerCase());
        } else if (list.equals("users"))
        {
            if (this.users.contains(item.toLowerCase()))
            {
                return false;
            }
            this.users.add(item.toLowerCase());
            this.onlyOwner = false;
        } else
        {
            return false;
        }
        return true;
    }

    public boolean remove(String list, String item)
    {
        if (list.equals("players"))
        {
            if (!this.players.contains(item.toLowerCase()))
            {
                return false;
            }
            this.players.remove(item.toLowerCase());
        } else if (list.equals("groups"))
        {
            if (!this.groups.contains(item.toLowerCase()))
            {
                return false;
            }
            this.groups.remove(item.toLowerCase());
        } else if (list.equals("users"))
        {
            if (!this.users.contains(item.toLowerCase()))
            {
                return false;
            }
            this.users.remove(item.toLowerCase());
        } else
        {
            return false;
        }
        return true;
    }

    public boolean contains(String list, String item)
    {
        if (list.equals("players"))
        {
            return players.contains(item.toLowerCase());
        } else if (list.equals("groups"))
        {
            return groups.contains(item.toLowerCase());
        } else if (list.equals("users"))
        {
            return users.contains(item.toLowerCase());
        } else
        {
            return false;
        }
    }

    public void clear(String list)
    {
        if (list.equals("players"))
        {
            players.clear();
        } else if (list.equals("groups"))
        {
            groups.clear();
        } else if (list.equals("users"))
        {
            users.clear();
        }
    }

    public int getSize(String info)
    {
        if (info.equals("players"))
        {
            return this.players.size();
        }
        if (info.equals("groups"))
        {
            return this.groups.size();
        }
        if (info.equals("users"))
        {
            return this.users.size();
        }
        return -1;
    }

    public ArrayList<String> getPlayers()
    {
        return this.players;
    }

    public ArrayList<String> getGroups()
    {
        return this.groups;
    }

    public ArrayList<String> getUsers()
    {
        return this.users;
    }

}