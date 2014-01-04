package com.mythicacraft.teleportledger;

import org.bukkit.entity.Player;

import com.mythicacraft.teleportledger.TeleportLedger.TeleportType;

public class TeleportRequest {
	
	private Player owner;
	private Player requestee;
	private TeleportType TYPE;
	
	public TeleportRequest(Player owner, Player requestee, TeleportType type){
		this.owner = owner;
		this.requestee = requestee;
		this.TYPE = type;
	}
	
	public Player getOwner(){
		return owner;
	}
	
	public String getOwnerName(){
		return owner.getName().toString();
	}
	
	public Player getRequestee(){
		return requestee;
	}
	
	public String getRequesteeName(){
		return requestee.getName().toString();
	}
	
	public TeleportType getType(){
		return TYPE;
	}
	
	public void setOwner(Player owner){
		this.owner = owner;
	}
	
	public void setRequestee(Player requestee){
		this.requestee = requestee;
	}
	
	public void setType(TeleportType type){
		this.TYPE = type;
	}
	
}
