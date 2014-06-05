package org.eclipse.smarthome.config.discovery;


/**
 * The {@link DiscoveryResultFlag} class specifies a list of flags
 * which a {@link DiscoveryResult} object can take.
 *
 * @author Michael Grammling - Initial Contribution.
 *
 * @see DiscoveryResult
 */
public enum DiscoveryResultFlag {

    /**
     * The flag {@code NEW} to signal that the result object should be regarded
     * as <i>new</i> by the system so that a further processing should be applied.
     */
    NEW,

    /**
     * The flag {@code IGNORED} to signal that the result object should be regarded
     * as <i>known</i> by the system so that a further processing should be skipped.
     */
    IGNORED;

}
