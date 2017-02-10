package com.kiralycraft.mobteleport;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.kiralycraft.mobteleport.DatabaseManager.Transfer;

public class DatabaseManager 
{
	class Transfer
	{
		private Player from=null,to=null;
		private BigDecimal cost=null;
		private Entity mob=null;
		private long lastActivity=0;
		public void setFrom(Player from)
		{
			this.from = from;
		}
		public void setTo(Player to)
		{
			this.to = to;
		}
		public Player getTo()
		{
			return this.to;
		}
		public Player getFrom()
		{
			return this.from;
		}
		public void setPrice(BigDecimal price)
		{
			this.cost = price;
		}
		public BigDecimal getPrice()
		{
			return this.cost;
		}
		public void setMob(Entity mob)
		{
			this.mob = mob;
		}
		public Entity getMob()
		{
			return this.mob;
		}
		public void setLastActivity(long l)
		{
			this.lastActivity = l;
		}
		public long getLastActivity()
		{
			return this.lastActivity;
		}
	}
	ArrayList<Transfer> transfers;

	ArrayList<Transfer> expiredList;
	public DatabaseManager()
	{
		transfers = new ArrayList<Transfer>();
		expiredList =  new ArrayList<Transfer>();
	}
	protected boolean isPlayerInSendDatabase(Player player) 
	{
		synchronized (transfers)
		{
			for (Transfer tr:transfers)
			{
				if (tr.getFrom().equals(player))
				{
					return true;
				}
			}
			return false;
		}
	}
	protected Transfer getPlayerTransfer(Player player) 
	{
		synchronized (transfers)
		{
			for (Transfer tr:transfers)
			{
				if (tr.getFrom().equals(player))
				{
					return tr;
				}
			}
			return null;
		}
	}
	protected boolean isTransferDataComplete(Player player)
	{
		synchronized (transfers)
		{
			for (Transfer tr:transfers)
			{
				if (tr.getFrom().equals(player))
				{
					return tr.getTo()!=null;
				}
			}
		}
		return false;
	}
	protected void updateLastActivity()
	{
		synchronized (transfers)
		{
			synchronized(expiredList)
			{
				expiredList.clear();
			}
			for (Transfer tr:transfers)
			{
				if (System.currentTimeMillis()-tr.getLastActivity()>=120*1000)
				{
					synchronized(expiredList)
					{
						expiredList.add(tr);
					}
				}
			}
			synchronized(expiredList)
			{
				for (Transfer t:expiredList)
				{
					transfers.remove(t);
				}
			}
		}
	}
	protected void addNewPlayer(Player player,Entity ent)
	{
		synchronized (transfers)
		{
			Transfer tmpTransf = new Transfer();
			tmpTransf.setFrom(player);
			tmpTransf.setMob(ent);
			tmpTransf.setLastActivity(System.currentTimeMillis());
			transfers.add(tmpTransf);
		}
	}
	public void updatePlayerMobChoice(Player p, Entity tmpEnt) 
	{
		synchronized (transfers)
		{
			for (Transfer tr:transfers)
			{
				if (tr.getFrom().equals(p))
				{
					tr.setMob(tmpEnt);
					break;
				}
			}
		}
	}
	public void addDestAndPrice(Player player, Player targetPlayer, BigDecimal bigDecimal) 
	{
		synchronized (transfers)
		{
			for (Transfer tr:transfers)
			{
				if (tr.getFrom().equals(player))
				{
					tr.setTo(targetPlayer);
					tr.setPrice(bigDecimal);
					tr.setLastActivity(System.currentTimeMillis());
					break;
				}
			}
		}
	}
	public boolean isTargetBusy(Player targetPlayer) 
	{
		synchronized (transfers)
		{
			for (Transfer tr:transfers)
			{
				if (tr.getTo()!=null && tr.getTo().equals(targetPlayer))
				{
					return true;
				}
			}
			return false;
		}
	}
	public Transfer getPlayerTargetTransfer(Player player) 
	{
		synchronized (transfers)
		{
			for (Transfer tr:transfers)
			{
				if (tr.getTo().equals(player))
				{
					return tr;
				}
			}
		}
		return null;
	}
	public boolean removeTransfer(Transfer tr)
	{
		synchronized (transfers)
		{
			transfers.remove(tr);
			return true;
		}
	}
}
