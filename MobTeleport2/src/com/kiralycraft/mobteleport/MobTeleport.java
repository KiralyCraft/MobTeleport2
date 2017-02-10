package com.kiralycraft.mobteleport;

import java.math.BigDecimal;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.earth2me.essentials.api.Economy;
import com.earth2me.essentials.api.NoLoanPermittedException;
import com.earth2me.essentials.api.UserDoesNotExistException;


public class MobTeleport extends JavaPlugin implements Listener
{
	DatabaseManager databaseMan;
	public void onEnable()
	{
		databaseMan = new DatabaseManager();
		getServer().getPluginManager().registerEvents(this, this);
	}
 
	public void onDisable()
	{
		
	}
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if (sender instanceof Player) 
		{
			Player player = (Player) sender;
			return CommandHandler.handleCommand(player,cmd,args,databaseMan);
		}
		return false;
	}
	@EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event)
	{
        CommandHandler.handleInteraction(event,databaseMan);
    }
	protected static boolean addMoney(Player player,BigDecimal amount)
	{
		try 
		{
			Economy.add(player.getName(), amount);
			return true;
		} 
		catch (NoLoanPermittedException | ArithmeticException | UserDoesNotExistException e) 
		{
			return false;
		}
	}
	protected static boolean substractMoney(Player player,BigDecimal amount)
	{
		try 
		{
			if (Economy.hasEnough(player.getName(), amount))
			{
				Economy.substract(player.getName(), amount);
				return true;
			}
			else
			{
				return false;
			}
		} 
		catch (NoLoanPermittedException | ArithmeticException | UserDoesNotExistException e) 
		{
			return false;
		}
	}
}
