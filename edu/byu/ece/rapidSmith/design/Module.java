/*
 * Copyright (c) 2010 Brigham Young University
 * 
 * This file is part of the BYU RapidSmith Tools.
 * 
 * BYU RapidSmith Tools is free software: you may redistribute it 
 * and/or modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 2 of 
 * the License, or (at your option) any later version.
 * 
 * BYU RapidSmith Tools is distributed in the hope that it will be 
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 * 
 * A copy of the GNU General Public License is included with the BYU 
 * RapidSmith Tools. It can be found at doc/gpl2.txt. You may also 
 * get a copy of the license at <http://www.gnu.org/licenses/>.
 * 
 */
package edu.byu.ece.rapidSmith.design;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import com.caucho.hessian.io.Deflation;
import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;

import edu.byu.ece.rapidSmith.design.helper.PortMap;
import edu.byu.ece.rapidSmith.design.helper.ExternalPortSignal;
import edu.byu.ece.rapidSmith.device.Device;
import edu.byu.ece.rapidSmith.device.PrimitiveSite;
import edu.byu.ece.rapidSmith.device.PrimitiveType;
import edu.byu.ece.rapidSmith.device.Tile;
import edu.byu.ece.rapidSmith.device.TileType;
import edu.byu.ece.rapidSmith.device.helper.HashPool;
import edu.byu.ece.rapidSmith.util.FileTools;
import edu.byu.ece.rapidSmith.util.MessageGenerator;

/**
 * This class represents the modules as found in XDL.  They are used to describe
 * hard macros and RPMs and instances of each.
 * @author Chris Lavin
 * Created on: Jun 22, 2010
 */
public class Module implements Serializable{

	private static final long serialVersionUID = 7127893920489370872L;
	/** This is the key into externalPortMap for retrieving the constraints used to build the hard macro */
	public static final String moduleBuildConstraints = "MODULE_BUILD_CONSTRAINTS";
	/** Unique name of this module */
	private String name;
	/** All of the attributes in this module */
	private ArrayList<Attribute> attributes;
	/** This is the anchor of the module */
	private Instance anchor;
	/** Ports on the module */
	private HashMap<String, Port> portMap;
	/** Instances which are part of the module */
	private HashMap<String,Instance> instanceMap;
	/** Nets of the module */
	private HashMap<String,Net> netMap;
	/** Keeps track of the minimum clock period of this module */
	private float minClkPeriod = Float.MAX_VALUE;
	/** Provides a catch-all map to store information about hard macro */
	private HashMap<String, ArrayList<String>> metaDataMap;
	
	private ArrayList<PrimitiveSite> validPlacements;

	/**
	 * Empty constructor, strings are null, everything else is initialized
	 */
	public Module(){
		name = null;
		anchor = null;
		attributes = new ArrayList<Attribute>();
		portMap = new HashMap<String, Port>();
		instanceMap = new HashMap<String,Instance>();
		netMap = new HashMap<String,Net>();
		validPlacements = new ArrayList<PrimitiveSite>();
	}
	
	/**
	 * Creates and returns a new hard macro design with the appropriate
	 * settings and adds this module to the module list.  
	 * @return A complete hard macro design with this module as the hard macro.
	 */
	public Design createDesignFromModule(String partName){
		Design design = new Design();
		design.setPartName(partName);
		design.setName(Design.hardMacroDesignName);
		design.setIsHardMacro(true);
		design.addModule(this);
		return design;
	}
	
	
	/**
	 * Sets the name of this module 
	 * @param name New name for this module
	 */
	public void setName(String name){
		this.name = name;
	}

	/**
	 * Gets and returns the current name of this module
	 * @return The current name of this module
	 */
	public String getName(){
		return name;
	}

	/**
	 * Gets and returns the current attributes of this module
	 * @return The current attributes of this module
	 */
	public ArrayList<Attribute> getAttributes(){
		return attributes;
	}
	
	/**
	 * Adds the attribute with value to this module.
	 * @param physicalName Physical name of the attribute.
	 * @param value Value to set the new attribute to.
	 */
	public void addAttribute(String physicalName, String logicalName, String value){
		attributes.add(new Attribute(physicalName, logicalName, value));
	}
	
	/**
	 * Add the attribute to this module.
	 * @param attribute The attribute to add.
	 */
	public void addAttribute(Attribute attribute){
		attributes.add(attribute);
	}
	
