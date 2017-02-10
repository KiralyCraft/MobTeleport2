package com.kiralycraft.mobteleport;

import java.math.BigDecimal;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

import com.kiralycraft.mobteleport.DatabaseManager.Transfer;

public class CommandHandler
{

	public static boolean handleCommand(Player player, Command cmd, String[] args, DatabaseManager databaseMan) 
	{
		if (cmd.getLabel().contentEquals("mt"))
		{
			if (args.length==0)
			{
				sendChatMessage(player,ChatColor.GREEN,"Ajutor MobTeleport:");
				sendChatMessage(player,ChatColor.WHITE,"/mt sell <NumePlayer> <Suma> - Pentru a vinde un mob");
				sendChatMessage(player,ChatColor.WHITE,"/mt accept - Pentru a cumpara un mob care ti-a fost vandut");
				sendChatMessage(player,ChatColor.WHITE,"/mt deny - Pentru a refuza un mob care ti-a fost vandut");
				sendChatMessage(player,ChatColor.WHITE,"/mt deselect - Deselecteaza mobul selectat");
				sendChatMessage(player,ChatColor.GREEN,"Pentru a selecta un mob spre a fi vandut, tine in mana un carbune, apasa Shift si da click dreapta pe mob.");
				return true;
			}
			else
			{
				if (args[0].equalsIgnoreCase("sell")) //daca vrea sa vanda ceva
				{
					if (args.length>=3)//daca a specificat si player, si mob
					{
						databaseMan.updateLastActivity();
						if (!databaseMan.isPlayerInSendDatabase(player))
						{
							sendChatMessage(player,ChatColor.RED,"Nu ai selectat nici un mob. Da click dreapta pe un animal cu un carbune intre timp ce tii apasat Shift ca sa il selectezi.");
						}
						else if (databaseMan.isTransferDataComplete(player))
						{
							sendChatMessage(player,ChatColor.RED,"Ai deja un transfer in desfasurare. Foloste /mt deny ca sa il anulezi.");
						}
						else//cine e in database are setat from si mob, ramane to si pret
						{
							try 
							{
								Player targetPlayer = getPlayerByName(args[1]);
								if (targetPlayer==null)
								{
									sendChatMessage(player,ChatColor.RED,"Nu exista nici un player online cu \""+args[1]+"\" in nume.");
								}
								else
								{
									if (databaseMan.isTargetBusy(targetPlayer))
									{
										sendChatMessage(player,ChatColor.RED,"Playerul destinatar are un transfer in asteptare. Incearca iar peste 120 de secunde.");
									}
									else
									{
										if (!isValidNumber(args[2]))
										{
											sendChatMessage(player,ChatColor.RED,"Pretul introdus nu este un numar real.");
										}
										else
										{
											databaseMan.addDestAndPrice(player,targetPlayer,new BigDecimal(Double.parseDouble(args[2])));
											notifyTargetPlayer(player,targetPlayer,databaseMan);
											return true;
										}
									}
								}
							}
							catch (Exception e) 
							{
								sendChatMessage(player,ChatColor.RED,"Exista mai multi playeri care au \""+args[1]+"\" in nume. Specifica numele mai clar.");
							}
						}
					}
					else
					{
						return false;
					}
				}
				else if (args[0].equalsIgnoreCase("accept"))
				{
					databaseMan.updateLastActivity();
					if (!databaseMan.isTargetBusy(player))
					{
						sendChatMessage(player,ChatColor.RED,"Nu ai nici un transfer in asteptare.");
					}
					else
					{
						Transfer tmpTransfer = databaseMan.getPlayerTargetTransfer(player);
						if (!MobTeleport.substractMoney(tmpTransfer.getTo(), tmpTransfer.getPrice()))
						{
							sendChatMessage(tmpTransfer.getTo(),ChatColor.RED,"Nu ai destui bani.");
							sendChatMessage(tmpTransfer.getFrom(),ChatColor.RED,"Destinatarul nu are destui bani. Transfer anulat.");
							databaseMan.removeTransfer(tmpTransfer);
							
						}
						else
						{
							tmpTransfer.getMob().teleport(tmpTransfer.getTo());
							MobTeleport.addMoney(tmpTransfer.getFrom(), tmpTransfer.getPrice());
							sendChatMessage(tmpTransfer.getFrom(),ChatColor.GREEN,"Ai primit "+tmpTransfer.getPrice().intValue()+" bani pentru ca i-ai vandut un "+tmpTransfer.getMob().getName()+" lui "+tmpTransfer.getTo().getName());
							sendChatMessage(tmpTransfer.getTo(),ChatColor.GREEN,"Ai platit "+tmpTransfer.getPrice().intValue()+" bani pentru pentru ca ai cumparat un "+tmpTransfer.getMob().getName()+" de la "+tmpTransfer.getFrom().getName());
							databaseMan.removeTransfer(tmpTransfer);
						}
					}
				}
				else if (args[0].equalsIgnoreCase("deny"))
				{
					databaseMan.updateLastActivity();
					if (!databaseMan.isTargetBusy(player))
					{
						sendChatMessage(player,ChatColor.RED,"Nu ai nici un transfer in asteptare.");
					}
					else
					{
						Transfer tmpTransfer = databaseMan.getPlayerTargetTransfer(player);
						sendChatMessage(tmpTransfer.getFrom(),ChatColor.RED,"Destinatarul a anulat transferul.");
						databaseMan.removeTransfer(tmpTransfer);
						sendChatMessage(player,ChatColor.GREEN,"Transferul in asteptare a fost anulat.");
					}
					
					if (!databaseMan.isTransferDataComplete(player))
					{
						sendChatMessage(player,ChatColor.RED,"Nu ai nici un transfer in desfasurare.");
					}
					else
					{
						Transfer tmpTransfer = databaseMan.getPlayerTransfer(player);
						sendChatMessage(tmpTransfer.getTo(),ChatColor.RED,"Vanzatorul a anulat transferul.");
						databaseMan.removeTransfer(tmpTransfer);
						sendChatMessage(player,ChatColor.GREEN,"Transferul in desfasurare anulat.");
					}
				}
				else if (args[0].equalsIgnoreCase("deselect"))
				{
					if (!databaseMan.isPlayerInSendDatabase(player))
					{
						sendChatMessage(player,ChatColor.RED,"Nu ai selectat nici un mob. Nu ai ce sa deselectezi.");
					}
					else
					{
						Transfer tmpTransfer = databaseMan.getPlayerTransfer(player);
						databaseMan.removeTransfer(tmpTransfer);
						sendChatMessage(player,ChatColor.GREEN,"Mobul a fost deselectat.");
					}
				}
				else if (args[0].equalsIgnoreCase("info"))
				{
					sendChatMessage(player,ChatColor.GREEN,"Plugin facut cu dragoste de KiralyCraft <3");
				}
				else
				{
					return false;
				}
			}
		}
		return true;
	}
	private static void notifyTargetPlayer(Player player, Player targetPlayer2, DatabaseManager databaseMan) 
	{
		player.sendMessage(ChatColor.GREEN+"Cererea a fost facuta. Foloseste /mt deny pentru a o anula.");
		Transfer tmpTransfer = databaseMan.getPlayerTransfer(player);
		targetPlayer2.sendMessage(ChatColor.AQUA+player.getName()+ChatColor.GOLD+" doreste sa iti trimita un "+ChatColor.GREEN+tmpTransfer.getMob().getName()+ChatColor.GOLD+" pentru "+ChatColor.AQUA+tmpTransfer.getPrice()+ChatColor.GOLD+" bani.");
		targetPlayer2.sendMessage(ChatColor.RED+"Scrie "+ChatColor.DARK_GREEN+"/mt accept"+ChatColor.RED+" pentru a-l cumpara.");
		targetPlayer2.sendMessage(ChatColor.RED+"Scrie "+ChatColor.DARK_GREEN+"/mt deny"+ChatColor.RED+" pentru a-l refuza.");
		
	}
	public static void handleInteraction(PlayerInteractEntityEvent event, DatabaseManager databaseMan) 
	{
		Player p = event.getPlayer();
		if (p.getInventory().getItemInMainHand().getType().equals(Material.COAL) && p.isSneaking() && event.getHand() == EquipmentSlot.HAND)
		{
			Entity tmpEnt = event.getRightClicked();
			if (!(tmpEnt instanceof LivingEntity) || (tmpEnt instanceof Player))
			{
				sendChatMessage(p,ChatColor.RED,"Poti selecta doar mobi (animale/monstri).");
			}
			else
			{
				if (!databaseMan.isPlayerInSendDatabase(p))
				{
					databaseMan.addNewPlayer(p, tmpEnt);
					sendChatMessage(p,ChatColor.GREEN,"Mobul a fost selectat.");
				}
				else
				{
					databaseMan.updatePlayerMobChoice(p,tmpEnt);
					sendChatMessage(p,ChatColor.GREEN,"Mobul selectat a fost actualizat.");
				}
			}
		}
	}
	protected static void sendChatMessage(Player pl,ChatColor red,String msg)
	{
		pl.sendMessage(red+msg);
	}
	private static Player getPlayerByName(String str) throws Exception
	{
		Player foundplayer = null;
		for (Player pl:Bukkit.getOnlinePlayers())
		{
			if (pl.getName().toLowerCase().contains(str.toLowerCase()))
			{
				if (foundplayer!=null)
				{
					throw new Exception("Too many players with the same name");
				}
				else
				{
					foundplayer = pl;
				}
			}
		}
		return foundplayer;
	}
	private static boolean isValidNumber(String s)
	{
		try
		{
			Double.parseDouble(s);
			return true;
		}
		catch(Exception e)
		{
			return false;
		}
	}
}
