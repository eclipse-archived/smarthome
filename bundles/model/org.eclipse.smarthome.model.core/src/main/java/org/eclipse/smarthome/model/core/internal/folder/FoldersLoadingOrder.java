/**
 * 
 */
package org.eclipse.smarthome.model.core.internal.folder;

/**
 * Defines the loading order of configuration folders
 * @author Fabio Marini
 */
public enum FoldersLoadingOrder {
	/**
	 * Persistence folder <br/>
	 * The first one loaded
	 */
	PERSISTENCE("persistence"),
	
	/**
	 * Items folder
	 */
	ITEMS("items"),
	
	/**
	 * Scripts folder
	 */
	SCRIPTS("scripts"),
	
	/**
	 * Rules folder
	 */
	RULES("rules"),
	
	/**
	 * Sitemaps folder<br/>
	 * The last one loaded
	 */
	SITEMAPS("sitemaps");
	
	private String folderName;
	
	/**
	 * Build the enum item
	 * @param folderName folder name
	 */
	private FoldersLoadingOrder(String folderName)
	{
		this.folderName = folderName;
	}

	public String getFolderName() {
		return folderName;
	}
}
