package com.mythicacraft.teleportledger;

import org.bukkit.entity.Player;

public class TeleportRequest {
	
	private Player owner;
	private Player requestee;
	private TeleportLedger.TELEPORT_TYPE TYPE;
	
	public TeleportRequest(Player owner, Player requestee, TeleportLedger.TELEPORT_TYPE type){
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
	
	public TeleportLedger.TELEPORT_TYPE getType(){
		return TYPE;
	}
	
	public void setOwner(Player owner){
		this.owner = owner;
	}
	
	public void setRequestee(Player requestee){
		this.requestee = requestee;
	}
	
	public void setType(TeleportLedger.TELEPORT_TYPE type){
		this.TYPE = type;
	}
	
}