	/**
	 * Checks if the design attribute has an attribute with a physical
	 * name called phyisicalName.  
	 * @param physicalName The physical name of the attribute to check for.
	 * @return True if this module contains an attribute with the 
	 * 		   physical name physicalName, false otherwise.
	 */
	public boolean hasAttribute(String physicalName){
		for(Attribute attr : attributes){
			if(attr.getPhysicalName().equals(physicalName)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Sets the list of attributes for this module.
	 * @param attributes The new list of attributes to associate with this
	 * module.
	 */
	public void setAttributes(ArrayList<Attribute> attributes){
		this.attributes = attributes;
	}

	
	/**
	 * This gets and returns the instance anchor of the module.
	 * @return Instance which is the anchor for this module.
	 */
	public Instance getAnchor(){
		return anchor;
	}
	
	/**
	 * Gets and returns the instance in the module called name.
	 * @param name Name of the instance in the module to get.
	 * @return The instance name or null if it does not exist.
	 */
	public Instance getInstance(String name){
		return instanceMap.get(name);
	}
	
	/**
	 * Gets and returns all of the instances part of this module.
	 * @return The instances that are part of this module.
	 */
	public Collection<Instance> getInstances(){
		return instanceMap.values();
	}
	
	/**
	 * Gets and returns the net in the module called name.
	 * @param name Name of the net in the module to get.
	 * @return The net name or null if it does not exist.
	 */
	public Net getNet(String name){
		return netMap.get(name);
	}
	
	/**
	 * Gets and returns all the nets that are part of the module.
	 * @return The nets that are part of this module.
	 */
	public Collection<Net> getNets(){
		return netMap.values();
	}
	
	/**
	 * Removes a net from the design
	 * @param name The name of the net to remove.
	 */
	public void removeNet(String name){
		Net n = getNet(name);
		if(n != null) removeNet(n);
	}
	
	/**
	 * Removes a net from the design
	 * @param net The net to remove from the design.
	 */
	public void removeNet(Net net){
		for(Pin p : net.getPins()){
			p.getInstance().getNetList().remove(net);
			if(p.getNet().equals(net)){
				p.setNet(null);
			}
		}
		netMap.remove(net.getName());
	}
	
	
	/**
	 * This method carefully removes an instance in a design with its
	 * pins and possibly nets.  Nets are only removed if they are empty
	 * after removal of the instance's pins. This method CANNOT remove 
	 * instances that are part of a ModuleInstance.
	 * @param name The instance name in the module to remove.
	 * @return True if the operation was successful, false otherwise.
	 */
	public boolean removeInstance(String name){
		return removeInstance(getInstance(name));
	}
	
	/**
	 * This method carefully removes an instance in a design with its
	 * pins and possibly nets.  Nets are only removed if they are empty
	 * after removal of the instance's pins. This method CANNOT remove 
	 * instances that are part of a ModuleInstance.
	 * @param instance The instance in the design to remove.
	 * @return True if the operation was successful, false otherwise.
	 */
	public boolean removeInstance(Instance instance){
		if(instance.getModuleInstance() != null){
			return false;
		}
		for(Pin p : instance.getPins()){
			p.getNet().unroute(); 
			if(p.getNet().getPins().size() == 1){
				netMap.remove(p.getNet().getName());
			}
			else{
				p.getNet().removePin(p);				
			}
		}
		instanceMap.remove(instance.getName());
		instance.setDesign(null);
		instance.setNetList(null);
		instance.setModuleTemplate(null);
		return true;
	}
	
	
	
	/**
	 * Sets the anchor instance for this module.
	 * @param anchor New anchor instance for this module.
	 */
	public void setAnchor(Instance anchor){
		this.anchor = anchor;
	}
	
	/**
	 * Gets and returns the port list for this module.
	 * @return The port list for this module.
	 */
	public Collection<Port> getPorts(){
		return portMap.values();
	}

	/**
	 * Sets the port list for this module.
	 * @param portList The new port list to be set for this module.
	 */
	public void setPorts(ArrayList<Port> portList){
		portMap.clear();
		for(Port p: portList){
			addPort(p);
		}
	}
	
	/**
	 * Adds a port to this module.
	 * @param port The new port to add.
	 */
	public void addPort(Port port){
		this.portMap.put(port.getName(), port);
	}
	
	/**
	 * Returns the port with the given name.
	 * @param name the port's name
	 * @return the port
	 */
	public Port getPort(String name){
		return this.portMap.get(name);
	}
	
	/**
	 * Adds a net to this module.
	 * @param net The net to add to the module.
	 */
	public void addNet(Net net){
		this.netMap.put(net.getName(), net);
	}
	
	/**
	 * Adds an instance to this module.
	 * @param inst The instance to add to the module.
	 */
	public void addInstance(Instance inst){
		this.instanceMap.put(inst.getName(), inst);
	}
	
	/**
	 * @return the metaDataMap
	 */
	public HashMap<String, ArrayList<String>> getMetaDataMap() {
		return metaDataMap;
	}

	/**
	 * @param metaDataMap the metaDataMap to set
	 */
	public void setMetaDataMap(HashMap<String, ArrayList<String>> metaDataMap) {
		this.metaDataMap = metaDataMap;
	}

	/**
	 * @param minClkPeriod the minClkPeriod to set
	 */
	public void setMinClkPeriod(float minClkPeriod) {
		this.minClkPeriod = minClkPeriod;
	}

	/**
	 * @return the minClkPeriod
	 */
	public float getMinClkPeriod() {
		return minClkPeriod;
	}

	/**
	 * Sets the design in all the module's instances to null
	 */
	public void disconnectDesign(){
		for(Instance i: this.getInstances()){
			i.setDesign(null);
		}
	}

	/**
	 * Does a brute force search to find all valid locations of where this module
	 * can be placed.
	 * @return A list of valid anchor sites for the module to be placed.
	 */
	public ArrayList<PrimitiveSite> calculateAllValidPlacements(Device dev){
		if(getAnchor() == null) return null;
		ArrayList<PrimitiveSite> validSites = new ArrayList<PrimitiveSite>();
		PrimitiveSite[] sites = dev.getAllCompatibleSites(getAnchor().getType());
		for(PrimitiveSite newAnchorSite : sites){
			if(isValidPlacement(newAnchorSite, dev)){
				validSites.add(newAnchorSite);
			}
		}
		this.validPlacements = validSites;
		return validSites;
	}
	
	/**
	 * Gets the previously calculated valid placement locations for this particular module.
	 * @return A list of anchor primitive sites which are valid for this module.
	 */
	public ArrayList<PrimitiveSite> getAllValidPlacements(){
		return this.validPlacements;
	}
	
	public boolean isValidPlacement(PrimitiveSite proposedAnchorSite, Device dev){
		// Check if parameters are null
		if(proposedAnchorSite == null || dev == null){
			return false;
		}

		// Do some error checking on the newAnchorSite
		PrimitiveSite p = anchor.getPrimitiveSite();
		Tile t = proposedAnchorSite.getTile();
		PrimitiveSite newValidSite = Device.getCorrespondingPrimitiveSite(p, anchor.getType(), t);
		if(!proposedAnchorSite.equals(newValidSite)){
			return false;
		}
		
		//=======================================================//
		/* Check instances at proposed location                  */
		//=======================================================//
		for(Instance inst : getInstances()){
			PrimitiveSite templateSite = inst.getPrimitiveSite();
			Tile newTile = getCorrespondingTile(templateSite.getTile(), proposedAnchorSite.getTile(), dev);
			if(newTile == null){
				return false;
			}
			if(Device.getCorrespondingPrimitiveSite(templateSite, inst.getType(), newTile) == null){
				return false;
			}
		}
		
		//=======================================================//
		/* Check nets at proposed location                       */
		//=======================================================//
		for(Net net : getNets()){
			for(PIP pip : net.getPIPs()){
				if(getCorrespondingTile(pip.getTile(), proposedAnchorSite.getTile(), dev) == null){
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * This method will calculate and return the corresponding tile of a module
	 * for a new anchor location.
	 * @param templateTile The tile in the module which acts as a template.
	 * @param newAnchorTile This is the tile of the new anchor instance of the module.
	 * @param dev The device which corresponds to this module.
	 * @return The new tile of the module instance which corresponds to the templateTile, or null
	 * if none exists.
	 */
	public Tile getCorrespondingTile(Tile templateTile, Tile newAnchorTile, Device dev){
		int tileXOffset = templateTile.getTileXCoordinate() - anchor.getTile().getTileXCoordinate();
		int tileYOffset = templateTile.getTileYCoordinate() - anchor.getTile().getTileYCoordinate();
		int newTileX = newAnchorTile.getTileXCoordinate() + tileXOffset;
		int newTileY = newAnchorTile.getTileYCoordinate() + tileYOffset;
		String oldName = templateTile.getName();
		String newName = oldName.substring(0, oldName.lastIndexOf('X')+1) + newTileX + "Y" + newTileY;
		Tile correspondingTile = dev.getTile(newName); 
		if(correspondingTile == null){
			if(templateTile.getType().equals(TileType.CLBLL)){
				correspondingTile = dev.getTile("CLBLM_X" + newTileX + "Y" + newTileY);
			}else if(templateTile.getType().equals(TileType.CLBLM)){
				correspondingTile = dev.getTile("CLBLL_X" + newTileX + "Y" + newTileY);
			}
		}
		return correspondingTile;
	}
	
	/**
	 * Generates the hashCode strictly on the module name.
	 */
	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	/**
	 *  Checks if two modules are equal based on the name of the module.
	 */
	@Override
	public boolean equals(Object obj){
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		Module other = (Module) obj;
		if(name == null){
			if(other.name != null)
				return false;
		}
		else if(!name.equals(other.name))
			return false;
		return true;
	}
	
	public String toString(){
		return name;
	}
}
