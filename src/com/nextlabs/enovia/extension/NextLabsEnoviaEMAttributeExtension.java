/**
 * NextLabs Enovia EM Attribute Extension Interface
 */
package com.nextlabs.enovia.extension;

import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.DomainObject;
import matrix.db.Context;

public interface NextLabsEnoviaEMAttributeExtension {
	
	MapList getData(Context context, DomainObject object);
	
}
