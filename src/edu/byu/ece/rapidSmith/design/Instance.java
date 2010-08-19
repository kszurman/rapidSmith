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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;

import edu.byu.ece.rapidSmith.device.PrimitiveSite;
import edu.byu.ece.rapidSmith.device.PrimitiveType;
import edu.byu.ece.rapidSmith.device.Tile;

/**
 * This class represents the inst object in XDL design files.  It can determine
 * if an instance is placed/unplaced, its name, attributes, module instance and
 * also maintains a net list of which nets connect to the instance pins.
 * @author Chris Lavin
 * Created on: Jun 22, 2010
 */
public class Instance implements Serializable{

	private static final long serialVersionUID = -7993281723620318536L;

	/** Unique name of this instance */
	private String name;
	/** The XDL Design this instance belongs to */
	private transient Design design;
	/** All of the attributes in this instance */
	private ArrayList<Attribute> attributes;
	/** Type of the instance (e.g. "SLICEM" or "SLICEL") */
	private PrimitiveType type;
	/** When an instance is unplaced, it might be bonded (true) or unbonded (false) */
	private Boolean bonded;
	/** This is a site of where the primitive will reside */
	private PrimitiveSite site;
	/** A list of nets to which this instance is connected */
	private HashSet<Net> netList;
	/** Name of the module instance which this instance belongs to */
	private ModuleInstance moduleInstance;
	/** The module template (or definition) this instance is a member of */
	private Module moduleTemplate;
	/** The instance in the module template corresponding to this instance */
	private Instance moduleTemplateInstance;
	/** The X coordinate of the instance (ex: SLICE_X#Y5) */
	private int instanceX;
	/** The Y coordinate of the instance (ex: SLICE_X5Y#) */
	private int instanceY;
	
	/**
	 * Creates a new Instance, everything is empty, false or -1
	 */
	public Instance(){
		name = null;
		design = null;
		attributes = new ArrayList<Attribute>();
		type = null;
		bonded = null;
		site = null;
		netList = new HashSet<Net>();
		
		// Null unless this instance is of a module
		this.moduleInstance = null;
		this.moduleTemplate = null;
		this.moduleTemplateInstance = null;
	}
	
	/**
	 * Creates a new Instance called name. Apart from the name,  
	 * the new Instance is an exact copy of inst.
	 * @param name name of the new Instance.
	 * @param type The instance type of the new instance. 
	 */
	public Instance(String name, PrimitiveType type){
		this.name = name;
		this.type = type;
		
		design = null;
		attributes = new ArrayList<Attribute>();
		bonded = null;
		site = null;
		netList = new HashSet<Net>();
		
		// Null unless this instance is of a module
		this.moduleInstance = null;
		this.moduleTemplate = null;
		this.moduleTemplateInstance = null;
	}

	/**
	 * Gets and returns the current attributes of this instance
	 * @return The current attributes of this instance
	 */
	public ArrayList<Attribute> getAttributes(){
		return attributes;
	}
	
	/**
	 * Adds the attribute with value to this instance.
	 * @param physicalName Physical name of the attribute.
	 * @param value Value to set the new attribute to.
	 */
	public void addAttribute(String physicalName, String logicalName, String value){
		attributes.add(new Attribute(physicalName, logicalName, value));
	}
	
	/**
	 * Add the attribute to this instance.
	 * @param attribute The attribute to add.
	 */
	public void addAttribute(Attribute attribute){
		attributes.add(attribute);
	}
	
	/**
	 * Checks if the design attribute has an attribute with a physical
	 * name called phyisicalName.  
	 * @param physicalName The physical name of the attribute to check for.
	 * @return True if this instance contains an attribute with the 
	 * physical name physicalName, false otherwise.
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
	 * Sets the list of attributes for this instance.
	 * @param attributes The new list of attributes to associate with this
	 * instance.
	 */
	public void setAttributes(ArrayList<Attribute> attributes){
		this.attributes = attributes;
	}
	
