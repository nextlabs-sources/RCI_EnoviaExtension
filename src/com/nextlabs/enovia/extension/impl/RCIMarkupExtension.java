/**
 * NextLabs Enovia EM RCI Markup Extension implementation
 */
package com.nextlabs.enovia.extension.impl;

import matrix.db.Context;
import matrix.util.StringList;

import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.DomainConstants;
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

public class RCIMarkupExtension implements NextLabsEnoviaEMAttributeExtension {

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
			logger.debug("RCIMarkupExtension getData() - Checking citizenship...");
			CitizenshipResult citizenshipResult = getCitizenshipResult(context.getUser());
			
			isCitizenshipConfirmed = citizenshipResult.IsConfirmed;
			
			if (citizenshipResult.ResultCode != RCIExtensionConstant.CITIZENSHIP_CONFIRMED_SUCCESS) {
				logger.info("RCIMarkupExtension getData() - Citizenship ResultCode is " + 
						citizenshipResult.ResultCode + " and Message is" + citizenshipResult.ResultMessage);
			}
		} catch (ClassificationRepositoryAPIException ex) {
			logger.error("RCIMarkupExtension getData() caught exception : %s", ex, ex.getMessage());
			
			isClassificationAPIFault = true;
		}
		
		try {
			String nameId = domObj.getName() + ":" + domObj.getId();
			
			// check structure
			logger.debug("RCIMarkupExtension getData() - Checking structure...");
			boolean isStructureValid = EICUtils.checkStructure(context, domObj);
			logger.debug("RCIMarkupExtension getData() %s - StructureValid is %s", 
					nameId, String.valueOf(isStructureValid));
			
			// get export control
			logger.debug("RCIMarkupExtension getData() - Getting export control...");
			StringList lstExportControl = EICUtils.getExportControl(context, domObj);
			String exportControls = rciExtensionUtil.convertStrListToStr(lstExportControl);
			
			if (exportControls.trim().equals("")) {
				logger.debug("RCIMarkupExtension getData() %s - Export control NO RESULT", nameId);
			} else {
				logger.debug("RCIMarkupExtension getData() %s - Export control %s", nameId, exportControls);
			}
						
			// get related ECO
			logger.debug("RCIMarkupExtension getData() - Getting related ECO...");
			
			MapList relatedECOs = EICUtils.getRelatedECO(context, domObj);
			logger.debug("RCIMarkupExtension getData() %s - relatedECOs isNull=%s", 
					nameId, String.valueOf(relatedECOs == null));
			
			if (relatedECOs == null || relatedECOs.size() <= 0) {
				// if there is no related eco, 
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
				
				return data;
			}
			
			logger.debug("RCIMarkupExtension getData() %s - relatedECOs size is %d", nameId, relatedECOs.size());
			
			for (int j = 0; j < relatedECOs.size(); j++) {
				Map<String, Object> mapECO = (Map<String, Object>) relatedECOs.get(j);
				
				String sObjectID = (String) mapECO.get(DomainConstants.SELECT_ID);
				DomainObject dObject = new DomainObject(sObjectID);
				
				// get related Parts
				logger.debug("RCIMarkupExtension getData() - Getting related parts for ECO...");
				MapList relatedParts = EICUtils.getRelatedParts(context, dObject);
				ArrayList<String> lstPartNumber = new ArrayList<String>();
				
				logger.debug("RCIMarkupExtension getData() %s - relatedParts isNull=%s", 
						nameId, String.valueOf(relatedParts == null));
				
				if (relatedParts != null && relatedParts.size() > 0) {
					logger.debug("RCIMarkupExtension getData() %s - relatedParts size is %d", 
							nameId, relatedParts.size());
					
					for (int i = 0; i < relatedParts.size(); i++) {
						// adding attribute to each related part for multiple query
						
						Map<String, Object> mapPart = (Map<String, Object>) relatedParts.get(i);
						
						// add in structure validation result
						mapPart.put(RCIExtensionConstant.STRUCTURE_VALID, String.valueOf(isStructureValid));
						
						// add in export control result
						mapPart.put(RCIExtensionConstant.EXPORT_CONTROL, exportControls);
						
						// add in vault
						mapPart.put(RCIExtensionConstant.VAULT, mapPart.get(RCIExtensionConstant.VAULT));
						
						// add in citizenship check
						mapPart.put(RCIExtensionConstant.CITIZENSHIP_CONFIRMED, String.valueOf(isCitizenshipConfirmed));
						
						// add in event id
						mapPart.put(RCIExtensionConstant.EVENT_ID, eventId);
						
						if (mapPart.containsKey(RCIExtensionConstant.PART_NUMBER)) {
							lstPartNumber.add(
									mapPart.get(RCIExtensionConstant.PART_NUMBER).toString());
						}
					}
					
					logger.debug("RCIMarkupExtension getData() %s - lstPartNumber size is %d", 
							nameId, lstPartNumber.size());
					
					String[] partNumbers = new String[lstPartNumber.size()];
					partNumbers = lstPartNumber.toArray(partNumbers);
					
					try {
						logger.debug("RCIMarkupExtension getData() - Getting classification for partNumbers...");
						PartsClassificationResult partsClassificationResult = getClassification(partNumbers);
						
						logger.debug("RCIMarkupExtension getData() %s - Parts classification result size is %d", 
								nameId, partsClassificationResult.PartClassificationRecords.length);
						relatedParts = rciExtensionUtil.setClassificationResult(relatedParts, 
								partsClassificationResult);
					} catch (ClassificationRepositoryAPIException cre) {
						logger.error("RCIMarkupExtension getData() caught exception : %s", cre, cre.getMessage());
						
						relatedParts = rciExtensionUtil.setClassificationFault(relatedParts, true);
					}
					
					data.addAll(relatedParts);
					
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
				
			}
		} catch (Exception ex) {
			logger.error("RCIMarkupExtension getData() caught exception : %s", ex, ex.getMessage());
		}
		
		long endTime = System.currentTimeMillis();
		logger.debug("RCIMarkupExtension getData() is complete within " + 
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
		logger.debug("RCIMarkupExtension getClassification() is complete within " + 
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
		logger.debug("RCIMarkupExtension getCitizenshipResult() is complete within " + 
				(endTime - startTime) + " ms");
		
		return citizenshipResult;
	}
	
}
