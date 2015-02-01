/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package demenius.groupsupport;

//import com.nijiko.permissions.PermissionHandler;
//import com.nijikokun.bukkit.Permissions.Permissions;
import java.io.*;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class GroupSupport extends JavaPlugin
{
    public static final Logger log = Logger.getLogger("Minecraft");
    //private static PermissionHandler permissionHandler = null;
    private GroupManager manager;

    @Override
    public void onEnable()
    {
        manager = new GroupManager(this);

        try
        {
            if (!getDataFolder().exists())
            {
                log.info("[" + getDescription().getName() + "] Data Folder Not Found!");
                log.info("[" + getDescription().getName() + "] Creating Data Folder ");
                new File(getDataFolder().toString()).mkdir();
            }

            if (!new File(getDataFolder(), "GroupSupport.ser").exists())
            {
                if (new File("GroupSupport.ser").exists())
                {
                    new File("GroupSupport.ser").renameTo(new File(getDataFolder(), "GroupSupport.ser"));
                }
            }
        } catch (SecurityException i)
        {
            log.severe("[" + getDescription().getName() + "] Could not read/write GroupSupport data folder! Aborting.");
            return;
        }

        try
        {
            FileInputStream fIn = new FileInputStream(new File(getDataFolder(), "GroupSupport.ser"));
            ObjectInputStream oIn = new ObjectInputStream(fIn);
            manager.loadGroups(oIn);
            oIn.close();
            fIn.close();
            log.info("[" + getDescription().getName() + "] Groups Have Been Loaded.");
        } catch (IOException i)
        {
            log.warning("[" + getDescription().getName() + "] Could Not Load GroupSupport Groups File!");
            log.warning("[" + getDescription().getName() + "] If This Is Not Your First Run, Please Stop Your Server, Or Risk Loosing Your Groups");
            log.warning("[" + getDescription().getName() + "] The File plugins/GroupSupport/GroupSupport.ser Is Missing Or Unreadable.");
            log.warning("[" + getDescription().getName() + "] Please Check That The File /plugin/GroupSupport/GroupSupport.ser Exists.");
            log.warning("[" + getDescription().getName() + "] If This Is Your First Time Running The Plugin, Ignore This Message.");
        } catch (ClassNotFoundException i)
        {
            log.severe("[" + getDescription().getName() + "] Something Has Gone Wrong. Please Leave A Forum Post");
        }
    }

    @Override
    public void onDisable()
    {
        try
        {
            String name = getDataFolder() + "/GroupSupport.ser";

            FileOutputStream fOut = new FileOutputStream(name);
            ObjectOutputStream oOut = new ObjectOutputStream(fOut);
            manager.saveGroups(oOut);
            oOut.close();
            fOut.close();

            log.info("[" + getDescription().getName() + "] Groups Have Been Saved");
        } catch (IOException i)
        {
            log.warning("Could Not Save GroupSupport Data!");
        }
    }

    public boolean playerExists(String player)
    {
        OfflinePlayer[] offpl = this.getServer().getOfflinePlayers();
        for (int i = 0; i < offpl.length; i++)
        {
            if (player.equalsIgnoreCase(offpl[i].getName()))
            {
                return true;
            }
        }
        return false;
    }

    public boolean playerInGroup(Player player, String group)
    {
        return manager.playerInGroup(player.getName(), group);
    }

    public boolean groupUseAllowed(Player player, String group)
    {
        return this.manager.groupUseAllowed(player.getName(), group);
    }

    public void sendPlayerMessage(String p, String msg)
    {
        Player player = this.getServer().getPlayer(p);
        if (player != null)
        {
            player.sendMessage(ChatColor.RED + msg);
        } else
        {
            log.info(msg);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
    {
        if (args.length < 1)
        {
            return false;
        }
        if (cmd.getName().equalsIgnoreCase("GroupSupport"))
        {
            String argList[] = new String[args.length - 1];
            for (int i = 1; i < args.length; i++)
            {
                argList[i - 1] = args[i];
            }
            return useCommand(sender, args[0].toLowerCase(), argList);
        }
        return false;
    }

    private boolean useCommand(CommandSender sender, String commandLabel, String[] args)
    {
        if (commandLabel.equalsIgnoreCase("create") || commandLabel.equalsIgnoreCase("modify"))
        {
            if (args.length < 1)
            {
                return false;
            }
            this.manager.execute(commandLabel, sender.getName(), args);
            return true;
        } else if (commandLabel.equalsIgnoreCase("merge"))
        {
            if (args.length < 2)
            {
                return false;
            }
            sender.sendMessage(ChatColor.RED + this.manager.merge(sender.getName(), args));
            return true;
        } else if (commandLabel.equalsIgnoreCase("delete"))
        {
            if (args.length < 1)
            {
                return false;
            }
            for(int i = 0; i < args.length; i++)
            {
                if (!manager.deleteGroup(args[i], sender.getName()))
                {
                    sender.sendMessage(ChatColor.RED + args[i] + " Does Not Exist Or Access Denied");
                }else
                    sender.sendMessage(ChatColor.RED + args[i] + " Deleted");
            }
            return true;
        } else if (commandLabel.equalsIgnoreCase("list"))
        {
            if (args.length == 0)
            {
                sender.sendMessage(ChatColor.RED + "Available Groups: ");
                sender.sendMessage(ChatColor.RED + manager.getGroupList(sender.getName()));
            } else
            {
                manager.getInGroupList(sender.getName(), args);
            }
            return true;
        } else if(commandLabel.equalsIgnoreCase("reset"))
        {
            if(args.length == 0)
            {
                return false;
            }
            for(int i = 0; i < args.length; i++)
            {
                this.useCommand(sender, "modify", new String[] {args[i], "-cp", "-cg", "-cu"});
            }
            return true;
        }else if (commandLabel.equalsIgnoreCase("help"))
        {
            if (args.length == 0)
            {
                sender.sendMessage(ChatColor.RED + "For More Info On A Specific Command, Type help [command-name]");
                sender.sendMessage(ChatColor.RED + "All Command Are Case Insensative.");

                sender.sendMessage(ChatColor.RED + "Create   -   Creates A New Group, Group Name Must Be Unique");
                sender.sendMessage(ChatColor.RED + "Modify   -   Modify An Existing Group");
                sender.sendMessage(ChatColor.RED + "Merge    -   Merge 1 Or More Groups Into A New Group");
                sender.sendMessage(ChatColor.RED + "List     -   Lists Groups On The Server Or Members In A Group");
                sender.sendMessage(ChatColor.RED + "Reset    -   Clears All Players/Groups/Users From The Group");
                sender.sendMessage(ChatColor.RED + "Delete   -   Delete 1 Or More Existing Groups");
                sender.sendMessage(ChatColor.RED + "Help     -   Provides Help Info For GroupSupport");
                sender.sendMessage(ChatColor.RED + "Example  -   Provides A Detailed Example");
                sender.sendMessage(ChatColor.RED + "For Additional Details, Please Visit []");
            } else
            {
                if (args[0].equalsIgnoreCase("create"))
                {
                    sender.sendMessage(ChatColor.RED + "Create   -   Creates A New Group, Group Name Must Be Unique");
                    sender.sendMessage(ChatColor.RED + "The Group Will Not Be Created If The Name Already Exists");
                    sender.sendMessage(ChatColor.RED + "All Options Except Name Are Optional And Can Be Modified");
                    sender.sendMessage(ChatColor.RED + "Create [name] -ap ... -ag ... -au ... [-r -a -o]");
                    sender.sendMessage(ChatColor.RED + "-ap/ag/au ... Adds Players/Groups/Users Into The Group, Case Insensitive");
                    sender.sendMessage(ChatColor.RED + "[-r -a -o] Restricts|Allows Users To Use Group Or Owner Only Respectively");
                    sender.sendMessage(ChatColor.RED + "Only Use One Of [-r|-a|-o], If None Are Used It Defaults To Public Unless Users Have Been Added");
                    sender.sendMessage(ChatColor.RED + "The Last Of [-r|-a|-o] Entered Will Supersede All Others");
                } else if (args[0].equalsIgnoreCase("modify"))
                {
                    sender.sendMessage(ChatColor.RED + "Modify   -   Modify An Existing Group");
                    sender.sendMessage(ChatColor.RED + "You Must Be The Owner To Modify A Group");
                    sender.sendMessage(ChatColor.RED + "All Options Except [name] Are Optional");
                    sender.sendMessage(ChatColor.RED + "Modify [name] -cp -cg -cu -ap ... -ag ... -au ... -rp ... -rg ... -ru ... -rn [name] [-r -a -o]");
                    sender.sendMessage(ChatColor.RED + "-cp/cg/cu Clears The players/groups/users Lists In The Group");
                    sender.sendMessage(ChatColor.RED + "-ap/ag/au ... Adds Players/Groups/Users Into The Group, Case Insensitive");
                    sender.sendMessage(ChatColor.RED + "-rp/rg/ru ... Removes Players/Groups/Users From The Group, Case Insensitive");
                    sender.sendMessage(ChatColor.RED + "-rp/rg/ru ... Removes Players/Groups/Users From The Group, Case Insensitive");
                    sender.sendMessage(ChatColor.RED + "-rn ... Renames The Group, [name] Must Be Unique");
                    sender.sendMessage(ChatColor.RED + "[-r -a -o] Restricts|Allows Users To Use Group Or Owner Only Respectively");
                    sender.sendMessage(ChatColor.RED + "Only Use One Of [-r|-a|-o], If None Are Used It Defaults To Public Unless Users Have Been Added");
                    sender.sendMessage(ChatColor.RED + "The Last Of [-r|-a|-o] Entered Will Supersede All Others");
                } else if (args[0].equalsIgnoreCase("merge"))
                {
                    sender.sendMessage(ChatColor.RED + "Merge    -   Merge 1 Or More Groups Into A New Group");
                    sender.sendMessage(ChatColor.RED + "Merge [name] [group] [group] ...");
                    sender.sendMessage(ChatColor.RED + "Ex. merge groups123 group1 group2 group3");
                } else if (args[0].equalsIgnoreCase("list"))
                {
                    sender.sendMessage(ChatColor.RED + "List     -   Lists Groups On The Server Or Members In A Group");
                    sender.sendMessage(ChatColor.RED + "list [name] [players|groups|users]");
                    sender.sendMessage(ChatColor.RED + "[name] Is Optional, If [name] Is Used It Will List All Members In That Group");
                    sender.sendMessage(ChatColor.RED + "[players|groups|users] Is Optional, If Used [name] Is Required");
                    sender.sendMessage(ChatColor.RED + "Will Show All Members Of The Selected List(s)");
                    sender.sendMessage(ChatColor.RED + "list");
                    sender.sendMessage(ChatColor.RED + "Will List All Groups On The Server");
                    sender.sendMessage(ChatColor.RED + "list groupName players groups");
                    sender.sendMessage(ChatColor.RED + "Will List All Members In The players and groups Lists In groupName");
                } else if (args[0].equalsIgnoreCase("reset"))
                {
                    sender.sendMessage(ChatColor.RED + "Reset    -   Clears All Players/Groups/Users From The Groups");
                    sender.sendMessage(ChatColor.RED + "reset [name1] [name2] ...");
                    sender.sendMessage(ChatColor.RED + "Same As Calling modify [name1] -cp -cg -cu");
                    sender.sendMessage(ChatColor.RED + "                modify [name2] -cp -cg -cu");
                } else if (args[0].equalsIgnoreCase("delete"))
                {
                    sender.sendMessage(ChatColor.RED + "Delete   -   Delete 1 Or More Existing Groups");
                    sender.sendMessage(ChatColor.RED + "Delete   [group] [group] ...");
                    sender.sendMessage(ChatColor.RED + "Ex. delete group1 group2 group3");
                } else if (args[0].equalsIgnoreCase("help"))
                {
                    sender.sendMessage(ChatColor.RED + "Help     -   Provides Help Info For ServerGroups");
                    sender.sendMessage(ChatColor.RED + "Help [command-name]");
                    sender.sendMessage(ChatColor.RED + "Ex. help modify");
                } else if (args[0].equalsIgnoreCase("example"))
                {
                    sender.sendMessage(ChatColor.RED + "Help     -   Provides Help Info For ServerGroups");
                } else
                {
                    sender.sendMessage(ChatColor.RED + "Example  -   Provides A Detailed Example");
                }
            }
            return true;
        }
        return false;
    }

}