	/**
	 * Gets the name of the instance of the module this Instance is a member of.
	 * @return The name of the instance of the module.
	 */
	public String getModuleInstanceName(){
		return moduleInstance != null ? moduleInstance.getName() : null;
	}

	/**
	 * Sets the module instance  of this instance (the instance of a 
	 * module to which this instance belongs).  
	 * @param instanceModuleName The name of the module instance.
	 */
	public void setModuleInstance(ModuleInstance instanceModule){
		this.moduleInstance = instanceModule;
	}
	
	/**
	 * Checks if the instance inst and this instance are members of 
	 * the same module instance.
	 * @param inst The instance to check.
	 * @return True if both instances are members of the same module instance
	 * false otherwise.
	 */
	public boolean isMemberOfSameModuleInstance(Instance inst){
		return this.moduleInstance.equals(inst.moduleInstance);
	}
	
	/**
	 * Gets the module template this instance is a member of
	 * @return The module template this instance is a member of
	 */
	public Module getModuleTemplate(){
		return moduleTemplate;
	}

	/**
	 * Sets the module class this instance implements.
	 * @param instanceModuleTemplate The module which this instance implements.
	 */
	public void setModuleTemplate(Module instanceModuleTemplate){
		this.moduleTemplate = instanceModuleTemplate;
	}

	/**
	 * Sets the corresponding instance inside a module template of this instance.
	 * @param moduleTemplateInstance The instance in the module to which this
	 * instance corresponds.
	 */
	public void setModuleTemplateInstance(Instance moduleTemplateInstance){
		this.moduleTemplateInstance = moduleTemplateInstance;
	}

	/**
	 * Gets and returns the instance found in the module which this instance implements.
	 * @return The instance found in the module which this instance implements.
	 */
	public Instance getModuleTemplateInstance(){
		return moduleTemplateInstance;
	}

	/**
	 * Gets the user name of the instance
	 * @return The user assigned name of the instance
	 */
	public String getName(){
		return this.name;
	}
	
	/**
	 * Gets the type of the instance (such as "SLICEL" or "SLICEM")
	 * @return A string of the type surrounded by quotes
	 */
	public PrimitiveType getType(){
		return type;
	}
	
	/**
	 * Determines if the instance is placed or unplaced
	 * @return True if the instance is placed, false otherwise
	 */
	public Boolean isPlaced(){
		return getPrimitiveSite() != null;
	}
	
	/**
	 * When an instance is unplaced, it could be bonded (true) or unbonded (false)
	 * @return True if bonded, false if unbonded, null otherwise.
	 */
	public Boolean getBonded(){
		return bonded;
	}
	
	/**
	 * Gets the tile object where this instance resides on the chip
	 * @return The tile object where this instance resides
	 */
	public Tile getTile(){
		return site == null ? null : site.getTile();
	}
	
	/**
	 * Gets the string that defines the instance location (such as 'SLICE_X0Y0')
	 * @return A string of the instance and its X,Y location
	 */
	public String getPrimitiveSiteName(){
		if(site == null){
			return null;
		}
		return site.getName();
	}

	/**
	 * Gets the PrimitiveSite for this instance.
	 * @return The PrimitiveSite or null if it is not placed.
	 */
	public PrimitiveSite getPrimitiveSite(){
		return site;
	}
	
	/**
	 * Sets the design which this instance is a part of.
	 * @param design the new design of this instance.
	 */
	public void setDesign(Design design){
		this.design = design;
	}

	/**
	 * Gets and returns the design which this instance is a part of.
	 * @return the design this instance is a part of.
	 */
	public Design getDesign(){
		return design;
	}

	/**
	 * Sets the user name of the instance
	 * @param name The desired name for the instance
	 */
	public void setName(String name){
		this.name = name;
	}
	
	/**
	 * Sets the instance type
	 * @param type The desired type for the instance
	 */
	public void setType(PrimitiveType type){
		this.type = type;
	}
	
