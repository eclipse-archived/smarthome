/*******************************************************************************
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH
 * http://www.prosyst.com
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ProSyst Software GmbH - initial API and implementation
 *******************************************************************************/

package org.eclipse.smarthome.automation.core.provider;

/**
 * This class is designed to serves as a holder of most significant information for a bundle that provides resources
 * for automation objects - bundle ID and bundle version. These two features of the bundle, define it uniquely and
 * determine if the bundle was updated, which needs to be checked after the system has been restarted.
 * 
 * @author Ana Dimova - Initial Contribution
 * 
 */
public class Vendor {

    /**
     * This field provides a bundle ID of a bundle that provides resources for automation objects.
     */
    private String vendorId;

    /**
     * This field provides a bundle version of a bundle that provides resources for automation objects.
     */
    private String vendorVersion;

    /**
     * This constructor initialize the {@link vendorId} and the {@link vendorVersion} of the vendor with corresponding
     * bundle ID and bundle version of a bundle that provides resources for the automation objects.
     * 
     * @param id a bundle ID of a bundle that providing resources for automation objects.
     * @param version a bundle version of a bundle that provides resources for the automation objects.
     */
    public Vendor(String id, String version) {
        vendorId = id;
        vendorVersion = version;
    }

    /**
     * Getter of {@link vendorId}.
     * 
     * @return a bundle ID of a bundle that provides resources for the automation objects.
     */
    public String getVendorId() {
        return vendorId;
    }

    /**
     * Getter of {@link vendorVersion}.
     * 
     * @return a bundle version of a bundle that provides resources for the automation objects.
     */
    public String getVendorVersion() {
        return vendorVersion;
    }

    /**
     * Setter of {@link vendorVersion}.
     * 
     * @param version a bundle version of a bundle that provides resources for the automation objects.
     */
    public void setVendorVersion(String version) {
        vendorVersion = version;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Vendor) {
            Vendor other = (Vendor) obj;
            return vendorId.endsWith(other.vendorId) && vendorVersion.equals(other.vendorVersion);
        }
        return false;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return vendorId.hashCode() + vendorVersion.hashCode();
    }

}
