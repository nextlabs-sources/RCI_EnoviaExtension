/**
 * NextLabs Enovia EM RCI Export Control Extension implementation
 */
package com.nextlabs.enovia.extension.impl;

import java.util.Map;

import matrix.db.Context;
import matrix.util.StringList;

import org.apache.log4j.Logger;

import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.MapList;
import com.nextlabs.enovia.extension.NextLabsEnoviaEMAttributeExtension;
import com.rockwellcollins.classificationrepository.CitizenshipResult;
import com.rockwellcollins.classificationrepository.ClassificationRepositoryAPIException;
import com.rockwellcollins.pdm.nextlabs.EICUtils;

public class RCIExportControlExtension implements NextLabsEnoviaEMAttributeExtension {
	
private static RCIExtensionLogger logger = null;
	
	static {
		logger = new RCIExtensionLogger(Logger.getLogger("RCIEXTLOGGER"));
	}

	@SuppressWarnings("unchecked")
	public MapList getData(Context context, DomainObject domObj) {
		MapList data = new MapList();
		RCIExtensionUtil rciExtensionUtil = new RCIExtensionUtil();
		
		String eventId = rciExtensionUtil.getEventId(context.getUser(), System.nanoTime());
		
		long startTime = System.currentTimeMillis();
		
		// citizenship check
		boolean isCitizenshipConfirmed = RCIExtensionConstant.CITIZENSHIP_CONFIRMED_DEFAULT_VALUE;
		boolean isClassificationAPIFault = false;
		
		try {
			logger.debug("RCIExportControlExtension getData() - Checking citizenship...");
			CitizenshipResult citizenshipResult = getCitizenshipResult(context.getUser());
			
			isCitizenshipConfirmed = citizenshipResult.IsConfirmed;
			
			if (citizenshipResult.ResultCode != RCIExtensionConstant.CITIZENSHIP_CONFIRMED_SUCCESS) {
				logger.info("RCIExportControlExtension getData() - Citizenship ResultCode is " + 
						citizenshipResult.ResultCode + " and Message is" + citizenshipResult.ResultMessage);
			}
		} catch (ClassificationRepositoryAPIException ex) {
			logger.error("RCIExportControlExtension getData() caught exception : %s", ex, ex.getMessage());
			
			isClassificationAPIFault = true;
		}
		
		try {
			String nameId = domObj.getName() + ":" + domObj.getId();
			
			// get export control
			logger.debug("RCIExportControlExtension getData() - Getting export control...");
			StringList lstExportControl = EICUtils.getExportControl(context, domObj);
			String exportControls = rciExtensionUtil.convertStrListToStr(lstExportControl);
			
			if (exportControls.trim().equals("")) {
				logger.debug("RCIExportControlExtension getData() %s - Export control NO RESULT", nameId);
			} else {
				logger.debug("RCIExportControlExtension getData() %s - Export control %s", nameId, exportControls);
			}
			
			// retrieve the compulsory attributes
			StringList selectList = new StringList();
			selectList.addElement("id");
			selectList.addElement("type");
			selectList.addElement("name");
			selectList.addElement("revision");
			selectList.addElement("vault");
			selectList.addElement("description");

			Map<String, Object> map = domObj.getInfo(context, selectList);
			
			map.put(RCIExtensionConstant.EXPORT_CONTROL, exportControls);
			
			// add in citizenship check
			map.put(RCIExtensionConstant.CITIZENSHIP_CONFIRMED, String.valueOf(isCitizenshipConfirmed));
			
			// add in event id
			map.put(RCIExtensionConstant.EVENT_ID, eventId);
			
			if (isClassificationAPIFault) {
				map.put(RCIExtensionConstant.CLASSIFICATION_API_EXCEPTION, String.valueOf(isClassificationAPIFault));
			}
			
			data.add(map);
			
			if (logger.isNXLDebugEnable)
				rciExtensionUtil.printData(data);
		} catch (Exception ex) {
			logger.error("RCIExportControlExtension getData() caught exception : %s", ex, ex.getMessage());
		}
		
		long endTime = System.currentTimeMillis();
		logger.debug("RCIExportControlExtension getData() is complete within " + 
				(endTime - startTime) + " ms");
		
		return data;
	}
	
	// return CitizenshipResult from IClassificationRepositoryAPI
	private CitizenshipResult getCitizenshipResult(String username) 
			throws ClassificationRepositoryAPIException {
		RCIClassificationAgent rciClassificationAgent = new RCIClassificationAgent();
		long startTime = System.currentTimeMillis();
		
		CitizenshipResult citizenshipResult = rciClassificationAgent.getCitizenshipResult(username);
		
		long endTime = System.currentTimeMillis();
		logger.debug("RCIExportControlExtension getCitizenshipResult() is complete within " + 
				(endTime - startTime) + " ms");
		
		return citizenshipResult;
	}

}