	/**
	 * Sets the bonded parameter for this instance
	 * @param bonded True if bonded, false if unbonded, or null otherwise.
	 */
	public void setBonded(Boolean bonded){
		this.bonded = bonded;
	}
	
	/**
	 * Places the primitive at the site specified.
	 * @param site The site where the instance will reside
	 */
	public void place(PrimitiveSite site){
		if(this.site != null && design != null){
			design.releasePrimitiveSite(site);
		}
		setPrimitiveSite(site);
		setBonded(null);
		if(design != null) design.setPrimitiveSiteUsed(site, this);
	}
	
	/**
	 * Removes all placement information for the instance. 
	 */
	public void unPlace(){
		if(design != null) design.releasePrimitiveSite(site);
		setPrimitiveSite(null);
	}
	
	/**
	 * Sets the instance location string and updates X and Y accordingly (if applicable)
	 * @param loc The desired string to describe the instance location
	 */
	private void setPrimitiveSite(PrimitiveSite site){
		if(site == null){
			instanceX = -1;
			instanceY = -1;
			this.site = site;
			return;
		}
		String name = site.getName();
		// Populate the X and Y coordinates based on name
		int i = name.length();
		int end = i;
		
		// Find Primitive Y coordinate (if exists)
		while(i > 0 && name.charAt(i-1) != 'Y'){i--;}
		instanceY = i==0 ? -1 : Integer.parseInt(name.substring(i,end));
		end = i - 1;
		
		// Find Primitive X coordinate (if exists)
		while(i > 0 && name.charAt(i-1) != 'X'){i--;}
		instanceX = i==0 ? -1 : Integer.parseInt(name.substring(i,end));
		
		this.site = site;
	}

	/**
	 * Sets the net list which contain nets connecting to this instance.
	 * @param netList the netList to set
	 */
	public void setNetList(HashSet<Net> netList) {
		this.netList = netList;
	}

	/**
	 * Gets and returns the nets that connect to this instance and its pins.
	 * @return the netList The net list
	 */
	public HashSet<Net> getNetList() {
		return netList;
	}
	
	/**
	 * Gets and returns the integer X value of the instance location 
	 * (ex: SLICE_X5Y10, it will return 5).
	 * @return The X integer value of the site name.
	 */
	public int getInstanceX(){
		return instanceX;
	}

	/**
	 * Gets and returns the integer Y value of the instance location 
	 * (ex: SLICE_X5Y10, it will return 10).
	 * @return The Y integer value of the site name.
	 */
	public int getInstanceY(){
		return instanceY;
	}

	/**
	 * Adds the XDL_Net net to the end of the netList for this instance
	 * @param net The net to be added
	 */
	public void addToNetList(Net net){
		this.netList.add(net);
	}

	/**
	 * Generates a hash code for the instance based on instance name. Instance
	 * names should be unique throughout a design.
	 */
	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	/**
	 * Checks if this instance is equal to obj.  Checks only instance name 
	 * to determine equality as instance names should be unique throughout 
	 * a design.
	 */
	@Override
	public boolean equals(Object obj){
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		Instance other = (Instance) obj;
		if(name == null){
			if(other.name != null)
				return false;
		}
		else if(!name.equals(other.name))
			return false;
		return true;
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		String nl = System.getProperty("line.separator");
		sb.append("inst \"" + name + "\" \"" + type +"\",");
		if(isPlaced()){
			sb.append("placed " + getTile() + " " + site);
		}
		else{
			sb.append("unplaced");
		}
		if(moduleInstance != null){
			sb.append("module \"" + 
					getModuleInstanceName() + "\" \"" +
					getModuleTemplate().getName() + "\" \"" +
					getModuleTemplateInstance().getName() + "\" ,");
		}
		sb.append(nl + "  cfg \"");
		for(Attribute attr : attributes){
			sb.append(" " + attr.toString());
		}
		sb.append(" \"" + nl + "  ;" + nl);
		
		return sb.toString();
	}
}
