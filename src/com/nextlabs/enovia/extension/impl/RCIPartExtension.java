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
import com.rockwellcollins.classificationrepository.PartsClassificationResult;
import com.rockwellcollins.pdm.nextlabs.EICUtils;

public class RCIPartExtension implements NextLabsEnoviaEMAttributeExtension {
	
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
			logger.debug("RCIPartExtension getData() - Checking citizenship...");
			CitizenshipResult citizenshipResult = getCitizenshipResult(context.getUser());
			
			isCitizenshipConfirmed = citizenshipResult.IsConfirmed;
			
			if (citizenshipResult.ResultCode != RCIExtensionConstant.CITIZENSHIP_CONFIRMED_SUCCESS) {
				logger.info("RCIPartExtension getData() - Citizenship ResultCode is " + 
						citizenshipResult.ResultCode + " and Message is" + citizenshipResult.ResultMessage);
			}
		} catch (ClassificationRepositoryAPIException ex) {
			logger.error("RCIPartExtension getData() caught exception : %s", ex, ex.getMessage());
			
			isClassificationAPIFault = true;
		}
		
		try {
			String nameId = domObj.getName() + ":" + domObj.getId();
			
			// check structure
			logger.debug("RCIPartExtension getData() - Checking structure...");
			boolean isStructureValid = EICUtils.checkStructure(context, domObj); 
			logger.debug("RCIPartExtension getData() %s - StructureValid is %s", 
					nameId, String.valueOf(isStructureValid));
						
			// get export control
			logger.debug("RCIPartExtension getData() - Getting export control...");
			StringList lstExportControl = EICUtils.getExportControl(context, domObj);
			String exportControls = rciExtensionUtil.convertStrListToStr(lstExportControl);
			
			if (exportControls.trim().equals("")) {
				logger.debug("RCIPartExtension getData() %s - Export control NO RESULT", nameId);
			} else {
				logger.debug("RCIPartExtension getData() %s - Export control %s", nameId, exportControls);
			}
			
			// in part extension, related parts will only contain one part
			// using the existing data structure (MapList)
			MapList relatedParts = new MapList();
			
			// retrieve the compulsory attributes
			StringList selectList = new StringList();
			selectList.addElement("id");
			selectList.addElement("type");
			selectList.addElement("name");
			selectList.addElement("revision");
			selectList.addElement("vault");
			selectList.addElement("description");

			Map<String, Object> map = domObj.getInfo(context, selectList);
			
			logger.debug("RCIPartExtension getData() %s - part: %s", nameId, map);
			
			// add in structure validation result
			map.put(RCIExtensionConstant.STRUCTURE_VALID, String.valueOf(isStructureValid));
			
			// add in export control result
			map.put(RCIExtensionConstant.EXPORT_CONTROL, exportControls);
			
			// add in citizenship check
			map.put(RCIExtensionConstant.CITIZENSHIP_CONFIRMED, String.valueOf(isCitizenshipConfirmed));
			
			// add in event id
			map.put(RCIExtensionConstant.EVENT_ID, eventId);
						
			if (isClassificationAPIFault) {
				map.put(RCIExtensionConstant.CLASSIFICATION_API_EXCEPTION, String.valueOf(isClassificationAPIFault));
			}
			
			relatedParts.add(map);
			
			// there will be only one part for classification
			String[] partNumbers = new String[1];
			partNumbers[0] = domObj.getName();
			
			try {
				logger.debug("RCIPartExtension getData() - Getting classification for partNumbers...");
				PartsClassificationResult partsClassificationResult = getClassification(partNumbers);
				
				logger.debug("RCIPartExtension getData() %s - Parts classification result size is %d", 
						nameId, partsClassificationResult.PartClassificationRecords.length);
				
				relatedParts = rciExtensionUtil.setClassificationResult(relatedParts, 
						partsClassificationResult);
			} catch (ClassificationRepositoryAPIException cre) {
				logger.error("RCIPartExtension getData() caught exception : %s", cre, cre.getMessage());
				
				relatedParts = rciExtensionUtil.setClassificationFault(relatedParts, true);
			}
			
			data = relatedParts;
			
			if (logger.isNXLDebugEnable)
				rciExtensionUtil.printData(data);
		} catch (Exception ex) {
			logger.error("RCIPartExtension getData() caught exception : %s", ex, ex.getMessage());
		}
				
		long endTime = System.currentTimeMillis();
		logger.debug("RCIPartExtension getData() is complete within " + 
				(endTime - startTime) + " ms");
		
		return data;
	}
	
	// return PartsClassificationResult from IClassificationRepositoryAPI
	private PartsClassificationResult getClassification(String[] partNumbers) 
			throws ClassificationRepositoryAPIException {
		RCIClassificationAgent rciClassificationAgent = new RCIClassificationAgent();
		long startTime = System.currentTimeMillis();
		
		PartsClassificationResult partClassificationResult = 
				rciClassificationAgent.getClassification(partNumbers);
		
		long endTime = System.currentTimeMillis();
		logger.debug("RCIPartExtension getClassification() is complete within " + 
				(endTime - startTime) + " ms");
        
		return partClassificationResult;
	}
	
	// return CitizenshipResult from IClassificationRepositoryAPI
	private CitizenshipResult getCitizenshipResult(String username) 
			throws ClassificationRepositoryAPIException {
		RCIClassificationAgent rciClassificationAgent = new RCIClassificationAgent();
		long startTime = System.currentTimeMillis();
		
		CitizenshipResult citizenshipResult = rciClassificationAgent.getCitizenshipResult(username);
		
		long endTime = System.currentTimeMillis();
		logger.debug("RCICADModelExtension getCitizenshipResult() is complete within " + 
				(endTime - startTime) + " ms");
		
		return citizenshipResult;
	}

}
