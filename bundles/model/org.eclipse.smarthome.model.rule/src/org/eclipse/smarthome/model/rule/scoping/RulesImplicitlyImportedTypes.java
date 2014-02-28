package org.eclipse.smarthome.model.rule.scoping;

import org.eclipse.smarthome.model.script.scoping.ScriptImplicitlyImportedTypes;

import com.google.inject.Singleton;

/**
 * This class registers all statically available functions as well as the
 * extensions for specific jvm types, which should only be available in rules,
 * but not in scripts
 * 
 * @author Kai Kreuzer - Initial contribution and API
 * @author Oliver Libutzki - Xtext 2.5.0 migration
 *
 */

@Singleton
public class RulesImplicitlyImportedTypes extends ScriptImplicitlyImportedTypes {

}
