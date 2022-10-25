/**
 * NextLabs Enovia EM RCI CAD Model Extension implementation
 */
package com.nextlabs.enovia.extension.impl;

import matrix.db.Context;
import matrix.util.StringList;

import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.DomainObject;
import com.nextlabs.enovia.extension.NextLabsEnoviaEMAttributeExtension;
import com.rockwellcollins.classificationrepository.CitizenshipResult;
import com.rockwellcollins.classificationrepository.ClassificationRepositoryAPIException;
import com.rockwellcollins.classificationrepository.PartsClassificationResult;
import com.rockwellcollins.pdm.nextlabs.EICUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class RCICADModelExtension implements NextLabsEnoviaEMAttributeExtension {
	
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
			logger.debug("RCICADModelExtension getData() - Checking citizenship...");
			CitizenshipResult citizenshipResult = getCitizenshipResult(context.getUser());
			
			isCitizenshipConfirmed = citizenshipResult.IsConfirmed;
			
			if (citizenshipResult.ResultCode != RCIExtensionConstant.CITIZENSHIP_CONFIRMED_SUCCESS) {
				logger.info("RCICADModelExtension getData() - Citizenship ResultCode is " + 
						citizenshipResult.ResultCode + " and Message is" + citizenshipResult.ResultMessage);
			}
		} catch (ClassificationRepositoryAPIException ex) {
			logger.error("RCICADModelExtension getData() caught exception : %s", ex, ex.getMessage());
			
			isClassificationAPIFault = true;
		}
		
		try {
			String nameId = domObj.getName() + ":" + domObj.getId();
						
			// check structure
			logger.debug("RCICADModelExtension getData() - Checking structure...");
			boolean isStructureValid = EICUtils.checkStructure(context, domObj); 
			logger.debug("RCICADModelExtension getData() %s - StructureValid is %s", 
					nameId, String.valueOf(isStructureValid));
			
			// get export control
			logger.debug("RCICADModelExtension getData() - Getting export control...");
			StringList lstExportControl = EICUtils.getExportControl(context, domObj);
			String exportControls = rciExtensionUtil.convertStrListToStr(lstExportControl);
			
			if (exportControls.trim().equals("")) {
				logger.debug("RCICADModelExtension getData() %s - Export control NO RESULT", nameId);
			} else {
				logger.debug("RCICADModelExtension getData() %s - Export control %s", nameId, exportControls);
			}
			
			// get related Parts
			logger.debug("RCICADModelExtension getData() - Getting related parts...", 
					nameId);
			
			MapList relatedParts = EICUtils.getRelatedParts(context, domObj);
			ArrayList<String> lstPartNumber = new ArrayList<String>();
			
			logger.debug("RCICADModelExtension getData() %s - relatedParts isNull=%s", 
					nameId, String.valueOf(relatedParts == null));
			
			if (relatedParts != null && relatedParts.size() > 0) {
				logger.debug("RCICADModelExtension getData() %s - relatedParts size is %d", 
						nameId, relatedParts.size());
				
				for (int i = 0; i < relatedParts.size(); i++) {
					// adding attribute to each related part for multiple query
					
					Map<String, Object> map = (Map<String, Object>) relatedParts.get(i);
					logger.debug("RCICADModelExtension getData() %s - relatedParts %d: %s", nameId, i, map);
			
					// add in structure validation result
					map.put(RCIExtensionConstant.STRUCTURE_VALID, String.valueOf(isStructureValid));
					
					// add in export control result
					map.put(RCIExtensionConstant.EXPORT_CONTROL, exportControls);
					
					// add in vault
					map.put(RCIExtensionConstant.VAULT, map.get(RCIExtensionConstant.VAULT));
					
					// add in citizenship check
					map.put(RCIExtensionConstant.CITIZENSHIP_CONFIRMED, String.valueOf(isCitizenshipConfirmed));
					
					// add in event id
					map.put(RCIExtensionConstant.EVENT_ID, eventId);
					
					if (map.containsKey(RCIExtensionConstant.PART_NUMBER)) {
						lstPartNumber.add(
								map.get(RCIExtensionConstant.PART_NUMBER).toString());
					}
				}
				
				logger.debug("RCICADModelExtension getData() %s - lstPartNumber size is %d", 
						nameId, lstPartNumber.size());
				
				String[] partNumbers = new String[lstPartNumber.size()];
				partNumbers = lstPartNumber.toArray(partNumbers);
				
				try {
					logger.debug("RCICADModelExtension getData() - Getting classification for partNumbers...");
					PartsClassificationResult partsClassificationResult = getClassification(partNumbers);
					
					logger.debug("RCICADModelExtension getData() %s - Parts classification result size is %d", 
							nameId, partsClassificationResult.PartClassificationRecords.length);
					
					relatedParts = rciExtensionUtil.setClassificationResult(relatedParts, 
							partsClassificationResult);
				} catch (ClassificationRepositoryAPIException cre) {
					logger.error("RCICADModelExtension getData() caught exception : %s", cre, cre.getMessage());
					
					relatedParts = rciExtensionUtil.setClassificationFault(relatedParts, true);
				}
				
				data = relatedParts;
			} else {
				// if there is no related part, 
				// user classification likes citizenship_confirmed will still be added.
				Map<String, Object> map = new HashMap<String, Object>();
				
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
								
				data.add(map);
			}
			
			if (logger.isNXLDebugEnable)
				rciExtensionUtil.printData(data);
		} catch (Exception ex) {
			logger.error("RCICADModelExtension getData() caught exception : %s", ex, ex.getMessage());
		}
				
		long endTime = System.currentTimeMillis();
		logger.debug("RCICADModelExtension getData() is complete within " + 
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
		logger.debug("RCICADModelExtension getClassification() is complete within " + 
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
